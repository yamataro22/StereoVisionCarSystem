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
    int threshVal = 120;
    int gaussVal = 27;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_screen);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        initCamera();
        readSavedValues();
    }

    private void readSavedValues() {
        InternalMemoryDataManager dataManager = new InternalMemoryDataManager(getApplicationContext());
        try {
            threshVal = Integer.parseInt(dataManager.read(FilterParameterTag.Thresh));
            gaussVal = Integer.parseInt(dataManager.read(FilterParameterTag.Gauss));
        } catch (InternalMemoryDataManager.SavingException e) {
            e.printStackTrace();
        }
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


    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(Mat inputFrame) {
        Log.d("resolution", inputFrame.size().toString());
        List<Filtr> filtrs = new ArrayList<>();
        filtrs.add(new GrayFiltr());
        filtrs.add(new GBlurFiltr(27));
        filtrs.add(new BinaryThreshFiltr(120));
        filtrs.add(new CannyFiltr());

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
        Log.i("filtry", "jestem w OnPause");
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }
    public void onDestroy() {
        super.onDestroy();
        Log.i("filtry", "jestem w onDestroy");
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }


}
