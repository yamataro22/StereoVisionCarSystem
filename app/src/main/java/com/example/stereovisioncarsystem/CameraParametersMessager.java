package com.example.stereovisioncarsystem;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.example.stereovisioncarsystem.FilterCalibration.InternalMemoryDataManager;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class CameraParametersMessager {
    Context appContext;
    CameraFacing facing;

    String cameraMatrix;
    String distCoeffs;

    String clientCameraMatrix;
    String clientDistCoeffs;


    String qMartix;
    InternalMemoryDataManager dataManager;

    public CameraParametersMessager(Context applicationContext)
    {
        this.appContext = applicationContext;
        this.facing = CameraFacing.Back;
        dataManager = new InternalMemoryDataManager(appContext);
    }


    public CameraParametersMessager(Context applicationContext, CameraFacing camera)
    {
        this.appContext = applicationContext;
        this.facing = camera;
        dataManager = new InternalMemoryDataManager(appContext);
    }

    public void saveServer(String formattedCameraMatrix, String fromatedDiffParams) throws InternalMemoryDataManager.SavingException {

        String savingDir = createServerFilename();
        String dataToSave = formattedCameraMatrix + "%" + fromatedDiffParams;
        dataManager.save(savingDir, dataToSave);
    }

    public void saveClientParams(String formattedCameraMatrix, String fromatedDiffParams) throws InternalMemoryDataManager.SavingException {

        String savingDir = createClientFilename();
        String dataToSave = formattedCameraMatrix + "%" + fromatedDiffParams;
        dataManager.save(savingDir, dataToSave);
    }


    public void readServerParams() throws InternalMemoryDataManager.SavingException {

        String data = dataManager.read(createServerFilename());
        String[] pos = data.split("%");
        cameraMatrix = pos[0];
        distCoeffs = pos[1];
    }

    public void readClientParams() throws InternalMemoryDataManager.SavingException {

        String data = dataManager.read(createClientFilename());
        String[] pos = data.split("%");
        clientCameraMatrix = pos[0];
        clientDistCoeffs = pos[1];
    }

    public void readQMartix() throws InternalMemoryDataManager.SavingException {

        qMartix = dataManager.read(SavedParametersTags.QMatrix);
    }

    public Mat getQMat()
    {
        Mat QMartixMat = new Mat(4,4, CvType.CV_64FC1);
        Pattern pattern = Pattern.compile("-?[0-9]{1,4}(\\.[0-9]*)?");
        Matcher m = pattern.matcher(qMartix);
        List<Double> dataList = new ArrayList<>();

        while(m.find())
        {
            dataList.add(Double.parseDouble(m.group(0)));
        }

        int k = 0;
        Log.d("matchingRegex", dataList.toString());


        for(int i = 0; i < 4; i++)
        {
            for(int j = 0; j < 4; j++)
            {
                QMartixMat.put(i,j,dataList.get(k++));
            }
        }
        QMartixMat.dump();

        return QMartixMat;
    }

    public Mat readStereoMatrixBySize(SavedParametersTags tag, int rows, int cols) throws InternalMemoryDataManager.SavingException {

        Mat calibMat = new Mat(rows,cols, CvType.CV_64FC1);

        String calibString = dataManager.read(tag);
        Pattern pattern = Pattern.compile("-?[0-9]{1,5}(\\.[0-9]*)?");
        Matcher m = pattern.matcher(calibString);
        List<Double> dataList = new ArrayList<>();

        while(m.find())
        {
            dataList.add(Double.parseDouble(m.group(0)));
        }

        int k = 0;
        Log.d("matchingRegex", dataList.toString());


        for(int i = 0; i < rows; i++)
        {
            for(int j = 0; j < cols; j++)
            {
                calibMat.put(i,j,dataList.get(k++));
            }
        }
        return calibMat;

    }





    public Mat readStereoRMatrix(SavedParametersTags tag) throws InternalMemoryDataManager.SavingException {
        return readStereoMatrixBySize(tag,3,3);
    }

    public Mat readStereoPMatrix(SavedParametersTags tag) throws InternalMemoryDataManager.SavingException {
        return readStereoMatrixBySize(tag,3,4);
    }

    public Mat readStereoTMatrix(SavedParametersTags tag) throws InternalMemoryDataManager.SavingException {
        return readStereoMatrixBySize(tag,3,1);
    }

    public Mat readFMatrix(SavedParametersTags tag) throws InternalMemoryDataManager.SavingException
    {
        return readStereoMatrixBySizeAndType(tag,3,3,6);
    }

    public Mat readStereoMatrixBySizeAndType(SavedParametersTags tag, int rows, int cols, int type) throws InternalMemoryDataManager.SavingException {

        Mat calibMat = new Mat(rows,cols, type);

        String calibString = dataManager.read(tag);
        Log.d("odczytF",calibString);
        Pattern pattern = Pattern.compile("-?[0-9]+(\\.[0-9]*)?");
        Matcher m = pattern.matcher(calibString);
        List<Double> dataList = new ArrayList<>();

        while(m.find())
        {
            dataList.add(Double.parseDouble(m.group(0)));
        }

        int k = 0;
        Log.d("matchingRegex", dataList.toString());


        for(int i = 0; i < rows; i++)
        {
            for(int j = 0; j < cols; j++)
            {
                calibMat.put(i,j,dataList.get(k++));
            }
        }
        return calibMat;

    }


    private String createServerFilename() {
        return Build.MODEL+"_"+facing;
    }

    private String createClientFilename() {
        return "client"+"_"+facing;
    }


    public CameraData getCameraData()
    {
        return new CameraData(cameraMatrix,distCoeffs);
    }


    public CameraData getClientCameraData()
    {
        return new CameraData(clientCameraMatrix,clientDistCoeffs);
    }




    public class SavingException extends Exception {
    }
}
