package com.example.stereovisioncarsystem;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import org.opencv.android.OpenCVLoader;

import com.example.stereovisioncarsystem.ExternalCalibrator.CameraCalibrationActivity2;
import com.example.stereovisioncarsystem.FilterCalibration.FilterCalibrationActivity;
import com.example.stereovisioncarsystem.FilterCalibration.FilterSettingsActivity;

public class MainActivity extends AppCompatActivity {

    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        Log.d("receiveTask", "jestem w jebanym on create");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            createAndStartIntent(SettingsActivity.class);
        }
        else if(id == R.id.action_text_communication) {
            createAndStartIntent(CommunicationTestActivity.class);
        }
        else if(id == R.id.action_photo_communication) {
            createAndStartIntent(ReceiveFramesActivity.class);
        }
        else if(id == R.id.action_server_dual_communication) {
            createAndStartIntent(ServerDualCameraCalibrationActivity.class);
        }
        else if(id == R.id.action_client_dual_communication) {
            createAndStartIntent(ClientDualCameraActivity.class);
        }
        else if(id == R.id.opencvcalib) {
            createAndStartIntent(CameraCalibrationActivity2.class);
        }
        else if(id == R.id.action_client_stereo_distance_meter) {
            createAndStartIntent(ClientStereoDistanceMeter.class);
        }
        else if(id == R.id.action_server_stereo_distance_meter) {
            createAndStartIntent(ServerStereoDistanceMeter.class);
        }
        else if(id == R.id.filters_parameters_calibration) {
            createAndStartIntent(FilterSettingsActivity.class);
        }
        return super.onOptionsItemSelected(item);
    }

    private void createAndStartIntent(Class<?> cls)
    {
        Intent intent = new Intent(this,cls);
        startActivity(intent);
    }
    public void onStartCapturingButton(View view)
    {
        Intent intent = new Intent(this, CameraScreenActivity.class);
        startActivity(intent);

    }
}
