package com.example.stereovisioncarsystem;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CalibrationActivity extends AppCompatActivity implements ObservedCameraFramesCapturer.CameraFrameConnector, Counter.CounterListener {

    private TextView processInformationTextView, cameraMatrixTextView;
    private EditText framesQuantityEditText;
    private Button startCalibrationButton, showMatrixButton, saveButton;
    private Spinner cameraTypeSpinner;
    private Calibrator calibrator;
    private JavaCameraView javaCameraView;
    private ImageView imageView;
    private SeekBar photoSeekBar;
    private ObservedSingleCameraFramesCapturer capturer;
    private CounterManager counterManager;
    private Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);

        counterManager = new CounterManager();
        handler = new Handler(getMainLooper());
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

    private void initGUI() {
        processInformationTextView = findViewById(R.id.calib_instruction_text_view);
        startCalibrationButton = findViewById(R.id.start_calibration_button);
        javaCameraView = findViewById(R.id.camera_view);
        cameraTypeSpinner = findViewById(R.id.camera_type_spinner);
        capturer = new ObservedSingleCameraFramesCapturer(this);
        imageView = findViewById(R.id.image_preview);
        photoSeekBar = findViewById(R.id.photo_choose_seek_bar);
        photoSeekBar.setMax(counterManager.getFramesQuantity()-1);
        framesQuantityEditText = findViewById(R.id.frames_quantity_edit_text);
        showMatrixButton = findViewById(R.id.showCameraMatrixButton);
        saveButton = findViewById(R.id.saveButton);
        cameraMatrixTextView = findViewById(R.id.cameraMatrixTextView);
    }

    private void exqListeners() {
        startCalibrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tryToInitCalibration();
            }
        });

        photoSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(calibrator!=null)
                {
                    imageView.setImageBitmap(calibrator.getColorPhotoByIndex(i));
                    showMessageToUser("Photo nb " + (i + 1));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        showMatrixButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchViewInCalibrationVerificationScreen();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tryToSaveCameraMatrix();
            }
        });
    }

    private void tryToSaveCameraMatrix() {


        CameraParametersMessager messager = new CameraParametersMessager(getApplicationContext(), getChosenCameraFacing());
        try
        {
            messager.save(calibrator.getFormattedCameraMatrix(), calibrator.getFromatedDiffParams());
        }
        catch (CameraParametersMessager.SavingException e)
        {
            Toast.makeText(this,"Saving didn't work", Toast.LENGTH_LONG).show();
        }
    }

    public CameraFacing getChosenCameraFacing()
        {
        return CameraFacing.getCameraFacing(cameraTypeSpinner.getSelectedItem().toString());
    }

    private void switchViewInCalibrationVerificationScreen() {
        if(isCameraMatrixVisible())
        {
            hideMatrixGUI();
            setPhotosPreviewVisibility(View.VISIBLE);
            showMatrixButton.setText("Show matrix");
        }
        else
        {
            setPhotosPreviewVisibility(View.GONE);
            showMatrixGUI();
            showMatrixButton.setText("Hide matrix");
        }
    }

    private boolean isCameraMatrixVisible() {
        return cameraMatrixTextView.getVisibility() == View.VISIBLE;
    }

    private void hideMatrixGUI()
    {
        cameraMatrixTextView.setVisibility(View.GONE);
    }

    private void showMatrixGUI()
    {
        cameraMatrixTextView.setVisibility(View.VISIBLE);
        cameraMatrixTextView.setText(getCameraParametersAndFormat());
    }

    private String getCameraParametersAndFormat() {
        String cameraMatrix = calibrator.getFormattedCameraMatrix();
        String diffParams = calibrator.getFromatedDiffParams();
        return cameraMatrix + "\n" + diffParams;
    }

    private void setPhotosPreviewVisibility(int visibility) {
        photoSeekBar.setVisibility(visibility);
        imageView.setVisibility(visibility);
    }

    private void initCamera() {
        javaCameraView.setCvCameraViewListener(capturer);
    }

    private int getCameraIndex() {
        String cameraType = cameraTypeSpinner.getSelectedItem().toString();
        return getCameraIDFromString(cameraType);
}

    private int getCameraIDFromString(String cameraType) {
        int cameraID;
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
        return cameraID;
    }

    private void tryToInitCalibration() {
        try
        {
            initCounterAndUpdateGUI();
        } catch (CounterManager.CounterRunningException e) {
            Toast.makeText(this,"Counter already running",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    private void initCounterAndUpdateGUI() throws CounterManager.CounterRunningException {
        counterManager.initCounter();
        updateVariablesAndGUI();
        createNewCalibrator();
        hideFramesVerificationGUI();
        initAndShowCameraScreen();
        counterManager.runNewCounter(handler, this);
    }


    private void createNewCalibrator() {
        calibrator = null;
        calibrator = new Calibrator(counterManager.getFramesQuantity());
    }

    private void updateVariablesAndGUI(){
        counterManager.changeConfig(Integer.parseInt(framesQuantityEditText.getText().toString()));
        photoSeekBar.setMax(counterManager.getFramesQuantity()-1);
    }

    private void initAndShowCameraScreen() {
        updateCapturerCameraIndex();
        javaCameraView.setCameraIndex(getCameraIndex());
        showCameraScreen();
    }

    private void showCameraScreen() {
        javaCameraView.setVisibility(View.VISIBLE);
        javaCameraView.enableView();
    }

    private void updateCapturerCameraIndex()
    {
        capturer.setCameraOrientation(getCameraIndex(),
                getWindowManager().getDefaultDisplay().getRotation());
    }

    private void hideFramesVerificationGUI() {
        setPhotosPreviewVisibility(View.GONE);
        if(isCameraMatrixVisible())
        {
            hideMatrixGUI();
        }
        saveButton.setVisibility(View.GONE);
        showMatrixButton.setVisibility(View.GONE);
    }

    private void hideCameraScreen() {
        javaCameraView.setVisibility(View.GONE);
        javaCameraView.disableView();
    }

    private void showFramesVerificationGUI() {
        setPhotosPreviewVisibility(View.VISIBLE);
        imageView.setImageBitmap(calibrator.getColorPhotoByIndex(0));
        showMessageToUser("Check frames :)");
        saveButton.setVisibility(View.VISIBLE);
        showMatrixButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void sendFrame(Mat frame)
    {
        calibrator.processFrame(frame);
    }

    @Override
    public void onTick() {
        capturer.getSingleFrameToBeProcessed();
    }

    @Override
    public void onUpdate(int seconds, int remaining) {
        showMessageToUser("time: " + seconds +" ;" +
                remaining+" remaining");
    }

    private void showMessageToUser(String message) {
        processInformationTextView.setText(message);
    }

    @Override
    public void onFinish() {
        SystemClock.sleep(50);
        hideCameraScreen();
        tryToUndisortImages();
    }

    private void tryToUndisortImages()
    {
        try {
            calibrator.performUndisortion();
            showFramesVerificationGUI();
        } catch (Calibrator.ChessboardsNotOnAllPhotosException e) {
            int newSize = calibrator.deleteInvalidImages();
            photoSeekBar.setMax(newSize-1);
            tryToUndisortImages();
        } catch (Calibrator.NotEnoughChessboardsException e)
        {
            showMessageToUser("Less than 2 photos:(");
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageResource(R.drawable.pig);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        counterManager.interrupt();
        showStartupScreen();
    }

    private void showStartupScreen() {
        hideFramesVerificationGUI();
        hideCameraScreen();
        showMessageToUser("Press calibrate to start");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }
}
