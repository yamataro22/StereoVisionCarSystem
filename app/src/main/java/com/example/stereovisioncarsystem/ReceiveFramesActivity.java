package com.example.stereovisioncarsystem;


import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Message;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.Utils;
import org.opencv.core.Mat;


public class ReceiveFramesActivity extends CommunicationBasicActivity implements CameraFramesCapturer.CameraFrameConnector {

    protected static final int  MY_PERMISSIONS_REQUEST_CAMERA =1;
    public static final int MESSAGE_READ = 1;

    protected CameraBridgeViewBase mOpenCvCameraView;
    Button btnDiscoverPeers, btnConnect, btnStartCapturing;
    TextView twConnectionStatus;
    Spinner spinnerPeers;

    private boolean isCameraViewDisabledOnClient = false;
    CameraFramesCapturer capturer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_frames);

        initialWork();
        exqListener();

        Log.d("serverLogs", "ReceiveFramesActivity, sprawdzam permissiony");

        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                // Show an explanation to the user *asynchronously* -- don't block
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);
            }
        }
        capturer = new CameraFramesCapturer(this);
    }

    private void initialWork()
    {
        btnDiscoverPeers = findViewById(R.id.button_discover_peers);
        btnConnect = findViewById(R.id.connect_button);
        btnStartCapturing = findViewById(R.id.start_capturing_button);
        twConnectionStatus = findViewById(R.id.connection_status_text_view);
        spinnerPeers = findViewById(R.id.spinner);
        mOpenCvCameraView = findViewById(R.id.InvisibleOpenCvView);
    }

    private void exqListener()
    {

        btnDiscoverPeers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enableWiFi();
                discoverPeers();
            }
        });

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = (int)spinnerPeers.getSelectedItemId();
                connectToPeer(getDeviceByIndexAndUpdate(id));
            }
        });

        btnStartCapturing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("serverLogs", "ReceiveFramesActivity, kliknąłem przycisk");

                mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
                mOpenCvCameraView.setCameraIndex(1);
                mOpenCvCameraView.setCvCameraViewListener(capturer);
                mOpenCvCameraView.setMaxFrameSize(320,640);
                mOpenCvCameraView.enableView();
            }
        });


    }

    private void checkClientStatusAndSendMessage(Object message)
    {
        try {
            sendMessageToServer(message);
        } catch (NullClientException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageToServer(Object message) throws NullClientException
    {
        if(clientClass == null) throw new NullClientException();
        if(clientClass.clientMsgHandler == null) throw new NullClientException();

        Log.d("serverLogs", "Wysyłam wiadomość");
        Message msg = clientClass.clientMsgHandler.obtainMessage(0, message);
        clientClass.clientMsgHandler.sendMessage(msg);
    }

    public byte[] mat2Byte(Mat img)
    {
        int total_bytes = img.cols()*img.rows();
        Log.d("serverLogs", "Wysyłam wiadomość długości: " + total_bytes);
        Log.d("serverLogs", "rows: " + img.rows());
        Log.d("serverLogs", "cols: " + img.cols());
        Log.d("serverLogs", "typ: " + img.type());
        byte[] returnByte = new byte[total_bytes];
        img.get(0,0,returnByte);
        return returnByte;

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
        twConnectionStatus.setText("client");
        super.onClientConnected();
    }

    @Override
    protected void onServerConnected() {
        twConnectionStatus.setText("host");
        super.onServerConnected();
    }

    @Override
    protected void onPeersListUpdate(String[] deviceNameArray) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_spinner_item, deviceNameArray);
        spinnerPeers.setAdapter(adapter);
    }

    @Override
    protected void onDiscoverPeersInitiationFailure() {
        twConnectionStatus.setText("Wykrywanie nieudane");
    }

    @Override
    protected void onDiscoverPeersInitiationSuccess() {
        twConnectionStatus.setText("Wykrywanie rozpoczęte");
    }

    @Override
    protected boolean processMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_READ:

                byte[] readBuffer = (byte[]) msg.obj;

                Mat mat = new Mat(240,320,0);
                mat.put(0,0,readBuffer);
                Bitmap btm = Bitmap.createBitmap(mat.cols(), mat.rows(),Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(mat,btm);

                ImageView im = findViewById(R.id.hostSurface);
                im.setImageBitmap(btm);
        }
        return true;
    }

    @Override
    protected void onConnectionFail()
    {
        twConnectionStatus.setText("Rozłączono");
        disableCameraViewOnClient();
        super.onConnectionFail();
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

    private void disableCameraViewOnClient() {
        if (clientClass != null) {
            mOpenCvCameraView.disableView();
            isCameraViewDisabledOnClient = true;
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




