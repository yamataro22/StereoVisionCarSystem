package com.example.stereovisioncarsystem;

import com.example.stereovisioncarsystem.Filtr.BinaryThreshFiltr;
import com.example.stereovisioncarsystem.Filtr.CannyFiltr;
import com.example.stereovisioncarsystem.Filtr.Filtr;
import com.example.stereovisioncarsystem.Filtr.GrayFiltr;

import org.opencv.core.Mat;

class RectangleFinder {
    private Filtr grayFiltr;
    private Filtr threshFiltr;
    private Filtr cannyFiltr;

    public RectangleFinder() {
        init();
    }

    private void init() {
        threshFiltr = new BinaryThreshFiltr(189);
        cannyFiltr = new CannyFiltr();
        grayFiltr = new GrayFiltr();
    }

    public void findRectangles(Mat frame)
    {
        if(frame.channels() == 3)
        {
            grayFiltr.filtr(frame);
        }
        applyFilters(frame);
    }

    private void applyFilters(Mat frame) {
        threshFiltr.filtr(frame);
        cannyFiltr.filtr(frame);
    }


}
