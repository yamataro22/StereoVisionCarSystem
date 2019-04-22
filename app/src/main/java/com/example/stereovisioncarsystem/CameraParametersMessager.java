package com.example.stereovisioncarsystem;

import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import com.example.stereovisioncarsystem.FilterCalibration.InternalMemoryDataManager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

class CameraParametersMessager {
    Context appContext;
    CameraFacing facing;

    String cameraMatrix;
    String distCoeffs;
    InternalMemoryDataManager dataManager;


    public CameraParametersMessager(Context applicationContext, CameraFacing camera)
    {
        this.appContext = applicationContext;
        this.facing = camera;
        dataManager = new InternalMemoryDataManager(appContext);
    }

    public void save(String formattedCameraMatrix, String fromatedDiffParams) throws InternalMemoryDataManager.SavingException {

        String savingDir = createFilename();
        String dataToSave = formattedCameraMatrix + "%" + fromatedDiffParams;
        dataManager.save(savingDir, dataToSave);
    }


    public void read() throws InternalMemoryDataManager.SavingException {

        String data = dataManager.read(createFilename());
        String[] pos = data.split("%");
        cameraMatrix = pos[0];
        distCoeffs = pos[1];
    }

    private String createFilename() {
        return Build.MODEL+"_"+facing;
    }

    public CameraData getCameraData()
    {
        return new CameraData(cameraMatrix,distCoeffs);
    }






    public class SavingException extends Exception {
    }
}
