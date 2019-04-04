package com.example.stereovisioncarsystem.Filtr;

import com.example.stereovisioncarsystem.Filtr.Filtr;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class BinaryThreshFiltr extends Filtr {

    private int binaryThreshParam = 140;

    public BinaryThreshFiltr() {
    }


    public BinaryThreshFiltr(int binaryThreshParam) {
        this.binaryThreshParam = binaryThreshParam;
    }

    void setBinaryThreshParam(int newParam)
    {
        binaryThreshParam  = newParam;
    }


    @Override
    public void filtr(Mat src) {
        Imgproc.threshold(src, src, binaryThreshParam, 255, 0);
    }

    @Override
    public void filtr(Mat src, Mat dst) {
        Imgproc.threshold(src, dst, binaryThreshParam, 255, 0);
    }
}
