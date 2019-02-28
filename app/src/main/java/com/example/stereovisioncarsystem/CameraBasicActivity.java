package com.example.stereovisioncarsystem;

import android.Manifest;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

abstract public class CameraBasicActivity extends Activity  implements CameraBridgeViewBase.CvCameraViewListener {

    static {
        OpenCVLoader.initDebug();
    }

    protected CameraBridgeViewBase mOpenCvCameraView;

    protected static final String  TAG = "CameraScreenActivity";
    protected static final int  MY_PERMISSIONS_REQUEST_CAMERA =1;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permissions not granted");
            Log.i("filtry", "nie przyznano praw");
            // Permission is not granted
            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                Log.i(TAG, "Sending permission request");
                Log.i("filtry", "wysyłam do wyboru praw");
                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.i("filtry", "wchodzę do requestpermissionresult");
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    //mCamera = getCameraInstance();
                    //mPreview = new CameraPreview(this, mCamera);
                    //FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
                    //preview.addView(mPreview);
                    Log.i(TAG, "włączam vidok opencv");


                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @Override
    abstract public void onCameraViewStarted(int width, int height);

    @Override
    abstract public void onCameraViewStopped();

    @Override
    abstract public Mat onCameraFrame(Mat inputFrame);


}
