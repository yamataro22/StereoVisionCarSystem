package com.example.stereovisioncarsystem;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.stereovisioncarsystem.CameraCapturers.ObservedCameraFramesCapturer;
import com.example.stereovisioncarsystem.ServerClientCommunication.ClientServerMessages;
import com.example.stereovisioncarsystem.ServerClientCommunication.ServerHandlerMsg;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class ServerDisparityBasedDistanceMeter extends ServerDistanceMeter implements ObservedCameraFramesCapturer.CameraFrameConnector
                                                                                    , DisparityPhotoParser.DistanceListener {

    private Button switchViewButton, hideViewButton;
    private ImageView verificationImageView;
    private TextView disparityTextView, lengthTextView;
    private boolean isFilteredImageVisible = false;
    private Bitmap filteredDisparityImageBitmap;
    private Bitmap disparityImageBitmap;

    public final static String TAG = "serverClientCom";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        exqListeners();
    }

    @Override
    protected void initSpecificGUI() {
        hideViewButton = findViewById(R.id.hide_view_button);
        switchViewButton = findViewById(R.id.switch_image_button);
        verificationImageView = findViewById(R.id.disparity_image_view);
        disparityTextView = findViewById(R.id.disparity_text_view);
        lengthTextView = findViewById(R.id.length_text_view);
    }

    private void showVerificationGUI() {
        disableView();
        setStandardGUIVisibility(View.GONE);
        setVerificationGUIVisibility(View.VISIBLE);
    }

    private void hideVerificationGUI() {
        setVerificationGUIVisibility(View.GONE);
        setStandardGUIVisibility(View.VISIBLE);
        enableView();
    }

    private void setStandardGUIVisibility(int visibility) {
        connectButton.setVisibility(visibility);
        skipFramesButton.setVisibility(visibility);
        statusTextView.setVisibility(visibility);
        im.setVisibility(visibility);
        mOpenCvCameraView.setVisibility(visibility);
        disparityImageView.setVisibility(visibility);
        distanceTextView.setVisibility(visibility);
    }

    private void setVerificationGUIVisibility(int visible) {
        hideViewButton.setVisibility(visible);
        switchViewButton.setVisibility(visible);
        verificationImageView.setVisibility(visible);
        disparityTextView.setVisibility(visible);
        lengthTextView.setVisibility(visible);
    }

    @Override
    protected void initParser() {
        stereoPhotoParser = new DisparityPhotoParser(this);

    }

    @SuppressLint("ClickableViewAccessibility")
    private void exqListeners() {

        switchViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchVerificationFrame();
            }
        });

        hideViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideVerificationGUI();
            }
        });

        verificationImageView.setOnTouchListener(new View.OnTouchListener() {
            Matrix inverse = new Matrix();
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int index = event.getActionIndex();
                final float[] coords = new float[] { event.getX(index), event.getY(index) };

                verificationImageView.getImageMatrix().invert(inverse);
                inverse.postTranslate(v.getScrollX(), v.getScrollY());
                inverse.mapPoints(coords);

                int x = (int) Math.floor(coords[0]);
                int y = (int) Math.floor(coords[1]);
                double disp = stereoPhotoParser.retreiveDisparity(x,y);
                disparityTextView.setText(disp+"");

                double length = stereoPhotoParser.calculateLenghtFromDisparity(x,y);
                lengthTextView.setText(length+"");

                Log.d("TouchLocation", "onTouch x: " + x + ", y: " + y);
                return false;
            }
        });
    }

    private void switchVerificationFrame() {
        if(isFilteredImageVisible)
        {
            isFilteredImageVisible = false;
            verificationImageView.setImageBitmap(disparityImageBitmap);
        }
        else
        {
            isFilteredImageVisible = true;
            verificationImageView.setImageBitmap(filteredDisparityImageBitmap);
        }
    }


    @Override
    protected void processClientFrame(Mat mat) {
        stereoPhotoParser.addClientFrame(mat);
        Bitmap btm = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, btm);
        im.setImageBitmap(btm);
    }

    @Override
    public void processServerFrame(Mat frame) {
        stereoPhotoParser.addServerFrame(frame);
        stereoPhotoParser.computeDisparityMap();
    }

    @Override
    public void onDistanceCalculated(final double distanceInPixels,final double distanceInLength) {
        //no need to implement
    }

    @Override
    public void onDisparityCalculated(final Mat disparityMap, final Mat disparity8) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showVerificationGUI();
                filteredDisparityImageBitmap = Bitmap.createBitmap(disparityMap.cols(), disparityMap.rows(), Bitmap.Config.ARGB_8888);
                disparityImageBitmap = Bitmap.createBitmap(disparity8.cols(), disparity8.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(disparityMap, filteredDisparityImageBitmap);
                Utils.matToBitmap(disparity8, disparityImageBitmap);
                setImage(verificationImageView,filteredDisparityImageBitmap);
            }
        });
    }


    private void setImage(final ImageView image,final Bitmap btm){
            runOnUiThread(new Runnable() {
            @Override
            public void run() {
                image.setImageBitmap(btm);
            }
        });
    }

}
