package com.example.stereovisioncarsystem;


import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
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
import android.widget.Toast;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraRenderer;
import org.opencv.android.JavaCameraView;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.net.InetAddress;


public class ReceiveFramesActivity extends CommunicationBasicActivity implements CameraFramesCapturer.CameraFrameConnector {

    protected static final String  TAG = "ReceiveFramesActivity";
    protected static final int  MY_PERMISSIONS_REQUEST_CAMERA =1;
    protected CameraBridgeViewBase mOpenCvCameraView;
    Button btnDiscoverPeers, btnConnect, btnStartCapturing;
    TextView twConnectionStatus;
    Spinner spinnerPeers;
    boolean flag = false;


    ServerReceiver serverClass;
    ClientSender clientClass;
    InetAddress groupOwnerAdress;
    WifiP2pDevice device;

    private boolean peerEstablished = false;
    private boolean isClient = false;
    CameraFramesCapturer capturer;

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
        if(clientClass.isSocketAlive())
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
    
    public static final int MESSAGE_READ = 1;
    
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
    protected Handler createMessageReceivedHandler() {
        return new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                //Log.d("serverLogs", "Jestem w handlerze"+msg.toString());
                switch (msg.what) {
                    case MESSAGE_READ:

                        byte[] readBuffer = (byte[]) msg.obj;

                        Mat mat = new Mat(240,320,0);
                        mat.put(0,0,readBuffer);
                        Bitmap btm = Bitmap.createBitmap(mat.cols(), mat.rows(),Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(mat,btm);

                        //SurfaceView jcv = findViewById(R.id.hostSurface);
                        //jcv.setZOrderOnTop(true);
                        //Canvas canvas = new Canvas(btm);
                        ImageView im = findViewById(R.id.hostSurface);
                        im.setImageBitmap(btm);


                        //canvas.drawBitmap(btm,0, 0,new Paint());
                        //canvas.drawColor(Color.BLUE);
                        //jcv.draw(canvas);
                        //String tempMsg = new String(readBuffer, 0, msg.arg1);
                        //Log.d("serverLogs", "otrzymano: " + tempMsg);
                        //readMsgBox.setText(tempMsg);
                        break;
                }
                return true;
            }
        });
    }

    @Override
    protected WifiP2pManager.ConnectionInfoListener createConnectionInfoListener() {
        return new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
                //final InetAddress groupOwnerAdress = wifiP2pInfo.groupOwnerAddress;
                groupOwnerAdress = wifiP2pInfo.groupOwnerAddress;


                if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
                    Log.i("serverLogs", "ConnectionListener; Połaczony jako host, tworzę nowy serwer");

                    twConnectionStatus.setText("host");
                    serverClass = new ServerReceiver(messageHandler);
                    peerEstablished = true;
                    serverClass.start();

                    Log.i("serverLogs", "ConnectionListener; Nowy serwer stworzony " + serverClass.isAlive());
                } else if (wifiP2pInfo.groupFormed) {
                    twConnectionStatus.setText("client");
                    Log.i("serverLogs", "ConnectionListener; Połaczony jako klient, tworzę nowy wątek klienta");

                    clientClass = new ClientSender(groupOwnerAdress);
                    clientClass.start();
                    peerEstablished = true;
                    isClient = true;

                    Log.i("serverLogs", "ConnectionListener; Stworzyłem obiekt klienta; status: ");
                }
            }
        };
    }

    @Override
    protected WifiP2pManager.PeerListListener createPeerListListener() {
        return new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peerList) {

                if (!arePeersUpdate(peerList)) {

                    updatePeersArray(peerList);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_spinner_item, deviceNameArray);
                    spinnerPeers.setAdapter(adapter);
                }
                if (isPeerListEmpty()) {
                    Toast.makeText(getApplicationContext(), "Nie ma żadnych peerów", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        };
    }

    @Override
    protected WifiP2pManager.ActionListener createDiscoverPeersActionListener() {
        return new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                twConnectionStatus.setText("Wykrywanie rozpoczęte");
            }

            @Override
            public void onFailure(int i) {
                twConnectionStatus.setText("Wykrywanie nieudane");
            }
        };
    }

    @Override
    protected WifiP2pManager.ActionListener createConnectActionListener() {
        return new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int i) {
                Toast.makeText(getApplicationContext(), "Not connected", Toast.LENGTH_SHORT).show();
                return;
            }
        };
    }

    @Override
    protected void onConnectionFail() {

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
                wifiOn();
                discoverPeers();
            }
        });

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                device = deviceArray[(int)spinnerPeers.getSelectedItemId()];
                connectToPeer(device);
            }
        });

        btnStartCapturing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("serverLogs", "ReceiveFramesActivity, kliknąłem przycisk");

                mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
                mOpenCvCameraView.setCameraIndex(1);
                mOpenCvCameraView.setCvCameraViewListener(capturer);
                //mOpenCvCameraView.setAlpha(0);
                mOpenCvCameraView.setMaxFrameSize(320,640);
                mOpenCvCameraView.enableView();
            }
        });


    }





    private void wifiOn()
    {
        if(wifiNotEnabled())
        {
            wifiManager.setWifiEnabled(true);
        }
    }

    boolean wifiNotEnabled()
    {
        return wifiManager.isWifiEnabled() ? false : true;
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

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

}

/*
    <SurfaceView
        android:layout_below="@id/start_capturing_button"
        android:id="@+id/surfaceView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_margin="25dp" />


 */