package com.example.stereovisioncarsystem;

import com.example.stereovisioncarsystem.Filtr.Filtr;
import com.example.stereovisioncarsystem.Filtr.GrayFiltr;

import org.opencv.calib3d.StereoMatcher;
import org.opencv.calib3d.StereoSGBM;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point3;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.ximgproc.DisparityWLSFilter;
import org.opencv.ximgproc.Ximgproc;


class DisparityPhotoParser extends StereoPhotoParser{

    private Mat disparityReal;


    public DisparityPhotoParser(DistanceListener distanceListener) {
        super(distanceListener);
    }

    @Override
     protected void applyFilters(Mat frame) {
        if(frame.channels() > 1)
        {
            Filtr gray = new GrayFiltr();
            gray.filtr(frame);
        }
    }


    public void computeDisparityMap()
    {
        Mat disparity8 = new Mat();
        Mat filteredMat = new Mat();
        Mat dst = new Mat();

        int minDisparity = 1;
        int numDisparities = 256;
        StereoSGBM stereo = StereoSGBM.create(minDisparity,numDisparities,3,(int)(8*3*Math.pow(3,2)), (int)(32*3*Math.pow(3,2)),100,1,
                15,1000,16,1);
        StereoMatcher stereoR = Ximgproc.createRightMatcher(stereo);
        DisparityWLSFilter wlsFilter = Ximgproc.createDisparityWLSFilter(stereo);
        wlsFilter.setLambda(80000);
        wlsFilter.setSigmaColor(1.8);


        Mat disparityL = new Mat();
        Mat disparityR = new Mat();

        stereo.compute(serverFrame,clientFrame, disparityL);
        stereoR.compute(clientFrame,serverFrame, disparityR);

        disparityReal = new Mat(disparityL.rows(), disparityL.cols(),CvType.CV_32F);


        for(int i = 0; i < disparityL.rows(); i++)
        {
            for(int j = 0; j < disparityL.cols(); j++)
            {
                disparityReal.put(i,j,disparityL.get(i,j)[0]/16);
            }
        }


        Core.MinMaxLocResult minMaxLocResult = Core.minMaxLoc(disparityL);
        double range = 255/minMaxLocResult.maxVal - minMaxLocResult.minVal;
        disparityL.convertTo(disparity8, CvType.CV_8UC1,range);



        Mat kernel = Mat.zeros(new Size(3,3), CvType.CV_8U);
        Imgproc.morphologyEx(disparity8,disparity8,Imgproc.MORPH_CLOSE, kernel);

        wlsFilter.filter(disparityL,serverFrame,filteredMat,disparityR);
        Core.normalize(filteredMat, filteredMat,0,255,Core.NORM_MINMAX);
        filteredMat.convertTo(dst, CvType.CV_8UC3);
        Imgproc.applyColorMap(dst,dst,Imgproc.COLORMAP_HOT);

        distanceListener.onDisparityCalculated(dst, disparity8);
    }


    public double retreiveDisparity(int x, int y) {
        return disparityReal.get(y,x)[0];
    }

    public double calculateLenghtFromDisparity(int x, int y) {

        Point3 point3 = new Point3(x, y, disparityReal.get(y,x)[0]);

        MatOfPoint3f input = new MatOfPoint3f(point3);
        MatOfPoint3f output = new MatOfPoint3f();

        Core.perspectiveTransform(input, output, qMat);

        return output.toArray()[0].z;
    }

    @Override
    public void drawObjectOnClientFrame(Mat mat) {

    }

    @Override
    public void drawObjectOnServerFrame(Mat frame) {

    }

    @Override
    public void findDistanceBetweenObjects() {

    }

    @Override
    public void setFilterParams(int threshVal, int gaussVal, boolean isThreshInverted) {

    }

    @Override
    public void setConstrins(int minArea, int maxArea, double minRatio, double maxRatio) {

    }


}
