package com.example.stereovisioncarsystem.FilterCalibration;

/**
 * Created by adamw on 25.11.2018.
 */
import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ContourCreator {

    private String TAG = "contoursCreator";
    private Mat src;
    private List<MatOfPoint> contours;
    private List<RotatedRect> minRect;
    private Random rng = new Random(12345);
    private List<Shape> shapesToDraw;
    private RectangularShape objectShape;


    public ContourCreator(Mat src)
    {
        this.src = src;
        shapesToDraw = new ArrayList<>();
        contours = new ArrayList<>();
        findContours();

    }

    private void findContours()
    {
        Imgproc.findContours(src, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        filterContours(200000,10000,3.5, 7);
    }

    private void filterContours(double maxArea, double minArea, double minRatio, double maxRatio) {
        Log.d(TAG, "Przed filtracją: " + contours.size());
        List<MatOfPoint> filteredContours = new ArrayList<>();

        for(MatOfPoint contour : contours)
        {
            Rect rect = Imgproc.boundingRect(contour);
            double area = rect.area();

            double ratio = rect.height/rect.width;
            if(ratio < 1)
                ratio = rect.width/rect.height;

            if(ratio < minRatio || ratio > maxArea)
                continue;
            if(area > maxArea || area < minArea)
                continue;
            filteredContours.add(contour);
        }
        contours.clear();
        contours = filteredContours;
        Log.d(TAG, "Po filtracji: " + contours.size());
    }

    public void drawRectangles(Mat dst)
    {
        if (minRect == null)
        {
            findRectangles();
        }
        if(minRect.isEmpty())
            return;
        for (int i = 0; i < minRect.size(); i++)
        {
            Scalar color = new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256));
            Point[] rectPoints = new Point[4];
            minRect.get(i).points(rectPoints);
            for (int j = 0; j < 4; j++) {
                Imgproc.line(dst, rectPoints[j], rectPoints[(j+1) % 4], color);
            }
        }
    }

    private void findRectangles()
    {
        minRect = new ArrayList<>(contours.size());
        if (contours.size() == 0)
        {
            return;
        }
        else
        {
            for (MatOfPoint contour : contours)
            {
                minRect.add(Imgproc.minAreaRect(new MatOfPoint2f(contour.toArray())));
            }
        }
    }

    public void addRectangularObject()
    {
        sortByArea(minRect);
        eliminateDuplicates(minRect, 0, 5);

        Point[] rect_points = new Point[4];

        if (minRect.size() > 0)
        {

            minRect.get(0).points(rect_points);
            objectShape = new RectangularShape(rect_points);
            shapesToDraw.add(objectShape);
        }
    }

    public void drawShapes(Mat dst)
    {
        if(shapesToDraw.isEmpty()) return;

        for(Shape shape : shapesToDraw)
        {
            shape.drawShape(dst);
        }
    }
    private void sortByArea(List<RotatedRect> rectangles)
    {
        for (int i = 0; i < rectangles.size()-1; i++)
        {
            for (int j = 0; j < rectangles.size() - 1; j++)
            {
                RotatedRect firstRect = rectangles.get(j + 1);
                RotatedRect secondRect = rectangles.get(j);
                if (firstRect.size.area() > secondRect.size.area())
                {
                    Collections.swap(rectangles, j+1,j);
                }
            }
        }
    }

    public void addAllRectangles()
    {
        if (minRect.size() == 0)
        {
            findRectangles();
        }
        for (int i = 0; i < minRect.size(); i++)
        {
            Point rectPoints[] = new Point[4];
            minRect.get(i).points(rectPoints);
            shapesToDraw.add(new RectangularShape(rectPoints));
        }
    }

    public void addText(Mat src, String textToAdd)
    {

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

    private void eliminateDuplicates(List<RotatedRect> rectangles, int dupIndex, int percentError)
    {
        double frameArea = rectangles.get(dupIndex).size.area();
        double max = frameArea + percentError * 0.01*frameArea;
        double min = frameArea - percentError * 0.01*frameArea;
        for (int i = 0; i < rectangles.size(); i++)
        {
            if (i == dupIndex)
            {
                continue;
            }
            else
            {
                double currentArea = rectangles.get(i).size.area();
                if ((currentArea > min) && (currentArea < max))
                {
                    rectangles.remove(i);
                }
            }
        }

    }


    public void findAndDrawObject(Mat dst)
    {
        if (minRect == null)
        {
            findRectangles();
        }
        if(minRect.isEmpty())
            return;

        addRectangularObject();
        drawShapes(dst);
    }

    public Point getAnchorPoint()
    {
        return objectShape.getAnchorPoint();
    }

    public boolean isEmpty() {
        return objectShape == null ? true : false;
    }
}
