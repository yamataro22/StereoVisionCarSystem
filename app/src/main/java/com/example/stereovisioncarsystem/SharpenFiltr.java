package com.example.stereovisioncarsystem;

import android.view.ScaleGestureDetector;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

public class SharpenFiltr extends Filtr{

    private int ddepth = -1;
    private double delta = 0.0;
    private Point anchor = new Point( -1, -1);


    public SharpenFiltr()
    {
    }

    @Override
    public void filtr(Mat src)
    {
        Imgproc.filter2D(src, src, ddepth , createKerner(), anchor, delta, Core.BORDER_DEFAULT );
    }

    @Override
    public void filtr(Mat src, Mat dst)
    {
        Imgproc.filter2D(src, dst, ddepth , createKerner(), anchor, delta, Core.BORDER_DEFAULT );
    }

    private Mat createKerner()
    {
        Mat kernel = new Mat();
        int row = 0, col = 0;
        kernel.put(row ,col, 0, -1, 0, -1, 5, -1, 0, -1, 0 );
        return kernel;
    }
}
