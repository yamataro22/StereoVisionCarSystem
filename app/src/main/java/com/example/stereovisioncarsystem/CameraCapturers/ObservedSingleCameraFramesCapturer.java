package com.example.stereovisioncarsystem.CameraCapturers;


import com.example.stereovisioncarsystem.Filtr.Filtr;
import com.example.stereovisioncarsystem.Filtr.GrayFiltr;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class ObservedSingleCameraFramesCapturer extends ObservedCameraFramesCapturer {

    private Filtr gray = new GrayFiltr();
    private boolean shouldBeSent = false;

    public ObservedSingleCameraFramesCapturer(CameraFrameConnector connector) {
        super(connector);
    }

    public void getSingleFrameToBeProcessed()
    {
        shouldBeSent = true;
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        Core.transpose(mRgba, mRgbaT);
        Imgproc.resize(mRgbaT, mRgbaF, mRgbaF.size(), 0,0, 0);
        Core.flip(mRgbaF, mRgba, -1 );
        gray.filtr(mRgba);

        if(shouldBeSent)
        {
            connector.sendFrame(mRgba.clone());
            shouldBeSent = false;
        }

        return mRgba;
    }
}
