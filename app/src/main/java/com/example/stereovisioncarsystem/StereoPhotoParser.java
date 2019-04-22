package com.example.stereovisioncarsystem;

import android.util.Log;

import com.example.stereovisioncarsystem.FilterCalibration.ContourCreator;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Point3;

class StereoPhotoParser {
    private ContourCreator clientContourCreator;
    private ContourCreator serverContourCreator;

    private Mat clientFrame;
    private Mat serverFrame;

    public StereoPhotoParser() {
        init();
    }

    private void init() {
        clientFrame = new Mat();
        serverFrame = new Mat();
    }

    public void addClientFrame(Mat mat) {
        mat.copyTo(clientFrame);
    }

    public void drawObjectOnClientFrame(Mat mat) {
        clientContourCreator = new ContourCreator(mat);
        clientContourCreator.findAndDrawObject(mat);
    }

    public void addServerFrame(Mat frame) {
        frame.copyTo(serverFrame);
    }

    public void drawObjectOnServerFrame(Mat frame) {
        serverContourCreator = new ContourCreator(frame);
        serverContourCreator.findAndDrawObject(frame);
    }

    public void findDistanceBetweenObjects() {
        Point clientPoint = clientContourCreator.getAnchorPoint();
        Point serverPoint = serverContourCreator.getAnchorPoint();

        Point3 point3 = new Point3(serverPoint.x, serverPoint.y, Math.abs(serverPoint.x - clientPoint.x));
        Log.d("DZIALA", "odległość w pixelach: " + point3.z);
    }
}
