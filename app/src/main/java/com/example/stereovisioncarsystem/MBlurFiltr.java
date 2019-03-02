package com.example.stereovisioncarsystem;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;


public class MBlurFiltr extends Filtr {


    private int mBlurParam = 9;

    public MBlurFiltr()
    {

    }

    public MBlurFiltr(int mBlurParam)
    {
        this.mBlurParam = mBlurParam;
    }


    public void setmBlurParam(int mBlurParam) {
        this.mBlurParam = mBlurParam;
    }

    @Override
    public void filtr(Mat src)
    {
        Imgproc.medianBlur(src, src, mBlurParam);
    }

    @Override
    public void filtr(Mat src, Mat dst)
    {
        Imgproc.medianBlur(src, dst, mBlurParam);
    }


}
