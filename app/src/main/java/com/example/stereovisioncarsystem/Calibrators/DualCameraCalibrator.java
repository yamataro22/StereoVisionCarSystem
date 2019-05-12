package com.example.stereovisioncarsystem.Calibrators;

import android.util.Log;

import com.example.stereovisioncarsystem.CameraData;
import com.example.stereovisioncarsystem.Filtr.Filtr;
import com.example.stereovisioncarsystem.Filtr.GrayFiltr;
import com.example.stereovisioncarsystem.Tools;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.CvType.CV_64F;

public class DualCameraCalibrator extends Calibrator
{
    private OnStereoCalibrationresult resultListener;

    private int framesQuantity = 5;
    private int howManyCaptured = 0;

    private final Size patternSize = new Size(4, 11);
    private final int numSquares = (int)(patternSize.width * patternSize.height);
    private double squareSize = 0.0175;

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




    private Mat R;
    private Mat T;
    private Mat E;
    private Mat F;



    public Mat getR1() {
        return R1;
    }

    public Mat getR2() {
        return R2;
    }

    public Mat getP1() {
        return P1;
    }

    public Mat getP2() {
        return P2;
    }

    Mat R1 = new Mat(3, 3, CV_64F);
    Mat R2 = new Mat(3, 3, CV_64F);
    Mat P1 = new Mat(3, 4, CV_64F);
    Mat P2 = new Mat(3, 4, CV_64F);
    Mat qMatrix = new Mat(4, 4, CV_64F);

    Mat map1s;
    Mat map2s;
    Mat unrectified;

    public boolean isCalibrated = false;


    public DualCameraCalibrator(OnStereoCalibrationresult listener) {
        this.resultListener = listener;
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

        R = new Mat();
        T = new Mat();
        E = new Mat();
        F = new Mat();

        map1s=new Mat();
        map2s=new Mat();

    }

