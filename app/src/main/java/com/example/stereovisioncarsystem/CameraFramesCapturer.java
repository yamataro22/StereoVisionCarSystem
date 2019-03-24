package com.example.stereovisioncarsystem;
import android.os.SystemClock;
import android.util.Log;

import org.opencv.android.*;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;


public class CameraFramesCapturer implements CameraBridgeViewBase.CvCameraViewListener2  {
    Mat mRgba;
    Mat mRgbaF;
    Mat mRgbaT;
    Filtr gray = new GrayFiltr();

    public interface CameraFrameConnector
    {
        void sendFrame(Mat frame);
    }

    private CameraFrameConnector connector;

    public CameraFramesCapturer(CameraFrameConnector connector) {
        Log.d("serverLogs", "CameraFramesCapturer, constructor");
        this.connector = connector;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.d("serverLogs", "CameraFramesCapturer, cameraViewStarted");
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mRgbaF = new Mat(height, width, CvType.CV_8UC4);
        mRgbaT = new Mat(width, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        Log.d("serverLogs", "CameraFramesCapturer, cameraViewStopped");
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        // Rotate mRgba 90 degrees
        Core.transpose(mRgba, mRgbaT);
        Imgproc.resize(mRgbaT, mRgbaF, mRgbaF.size(), 0,0, 0);
        Core.flip(mRgbaF, mRgba, -1 );
        gray.filtr(mRgba);
        connector.sendFrame(mRgba);
        SystemClock.sleep(50);
        return mRgba;
    }

   /* @Override
    public Mat onCameraFrame(Mat inputFrame) {
        Log.d("serverLogs", "CameraFramesCapturer, sendFrame");
        Log.d("serverLogs", "CameraFramesCapturer, typ:"+ inputFrame.type());
        mRgba = inputFrame;
        // Rotate mRgba 90 degrees
        Core.transpose(mRgba, mRgbaT);
        Imgproc.resize(mRgbaT, mRgbaF, mRgbaF.size(), 0,0, 0);
        Core.flip(mRgbaF, mRgba, 2 );
        connector.sendFrame(mRgba);
        return mRgba;

        //return inputFrame;
    }*/
}
