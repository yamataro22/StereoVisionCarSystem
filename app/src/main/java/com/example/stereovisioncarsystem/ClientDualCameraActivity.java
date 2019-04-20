package com.example.stereovisioncarsystem;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.stereovisioncarsystem.CameraCapturers.ObservedRotatedCameraFramesCapturer;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

public class ClientDualCameraActivity extends CommunicationBasicActivity implements ObservedRotatedCameraFramesCapturer.CameraFrameConnector {
    Button connectButton, disconnectButton, startCapturingButton;
    TextView statusTextView;
    final String clientDeviceName = "OnePlus 6T";
    private boolean isConnected = false;
    protected static final int  MY_PERMISSIONS_REQUEST_CAMERA =1;

    private boolean isCameraViewDisabledOnClient = false;
    protected ObservedRotatedCameraFramesCapturer capturer;
    protected CameraBridgeViewBase mOpenCvCameraView;
    CameraData cameraData;

    public final static String TAG = "serverClientCom";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_dual_camera);

        enableWiFi();
        init();
        exqListeners();
        checkPermissions();

        getSavedCameraMatrix();
    }

    private void getSavedCameraMatrix()
    {
        CameraParametersMessager messager = new CameraParametersMessager(getApplicationContext(),CameraFacing.Back);
        try {
            messager.read();
            cameraData = messager.getCameraData();
            Log.d("camMatrixSender", "odczytano macierz: " + cameraData);
        } catch (CameraParametersMessager.SavingException e) {
            Log.d("camMatrixSender", "wyjątek przy odczytywaniu :(");
            e.printStackTrace();
        }
    }

    private void checkPermissions()
    {
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
        capturer = new ObservedRotatedCameraFramesCapturer(this);

        connectButton = findViewById(R.id.connect_button);
        disconnectButton = findViewById(R.id.disconnect_button);
        statusTextView = findViewById(R.id.connection_status_text_view);
        startCapturingButton = findViewById(R.id.start_capturing_button);
        mOpenCvCameraView = findViewById(R.id.camera_view);
    }

    private void exqListeners() {
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enableWiFi();
                discoverPeers();
            }
        });

        startCapturingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
                mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK);
                capturer.setCameraOrientation(CameraBridgeViewBase.CAMERA_ID_BACK, getWindowManager().getDefaultDisplay().getRotation());
                mOpenCvCameraView.setCvCameraViewListener(capturer);
                mOpenCvCameraView.enableView();
            }
        });
    }

    private void checkClientStatusAndSendMessage(Object message)
    {
        try {
            sendMessageToServer(message);
        } catch (ClientDualCameraActivity.NullClientException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageToServer(Object message) throws ClientDualCameraActivity.NullClientException
    {
        if(clientClass == null) throw new ClientDualCameraActivity.NullClientException();
        if(clientClass.clientMsgHandler == null) throw new ClientDualCameraActivity.NullClientException();

        Log.i(TAG, "Wysyłam wiadomość do serwera");
        FrameParameters params = (FrameParameters) message;
        Message msg = clientClass.clientMsgHandler.obtainMessage(ClientHandlerMsg.FRAME_MSG, params.rows,params.cols,params.bytes);
        clientClass.clientMsgHandler.sendMessage(msg);
    }

    public FrameParameters mat2Byte(Mat img)
    {
        int total_bytes = img.cols()*img.rows();
        Log.d("serverLogs", "Wysyłam wiadomość długości: " + total_bytes);
        Log.d("serverLogs", "rows: " + img.rows());
        Log.d("serverLogs", "cols: " + img.cols());
        Log.d("serverLogs", "typ: " + img.type());
        byte[] returnByte = new byte[total_bytes];
        img.get(0,0,returnByte);
        return new FrameParameters(returnByte, img.rows(), img.cols());
    }

    private class FrameParameters
    {
        public FrameParameters(byte[] bytes, int rows, int cols) {
            this.bytes = bytes;
            this.rows = rows;
            this.cols = cols;
        }

        public byte[] bytes;
        public int rows;
        public int cols;
    }

    @Override
    public void sendFrame(Mat frame) {
        Log.d("serverLogs", "Otrzymano klatkę");
        checkClientStatusAndSendMessage(mat2Byte(frame));
    }


    private void enableCameraViewOnClient() {
        if(isCameraViewDisabledOnClient) {
            Log.i("serverLogs", "ConnectionListener; onResume, włączam widok");
            mOpenCvCameraView.enableView();
            isCameraViewDisabledOnClient = false;
        }
    }

    @Override
    protected void onClientConnected() {
        statusTextView.setText("client");
        isConnected = true;
        Log.i(TAG, "MainActiviy, jestem przed wywołaniem super");
        super.onClientConnected();
        Log.i(TAG, "MainActiviy, jestem po wywołaniu super");
        clientClass.setCameraData(cameraData);

        SystemClock.sleep(400);
        clientClass.sendReadyMessageToHandler();

    }

    @Override
    public void onWiFiOnListener() {
        super.onWiFiOnListener();
        discoverPeers();
        Log.d("serverLogs","Próbuję wyszukać peery");
    }

    @Override
    protected void onPeersListUpdate(String[] deviceNameArray)
    {
        Log.d("serverLogs","On peer list update");
        if(isConnected) return;
        for (String device:deviceNameArray) {
            Log.d("serverLogs","Znaleziono: " + device);
        }
        if(isClientInDeviceArray())
        {
            Log.d("serverLogs","Client is in device array");
            WifiP2pDevice device = getDeviceByName();
            connectToPeer(device);
        }
    }

    private WifiP2pDevice getDeviceByName() {

        for (WifiP2pDevice device :getDeviceArray()
        ) {
            if(device.deviceName.equals(clientDeviceName))
            {
                Log.d("serverLogs","Found device: " + device.deviceName);
                return device;
            }

        }
        return null;
    }

    private boolean isClientInDeviceArray() {

        for (WifiP2pDevice device:getDeviceArray()) {
            if(device.deviceName.equals(clientDeviceName))
            {
                return true;
            }

        }
        return false;
    }

    @Override
    protected void onDiscoverPeersInitiationFailure() {
        statusTextView.setText("Failed");
    }

    @Override
    protected void onDiscoverPeersInitiationSuccess() {
        statusTextView.setText("Searching");
    }

    @Override
    protected boolean processMessage(Message msg) {
        return false;
    }

    @Override
    protected void onConnectionFail() {
        statusTextView.setText("DC'ed");
        isConnected = false;
        disableCameraViewOnClient();
        super.onConnectionFail();

    }
    private void disableCameraViewOnClient() {
        if (clientClass != null) {
            mOpenCvCameraView.disableView();
            isCameraViewDisabledOnClient = true;
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

    private class NullClientException extends Exception
    {

        @Override
        public String toString() {
            return "Null client exception:(";
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        enableCameraViewOnClient();
    }

    @Override
    protected void onPause() {
        disableCameraViewOnClient();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        disableCameraViewOnClient();
        super.onDestroy();
    }
}
