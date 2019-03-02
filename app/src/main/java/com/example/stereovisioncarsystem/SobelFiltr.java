package com.example.stereovisioncarsystem;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class SobelFiltr extends Filtr {

    SobelParams parametry = new SobelParams(CvType.CV_16S, 1, 0, 1);


    @Override
    public void filtr(Mat src) {
        Mat dst = new Mat();
        makeSobel(parametry,src,dst);
        dst.copyTo(src);
    }

    @Override
    public void filtr(Mat src, Mat dst)
    {
        makeSobel(parametry,src,dst);
    }

    void filtr(SobelParams params, Mat src, Mat dst)
    {
        makeSobel(params, src, dst);
    }

    public void makeSobel(SobelParams params, Mat src, Mat dst)
    {
        Mat grad_x = new Mat();
        Mat grad_y = new Mat();
        Mat abs_grad_x = new Mat();
        Mat abs_grad_y = new Mat();

        Imgproc.Sobel(src, grad_x, parametry.ddepth, 1, 0, parametry.ksize, parametry.scale, parametry.delta, Core.BORDER_DEFAULT);
        Imgproc.Sobel(src, grad_y, parametry.ddepth, 0, 1, parametry.ksize, parametry.scale, parametry.delta, Core.BORDER_DEFAULT);

        Core.convertScaleAbs(grad_x, abs_grad_x);
        Core.convertScaleAbs(grad_y, abs_grad_y);
        Core.addWeighted(abs_grad_x, 0.5, abs_grad_y, 0.5, 0, dst);
    }


    public class SobelParams
    {
        int ddepth; //głębia obrazu
        int ksize;
        int delta;
        int scale;

        SobelParams(int ddep, int ksiz, int del, int sc)
        {
            ddepth = ddep; //głębia obrazu
            ksize = ksiz;
            delta = del;
            scale = sc;
        }
    }
}
