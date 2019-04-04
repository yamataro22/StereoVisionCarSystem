package com.example.stereovisioncarsystem.Filtr;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

public class RGBThreshFiltr extends Filtr{

    private RGBThreshParams threshRGBParam = new RGBThreshParams(30,50,30,50,30,50);


    @Override
    public void filtr(Mat src) {
        Core.inRange(src, new Scalar(threshRGBParam.low_b, threshRGBParam.low_g, threshRGBParam.low_r)
                , new Scalar(threshRGBParam.high_b, threshRGBParam.high_g, threshRGBParam.high_r), src);
    }

    @Override
    public void filtr(Mat src, Mat dst) {
        Core.inRange(src, new Scalar(threshRGBParam.low_b, threshRGBParam.low_g, threshRGBParam.low_r)
                , new Scalar(threshRGBParam.high_b, threshRGBParam.high_g, threshRGBParam.high_r), dst);
    }




    public class RGBThreshParams
    {
        public int low_b;
        public int low_r;
        public int low_g;
        public int high_b;
        public int high_r;
        public int high_g;

        RGBThreshParams(int lb, int lg, int lr, int hb, int hg, int hr)
        {
            low_b = lb;
            low_r = lr;
            low_g = lg;
            high_b = hb;
            high_r = hr;
            high_g = hg;
        }
    }




}
