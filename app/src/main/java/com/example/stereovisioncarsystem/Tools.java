package com.example.stereovisioncarsystem;

import android.content.Context;
import android.util.Log;
import android.widget.Spinner;
import android.widget.Toast;

import org.opencv.core.Mat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class Tools {

    public static void makeToast(Context context, String message)
    {
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
    }

    public static int getIntFromString(String string)
    {
        return Integer.parseInt(string);
    }

    public static int getSpinnerIndex(Spinner spinner, String myString){
        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)){
                return i;
            }
        }
        return 0;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bid = new BigDecimal(value);
        bid = bid.setScale(places, RoundingMode.HALF_UP);
        return bid.doubleValue();
    }

    public static String getNonScientificMatValues(Mat data) {
        String tempMat = "[";
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        decimalFormat.setMaximumFractionDigits(15);
        for(int row = 0; row < data.rows(); row++)
        {
            for(int col = 0; col < data.cols(); col++)
            {
                tempMat += decimalFormat.format(data.get(row,col)[0]).replace(',', '.');
                if(col < data.cols() - 1) tempMat += ", ";
            }
            if(row < data.rows() - 1) tempMat += ";\n";
        }
        tempMat += "]";
        Log.d("nonScientificMat", tempMat);
        return tempMat;
    }

}
