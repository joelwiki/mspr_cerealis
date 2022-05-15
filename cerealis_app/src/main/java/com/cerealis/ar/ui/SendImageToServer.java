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

    public SendImageToServer(String host) {
        this.host = host;
    }

    public void sendImageToServer(String file) {
        URLConnection urlconnection = null;
        try {
            InputStream packageVersionFile = null;
            URL url = null;

            /* if file are standalone packet */
            if (file == null) {
                //send builtin packet
                Log.e("TCPThread", "cannot send nothing to the server");
            } else {
                packageVersionFile = new FileInputStream(file);
                url = new URL(host); // setting the same name that the package on the drone
            }

            urlconnection = url.openConnection();
            urlconnection.setDoOutput(true);
            urlconnection.setDoInput(true);


            if (urlconnection instanceof HttpURLConnection) {
                urlconnection.setRequestProperty("Content-Type", "image/jpg");
                urlconnection.setRequestProperty("file", "rhino.jpg");
                ((HttpURLConnection) urlconnection).setRequestMethod("POST");
                ((HttpURLConnection) urlconnection).setRequestProperty("Content-type", "multipart/form-data");
                ((HttpURLConnection) urlconnection).connect();
            }


            BufferedOutputStream bos = new BufferedOutputStream(urlconnection.getOutputStream());
            BufferedInputStream bis = new BufferedInputStream(packageVersionFile);


            int i;
            // read byte by chunck
            byte[] buffer = new byte[4 * 1024];

            int read;
            int k = 0;
            while ((read = bis.read(buffer, 0, buffer.length)) != -1) {
                bos.write(buffer, 0, read);
                bos.flush();
            }

            bis.close();
            bos.close();

            System.out.println("DATA FROM RETURN " + ((HttpURLConnection) urlconnection).getResponseMessage());

            /* see http code */


            switch (((HttpURLConnection) urlconnection).getResponseCode()) {
                case 200:
                    Log.i("TcpThread", "Transmission OK");
                    //start only if the package was successfully sent
                    break;
                case 201:
                    // transmission was ok and ressources was created
                    Log.i("TcpThread", "Transmission OK");
                    //start only if the package was successfully sent

                    break;
                case 400:
                case 403:
                    Log.i("TcpThread", "Transmission FAILED");
                    break;
                case 500:
                    Log.i("TcpThread", "Server FAILED");
                    break;
                default:
                    break;
            }

        } catch (Exception e) {
            System.out.println("ERROR DURING CONNECTION");
            e.printStackTrace();
        }

    }


    public String uploadFile(String file, int drawing) {
        String responseString = null;

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

                            int result = (int) ((num/total) *100);

                            if(result>=100){
                                //so we have finished
                                System.out.println("Transfer finished");

                            }

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
            } else {
                responseString = "Error occurred! Http Status Code: "
                        + statusCode;
            }

            System.out.println(responseString);



        } catch (ClientProtocolException e) {
            e.printStackTrace();
            responseString = e.toString();
        } catch (IOException e) {
            e.printStackTrace();
            responseString = e.toString();
        }

        return responseString;
    }


}