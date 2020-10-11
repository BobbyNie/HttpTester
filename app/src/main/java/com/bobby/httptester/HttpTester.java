package com.bobby.httptester;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class HttpTester {
    private int interval;
    private boolean runHttpTest = false;
    private String urlStr = "";
    private int threadCount;
    private Thread mainThread ;
    private ExecutorService pool;
    private MainActivity activity ;
 //   private int Interval

    public HttpTester(final String urlStr, final int threadCount, final int interval,final MainActivity activity){
        this.urlStr = urlStr;
        this.threadCount = threadCount;
        this.interval = interval;
        this.activity = activity;
        mainThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpTester.this.run();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        pool = Executors.newFixedThreadPool(threadCount*3);
    }

    public void start(){
        runHttpTest = true;
        mainThread.start();
    }

    public void stop(){
        runHttpTest = false;
        mainThread.interrupt();
    }


    public void run() throws InterruptedException, MalformedURLException {
        while (runHttpTest){
            final List<Future<String>> futureList = new ArrayList<Future<String>>();

            for(int i =0 ;i < threadCount;i++) {
                final int fi = i;
                Future<String> future = pool.submit(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        String r;
                        String oneUrlStr = urlStr;
                        if(!oneUrlStr.contains("?")){
                            oneUrlStr = oneUrlStr+ "?i="+fi;
                        }else{
                            oneUrlStr = oneUrlStr+ "&i="+fi;
                        }
                        oneUrlStr = oneUrlStr+"&t="+System.currentTimeMillis();

                        URL url = new URL(urlStr);
                        HttpURLConnection conn = null;
                        try {
                            conn = (HttpURLConnection) url.openConnection();
                            conn.setConnectTimeout(interval);
                            conn.setReadTimeout(interval);
                            conn.setRequestMethod("GET");
                            if (conn.getResponseCode() == 200) {
                                r = "OK";
                                InputStream in = conn.getInputStream();
                                byte[] data = new byte[1024];
                                int o = in.read(data);
                                while (o > 0) {
                                    o = in.read(data);
                                }
                                in.close();
                                conn.getOutputStream().close();
                            }
                            r = "" + conn.getResponseCode();
                        } catch (Exception e) {
                            r = e.getClass().getName();
                            e.printStackTrace();
                        }
                        return r;
                    }
                });
                futureList.add(future);
            }

            pool.submit(new Runnable() {
                @Override
                public void run() {
                    HashMap<String,Integer> statisticsMap = new HashMap<>();
                    for(Future<String> future: futureList){
                        try {
                            String s = future.get( interval, TimeUnit.SECONDS);
                            if(statisticsMap.get(s) == null){
                                statisticsMap.put(s,0);
                            }
                            statisticsMap.put(s,statisticsMap.get(s)+1);
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (TimeoutException e) {
                            e.printStackTrace();
                        }
                    }
                    MainActivity.appendText2logText(""+statisticsMap.toString());

                }
            });


            Thread.sleep(interval*1000);
        }
    }
}
