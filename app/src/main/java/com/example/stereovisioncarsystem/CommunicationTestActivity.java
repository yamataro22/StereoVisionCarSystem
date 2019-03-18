package com.example.stereovisioncarsystem;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class CommunicationTestActivity extends AppCompatActivity {

    Button btnOnOff, btnDiscover, btnSend;
    ListView listView;
    TextView readMsgBox, connectionStatus;
    EditText writeMsg;


    WifiManager wifiManager;
    WifiP2pManager wifiP2pManager;
    WifiP2pManager.Channel p2pChannel;

    BroadcastReceiver broadcastReceiver;
    IntentFilter intentFilter;

    List<WifiP2pDevice> peers = new ArrayList<>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;

    static final int MESSAGE_READ = 1;

    ServerClass serverClass;
    ClientClass clientClass;
    SendReceive sendReceive;
    InetAddress groupOwnerAdress;


    protected static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;

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

    //służy do obsługi otrzymania wiadomości

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
                    readMsgBox.setText(tempMsg);
                    break;
            }
            return true;
        }
    });

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
                wifiP2pManager.discoverPeers(p2pChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        connectionStatus.setText("Wykrywanie rozpoczęte");
                    }

                    @Override
                    public void onFailure(int i) {
                        connectionStatus.setText("Wykrywanie nieudane");
                    }
                });
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final WifiP2pDevice device = deviceArray[i];
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;

                wifiP2pManager.connect(p2pChannel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getApplicationContext(), "Connected to " + device.deviceName, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int i) {
                        Toast.makeText(getApplicationContext(), "Not connected", Toast.LENGTH_SHORT).show();
                        return;
                    }
                });
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //String msg = writeMsg.getText().toString();
                Log.d("serverLogs", "Próbuję wysłać wiadomość. Status sendreceive: ");
                //sendReceive.write(msg.getBytes());
                //Object[] objArray = new Object[1];
                //objArray[0] = groupOwnerAdress;
                //new SendingClient().execute(objArray);
                if (clientClass.clientMsgHandler != null) {
                    Message msg = clientClass.clientMsgHandler.obtainMessage(0);
                    Log.d("serverLogs", "Wysyłam wiadomość");
                    clientClass.clientMsgHandler.sendMessage(msg);
                }
            }
        });
    }

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            //final InetAddress groupOwnerAdress = wifiP2pInfo.groupOwnerAddress;
            groupOwnerAdress = wifiP2pInfo.groupOwnerAddress;


            if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
                Log.i("serverLogs", "Połaczony jako host, rozpoczynam wątek serwera");
                connectionStatus.setText("host");
                serverClass = new ServerClass();
                serverClass.start();
                Log.i("serverLogs", "Stworzyłem obiekt servera; status: " + serverClass.isAlive());
            } else if (wifiP2pInfo.groupFormed) {
                  connectionStatus.setText("client");
                    Log.i("serverLogs", "Połaczony jako klient, rozpoczynam wątek klienta");
                    clientClass = new ClientClass(groupOwnerAdress);
                    //clientClass.run();
                    clientClass.start();
                    Log.i("serverLogs", "Stworzyłem obiekt klienta; status: ");
            }
        }
    };

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            if (!peerList.getDeviceList().equals(peers)) {
                peers.clear();
                peers.addAll(peerList.getDeviceList());


                deviceNameArray = new String[peerList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];

                int index = 0;

                for (WifiP2pDevice device : peers) {
                    deviceNameArray[index] = device.deviceName;
                    deviceArray[index] = device;
                    index++;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNameArray);
                listView.setAdapter(adapter);

            }
            if (peers.size() == 0) {
                Toast.makeText(getApplicationContext(), "Nie ma żadnych peerów", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    };

    private void initialWork() {
        btnOnOff = findViewById(R.id.wifi_on_button);
        btnDiscover = findViewById(R.id.discover_button);
        btnSend = findViewById(R.id.send_button);
        listView = findViewById(R.id.peer_list_view);
        readMsgBox = findViewById(R.id.received_message_text_view);
        connectionStatus = findViewById(R.id.is_connected_text_view);
        writeMsg = findViewById(R.id.send_message_edit_text);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        p2pChannel = wifiP2pManager.initialize(this, getMainLooper(), null);

        broadcastReceiver = new WiFiBroadcastReceiver(wifiP2pManager, p2pChannel, this);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);


    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    public static class FileServerAsyncTask extends AsyncTask<Void, Void, String> {

        private Context context;
        private TextView statusText;
        private Handler handler;

        public FileServerAsyncTask(Context context, View statusText, Handler handler) {
            this.context = context;
            this.statusText = (TextView) statusText;
            this.handler = handler;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {

                /**
                 * Create a server socket and wait for client connections. This
                 * call blocks until a connection is accepted from a client
                 */
                ServerSocket serverSocket = new ServerSocket(3333);
                Socket client = serverSocket.accept();

                /**
                 * If this code is reached, a client has connected and transferred data
                 * Save the input stream from the client as a JPEG file
                 */
                byte[] buffer = new byte[1024];
                int bytes;
                InputStream inputStream = client.getInputStream();


                while (client != null) {
                    try {
                        bytes = inputStream.read(buffer);
                        if (bytes > 0) {
                            handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                serverSocket.close();
                return "dziala";
            } catch (IOException e) {
                Log.i("serverLogs", "exception w fileAsyncTasku");
                return null;
            }
        }

        /**
         * Start activity that can handle the JPEG image
         */
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Log.i("serverLogs", "jestem w postexecute");
            }
        }
    }



    private class SendReceive extends Thread {
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public SendReceive(Socket socket) {
            this.socket = socket;
            Log.i("serverLogs", "Konstruktor klasy sendReceive");
            try {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            Log.i("serverLogs", "Jestem w SendReceive, metoda run.");
            byte[] buffer = new byte[1024];
            int bytes;

            while (socket != null) {
                try {
                    bytes = inputStream.read(buffer);
                    if (bytes > 0) {
                        handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    public class ServerClass extends Thread
    {
        Socket socket;
        ServerSocket serverSocket;
        InputStream inputStream;

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            Log.d("serverLogs", "klasa ServerClass wystartowała");
            try {
                serverSocket = new ServerSocket(3333);


                    Log.d("serverLogs", "Czekam na akceptację socketa!");
                    socket = serverSocket.accept();
                    Log.d("serverLogs", "Zaakceptowano socketa!");
                    inputStream = socket.getInputStream();

                    while(socket!=null && !socket.isClosed())
                    {
                        Log.d("serverLogs", "Socket różny od nulla!");
                        try{
                            bytes = inputStream.read(buffer);
                            if (bytes > 0) {
                                Log.d("serverLogs", "Ilość bajtów różna od zera");
                                Message m = Message.obtain(handler, MESSAGE_READ, bytes, -1, buffer);
                                handler.sendMessage(m);
                            }
                        }   catch(IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    Log.d("serverLogs", "No i socket null");
                    Log.i("serverLogs", "Jestem w serverClass. tworzę sendReceive");



//                sendReceive = new SendReceive(socket);
//                sendReceive.start();
//                Log.i("serverLogs", "Jestem w serverClass. status SendReceive"+sendReceive.isAlive());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class ClientClass extends Thread
    {
        Socket socket;
        String hostAddress;
        public Handler clientMsgHandler;
        OutputStream outputStream;
        private int i = 0;

        public ClientClass(InetAddress hostAddr) {

            hostAddress = hostAddr.getHostAddress();
            socket = new Socket();
        }

        @SuppressLint("HandlerLeak")
        @Override
        public void run() {



            try {
                socket.connect(new InetSocketAddress(hostAddress, 3333),500);
                outputStream = socket.getOutputStream();
                Log.d("serverLogs", "Jestem w clientClass. tworzę looperka");

                Looper.prepare();

                clientMsgHandler = new Handler()
                {
                    public void handleMessage(Message msg)
                    {
                        Log.d("serverLogs", "Jestem w handlerze, zaraz będę wysyłał wiadomość!");
                        if(msg.what == 0)
                        {
                            try {
                                outputStream.write((""+i).getBytes());
                                i++;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                };

                Looper.loop();
            } catch (IOException e) {
                e.printStackTrace();
            }







        }
    }

}
