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
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;


class SingleCameraCalibrator extends Calibrator{
    List<Mat> colorFrames;
    List<Mat> grayFrames;
    List<Mat> undistortedFrames;


    public int HOW_MANY_FRAMES;

    private final Size patternSize = new Size(4, 11);
    private final int numSquares = (int)(patternSize.width * patternSize.height);
    private double squareSize = 0.0181;
    private int flags;

    List<Integer> invalidImagesIndexes;
    private MatOfPoint2f imageCorners;
    private Mat intrinsic;
    private Mat distCoeffs;
    private List<Mat> imagePoints;
    private List<Mat> objectPoints;
    private Mat tempSavedImage;
    private Filtr filtr;
    private boolean isCalibrated = false;


    public SingleCameraCalibrator(int howManyFrames)
    {
        HOW_MANY_FRAMES = howManyFrames;
        colorFrames = new ArrayList<>(HOW_MANY_FRAMES);
        undistortedFrames = new ArrayList<>(HOW_MANY_FRAMES);
        grayFrames = new ArrayList<>(HOW_MANY_FRAMES);

        invalidImagesIndexes = new ArrayList<>();

        flags = Calib3d.CALIB_FIX_PRINCIPAL_POINT +
                Calib3d.CALIB_ZERO_TANGENT_DIST +
                Calib3d.CALIB_FIX_ASPECT_RATIO +
                Calib3d.CALIB_FIX_K4 +
                Calib3d.CALIB_FIX_K5;

        imageCorners = new MatOfPoint2f();
        filtr = new GrayFiltr();
        imagePoints = new ArrayList<>();
        objectPoints = new ArrayList<>();
        intrinsic = new Mat(3, 3, CvType.CV_32FC1);
        distCoeffs = new Mat();
        tempSavedImage = new Mat();
    }

    protected void calcBoardCornerPositions(Mat corners) {
        final int cn = 3;
        float positions[] = new float[numSquares * cn];

        for (int i = 0; i < patternSize.height; i++) {
            for (int j = 0; j < patternSize.width * cn; j += cn) {
                positions[(int) (i * patternSize.width * cn + j + 0)] =
                        (2 * (j / cn) + i % 2) * (float) squareSize;
                positions[(int) (i * patternSize.width * cn + j + 1)] =
                        i * (float) squareSize;
                positions[(int) (i * patternSize.width * cn + j + 2)] = 0;
            }
        }
        corners.create(numSquares, 1, CvType.CV_32FC3);
        corners.put(0, 0, positions);
    }


    public void processFrame(Mat frame)
    {
        addFrameToColorFrames(frame);
        addFrameToGrayFrames(frame);
    }

    private void addFrameToColorFrames(Mat frame) {
        colorFrames.add(frame);
    }

    private void addFrameToGrayFrames(Mat frame) {
        Mat grayFrame;

        grayFrame = frame.clone();
        filtr.filtr(grayFrame);
        grayFrames.add(grayFrame);
    }

    public Bitmap getColorPhotoByIndex(int index)
    {
        if(index > colorFrames.size()) return null;
        if(!isCalibrated) return mat2Bitmap(colorFrames.get(index));
        else return mat2Bitmap(undistortedFrames.get(index));
    }

    public Bitmap getGrayPhotoByIndex(int index)
    {
        if(index > grayFrames.size()) return null;
        return mat2Bitmap(grayFrames.get(index));
    }

    public Bitmap getUndistortedPhotoByIndex(int index)
    {
        if(index > grayFrames.size()) return null;
        return mat2Bitmap(undistortedFrames.get(index));
    }

    public void performUndisortion() throws ChessboardsNotOnAllPhotosException, NotEnoughChessboardsException
    {
        drawChessboardsOnColorFrames();
        calculateCameraParameters();
        undistortImages();
    }

    private Bitmap mat2Bitmap(Mat mat)
    {
        Bitmap btm = Bitmap.createBitmap(mat.cols(), mat.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat,btm);
        return btm;
    }


    public int deleteInvalidImages()
    {
        for(int index = invalidImagesIndexes.size() - 1; index >= 0; index --)
        {
            grayFrames.remove((int)invalidImagesIndexes.get(index));
            colorFrames.remove((int)invalidImagesIndexes.get(index));
        }
        HOW_MANY_FRAMES = grayFrames.size();
        return HOW_MANY_FRAMES;
    }

