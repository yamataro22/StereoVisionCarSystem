package com.example.stereovisioncarsystem;

import android.util.Log;

import com.example.stereovisioncarsystem.Filtr.Filtr;
import com.example.stereovisioncarsystem.Filtr.GrayFiltr;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class DualCameraCalibrator extends Calibrator
{
    private final Size patternSize = new Size(4, 11);
    private final int numSquares = (int)(patternSize.width * patternSize.height);
    private double squareSize = 0.0181;
    private int howManyFramesToCalibration;
    private int flags;

    private String TAG = "dualCalibration";
    private final int INITIAL_LIST_CAPACITY = 10;
    private List<Mat> grayServerFrames;
    private List<Mat> colorServerFrames;
    private List<Mat> grayClientFrames;

    private List<Mat> clientImagePoints;
    private List<Mat> serverImagePoints;

    private List<Mat> objectPoints;
    private MatOfPoint2f clientImageCorners;
    private MatOfPoint2f serverImageCorners;


    Filtr grayFiltr;
    private Mat tempSavedImage;
    private List<Integer> invalidImagesIndexes;
    private CameraData serverCameraData;
    private CameraData clientCameraData;

    public DualCameraCalibrator() {
        init();
    }

    private void init() {
        grayFiltr = new GrayFiltr();
        colorServerFrames = new ArrayList<>(INITIAL_LIST_CAPACITY);
        grayServerFrames = new ArrayList<>(INITIAL_LIST_CAPACITY);
        grayClientFrames = new ArrayList<>(INITIAL_LIST_CAPACITY);

        clientImagePoints = new ArrayList<>();
        serverImagePoints = new ArrayList<>();
        invalidImagesIndexes = new ArrayList<>();

        objectPoints = new ArrayList<>();
        tempSavedImage = new Mat();
        clientImageCorners = new MatOfPoint2f();
        serverImageCorners = new MatOfPoint2f();

        flags = Calib3d.CALIB_FIX_PRINCIPAL_POINT +
                Calib3d.CALIB_ZERO_TANGENT_DIST +
                Calib3d.CALIB_FIX_ASPECT_RATIO +
                Calib3d.CALIB_FIX_K4 +
                Calib3d.CALIB_FIX_K5;

    }

    public void processFrames(Mat serverMat, Mat clientMat)
    {
        addLeftFrame(serverMat);
        addRightFrame(clientMat);

        if(grayServerFrames.size()==5)
        {
            howManyFramesToCalibration = 5;
            performUndisortion();
        }
    }

    private void addLeftFrame(Mat serverMat)
    {
        colorServerFrames.add(serverMat);
        Mat grayFrame = serverMat.clone();
        grayFiltr.filtr(grayFrame);
        grayServerFrames.add(grayFrame);
    }

    private void addRightFrame(Mat clientMat) {
        Mat grayFrame = clientMat.clone();
        grayClientFrames.add(grayFrame);
    }



    @Override
    public void performUndisortion()
    {
        try {
            findChessboards();
        } catch (ChessboardsNotOnAllPhotosException e)
        {
            Log.d(TAG, "Didn't found chessboards on both pictures, invalid; " + invalidImagesIndexes.size());
            deleteInvalidImages();
            try {
                findChessboards();
            } catch (ChessboardsNotOnAllPhotosException e1) {
                e1.printStackTrace();
            } catch (NotEnoughChessboardsException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } catch (NotEnoughChessboardsException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Found checkers on both Mats :)");
        calculateCameraParameters();

    }

    private void calculateCameraParameters()
    {
        fillObjectPoints();

        Mat R = new Mat();
        Mat T = new Mat();
        Mat E = new Mat();
        Mat F = new Mat();

        double error = Calib3d.stereoCalibrate(objectPoints,clientImagePoints,serverImagePoints,
                        serverCameraData.getCameraMatrixMat(),serverCameraData.getDistCoeffsMat(),clientCameraData.getCameraMatrixMat(),clientCameraData.getDistCoeffsMat(),
                        tempSavedImage.size(), R, T, E, F, flags);

        Log.d(TAG, "R: " + R.dump());
        Log.d(TAG, "T: " + T.dump());
        Log.d(TAG, "E: " + E.dump());
        Log.d(TAG, "F: " + F.dump());
        Log.d(TAG, "error " + error);
    }

    private void fillObjectPoints() {
        objectPoints.add(Mat.zeros(numSquares, 1, CvType.CV_32FC3));
        calcBoardCornerPositions(objectPoints.get(0));
        for (int i = 1; i < clientImagePoints.size(); i++) {
            objectPoints.add(objectPoints.get(0));
        }
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

    public void deleteInvalidImages()
    {
        for(int index = invalidImagesIndexes.size() - 1; index >= 0; index --)
        {
            grayClientFrames.remove((int)invalidImagesIndexes.get(index));
            grayServerFrames.remove((int)invalidImagesIndexes.get(index));
            colorServerFrames.remove((int)invalidImagesIndexes.get(index));
        }
        howManyFramesToCalibration = grayClientFrames.size();
    }

    protected void findChessboards() throws ChessboardsNotOnAllPhotosException, NotEnoughChessboardsException
    {
        TermCriteria term = new TermCriteria(TermCriteria.EPS | TermCriteria.MAX_ITER, 30, 0.1);

        //clientImagePoints.clear();
        //serverImagePoints.clear();
        //invalidImagesIndexes.clear();

        Log.d(TAG, "grayClientFrame[0] size: "+grayClientFrames.get(0).size());
        Log.d(TAG, "grayServerFrame[0] size: "+grayServerFrames.get(0).size());
        Log.d(TAG, "patternSize: "+patternSize);
        int howManyFound = 0;
        for(int i = 0; i < grayClientFrames.size(); i++)
        {
            boolean clientFound = Calib3d.findCirclesGrid(grayClientFrames.get(i), patternSize,
                    clientImageCorners,  Calib3d.CALIB_CB_ASYMMETRIC_GRID);
            boolean serverFound = Calib3d.findCirclesGrid(grayServerFrames.get(i), patternSize,
                    serverImageCorners,  Calib3d.CALIB_CB_ASYMMETRIC_GRID);
            if(clientFound && serverFound)
            {
                Imgproc.cornerSubPix(grayClientFrames.get(i), clientImageCorners, new Size(11, 11), new Size(-1, -1), term);
                Imgproc.cornerSubPix(grayServerFrames.get(i), serverImageCorners, new Size(11, 11), new Size(-1, -1), term);
                saveState();
                grayClientFrames.get(i).copyTo(tempSavedImage);
                howManyFound++;
            }
            else
            {
                invalidImagesIndexes.add(i);
            }
        }
        if(howManyFound != howManyFramesToCalibration)
            throw new ChessboardsNotOnAllPhotosException();
    }


    private void saveState()
    {
        clientImagePoints.add(clientImageCorners);
        clientImageCorners = new MatOfPoint2f();
        serverImagePoints.add(serverImageCorners);
        serverImageCorners = new MatOfPoint2f();

    }

    public void setServerCameraParameters(CameraData serverCameraData)
    {
        this.serverCameraData = serverCameraData;

        Log.d(TAG, "Ustawiono parametry kamery serwera:\n" +
                serverCameraData.getCameraMatrixMat().dump() + '\n' + serverCameraData.getDistCoeffsMat().dump());
    }


    public void setClientCameraParameters(CameraData clientCameraData)
    {
        this.clientCameraData = clientCameraData;

        Log.d(TAG, "Ustawiono parametry kamery klienta:\n" +
                clientCameraData.getCameraMatrixMat().dump() + '\n' + clientCameraData.getDistCoeffsMat().dump());
    }
}
