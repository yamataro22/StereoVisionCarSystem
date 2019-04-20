package com.example.stereovisioncarsystem;

import com.example.stereovisioncarsystem.StringMatConverters.StringToMatCamMatrixCoverter;
import com.example.stereovisioncarsystem.StringMatConverters.StringToMatConverter;
import com.example.stereovisioncarsystem.StringMatConverters.StringToMatDistCoeffsConverter;

import org.opencv.core.Mat;

public class CameraData
{
    public String getCameraMatrix() {
        return cameraMatrix;
    }

    public String getDistCoeffs() {
        return distCoeffs;
    }

    private String cameraMatrix;
    private String distCoeffs;
    private final String separator = "%";

    public CameraData(String cameraMatrix, String distCoeffs) {
        this.cameraMatrix = cameraMatrix;
        this.distCoeffs = distCoeffs;
    }

    public CameraData(String cameraData) {
        String[] data = cameraData.split(separator);
        cameraMatrix = data[0];
        distCoeffs = data[1];
    }

    public Mat getCameraMatrixMat()
    {
        StringToMatConverter converter = new StringToMatCamMatrixCoverter();
        return converter.convert(cameraMatrix);

    }

    public Mat getDistCoeffsMat()
    {
        StringToMatConverter converter = new StringToMatDistCoeffsConverter(1,5);
        return converter.convert(distCoeffs);
    }


    @Override
    public String toString() {
        return cameraMatrix + separator + distCoeffs;
    }
}
