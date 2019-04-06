package com.example.stereovisioncarsystem.CameraCapturers;


import android.content.res.Configuration;

import com.example.stereovisioncarsystem.CameraFramesFlipper;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

public class CameraFramesCapturer implements CameraBridgeViewBase.CvCameraViewListener2 {
    protected Mat rgbaMat;
    protected CameraFramesFlipper flipper;
    protected int cameraID = CameraBridgeViewBase.CAMERA_ID_BACK;
    protected int orientation = Configuration.ORIENTATION_PORTRAIT;


    public void setCameraOrientation(int cameraID)
    {
        this.cameraID = cameraID;
    }
    public void setCameraOrientation(int cameraID, int orientation)
    {
        this.cameraID = cameraID;
        this.orientation = orientation;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

        flipper = new CameraFramesFlipper(width,height);

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        rgbaMat = inputFrame.rgba();
        flipper.adjustCameraFrames(rgbaMat,cameraID, orientation);
        return rgbaMat;
    }



}
