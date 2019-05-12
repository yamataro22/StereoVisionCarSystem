package com.example.stereovisioncarsystem;

import android.util.Log;

import com.example.stereovisioncarsystem.FilterCalibration.ContourCreator;
import com.example.stereovisioncarsystem.Filtr.BinaryThreshFiltr;
import com.example.stereovisioncarsystem.Filtr.CannyFiltr;
import com.example.stereovisioncarsystem.Filtr.Filtr;
import com.example.stereovisioncarsystem.Filtr.GBlurFiltr;
import com.example.stereovisioncarsystem.Filtr.GrayFiltr;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

class StereoPhotoParser {

    private final DistanceListener distanceListener;

    private ContourCreator clientContourCreator;
    private ContourCreator serverContourCreator;

    private Mat clientFrame;
    private Mat serverFrame;
    private Mat qMat;

    int threshVal  = 120;
    int gaussVal = 3;
    boolean isThreshInverted;

    private int maxArea = 100000;
    private int minArea = 10000;
    private double maxRatio = 5;
    private double minRatio = 1.2;



    Mat map1c;
    Mat map2c;
    Mat map1s;
    Mat map2s;
    Size picSize;
    Mat R1, R2, P1, P2;
    CameraData clientCameraData, serverCameraData;

    private final String TAG = "stereoParser";

    public StereoPhotoParser(DistanceListener distanceListener)
    {
        this.distanceListener = distanceListener;
        init();
    }

    public void setConstrins(int minArea, int maxArea, double minRatio, double maxRatio)
    {
        this.minArea = minArea;
        this.maxArea = maxArea;
        this.maxRatio = maxRatio;
        this.minRatio = minRatio;
    }


    public void setFilterParams(int threshVal, int gaussVal, boolean isThreshInverted)
    {
        this.threshVal = threshVal;
        this.gaussVal = gaussVal;
        this.isThreshInverted = isThreshInverted;
    }

    public void setCameraData(CameraData clientCameraData, CameraData serverCameraData)
    {
        this.clientCameraData = clientCameraData;
        this.serverCameraData = serverCameraData;
    }

    private void init() {
        clientFrame = new Mat();
        serverFrame = new Mat();
    }

    public void addClientFrame(Mat mat) {
        applyFilters(mat);
        if(picSize == null || map1c == null)
        {
            Log.d(TAG, "i'm calculation maps for client");
            picSize = mat.size();
            initClientRectificationMaps();
        }

        rectifyClientFrame(mat);
        mat.copyTo(clientFrame);
    }


    private void initClientRectificationMaps() {
        map1c=new Mat();
        map2c=new Mat();
        Imgproc.initUndistortRectifyMap(clientCameraData.getCameraMatrixMat(),clientCameraData.getDistCoeffsMat(),R2, P2,picSize, CvType.CV_16SC2, map1c, map2c);
        Log.d(TAG, "map1c size: " + map1c.size().toString());
    }

    public void drawObjectOnClientFrame(Mat mat) {
        clientContourCreator = new ContourCreator(mat);
        clientContourCreator.setConstrains(minArea,maxArea,minRatio,maxRatio);
        clientContourCreator.findAndDrawObject(mat);
    }

    public void addServerFrame(Mat frame) {
        applyFilters(frame);
        if(picSize == null || map1s == null)
        {
            Log.d(TAG, "i'm calculation maps for server");
            picSize = frame.size();
            initServerRectificationMaps();
        }

        rectifyServerFrame(frame);
        frame.copyTo(serverFrame);
    }

    private void initServerRectificationMaps() {
        map1s=new Mat();
        map2s=new Mat();
        Log.d(TAG, "created server maps");
        Imgproc.initUndistortRectifyMap(serverCameraData.getCameraMatrixMat(),serverCameraData.getDistCoeffsMat(),R1, P1,picSize, CvType.CV_16SC2, map1s, map2s);
        Log.d(TAG, "map1s size: " + map1s.size().toString());
    }

    private void applyFilters(Mat frame) {

        List<Filtr> filters = new ArrayList<>();
        if(frame.channels() > 1) filters.add(new GrayFiltr());
        filters.add(new GBlurFiltr(gaussVal));
        filters.add(new BinaryThreshFiltr(threshVal,isThreshInverted));
        filters.add(new CannyFiltr());

        for(Filtr f : filters)
        {
            f.filtr(frame);
        }
    }

    public void drawObjectOnServerFrame(Mat frame) {
        serverContourCreator = new ContourCreator(frame);
        serverContourCreator.setConstrains(minArea,maxArea,minRatio,maxRatio);
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

    public void setRectificationParams(Mat r1, Mat r2, Mat p1, Mat p2) {
        R1 = r1;
        R2 = r2;
        P1 = p1;
        P2 = p2;
    }

    private void rectifyServerFrame(Mat frame) {
        Imgproc.remap(frame,frame,map1s,map2s, Imgproc.INTER_LINEAR);
    }

    private void rectifyClientFrame(Mat frame) {
        Imgproc.remap(frame,frame,map1c,map2c, Imgproc.INTER_LINEAR);
    }

    public interface DistanceListener
    {
        void onDistanceCalculated(double distanceInPixels, double distanceInLength);
    }

}
