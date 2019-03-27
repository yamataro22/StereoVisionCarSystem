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
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public abstract class CommunicationBasicActivity extends AppCompatActivity {

    protected WifiManager wifiManager;
    protected WifiP2pManager wifiP2pManager;
    protected WifiP2pManager.Channel p2pChannel;

    protected BroadcastReceiver broadcastReceiver;
    protected IntentFilter intentFilter;
    protected InetAddress groupOwnerAdress;

    private List<WifiP2pDevice> peers = new ArrayList<>();
    private String[] deviceNameArray;

    private WifiP2pDevice[] deviceArray;

    WifiP2pManager.ConnectionInfoListener connectionInfoListener;

    WifiP2pManager.PeerListListener peerListListener;
    WifiP2pManager.ActionListener discoverPeersActionListener;
    WifiP2pManager.ActionListener connectedActionListener;
    Handler messageHandler;



    protected abstract void onClientConnected();

    protected abstract void onServerConnected();
    protected abstract void onPeersListEmpty();
    protected abstract void onPeersListUpdate(String[] deviceNameArray);
    protected abstract void onDiscoverPeersInitiationFailure();
    protected abstract void onDiscoverPeersInitiationSuccess();
    protected abstract void onConnectionFailure();
    protected abstract void onConnectionSuccess();
    protected abstract void onConnectionFail();
    protected abstract boolean processMessage(Message msg);

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        initialWork();
        initListeners();
    }

    private void initialWork() {

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

        wifiP2pManager.connect(p2pChannel, config, connectedActionListener);
    }

    public WifiP2pDevice[] getDeviceArray() {
        return deviceArray;
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
    private boolean isWiFiEnabled() {
        return wifiManager.isWifiEnabled() ? true : false;
    }








}
