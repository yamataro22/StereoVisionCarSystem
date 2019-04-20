package com.example.stereovisioncarsystem;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.util.LogPrinter;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stereovisioncarsystem.CameraCapturers.CameraFramesCapturer;
import com.example.stereovisioncarsystem.CameraCapturers.ObservedCameraFramesCapturer;
import com.example.stereovisioncarsystem.CameraCapturers.ObservedSingleCameraFramesCapturer;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class ServerDualCameraCalibrationActivity extends CommunicationBasicActivity implements View.OnTouchListener, ObservedCameraFramesCapturer.CameraFrameConnector {

    Button connectButton, skipFramesButton;
    TextView statusTextView;
    ImageView im;
    public static final int MESSAGE_READ = 1;
    ObservedSingleCameraFramesCapturer capturer;
    protected static final int  MY_PERMISSIONS_REQUEST_CAMERA =1;
    protected CameraBridgeViewBase mOpenCvCameraView;
    private boolean isCameraViewDisabledOnClient = true;
    private DualCameraCalibrator calibrator;
    Mat matBuffer;

    public final static int SPECIAL_MESSAGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_dual_camera_calibration);
        View contentView = findViewById(R.id.dual_calibration_whole);
        contentView.setOnTouchListener(this);
        capturer = new ObservedSingleCameraFramesCapturer(this);
        enableWiFi();

        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                // Show an explanation to the user *asynchronously* -- don't block
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);
            }
        }
        Log.d("serverLogs", "DualScreen, orientation: " + getResources().getConfiguration().orientation);
        Log.d("serverLogs", "DualScreen, rotetion: " + getWindowManager().getDefaultDisplay().getRotation());

        init();

        exqListeners();
    }



    private void init() {
        connectButton = findViewById(R.id.connect_button);
        skipFramesButton = findViewById(R.id.skip_frames_button);
        statusTextView = findViewById(R.id.connection_status_text_view);
        im = findViewById(R.id.serwer_camera_view);
        Log.d("serverLogs", "DualScreen, init");
        mOpenCvCameraView = findViewById(R.id.self_server_camera_view);

        matBuffer = new Mat();
        calibrator = new DualCameraCalibrator();
        loadSavedCalibration();

    }

    private void loadSavedCalibration()
    {
        CameraParametersMessager messager = new CameraParametersMessager(getApplicationContext(),CameraFacing.Back);
        try {
            messager.read();
            calibrator.setServerCameraParameters(messager.getCameraMatrixMat(),messager.getDistCoeffsMat());
        } catch (CameraParametersMessager.SavingException e) {
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
                serverClass.sendMsgToClient("x");
            }
        });
    }

    private void initCamera()
    {
        mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK);
        capturer.setCameraOrientation(CameraBridgeViewBase.CAMERA_ID_BACK, getWindowManager().getDefaultDisplay().getRotation());
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
    protected boolean processMessage(Message msg)
    {
        messageHandler.dump(new LogPrinter(Log.DEBUG, "HandlerDump"), "");
        switch (msg.what) {
            case MESSAGE_READ: {
                byte[] readBuffer = (byte[]) msg.obj;

                Mat mat = new Mat(msg.arg1, msg.arg2, CvType.CV_8U);
                mat.put(0, 0, readBuffer);
                Bitmap btm = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(mat, btm);
                im.setImageBitmap(btm);
                matBuffer = mat;
                break;
            }
            case SPECIAL_MESSAGE:
            {
                String message = (String)msg.obj;
                Log.d("receiveTask", "SerwerClass, wysyłam zapytanie o macierze, msg.obj="+message);
                if(message.equals("r"))
                {
                    serverClass.sendMsgToClient("y");
                }
                else
                {
                    Log.d("receiveTask", "SerwerClass, Finito, otrzymano macierz");
                }
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
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if(motionEvent.getAction() == MotionEvent.ACTION_DOWN)
        {
            Log.d("actionEvents", "wysyłam wiadomośc do klienta");
            capturer.getSingleFrameToBeProcessed();
        }

        return true;
    }

    @Override
    public void sendFrame(Mat frame) {
        Log.d("actionEvents", "wysyłam klatki do kalibratora");
        calibrator.processFrames(frame,matBuffer);
    }
}
