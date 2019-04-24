package com.example.stereovisioncarsystem;

import android.util.Log;

import com.example.stereovisioncarsystem.FilterCalibration.ContourCreator;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;

class StereoPhotoParser {

    private final DistanceListener distanceListener;

    private ContourCreator clientContourCreator;
    private ContourCreator serverContourCreator;

    private Mat clientFrame;
    private Mat serverFrame;
    private Mat qMat;

    public StereoPhotoParser(DistanceListener distanceListener)
    {
        this.distanceListener = distanceListener;
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
        if(clientContourCreator.isEmpty() || serverContourCreator.isEmpty()) return;

        Point clientPoint = clientContourCreator.getAnchorPoint();
        Point serverPoint = serverContourCreator.getAnchorPoint();

        Point3 point3 = new Point3(serverPoint.x, serverPoint.y, Math.abs(serverPoint.x - clientPoint.x));
        Log.d("DZIALA", "odległość w pixelach: " + point3.z);

        MatOfPoint3f input = new MatOfPoint3f(point3);
        MatOfPoint3f output = new MatOfPoint3f();

        /* Transform 2D coordinates to 3D coordinates, using Q matrix */
        Core.perspectiveTransform(input, output, qMat);

        Log.d("DZIALA", "input: " + input.dump());
        Log.d("DZIALA", "output: " + output.dump());
        Log.d("DZIALA", "output: " + qMat.dump());

        distanceListener.onDistanceCalculated(point3.z, output.toArray()[0].z);



    }

    public void setQMat(Mat QMat) {
        this.qMat = QMat;
    }

    public interface DistanceListener
    {
        void onDistanceCalculated(double distanceInPixels, double distanceInLength);
    }

}
