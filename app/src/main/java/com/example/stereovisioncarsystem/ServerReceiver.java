package com.example.stereovisioncarsystem;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerReceiver extends Thread {

    Socket socket;
    ServerSocket serverSocket;
    Handler handler;

    public ServerReceiver(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        int bytes;

        Log.d("serverLogs", "SerwerClass; Początek run'a");
        try {

            serverSocket = new ServerSocket(3333);

            while(true)
            {
                Log.d("serverLogs", "SerwerClass; Czekam na akceptację socketa!");
                socket = serverSocket.accept();
                //inputStream = socket.getInputStream();
                Log.d("serverLogs", "SerwerClass; Zaakceptowano socketa!");


                while ((bytes = socket.getInputStream().read(buffer)) > 0)
                {
                    Log.d("serverLogs", "SerwerClass; Ilość bajtów różna od zera");
                    Message m = Message.obtain(handler, CommunicationTestActivity.MESSAGE_READ, bytes, -1, buffer);
                    handler.sendMessage(m);
                }
                Log.d("serverLogs", "SerwerClass; zamykam socketa");
                socket.close();

            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
