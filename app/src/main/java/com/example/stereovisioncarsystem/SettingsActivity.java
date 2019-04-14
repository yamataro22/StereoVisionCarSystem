package com.example.stereovisioncarsystem;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsActivity extends AppCompatActivity {

    Button calibrationButton, loadCalibrationButton;
    TextView calibrationTextView, deviceNameTextView;
    Spinner cameraSpinner;
    String deviceName = Build.MODEL;
    String cameraMatrix = "";
    String distCoeffs = "";
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
    }

    private void init() {
        calibrationButton = findViewById(R.id.calibrate_button);
        loadCalibrationButton = findViewById(R.id.loadCalibrationButton);
        calibrationTextView = findViewById(R.id.cameraMatrixTextView);
        deviceNameTextView = findViewById(R.id.deviceNameTextView);
        deviceNameTextView.setText(deviceName);
        cameraSpinner = findViewById(R.id.camera_type_spinner);
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
                loadSavedCalibration("server","back");
            }
        });
    }

    private void loadSavedCalibration(String server, String back)
    {
        String filename = createFileName();

        try {
            FileInputStream fileInputStream = openFileInput(filename);
            int read = -1;
            StringBuffer buffer = new StringBuffer();
            while((read=fileInputStream.read())!=-1)
            {
                buffer.append((char)read);
            }
            String data = buffer.toString();
            String[] pos = data.split("%");
            cameraMatrix = pos[0];
            distCoeffs = pos[1];
            calibrationTextView.setText(cameraMatrix + "\n\n" + distCoeffs);
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String createFileName()
    {
        return Build.MODEL+"_"+cameraSpinner.getSelectedItem().toString();
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
