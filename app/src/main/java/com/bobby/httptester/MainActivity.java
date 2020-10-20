package com.bobby.httptester;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
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
    private EditText threadsText,intervalText,connectTimeOutText,readTimeOutText;
    private TextView logView;
    private TextView publicIpView;
    private OutputStreamWriter logFileWrite = null;
    private Button runBtn;
    private Button stopBtn;
    private HttpTester tester = null;
    static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss.SSS");
    static SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd ");
    private String PUBLIC_IP_SERVICE_URL="https://api.ipify.org/";

    static class UpdateViewHander extends Handler{
        @Override
        public void handleMessage(Message msg) {
            CharSequence cs = msg.getData().getCharSequence ("str");
            ((TextView)msg.obj).setText(cs);
        }
    };
    static Handler updateViewHander = new UpdateViewHander();

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        testUrlText = (EditText) findViewById(R.id.testUrlText);
        threadsText = (EditText) findViewById(R.id.threadsText);
        connectTimeOutText = (EditText) findViewById(R.id.connectTimeOutText);
        readTimeOutText = (EditText) findViewById(R.id.readTimeOutText);
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
        int connectTimeOutInt= Integer.valueOf(connectTimeOutText.getText().toString());
        int readTimeOutInt= Integer.valueOf(readTimeOutText.getText() .toString());
        try {
            //每次run  重新创建log 文件
            if(logFileWrite != null){
                logFileWrite.close();
            }

            logFileWrite = new OutputStreamWriter(new FileOutputStream(new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                    , "runlog" + sdf.format(new Date(System.currentTimeMillis())) + ".log")));
            logFileWrite.write("testUrl:"+testUrlStr+"\n");
            logFileWrite.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        tester = new HttpTester(testUrlStr, threadsInt, intervalInt,connectTimeOutInt, readTimeOutInt,this);
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
        if(str != null) {
            //log with yyyyMMdd
            try {
                logFileWrite.write(str);
                logFileWrite.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (sb.length() > 1024) {
                sb.setLength(1024);
            }
            //print without yyyyMMdd
            if(str.startsWith(yyyyMMdd.format(new Date()))){
                str = str.substring(11);
            }
            sb.insert(0, str);
            updateViewByHander(logView, sb);
        }
    }

    private void updateViewByHander(View view,CharSequence str) {
        Message msg = new Message();
        msg.obj = view;
        Bundle bundle = new Bundle();
        bundle.putCharSequence("str",str);
        msg.setData(bundle);
        updateViewHander.sendMessage(msg);
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
                            updateViewByHander(publicIpView,ipInfo);
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
