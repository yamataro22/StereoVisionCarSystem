package com.example.stereovisioncarsystem.FilterCalibration;

import org.opencv.core.Mat;
import org.opencv.core.Point;

/**
 * Created by adamw on 25.11.2018.
 */

public abstract class Shape {
    public Shape(Point[] Points)
    {
        this.points = Points;
    }

    abstract void drawShape(Mat dst);

    protected Point[] points;
    protected int lineThickness = 8;
}
