package com.example.stereovisioncarsystem;

import android.util.Log;

import com.example.stereovisioncarsystem.FilterCalibration.ContourCreator;
import com.example.stereovisioncarsystem.Filtr.BinaryThreshFiltr;
import com.example.stereovisioncarsystem.Filtr.CannyFiltr;
import com.example.stereovisioncarsystem.Filtr.Filtr;
import com.example.stereovisioncarsystem.Filtr.GBlurFiltr;
import com.example.stereovisioncarsystem.Filtr.GrayFiltr;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;

import java.util.ArrayList;
import java.util.List;

public class FeaturePhotoParser extends StereoPhotoParser{

    private ContourCreator clientContourCreator;
    private ContourCreator serverContourCreator;

    private int maxArea = 100000;
    private int minArea = 10000;
    private double maxRatio = 5;
    private double minRatio = 1.2;

    int threshVal  = 120;
    int gaussVal = 3;
    boolean isThreshInverted;



    public FeaturePhotoParser(DistanceListener distanceListener) {
        super(distanceListener);
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



    public void drawObjectOnClientFrame(Mat mat) {
        clientContourCreator = new ContourCreator(mat);
        clientContourCreator.setConstrains(minArea,maxArea,minRatio,maxRatio);
        clientContourCreator.findAndDrawObject(mat);
    }

    public void drawObjectOnServerFrame(Mat frame) {
        serverContourCreator = new ContourCreator(frame);
        serverContourCreator.setConstrains(minArea,maxArea,minRatio,maxRatio);
        serverContourCreator.findAndDrawObject(frame);
    }


    @Override
    protected void applyFilters(Mat mat) {
        List<Filtr> filters = new ArrayList<>();
        if(mat.channels() > 1) filters.add(new GrayFiltr());
        filters.add(new GBlurFiltr(gaussVal));
        filters.add(new BinaryThreshFiltr(threshVal,isThreshInverted));
        filters.add(new CannyFiltr());

        for(Filtr f : filters)
        {
            f.filtr(mat);
        }
    }

    public void findDistanceBetweenObjects() {
        if(clientContourCreator.isEmpty() || serverContourCreator.isEmpty()) return;

        Point clientPoint = clientContourCreator.getAnchorPoint();
        Point serverPoint = serverContourCreator.getAnchorPoint();

        Point3 point3 = new Point3(serverPoint.x, serverPoint.y, Math.abs(serverPoint.x - clientPoint.x));
        Log.d("DZIALA", "odległość w pixelach: " + point3.z);

        MatOfPoint3f input = new MatOfPoint3f(point3);
        MatOfPoint3f output = new MatOfPoint3f();

        /* Transforming 2D to 3D coords, using Qmat*/
        Core.perspectiveTransform(input, output, qMat);

        distanceListener.onDistanceCalculated(point3.z, output.toArray()[0].z);
    }

    @Override
    public void computeDisparityMap() {

    }

    @Override
    public double retreiveDisparity(int x, int y) {
        return 0;
    }

    @Override
    public double calculateLenghtFromDisparity(int x, int y) {
        return 0;
    }


}
