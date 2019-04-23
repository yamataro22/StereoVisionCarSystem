package com.example.stereovisioncarsystem;

import android.content.Context;
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
}
