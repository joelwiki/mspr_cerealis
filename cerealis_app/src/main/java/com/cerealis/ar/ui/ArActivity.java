/*
 * Copyright 2018 Google LLC. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cerealis.ar.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.PixelCopy;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.cerealis.ar.R;
import com.cerealis.ar.service.ApiService;
import com.google.android.material.snackbar.Snackbar;
import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.NotTrackingException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.Light;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * LineViewMainActivity - built on HelloSceneForm sample.
 */
public class ArActivity extends AppCompatActivity {
    private static final String TAG = ArActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;
    private static final int MAX_ANCHORS = 1;

    private ArFragment arFragment;
    private ModelRenderable snakeRenderable;
    private ModelRenderable rhinoRenderable;
    private ModelRenderable monkeyRenderable;

    private AnchorNode anchorNode;
    private List<AnchorNode> anchorNodeList = new ArrayList<>();
    private Integer numberOfAnchors = 0;
    private AnchorNode currentSelectedAnchorNode = null;
    private Node nodeForLine;
    private boolean photoHasBeenTaken = false;


    SendImageToServer sendImageToServer;

    private int drawing = 0;

    @RequiresApi(api = VERSION_CODES.O)
    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    // CompletableFuture requires api level 24
    // FutureReturnValueIgnored is not valid
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }

        drawing = getIntent().getIntExtra("drawing",0);


        setContentView(R.layout.activity_ux);

        Toast.makeText(this, "Drawing "+ drawing,Toast.LENGTH_LONG).show();

        Button button = findViewById(R.id.capture);


        FrameLayout frameLayout = findViewById(R.id.frameLayout);
        frameLayout.setDrawingCacheEnabled(true);


        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        //arFragment


        Light spotLightYellow = Light.builder(Light.Type.FOCUSED_SPOTLIGHT)
                .setColor(new Color(android.graphics.Color.YELLOW))
                .setIntensity(50)
                .setShadowCastingEnabled(true)
                .build();


        //arFragment.getArSceneView().getScene().getSunlight().setLight(spotLightYellow);

        // When you build a Renderable, Sceneform loads its resources in the background while returning
        // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().



        /*
        ModelRenderable.builder()
                .setSource(this, R.raw.snake)
                .build()
                .thenAccept(renderable -> snakeRenderable = renderable)

                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load snake renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);

                            toast.show();
                            return null;
                        });


         */
        ModelRenderable.builder()
                .setSource(this, R.raw.rhino)
                .build()
                .thenAccept(renderable -> rhinoRenderable = renderable)

                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load rhino renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);

                            toast.show();
                            return null;
                        });

        ModelRenderable.builder()
                .setSource(this, R.raw.snake)
                .build()
                .thenAccept(renderable -> snakeRenderable = renderable)

                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load snake renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);

                            toast.show();
                            return null;
                        });

        ModelRenderable.builder()
                .setSource(this, R.raw.monkey)
                .build()
                .thenAccept(renderable -> monkeyRenderable = renderable)

                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load snake renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);

                            toast.show();
                            return null;
                        });

        arFragment.getArSceneView().getPlaneRenderer().setVisible(false);
        if (arFragment.getArSceneView().getPlaneRenderer().isEnabled()) {
            Toast.makeText(this, "Scene is visible", Toast.LENGTH_LONG).show();
        }

        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    // Do nothng on plane taps for now

                });


        arFragment.getArSceneView().getScene().addOnPeekTouchListener(this::handleOnTouch);


        //Add a listener for the back button
        findViewById(R.id.share_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Make a screenshot of the drawing

                boolean result = true;
                result = new ScreenShotCapturer().recordImage(getApplicationContext(),arFragment.getArSceneView().getDrawingCache());

                if(result){
                    openSocialMediaDialog();
                }else{
                    Snackbar.make(arFragment.getArSceneView(),"Nous n'avons pas réussi à enregistrer l'image",
                            Snackbar.LENGTH_LONG).show();
                }


            }
        });


        findViewById(R.id.rotate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO ROTATE VIEW
                Log.d(TAG, "rotate anchor");
                if (currentSelectedAnchorNode != null) {
                    //Get the current Pose and transform it then set a new anchor at the new pose
                    Session session = arFragment.getArSceneView().getSession();
                    Anchor currentAnchor = currentSelectedAnchorNode.getAnchor();
                    Pose oldPose = currentAnchor.getPose();
                    Pose newPose = oldPose.compose(Pose.makeTranslation(0, 0, -0.05f));
                    currentSelectedAnchorNode = moveRenderable(currentSelectedAnchorNode, newPose);
                }
            }
        });



        send(drawing);

    }

    private void openSocialMediaDialog() {

        ApiService apiService = new ApiService(this);


        View view = getLayoutInflater().inflate(R.layout.alertuser,null);

        EditText editTextUser,editTextEmail;

        editTextUser = view.findViewById(R.id.name);
        editTextEmail = view.findViewById(R.id.email);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setView(view);
        alertDialog.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                String email = editTextEmail.getText().toString();
                String user = editTextUser.getText().toString();

                if(user.isEmpty()){
                    editTextUser.setError("Error, user is empty");

                }

                if (email.isEmpty()){
                    editTextEmail.setError("Error : your email is empty");
                }

                if(!user.isEmpty() && (!email.isEmpty() && validate(email))){
                    apiService.sendUser(user,email);

                    //create screenshot
                }else{
                    Toast.makeText(getApplicationContext(),"Error : please check your info", Toast.LENGTH_SHORT).show();
                }
            }
        });

        alertDialog.show();

    }

    public static boolean validate(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    private void dispatchTakePictureIntent() {

        View MainView = this.getWindow().getDecorView();
        MainView.setDrawingCacheEnabled(true);
        MainView.buildDrawingCache();
        Bitmap MainBitmap = MainView.getDrawingCache();
        Rect frame = new Rect();

        this.getWindow().getDecorView().findViewById(R.id.ux_fragment);
        //to remove statusBar from the taken sc
        int statusBarHeight = frame.top;
        //using screen size to create bitmap
        int width = this.getWindowManager().getDefaultDisplay().getWidth();
        int height = this.getWindowManager().getDefaultDisplay().getHeight();

        ArSceneView arSceneView = arFragment.getArSceneView();

        final Bitmap bitmap = Bitmap.createBitmap(arSceneView.getWidth(), arSceneView.getHeight(), Bitmap.Config.ARGB_8888);


        Bitmap OutBitmap = Bitmap.createBitmap(MainBitmap, 0, statusBarHeight, width, height - statusBarHeight);


        final HandlerThread handlerThread = new HandlerThread("PixelCopier");
        handlerThread.start();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            PixelCopy.request(arSceneView, OutBitmap, (copyResult) -> {
                if (copyResult == PixelCopy.SUCCESS) {

                    String path = Environment.getExternalStorageDirectory().toString();
                    OutputStream fOut = null;
                    //you can also using current time to generate name

                    File file = new File(Environment.getExternalStorageDirectory().toString() + "/DCIM/testScreenShot.png");
                    try {
                        fOut = new FileOutputStream(file);
                        OutBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                        fOut.flush();
                        fOut.close();

                        //this line will add the saved picture to gallery
                        //MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());


                        Intent share = new Intent(Intent.ACTION_SEND);
                        share.setType("image/*");


                        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        Uri photoURI = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", file);
                        share.putExtra(Intent.EXTRA_TEXT, "#cerealis #coloring #AR");
                        share.putExtra(Intent.EXTRA_STREAM, photoURI);
                        startActivity(Intent.createChooser(share, "Share Image"));


                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                } else {
                    Toast toast = Toast.makeText(this,
                            "Error" + copyResult, Toast.LENGTH_LONG);
                    toast.show();
                }
                handlerThread.quitSafely();
            }, new Handler(handlerThread.getLooper()));



        }




    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            //Bundle extras = data.getExtras();
            //Bitmap imageBitmap = (Bitmap) extras.get("data");
            Toast.makeText(getApplicationContext(), "Photo ok", Toast.LENGTH_LONG).show();

            File dir= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

            photoHasBeenTaken = true;

            File fileToSend = new File(dir+"/testTF.jpeg");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    sendImageToServer = new SendImageToServer("http://192.168.1.72:5000/file-upload");
                    sendImageToServer.uploadFile(fileToSend.getAbsolutePath(),1);
                }
            }).start();
        }

    }

    public void send(int drawing){
        String dir= Environment.getExternalStorageDirectory().toString();

        photoHasBeenTaken = true;

        File fileToSend = new File(dir+"/DCIM/testTF.jpg");

        new Thread(new Runnable() {
            @Override
            public void run() {
                sendImageToServer = new SendImageToServer("http://192.168.1.72:5000/file-upload");
                sendImageToServer.uploadFile(fileToSend.getAbsolutePath(),drawing);
            }
        }).start();
    }

    @RequiresApi(api = VERSION_CODES.O)
    private void handleOnTouch(HitTestResult hitTestResult, MotionEvent motionEvent) {
        Log.d(TAG, "handleOnTouch");
        // First call ArFragment's listener to handle TransformableNodes.
        arFragment.onPeekTouch(hitTestResult, motionEvent);

        Session session = arFragment.getArSceneView().getSession();


        //We are only interested in the ACTION_UP events - anything else just return
        if (motionEvent.getAction() != MotionEvent.ACTION_UP) {
            return;
        }

        // Check for touching a Sceneform node



        setColorSnake(session,new ArrayList<com.cerealis.ar.ui.Color>());


        if(!photoHasBeenTaken){
            Snackbar.make(arFragment.getArSceneView(),"Prend d'abord en photo ton dessin", Snackbar.LENGTH_LONG).show();
            return;
        }else {

            if(!sendImageToServer.imageHasBeenProcessed() || sendImageToServer.isError()){
                Snackbar.make(arFragment.getArSceneView(),"Merci d'attendre quelques instants", Snackbar.LENGTH_LONG).show();
                return;
            }


            ArrayList<com.cerealis.ar.ui.Color> colorsArrayList = getArrayListColors(sendImageToServer.getResult());


            if (hitTestResult.getNode() == null) {
                Log.d(TAG, "handleOnTouch hitTestResult.getNode() != null");
                //Toast.makeText(LineViewMainActivity.this, "hitTestResult is not null: ", Toast.LENGTH_SHORT).show();
                Node hitNode = hitTestResult.getNode();


                switch (drawing){
                    case 0:
                        setColorSnake(session, colorsArrayList);
                        break;
                    case 1:
                        setRhinoColor(session,colorsArrayList);
                        break;
                    case 2:
                        setColorSnake(session,colorsArrayList);
                        break;
                }

            }
        }



    }

    @RequiresApi(api = VERSION_CODES.O)
    private ArrayList<com.cerealis.ar.ui.Color> getArrayListColors(String result) {

        ArrayList<com.cerealis.ar.ui.Color> colorArrayList = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(result);

            JSONArray jsonArray = jsonObject.getJSONArray("colors");


            for(int i = 0; i < jsonArray.length();i++){
                colorArrayList.add(hex2Rgb(jsonArray.getString(i)));

                System.out.println(jsonArray.getString(i));

                System.out.println(colorArrayList.get(i).r);
                System.out.println(colorArrayList.get(i).g);
                System.out.println(colorArrayList.get(i).b);
                System.out.println("\n");


            }

            return colorArrayList;

        } catch (JSONException e) {
            e.printStackTrace();
            return null;

        }

    }

    public static com.cerealis.ar.ui.Color hex2Rgb(String colorStr) {

        colorStr = colorStr.replace("#","");
        return new com.cerealis.ar.ui.Color(
                Integer.valueOf(colorStr.substring(0,2),16),
                Integer.valueOf(colorStr.substring(2,4),16),
                Integer.valueOf(colorStr.substring(4,6),16)
                );

    }



    private void setColorSnake(Session session, ArrayList<com.cerealis.ar.ui.Color> colors){


        // Place the anchor 0.5m in front of the camera. Make sure we are not at maximum anchor first.
        Log.d(TAG, "adding Andy in fornt of camera");
        if (numberOfAnchors < MAX_ANCHORS) {
            Frame frame = arFragment.getArSceneView().getArFrame();
            int currentAnchorIndex = numberOfAnchors;
            try {

                if (frame.getCamera().getTrackingState() != TrackingState.TRACKING) {

                    Toast.makeText(this, "Bouge l'appareil avant toucher l'écran", Toast.LENGTH_LONG).show();
                    return;
                }


                Anchor newMarkAnchor = session.createAnchor(
                        frame.getCamera().getPose()
                                .compose(Pose.makeTranslation(0, 0, -10.00f))
                                .extractTranslation());
                AnchorNode addedAnchorNode = new AnchorNode(newMarkAnchor);

                snakeRenderable.setShadowCaster(true);

                //arFragment.getArSceneView().setLightEstimationEnabled(true);


                addedAnchorNode.setRenderable(snakeRenderable);


                for(int i = 0; i < snakeRenderable.getSubmeshCount(); i++){


                    switch (i){
                        case 0 :
                            snakeRenderable.getMaterial(0).setFloat3("baseColorTint", new Color(android.graphics.Color.rgb(0, 0, 255)));
                            break;
                        case 1 :
                            snakeRenderable.getMaterial(1).setFloat3("baseColorTint", new Color(android.graphics.Color.rgb(0, 0, 255)));
                            break;
                        case 2 :
                            snakeRenderable.getMaterial(2).setFloat3("baseColorTint", new Color(android.graphics.Color.rgb(0, 255, 0)));
                            break;
                        case 3:
                            snakeRenderable.getMaterial(3).setFloat3("baseColorTint", new Color(android.graphics.Color.rgb(255, 0, 255)));
                            break;
                        case 4 :
                            snakeRenderable.getMaterial(4).setFloat3("baseColorTint", new Color(android.graphics.Color.rgb(255, 0, 0)));
                            break;
                        case 5:
                            snakeRenderable.getMaterial(5).setFloat3("baseColorTint", new Color(android.graphics.Color.rgb(255, 255, 255)));
                            break;
                        case 6:
                            snakeRenderable.getMaterial(6).setFloat3("baseColorTint", new Color(android.graphics.Color.rgb(0, 255, 255)));
                            break;
                    }

                }

                addAnchorNode(addedAnchorNode);
                currentSelectedAnchorNode = addedAnchorNode;


            } catch (NotTrackingException e) {
                Log.d(TAG, "Not tracking ");

            }
        } else {
            Log.d(TAG, "MAX_ANCHORS exceeded");
        }


    }

    private void setMonkeyColor(ArrayList<Integer> listColors){

    }

    private void setRhinoColor(Session session, ArrayList<com.cerealis.ar.ui.Color> colors){

        // Place the anchor 0.5m in front of the camera. Make sure we are not at maximum anchor first.
        Log.d(TAG, "adding Andy in fornt of camera");
        if (numberOfAnchors < MAX_ANCHORS) {
            Frame frame = arFragment.getArSceneView().getArFrame();
            int currentAnchorIndex = numberOfAnchors;
            try {

                if (frame.getCamera().getTrackingState() != TrackingState.TRACKING) {

                    Toast.makeText(this, "The camera is not tracking", Toast.LENGTH_LONG).show();
                    return;
                }


                Anchor newMarkAnchor = session.createAnchor(
                        frame.getCamera().getPose()
                                .compose(Pose.makeTranslation(0, 0, -10.00f))
                                .extractTranslation());
                AnchorNode addedAnchorNode = new AnchorNode(newMarkAnchor);

                rhinoRenderable.setShadowCaster(true);

                //arFragment.getArSceneView().setLightEstimationEnabled(true);


                addedAnchorNode.setRenderable(rhinoRenderable);



                for(int i = 0; i < rhinoRenderable.getSubmeshCount(); i++){

                    switch (i){
                        case 0 :
                            com.cerealis.ar.ui.Color colorBodyDown = colors.get(4);
                            rhinoRenderable.getMaterial(0).setFloat3("baseColorTint", new Color(android.graphics.Color.rgb((int) colorBodyDown.r, (int) colorBodyDown.g, (int)colorBodyDown.b)));
                            break;
                        case 1 :
                            com.cerealis.ar.ui.Color  colorHeadDown = colors.get(1);
                            rhinoRenderable.getMaterial(1).setFloat3("baseColorTint", new Color(android.graphics.Color.rgb((int)colorHeadDown.r, (int) colorHeadDown.g, (int)colorHeadDown.b)));
                            break;
                        case 2 :
                            com.cerealis.ar.ui.Color  colorHeadUp = colors.get(0);
                            rhinoRenderable.getMaterial(2).setFloat3("baseColorTint", new Color((int) colorHeadUp.r, (int) colorHeadUp.g, (int)colorHeadUp.b));
                            break;
                        case 3:
                            com.cerealis.ar.ui.Color  colorHorn = colors.get(2);
                            rhinoRenderable.getMaterial(3).setFloat3("baseColorTint", new Color((int) colorHorn.r, (int) colorHorn.g, (int)colorHorn.b));
                            break;
                        case 4 :
                            rhinoRenderable.getMaterial(4).setFloat3("baseColorTint", new Color(android.graphics.Color.rgb((int) 0, (int) 0, 0)));
                            break;
                        case 5:
                            com.cerealis.ar.ui.Color  colorBodyUp = colors.get(3);
                            rhinoRenderable.getMaterial(5).setFloat3("baseColorTint", new Color(android.graphics.Color.rgb(colorBodyUp.r, colorBodyUp.g, colorBodyUp.b)));
                            break;
                    }

                }

                addAnchorNode(addedAnchorNode);
                currentSelectedAnchorNode = addedAnchorNode;


            } catch (NotTrackingException e) {
                Log.d(TAG, "Not tracking ");

            }
        } else {
            Log.d(TAG, "MAX_ANCHORS exceeded");
        }

    }

    /**
     * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
     * on this device.
     *
     * <p>Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
     *
     * <p>Finishes the activity if Sceneform can not run
     */


    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        if (Build.VERSION.SDK_INT < VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later");
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
            activity.finish();
            return false;
        }
        String openGlVersionString =
                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }
        return true;
    }

    private void removeAnchorNode(AnchorNode nodeToremove) {
        //Remove an anchor node
        if (nodeToremove != null) {
            arFragment.getArSceneView().getScene().removeChild(nodeToremove);
            anchorNodeList.remove(nodeToremove);
            nodeToremove.getAnchor().detach();
            nodeToremove.setParent(null);
            nodeToremove = null;
            numberOfAnchors--;
            //Toast.makeText(LineViewMainActivity.this, "Test Delete - markAnchorNode removed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(ArActivity.this, "Delete - no node selected! Touch a node to select it.", Toast.LENGTH_SHORT).show();
        }
    }



    private void addAnchorNode(AnchorNode nodeToAdd) {
        //Add an anchor node
        nodeToAdd.setParent(arFragment.getArSceneView().getScene());
        anchorNodeList.add(nodeToAdd);
        numberOfAnchors++;
    }

    private AnchorNode moveRenderable(AnchorNode markAnchorNodeToMove, Pose newPoseToMoveTo) {
        //Move a renderable to a new pose
        if (markAnchorNodeToMove != null) {
            arFragment.getArSceneView().getScene().removeChild(markAnchorNodeToMove);
            anchorNodeList.remove(markAnchorNodeToMove);
        } else {
            Log.d(TAG, "moveRenderable - markAnchorNode was null, the little £$%^...");
            return null;
        }
        Frame frame = arFragment.getArSceneView().getArFrame();
        Session session = arFragment.getArSceneView().getSession();
        Anchor markAnchor = session.createAnchor(newPoseToMoveTo.extractTranslation());
        AnchorNode newMarkAnchorNode = new AnchorNode(markAnchor);
        newMarkAnchorNode.setRenderable(snakeRenderable);
        newMarkAnchorNode.setParent(arFragment.getArSceneView().getScene());
        anchorNodeList.add(newMarkAnchorNode);

        return newMarkAnchorNode;
    }
}

