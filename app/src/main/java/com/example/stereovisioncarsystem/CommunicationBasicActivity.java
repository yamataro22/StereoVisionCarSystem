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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import java.util.ArrayList;
import java.util.List;

public abstract class CommunicationBasicActivity extends AppCompatActivity {

    WifiManager wifiManager;
    WifiP2pManager wifiP2pManager;
    WifiP2pManager.Channel p2pChannel;

    BroadcastReceiver broadcastReceiver;
    IntentFilter intentFilter;


    WifiP2pManager.ConnectionInfoListener connectionInfoListener;
    WifiP2pManager.PeerListListener peerListListener;
    WifiP2pManager.ActionListener discoverPeersActionListener;
    WifiP2pManager.ActionListener connectedActionListener;

    Handler messageHandler;

    List<WifiP2pDevice> peers = new ArrayList<>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        initialWork();

    }

    protected abstract Handler createMessageReceivedHandler();

    protected abstract WifiP2pManager.ConnectionInfoListener createConnectionInfoListener();

    protected abstract WifiP2pManager.PeerListListener createPeerListListener();

    protected abstract WifiP2pManager.ActionListener createDiscoverPeersActionListener();

    protected abstract WifiP2pManager.ActionListener createConnectActionListener();

    protected abstract void onConnectionFail();


    private void initialWork() {

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        p2pChannel = wifiP2pManager.initialize(this, getMainLooper(), null);

        broadcastReceiver = new WiFiBroadcastReceiver(wifiP2pManager, p2pChannel, this);

        messageHandler = createMessageReceivedHandler();
        connectionInfoListener = createConnectionInfoListener();
        peerListListener = createPeerListListener();
        discoverPeersActionListener = createDiscoverPeersActionListener();
        connectedActionListener = createConnectActionListener();

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }


    public void discoverPeers()
    {
        wifiP2pManager.discoverPeers(p2pChannel, discoverPeersActionListener);
    }

    public void updatePeersArray(WifiP2pDeviceList peerList) {
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

    public boolean arePeersUpdate(WifiP2pDeviceList peerList)
    {
        return peerList.getDeviceList().equals(peers) ? true : false;
    }

    public boolean isPeerListEmpty()
    {
        return peers.size() == 0 ? true : false;
    }

    public void connectToPeer(WifiP2pDevice which)
    {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = which.deviceAddress;

        wifiP2pManager.connect(p2pChannel, config, connectedActionListener);
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

}
