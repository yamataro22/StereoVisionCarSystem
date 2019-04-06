package com.example.stereovisioncarsystem.CameraCapturers;

import com.example.stereovisioncarsystem.Filtr.Filtr;
import com.example.stereovisioncarsystem.Filtr.GrayFiltr;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

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
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
    {
        super.onCameraFrame(inputFrame);
        gray.filtr(rgbaMat);
        if(shouldBeSent)
        {
            connector.sendFrame(rgbaMat.clone());
            shouldBeSent = false;
        }
        return rgbaMat;
    }
}
