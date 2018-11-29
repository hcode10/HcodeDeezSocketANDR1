package fr.urla.hcode.hcodedeezsocketandr;

import android.os.AsyncTask;
import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.ArrayList;

public class FtpBackground extends AsyncTask<String, String, String> {
    protected String doInBackground(String... args) {
        //Tableau qui va contenir les répertoire de premier niveau
        ArrayList<String> DirectoryList = new ArrayList<String>();

        FTPClient ftpClient = new FTPClient();

        try {
            ftpClient.connect(InetAddress.getByName("192.168.1.36"));
            ftpClient.login("hcode10", "ipod95110");
            ftpClient.changeWorkingDirectory("/");
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            ftpClient.enterLocalPassiveMode();

            FTPFile[] Directory = ftpClient.listDirectories();

            for (FTPFile file : Directory) {
                String details = file.getName();
                if(details.equals(".") || details.equals("..") || details.equals("????? ??"))continue;
                if (file.isDirectory()) {
                    details = "[" + details + "]";
                    Log.d("FTP", "Détails : " + details);


                    String final_dir = args[0] + "/" + file.getName();
                    File dir = new File(final_dir);

                    Log.d("FTP", "FinalDir : " + dir.getAbsolutePath());
                    if(dir.exists() && dir.isDirectory()) {
                        // do something here
                        readSubDirectory(file.getName(), final_dir, ftpClient);
                    } else {

                        dir.mkdir();
                        publishProgress("Création du dossier " + dir.getName());
                        readSubDirectory(file.getName(), final_dir, ftpClient);
                    }


                }


            }



            ftpClient.logout();
            ftpClient.disconnect();
        } catch(Exception ex){
            Log.d("FTP", "<font color='red'>Erreur : " + ex.getMessage() + "</font>");
        }

        return "";
    }

    private void readSubDirectory(String directoryName, String Actu_Directory, FTPClient ftpClient){
        if(directoryName.equals("????? ??"))return;
        try {
            ftpClient.changeWorkingDirectory("/" + directoryName + "/");
            //Log.d("FTP", "Working Directory is " + "/" + directoryName + "/");
            FTPFile[] Directory = ftpClient.listDirectories();

            for (FTPFile file : Directory) {
                String details = file.getName();
                if (file.isDirectory()) {
                    if(details.equals(".") || details.equals(".."))continue;
                    details = "[" + details + "]";
                    //Log.d("FTP", "+++++++++++++" + details);
                    //readSubDirectory(details, ftpClient);

                    String final_dir = Actu_Directory + "/" + file.getName();
                    File dir = new File(final_dir);

                    if(dir.exists() && dir.isDirectory()) {
                        // do something here
                        Log.d("FTP", "Remote path : " + ftpClient.printWorkingDirectory() + file.getName());
                        getMusic( ftpClient.printWorkingDirectory() + file.getName(), final_dir, ftpClient);
                    } else {
                        dir.mkdir();
                        getMusic( ftpClient.printWorkingDirectory() + file.getName(), final_dir, ftpClient);
                    }
                }


            }


        } catch (Exception ex){
            Log.d("FTP", "Erreur : " + ex.getMessage());
        }

    }

    private void getMusic(String directoryName, String Actu_Directory, FTPClient ftpClient){
        try {
            ftpClient.changeWorkingDirectory( directoryName );
            Log.d("FTP2", "Working Directory is " + ftpClient.printWorkingDirectory());

            FTPFile[] MusicFile = ftpClient.listFiles();

            for (FTPFile file : MusicFile) {
                if(file.getName().equals(".") || file.getName().equals(".."))continue;
                if (!file.isFile()) continue;
                String remoteFile1 = directoryName + "/" + file.getName();
                Log.d("FTP2", "+++++++++++++ Remote path is : " + remoteFile1);

                String final_dir = Actu_Directory + "/" + file.getName();
                File dir = new File(final_dir);

                if(dir.exists() && dir.isFile()){
                    publishProgress("Le fichier existe déjà !");
                    Log.d("FTP2", "Le fichier existe déjà !");
                    continue;
                }

                publishProgress("Téléchargement de " + dir.getName());
                File downloadFile1 = new File(dir.getAbsolutePath());
                OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(downloadFile1));
                Log.d("FTP2", "Start download : " + dir.getAbsolutePath());
                boolean success = ftpClient.retrieveFile(remoteFile1, outputStream1);
                if(success){
                    publishProgress("Téléchargement réussi !");
                    Log.d("FTP2", "Téléchargement réussi !");
                } else {
                    publishProgress("Erreur durant le Téléchargement !");
                    Log.d("FTP2", "Erreur durant le Téléchargement !");
                }

                outputStream1.close();

                Log.d("FTP2", "+++++++++++++ Remote path is : " + remoteFile1);
                Log.d("FTP2", "+++++++++++++ Final path is : " + dir.getAbsolutePath());
            }
        }catch (Exception e){
            Log.d("FTP", "Erreur : " + e.getMessage());
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        CharSequence s = MainActivity.mTextViewReplyFromServer.getText();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            MainActivity.mTextViewReplyFromServer.setText(Html.fromHtml(s + values[0],  Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE);
        } else {
            MainActivity.mTextViewReplyFromServer.setText(Html.fromHtml(s + values[0]), TextView.BufferType.SPANNABLE);
        }
        //MainActivity.mTextViewReplyFromServer.append(values[0] + "\r\n");
    }

    protected void onPostExecute(String result) {
        Log.d("FTP", "Re : " + result);
        MainActivity.mTextViewReplyFromServer.append("Téléchargement terminer !");
        //Where ftpClient is a instance variable in the main activity
    }
}
