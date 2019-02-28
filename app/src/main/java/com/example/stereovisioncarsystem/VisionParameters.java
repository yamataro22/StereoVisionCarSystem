package com.example.stereovisioncarsystem;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by adamw on 19.11.2018.
 */

public class VisionParameters implements Parcelable{

    private int gaussValue = 3;
    private int threshValue = 200;
    public static final int FILTERS_QUANTITY = 7;

    public Map<Filtr, Boolean> filtersMap = new LinkedHashMap<>();

    public VisionParameters() {
//        filtersMap.put(,false);
//        filtersMap.put(Filtr.filters.MBLUR,false);
//        filtersMap.put(Filtr.filters.GAUSS,false);
//        filtersMap.put(Filtr.filters.SHARPEN,false);
//        filtersMap.put(Filtr.filters.THRESHBINARY,false);
//        filtersMap.put(Filtr.filters.THRESHRGB,false);
//        filtersMap.put(Filtr.filters.CANNY,false);
    }


    public void toggleCanny()
    {

//        filtersMap.put(Filtr.filters.CANNY, !filtersMap.get(Filtr.filters.CANNY));
//        if(filtersMap.get(Filtr.filters.CANNY))
//        {
//            filtersMap.put(Filtr.filters.GRAY, true);
//            filtersMap.put(Filtr.filters.THRESHBINARY, true);
//        }
    }
    public void toggleBlur()
    {
       // filtersMap.put(Filtr.filters.MBLUR, !filtersMap.get(Filtr.filters.MBLUR));
    }
    public void toggleGray()
    {
      //  filtersMap.put(Filtr.filters.GRAY, !filtersMap.get(Filtr.filters.GRAY));
    }
    public void toggleGauss() {
        //filtersMap.put(Filtr.filters.GAUSS, !filtersMap.get(Filtr.filters.GAUSS));
    }

    public void toggleThresh()
    {
//        filtersMap.put(Filtr.filters.THRESHBINARY, !filtersMap.get(Filtr.filters.THRESHBINARY));
//        if(filtersMap.get(Filtr.filters.THRESHBINARY))
//        {
//            filtersMap.put(Filtr.filters.GRAY, true);
//        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    //    public int getThreshValue()
//    {
//        return threshValue;
//    }
//    public int getGaussValue()
//        {
//        return gaussValue;
//    }
//    public boolean get(Enum key)
//    {
//        return filtersMap.get(key);
//    }
//    public boolean getGray()
//    {
//        return filtersMap.get(Filtr.filters.GRAY);
//    }
//    public boolean getBlur()
//    {
//        return filtersMap.get(Filtr.filters.MBLUR);
//    }
//    public boolean getGauss()
//    {
//        return filtersMap.get(Filtr.filters.GAUSS);
//    }
//    public boolean getThresh()
//    {
//        return filtersMap.get(Filtr.filters.THRESHBINARY);
//    }
//    public boolean getCanny()
//    {
//        return filtersMap.get(Filtr.filters.CANNY);
//    }
//
//    public void setGray(boolean state)
//    {
//        filtersMap.put(Filtr.filters.GRAY, state);
//    }
//    public void setGauss(boolean state)
//    {
//        filtersMap.put(Filtr.filters.GAUSS, state);
//    }
//    public void setBlur(boolean state)
//    {
//        filtersMap.put(Filtr.filters.MBLUR, state);
//    }
//    public void setThresh(boolean state)
//    {
//        filtersMap.put(Filtr.filters.THRESHBINARY, state);
//    }
//    public void setCanny(boolean state)
//    {
//        filtersMap.put(Filtr.filters.CANNY, state);
//    }
//    public void setThreshValue(int newValue)
//    {
//        if(newValue > 0 && newValue < 256)
//        {
//            threshValue = newValue;
//        }
//    }
//    public void setGaussValue(int newValue)
//    {
//
//            gaussValue = newValue;
//
//    }
//
//    // 99.9% of the time you can just ignore this
//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
    // write your object's data to the passed-in Parcel
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(filtersMap.size());
        for(Map.Entry<Filtr,Boolean> entry : filtersMap.entrySet()){
            //out.writeString(entry.getKey().name());
           // out.writeValue(entry.getValue());
        }
        out.writeInt(threshValue);
        out.writeInt(gaussValue);
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<VisionParameters> CREATOR = new Parcelable.Creator<VisionParameters>() {
        public VisionParameters createFromParcel(Parcel in) {
            return new VisionParameters();
        }

        public VisionParameters[] newArray(int size) {
            return new VisionParameters[size];
        }


    };
//
//    // example constructor that takes a Parcel and gives you an object populated with it's values
//    private VisionParameters(Parcel in) {
//        int size = in.readInt();
//        for(int i = 0; i < size; i++){
//            Filtr.filters key = Filtr.filters.valueOf(in.readString());
//            Boolean value = (Boolean)in.readValue(null);
//            filtersMap.put(key,value);
//        }
//        threshValue = in.readInt();
//        gaussValue = in.readInt();
//    }
}
