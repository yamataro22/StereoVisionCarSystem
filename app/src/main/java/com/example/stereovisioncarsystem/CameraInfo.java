package com.example.stereovisioncarsystem;

import android.hardware.Camera;
import android.util.Log;

public class CameraInfo {

    public static void getCameraInfo(String logTag)
    {
        Log.i(logTag, "Number of cameras:" + Camera.getNumberOfCameras());
    }
}
