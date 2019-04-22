package com.example.stereovisioncarsystem.FilterCalibration;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.stereovisioncarsystem.FilterParameterTag;
import com.example.stereovisioncarsystem.R;

public class FilterSettingsActivity extends Activity {

    EditText threshEdittext;
    Spinner gaussSpinner;
    Button saveButton;

    private int threshValue = 120;
    private int gaussValue = 3;

    private final String TAG = "dataIO";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_settings);

        init();
        exqListeners();
        readParameters();
    }

    private void readParameters() {
        InternalMemoryDataManager dataManager = new InternalMemoryDataManager(getApplicationContext());

        try {
            threshValue = Integer.parseInt(dataManager.read(FilterParameterTag.Thresh));
            threshEdittext.setText(threshValue+"");
            gaussValue = Integer.parseInt(dataManager.read(FilterParameterTag.Gauss));
            gaussSpinner.setSelection(getSpinnerIndex(gaussSpinner,gaussValue+""));
        } catch (InternalMemoryDataManager.SavingException e) {
            e.printStackTrace();
            threshEdittext.setText(threshValue+"");
            gaussSpinner.setSelection(getSpinnerIndex(gaussSpinner,gaussValue+""));
        }

    }

    private int getSpinnerIndex(Spinner spinner, String myString){
        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)){
                return i;
            }
        }
        return 0;
    }

    private void init() {
        threshEdittext = findViewById(R.id.threshEditText);
        threshEdittext.setText(threshValue+"");

        gaussSpinner = findViewById(R.id.gaussSpinner);

        saveButton = findViewById(R.id.save_settings_button);
    }

    private void exqListeners() {
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InternalMemoryDataManager dataManager = new InternalMemoryDataManager(getApplicationContext());
                try {
                    dataManager.save(FilterParameterTag.Thresh, threshEdittext.getText().toString());
                    dataManager.save(FilterParameterTag.Gauss, gaussSpinner.getSelectedItem().toString());
                } catch (InternalMemoryDataManager.SavingException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void onClickCalibrateThresh(View v)
    {
        threshValue = Integer.parseInt(threshEdittext.getText().toString());
        Intent intent = new Intent(this, ThreshCalibrationActivity.class);
        intent.putExtra(ThreshCalibrationActivity.THRESH_1, threshValue);
        int requestCode = 1;
        startActivityForResult(intent, requestCode);
    }

    public void onClickCalibrateGauss(View view) {

        String value = gaussSpinner.getSelectedItem().toString();
        gaussValue = Integer.parseInt(value);


        Intent intent = new Intent(this, GaussCalibrationActivity.class);
        intent.putExtra(GaussCalibrationActivity.GAUSS_VALUE, gaussValue);
        intent.putExtra(GaussCalibrationActivity.THRESH_VALUE, threshValue);
        startActivity(intent);
    }

    protected void onActivityResult (int requestCode, int resultCode, Intent data) {

        if(requestCode == 1)
        {
            threshValue = data.getIntExtra(ThreshCalibrationActivity.THRESH_2, 100);
            threshEdittext.setText(threshValue+"");
        }
    }


}
