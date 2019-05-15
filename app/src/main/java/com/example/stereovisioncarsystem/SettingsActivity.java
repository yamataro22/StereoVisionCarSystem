package com.example.stereovisioncarsystem;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stereovisioncarsystem.FilterCalibration.InternalMemoryDataManager;

public class SettingsActivity extends AppCompatActivity {

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
        Log.d("debuggingSaving", "SettingsActivity on Create");
        init();
        exqListeners();
        loadPreviousSettings();

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
                    dataManager.saveDeviceType(getDeviceType());
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
        deviceTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateGUIVisibility();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private DeviceTypes getDeviceType() {
        String device = deviceTypeSpinner.getSelectedItem().toString();

        if(device.equalsIgnoreCase("Server"))
            return DeviceTypes.SERVER;
        else
            return DeviceTypes.CLIENT;

    }

    private void updateGUIVisibility()
    {
        String deviceType = deviceTypeSpinner.getSelectedItem().toString();
        View layout = findViewById(R.id.stereo_settings_layout);
        final int visibility = deviceType.equalsIgnoreCase("client") ? View.GONE : View.VISIBLE;
        layout.setVisibility(visibility);
    }

    private void createAndStartIntent(Class<?> cls)
    {
        Intent intent = new Intent(this,cls);
        startActivity(intent);
    }

    private void loadPreviousSettings() {
        InternalMemoryDataManager dataManager = new InternalMemoryDataManager(getApplicationContext());
        try {
            String quantity = dataManager.read(SavedParametersTags.NbOfStereoCalibrationFrames);
            framesNbEditText.setText(quantity);
        } catch (InternalMemoryDataManager.SavingException e) {
            e.printStackTrace();
        }
        try {
            setDeviceType(dataManager.readDeviceType());
        } catch (InternalMemoryDataManager.SavingException e) {
            e.printStackTrace();
            Tools.makeToast(getApplicationContext(), "No device type in memory");
            setDeviceType(DeviceTypes.SERVER);
        }
        updateGUIVisibility();
    }

    private void setDeviceType(DeviceTypes deviceTypes) {

        int pos;

        if(deviceTypes == DeviceTypes.SERVER)
        {
            pos = Tools.getSpinnerIndex(deviceTypeSpinner, "server");

        }
        else
        {
            pos = Tools.getSpinnerIndex(deviceTypeSpinner, "client");
        }

        deviceTypeSpinner.setSelection(pos);
    }

    private void loadSavedCalibration()
    {
        String facing = cameraSpinner.getSelectedItem().toString();
        CameraParametersMessager messager = new CameraParametersMessager(getApplicationContext(),CameraFacing.getCameraFacing(facing));
        try {
            messager.readServerParams();
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
