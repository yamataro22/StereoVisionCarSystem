package com.example.stereovisioncarsystem;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public String getCameraMatrixString() {
        return cameraMatrix;
    }

    public String getDistCoeffString() {
        return distCoeffs;
    }

    public Mat getCameraMatrixMat()
    {
        Pattern pattern = Pattern.compile("[0-9]{4},[0-9]{2}");
        Matcher matcher = pattern.matcher(cameraMatrix);

        List<String> matches = createListFromMatches(matcher);
        matches = replaceComasWithDots(matches);
        Double[] parametersList = converStringToDoubleArray(matches);

        return createMatFromDoubleArray(parametersList);
    }

    private List<String> createListFromMatches(Matcher matcher) {
        List<String> matches = new ArrayList<>(9);
        while(matcher.find())
        {
            matches.add(matcher.group());
        }
        return matches;
    }

    private Double[] converStringToDoubleArray(List<String> matches) {
        Double[] parametersList = new Double[9];
        for(int i = 0; i < matches.size(); i++)
        {
            parametersList[i] = Double.parseDouble(matches.get(i));
        }
        return parametersList;
    }

    private Mat createMatFromDoubleArray(Double[] parametersList) {
        Mat mat = new Mat(3, 3, CvType.CV_32FC1);
        int k = 0;
        for(int i = 0; i < 3; i++)
        {
            for(int j = 0; j < 3; j++)
            {
                mat.put(i,j,parametersList[k++]);
            }
        }
        return mat;
    }

    private List<String> replaceComasWithDots(List<String> matches) {
        List<String> matchesDot = new ArrayList<>(9);
        for(String x : matches)
        {
            matchesDot.add( x.replace(',','.'));
        }
        return matchesDot;
    }

    public class SavingException extends Exception {
    }
}
