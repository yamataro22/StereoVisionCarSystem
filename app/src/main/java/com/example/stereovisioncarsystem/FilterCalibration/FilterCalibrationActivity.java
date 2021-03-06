package com.example.stereovisioncarsystem.FilterCalibration;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;

import com.example.stereovisioncarsystem.CameraBasicActivity;
import com.example.stereovisioncarsystem.R;

import org.opencv.core.Mat;

abstract public class FilterCalibrationActivity extends CameraBasicActivity implements View.OnTouchListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_calibration);
        mOpenCvCameraView = findViewById(R.id.calibrationOpencvView);
        mOpenCvCameraView.enableView();
        mOpenCvCameraView.enableFpsMeter();
        mOpenCvCameraView.setMaxFrameSize(2400, 2000);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setOnTouchListener(this);
    }

    @Override
    abstract public void onCameraViewStarted(int width, int height);

    @Override
    abstract public void onCameraViewStopped();

    @Override
    abstract public Mat onCameraFrame(Mat inputFrame);


    @Override
     abstract public boolean onKeyDown(int keyCode, KeyEvent event);
}
