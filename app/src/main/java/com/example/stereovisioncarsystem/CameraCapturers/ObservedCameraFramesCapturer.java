package com.example.stereovisioncarsystem.CameraCapturers;

import org.opencv.core.Mat;

public class ObservedCameraFramesCapturer extends CameraFramesCapturer {

    protected CameraFrameConnector connector;


    public interface CameraFrameConnector
    {
        void processServerFrame(Mat frame);
    }
    public ObservedCameraFramesCapturer(CameraFrameConnector connector)
    {
        this.connector = connector;
    }
}
