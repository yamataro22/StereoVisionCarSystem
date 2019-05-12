package com.example.stereovisioncarsystem;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.stereovisioncarsystem.FilterCalibration.InternalMemoryDataManager;

import org.opencv.core.Mat;

public class StereoMartixesVerificationActivity extends AppCompatActivity {
    private final int ROWS = 4;
    private final int COLS = 4;
    private final int ROUND = 5;

    EditText[][] editTextMatrix;


    Button loadButton, saveButton, editButton;
    RadioButton r1Button, r2Button, t1Button, t2Button, qButton;
    RadioButton rButton, tButton, eButton, fButton;
    RadioGroup radioGroup;

    enum Edits{t1,t2,r1,r2,q,r,t,e,f}

    Edits currentlyEdited;

    Mat R1, R2, T1, T2, Q, Rmat, T, E, F;
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
        rButton = findViewById(R.id.r_button);
        tButton = findViewById(R.id.t_button);
        eButton = findViewById(R.id.e_button);
        fButton = findViewById(R.id.f_button);
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
                    if(isPosMatChanged(getCurrentlyEditedMat()))
                    {
                        Tools.makeToast(getApplicationContext(),"there are changes..");
                        updateMat(getCurrentlyEditedMat());
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
                    case R.id.r_button:
                        currentlyEdited = Edits.r;
                        loadMartixIntoTable(Rmat);
                        break;
                    case R.id.t_button:
                        currentlyEdited = Edits.t;
                        loadMartixIntoTable(T);
                        break;
                    case R.id.e_button:
                        currentlyEdited = Edits.e;
                        loadMartixIntoTable(E);
                        break;
                    case R.id.f_button:
                        currentlyEdited = Edits.f;
                        loadMartixIntoTable(F);
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

                    dataManager.save(SavedParametersTags.R,Rmat.dump());
                    dataManager.save(SavedParametersTags.T,T.dump());
                    dataManager.save(SavedParametersTags.E,E.dump());
                    dataManager.save(SavedParametersTags.F,F);

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
            R1 = cameraMessager.readStereoRMatrix(SavedParametersTags.R1);
            R2 = cameraMessager.readStereoRMatrix(SavedParametersTags.R2);
            T1 = cameraMessager.readStereoPMatrix(SavedParametersTags.T1);
            T2 = cameraMessager.readStereoPMatrix(SavedParametersTags.T2);
            Rmat = cameraMessager.readStereoRMatrix(SavedParametersTags.R);
            T = cameraMessager.readStereoTMatrix(SavedParametersTags.T);
            E = cameraMessager.readStereoRMatrix(SavedParametersTags.E);
            F = cameraMessager.readFMatrix(SavedParametersTags.F);

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


    private Mat getCurrentlyEditedMat()
    {
        switch(currentlyEdited)
        {
            case q:
                return Q;
            case r1:
                return R1;
            case r2:
                return R2;
            case t1:
                return T1;
            case t2:
                return T2;
            case r:
                return Rmat;
            case t:
                return T;
            case e:
                return E;
            case f:
                return F;
            default:
                return null;
        }
    }

    private boolean isPosMatChanged(Mat mat)
    {

        for (int rows = 0; rows < mat.rows(); rows++) {
            for (int cols = 0; cols < mat.cols(); cols++) {
                double m = Tools.round(mat.get(0,0)[0], ROUND);
                if (checkField(m, editTextMatrix[rows][cols])) return true;
            }
        }
        return false;
    }

    private boolean checkField(double m, EditText et) {
        if (m != Double.parseDouble(et.getText().toString())) return true;
        return false;
    }

    private void updateMat(Mat mat)
    {
        for(int rows = 0; rows < mat.rows(); rows++)
        {
            for(int cols = 0; cols < mat.cols(); cols++)
            {
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

        adjustTableSize(mat.rows(), mat.cols());
        updateTableValues(mat);

    }

    private void adjustTableSize(int p_rows, int p_cols) {

        for (int rows = 0; rows < ROWS; rows++) {
            for (int cols = 0; cols < COLS; cols++) {
                if(rows < p_rows && cols < p_cols)
                    editTextMatrix[rows][cols].setVisibility(View.VISIBLE);
                else
                    editTextMatrix[rows][cols].setVisibility(View.GONE);
            }
        }
    }

    private void updateTableValues(Mat mat) {
        for (int rows = 0; rows < mat.rows(); rows++) {
            for (int cols = 0; cols < mat.cols(); cols++) {
                editTextMatrix[rows][cols].setText(Tools.round(mat.get(rows, cols)[0], ROUND) + "");
            }
        }
    }


}
