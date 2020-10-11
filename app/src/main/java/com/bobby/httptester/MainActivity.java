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

public class MainActivity extends AppCompatActivity {
    private Thread mainThread = null;
    private boolean run = false;
    private EditText testUrlText ;
    private EditText threadsText ;
    private EditText intervalText ;
    static private TextView logView;
    static private OutputStreamWriter logFileWrite = null;
    private Button runBtn;
    private Button stopBtn;



    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        testUrlText = (EditText)findViewById(R.id.testUrlText);
        threadsText = (EditText)findViewById(R.id.threadsText);
        intervalText = (EditText)findViewById(R.id.intervalText);
        runBtn = (Button)findViewById(R.id.runBtn);
        stopBtn = (Button)findViewById(R.id.stopBtn);
        logView = (TextView)findViewById(R.id.logView);
        logView.setMovementMethod(ScrollingMovementMethod.getInstance());
        try {
            logFileWrite = new OutputStreamWriter(new FileOutputStream(new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                    ,"runlog.log")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void onRunBtnClick(View view){
        String testUrlStr = testUrlText.getText().toString();
        int threadsInt = Integer.valueOf(threadsText.getText().toString());
        int intervalInt = Integer.valueOf(intervalText.getText().toString());
        appendText2logText(System.currentTimeMillis()+":"+testUrlStr+":"+threadsInt+":"+intervalInt+"\n");

        runBtn.setClickable (false);
        stopBtn.setClickable(true);

    }

    public synchronized static void  appendText2logText(String str){
        StringBuffer sb = new StringBuffer( logView.getText());
        sb.insert(0,str);
        if(sb.length() > 10*1024*1024){
            sb.setLength(10*1024*1024);
        }
        logView.setText(sb);
        try {
            logFileWrite.write(str);
            logFileWrite.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        if(logFileWrite != null) {
            try {
                logFileWrite.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();  // Always call the superclass
    }

    public void onStopBtnClick(View view){

        stopBtn.setClickable(false);
        runBtn.setClickable(true);
    }
}
