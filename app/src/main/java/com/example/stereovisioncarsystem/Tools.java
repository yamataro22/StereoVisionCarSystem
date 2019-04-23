package com.example.stereovisioncarsystem;

import android.content.Context;
import android.widget.Spinner;
import android.widget.Toast;

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

}
