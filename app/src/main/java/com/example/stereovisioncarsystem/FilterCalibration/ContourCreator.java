package com.example.stereovisioncarsystem.FilterCalibration;

/**
 * Created by adamw on 25.11.2018.
 */
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.List;
import java.util.Random;

public class ContourCreator {

    public ContourCreator(Mat src)
    {
        this.src = src;
        findContours();
    }

    public void addFrame()
    {

    }
    public void addCoordinateSystem()
    {

    }
    public void addObject()
    {

    }
    public void addAllRectangles()
    {
        if (minRect.length == 0)
        {
            findRectangles();
        }
    }

    public void addText(Mat src, String textToAdd)
    {

    }

    public void drawShapes(Mat dst)
    {
        if (minRect == null)
        {
            findRectangles();
        }
        for (int i = 0; i < minRect.length; i++)
        {
            Scalar color = new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256));
            Point[] rectPoints = new Point[4];
            minRect[i].points(rectPoints);
            //shapesToDraw.push_back(new RamkaPodloza(rectPoints));
            for (int j = 0; j < 4; j++) {
                Imgproc.line(dst, rectPoints[j], rectPoints[(j+1) % 4], color);
            }
        }
    }
    public void drawContoursOnly(Mat dst)
    {
        dst.create(dst.size(), CvType.CV_8UC3);

        for (int i = 0; i < contours.size(); i++)
        {
            Imgproc.drawContours(dst, contours, i, new Scalar(0, 255, 0));
        }
    }
    public double getRelativeObjectCoords()
    {

        return 0.0;
    }

    public double getAbsoluteObjectCoords(double width, double height)
    {

        return 0.0;
    }



    private Mat src;
    private List<MatOfPoint> contours;
    private RotatedRect[] minRect;
    private Random rng = new Random(12345);


    private void findContours()
    {
        Imgproc.findContours(src, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
    }
    private void findRectangles()
    {
        if (contours.size() == 0)
        {
            return;
        }
        else
        {
            minRect = new RotatedRect[contours.size()];
            for (int i = 0; i < contours.size(); i++)
            {
                minRect[i] = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(i).toArray()));
            }
        }
    }

}
