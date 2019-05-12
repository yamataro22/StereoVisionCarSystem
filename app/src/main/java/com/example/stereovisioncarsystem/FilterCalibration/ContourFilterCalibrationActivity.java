package com.example.stereovisioncarsystem.FilterCalibration;

import android.os.SystemClock;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import com.example.stereovisioncarsystem.CameraBasicActivity;
import com.example.stereovisioncarsystem.Filtr.BinaryThreshFiltr;
import com.example.stereovisioncarsystem.Filtr.CannyFiltr;
import com.example.stereovisioncarsystem.Filtr.Filtr;
import com.example.stereovisioncarsystem.Filtr.GBlurFiltr;
import com.example.stereovisioncarsystem.Filtr.GrayFiltr;
import com.example.stereovisioncarsystem.R;
import com.example.stereovisioncarsystem.SavedParametersTags;
import com.example.stereovisioncarsystem.Tools;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

public class ContourFilterCalibrationActivity extends CameraBasicActivity implements View.OnTouchListener {
    private final String TAG = "contourCalibration";

    private int threshVal = 120;
    private int gaussVal = 27;
    private boolean isInverted = false;
    private List<Filtr> filtrs;

    private int maxArea = 100000;
    private int minArea = 10000;
    private double maxRatio = 1.5;
    private double minRatio = 1;
    private ContourCreator contourCreator;


    SeekBar maxRatioSeekBar, minRatioSeekBar, maxAreaSeekBar, minAreaSeekBar;
    TextView maxRatioTextView, minRatioTextView, maxAreaTextView, minAreaTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contour_filter_calibration);

        initGUI();
        initCamera();
        readSavedValues();
        updateIndicatorValues();
        initFilters();
        exqListeners();
    }

    private void exqListeners() {
        maxAreaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d(TAG, "maxArea changed:  + ");
                maxArea = seekBar.getProgress();
                Log.d(TAG, "maxArea changed:  + " + maxArea);
                maxAreaTextView.setText(maxArea+"");
                contourCreator.setConstrains(minArea,maxArea,minRatio,maxRatio);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        minAreaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                minArea = seekBar.getProgress();
                Log.d(TAG, "minArea changed:  + " + minArea);
                minAreaTextView.setText(minArea+"");
                contourCreator.setConstrains(minArea,maxArea,minRatio,maxRatio);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        maxRatioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                maxRatio = ((double)seekBar.getProgress())/10;
                Log.d(TAG, "maxRatio changed:  + " + maxRatio);
                maxRatioTextView.setText(maxRatio+"");
                contourCreator.setConstrains(minArea,maxArea,minRatio,maxRatio);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        minRatioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                minRatio = ((double)seekBar.getProgress())/10;
                Log.d(TAG, "minRatio changed:  + " + minRatio);
                minRatioTextView.setText(minRatio+"");
                contourCreator.setConstrains(minArea,maxArea,minRatio,maxRatio);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    private void initGUI() {
        maxAreaSeekBar = findViewById(R.id.max_area_seek_bar);
        minAreaSeekBar = findViewById(R.id.min_area_seek_bar);
        maxRatioSeekBar = findViewById(R.id.max_ratio_seek_bar);
        minRatioSeekBar = findViewById(R.id.min_ratio_seek_bar);

        maxAreaTextView = findViewById(R.id.max_area_text_view);
        minAreaTextView = findViewById(R.id.min_area_text_view);
        maxRatioTextView = findViewById(R.id.max_ratio_text_view);
        minRatioTextView = findViewById(R.id.min_ratio_text_view);

        contourCreator = new ContourCreator();

    }

    private void updateIndicatorValues() {
        updateSpinners();
        updateTextViews();
    }

    private void updateTextViews() {
        maxAreaTextView.setText(maxArea+"");
        minAreaTextView.setText(minArea+"");
        maxRatioTextView.setText(maxRatio+"");
        minRatioTextView.setText(minRatio+"");
    }

    private void updateSpinners() {
        maxAreaSeekBar.setProgress(maxArea);
        minAreaSeekBar.setProgress(minArea);
        maxRatioSeekBar.setProgress((int)maxRatio*10);
        minRatioSeekBar.setProgress((int)minRatio*10);
    }


    private void initCamera() {
        mOpenCvCameraView = findViewById(R.id.calibrationOpencvView);
        mOpenCvCameraView.enableView();
        mOpenCvCameraView.enableFpsMeter();
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setOnTouchListener(this);
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

        try {
            maxArea = dataManager.readInt(SavedParametersTags.maxArea);
            minArea = dataManager.readInt(SavedParametersTags.minArea);
            maxRatio = dataManager.readDouble(SavedParametersTags.maxRatio);
            minRatio = dataManager.readDouble(SavedParametersTags.minRatio);
        } catch (InternalMemoryDataManager.SavingException e) {
            e.printStackTrace();
            Tools.makeToast(getApplicationContext(), "No default parameters");
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
        contourCreator.processFrameDuringCalibration(inputFrame);
        SystemClock.sleep(10);
        return inputFrame;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            InternalMemoryDataManager messanger = new InternalMemoryDataManager(getApplicationContext());
            try {
                messanger.saveInt(SavedParametersTags.minArea,minArea);
                messanger.saveInt(SavedParametersTags.maxArea,maxArea);
                messanger.saveDouble(SavedParametersTags.minRatio,minRatio);
                messanger.saveDouble(SavedParametersTags.maxRatio,maxRatio);
            } catch (InternalMemoryDataManager.SavingException e) {
                e.printStackTrace();
            }

            finish();
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}
