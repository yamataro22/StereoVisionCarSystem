package com.example.stereovisioncarsystem;

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

public class CommunicationTestActivity extends CommunicationBasicActivity {

    Button btnOnOff, btnDiscover, btnSend;
    ListView listView;
    TextView readMsgBox, connectionStatus;
    EditText writeMsg;

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

    private void exqListener() {

        btnOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isWiFiEnabled()) {
                    disableWiFi();
                    btnOnOff.setText("ON");
                } else {
                    enableWiFi();
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
                connectToPeer(getDeviceByIndex(i));
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("serverLogs", "Próbuję wysłać wiadomość: " + writeMsg.getText().toString());
                checkClientStatusAndSendMessage(writeMsg.getText().toString());
            }
        });
    }

    @Override
    protected void onClientConnected() {
        connectionStatus.setText("client");
        super.onClientConnected();
    }

    @Override
    protected void onServerConnected() {
        connectionStatus.setText("host");
        super.onServerConnected();
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
    protected boolean processMessage(Message msg) {
        Log.d("serverLogs", "Jestem w handlerze"+msg.toString());
        switch (msg.what) {
            case ClientHandlerMsg.SPECIAL_MSG:
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
    protected void onConnectionFail() {
        connectionStatus.setText("Rozłączono");
        super.onConnectionFail();
    }

    private void checkClientStatusAndSendMessage(String message)
    {
        if(clientClass == null) return;
        if(clientClass.isSocketAlive())
        {
            Log.d("serverLogs", "Sprawdzam status, żyje");
            sendMessageToServer(message);
        }
    }

    private void sendMessageToServer(String message)
    {
        if (clientClass.clientMsgHandler != null) {
            Log.d("serverLogs", "Handler różny od nulla");
            Message msg = clientClass.clientMsgHandler.obtainMessage(ClientHandlerMsg.SPECIAL_MSG, message);
            Log.d("serverLogs", "Wysyłam wiadomość: " + message);
            clientClass.clientMsgHandler.sendMessage(msg);
        }
    }

    private void updateWiFiButton()
    {
        String status = isWiFiEnabled() ? "OFF" : "ON";
        btnOnOff.setText(status);

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
