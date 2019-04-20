package com.example.stereovisioncarsystem;

import android.content.Context;
import android.os.Build;
import android.widget.Toast;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

class CameraParametersMessager {
    Context appContext;
    CameraFacing facing;

    String cameraMatrix;
    String distCoeffs;

    public CameraParametersMessager(Context applicationContext, CameraFacing camera)
    {
        this.appContext = applicationContext;
        this.facing = camera;
    }

    public void save(String formattedCameraMatrix, String fromatedDiffParams) throws SavingException
    {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = appContext.openFileOutput(Build.MODEL+"_"+facing, Context.MODE_PRIVATE);
            fileOutputStream.write(formattedCameraMatrix.getBytes());
            fileOutputStream.write("%".getBytes());
            fileOutputStream.write(fromatedDiffParams.getBytes());
            Toast.makeText(appContext,"Saved to " + appContext.getFilesDir(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            throwException();
        } finally {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                throwException();
            }
        }
    }




    private void throwException() throws SavingException {
        throw new SavingException();
    }

    public void read() throws SavingException {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = appContext.openFileInput(createFilename());
            int read;
            StringBuffer buffer = new StringBuffer();
            while((read=fileInputStream.read())!=-1)
            {
                buffer.append((char)read);
            }
            String data = buffer.toString();
            String[] pos = data.split("%");
            cameraMatrix = pos[0];
            distCoeffs = pos[1];

        } catch (Exception e) {
            throwException();
            e.printStackTrace();

        } finally {
            try {
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                throwException();
            }
        }
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
