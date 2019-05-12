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

    public String getFormattedCameraMatrix()
    {
        Mat cameraFormatted = getCameraMatrixMat();
        String output = "";
        Double c00 = cameraFormatted.get(0,0)[0];
        Double c01 = cameraFormatted.get(0,1)[0];
        Double c02 = cameraFormatted.get(0,2)[0];
        Double c10 = cameraFormatted.get(1,0)[0];
        Double c11 = cameraFormatted.get(1,1)[0];
        Double c12 = cameraFormatted.get(1,2)[0];
        Double c20 = cameraFormatted.get(2,0)[0];
        Double c21 = cameraFormatted.get(2,1)[0];
        Double c22 = cameraFormatted.get(2,2)[0];

        output += String.format("[%07.2f   %07.2f   %07.2f,\n", c00,c01,c02);
        output += String.format(" %07.2f   %07.2f   %07.2f,\n", c10,c11,c12);
        output += String.format(" %07.2f   %07.2f   %07.2f]\n", c20,c21,c22);

        return output;
    }

    public String getFromatedDiffParams()
    {
        Mat distFormatted = getDistCoeffsMat();
        String output;

        Double c00 = distFormatted.get(0,0)[0];
        Double c01 = distFormatted.get(0,1)[0];
        Double c02 = distFormatted.get(0,2)[0];
        Double c03 = distFormatted.get(0,3)[0];
        Double c04 = distFormatted.get(0,4)[0];

        output = String.format("[%04.2f, %04.2f, %03.2f, %03.2f, %03.2f]", c00,c01,c02,c03,c04);
        return output;
    }


}
