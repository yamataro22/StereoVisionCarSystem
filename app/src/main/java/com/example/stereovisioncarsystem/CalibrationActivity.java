package com.example.stereovisioncarsystem;

import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stereovisioncarsystem.CameraCapturers.ObservedCameraFramesCapturer;
import com.example.stereovisioncarsystem.CameraCapturers.ObservedSingleCameraFramesCapturer;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.core.Mat;

public class CalibrationActivity extends AppCompatActivity implements ObservedCameraFramesCapturer.CameraFrameConnector {

    private TextView statusTextView, processInformationTextView;
    private EditText framesQuantityEditText;
    private Button startCalibrationButton, okButton, undisortButton;
    private Spinner cameraTypeSpinner;
    private Calibrator calibrator;
    private JavaCameraView javaCameraView;
    private ImageView imageView;
    private SeekBar photoSeekBar;
    private ObservedSingleCameraFramesCapturer capturer;
    private CounterConfigData counterConfig;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);

        counterConfig = new CounterConfigData();
        initToolbar();
        initGUI();
        exqListeners();
        initCamera();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Kalibracja");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void initCamera() {
        javaCameraView.setCvCameraViewListener(capturer);
    }

    private int getCameraIndex() {
        String cameraType = cameraTypeSpinner.getSelectedItem().toString();
        int cameraID;
        Log.d("serverLogs","Zarządano indexu kamery " + cameraType);
        switch(cameraType)
        {
            case "Front":
            {
                cameraID = CameraBridgeViewBase.CAMERA_ID_FRONT;
                break;
        }
            case "Back":
            {
                cameraID = CameraBridgeViewBase.CAMERA_ID_BACK;
                break;
            }
            default:
            {
                cameraID = CameraBridgeViewBase.CAMERA_ID_ANY;
            }
        }
        capturer.setCameraOrientation(cameraID, getWindowManager().getDefaultDisplay().getRotation());
        return cameraID;
}


    private void initGUI() {
        statusTextView = findViewById(R.id.calib_status_text_view);
        processInformationTextView = findViewById(R.id.calib_instruction_text_view);
        startCalibrationButton = findViewById(R.id.start_calibration_button);
        javaCameraView = findViewById(R.id.camera_view);
        cameraTypeSpinner = findViewById(R.id.camera_type_spinner);
        capturer = new ObservedSingleCameraFramesCapturer(this);
        imageView = findViewById(R.id.image_preview);
        photoSeekBar = findViewById(R.id.photo_choose_seek_bar);
        photoSeekBar.setMax(counterConfig.getFramesQuantity()-1);
        okButton = findViewById(R.id.ok_button);
        undisortButton = findViewById(R.id.undisort_button);
        framesQuantityEditText = findViewById(R.id.frames_quantity_edit_text);
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
                if(calibrator!=null)
                {
                    imageView.setImageBitmap(calibrator.getColorPhotoByIndex(i));
                    processInformationTextView.setText("Photo nb " + (i + 1));
                }
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
                boolean wasFound = false;
                processInformationTextView.setText("I'm looking for chessboards");
                try {
                    calibrator.drawChessboardsOnColorFrames();
                    wasFound = true;
                } catch (Calibrator.NotEnoughChessboardsException e) {
                    e.printStackTrace();
                    processInformationTextView.setText("Chessboards not on all photos :(");
                }
                if(wasFound) undisortButton.setVisibility(View.VISIBLE);
            }
        });
        undisortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calibrator.performUndisortion();
            }
        });
    }



    private void initCalibration() {
        if(counterConfig.isRunning())
        {
            Toast.makeText(this,"Przecież działa już",Toast.LENGTH_SHORT).show();
            return;
        }
        updateVariablesAndGUI();
        calibrator = null;
        calibrator = new Calibrator(counterConfig.getFramesQuantity());
        statusTextView.setText("Calib started");
        hideFramesVerificationGUI();
        showCameraScreen();
        runTimer();
    }

    private void updateVariablesAndGUI()
    {
        counterConfig.changeConfig(Integer.parseInt(framesQuantityEditText.getText().toString()));
        photoSeekBar.setMax(counterConfig.getFramesQuantity()-1);
    }


    private void showCameraScreen() {
        javaCameraView.setCameraIndex(getCameraIndex());
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
            private int interval = counterConfig.getInterval();
            private int seconds = interval;
            private int  howManyFramesToCapture = counterConfig.getFramesQuantity();


            private boolean isTimerRunning = true;
            private int counter = 0;
            private boolean isFirstTime = true;

            @Override
            public void run() {

                if(isFirstTime) counterConfig.run(); isFirstTime = false;
                counterConfig.updateStatus(isTimerRunning);

                if(counterConfig.shouldBeRunning())
                {
                    if(seconds == 0)
                    {
                        seconds = interval;
                        if(counter < howManyFramesToCapture)
                        {
                            counter++;
                            if(counter == howManyFramesToCapture) seconds = 1;
                            capturer.getSingleFrameToBeProcessed();
                        }
                        else
                        {
                            seconds = 0;
                            isTimerRunning = false;
                            counterConfig.updateStatus(isTimerRunning);
                            onCountdownFinish();
                        }
                    }

                    if(isTimerRunning)
                    {
                        processInformationTextView.setText("Change position of chessboard on every photo, time: " + seconds +" ;" +
                                (howManyFramesToCapture-counter)+" remaining");
                        Log.d("serverLogs","Runner, seconds: " + seconds);
                        seconds--;
                        handler.postDelayed(this, 1000);
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        counterConfig.interrupt();
    }

    private void hideCameraScreen() {
        javaCameraView.setVisibility(View.GONE);
        javaCameraView.disableView();
    }

    private void onCountdownFinish() {
        SystemClock.sleep(50);
        hideCameraScreen();
        showFramesVerificationGUI();
    }

    private void showFramesVerificationGUI() {
        photoSeekBar.setVisibility(View.VISIBLE);
        okButton.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.VISIBLE);
        imageView.setImageBitmap(calibrator.getColorPhotoByIndex(0));
        processInformationTextView.setText("Capturing finished, check colorFrames");
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