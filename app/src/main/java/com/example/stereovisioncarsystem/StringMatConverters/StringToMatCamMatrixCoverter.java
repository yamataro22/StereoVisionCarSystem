package com.example.stereovisioncarsystem.StringMatConverters;

import java.util.regex.Pattern;

public class StringToMatCamMatrixCoverter extends StringToMatConverter {

    public StringToMatCamMatrixCoverter() {
    }

    public StringToMatCamMatrixCoverter(int matrixRows, int matrixCols) {
        super(matrixRows, matrixCols);
    }

    @Override
    protected void initRegexMatcher() {
        pattern = Pattern.compile("[0-9]{4},[0-9]{2}");
    }
}
