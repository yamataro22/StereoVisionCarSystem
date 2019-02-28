package com.example.stereovisioncarsystem;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.*;
import org.opencv.core.CvType;

/**
 * Created by adamw on 13.11.2018.
 */

public class Filtr {

    private int mBlurParam = 9;
    private int gBlurMSize = 3;
    private int threshBinaryParam = 140;
    private cannyParams cannyParam = new cannyParams(50,3,3);
    private threshRGBParams threshRGBParam = new threshRGBParams(30,50,30,50,30,50);

    public enum filters {GRAY,MBLUR,GAUSS,SHARPEN,THRESHBINARY,THRESHRGB,CANNY};


    public void filtr(filters which, Mat src)
    {
        switch(which)
        {
            case GRAY:
                gray(src);
                break;
            case MBLUR:
                mBlur(src);
                break;
            case THRESHBINARY:
                thresh(src);
                break;
            case GAUSS:
                gBlur(src);
                break;
            case SHARPEN:
                sharp(src);
                break;
            case THRESHRGB:
                threshColor(src);
                break;
        }
    }

    void filtr(filters which, Mat src, Mat dst)
    {
        switch(which)
        {
            case GRAY:
                gray(src,dst);
                break;
            case MBLUR:
                mBlur(src,dst);
                break;
            case THRESHBINARY:
                thresh(src,dst);
                break;
            case GAUSS:
                gBlur(src,dst);
                break;
            case SHARPEN:
                sharp(src,dst);
                break;
            case THRESHRGB:
                threshColor(src,dst);
                break;
        }
    }

    void sobel(Mat src, Mat dst)
    {
        Mat grad_x = new Mat();
        Mat grad_y = new Mat();
        Mat abs_grad_x = new Mat();
        Mat abs_grad_y = new Mat();
        sobelParams parametry = new sobelParams(CvType.CV_16S, 1, 0, 1);
        Imgproc.Sobel(src, grad_x, parametry.ddepth, 1, 0, parametry.ksize, parametry.scale, parametry.delta, Core.BORDER_DEFAULT);
        Imgproc.Sobel(src, grad_y, parametry.ddepth, 0, 1, parametry.ksize, parametry.scale, parametry.delta, Core.BORDER_DEFAULT);

        Core.convertScaleAbs(grad_x, abs_grad_x);
        Core.convertScaleAbs(grad_y, abs_grad_y);
        Core.addWeighted(abs_grad_x, 0.5, abs_grad_y, 0.5, 0, dst);
    }

    void sobel(sobelParams params, Mat src, Mat dst)
    {
        Mat grad_x = new Mat();
        Mat grad_y = new Mat();
        Mat abs_grad_x = new Mat();
        Mat abs_grad_y = new Mat();
        Imgproc.Sobel(src, grad_x, params.ddepth, 1, 0, params.ksize, params.scale, params.delta, Core.BORDER_DEFAULT);
        Imgproc.Sobel(src, grad_y, params.ddepth, 0, 1, params.ksize, params.scale, params.delta, Core.BORDER_DEFAULT);

        Core.convertScaleAbs(grad_x, abs_grad_x);
        Core.convertScaleAbs(grad_y, abs_grad_y);
        Core.addWeighted(abs_grad_x, 0.5, abs_grad_y, 0.5, 0, dst);
    }
    void canny(Mat src, Mat dst)
    {
        Imgproc.Canny(src, dst, cannyParam.cannyThresh, cannyParam.cannyThresh*cannyParam.cannyRatio, cannyParam.cannyKernel);
    }
    void canny(cannyParams params, Mat src, Mat dst)
    {
        Imgproc.Canny(src, dst, params.cannyThresh, params.cannyThresh*params.cannyRatio, params.cannyKernel);
    }

    void setmBlurParam(int newParam)
    {
        mBlurParam = newParam;
    }
    void setgBlurParam(int newParam)
    {
        gBlurMSize = newParam;
    }
    void setThreshBinaryParam(int newParam)
    {
        threshBinaryParam = newParam;
    }

    public class cannyParams
    {
        int cannyThresh;
	    final int cannyMaxThresh = 100;
        int cannyRatio;
        int	cannyKernel;

