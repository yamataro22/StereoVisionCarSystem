package com.example.stereovisioncarsystem;

import android.util.Log;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class BasicCapturer implements CameraBridgeViewBase.CvCameraViewListener2 {

    Mat mRgba;
    Mat mRgbaF;
    Mat mRgbaT;
    Filtr gray = new GrayFiltr();


    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.d("serverLogs", "CameraFramesCapturer, cameraViewStarted");
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mRgbaF = new Mat(height, width, CvType.CV_8UC4);
        mRgbaT = new Mat(width, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        return inputFrame.gray();
    }


}
