package com.example.stereovisioncarsystem.FilterCalibration;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import com.example.stereovisioncarsystem.Filtr.BinaryThreshFiltr;
import com.example.stereovisioncarsystem.Filtr.Filtr;
import com.example.stereovisioncarsystem.Filtr.GrayFiltr;
import com.example.stereovisioncarsystem.R;

import org.opencv.core.Mat;

public class ThreshCalibrationActivity extends FilterCalibrationActivity {

    int threshValue;
    public static final String THRESH_1 = "m1";
    public static final String THRESH_2 = "m2";
    Filtr gray = new GrayFiltr();
    BinaryThreshFiltr threshBinary = new BinaryThreshFiltr();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        threshValue = intent.getIntExtra(THRESH_1, 120);
        final SeekBar sb = findViewById(R.id.seekBar);
        sb.setProgress(threshValue);

        TextView textView = findViewById(R.id.seekBarValue);
        textView.setText(threshValue+"");

        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                threshValue = sb.getProgress();
                TextView textView = findViewById(R.id.seekBarValue);
                textView.setText(threshValue+"");
                Log.i("wiadomosc", "xD");
            }
        });
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(Mat inputFrame) {

        gray.filtr(inputFrame);
        threshBinary.setBinaryThreshParam(threshValue);
        threshBinary.filtr(inputFrame);
        return inputFrame;

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            SeekBar sb = findViewById(R.id.seekBar);
            int newVal = sb.getProgress();
            Intent intent = new Intent();
            intent.putExtra(THRESH_2, newVal);
            setResult(RESULT_OK, intent);
            finish();
            return true;
        }
        return false;
    }
}
