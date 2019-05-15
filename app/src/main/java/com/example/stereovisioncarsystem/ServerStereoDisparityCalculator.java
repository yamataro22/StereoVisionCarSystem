package com.example.stereovisioncarsystem;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
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
import com.example.stereovisioncarsystem.ServerClientCommunication.ClientServerMessages;
import com.example.stereovisioncarsystem.ServerClientCommunication.ServerHandlerMsg;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class ServerStereoDisparityCalculator extends CommunicationBasicActivity implements View.OnTouchListener, ObservedCameraFramesCapturer.CameraFrameConnector
        , DisparityPhotoParser.DistanceListener {

    private Button connectButton, skipFramesButton;
    private TextView statusTextView, distanceTextView;
    private ImageView im,  disparityImageView;
    private ObservedSingleCameraFramesCapturer capturer;
    protected static final int  MY_PERMISSIONS_REQUEST_CAMERA =1;
    protected CameraBridgeViewBase mOpenCvCameraView;
    private boolean isCameraViewDisabledOnClient = true;

    Mat matBuffer;
    public final static String TAG = "serverClientCom";

    private DisparityPhotoParser disparityPhotoParser;
    private CameraData serverCameraData, clientCameraData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_stereo_distance_meter);
        View contentView = findViewById(R.id.server_stereo_xml);
        contentView.setOnTouchListener(this);
        capturer = new ObservedSingleCameraFramesCapturer(this);
        enableWiFi();

        checkPermissions();
        init();
        initParser();
        exqListeners();

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
        mOpenCvCameraView = findViewById(R.id.self_server_camera_view);
        disparityImageView = findViewById(R.id.disparity_camera_view);
        distanceTextView = findViewById(R.id.distance_text_view);

        matBuffer = new Mat();


    }

    private void initParser() {
        disparityPhotoParser = new DisparityPhotoParser(this);
        loadSavedCalibration();
        loadFilterParameters();
        loadCameraData();
    }

    private void loadCameraData() {
        CameraParametersMessager cameraMessanger = new CameraParametersMessager(getApplicationContext());
        try {
            cameraMessanger.readServerParams();
            cameraMessanger.readClientParams();

            serverCameraData = cameraMessanger.getCameraData();
            clientCameraData = cameraMessanger.getClientCameraData();
            Log.d("clientData", clientCameraData.getFormattedCameraMatrix()+"\n"+clientCameraData.getFromatedDiffParams());
            disparityPhotoParser.setCameraData(clientCameraData,serverCameraData);
        } catch (InternalMemoryDataManager.SavingException e) {
            e.printStackTrace();
        }
    }

    private void loadFilterParameters() {
        int threshVal  = 120;
        int gaussVal = 3;
        boolean isThreshInverted = false;

        InternalMemoryDataManager dataManager = new InternalMemoryDataManager(getApplicationContext());
        try {
            threshVal = Integer.parseInt(dataManager.read(SavedParametersTags.Thresh));
            gaussVal = Integer.parseInt(dataManager.read(SavedParametersTags.Gauss));
            isThreshInverted = Boolean.parseBoolean(dataManager.read(SavedParametersTags.IsThreshInverted));
        } catch (InternalMemoryDataManager.SavingException e) {
            e.printStackTrace();
            Toast.makeText(this, "Nie udało się odczytać z pamięci", Toast.LENGTH_SHORT).show();
        }
        disparityPhotoParser.setFilterParams(threshVal,gaussVal,isThreshInverted);
    }

    private void loadSavedCalibration()
    {
        loadCameraParameters();
        loadContourFilterParameters();
    }

    private void loadContourFilterParameters() {
        InternalMemoryDataManager messanger = new InternalMemoryDataManager(getApplicationContext());
        try {
            int minArea = messanger.readInt(SavedParametersTags.minArea);
            int maxArea = messanger.readInt(SavedParametersTags.maxArea);
            double minRatio = messanger.readDouble(SavedParametersTags.minRatio);
            double maxRatio = messanger.readDouble(SavedParametersTags.maxRatio);
            disparityPhotoParser.setConstrins(minArea,maxArea,minRatio,maxRatio);
        } catch (InternalMemoryDataManager.SavingException e) {
            e.printStackTrace();
        }

    }

    private void loadCameraParameters() {
        Mat R1, R2, P1, P2;

        CameraParametersMessager messager = new CameraParametersMessager(getApplicationContext());
        try {
            Mat QMat;
            messager.readQMartix();
            QMat = messager.getQMat();

            R1 = messager.readStereoRMatrix(SavedParametersTags.R1);
            R2 = messager.readStereoRMatrix(SavedParametersTags.R2);
            P1 = messager.readStereoPMatrix(SavedParametersTags.T1);
            P2 = messager.readStereoPMatrix(SavedParametersTags.T2);


            disparityPhotoParser.setQMat(QMat);
            disparityPhotoParser.setRectificationParams(R1, R2, P1, P2);
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


                disparityPhotoParser.addClientFrame(mat);
                disparityPhotoParser.drawObjectOnClientFrame(mat);

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
        disparityPhotoParser.addServerFrame(frame);
        disparityPhotoParser.drawObjectOnServerFrame(frame);
        disparityPhotoParser.findDistanceBetweenObjects();

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

    @Override
    public void onDisparityCalculated(Mat disparityMap, Mat disparity8) {

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
}
