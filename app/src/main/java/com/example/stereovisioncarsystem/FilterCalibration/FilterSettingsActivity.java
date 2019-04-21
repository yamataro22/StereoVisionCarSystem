package com.example.stereovisioncarsystem.FilterCalibration;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.stereovisioncarsystem.FilterCalibration.GaussCalibrationActivity;
import com.example.stereovisioncarsystem.FilterCalibration.ThreshCalibrationActivity;
import com.example.stereovisioncarsystem.R;

public class FilterSettingsActivity extends Activity {

    public static final String MAIN_SETTINGS = "ms1";
    public static final String SETTINGS_MAIN_THRESH = "ms2";
    public static final String SETTINGS_MAIN_GAUSS = "ms3";
    private int threshValue;
    private int gaussValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_settings);

        Intent intent = getIntent();
        threshValue = intent.getIntExtra(MAIN_SETTINGS, 120);
        EditText editText = findViewById(R.id.threshEditText);
        editText.setText(threshValue+"");
        Log.i("thresh","W settings: "+threshValue);
    }

    public void onClickCalibrateThresh(View v)
    {
        EditText editText = findViewById(R.id.threshEditText);
        threshValue = Integer.parseInt(editText.getText().toString());
        Intent intent = new Intent(this, ThreshCalibrationActivity.class);
        intent.putExtra(ThreshCalibrationActivity.THRESH_1, threshValue);
        int requestCode = 1; // Or dowolny
        startActivityForResult(intent, requestCode);
    }

    public void onClickCalibrateGauss(View view) {
        Spinner spinner = findViewById(R.id.gaussSpinner);
        String value = spinner.getSelectedItem().toString();
        gaussValue = Integer.parseInt(value);
        Log.i("dupa", value);
        Intent intent = new Intent(this, GaussCalibrationActivity.class);
        intent.putExtra(GaussCalibrationActivity.GAUSS_VALUE, gaussValue);
        startActivity(intent);
    }

    protected void onActivityResult (int requestCode, int resultCode, Intent data) {

        if(requestCode == 1)
        {
            threshValue = data.getIntExtra(ThreshCalibrationActivity.THRESH_2, 100);
            EditText editText = findViewById(R.id.threshEditText);
            editText.setText(threshValue+"");
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

            EditText editText = findViewById(R.id.threshEditText);
            String val = editText.getText().toString();
            threshValue = Integer.parseInt(val);

            Spinner spinner = findViewById(R.id.gaussSpinner);
            String value = spinner.getSelectedItem().toString();
            gaussValue = Integer.parseInt(value);

            Intent intent = new Intent();
            intent.putExtra(SETTINGS_MAIN_THRESH, threshValue);
            intent.putExtra(SETTINGS_MAIN_GAUSS, gaussValue);


            setResult(RESULT_OK, intent);
            finish();
            return true;
        }
        return false;
    }


}
