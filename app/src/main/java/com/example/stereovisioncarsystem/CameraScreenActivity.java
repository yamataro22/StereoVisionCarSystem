package com.example.stereovisioncarsystem;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.ImageView;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.Map;


public class CameraScreenActivity extends CameraBasicActivity {

    public static final String TYPE = "THRESH";
    public static final String THRESHPARAM = "140";

    public static final String PARAMETERS = "messageBlur";
    private boolean[] filters;
    Filtr f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_screen);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);


        /*int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Intent intent = getIntent();

            VisionParameters visionParams = intent.getParcelableExtra(PARAMETERS);
            filters = new boolean[VisionParameters.FILTERS_QUANTITY];


        }*/


        mOpenCvCameraView = findViewById(R.id.HelloOpenCvView);
        mOpenCvCameraView.enableView();
        mOpenCvCameraView.enableFpsMeter();
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }


    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(Mat inputFrame) {

        Filtr filtr = new BinaryThreshFiltr(120);
        filtr.filtr(inputFrame);
        return inputFrame;

    }

    @Override
    public void onPause()
    {
        Log.i("filtry", "jestem w OnPause");
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }
    public void onDestroy() {
        super.onDestroy();
        Log.i("filtry", "jestem w onDestroy");
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }


}
