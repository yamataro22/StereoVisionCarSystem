package com.example.stereovisioncarsystem.FilterCalibration;

import android.content.Intent;
import android.os.Bundle;
import android.view.SurfaceView;

import com.example.stereovisioncarsystem.CameraBasicActivity;
import com.example.stereovisioncarsystem.Filtr.Filtr;
import com.example.stereovisioncarsystem.Filtr.GBlurFiltr;
import com.example.stereovisioncarsystem.Filtr.GrayFiltr;
import com.example.stereovisioncarsystem.R;

import org.opencv.core.Mat;

public class GaussCalibrationActivity extends CameraBasicActivity {

    int gaussValue;
    public static final String GAUSS_VALUE = "m1";


    GBlurFiltr gauss = new GBlurFiltr();
    Filtr gray = new GrayFiltr();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_screen);
        Intent intent = getIntent();

        gaussValue = intent.getIntExtra(GAUSS_VALUE, 3);
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
        gray.filtr(inputFrame);
        gauss.setgBlurMatrixSize(gaussValue);
        gauss.filtr(inputFrame);
        return inputFrame;
    }
}
