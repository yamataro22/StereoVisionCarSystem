package com.example.stereovisioncarsystem.CameraCapturers;
import android.os.SystemClock;

import com.example.stereovisioncarsystem.Filtr.Filtr;
import com.example.stereovisioncarsystem.Filtr.GrayFiltr;

import org.opencv.android.*;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;


public class ObservedRotatedCameraFramesCapturer extends ObservedCameraFramesCapturer {

    Filtr gray = new GrayFiltr();

    public ObservedRotatedCameraFramesCapturer(CameraFrameConnector connector) {
        super(connector);
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
        SystemClock.sleep(60);
        return mRgba;
    }

}
