package com.example.stereovisioncarsystem.FilterCalibration;

import android.content.Context;
import android.widget.Toast;

import com.example.stereovisioncarsystem.DeviceTypes;
import com.example.stereovisioncarsystem.R;
import com.example.stereovisioncarsystem.SavedParametersTags;
import com.example.stereovisioncarsystem.Tools;

import org.opencv.core.Mat;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class InternalMemoryDataManager
{
    private Context context;

    public InternalMemoryDataManager(Context context) {
        this.context = context;
    }

    public void save(SavedParametersTags tag, String parameter) throws SavingException
    {
        String fileTag = tag.name() + "_" + "value";
        saveBytes(parameter.getBytes(), fileTag);
    }

    public void save(String fileName, String data) throws SavingException
    {
        saveBytes(data.getBytes(),fileName);
    }

    public void save(SavedParametersTags tag, Mat data) throws SavingException
    {
        String fileTag = tag.name() + "_" + "value";
        String mat = Tools.getNonScientificMatValues(data);
        byte[] byteData = mat.getBytes();
        saveBytes(byteData,fileTag);
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
        String fileTag = tag.name() + "_" + "value";
        return readBytes(fileTag);
    }

    public int readInt(SavedParametersTags tag) throws SavingException {
        String fileTag = tag.name() + "_" + "value";
        return Integer.parseInt(readBytes(fileTag));
    }

    public double readDouble(SavedParametersTags tag) throws SavingException {
        String fileTag = tag.name() + "_" + "value";
        return Double.parseDouble(readBytes(fileTag));
    }

    public String read(String filename) throws SavingException {
        return readBytes(filename);
    }


    private String readBytes(String fileTag) throws SavingException{
        FileInputStream fileInputStream = null;
        try {

            fileInputStream = context.openFileInput(fileTag);
            int read;
            StringBuffer buffer = new StringBuffer();
            while((read=fileInputStream.read())!=-1)
            {
                buffer.append((char)read);
            }
            return buffer.toString();

        } catch (Exception e) {
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


    public void saveDeviceType(DeviceTypes deviceType)  throws SavingException{

        String fileTag = SavedParametersTags.deviceType.name() + "_" + "value";
        saveBytes(deviceType.toString().getBytes(),fileTag);
    }

    public DeviceTypes readDeviceType() throws SavingException {
        String fileTag = SavedParametersTags.deviceType.name() + "_" + "value";
        String readName = readBytes(fileTag);
        return readName.equals(DeviceTypes.SERVER.toString()) ? DeviceTypes.SERVER : DeviceTypes.CLIENT;
    }

    public class SavingException extends Exception {
    }

    private void throwException() throws SavingException {
        throw new SavingException();
    }
}
