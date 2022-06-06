package com.cerealis.ar.service;

import android.content.Context;

import com.cerealis.ar.R;

import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class ApiService {

    String host ;
    public ApiService(Context context) {

        host = context.getString(R.string.hostname);
    }


    public boolean sendUser(String user, String email){

        if((user!=null || user.isEmpty()) && (email!=null||email.isEmpty())){
            HashMap<String, String> hashMapUser = new HashMap<>();

            hashMapUser.put("firstName", user);
            hashMapUser.put("email", email);

            JSONObject jo = new JSONObject(hashMapUser);

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    HttpURLConnection connection = null;
                    int is = 0;

                    try {
                        //Create connection

                        URL url = new URL("http://192.168.1.72:8080/addUser?firstName="+user+"&email="+email);
                        connection = (HttpURLConnection) url.openConnection();

                        connection.setRequestMethod("GET");


                        //Send request
                        System.out.println(jo.toString());

                        //Get Response

                        connection.setConnectTimeout(2000);


                        try {
                            is = connection.getResponseCode();
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                            if (connection instanceof HttpURLConnection) {
                                HttpURLConnection httpConn = (HttpURLConnection) connection;
                                int statusCode = httpConn.getResponseCode();
                                //if (statusCode != 200) {
                                  //  is = httpConn.getErrorStream();
                                //}
                            }
                        }
                        //return response.toString();
                    } catch (Exception e) {

                        e.printStackTrace();
                        //return null;

                    } finally {
                        if (connection != null) {
                            connection.disconnect();
                        }
                    }
                }
            });

            t.start();

            return true;
        }else{
            return false;
        }



    }
}
