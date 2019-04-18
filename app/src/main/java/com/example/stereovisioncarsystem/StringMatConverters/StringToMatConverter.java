package com.example.stereovisioncarsystem.StringMatConverters;

import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class StringToMatConverter {

    protected int matrixRows = 3;
    protected int matrixCols = 3;

    protected Pattern pattern;
    protected Matcher matcher;
    protected List<String> matches;
    protected Double[] parametersList;




    public StringToMatConverter() {
    }

    public StringToMatConverter(int matrixRows, int matrixCols) {
        this.matrixCols = matrixCols;
        this.matrixRows = matrixRows;
    }

    public Mat convert(String stringMat)
    {
        Log.d("MessagerLogs", "distCoeffs input" + stringMat);
        initRegexMatcher();
        findPatternInString(stringMat);
        createListFromMatches();
        replaceComasWithDots();
        converStringToDoubleArray();
        return createMatFromDoubleArray();
    }

    private void findPatternInString(String stringMat)
    {
        matcher = pattern.matcher(stringMat);
    }

    protected abstract void initRegexMatcher();



    private void createListFromMatches() {

        matches = new ArrayList<>(9);
        while(matcher.find())
        {
            matches.add(matcher.group());
        }
    }

    private void converStringToDoubleArray() {
        parametersList = new Double[matrixRows*matrixCols];
        for(int i = 0; i < matches.size(); i++)
        {
            parametersList[i] = Double.parseDouble(matches.get(i));
        }
    }

    private Mat createMatFromDoubleArray() {
        Mat mat = new Mat(matrixRows, matrixCols, CvType.CV_32FC1);
        int k = 0;
        for(int i = 0; i < matrixRows; i++)
        {
            for(int j = 0; j < matrixCols; j++)
                {
                mat.put(i,j,parametersList[k++]);
            }
        }
        return mat;
    }

    private void replaceComasWithDots() {
        List<String> matchesDot = new ArrayList<>(9);
        for(String x : matches)
        {
            matchesDot.add( x.replace(',','.'));
        }
        matches = matchesDot;
    }

}
