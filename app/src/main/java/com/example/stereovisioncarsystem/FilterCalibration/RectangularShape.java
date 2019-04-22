package com.example.stereovisioncarsystem.FilterCalibration;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;


public class RectangularShape extends Shape {

    public RectangularShape(Point[] Points) {
        super(Points);
    }

    public Point getCenterPoint()
    {
        double xPos = (points[0].x + points[1].x + points[2].x + points[3].x)/4;
        double yPos = (points[0].y + points[1].y + points[2].y + points[3].y) / 4;

        return new Point(xPos, yPos);
    }

    public Point getAnchorPoint()
    {
        sortPointsByXAscending();
        if(points[0].y > points[1].y)
            return points[1];
        else
            return points[0];
    }

    private void sortPointsByXAscending() {
        for(int i = 0; i < points.length - 1; i++)
        {
            if(points[i+1].x < points[i].x)
            {
                Collections.swap(Arrays.asList(points),i+1,i);
            }
        }
    }




    @Override
    void drawShape(Mat dst) {
        for (int j = 0; j < 4; j++)
        {
            Imgproc.line(dst, points[j], points[(j + 1) % 4], new Scalar(255, 0, 0),5);
        }
        Imgproc.circle(dst, getCenterPoint(), 5, new Scalar(255, 0, 0), 10);
    }
}
