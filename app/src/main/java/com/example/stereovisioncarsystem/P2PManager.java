package com.example.stereovisioncarsystem;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;


import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public abstract class P2PManager {

    android.os.Handler handler;

    WifiManager wifiManager;
    WifiP2pManager wifiP2pManager;
    WifiP2pManager.Channel p2pChannel;

    BroadcastReceiver broadcastReceiver;
    IntentFilter intentFilter;

    List<WifiP2pDevice> peers = new ArrayList<>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;
    InetAddress groupOwnerAdress;
    Context context;

    boolean areDiscovered = false;




    public P2PManager(Context context, WifiManager manager, WifiP2pManager p2pManager, BroadcastReceiver receiver)
    {
        this.wifiManager = manager;
        this.wifiP2pManager = p2pManager;
        this.broadcastReceiver = receiver;
        this.context = context;
        handler = createHandler();
    }

    private void initialWork() {


        BroadcastReceiver broadcastReceiver;
        IntentFilter intentFilter;

        List<WifiP2pDevice> peers = new ArrayList<>();
        String[] deviceNameArray;
        WifiP2pDevice[] deviceArray;
        InetAddress groupOwnerAdress;


        wifiP2pManager = (WifiP2pManager) context.getApplicationContext().getSystemService(Context.WIFI_P2P_SERVICE);
        p2pChannel = wifiP2pManager.initialize(context, context.getApplicationContext().getMainLooper(), null);

        //broadcastReceiver = new WiFiBroadcastReceiver(wifiP2pManager, p2pChannel, this);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);


    }



    abstract Handler createHandler();







    public P2PManager()
    {

    }



    public void discoverPeers()
    {
        wifiP2pManager.discoverPeers(p2pChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                areDiscovered = true;

            }

            @Override
            public void onFailure(int i) {
                areDiscovered = false;
            }
        });
    }

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {


            groupOwnerAdress = wifiP2pInfo.groupOwnerAddress;


            if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {

                //start server class

            } else if (wifiP2pInfo.groupFormed) {

                //start client class

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

            }
            if (peers.size() == 0) {
                //Toast.makeText(getApplicationContext(), "Nie ma żadnych peerów", Toast.LENGTH_SHORT).show();
                return;
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
