package com.example.stereovisioncarsystem.FilterCalibration;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.stereovisioncarsystem.SavedParametersTags;
import com.example.stereovisioncarsystem.R;

public class FilterSettingsActivity extends Activity {

    EditText threshEdittext;
    Spinner gaussSpinner;
    Button saveButton;
    CheckBox isInvertedCheckBox;

    private int threshValue = 120;
    private int gaussValue = 3;
    private boolean isThreshInverted = false;

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
            threshValue = Integer.parseInt(dataManager.read(SavedParametersTags.Thresh));
            threshEdittext.setText(threshValue+"");
            gaussValue = Integer.parseInt(dataManager.read(SavedParametersTags.Gauss));
            gaussSpinner.setSelection(getSpinnerIndex(gaussSpinner,gaussValue+""));
            isThreshInverted = Boolean.parseBoolean(dataManager.read(SavedParametersTags.IsThreshInverted));
            isInvertedCheckBox.setChecked(isThreshInverted);

        } catch (InternalMemoryDataManager.SavingException e) {
            e.printStackTrace();
            threshEdittext.setText(threshValue+"");
            gaussSpinner.setSelection(getSpinnerIndex(gaussSpinner,gaussValue+""));
            isInvertedCheckBox.setChecked(false);
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
        isInvertedCheckBox = findViewById(R.id.is_inverted_checkbox);
    }

    private void exqListeners() {

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InternalMemoryDataManager dataManager = new InternalMemoryDataManager(getApplicationContext());
                try {
                    dataManager.save(SavedParametersTags.Thresh, threshEdittext.getText().toString());
                    dataManager.save(SavedParametersTags.Gauss, gaussSpinner.getSelectedItem().toString());
                    dataManager.save(SavedParametersTags.IsThreshInverted, isInvertedCheckBox.isChecked()+"");
                } catch (InternalMemoryDataManager.SavingException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void onClickCalibrateThresh(View v)
    {
        threshValue = Integer.parseInt(threshEdittext.getText().toString());
        isThreshInverted = isInvertedCheckBox.isChecked();

        Intent intent = new Intent(this, ThreshCalibrationActivity.class);
        intent.putExtra(ThreshCalibrationActivity.THRESH_1, threshValue);
        intent.putExtra(ThreshCalibrationActivity.THRESH_3,isThreshInverted);

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
