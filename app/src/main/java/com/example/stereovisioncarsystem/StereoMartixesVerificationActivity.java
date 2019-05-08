package com.example.stereovisioncarsystem;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.stereovisioncarsystem.FilterCalibration.InternalMemoryDataManager;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Pattern;

public class StereoMartixesVerificationActivity extends AppCompatActivity {
    private final int ROWS = 4;
    private final int COLS = 4;
    EditText e00,e01,e02,e03,e10,e11,e12,e13,e20,e21,e22,e23,e30,e31,e32,e33;
    EditText[][] editTextMatrix;


    Button loadButton, saveButton, editButton;
    RadioButton r1Button, r2Button, t1Button, t2Button, qButton;
    RadioGroup radioGroup;
    enum Edits{t1,t2,r1,r2,q};
    Edits currentlyEdited;


    Mat R1, R2, T1, T2, Q;
    boolean areEditable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stereo_martixes_verification);
        init();
        exqListeners();
        loadMatrixes();
    }

    private void init() {
        editTextMatrix = new EditText[ROWS][COLS];
        fillEditTextMatrixReferences();


        loadButton = findViewById(R.id.load_button);
        saveButton = findViewById(R.id.save_button);
        editButton = findViewById(R.id.edit_button);

        r1Button = findViewById(R.id.r1_button);
        r2Button = findViewById(R.id.r2_button);
        t1Button = findViewById(R.id.t1_button);
        t2Button = findViewById(R.id.t2_button);
        qButton = findViewById(R.id.q_button);
        radioGroup = findViewById(R.id.radioGroup);
    }

    private void fillEditTextMatrixReferences() {
        editTextMatrix[0][0] = findViewById(R.id.et_00);
        editTextMatrix[0][1] = findViewById(R.id.et_01);
        editTextMatrix[0][2] = findViewById(R.id.et_02);
        editTextMatrix[0][3] = findViewById(R.id.et_03);
        editTextMatrix[1][0] = findViewById(R.id.et_10);
        editTextMatrix[1][1] = findViewById(R.id.et_11);
        editTextMatrix[1][2] = findViewById(R.id.et_12);
        editTextMatrix[1][3] = findViewById(R.id.et_13);
        editTextMatrix[2][0] = findViewById(R.id.et_20);
        editTextMatrix[2][1] = findViewById(R.id.et_21);
        editTextMatrix[2][2] = findViewById(R.id.et_22);
        editTextMatrix[2][3] = findViewById(R.id.et_23);
        editTextMatrix[3][0] = findViewById(R.id.et_30);
        editTextMatrix[3][1] = findViewById(R.id.et_31);
        editTextMatrix[3][2] = findViewById(R.id.et_32);
        editTextMatrix[3][3] = findViewById(R.id.et_33);
    }

    private void exqListeners() {
        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StereoMartixesVerificationActivity.this.loadMatrixes();
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!areEditable)
                {
                    areEditable = true;
                    setParametersEditable(true);
                    editButton.setText("finish");
                }
                else
                {
                    areEditable = false;
                    setParametersEditable(false);
                    if(areAnyChanges(currentlyEdited))
                    {
                        Tools.makeToast(getApplicationContext(),"there are changes..");
                        updateMatrix(currentlyEdited);
                    }
                    else
                    {
                        Tools.makeToast(getApplicationContext(),"no changes!");
                    }
                    editButton.setText("edit");
                }

            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId)
                {
                    case R.id.r1_button:
                        currentlyEdited = Edits.r1;
                        loadMartixIntoTable(R1);
                        break;
                    case R.id.r2_button:
                        currentlyEdited = Edits.r2;
                        loadMartixIntoTable(R2);
                        break;
                    case R.id.t1_button:
                        currentlyEdited = Edits.t1;
                        loadMartixIntoTable(T1);
                        break;
                    case R.id.t2_button:
                        currentlyEdited = Edits.t2;
                        loadMartixIntoTable(T2);
                        break;
                    case R.id.q_button:
                        currentlyEdited = Edits.q;
                        loadMartixIntoTable(Q);
                        break;
                }
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InternalMemoryDataManager dataManager = new InternalMemoryDataManager(getApplicationContext());
                try {
                    dataManager.save(SavedParametersTags.QMatrix,Q.dump());
                    dataManager.save(SavedParametersTags.R1,R1.dump());
                    dataManager.save(SavedParametersTags.R2,R2.dump());
                    dataManager.save(SavedParametersTags.T1,T1.dump());
                    dataManager.save(SavedParametersTags.T2,T2.dump());

                    Tools.makeToast(getApplicationContext(), "zapisano:)");
                } catch (InternalMemoryDataManager.SavingException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void loadMatrixes() {
        CameraParametersMessager cameraMessager = new CameraParametersMessager(getApplicationContext());
        try {
            R1 = cameraMessager.readStereoCalibMatrix(SavedParametersTags.R1);
            R2 = cameraMessager.readStereoCalibMatrix(SavedParametersTags.R2);
            T1 = cameraMessager.readStereoCalibMatrix(SavedParametersTags.T1);
            T2 = cameraMessager.readStereoCalibMatrix(SavedParametersTags.T2);

            cameraMessager.readQMartix();
            Q = cameraMessager.getQMat();
            qButton.setChecked(true);
            loadMartixIntoTable(Q);
            currentlyEdited = Edits.q;
        } catch (InternalMemoryDataManager.SavingException e) {
            e.printStackTrace();
            Tools.makeToast(getApplicationContext(), "Not all martixes in memory");
        }
    }

    private boolean areAnyChanges(Edits currentlyEdited) {
        boolean isChanged = false;
        switch (currentlyEdited)
        {
            case q:
                isChanged = isPosMatChanged(Q);
                break;
            case r1:
                isChanged = isPosMatChanged(R1);
                break;
            case r2:
                isChanged = isPosMatChanged(R2);
                break;
            case t1:
                isChanged = isPosMatChanged(T1);
                break;
            case t2:
                isChanged = isPosMatChanged(T2);
                break;
        }
        return isChanged;
    }

    private boolean isPosMatChanged(Mat mat)
    {

        for (int rows = 0; rows < mat.rows(); rows++) {
            for (int cols = 0; cols < mat.cols(); cols++) {
                double m = round(mat.get(0,0)[0],2);
                if (checkField(m, editTextMatrix[rows][cols])) return true;
            }
        }
        return false;
    }

    private boolean checkField(double m, EditText et) {
        if (m != Double.parseDouble(et.getText().toString())) return true;
        return false;
    }

    private void updateMatrix(Edits currentlyEdited) {
        switch (currentlyEdited)
        {
            case q:
                updateMat(Q);
                break;
            case r1:
                updateMat(R1);
                break;
            case r2:
                updateMat(R2);
                break;
            case t1:
                updateMat(T1);
                break;
            case t2:
                updateMat(T2);
                break;
        }

    }

    private void updateMat(Mat mat)
    {
        for(int rows = 0; rows < mat.rows(); rows++)
        {
            for(int cols = 0; cols < mat.cols(); cols++)
            {
                Log.d("updateMat", "rows:"+rows +" cols:"+cols);
                mat.put(rows,cols,Double.parseDouble(editTextMatrix[rows][cols].getText().toString()));
            }
        }
    }

    private void setParametersEditable(boolean b) {
        for(int rows = 0; rows < ROWS; rows++)
        {
            for(int cols = 0; cols < COLS; cols++)
            {
                editTextMatrix[rows][cols].setEnabled(b);
            }
        }
    }

    private void loadMartixIntoTable(Mat mat) {

        if(mat.rows()==4)
            setTableSizeOf4();
        else
            setTableSizeOf3();

        updateTableValues(mat);
    }

    private void updateTableValues(Mat mat) {
        for (int rows = 0; rows < mat.rows(); rows++) {
            for (int cols = 0; cols < mat.cols(); cols++) {
                editTextMatrix[rows][cols].setText(round(mat.get(rows, cols)[0], 2) + "");
            }
        }
    }

    private void setTableSizeOf3() {
        setBoundaryMatrixElemetsVisibility(View.GONE);
    }

    private void setTableSizeOf4()
    {
        setBoundaryMatrixElemetsVisibility(View.VISIBLE);
    }

    private void setBoundaryMatrixElemetsVisibility(int gone) {
        for (int rows = 0; rows < ROWS; rows++) {
            for (int cols = 0; cols < COLS; cols++) {
                if (rows == 3 || cols == 3)
                    editTextMatrix[rows][cols].setVisibility(gone);
            }
        }
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bid = new BigDecimal(value);
        bid = bid.setScale(places, RoundingMode.HALF_UP);
        return bid.doubleValue();
    }




}