        cannyParams(int cThresh, int cRatio, int cKernel)
        {
            cannyThresh = cThresh;
            cannyRatio = cRatio;
            cannyKernel = cKernel;
        }
    }
    public class threshRGBParams
    {
        public int low_b;
        public int low_r;
        public int low_g;
        public int high_b;
        public int high_r;
        public int high_g;
        threshRGBParams(int lb, int lg, int lr, int hb, int hg, int hr)
        {
            low_b = lb;
            low_r = lr;
            low_g = lg;
            high_b = hb;
            high_r = hr;
            high_g = hg;
        }
    }
    public class sobelParams
    {
        int ddepth; //głębia obrazu
        int ksize;
        int delta;
        int scale;

        sobelParams(int ddep, int ksiz, int del, int sc)
        {
            ddepth = ddep; //głębia obrazu
            ksize = ksiz;
            delta = del;
            scale = sc;
        }
    }

    private void gray(Mat src)
    {
        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2GRAY);
    }
    private void gray(Mat src, Mat dst)
    {
        Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGR2GRAY);
    }
    private void thresh(Mat src)
    {
        Imgproc.threshold(src, src, threshBinaryParam, 255, 0);
    }
    private void thresh(Mat src, Mat dst)
    {
        Imgproc.threshold(src, dst, threshBinaryParam, 255, 0);
    }
    private void mBlur(Mat src)
    {
        Imgproc.medianBlur(src, src, mBlurParam);
    }
    private void mBlur(Mat src, Mat dst)
    {
        Imgproc.medianBlur(src, dst, mBlurParam);
    }
    private void gBlur(Mat src)
    {
        Imgproc.GaussianBlur(src, src, new Size((double)gBlurMSize, (double)gBlurMSize), 0);
    }
    private void gBlur(Mat src, Mat dst)
    {
        Log.i("dupa", "robiem blura:" + gBlurMSize);
        Imgproc.GaussianBlur(src, dst, new Size((double)gBlurMSize, (double)gBlurMSize), 0 );
    }
    private void sharp(Mat src)
    {
        Mat kernel = new Mat();
        int row = 0, col = 0;
        kernel.put(row ,col, 0, -1, 0, -1, 5, -1, 0, -1, 0 );

        int ddepth = -1;
        double delta = 0.0;
        Point anchor = new Point( -1, -1);
        Imgproc.filter2D(src, src, ddepth , kernel, anchor, delta, Core.BORDER_DEFAULT );
    }
    private void sharp(Mat src, Mat dst)
    {
        Mat kernel = new Mat();
        int row = 0, col = 0;
        kernel.put(row ,col, 0, -1, 0, -1, 5, -1, 0, -1, 0 );

        int ddepth = -1;
        double delta = 0.0;
        Point anchor = new Point( -1, -1);
        Imgproc.filter2D(src, dst, ddepth , kernel, anchor, delta, Core.BORDER_DEFAULT );
    }
    private void threshColor(Mat src)
    {
        Core.inRange(src, new Scalar(threshRGBParam.low_b, threshRGBParam.low_g, threshRGBParam.low_r)
                , new Scalar(threshRGBParam.high_b, threshRGBParam.high_g, threshRGBParam.high_r), src);
    }
    private void threshColor(Mat src, Mat dst)
    {
        Core.inRange(src, new Scalar(threshRGBParam.low_b, threshRGBParam.low_g, threshRGBParam.low_r)
                , new Scalar(threshRGBParam.high_b, threshRGBParam.high_g, threshRGBParam.high_r), dst);
    }
    static String determineType(Mat src)
    {
        /*
        +--------+----+----+----+----+------+------+------+------+
        |        | C1 | C2 | C3 | C4 | C(5) | C(6) | C(7) | C(8) |
        +--------+----+----+----+----+------+------+------+------+
        | CV_8U  |  0 |  8 | 16 | 24 |   32 |   40 |   48 |   56 |
        | CV_8S  |  1 |  9 | 17 | 25 |   33 |   41 |   49 |   57 |
        | CV_16U |  2 | 10 | 18 | 26 |   34 |   42 |   50 |   58 |
        | CV_16S |  3 | 11 | 19 | 27 |   35 |   43 |   51 |   59 |
        | CV_32S |  4 | 12 | 20 | 28 |   36 |   44 |   52 |   60 |
        | CV_32F |  5 | 13 | 21 | 29 |   37 |   45 |   53 |   61 |
        | CV_64F |  6 | 14 | 22 | 30 |   38 |   46 |   54 |   62 |
        +--------+----+----+----+----+------+------+------+------+
         */
        return Integer.toString(src.type());
    }
}
