package com.bobby.httptester;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

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
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private Thread mainThread = null;
    private boolean run = false;
    private EditText testUrlText;
    private EditText threadsText;
    private EditText intervalText;
    private TextView logView;
    private OutputStreamWriter logFileWrite = null;
    private Button runBtn;
    private Button stopBtn;
    private HttpTester tester = null;
    static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss.SSS");


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        testUrlText = (EditText) findViewById(R.id.testUrlText);
        threadsText = (EditText) findViewById(R.id.threadsText);
        intervalText = (EditText) findViewById(R.id.intervalText);
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
//        appendText2logText(System.currentTimeMillis()+":"+testUrlStr+":"+threadsInt+":"+intervalInt+"\n");

        runBtn.setClickable(false);
        stopBtn.setClickable(true);
        appendText2logText(sdf.format(new Date(System.currentTimeMillis())) + ":started!\n");
    }

    public void onClearBtnClick(View view) {
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

    public void onStopBtnClick(View view) {
        tester.stop();
        stopBtn.setClickable(false);
        runBtn.setClickable(true);
        appendText2logText(sdf.format(new Date(System.currentTimeMillis())) + ":stop!\n");
    }
}
