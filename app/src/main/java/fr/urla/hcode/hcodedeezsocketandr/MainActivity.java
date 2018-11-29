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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
    private EditText mEditTextSendMessage;
    private String ServerIP = "192.168.1.36";
    public String pathZik;

    private static final String PREFS = "PREFS";
    private static final String PREFS_PATH_ZIK = "PREFS_PATH_ZIK";
    SharedPreferences sharedPreferences;

    private static final int SDCARD_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkStoragePermission();

        Button buttonDownload = (Button) findViewById(R.id.btn_download);
        Button buttonUpdate = (Button) findViewById(R.id.update_button);

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
                sendMessage("update<EOF>");
            }
        });


        sharedPreferences = getBaseContext().getSharedPreferences(PREFS, MODE_PRIVATE);

        //pour cela, on commence par regarder si on a déjà des éléments sauvegardés
        if (sharedPreferences.contains(PREFS_PATH_ZIK)) {

            pathZik = sharedPreferences.getString(PREFS_PATH_ZIK, null);
        } else {
            Intent intent = new Intent(this, FolderPicker.class);
            intent.putExtra("location", Environment.getExternalStorageDirectory().getAbsolutePath());
            startActivityForResult(intent, 9999);
        }

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

                    output.println(mEditTextSendMessage.getText() + "<EOF>");
                    output.flush();
                    BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    final String st = input.readLine();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            String s = mTextViewReplyFromServer.getText().toString();
                            if (st.trim().length() != 0)
                                mTextViewReplyFromServer.setText(s + "\nServeur : " + st);
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


    void checkStoragePermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            //Write permission is required so that folder picker can create new folder.
            //If you just want to pick files, Read permission is enough.

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        SDCARD_PERMISSION);
            }
        }

    }
}
