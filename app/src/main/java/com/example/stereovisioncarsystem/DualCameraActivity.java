package com.example.stereovisioncarsystem;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;

public class DualCameraActivity extends CommunicationBasicActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dual_camera);
        enableWiFi();
        discoverPeers();
    }

    @Override
    protected void onClientConnected() {

    }

    @Override
    protected void onServerConnected() {

    }

    @Override
    protected void onPeersListUpdate(String[] deviceNameArray) {

    }

    @Override
    protected void onDiscoverPeersInitiationFailure() {

    }

    @Override
    protected void onDiscoverPeersInitiationSuccess() {

    }

    @Override
    protected void onConnectionFail() {

    }

    @Override
    protected boolean processMessage(Message msg) {
        return false;
    }
}
