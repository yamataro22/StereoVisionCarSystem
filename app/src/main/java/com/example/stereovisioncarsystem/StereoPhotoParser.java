package com.example.stereovisioncarsystem;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public abstract class StereoPhotoParser {

    protected final DistanceListener distanceListener;


    protected Mat clientFrame;
    protected Mat serverFrame;
    protected Mat qMat;

    protected Mat map1c;
    protected Mat map2c;
    protected Mat map1s;
    protected Mat map2s;
    protected Size picSize;
    protected Mat R1, R2, P1, P2;
    protected CameraData clientCameraData, serverCameraData;

    public StereoPhotoParser(DistanceListener distanceListener)
    {
        this.distanceListener = distanceListener;
        init();
    }


    private void init() {
        clientFrame = new Mat();
        serverFrame = new Mat();
    }


    public void addClientFrame(Mat mat) {
        applyFilters(mat);
        if(picSize == null || map1c == null)
        {
            picSize = mat.size();
            initClientRectificationMaps();
        }
        rectifyClientFrame(mat);
        mat.copyTo(clientFrame);
    }

    public void addServerFrame(Mat frame) {
        applyFilters(frame);
        if(picSize == null || map1s == null)
        {
            picSize = frame.size();
            initServerRectificationMaps();
        }
        rectifyServerFrame(frame);
        frame.copyTo(serverFrame);
    }



    protected abstract void applyFilters(Mat mat);

    private void initClientRectificationMaps() {
        map1c=new Mat();
        map2c=new Mat();
        Imgproc.initUndistortRectifyMap(clientCameraData.getCameraMatrixMat(),clientCameraData.getDistCoeffsMat(),R2, P2,picSize, CvType.CV_16SC2, map1c, map2c);
    }

    private void initServerRectificationMaps() {
        map1s=new Mat();
        map2s=new Mat();
        Imgproc.initUndistortRectifyMap(serverCameraData.getCameraMatrixMat(),serverCameraData.getDistCoeffsMat(),R1, P1,picSize, CvType.CV_16SC2, map1s, map2s);
    }

    public void setCameraData(CameraData clientCameraData, CameraData serverCameraData)
    {
        this.clientCameraData = clientCameraData;
        this.serverCameraData = serverCameraData;
    }

    public abstract void drawObjectOnClientFrame(Mat mat);

    public abstract void drawObjectOnServerFrame(Mat frame);

    public abstract void findDistanceBetweenObjects();

    public abstract void setFilterParams(int threshVal, int gaussVal, boolean isThreshInverted);

    public abstract void setConstrins(int minArea, int maxArea, double minRatio, double maxRatio);

    public abstract void computeDisparityMap();

    public abstract double retreiveDisparity(int x, int y);

    public abstract double calculateLenghtFromDisparity(int x, int y);

    public interface DistanceListener
    {
        void onDistanceCalculated(double distanceInPixels, double distanceInLength);
        void onDisparityCalculated(Mat disparityMap, Mat disparity8);
    }

    private void rectifyServerFrame(Mat frame) {
        Imgproc.remap(frame,frame,map1s,map2s, Imgproc.INTER_LANCZOS4);
    }

    private void rectifyClientFrame(Mat frame) {
        Imgproc.remap(frame,frame,map1c,map2c, Imgproc.INTER_LANCZOS4);
    }

    public void setRectificationParams(Mat r1, Mat r2, Mat p1, Mat p2) {
        R1 = r1;
        R2 = r2;
        P1 = p1;
        P2 = p2;
    }

    public void setQMat(Mat QMat) {
        this.qMat = QMat;
    }


}
