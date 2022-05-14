package com.amodtech.ar.lineview;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

public class SendImageToServer extends Socket {

    String host;

    public SendImageToServer(String host){
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
                url = new URL(host ); // setting the same name that the package on the drone
            }
            
            urlconnection = url.openConnection();
            urlconnection.setDoOutput(true);
            urlconnection.setDoInput(true);



            if (urlconnection instanceof HttpURLConnection) {
                ((HttpURLConnection) urlconnection).setRequestMethod("POST");
                ((HttpURLConnection) urlconnection).setRequestProperty("Content-type", "text/plain");
                //((HttpURLConnection) urlconnection).setRequestProperty("Authorization", "Basic c3FkcjpzcWRyX2ludg=="); //hardcoded
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
                    Log.i("TcpThread","Transmission OK");
                    //start only if the package was successfully sent
                    break;
                case 201:
                    // transmission was ok and ressources was created
                    Log.i("TcpThread","Transmission OK");
                    //start only if the package was successfully sent

                    break;
                case 400:
                case 403:
                    Log.i("TcpThread","Transmission FAILED");
                    break;
                case 500:
                    Log.i("TcpThread","Server FAILED");
                    break;
                default:
                    break;
            }

        } catch (Exception e) {
            System.out.println("ERROR DURING CONNECTION");
            e.printStackTrace();
        }

    }
}