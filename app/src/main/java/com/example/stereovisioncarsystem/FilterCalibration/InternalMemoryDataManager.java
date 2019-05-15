package com.example.stereovisioncarsystem.FilterCalibration;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;


import com.example.stereovisioncarsystem.SavedParametersTags;
import com.example.stereovisioncarsystem.Tools;

import org.opencv.core.Mat;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class InternalMemoryDataManager
{
    private final String TAG = "dataIO";
    private Context context;

    public InternalMemoryDataManager(Context context) {
        this.context = context;
    }

    public void save(SavedParametersTags tag, String parameter) throws SavingException
    {
        FileOutputStream fileOutputStream = null;
        try {
            String fileTag = tag.name() + "_" + "value";
            fileOutputStream = context.openFileOutput(fileTag, Context.MODE_PRIVATE);
            fileOutputStream.write(parameter.getBytes());
            Toast.makeText(context,"Saved to " + context.getFilesDir(), Toast.LENGTH_LONG).show();
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

    public void save(String fileName, String data) throws SavingException
    {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            fileOutputStream.write(data.getBytes());
            Toast.makeText(context,"Saved to " + context.getFilesDir(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {

            e.printStackTrace();
            throwException();
        } finally {
            try {
                if(fileOutputStream == null) throwException();
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                throwException();
            }
        }
    }

    public void save(SavedParametersTags tag, Mat data) throws SavingException
    {
        FileOutputStream fileOutputStream = null;
        try {
            String fileTag = tag.name() + "_" + "value";
            fileOutputStream = context.openFileOutput(fileTag, Context.MODE_PRIVATE);
            String mat = Tools.getNonScientificMatValues(data);
            fileOutputStream.write(mat.getBytes());
            Toast.makeText(context,"Saved to " + context.getFilesDir(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {

            e.printStackTrace();
            throwException();
        } finally {
            try {
                if(fileOutputStream == null) throwException();
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                throwException();
            }
        }
    }

    public void saveInt(SavedParametersTags tag, int data) throws SavingException
    {
        String fileTag = tag.name() + "_" + "value";
        saveBytes((data+"").getBytes(), fileTag);
    }

    public void saveDouble(SavedParametersTags tag, double data) throws SavingException
    {
        String fileTag = tag.name() + "_" + "value";
        saveBytes((data+"").getBytes(), fileTag);
    }

    private void saveBytes(byte[] data, String fileTag) throws SavingException {
        FileOutputStream fileOutputStream = null;
        try {

            fileOutputStream = context.openFileOutput(fileTag, Context.MODE_PRIVATE);
            fileOutputStream.write(data);
            Toast.makeText(context,"Saved to " + context.getFilesDir(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {

            e.printStackTrace();
            throwException();
        } finally {
            try {
                if(fileOutputStream == null) throwException();
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                throwException();
            }
        }
    }


    public String read(SavedParametersTags tag) throws SavingException {
        FileInputStream fileInputStream = null;
        try {
            String fileTag = tag.name() + "_" + "value";
            fileInputStream = context.openFileInput(fileTag);
            int read;
            StringBuffer buffer = new StringBuffer();
            while((read=fileInputStream.read())!=-1)
            {
                buffer.append((char)read);
            }
            String data = buffer.toString();
            Log.d(TAG, "Otrzymano dane: " + data);
            return data;

        } catch (Exception e) {
            Log.d(TAG, "Otrzymano exception wewnątrz readServerParams");
            throwException();
            e.printStackTrace();

        } finally {
            try {
                if(fileInputStream== null) throwException();
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                throwException();
            }
        }
        return "";
    }

    public int readInt(SavedParametersTags tag) throws SavingException {
        FileInputStream fileInputStream = null;
        try {
            String fileTag = tag.name() + "_" + "value";
            fileInputStream = context.openFileInput(fileTag);
            int read;
            StringBuffer buffer = new StringBuffer();
            while((read=fileInputStream.read())!=-1)
            {
                buffer.append((char)read);
            }
            String data = buffer.toString();
            Log.d(TAG, "Otrzymano dane: " + data);
            return Integer.parseInt(data);

        } catch (Exception e) {
            Log.d(TAG, "Otrzymano exception wewnątrz readServerParams");
            throwException();
            e.printStackTrace();

        } finally {
            try {
                if(fileInputStream== null) throwException();
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                throwException();
            }
        }
        return 0;
    }

    public double readDouble(SavedParametersTags tag) throws SavingException {
        FileInputStream fileInputStream = null;
        try {
            String fileTag = tag.name() + "_" + "value";
            fileInputStream = context.openFileInput(fileTag);
            int read;
            StringBuffer buffer = new StringBuffer();
            while((read=fileInputStream.read())!=-1)
            {
                buffer.append((char)read);
            }
            String data = buffer.toString();
            Log.d(TAG, "Otrzymano dane: " + data);
            return Double.parseDouble(data);

        } catch (Exception e) {
            Log.d(TAG, "Otrzymano exception wewnątrz readServerParams");
            throwException();
            e.printStackTrace();

        } finally {
            try {
                if(fileInputStream== null) throwException();
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                throwException();
            }
        }
        return 0;
    }


    public String read(String filename) throws SavingException {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = context.openFileInput(filename);
            int read;
            StringBuffer buffer = new StringBuffer();
            while((read=fileInputStream.read())!=-1)
            {
                buffer.append((char)read);
            }
            return buffer.toString();

        } catch (Exception e) {
            Log.d(TAG, "Otrzymano exception wewnątrz readServerParams");
            throwException();
            e.printStackTrace();

        } finally {
            try {
                if(fileInputStream== null) throwException();
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                throwException();
            }
        }
        return "";
    }

    public void saveDeviceType(String deviceType) {

    }

    public SavedParametersTags readDeviceType(String server) {

    }

    public class SavingException extends Exception {
    }

    private void throwException() throws SavingException {
        throw new SavingException();
    }
}
