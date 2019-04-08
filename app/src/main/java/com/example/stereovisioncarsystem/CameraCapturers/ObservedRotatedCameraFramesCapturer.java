package com.example.stereovisioncarsystem.CameraCapturers;
import android.os.SystemClock;

import com.example.stereovisioncarsystem.Filtr.Filtr;
import com.example.stereovisioncarsystem.Filtr.GrayFiltr;

import org.opencv.android.*;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;


public class ObservedRotatedCameraFramesCapturer extends ObservedCameraFramesCapturer {


    int freshRate = 200;
    Filtr gray = new GrayFiltr();

    public ObservedRotatedCameraFramesCapturer(CameraFrameConnector connector) {
        super(connector);
    }

    public void setFreshRate(int freshRate) {
        this.freshRate = freshRate;
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
    {
        super.onCameraFrame(inputFrame);
        gray.filtr(rgbaMat);
        connector.sendFrame(rgbaMat);
        SystemClock.sleep(freshRate);
        return rgbaMat;
    }

}