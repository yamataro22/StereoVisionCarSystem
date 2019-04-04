package com.example.stereovisioncarsystem.Filtr;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class GrayFiltr extends Filtr {

    @Override
    public void filtr(Mat src) {
        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2GRAY);
    }


    @Override
    public void filtr(Mat src, Mat dst) {
        Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGR2GRAY);
    }
}
