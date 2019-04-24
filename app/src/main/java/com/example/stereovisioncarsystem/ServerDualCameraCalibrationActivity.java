package com.example.stereovisioncarsystem;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
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

public class ServerDualCameraCalibrationActivity extends CommunicationBasicActivity implements  ObservedCameraFramesCapturer.CameraFrameConnector, DualCameraCalibrator.OnStereoCalibrationresult {

    Button connectButton, skipFramesButton, saveButton;
    TextView connectionStatusTextView, calibrationStatusTextView, qMartixTextView;
    ImageView im,  disparityImageView;
    ObservedSingleCameraFramesCapturer capturer;
    protected static final int  MY_PERMISSIONS_REQUEST_CAMERA =1;
    protected CameraBridgeViewBase mOpenCvCameraView;
    private boolean isCameraViewDisabledOnClient = true;
    private DualCameraCalibrator calibrator;
    Mat matBuffer;

    public final static String TAG = "serverClientCom";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_dual_camera_calibration);
        capturer = new ObservedSingleCameraFramesCapturer(this);

        enableWiFi();

        checkPermissions();
        init();
        exqListeners();
    }


    private void init() {
        connectButton = findViewById(R.id.connect_button);
        connectButton.setEnabled(false);
        skipFramesButton = findViewById(R.id.skip_frames_button);
        connectionStatusTextView = findViewById(R.id.connection_status_text_view);
        im = findViewById(R.id.serwer_camera_view);

        mOpenCvCameraView = findViewById(R.id.self_server_camera_view);
        disparityImageView = findViewById(R.id.disparity_camera_view);
        calibrationStatusTextView = findViewById(R.id.calib_status);
        qMartixTextView = findViewById(R.id.qMartixTextView);
        saveButton = findViewById(R.id.save_button);

        matBuffer = new Mat();
        initCalibrator();
    }

    private void initCalibrator() {
        calibrator = new DualCameraCalibrator(this);

        int framesQuantity = loadFramesQuantity();
        calibrationStatusTextView.setText(framesQuantity+"");
        calibrator.setFramesQuantity(framesQuantity);
        loadSavedCalibration();
    }

    private int loadFramesQuantity() {
        InternalMemoryDataManager dataManager = new InternalMemoryDataManager(getApplicationContext());
        try {
            String framesQuantity = dataManager.read(SavedParametersTags.NbOfStereoCalibrationFrames);
            return Tools.getIntFromString(framesQuantity);
        } catch (InternalMemoryDataManager.SavingException e) {
            e.printStackTrace();
            Tools.makeToast(getApplicationContext(),"Reading failed");
            return 5;
        }
    }

    private void exqListeners() {
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if(calibrator.isCalibrated)
//                {
//
//                    Mat mat = calibrator.showDisparity();
//                    Bitmap btm = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
//                    Utils.matToBitmap(mat, btm);
//                    disparityImageView.setImageBitmap(btm);
//                }
                Log.d("actionEvents", "wysyłam wiadomośc do klienta");
                capturer.getSingleFrameToBeProcessed();
            }
        });
        skipFramesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serverClass.sendMsgToClient(ClientServerMessages.SKIP_FRAMES);
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InternalMemoryDataManager dataManager = new InternalMemoryDataManager(getApplicationContext());
                try {
                    Mat QMartix = calibrator.getQMartix();
                    Log.d(TAG, "zapisuję: " + QMartix.dump());
                    dataManager.save(SavedParametersTags.QMatrix,QMartix.dump());
                    Tools.makeToast(getApplicationContext(), "zapisano:)");
                } catch (InternalMemoryDataManager.SavingException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void loadSavedCalibration()
    {
        CameraParametersMessager messager = new CameraParametersMessager(getApplicationContext(),CameraFacing.Back);
        try {
            messager.read();
            calibrator.setServerCameraParameters(messager.getCameraData());
        } catch (InternalMemoryDataManager.SavingException e) {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
        }
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
            Log.d("dualCalibration", "wyłączam kamerkę");
            mOpenCvCameraView.disableView();
            isCameraViewDisabledOnClient = true;
        }
    }

    @Override
    protected boolean processMessageFromClient(Message msg)
    {
        switch (msg.what) {
            case ServerHandlerMsg.FRAME_MSG: {
                byte[] readBuffer = (byte[]) msg.obj;

                Mat mat = new Mat(msg.arg1, msg.arg2, CvType.CV_8U);
                mat.put(0, 0, readBuffer);
                Bitmap btm = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(mat, btm);
                im.setImageBitmap(btm);
                matBuffer = mat;
                break;
            }
            case ServerHandlerMsg.CLIENT_READY_MSG:
            {
                Log.d(TAG, "SerwerActivity, wysyłam zapytanie o macierze");
                connectButton.setEnabled(true);
                serverClass.sendMsgToClient(ClientServerMessages.GET_CAMERA_DATA);
                break;
            }
            case ServerHandlerMsg.CAMERA_DATA_RECEIVED_MSG:
            {
                String cameraParameters = (String)msg.obj;
                CameraData cameraData = new CameraData(cameraParameters);
                calibrator.setClientCameraParameters(cameraData);
            }
        }
        return true;
    }

    @Override
    public void processServerFrame(final Mat frame) {
        Log.d("actionEvents", "wysyłam klatki do kalibratora");
        try {
            calibrator.processFrames(frame,matBuffer);
        } catch (Calibrator.NotEnoughChessboardsException e) {
            e.printStackTrace();
            ////Tools.makeToast(getApplicationContext(),"Not enough chessboards");
        } catch (Calibrator.ChessboardsNotOnAllPhotosException e) {
            e.printStackTrace();
            //Tools.makeToast(getApplicationContext(),"Chessboards not on all photos");
        }finally {
            final int framesRemaining = calibrator.getRemainingFrames();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    calibrationStatusTextView.setText(framesRemaining+"");
                }
            });
        }
    }

    @Override
    public void onCameraResulat(final Mat qMatrix, Mat R1, Mat R2, Mat P1, Mat P2)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                qMartixTextView.setVisibility(View.VISIBLE);
                saveButton.setVisibility(View.VISIBLE);
                qMartixTextView.setText("Success!" + '\n' + "QMartix:" + '\n' + qMatrix.dump());
            }
        });


    }

    @Override
    public void onCalibrationStart() {
        Log.d("dualCalibration", "oncalibrationStart, wyłączam kamerkę");
        //disableView();
        //serverClass.sendMsgToClient(ClientServerMessages.CONNECTION_FINISHED);
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
        connectionStatusTextView.setText("host");
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
        connectionStatusTextView.setText("Wykrywanie nieudane");
    }

    @Override
    protected void onDiscoverPeersInitiationSuccess() {
        connectionStatusTextView.setText("Wykrywanie rozpoczęte");
    }

    @Override
    protected void onConnectionFail() {
        connectionStatusTextView.setText("Rozłączono");
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
