package com.example.stereovisioncarsystem;


import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.Message;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;


public class ReceiveFramesActivity extends CommunicationBasicActivity {



    Button btnDiscoverPeers, btnConnect, btnStartCapturing;
    TextView twConnectionStatus;
    Spinner spinnerPeers;



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

    @Override
    protected Handler createMessageReceivedHandler() {
        return new Handler(new Handler.Callback() {
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
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, deviceNameArray);
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
}
