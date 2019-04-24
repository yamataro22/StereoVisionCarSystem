package com.example.stereovisioncarsystem;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stereovisioncarsystem.FilterCalibration.InternalMemoryDataManager;

public class SettingsActivity extends AppCompatActivity {

    private final String serverDeviceName = "ONEPLUS A6013";
    private final String clientDeviceName = "GRACE";


    Button calibrationButton, loadCalibrationButton, saveButton;
    Button stereoCalibrationButton;
    TextView calibrationTextView, deviceNameTextView;
    Spinner cameraSpinner, deviceTypeSpinner;
    EditText framesNbEditText;
    String deviceName = Build.MODEL;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Ustawienia");
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        init();
        exqListeners();
        loadPreviousSettings();
        updateClientServerSpinner();
        hideUnnecessaryGUIinClientCase();
    }

    private void init() {
        calibrationButton = findViewById(R.id.calibrate_button);
        loadCalibrationButton = findViewById(R.id.loadCalibrationButton);
        calibrationTextView = findViewById(R.id.cameraMatrixTextView);
        deviceNameTextView = findViewById(R.id.deviceNameTextView);
        deviceNameTextView.setText(deviceName);
        cameraSpinner = findViewById(R.id.camera_type_spinner);
        saveButton = findViewById(R.id.save_button);
        framesNbEditText = findViewById(R.id.nb_of_frames_edit_text);
        deviceTypeSpinner = findViewById(R.id.device_type_spinner);
        stereoCalibrationButton = findViewById(R.id.calibrate_stereo_button);
    }

    private void exqListeners() {
        calibrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CalibrationActivity.class);
                startActivity(intent);
            }
        });
        loadCalibrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadSavedCalibration();
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InternalMemoryDataManager dataManager = new InternalMemoryDataManager(getApplicationContext());

                try {
                    dataManager.save(SavedParametersTags.NbOfStereoCalibrationFrames, framesNbEditText.getText().toString());
                } catch (InternalMemoryDataManager.SavingException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),"Saving failed",Toast.LENGTH_SHORT).show();
                }
            }
        });
        stereoCalibrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (deviceTypeSpinner.getSelectedItem().toString())
                {
                    case "Server":
                        createAndStartIntent(ServerDualCameraCalibrationActivity.class);
                        break;
                    case "Client":
                        createAndStartIntent(ClientDualCameraActivity.class);
                }
            }
        });
    }

    private void hideUnnecessaryGUIinClientCase()
    {
        String deviceType = deviceTypeSpinner.getSelectedItem().toString();
        if(deviceType.equalsIgnoreCase("client"))
        {
            View layout = findViewById(R.id.stereo_settings_layout);
            layout.setVisibility(View.GONE);
        }

    }

    private void createAndStartIntent(Class<?> cls)
    {
        Intent intent = new Intent(this,cls);
        startActivity(intent);
    }

    private void updateClientServerSpinner()
    {
        int pos = 0;
        if(deviceName.equalsIgnoreCase(serverDeviceName))
        {
            pos = Tools.getSpinnerIndex(deviceTypeSpinner, "server");

        }
        else if(deviceName.equalsIgnoreCase(clientDeviceName))
        {
            pos = Tools.getSpinnerIndex(deviceTypeSpinner, "client");
        }

        deviceTypeSpinner.setSelection(pos);
    }


    private void loadPreviousSettings() {
        InternalMemoryDataManager dataManager = new InternalMemoryDataManager(getApplicationContext());
        try {
            String quantity = dataManager.read(SavedParametersTags.NbOfStereoCalibrationFrames);
            framesNbEditText.setText(quantity);
        } catch (InternalMemoryDataManager.SavingException e) {
            e.printStackTrace();
        }
    }

    private void loadSavedCalibration()
    {
        String facing = cameraSpinner.getSelectedItem().toString();
        CameraParametersMessager messager = new CameraParametersMessager(getApplicationContext(),CameraFacing.getCameraFacing(facing));
        try {
            messager.read();
            CameraData cameraData = messager.getCameraData();
            calibrationTextView.setText(cameraData.getCameraMatrix() + "\n\n" + cameraData.getDistCoeffs());
        } catch (InternalMemoryDataManager.SavingException e) {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
        }
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
