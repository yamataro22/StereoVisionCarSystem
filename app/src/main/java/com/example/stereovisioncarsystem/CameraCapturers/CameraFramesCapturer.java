package com.example.stereovisioncarsystem.CameraCapturers;

import android.util.Log;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class CameraFramesCapturer implements CameraBridgeViewBase.CvCameraViewListener2 {

    protected Mat mRgba;
    protected Mat mRgbaF;
    protected Mat mRgbaT;


    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.d("serverLogs", "ObservedRotatedCameraFramesCapturer, cameraViewStarted");
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