    public void drawChessboardsOnColorFrames() throws ChessboardsNotOnAllPhotosException, NotEnoughChessboardsException {
        findChessboards();

        if(imagePoints.size() < 2) throw new NotEnoughChessboardsException();
        if(imagePoints.size() < HOW_MANY_FRAMES) throw new ChessboardsNotOnAllPhotosException();

        for(int i = 0; i < colorFrames.size(); i++)
        {
            Calib3d.drawChessboardCorners(colorFrames.get(i), patternSize, (MatOfPoint2f) imagePoints.get(i),true);
        }
    }

    protected int findChessboards()
    {
        TermCriteria term = new TermCriteria(TermCriteria.EPS | TermCriteria.MAX_ITER, 30, 0.1);

        imagePoints.clear();
        invalidImagesIndexes.clear();


        int howManyFound = 0;
        for(int i = 0; i < grayFrames.size(); i++)
        {
            boolean found = Calib3d.findCirclesGrid(grayFrames.get(i), patternSize,
                    imageCorners, Calib3d.CALIB_CB_ASYMMETRIC_GRID);
            if(found)
            {
                Imgproc.cornerSubPix(grayFrames.get(i), imageCorners, new Size(11, 11), new Size(-1, -1), term);
                saveState();
                grayFrames.get(i).copyTo(tempSavedImage);
                howManyFound++;
            }
            else
            {
                invalidImagesIndexes.add(i);
            }
        }
        return howManyFound;
    }


    private void saveState() {
        imagePoints.add(imageCorners);
        imageCorners = new MatOfPoint2f();
    }


    private void calculateCameraParameters()
    {
        if(isCalibrated) return;
        fillObjectPoints();

        List<Mat> rvecs = new ArrayList<>();
        List<Mat> tvecs = new ArrayList<>();
        intrinsic.put(0,0,1);
        intrinsic.put(1,1,1);
        double error = Calib3d.calibrateCamera(objectPoints, imagePoints, tempSavedImage.size(), intrinsic, distCoeffs, rvecs, tvecs, flags);
        isCalibrated = true;
        Log.d("serverLogs", "Camera matrix: " + intrinsic.dump());
        Log.d("serverLogs", "distCoeffs " + distCoeffs.dump());
        Log.d("serverLogs", "error " + error);
    }

    private void fillObjectPoints() {
        objectPoints.add(Mat.zeros(numSquares, 1, CvType.CV_32FC3));
        calcBoardCornerPositions(objectPoints.get(0));
        for (int i = 1; i < imagePoints.size(); i++) {
            objectPoints.add(objectPoints.get(0));
        }
    }

    private void undistortImages()
    {
        undistortedFrames.clear();
        for(int i = 0; i < colorFrames.size(); i++)
        {
            undistortedFrames.add(i,new Mat(colorFrames.get(i).size(), colorFrames.get(i).type()));
            Imgproc.undistort(colorFrames.get(i), undistortedFrames.get(i), intrinsic, distCoeffs);
        }
    }

    public Mat getCameraMatrix()
    {
        return intrinsic.clone();
    }

    public Mat getDiffParams() {
        return distCoeffs.clone();
    }

    public String getFormattedCameraMatrix()
    {
        String output = "";
        Double c00 = intrinsic.get(0,0)[0];
        Double c01 = intrinsic.get(0,1)[0];
        Double c02 = intrinsic.get(0,2)[0];
        Double c10 = intrinsic.get(1,0)[0];
        Double c11 = intrinsic.get(1,1)[0];
        Double c12 = intrinsic.get(1,2)[0];
        Double c20 = intrinsic.get(2,0)[0];
        Double c21 = intrinsic.get(2,1)[0];
        Double c22 = intrinsic.get(2,2)[0];

        output += String.format("[%07.2f   %07.2f   %07.2f,\n", c00,c01,c02);
        output += String.format(" %07.2f   %07.2f   %07.2f,\n", c10,c11,c12);
        output += String.format(" %07.2f   %07.2f   %07.2f]\n", c20,c21,c22);

        return output;
    }

    public String getFromatedDiffParams()
    {
        String output;

        Double c00 = distCoeffs.get(0,0)[0];
        Double c01 = distCoeffs.get(0,1)[0];
        Double c02 = distCoeffs.get(0,2)[0];
        Double c03 = distCoeffs.get(0,3)[0];
        Double c04 = distCoeffs.get(0,4)[0];

        output = String.format("[%04.2f, %04.2f, %03.2f, %03.2f, %03.2f]", c00,c01,c02,c03,c04);
        return output;
    }





}
