package com.example.stereovisioncarsystem;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class GBlurFiltr extends Filtr {

    private int gBlurMatrixSize = 3;


    public GBlurFiltr() {
    }

    public GBlurFiltr(int gBlurMatrixSize)
    {
        this.gBlurMatrixSize = gBlurMatrixSize;
    }

    void setgBlurMatrixSize(int newParam)
    {
        gBlurMatrixSize = newParam;
    }

    @Override
    public void filtr(Mat src) {
        Imgproc.GaussianBlur(src, src, new Size((double)gBlurMatrixSize, (double)gBlurMatrixSize), 0);
    }

    @Override
    public void filtr(Mat src, Mat dst) {
        Imgproc.GaussianBlur(src, dst, new Size((double)gBlurMatrixSize, (double)gBlurMatrixSize), 0 );
    }
}
