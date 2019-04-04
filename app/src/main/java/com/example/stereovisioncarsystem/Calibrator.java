package com.example.stereovisioncarsystem;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

class Calibrator {
    Mat[] frames;
    private int index = 0;

    public Calibrator()
    {
        frames = new Mat[5];
    }

    public void processFrame(Mat frame)
    {
        Log.d("serverLogs","Calibrator, Dodaję klatkę nr " + index);
        frames[index++]=frame;
    }

    public void checkIfFrameExists(int index)
    {
        Log.d("serverLogs","Sprawdzam czy istnieje " + Thread.currentThread());
        Log.d("serverLogs","Calibrator, rows: " + frames[index].rows());
        Log.d("serverLogs","Calibrator, cols: " + frames[index].cols());
    }

    public Bitmap getPhotoByIndex(int index)
    {
        Log.d("serverLogs","Calibrator, Zarządano klatki nr " + index);
        if(index > frames.length) return null;
        Log.d("serverLogs","Calibrator, rows: " + frames[index].rows());
        Log.d("serverLogs","Calibrator, cols: " + frames[index].cols());


        Bitmap btm = Bitmap.createBitmap(frames[index].cols(), frames[index].rows(),Bitmap.Config.ARGB_8888);
        Log.d("serverLogs","Calibrator, skonfigurowano");
        Utils.matToBitmap(frames[index],btm);
        return btm;
    }


    public void findChessboard() {
    }

    public void findChessboards() {
    }

    public void resetFrameIndex() {
        index = 0;
    }
}
