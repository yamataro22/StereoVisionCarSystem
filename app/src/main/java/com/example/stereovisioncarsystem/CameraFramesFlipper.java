package com.example.stereovisioncarsystem;

import android.content.res.Configuration;
import android.util.Log;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class CameraFramesFlipper {
    protected Mat mRgbaF;
    protected Mat mRgbaT;


    public final static int FRONT = CameraBridgeViewBase.CAMERA_ID_FRONT;
    public final static int BACK = CameraBridgeViewBase.CAMERA_ID_BACK;
    public final static int ROTATION_PORTRAIT = 0;
    public final static int ROTATION_LANDSCAPE_NORMAL = 1;
    public final static int ROTATION_LANDSCAPE_INVERTED = 3;



    public CameraFramesFlipper(int width, int height)
    {
        mRgbaF = new Mat(height, width, CvType.CV_8UC4);
        mRgbaT = new Mat(width, width, CvType.CV_8UC4);
    }

    public void adjustCameraFrames(Mat frame, int cameraId, int orientation)
    {
        if(orientation == ROTATION_LANDSCAPE_NORMAL) return;
        else if(orientation == ROTATION_PORTRAIT)
        {
            switch(cameraId)
            {
                case FRONT:
                {
                    Core.transpose(frame, mRgbaT);
                    Imgproc.resize(mRgbaT, mRgbaF, mRgbaF.size(), 0, 0, 0);
                    Core.flip(mRgbaF, frame, -1);
                    break;
                }
                case BACK:
                {
                    Core.transpose(frame, mRgbaT);
                    Imgproc.resize(mRgbaT, mRgbaF, mRgbaF.size(), 0, 0, 0);
                    Core.flip(mRgbaF, frame, 1);
                    break;
                }
            }
        }
        else if(orientation == ROTATION_LANDSCAPE_INVERTED)
        {
            switch(cameraId)
            {
                case FRONT:
                {
                    Core.flip(frame, frame, -1);
                    break;
                }
                case BACK:
                {
                    Core.flip(frame, frame, -1);
                    break;
                }
            }
        }
    }

    public void adjustCameraFrames(Mat frame, int cameraId)
    {
        switch(cameraId)
        {
            case FRONT:
            {
                transponseAndFilp(frame, -1);
                break;
            }
            case BACK:
            {
                transponseAndFilp(frame, 1);
                break;
            }
        }
    }

    private void transponseAndFilp(Mat frame, int flipCode) {
        Core.transpose(frame, mRgbaT);
        Imgproc.resize(mRgbaT, mRgbaF, mRgbaF.size(), 0, 0, 0);
        Core.flip(mRgbaF, frame, flipCode);
    }


}
