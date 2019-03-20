package com.example.stereovisioncarsystem;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class ReceiveFramesActivity extends AppCompatActivity {



    Button btnDiscoverPeers, btnConnect, btnStartCapturing;
    TextView twConnectionStatus;
    Spinner spinnerPeers;

    WifiManager wifiManager;
    WifiP2pManager wifiP2pManager;
    WifiP2pManager.Channel p2pChannel;

    BroadcastReceiver broadcastReceiver;
    IntentFilter intentFilter;

    List<WifiP2pDevice> peers = new ArrayList<>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;


    ServerReceiver serverClass;

    ClientSender clientClass;

    InetAddress groupOwnerAdress;

    private boolean peerEstablished = false;
    private boolean isClient = false;
    
    
    
    public static final int MESSAGE_READ = 1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_frames);

        initialWork();
        exqListener();
        
        
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Log.d("serverLogs", "Jestem w handlerze"+msg.toString());
            switch (msg.what) {
                case MESSAGE_READ:
                    Log.d("serverLogs", "Jestem w handlerze, spróbuję nadpisac wiadomość");
                    byte[] readBuffer = (byte[]) msg.obj;
                    String tempMsg = new String(readBuffer, 0, msg.arg1);
                    Log.d("serverLogs", "otrzymano: " + tempMsg);
                    //readMsgBox.setText(tempMsg);
                    break;
            }
            return true;
        }
    });



    private void initialWork()
    {
        btnDiscoverPeers = findViewById(R.id.discover_button);
        btnConnect = findViewById(R.id.connect_button);
        btnStartCapturing = findViewById(R.id.start_capturing_button);
        twConnectionStatus = findViewById(R.id.connection_status_text_view);
        spinnerPeers = findViewById(R.id.spinner);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }




    private void exqListener()
    {

        btnDiscoverPeers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wifiOn();
                wifiP2pManager.discoverPeers(p2pChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        twConnectionStatus.setText("Wykrywanie rozpoczęte");
                    }

                    @Override
                    public void onFailure(int i) {
                        twConnectionStatus.setText("Wykrywanie nieudane");
                    }
                });
            }
        });



    }

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            //final InetAddress groupOwnerAdress = wifiP2pInfo.groupOwnerAddress;
            groupOwnerAdress = wifiP2pInfo.groupOwnerAddress;


            if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
                Log.i("serverLogs", "ConnectionListener; Połaczony jako host, tworzę nowy serwer");

                twConnectionStatus.setText("host");
                serverClass = new ServerReceiver(handler);
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
}
