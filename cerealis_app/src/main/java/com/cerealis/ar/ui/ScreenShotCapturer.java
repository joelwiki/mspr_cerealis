package com.cerealis.ar.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class ScreenShotCapturer {

    Bitmap bitmap;

    public ScreenShotCapturer(){

    }

    public boolean recordImage(Context context, Bitmap bitmap){

        String filename = "capture.png";
        File sd = Environment.getExternalStorageDirectory();
        File dest = new File(sd, filename);

        if(bitmap == null){
            Toast.makeText(context,"bitmap is null", Toast.LENGTH_LONG).show();
        }

        try {
            FileOutputStream out = new FileOutputStream(dest);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }
}