    public void processFrames(Mat serverMat, Mat clientMat) throws NotEnoughChessboardsException, ChessboardsNotOnAllPhotosException {

        if(howManyCaptured == framesQuantity)
        {
            Log.d("dualCalibration", "powiadamiam serwer o rozpoczęciu komunikacji");
            resultListener.onCalibrationStart();
            Log.d("dualCalibration", "powiadamiłem, wróciłem, kalibruję");
            performUndisortion();
        }
        else
        {
            addLeftFrame(serverMat);
            addRightFrame(clientMat);
            howManyCaptured++;
        }

        if(grayServerFrames.size()>framesQuantity && isCalibrated)
        {
//            unrectified = new Mat();
//            Imgproc.remap(colorServerFrames.get(colorServerFrames.size()-1), unrectified,
//                    map1s,map2s, Imgproc.INTER_LINEAR);
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
    public void performUndisortion() throws NotEnoughChessboardsException, ChessboardsNotOnAllPhotosException {
        try {
            findChessboards();
            calculateCameraParameters();
        } catch (ChessboardsNotOnAllPhotosException e)
        {
            Log.d(TAG, "Didn't found chessboards on both pictures, invalid; " + invalidImagesIndexes.size());
            deleteInvalidImages();

            findChessboards();
            calculateCameraParameters();

            e.printStackTrace();
        } catch (NotEnoughChessboardsException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Found checkers on both Mats :)");


    }

    private void calculateCameraParameters()
    {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        fillObjectPoints();

        double error = Calib3d.stereoCalibrate(objectPoints,serverImagePoints,clientImagePoints,
                        serverCameraData.getCameraMatrixMat(),serverCameraData.getDistCoeffsMat(),clientCameraData.getCameraMatrixMat(),clientCameraData.getDistCoeffsMat(),
                        tempSavedImage.size(), R, T, E, F, Calib3d.CALIB_FIX_INTRINSIC|Calib3d.CALIB_FIX_PRINCIPAL_POINT|Calib3d.CALIB_ZERO_TANGENT_DIST,
                        new TermCriteria(TermCriteria.COUNT+TermCriteria.EPS, 100, 1e-5));

        Calib3d.stereoRectify(serverCameraData.getCameraMatrixMat(),serverCameraData.getDistCoeffsMat(),clientCameraData.getCameraMatrixMat(),clientCameraData.getDistCoeffsMat(),
                                tempSavedImage.size(),R,T,R1,R2, P1, P2,qMatrix, Calib3d.CALIB_ZERO_DISPARITY);

        //Imgproc.initUndistortRectifyMap(serverCameraData.getCameraMatrixMat(),serverCameraData.getDistCoeffsMat(),R1, P1,tempSavedImage.size(),CvType.CV_32FC1, map1s, map2s);
        isCalibrated = true;

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }





        Log.d(TAG, "R: \n" + R.dump());
        Log.d(TAG, "T: \n" + T.dump());
        Log.d(TAG, "E: \n" + E.dump());
        Log.d(TAG, "F: \n" + F.dump());

        Log.d(TAG, "F normalized: \n" + Tools.getNonScientificMatValues(F));


        Log.d(TAG, "F type:" + F.type());
        Log.d(TAG, "R1: \n" + R1.dump());
        Log.d(TAG, "R2: \n" + R2.dump());
        Log.d(TAG, "P1: \n" + P1.dump());
        Log.d(TAG, "P2: \n" + P2.dump());
        Log.d(TAG, "error " + error);
        Log.d(TAG, "Q \n" + qMatrix.dump());

        Log.d(TAG, "map1s type:" + map1s.type());
        Log.d(TAG, "map1s size:" + map1s.size());

        resultListener.onCameraResulat(qMatrix,R1,R2, P1, P2);
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
        Log.d(TAG, "deleteInvalidImages; deleting "+invalidImagesIndexes.size());
        for(int index = invalidImagesIndexes.size() - 1; index >= 0; index --)
        {
            grayClientFrames.remove((int)invalidImagesIndexes.get(index));
            grayServerFrames.remove((int)invalidImagesIndexes.get(index));
            colorServerFrames.remove((int)invalidImagesIndexes.get(index));
        }
        Log.d(TAG, "za delete, grayFramesQuantity "+grayClientFrames.size());
        howManyCaptured = grayClientFrames.size();
    }

    protected void findChessboards() throws ChessboardsNotOnAllPhotosException, NotEnoughChessboardsException
    {
        TermCriteria term = new TermCriteria(TermCriteria.EPS | TermCriteria.MAX_ITER, 30, 0.1);

        clientImagePoints.clear();
        serverImagePoints.clear();
        invalidImagesIndexes.clear();

        if(grayClientFrames.size() < 2)
        {
            throw new NotEnoughChessboardsException();
        }


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
        if(howManyFound != howManyCaptured)
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

    public Mat showDisparity()
    {
//        Mat disparity = new Mat(grayClientFrames.get(0).rows(),grayClientFrames.get(0).cols(), CV_64F);
//        Mat disparityNormalized = new Mat(grayClientFrames.get(0).rows(),grayClientFrames.get(0).cols(), CV_8U);
//        StereoBM bm = StereoBM.create(16,15);
//        bm.compute(grayServerFrames.get(0),grayClientFrames.get(0),disparity);
//
//        Core.normalize(disparity, disparityNormalized, 0, 255, Core.NORM_MINMAX, CV_8U);
        return unrectified;
    }

    public void setFramesQuantity(int i) {
        framesQuantity = i;
        Log.d(TAG, "frames quantity set to "+ i);
    }

    public int getRemainingFrames() {
        return framesQuantity - howManyCaptured;
    }

    public Mat getQMartix() {
        return qMatrix;
    }






    public interface OnStereoCalibrationresult {
        void onCameraResulat(Mat qMatrix, Mat R1, Mat R2, Mat P1, Mat P2);
        void onCalibrationStart();
    }

    public Mat getR() {
        return R;
    }

    public Mat getT() {
        return T;
    }

    public Mat getE() {
        return E;
    }

    public Mat normalizeAndGetF() {
        Mat fNormalized = new Mat(F.rows(), F.cols(), F.type());
        for(int i = 0; i < F.rows(); i++)
        {
            for(int j = 0; j < F.cols(); j++)
            {
                fNormalized.put(i,j, Tools.round(F.get(i,j)[0],5));
            }
        }
        return fNormalized;
    }

    public Mat getF()
    {
        return F;
    }
}
