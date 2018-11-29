package fr.urla.hcode.hcodedeezsocketandr;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import lib.folderpicker.FolderPicker;

public class MainActivity extends AppCompatActivity {

    public static TextView mTextViewReplyFromServer;
    public static ScrollView sview1;
    private String ServerIP = "192.168.1.36";
    public String pathZik;

    private static final String PREFS = "PREFS";
    private static final String PREFS_PATH_ZIK = "PREFS_PATH_ZIK";
    public static final String music_count = "music_count";
    public static  int total_music = 0;
    SharedPreferences sharedPreferences;

    private static final int SDCARD_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        Button buttonDownload = (Button) findViewById(R.id.btn_download);
        Button buttonUpdate = (Button) findViewById(R.id.update_button);
        Button buttonDateUpdate = (Button) findViewById(R.id.date_update);
        ProgressBar pg1 = (ProgressBar) findViewById(R.id.progressBar2);
        final ScrollView sview1 = (ScrollView) findViewById(R.id.scrollView2);

        pg1.setMax(100);
        pg1.setProgress(60, true);


        mTextViewReplyFromServer = (TextView) findViewById(R.id.tv_reply_from_server);

        buttonDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FtpBackground test = new FtpBackground();
                test.execute(pathZik);
            }
        });

        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("update");
            }
        });

        buttonDateUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("music_count");
            }
        });


        sharedPreferences = getBaseContext().getSharedPreferences(PREFS, MODE_PRIVATE);

        //pour cela, on commence par regarder si on a déjà des éléments sauvegardés
        if (sharedPreferences.contains(PREFS_PATH_ZIK)) {
            pathZik = sharedPreferences.getString(PREFS_PATH_ZIK, null);
        } else {
            if(checkStoragePermission()){
                Intent intent = new Intent(this, FolderPicker.class);
                intent.putExtra("location", Environment.getExternalStorageDirectory().getAbsolutePath());
                startActivityForResult(intent, 9999);
            } else {
                System.exit(0);
            }

        }


        sendMessage(music_count);

        mTextViewReplyFromServer.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {
                sview1.fullScroll(ScrollView.FOCUS_DOWN);
                // you can add a toast or whatever you want here
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {
                //override stub
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
                //override stub
            }

        });

    }

    private void sendMessage(final String msg) {

        final Handler handler = new Handler();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    //Replace below IP with the IP of that device in which server socket open.
                    //If you change port then change the port number in the server side code also.
                    Socket s = new Socket(ServerIP, 11000);

                    OutputStream out = s.getOutputStream();

                    PrintWriter output = new PrintWriter(out);

                    output.println(msg + "<EOF>");
                    output.flush();
                    BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    final String st = input.readLine();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {


                            String s = mTextViewReplyFromServer.getText().toString();
                            if(st != null) {


                                if (st.trim().length() != 0) {
                                    try {
                                        String splited_msg[] = st.split("@");
                                        String id = splited_msg[0].trim();
                                        String msg = splited_msg[1].trim();

                                        if (id.equals(music_count)) {
                                            total_music = Integer.parseInt(msg);
                                            msg = "Le serveur contient " + total_music + " musique";
                                        }

                                        mTextViewReplyFromServer.setText(s + "\nServeur : " + msg);
                                    } catch (Exception e) {
                                        mTextViewReplyFromServer.setText(s + "\nServeur Erreur : " + st);
                                    }


                                }

                            } else {
                                //ST != null
                                Log.d("hsocket", "ST == null");
                            }

                        }
                    });

                    output.close();
                    out.close();
                    s.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case 9999:
                if (resultCode == Activity.RESULT_OK) {

                    String folderLocation = data.getExtras().getString("data");
                    Log.d( "FTP", folderLocation );

                    sharedPreferences
                            .edit()
                            .putString(PREFS_PATH_ZIK, folderLocation)
                            .apply();

                    Toast.makeText(this, "Sauvegardé", Toast.LENGTH_SHORT).show();

                    FtpBackground test = new FtpBackground();
                    test.execute(pathZik);
                }
        }
    }


    public boolean checkStoragePermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            //Write permission is required so that folder picker can create new folder.
            //If you just want to pick files, Read permission is enough.

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        SDCARD_PERMISSION);
                return false;
            } else return true;
        }
        return true;
    }
}
