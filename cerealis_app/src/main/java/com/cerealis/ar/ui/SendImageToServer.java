package com.cerealis.ar.ui;

import android.util.Log;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

public class SendImageToServer extends Socket {

    String host;
    private boolean imageHasBeenProcessed = false;
    private boolean isError = false;

    String responseString = null;


    public SendImageToServer(String host) {
        this.host = host;
    }

    public String uploadFile(String file, int drawing) {
        responseString = null;
        imageHasBeenProcessed = false;
        isError = false;

        long totalSize = 0;


        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://192.168.1.72:5000/file-upload");

        try {
            long finalTotalSize = totalSize;
            AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                    new AndroidMultiPartEntity.ProgressListener() {

                        @Override
                        public void transferred(long num)
                        {
                            long total = finalTotalSize;

                            Log.e("test", String.valueOf(num));

                            //int result = (int) ((num/total) *100);

                            //if(result>=100){
                                //so we have finished
                                System.out.println("Transfer finished");

                            //}

                        }
                    });

            File sourceFile = new File(file);

            // Adding file data to http body
            entity.addPart("file", new FileBody(sourceFile));

            totalSize = entity.getContentLength();
            entity.addPart("drawing",new StringBody(""+drawing));
            // Extra parameters if you want to pass to server
            httppost.setEntity(entity);

            // Making server call
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity r_entity = response.getEntity();

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                // Server response
                responseString = EntityUtils.toString(r_entity);
                imageHasBeenProcessed = true;
            } else {
                responseString = "Error occurred! Http Status Code: "
                        + statusCode;

                imageHasBeenProcessed = false;
                isError = true;
            }





        } catch (ClientProtocolException e) {
            e.printStackTrace();
            responseString = e.toString();
        } catch (IOException e) {
            e.printStackTrace();
            responseString = e.toString();
        }

        return responseString;
    }


    public synchronized boolean imageHasBeenProcessed() {
        return imageHasBeenProcessed;
    }

    public synchronized boolean isError(){
        return isError;
    }

    public synchronized String getResult(){
        return responseString;
    }
}