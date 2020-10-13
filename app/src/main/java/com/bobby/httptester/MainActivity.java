package com.bobby.httptester;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.*;
import android.os.Bundle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private Thread mainThread = null;
    private boolean run = false;
    private EditText testUrlText;
    private EditText threadsText;
    private EditText intervalText;
    private TextView logView;
    private TextView publicIpView;
    private OutputStreamWriter logFileWrite = null;
    private Button runBtn;
    private Button stopBtn;
    private HttpTester tester = null;
    static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss.SSS");
    private String PUBLIC_IP_SERVICE_URL="https://api.ipify.org/";


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        testUrlText = (EditText) findViewById(R.id.testUrlText);
        threadsText = (EditText) findViewById(R.id.threadsText);
        intervalText = (EditText) findViewById(R.id.intervalText);
        publicIpView = (TextView) findViewById(R.id.pubIp);
        runBtn = (Button) findViewById(R.id.runBtn);
        stopBtn = (Button) findViewById(R.id.stopBtn);
        logView = (TextView) findViewById(R.id.logView);
        logView.setMovementMethod(ScrollingMovementMethod.getInstance());
        try {
            logFileWrite = new OutputStreamWriter(new FileOutputStream(new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                    , "runlog" + sdf.format(new Date(System.currentTimeMillis())) + ".log")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void onRunBtnClick(View view) {
        String testUrlStr = testUrlText.getText().toString();
        int threadsInt = Integer.valueOf(threadsText.getText().toString());
        int intervalInt = Integer.valueOf(intervalText.getText().toString());
        tester = new HttpTester(testUrlStr, threadsInt, intervalInt, this);
        tester.start();
        runBtn.setClickable(false);
        stopBtn.setClickable(true);
        appendText2logText(sdf.format(new Date(System.currentTimeMillis())) + ":started!\n");
    }

    public void onClearBtnClick(View view) {
        sb.setLength(0);
        logView.setText("");
    }
    StringBuffer sb = new StringBuffer();
    public synchronized void appendText2logText(String str) {
        sb.insert(0, str);
        try {
            logFileWrite.write(str);
            logFileWrite.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (sb.length() >  1024) {
            sb.setLength( 1024);
        }
        logView.setText(sb);
    }

    @Override
    public void onDestroy() {
        if (logFileWrite != null) {
            try {
                logFileWrite.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();  // Always call the superclass
    }


    public void onClickGetPublicIp(View view){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(PUBLIC_IP_SERVICE_URL);
                    HttpURLConnection conn =  (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(10 * 1000);
                    conn.setReadTimeout(10 * 1000);
                    conn.setRequestMethod("GET");
                    if (conn.getResponseCode() == 200) {
                        byte[] buff = new byte[200];
                        InputStream in = conn.getInputStream();
                        int o = in.read(buff);
                        if(o > 0){
                            String ipInfo = "公网IP:"+new String(buff);
                            publicIpView.setText(ipInfo);
                            appendText2logText(ipInfo+"\n");
                        }
                        in.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    public void onStopBtnClick(View view) {
        tester.stop();
        stopBtn.setClickable(false);
        runBtn.setClickable(true);
        appendText2logText(sdf.format(new Date(System.currentTimeMillis())) + ":stop!\n");
    }
}
