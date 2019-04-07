package com.example.stereovisioncarsystem;

import android.graphics.Bitmap;
import android.util.Log;

import com.example.stereovisioncarsystem.Filtr.Filtr;
import com.example.stereovisioncarsystem.Filtr.GrayFiltr;

import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point3;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

class Calibrator {
    Mat[] colorFrames;
    Mat[] grayFrames;
    Mat[] undistortedFrames;
    public static int HOW_MANY_FRAMES = 0;

    private int index = 0;
    int numCornersHor = 7;
    int numCornersVer = 5;
    private Size boardSize;
    private MatOfPoint3f obj;
    private MatOfPoint2f imageCorners;
    private Mat intrinsic;
    private Mat distCoeffs;
    private List<Mat> imagePoints;
    private List<Mat> objectPoints;
    private Mat tempSavedImage;

    private Filtr filtr;


    private boolean isCalibrated = false;



    public Calibrator(int howManyFrames)
    {
        HOW_MANY_FRAMES = howManyFrames;
        colorFrames = new Mat[HOW_MANY_FRAMES];
        grayFrames = new Mat[HOW_MANY_FRAMES];
        undistortedFrames = new Mat[HOW_MANY_FRAMES];

        imageCorners = new MatOfPoint2f();
        obj = new MatOfPoint3f();
        filtr = new GrayFiltr();
        imagePoints = new ArrayList<>();
        objectPoints = new ArrayList<>();
        intrinsic = new Mat(3, 3, CvType.CV_32FC1);
        distCoeffs = new Mat();
        tempSavedImage = new Mat();
        boardSize = new Size(this.numCornersHor, this.numCornersVer);

        int numSquares = this.numCornersHor * this.numCornersVer;
        for (int j = 0; j < numSquares; j++)
            obj.push_back(new MatOfPoint3f(new Point3(j / this.numCornersHor, j % this.numCornersVer, 0.0f)));

    }


    public void processFrame(Mat frame)
    {
        addFrameToColorFrames(frame);
        addFrameToGrayFrames(frame);
        index++;
    }

    private void addFrameToColorFrames(Mat frame) {
        colorFrames[index]=frame;
    }


    private void addFrameToGrayFrames(Mat frame) {
        Mat grayFrame;

        grayFrame = frame.clone();
        filtr.filtr(grayFrame);
        grayFrames[index]=grayFrame;
    }


    public Bitmap getColorPhotoByIndex(int index)
    {
        if(index > colorFrames.length) return null;
        if(!isCalibrated) return mat2Bitmap(colorFrames[index]);
        else return mat2Bitmap(undistortedFrames[index]);
    }

    public Bitmap getGrayPhotoByIndex(int index)
    {
        if(index > grayFrames.length) return null;
        return mat2Bitmap(grayFrames[index]);
    }

    public Bitmap getUndistortedPhotoByIndex(int index)
    {
        if(index > grayFrames.length) return null;
        return mat2Bitmap(undistortedFrames[index]);
    }

    public void performUndisortion()
    {
        //findChessboards();
        calculateCameraParameters();
        undistortImages();
    }

    private Bitmap mat2Bitmap(Mat mat)
    {
        Bitmap btm = Bitmap.createBitmap(mat.cols(), mat.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat,btm);
        return btm;
    }


    private int findChessboards()
    {
        TermCriteria term = new TermCriteria(TermCriteria.EPS | TermCriteria.MAX_ITER, 30, 0.1);

        int howManyFound = 0;
        for(Mat frame : grayFrames)
        {
            boolean found = Calib3d.findChessboardCorners(frame, boardSize, imageCorners,
                    Calib3d.CALIB_CB_ADAPTIVE_THRESH + Calib3d.CALIB_CB_NORMALIZE_IMAGE + Calib3d.CALIB_CB_FAST_CHECK);
            if(found)
            {
                Log.d("serverLogs", "Znaleziono szachy");
                Imgproc.cornerSubPix(frame, imageCorners, new Size(11, 11), new Size(-1, -1), term);
                saveState();
                frame.copyTo(tempSavedImage);
                howManyFound++;
            }
        }
        return howManyFound;
    }
    public void drawChessboardsOnColorFrames() throws NotEnoughChessboardsException
    {
        findChessboards();

        if(imagePoints.size() < HOW_MANY_FRAMES)
            throw new NotEnoughChessboardsException();
        else
        {
            for(int i = 0; i < colorFrames.length; i++)
            {
                Calib3d.drawChessboardCorners(colorFrames[i], boardSize, (MatOfPoint2f) imagePoints.get(i),true);
            }
        }
    }
    private void saveState() {
        imagePoints.add(imageCorners);
        imageCorners = new MatOfPoint2f();
        objectPoints.add(obj);
    }

    public void resetFrameIndex() {
        index = 0;
    }

    private void calculateCameraParameters()
    {
        List<Mat> rvecs = new ArrayList<>();
        List<Mat> tvecs = new ArrayList<>();
        intrinsic.put(0, 0, 1);
        intrinsic.put(1, 1, 1);

        Calib3d.calibrateCamera(objectPoints, imagePoints, tempSavedImage.size(), intrinsic, distCoeffs, rvecs, tvecs);
    }

    private void undistortImages()
    {
        for(int i = 0; i < colorFrames.length; i++)
        {
            undistortedFrames[i] = new Mat();
            Imgproc.undistort(colorFrames[i], undistortedFrames[i], intrinsic, distCoeffs);
        }
        isCalibrated = true;
    }

    public class NotEnoughChessboardsException extends Exception
    {

    }
}
