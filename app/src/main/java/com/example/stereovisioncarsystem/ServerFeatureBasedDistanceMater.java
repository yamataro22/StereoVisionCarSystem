package com.example.stereovisioncarsystem;


import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import com.example.stereovisioncarsystem.FilterCalibration.InternalMemoryDataManager;
import com.example.stereovisioncarsystem.ServerClientCommunication.ClientServerMessages;
import com.example.stereovisioncarsystem.ServerClientCommunication.ServerHandlerMsg;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;


public class ServerFeatureBasedDistanceMater extends ServerDistanceMeter implements StereoPhotoParser.DistanceListener {

    Mat matBuffer;
    public final static String TAG = "serverClientCom";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initSpecificGUI() {

    }


    @Override
    protected void initParser() {
        stereoPhotoParser = new FeaturePhotoParser(this);

        loadFilterParameters();
        loadContourFilterParameters();
    }

    private void loadFilterParameters() {
        int threshVal  = 120;
        int gaussVal = 3;
        boolean isThreshInverted = false;

        InternalMemoryDataManager dataManager = new InternalMemoryDataManager(getApplicationContext());
        try {
            threshVal = Integer.parseInt(dataManager.read(SavedParametersTags.Thresh));
            gaussVal = Integer.parseInt(dataManager.read(SavedParametersTags.Gauss));
            isThreshInverted = Boolean.parseBoolean(dataManager.read(SavedParametersTags.IsThreshInverted));
        } catch (InternalMemoryDataManager.SavingException e) {
            e.printStackTrace();
            Toast.makeText(this, "Nie udało się odczytać z pamięci", Toast.LENGTH_SHORT).show();
        }
        stereoPhotoParser.setFilterParams(threshVal,gaussVal,isThreshInverted);
    }

    private void loadContourFilterParameters() {
        InternalMemoryDataManager messanger = new InternalMemoryDataManager(getApplicationContext());
        try {
            int minArea = messanger.readInt(SavedParametersTags.minArea);
            int maxArea = messanger.readInt(SavedParametersTags.maxArea);
            double minRatio = messanger.readDouble(SavedParametersTags.minRatio);
            double maxRatio = messanger.readDouble(SavedParametersTags.maxRatio);
            stereoPhotoParser.setConstrins(minArea,maxArea,minRatio,maxRatio);
        } catch (InternalMemoryDataManager.SavingException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void processClientFrame(Mat mat) {
        stereoPhotoParser.addClientFrame(mat);
        stereoPhotoParser.drawObjectOnClientFrame(mat);

        Bitmap btm = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, btm);
        im.setImageBitmap(btm);
        matBuffer = mat;
    }

    @Override
    public void processServerFrame(Mat frame) {

        stereoPhotoParser.addServerFrame(frame);
        stereoPhotoParser.drawObjectOnServerFrame(frame);
        stereoPhotoParser.findDistanceBetweenObjects();
        Bitmap btm = Bitmap.createBitmap(frame.cols(), frame.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(frame, btm);
        setImage(disparityImageView,btm);

    }

    @Override
    public void onDistanceCalculated(final double distanceInPixels,final double distanceInLength) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                distanceTextView.setText("pix: "+ distanceInPixels + '\n' + "; length: " + distanceInLength);
            }
        });

    }

    @Override
    public void onDisparityCalculated(final Mat disparityMap, final Mat disparity8) {
        //no need to implement
    }


    private void setImage(final ImageView image,final Bitmap btm){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                image.setImageBitmap(btm);
            }
        });
    }

}
