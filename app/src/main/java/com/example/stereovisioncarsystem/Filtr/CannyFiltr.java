package com.example.stereovisioncarsystem.Filtr;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class CannyFiltr extends Filtr {

    private CannyParams cannyParams = new CannyParams(50,3,3);

    @Override
    public void filtr(Mat src) {
        Mat dst = new Mat(src.rows(), src.cols(), src.type());
        makeCanny(cannyParams, src, dst);
        dst.copyTo(src);
    }

    @Override
    public void filtr(Mat src, Mat dst)
    {
        makeCanny(cannyParams, src,dst);
    }

    public void filtr(CannyParams params, Mat src, Mat dst)
    {
        makeCanny(params,src,dst);
    }

    private void makeCanny(CannyParams params, Mat src, Mat dst)
    {
        Imgproc.Canny(src, dst, params.cannyThresh, params.cannyThresh*params.cannyRatio, params.cannyKernel, true);
    }

    public class CannyParams
    {
        int cannyThresh;
        final int cannyMaxThresh = 100;
        int cannyRatio;
        int	cannyKernel;

        CannyParams(int cThresh, int cRatio, int cKernel)
        {
            cannyThresh = cThresh;
            cannyRatio = cRatio;
            cannyKernel = cKernel;
        }
    }

}
