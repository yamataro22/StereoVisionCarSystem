package com.example.stereovisioncarsystem;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;

import android.os.Bundle;
import android.os.Message;


import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;


public class CommunicationTestActivity extends CommunicationBasicActivity {

    Button btnOnOff, btnDiscover, btnSend;
    ListView listView;
    TextView readMsgBox, connectionStatus;
    EditText writeMsg;


    public static final int MESSAGE_READ = 1;

    ServerReceiver serverClass;
    ClientSender clientClass;
    InetAddress groupOwnerAdress;
    WifiP2pDevice device;


    boolean peerEstablished = false;
    boolean isClient = false;


    @Override
    protected void onClientConnected() {
        connectionStatus.setText("client");
        Log.i("serverLogs", "ConnectionListener; Połaczony jako klient, tworzę nowy wątek klienta");

        clientClass = new ClientSender(groupOwnerAdress);
        clientClass.start();
        peerEstablished = true;
        isClient = true;

        Log.i("serverLogs", "ConnectionListener; Stworzyłem obiekt klienta; status: ");
    }

    @Override
    protected void onServerConnected() {
        Log.i("serverLogs", "ConnectionListener; Połaczony jako host, tworzę nowy serwer");

        connectionStatus.setText("host");
        serverClass = new ServerReceiver(messageHandler);
        peerEstablished = true;
        serverClass.start();

        Log.i("serverLogs", "ConnectionListener; Nowy serwer stworzony " + serverClass.isAlive());
    }

    @Override
    protected void onPeersListEmpty() {
        Toast.makeText(getApplicationContext(), "Nie ma żadnych peerów", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPeersListUpdate(String[] deviceNameArray) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNameArray);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onDiscoverPeersInitiationFailure() {
        connectionStatus.setText("Wykrywanie nieudane");
    }

    @Override
    protected void onDiscoverPeersInitiationSuccess() {
        connectionStatus.setText("Wykrywanie rozpoczęte");
    }

    @Override
    protected void onConnectionFailure() {
        Toast.makeText(getApplicationContext(), "Not connected", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onConnectionSuccess() {
        Toast.makeText(getApplicationContext(), "Connected to " + device.deviceName, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected boolean processMessage(Message msg) {
        Log.d("serverLogs", "Jestem w handlerze"+msg.toString());
        switch (msg.what) {
            case MESSAGE_READ:
                Log.d("serverLogs", "Jestem w handlerze, spróbuję nadpisac wiadomość");
                byte[] readBuffer = (byte[]) msg.obj;
                String tempMsg = new String(readBuffer, 0, msg.arg1);
                Log.d("serverLogs", "otrzymano: " + tempMsg);
                readMsgBox.setText(tempMsg);
                break;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication_test);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Test komunikacji");
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        initialWork();
        exqListener();


    }

    private void initialWork() {

        btnOnOff = findViewById(R.id.wifi_on_button);
        btnDiscover = findViewById(R.id.discover_button);
        btnSend = findViewById(R.id.send_button);
        listView = findViewById(R.id.peer_list_view);
        readMsgBox = findViewById(R.id.received_message_text_view);
        connectionStatus = findViewById(R.id.is_connected_text_view);
        writeMsg = findViewById(R.id.send_message_edit_text);

        updateWiFiButton();
    }



    @Override
    protected void onConnectionFail() {
        connectionStatus.setText("Rozłączono");

        if(isClient && peerEstablished) {
            Log.d("serverLogs", "On Pause; Staram się usunąć klienta");
            if(clientClass!=null)
            {
                clientClass.clear();
                clientClass.interrupt();
                clientClass = null;
            }

        }

    }

    private void exqListener() {

        btnOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(false);
                    btnOnOff.setText("ON");
                } else {
                    wifiManager.setWifiEnabled(true);
                    btnOnOff.setText("OFF");
                }
            }
        });

        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                discoverPeers();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                device = getDeviceArray()[i];
                connectToPeer(device);
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //String msg = writeMsg.getText().toString();
                Log.d("serverLogs", "Próbuję wysłać wiadomość. Status sendreceive: ");
                checkClientStatusAndSendMessage(writeMsg.getText().toString());
            }
        });
    }

    private void checkClientStatusAndSendMessage(String message)
    {
        if(clientClass.isSocketAlive())
        {
            sendMessageToServer(message);
        }
    }

    private void sendMessageToServer(String message)
    {
        if (clientClass.clientMsgHandler != null) {
            Message msg = clientClass.clientMsgHandler.obtainMessage(0, message);
            Log.d("serverLogs", "Wysyłam wiadomość");
            clientClass.clientMsgHandler.sendMessage(msg);
        }
    }

    private void updateWiFiButton()
    {
        String status = wifiManager.isWifiEnabled() ? "OFF" : "ON";
        btnOnOff.setText(status);

    }



    @Override
    protected void onPause() {
        super.onPause();

        if(isClient && peerEstablished) {
            Log.d("serverLogs", "On Pause; Staram się usunąć klienta");
            clientClass.clear();
            clientClass.interrupt();
            clientClass = null;
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

}
