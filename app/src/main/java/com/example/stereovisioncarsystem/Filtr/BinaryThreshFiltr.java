package com.example.stereovisioncarsystem.Filtr;

import com.example.stereovisioncarsystem.Filtr.Filtr;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class BinaryThreshFiltr extends Filtr {

    private int binaryThreshParam = 140;
    private int type = 0;

    public BinaryThreshFiltr() {
    }


    public BinaryThreshFiltr(int binaryThreshParam) {
        this.binaryThreshParam = binaryThreshParam;
    }

    public BinaryThreshFiltr(int binaryThreshParam, boolean isInverted) {
        this.binaryThreshParam = binaryThreshParam;
        if(isInverted) makeInverted();
    }

    public void setBinaryThreshParam(int newParam)
    {
        binaryThreshParam  = newParam;
    }

    public void makeInverted()
    {
        type = Imgproc.THRESH_BINARY_INV;
    }

    @Override
    public void filtr(Mat src) {
        Imgproc.threshold(src, src, binaryThreshParam, 255, type);
    }

    @Override
    public void filtr(Mat src, Mat dst) {
        Imgproc.threshold(src, dst, binaryThreshParam, 255, type);
    }
}
