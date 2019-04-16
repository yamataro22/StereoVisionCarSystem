package com.example.stereovisioncarsystem;


import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;

import com.example.stereovisioncarsystem.Filtr.Filtr;
import com.example.stereovisioncarsystem.Filtr.GBlurFiltr;
import com.example.stereovisioncarsystem.Filtr.GrayFiltr;

import org.opencv.core.Mat;
import java.util.ArrayList;
import java.util.List;


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
        mOpenCvCameraView.setCameraIndex(0);
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
        Mat dst = new Mat();

        List<Filtr> filtrs = new ArrayList<>();
        filtrs.add(new GrayFiltr());
        filtrs.add(new GBlurFiltr(27));
        //filtrs.add(new BinaryThreshFiltr(120));
        //filtrs.add(new CannyFiltr());

        for(Filtr filtr : filtrs)
        {
            filtr.filtr(inputFrame);
        }

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
