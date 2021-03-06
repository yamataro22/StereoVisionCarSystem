package com.example.stereovisioncarsystem;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

public class WiFiBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private CommunicationBasicActivity activity;
    private WifiManager wifiManager;

    public WiFiBroadcastReceiver(WifiManager wifiManager, WifiP2pManager manager, WifiP2pManager.Channel channel, CommunicationBasicActivity activity) {
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
        this.wifiManager = wifiManager;
    }



    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

            if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED)
            {
                Toast.makeText(context,"WiFi on",Toast.LENGTH_SHORT).show();
            }
            else if(state == WifiP2pManager.WIFI_P2P_STATE_DISABLED)
            {
                Toast.makeText(context,"WiFi off",Toast.LENGTH_SHORT).show();
            }


        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if(manager != null)
            {
                manager.requestPeers(channel,activity.peerListListener);
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            if(manager == null)
            {
                return;
            }

            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if(networkInfo.isConnected())
            {
                manager.requestConnectionInfo(channel,activity.connectionInfoListener);
            }
            else
            {
                activity.onConnectionFail();
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
        }
        else if(WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action))
        {
            int info = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,0);

            switch(info)
            {
                case WifiManager.WIFI_STATE_DISABLED:
                {
                    break;
                }
                case WifiManager.WIFI_STATE_ENABLED:
                {
                    activity.onWiFiOnListener();
                    break;
                }
            }
        }
    }
}
