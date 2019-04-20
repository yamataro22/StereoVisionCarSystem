package com.example.stereovisioncarsystem;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public abstract class CommunicationBasicActivity extends AppCompatActivity {

    private WifiManager wifiManager;

    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel p2pChannel;
    private BroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;
    protected InetAddress groupOwnerAdress;
    private List<WifiP2pDevice> peers = new ArrayList<>();
    private String[] deviceNameArray;
    private WifiP2pDevice[] deviceArray;
    private WifiP2pDevice connectedDevice = null;
    WifiP2pManager.ConnectionInfoListener connectionInfoListener;

    WifiP2pManager.PeerListListener peerListListener;
    WifiP2pManager.ActionListener discoverPeersActionListener;
    WifiP2pManager.ActionListener connectedActionListener;
    Handler messageHandler;

    protected ClientSender clientClass;
    protected ServerReceiver serverClass;
    boolean isServerCreated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialWork();
        initListeners();
    }

    private void initialWork() {

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        enableWiFi();
        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        p2pChannel = wifiP2pManager.initialize(this, getMainLooper(), null);

        broadcastReceiver = new WiFiBroadcastReceiver(wifiManager, wifiP2pManager, p2pChannel, this);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
    }
    private void initListeners() {
        connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {

                groupOwnerAdress = wifiP2pInfo.groupOwnerAddress;


                if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
                    onServerConnected();

                } else if (wifiP2pInfo.groupFormed) {
                    onClientConnected();
                }
            }
        };

        peerListListener = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peerList) {

                if (!arePeersUpdate(peerList)) {

                    updatePeersArray(peerList);
                    onPeersListUpdate(deviceNameArray);
                }
                if (isPeerListEmpty()) {
                    onPeersListEmpty();
                }
            }
        };

        discoverPeersActionListener = new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                onDiscoverPeersInitiationSuccess();
            }

            @Override
            public void onFailure(int i) {
                onDiscoverPeersInitiationFailure();
            }
        };

        connectedActionListener = new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                onConnectionSuccess();
            }

            @Override
            public void onFailure(int i) {
                onConnectionFailure();
            }
        };

        messageHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                return processMessage(msg);
            }
        });
    }
    protected void onClientConnected()
    {
        Log.i("serverLogs", "ConnectionListener; Połaczony jako klient, tworzę nowy wątek klienta");
        clientClass = new ClientSender(groupOwnerAdress);
        clientClass.start();
        Log.i("serverLogs", "ConnectionListener; Stworzyłem obiekt klienta; status: ");
    }
    protected void onServerConnected()
    {
        Log.i("serverLogs", "ConnectionListener; Połaczony jako host, tworzę nowy serwer");
        if(!isServerCreated)
        {
            serverClass = new ServerReceiver(messageHandler);
            serverClass.start();
            isServerCreated = true;
        }
        else
        {
            Log.i("serverLogs", "ConnectionListener; Serwer był już stworzony");
        }
    }
    protected abstract void onPeersListUpdate(String[] deviceNameArray);
    protected abstract void onDiscoverPeersInitiationFailure();

    protected abstract void onDiscoverPeersInitiationSuccess();

    protected void onConnectionFail()
    {

        if(clientClass!=null)
        {
            Log.d("serverLogs", "onConnectionFail; Staram się usunąć klienta");
            clientClass.sendEndMessage();
            SystemClock.sleep(40);
            clientClass.clear();
            clientClass = null;
        }
        if(serverClass!=null)
        {
            try
            {
                Log.d("serverLogs", "CommunicationTestActivity; onConnectionFail; Staram się usunąć serwer");
                serverClass.closeServer();
                isServerCreated = false;
                serverClass=null;
            }
            catch(IOException e)
            {
                Log.d("serverLogs", "CommunicationTestActivity; On Destroy; Wyjątek");
            }
        }
    }

    protected abstract boolean processMessage(Message msg);
    protected void onPeersListEmpty()
    {
        Toast.makeText(getApplicationContext(), "Nie ma żadnych peerów", Toast.LENGTH_SHORT).show();
    }

    protected void onConnectionFailure()
    {
        Toast.makeText(getApplicationContext(), "Not connected", Toast.LENGTH_SHORT).show();
    }

    protected void onConnectionSuccess()
    {
        Toast.makeText(getApplicationContext(), "Connected to " + getConnectedDevice().deviceName, Toast.LENGTH_SHORT).show();
    }

    public void discoverPeers()
    {
        wifiP2pManager.discoverPeers(p2pChannel, discoverPeersActionListener);
    }

    private void updatePeersArray(WifiP2pDeviceList peerList) {
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
    }

    private boolean arePeersUpdate(WifiP2pDeviceList peerList)
    {
        return peerList.getDeviceList().equals(peers) ? true : false;
    }

    private boolean isPeerListEmpty()
    {
        return peers.size() == 0 ? true : false;
    }

    protected void connectToPeer(WifiP2pDevice which)
    {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = which.deviceAddress;
        connectedDevice = which;
        wifiP2pManager.connect(p2pChannel, config, connectedActionListener);
    }

    public WifiP2pDevice[] getDeviceArray() {
        return deviceArray;
    }

    public WifiP2pDevice getConnectedDevice() {
        return connectedDevice;
    }

    public void enableWiFi()
    {
        if(!isWiFiEnabled())
        {
            wifiManager.setWifiEnabled(true);
        }
    }

    public void disableWiFi()
    {
        if(isWiFiEnabled())
        {
            wifiManager.setWifiEnabled(false);
        }
    }

    public boolean isWiFiEnabled() {
        return wifiManager.isWifiEnabled() ? true : false;
    }


    public WifiP2pDevice getDeviceByIndex(int id)
    {
        return deviceArray[id];
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, intentFilter);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(clientClass != null)
        {
            Log.d("serverLogs", "On Pause; Staram się usunąć klienta");
            clientClass.sendBreakMessageToHandler();
            SystemClock.sleep(40);
            clientClass.clear();
            clientClass = null;
        }
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //disableWiFi();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(serverClass!=null)
        {
            try
            {
                Log.d("serverLogs", "CommunicationTestActivity; On Destroy; Staram się usunąć serwer");
                serverClass.closeServer();
                serverClass = null;
            }
            catch(IOException e)
            {
                Log.d("serverLogs", "CommunicationTestActivity; On Destroy; Wyjątek");
            }
        }
        if(clientClass!=null)
        {

            clientClass.sendEndMessage();
            SystemClock.sleep(40);
            clientClass.clear();
            clientClass = null;
        }
        disableWiFi();
    }

    public void onWiFiOnListener() {
    }
}
