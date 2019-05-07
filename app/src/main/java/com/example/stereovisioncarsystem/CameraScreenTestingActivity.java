package com.example.stereovisioncarsystem;


import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;

import com.example.stereovisioncarsystem.FilterCalibration.ContourCreator;
import com.example.stereovisioncarsystem.FilterCalibration.InternalMemoryDataManager;
import com.example.stereovisioncarsystem.Filtr.BinaryThreshFiltr;
import com.example.stereovisioncarsystem.Filtr.CannyFiltr;
import com.example.stereovisioncarsystem.Filtr.Filtr;
import com.example.stereovisioncarsystem.Filtr.GBlurFiltr;
import com.example.stereovisioncarsystem.Filtr.GrayFiltr;

import org.opencv.core.Mat;
import java.util.ArrayList;
import java.util.List;


public class CameraScreenTestingActivity extends CameraBasicActivity {

    private int threshVal = 120;
    private int gaussVal = 27;
    private boolean isInverted = false;

    private List<Filtr> filtrs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_screen);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        initCamera();
        readSavedValues();
        initFilters();
    }

    private void initCamera() {
        mOpenCvCameraView = findViewById(R.id.HelloOpenCvView);
        mOpenCvCameraView.setMaxFrameSize(4000,4000);
        mOpenCvCameraView.enableView();
        mOpenCvCameraView.enableFpsMeter();
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCameraIndex(0);
        mOpenCvCameraView.setCvCameraViewListener(this);

    }

    private void readSavedValues() {
        InternalMemoryDataManager dataManager = new InternalMemoryDataManager(getApplicationContext());
        try {
            threshVal = Integer.parseInt(dataManager.read(SavedParametersTags.Thresh));
            gaussVal = Integer.parseInt(dataManager.read(SavedParametersTags.Gauss));
            isInverted = Boolean.parseBoolean(dataManager.read(SavedParametersTags.IsThreshInverted));
        } catch (InternalMemoryDataManager.SavingException e) {
            e.printStackTrace();
        }
    }

    private void initFilters()
    {
        filtrs = new ArrayList<>();
        filtrs.add(new GrayFiltr());
        filtrs.add(new GBlurFiltr(gaussVal));
        filtrs.add(new BinaryThreshFiltr(threshVal, isInverted));
        filtrs.add(new CannyFiltr());

    }


    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(Mat inputFrame) {

        for(Filtr filtr : filtrs)
        {
            filtr.filtr(inputFrame);
        }

        ContourCreator contourCreator = new ContourCreator(inputFrame);
        contourCreator.findAndDrawObject(inputFrame);
        return inputFrame;

    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }


}
