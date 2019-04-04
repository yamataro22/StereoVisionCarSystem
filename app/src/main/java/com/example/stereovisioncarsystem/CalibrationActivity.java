package com.example.stereovisioncarsystem;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stereovisioncarsystem.CameraCapturers.ObservedCameraFramesCapturer;
import com.example.stereovisioncarsystem.CameraCapturers.ObservedSingleCameraFramesCapturer;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

public class CalibrationActivity extends AppCompatActivity implements ObservedCameraFramesCapturer.CameraFrameConnector {

    private TextView statusTextView, processInformationTextView;
    private Button startCalibrationButton, okButton;
    private Spinner cameraTypeSpinner;
    private Calibrator calibrator;
    private JavaCameraView javaCameraView;
    private ImageView imageView;
    private SeekBar photoSeekBar;
    private ObservedSingleCameraFramesCapturer capturer;


    private boolean isTimerRunning = false;
    private int seconds = 3;
    final private int howManyFramesCaptured = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Kalibracja");

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        init();
        exqListeners();
        initCamera();
    }

    private void initCamera() {
        javaCameraView.setCameraIndex(getCameraIndex());
        //javaCameraView.setMaxFrameSize(350,350);
        javaCameraView.setCvCameraViewListener(capturer);
    }

    private int getCameraIndex() {
        String cameraType = cameraTypeSpinner.getSelectedItem().toString();
        switch(cameraType)
        {
            case "Front":
            {
                return CameraBridgeViewBase.CAMERA_ID_FRONT;
            }
            case "Back":
            {
                return CameraBridgeViewBase.CAMERA_ID_BACK;
            }
        }


        return 0;
    }


    private void init() {
        statusTextView = findViewById(R.id.calib_status_text_view);
        processInformationTextView = findViewById(R.id.calib_instruction_text_view);
        startCalibrationButton = findViewById(R.id.start_calibration_button);
        calibrator = new Calibrator();
        javaCameraView = findViewById(R.id.camera_view);
        cameraTypeSpinner = findViewById(R.id.camera_type_spinner);
        capturer = new ObservedSingleCameraFramesCapturer(this);
        imageView = findViewById(R.id.image_preview);
        photoSeekBar = findViewById(R.id.photo_choose_seek_bar);
        photoSeekBar.setMax(howManyFramesCaptured-1);
        okButton = findViewById(R.id.ok_button);
    }


    private void exqListeners() {
        startCalibrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initCalibration();
            }
        });
        photoSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                imageView.setImageBitmap(calibrator.getPhotoByIndex(i));
                processInformationTextView.setText("Photo nb " + (i + 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calibrator.findChessboards();
                processInformationTextView.setText("Finding chessboards");
            }
        });
    }

    private void initCalibration() {

        statusTextView.setText("Calib started");
        hideFramesVerificationGUI();
        calibrator.resetFrameIndex();
        showCameraScreen();
        initTimerHandler();
    }

    private void initTimerHandler() {
        runTimer();
        isTimerRunning = true;
    }

    private void showCameraScreen() {
        javaCameraView.setVisibility(View.VISIBLE);
        javaCameraView.enableView();
    }

    private void hideFramesVerificationGUI() {
        photoSeekBar.setVisibility(View.GONE);
        okButton.setVisibility(View.GONE);
        imageView.setVisibility(View.GONE);
    }

    private void runTimer() {
        final Handler handler = new Handler(getMainLooper());
        handler.post(new Runnable() {
            private int counter = 0;
            @Override
            public void run() {

                if(seconds == 0)
                {
                    seconds = 3;
                    if(counter < howManyFramesCaptured)
                    {
                        counter++;
                        if(counter == 5) seconds = 1;
                        capturer.getSingleFrameToBeProcessed();
                    }
                    else
                    {
                        seconds = 0;
                        isTimerRunning = false;
                        onCountdownFinish();
                    }
                }



                if(isTimerRunning)
                {
                    processInformationTextView.setText("Change position of chessboard on every photo, time: " + seconds);
                    Log.d("serverLogs","Runner, seconds: " + seconds);
                    seconds--;
                    handler.postDelayed(this, 1000);
                }
                else
                {
                    seconds = 3;
                }
            }
        });
    }



    private void onCountdownFinish() {
        SystemClock.sleep(50);
        hideCameraScreen();
        showFramesVerificationGUI();
    }

    private void hideCameraScreen() {
        javaCameraView.setVisibility(View.GONE);
        javaCameraView.disableView();
    }

    private void showFramesVerificationGUI() {
        photoSeekBar.setVisibility(View.VISIBLE);
        okButton.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.VISIBLE);
        imageView.setImageBitmap(calibrator.getPhotoByIndex(0));
        processInformationTextView.setText("Capturing finished, check frames");
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void sendFrame(Mat frame)
    {
        calibrator.processFrame(frame);
    }


}
