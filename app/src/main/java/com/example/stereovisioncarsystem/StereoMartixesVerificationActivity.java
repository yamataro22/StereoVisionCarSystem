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

    EditText e00,e01,e02,e03,e10,e11,e12,e13,e20,e21,e22,e23,e30,e31,e32,e33;
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
        e00 = findViewById(R.id.et_00);
        e01 = findViewById(R.id.et_01);
        e02 = findViewById(R.id.et_02);
        e03 = findViewById(R.id.et_03);
        e10 = findViewById(R.id.et_10);
        e11 = findViewById(R.id.et_11);
        e12 = findViewById(R.id.et_12);
        e13 = findViewById(R.id.et_13);
        e20 = findViewById(R.id.et_20);
        e21 = findViewById(R.id.et_21);
        e22 = findViewById(R.id.et_22);
        e23 = findViewById(R.id.et_23);
        e30 = findViewById(R.id.et_30);
        e31 = findViewById(R.id.et_31);
        e32 = findViewById(R.id.et_32);
        e33 = findViewById(R.id.et_33);

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
                isChanged = isQMatChanged();
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
        double m = round(mat.get(0,0)[0],2);
        if (checkField(m, e00)) return true;
        m = round(mat.get(0,1)[0],2);
        if (checkField(m, e01)) return true;
        m = round(mat.get(0,2)[0],2);
        if (checkField(m, e02)) return true;
        m = round(mat.get(1,0)[0],2);
        if (checkField(m, e10)) return true;
        m = round(mat.get(1,1)[0],2);
        if (checkField(m, e11)) return true;
        m = round(mat.get(1,2)[0],2);
        if (checkField(m, e12)) return true;
        m = round(mat.get(2,0)[0],2);
        if (checkField(m, e20)) return true;
        m = round(mat.get(2,1)[0],2);
        if (checkField(m, e21)) return true;
        m = round(mat.get(2,2)[0],2);
        if (checkField(m, e22)) return true;

        return false;
    }

    private boolean isQMatChanged() {

        double m = round(Q.get(0,0)[0],2);
        if (checkField(m, e00)) return true;
        m = round(Q.get(0,1)[0],2);
        if (checkField(m, e01)) return true;
        m = round(Q.get(0,2)[0],2);
        if (checkField(m, e02)) return true;
        m = round(Q.get(0,3)[0],2);
        if (checkField(m, e03)) return true;
        m = round(Q.get(1,0)[0],2);
        if (checkField(m, e10)) return true;
        m = round(Q.get(1,1)[0],2);
        if (checkField(m, e11)) return true;
        m = round(Q.get(1,2)[0],2);
        if (checkField(m, e12)) return true;
        m = round(Q.get(1,3)[0],2);
        if (checkField(m, e13)) return true;
        m = round(Q.get(2,0)[0],2);
        if (checkField(m, e20)) return true;
        m = round(Q.get(2,1)[0],2);
        if (checkField(m, e21)) return true;
        m = round(Q.get(2,2)[0],2);
        if (checkField(m, e22)) return true;
        m = round(Q.get(2,3)[0],2);
        if (checkField(m, e23)) return true;
        m = round(Q.get(3,0)[0],2);
        if (checkField(m, e30)) return true;
        m = round(Q.get(3,1)[0],2);
        if (checkField(m, e31)) return true;
        m = round(Q.get(3,2)[0],2);
        if (checkField(m, e32)) return true;
        m = round(Q.get(3,3)[0],2);
        if (checkField(m, e33)) return true;

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
                updateQMat();
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
        mat.put(0,0,Double.parseDouble(e00.getText().toString()));
        mat.put(0,1,Double.parseDouble(e01.getText().toString()));
        mat.put(0,2,Double.parseDouble(e02.getText().toString()));
        mat.put(1,0,Double.parseDouble(e10.getText().toString()));
        mat.put(1,1,Double.parseDouble(e11.getText().toString()));
        mat.put(1,2,Double.parseDouble(e12.getText().toString()));
        mat.put(2,0,Double.parseDouble(e20.getText().toString()));
        mat.put(2,1,Double.parseDouble(e21.getText().toString()));
        mat.put(2,2,Double.parseDouble(e22.getText().toString()));
    }

    private void updateQMat()
    {
        Q.put(0,0,Double.parseDouble(e00.getText().toString()));
        Q.put(0,1,Double.parseDouble(e01.getText().toString()));
        Q.put(0,2,Double.parseDouble(e02.getText().toString()));
        Q.put(0,3,Double.parseDouble(e03.getText().toString()));
        Q.put(1,0,Double.parseDouble(e10.getText().toString()));
        Q.put(1,1,Double.parseDouble(e11.getText().toString()));
        Q.put(1,2,Double.parseDouble(e12.getText().toString()));
        Q.put(1,3,Double.parseDouble(e13.getText().toString()));
        Q.put(2,0,Double.parseDouble(e20.getText().toString()));
        Q.put(2,1,Double.parseDouble(e21.getText().toString()));
        Q.put(2,2,Double.parseDouble(e22.getText().toString()));
        Q.put(2,3,Double.parseDouble(e23.getText().toString()));
        Q.put(3,0,Double.parseDouble(e30.getText().toString()));
        Q.put(3,1,Double.parseDouble(e31.getText().toString()));
        Q.put(3,2,Double.parseDouble(e32.getText().toString()));
        Q.put(3,3,Double.parseDouble(e33.getText().toString()));
    }


    private void setParametersEditable(boolean b) {
        e00.setEnabled(b);
        e01.setEnabled(b);
        e02.setEnabled(b);
        e03.setEnabled(b);
        e10.setEnabled(b);
        e11.setEnabled(b);
        e12.setEnabled(b);
        e13.setEnabled(b);
        e20.setEnabled(b);
        e21.setEnabled(b);
        e22.setEnabled(b);
        e23.setEnabled(b);
        e30.setEnabled(b);
        e31.setEnabled(b);
        e32.setEnabled(b);
        e33.setEnabled(b);
    }


    private void loadMartixIntoTable(Mat mat) {
        if(mat.rows()==4)
        {
            setTableSizeOf4();
            e00.setText(round(mat.get(0,0)[0],2)+"");
            e01.setText(round(mat.get(0,1)[0],2)+"");
            e02.setText(round(mat.get(0,2)[0],2)+"");
            e03.setText(round(mat.get(0,3)[0],2)+"");
            e10.setText(round(mat.get(1,0)[0],2)+"");
            e11.setText(round(mat.get(1,1)[0],2)+"");
            e12.setText(round(mat.get(1,2)[0],2)+"");
            e13.setText(round(mat.get(1,3)[0],2)+"");
            e20.setText(round(mat.get(2,0)[0],2)+"");
            e21.setText(round(mat.get(2,1)[0],2)+"");
            e22.setText(round(mat.get(2,2)[0],2)+"");
            e23.setText(round(mat.get(2,3)[0],2)+"");
            e30.setText(round(mat.get(3,0)[0],2)+"");
            e31.setText(round(mat.get(3,1)[0],2)+"");
            e32.setText(round(mat.get(3,2)[0],2)+"");
            e33.setText(round(mat.get(3,3)[0],2)+"");
        }
        else
        {
            setTableSizeOf3();
            e00.setText(round(mat.get(0,0)[0],2)+"");
            e01.setText(round(mat.get(0,1)[0],2)+"");
            e02.setText(round(mat.get(0,2)[0],2)+"");
            e10.setText(round(mat.get(1,0)[0],2)+"");
            e11.setText(round(mat.get(1,1)[0],2)+"");
            e12.setText(round(mat.get(1,2)[0],2)+"");
            e20.setText(round(mat.get(2,0)[0],2)+"");
            e21.setText(round(mat.get(2,1)[0],2)+"");
            e22.setText(round(mat.get(2,2)[0],2)+"");
        }
    }

    private void setTableSizeOf3() {
        e03.setVisibility(View.GONE);
        e13.setVisibility(View.GONE);
        e23.setVisibility(View.GONE);
        e33.setVisibility(View.GONE);
        e30.setVisibility(View.GONE);
        e31.setVisibility(View.GONE);
        e32.setVisibility(View.GONE);
    }

    private void setTableSizeOf4()
    {
        e03.setVisibility(View.VISIBLE);
        e13.setVisibility(View.VISIBLE);
        e23.setVisibility(View.VISIBLE);
        e33.setVisibility(View.VISIBLE);
        e30.setVisibility(View.VISIBLE);
        e31.setVisibility(View.VISIBLE);
        e32.setVisibility(View.VISIBLE);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bid = new BigDecimal(value);
        bid = bid.setScale(places, RoundingMode.HALF_UP);
        return bid.doubleValue();
    }




}
