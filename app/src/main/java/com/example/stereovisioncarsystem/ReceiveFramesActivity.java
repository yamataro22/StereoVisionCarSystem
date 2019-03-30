package com.example.stereovisioncarsystem;


import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Message;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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


    ServerReceiver serverClass;
    ClientSender clientClass;

    private boolean isDisabled = false;
    boolean isServerCreated = false;


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
    @Override
    public void sendFrame(Mat frame) {
        Log.d("serverLogs", "Otrzymano klatkę");
        checkClientStatusAndSendMessage(mat2Byte(frame));
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

    private void checkClientStatusAndSendMessage(Object message)
    {
        if(clientClass!=null)
        {
            sendMessageToServer(message);
        }
    }



    private void sendMessageToServer(Object message)
    {
        if (clientClass.clientMsgHandler != null) {
            Message msg = clientClass.clientMsgHandler.obtainMessage(0, message);
            Log.d("serverLogs", "Wysyłam wiadomość");
            clientClass.clientMsgHandler.sendMessage(msg);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isDisabled) {
            Log.i("serverLogs", "ConnectionListener; onResume, włączam widok");
            mOpenCvCameraView.enableView();
            isDisabled = false;
        }
    }

    @Override
    protected void onClientConnected() {
        twConnectionStatus.setText("client");
        Log.i("serverLogs", "ConnectionListener; Połaczony jako klient, tworzę nowy wątek klienta");

        clientClass = new ClientSender(groupOwnerAdress);
        clientClass.start();

        Log.i("serverLogs", "ConnectionListener; Stworzyłem obiekt klienta; status: ");
    }

    @Override
    protected void onServerConnected() {
        Log.i("serverLogs", "ConnectionListener; Połaczony jako host, tworzę nowy serwer");
        if(!isServerCreated)
        {
            twConnectionStatus.setText("host");
            serverClass = new ServerReceiver(messageHandler);
            serverClass.start();
        }
        else
        {
            Log.i("serverLogs", "ConnectionListener; Serwer był już stworzony");
        }


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

        if(clientClass!=null) {
            Log.d("serverLogs", "On Pause; Staram się usunąć klienta");
            if(clientClass!=null)
            {
                clientClass.clear();
                clientClass = null;
            }
        }
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
    protected void onPause() {
        super.onPause();
        if(clientClass != null)
        {
            Log.d("serverLogs", "On Pause; Staram się usunąć klienta");
            mOpenCvCameraView.disableView();
            isDisabled = true;
            sendBreakMessageToServer();
            SystemClock.sleep(40);
            clientClass.clear();
            clientClass = null;
        }

    }

    private void sendBreakMessageToServer()
    {
        if (clientClass.clientMsgHandler != null) {
            Log.d("serverLogs", "ReceiveFramesActivity; SendEmptyMessage; Handler różny od nulla");
            Message msg = clientClass.clientMsgHandler.obtainMessage(1, "b");
            Log.d("serverLogs", "ReceiveFramesActivity; SendEmptyMessage; Wysyłam żeby zakończyć komuniakcję");
            clientClass.clientMsgHandler.sendMessage(msg);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}

