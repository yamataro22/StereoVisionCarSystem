package com.example.stereovisioncarsystem;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

public class ServerDualCameraActivity extends CommunicationBasicActivity implements CameraFramesCapturer.CameraFrameConnector{

    Button connectButton, disconnectButton;
    TextView statusTextView;
    ImageView im;
    public static final int MESSAGE_READ = 1;
    BasicCapturer capturer;
    protected static final int  MY_PERMISSIONS_REQUEST_CAMERA =1;
    protected CameraBridgeViewBase mOpenCvCameraView;
    private boolean isCameraViewDisabledOnClient = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_dual_camera);
        enableWiFi();
        init();
        exqListeners();

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
        capturer = new BasicCapturer();
    }

    private void init() {
        connectButton = findViewById(R.id.connect_button);
        disconnectButton = findViewById(R.id.disconnect_button);
        statusTextView = findViewById(R.id.connection_status_text_view);
        im = findViewById(R.id.serwer_camera_view);
        mOpenCvCameraView = findViewById(R.id.camera_view);
        initCamera();
    }

    private void exqListeners() {
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enableWiFi();
                discoverPeers();
            }
        });
    }

    private void initCamera()
    {
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCameraIndex(1);
        mOpenCvCameraView.setCvCameraViewListener(capturer);
        mOpenCvCameraView.setMaxFrameSize(1500,1500);
    }
    private void enableView()
    {
        if(isCameraViewDisabledOnClient) {
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
        enableView();
        super.onServerConnected();
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
    protected boolean processMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_READ:

                byte[] readBuffer = (byte[]) msg.obj;

                Mat mat = new Mat(240,320,0);
                mat.put(0,0,readBuffer);
                Bitmap btm = Bitmap.createBitmap(mat.cols(), mat.rows(),Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(mat,btm);
                im.setImageBitmap(btm);
        }
        return true;
    }

    @Override
    protected void onConnectionFail() {
        statusTextView.setText("Rozłączono");
        super.onConnectionFail();

    }

    @Override
    public void sendFrame(Mat frame) {

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
