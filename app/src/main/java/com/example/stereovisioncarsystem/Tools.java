package com.example.stereovisioncarsystem;

import android.content.Context;
import android.widget.Spinner;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
}
