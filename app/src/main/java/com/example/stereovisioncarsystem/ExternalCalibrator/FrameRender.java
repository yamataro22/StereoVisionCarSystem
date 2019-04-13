package com.example.stereovisioncarsystem.ExternalCalibrator;

import com.example.stereovisioncarsystem.ExternalCalibrator.CameraCalibrator;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

public abstract class FrameRender {
    protected CameraCalibrator mCalibrator;

    public Mat render(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat rgbaFrame = inputFrame.rgba();
        Mat grayFrame = inputFrame.gray();
        mCalibrator.processFrame(grayFrame, rgbaFrame);

        return rgbaFrame;
    }
}
