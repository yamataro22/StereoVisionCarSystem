package com.example.stereovisioncarsystem.CameraCapturers;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

public class ObservedSingleCameraFramesCapturer extends ObservedCameraFramesCapturer {

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
        if(shouldBeSent)
        {
            connector.processServerFrame(rgbaMat.clone());
            shouldBeSent = false;
        }
        return rgbaMat;
    }
}
