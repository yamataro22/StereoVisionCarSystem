package com.example.stereovisioncarsystem;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.ImageView;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.Map;


public class CameraScreenActivity extends CameraBasicActivity {

    public static final String TYPE = "THRESH";
    public static final String THRESHPARAM = "140";

    public static final String PARAMETERS = "messageBlur";
    private boolean[] filters;
    Filtr f = new Filtr();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_screen);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Intent intent = getIntent();
            VisionParameters visionParams = intent.getParcelableExtra(PARAMETERS);
            filters = new boolean[VisionParameters.FILTERS_QUANTITY];

            int i = 0;
            for(Enum key:visionParams.filtersMap.keySet()) {
                filters[i] = visionParams.get(key);
                i++;
            }
            f.setThreshBinaryParam(visionParams.getThreshValue());
            f.setgBlurParam(visionParams.getGaussValue());
        }

        mOpenCvCameraView = findViewById(R.id.HelloOpenCvView);
        mOpenCvCameraView.enableView();
        mOpenCvCameraView.enableFpsMeter();
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }



    public void putMatInImageView(Mat m, ImageView view)
    {
        Imgproc.putText(m, "hi there ;)", new Point(30,80), Core.FONT_HERSHEY_SCRIPT_SIMPLEX, 2.2, new Scalar(200,200,0),2);
        Bitmap bm = Bitmap.createBitmap(m.cols(), m.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(m, bm);
        view.setImageBitmap(bm);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(Mat inputFrame) {
        Mat dst = new Mat();

        for(int i = 0; i < filters.length-1; i++)
        {
            if(filters[i]) f.filtr(Filtr.filters.values()[i],inputFrame);
        }
        if(filters[filters.length-1])
        {
            f.canny(inputFrame, dst);
            return dst;
        }
        return inputFrame;
    }

    @Override
    public void onPause()
    {
        Log.i("filtry", "jestem w OnPause");
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }
    public void onDestroy() {
        super.onDestroy();
        Log.i("filtry", "jestem w onDestroy");
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }


}
