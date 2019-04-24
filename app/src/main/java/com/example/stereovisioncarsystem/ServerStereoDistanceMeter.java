package com.example.stereovisioncarsystem;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stereovisioncarsystem.CameraCapturers.ObservedCameraFramesCapturer;
import com.example.stereovisioncarsystem.CameraCapturers.ObservedSingleCameraFramesCapturer;
import com.example.stereovisioncarsystem.FilterCalibration.InternalMemoryDataManager;
import com.example.stereovisioncarsystem.Filtr.BinaryThreshFiltr;
import com.example.stereovisioncarsystem.Filtr.CannyFiltr;
import com.example.stereovisioncarsystem.Filtr.Filtr;
import com.example.stereovisioncarsystem.Filtr.GBlurFiltr;
import com.example.stereovisioncarsystem.Filtr.GrayFiltr;
import com.example.stereovisioncarsystem.ServerClientCommunication.ClientServerMessages;
import com.example.stereovisioncarsystem.ServerClientCommunication.ServerHandlerMsg;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

public class ServerStereoDistanceMeter extends CommunicationBasicActivity implements View.OnTouchListener, ObservedCameraFramesCapturer.CameraFrameConnector
                                                                                    , StereoPhotoParser.DistanceListener {

    Button connectButton, skipFramesButton;
    TextView statusTextView, distanceTextView;
    ImageView im,  disparityImageView;
    ObservedSingleCameraFramesCapturer capturer;
    protected static final int  MY_PERMISSIONS_REQUEST_CAMERA =1;
    protected CameraBridgeViewBase mOpenCvCameraView;
    private boolean isCameraViewDisabledOnClient = true;

    Mat matBuffer;
    public final static String TAG = "serverClientCom";

    int threshVal = 120;
    int gaussVal = 3;
    StereoPhotoParser stereoPhotoParser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_stereo_distance_meter);
        View contentView = findViewById(R.id.server_stereo_xml);
        contentView.setOnTouchListener(this);
        capturer = new ObservedSingleCameraFramesCapturer(this);
        enableWiFi();

        checkPermissions();
        readParametersFromMemory();

        init();
        exqListeners();
    }

    private void readParametersFromMemory() {
        InternalMemoryDataManager dataManager = new InternalMemoryDataManager(getApplicationContext());
        try {
            threshVal = Integer.parseInt(dataManager.read(SavedParametersTags.Thresh));
            gaussVal = Integer.parseInt(dataManager.read(SavedParametersTags.Gauss));
        } catch (InternalMemoryDataManager.SavingException e) {
            e.printStackTrace();
            Toast.makeText(this, "Nie udało się odczytać z pamięci", Toast.LENGTH_SHORT).show();
        }

    }

    private void checkPermissions() {
        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                // Show an explanation to the user *asynchronously* -- don't block
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);
            }
        }
    }


    private void init() {
        connectButton = findViewById(R.id.connect_button);
        skipFramesButton = findViewById(R.id.skip_frames_button);
        statusTextView = findViewById(R.id.connection_status_text_view);
        im = findViewById(R.id.serwer_camera_view);
        Log.d("serverLogs", "DualScreen, init");
        mOpenCvCameraView = findViewById(R.id.self_server_camera_view);
        disparityImageView = findViewById(R.id.disparity_camera_view);
        distanceTextView = findViewById(R.id.distance_text_view);

        matBuffer = new Mat();


        stereoPhotoParser = new StereoPhotoParser(this);
        loadSavedCalibration();
    }

    private void loadSavedCalibration()
    {
        CameraParametersMessager messager = new CameraParametersMessager(getApplicationContext());
        try {
            Mat QMat;
            messager.readQMartix();
            QMat = messager.getQMat();
            stereoPhotoParser.setQMat(QMat);
            Log.d(TAG,QMat.dump());
        } catch (InternalMemoryDataManager.SavingException e) {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void exqListeners() {
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enableWiFi();
                discoverPeers();
            }
        });
        skipFramesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serverClass.sendMsgToClient(ClientServerMessages.SKIP_FRAMES);
            }
        });
    }

    private void initCamera()
    {
        mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK);
        capturer.setCameraOrientation(CameraBridgeViewBase.CAMERA_ID_BACK, getWindowManager().getDefaultDisplay().getRotation());
        mOpenCvCameraView.setMaxFrameSize(1280,720);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(capturer);
    }
    private void enableView()
    {
        if(isCameraViewDisabledOnClient) {
            Log.d("serverLogs", "DualScreen, enable view");
            mOpenCvCameraView.enableView();
            isCameraViewDisabledOnClient = false;
        }
    }

    private void disableView()
    {
        if(!isCameraViewDisabledOnClient) {
            mOpenCvCameraView.disableView();
            isCameraViewDisabledOnClient = true;
        }
    }

    @Override
    public void onWiFiOnListener() {
        super.onWiFiOnListener();
        discoverPeers();
    }

    @Override
    protected void onClientConnected()
    {
        Toast.makeText(this,"Coś poszło nie tak",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onServerConnected()
    {
        statusTextView.setText("host");
        super.onServerConnected();
        initCamera();
        enableView();
    }

    @Override
    protected void onPeersListUpdate(String[] deviceNameArray)
    {
    }



    @Override
    protected void onDiscoverPeersInitiationFailure() {
        statusTextView.setText("Wykrywanie nieudane");
    }

    @Override
    protected void onDiscoverPeersInitiationSuccess() {
        statusTextView.setText("Wykrywanie rozpoczęte");
    }

    @Override
    protected boolean processMessageFromClient(Message msg)
    {
        switch (msg.what) {
            case ServerHandlerMsg.FRAME_MSG: {
                capturer.getSingleFrameToBeProcessed();

                byte[] readBuffer = (byte[]) msg.obj;
                Mat mat = new Mat(msg.arg1, msg.arg2, CvType.CV_8U);
                mat.put(0, 0, readBuffer);

                applyFilters(mat);
                stereoPhotoParser.addClientFrame(mat);
                stereoPhotoParser.drawObjectOnClientFrame(mat);



                Bitmap btm = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(mat, btm);
                im.setImageBitmap(btm);
                matBuffer = mat;
                break;
            }
            case ServerHandlerMsg.CLIENT_READY_MSG:
            {
                Log.d(TAG, "SerwerActivity, wysyłam zapytanie o macierze");
                serverClass.sendMsgToClient(ClientServerMessages.GET_CAMERA_DATA);
                break;
            }
            case ServerHandlerMsg.CAMERA_DATA_RECEIVED_MSG:
            {
                String cameraParameters = (String)msg.obj;
                CameraData cameraData = new CameraData(cameraParameters);
                Log.d(TAG,"ServerActivity, CameraData stworzony: \n" + cameraData.getCameraMatrix() + cameraData.getDistCoeffs());
//                calibrator.setClientCameraParameters(cameraData);
            }
        }
        return true;
    }

    @Override
    protected void onConnectionFail() {
        statusTextView.setText("Rozłączono");
        super.onConnectionFail();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.i("filtry", "wchodzę do requestpermissionresult");
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d("serverLogs", "ReceiveFramesActivity, przyznano prawa!");


                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disableView();
        mOpenCvCameraView = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        disableView();
    }



    @Override
    public void processServerFrame(Mat frame) {
        Log.d("actionEvents", "wysyłam klatki do kalibratora");

        applyFilters(frame);
        stereoPhotoParser.addServerFrame(frame);
        stereoPhotoParser.drawObjectOnServerFrame(frame);
        stereoPhotoParser.findDistanceBetweenObjects();

        Bitmap btm = Bitmap.createBitmap(frame.cols(), frame.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(frame, btm);
        setImage(disparityImageView,btm);

        //calibrator.processFrames(frame,matBuffer);
    }

    @Override
    public void onDistanceCalculated(final double distanceInPixels,final double distanceInLength) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                distanceTextView.setText("pix: "+ distanceInPixels + '\n' + "; length: " + distanceInLength);
            }
        });

    }

    private void applyFilters(Mat frame) {
        List<Filtr> filters = new ArrayList<>();
        if(frame.channels() > 1) filters.add(new GrayFiltr());
        filters.add(new GBlurFiltr(gaussVal));
        filters.add(new BinaryThreshFiltr(threshVal));
        filters.add(new CannyFiltr());


        for(Filtr f : filters)
        {
            f.filtr(frame);
        }
    }

    private void setImage(final ImageView image,final Bitmap btm){
            runOnUiThread(new Runnable() {
            @Override
            public void run() {
                image.setImageBitmap(btm);
            }
        });
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}
