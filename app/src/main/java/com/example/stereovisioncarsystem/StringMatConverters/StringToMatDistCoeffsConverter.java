package com.example.stereovisioncarsystem.StringMatConverters;

import com.example.stereovisioncarsystem.StringMatConverters.StringToMatConverter;

import java.util.regex.Pattern;

public class StringToMatDistCoeffsConverter extends StringToMatConverter {

    public StringToMatDistCoeffsConverter(int matrixRows, int matrixCols) {
        super(matrixRows, matrixCols);
    }

    @Override
    protected void initRegexMatcher() {
        pattern = Pattern.compile("-?[0-9],[0-9]{2}");
    }
}
