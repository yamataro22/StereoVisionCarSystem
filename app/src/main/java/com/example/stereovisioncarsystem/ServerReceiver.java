package com.example.stereovisioncarsystem;

import android.os.Handler;
import android.os.Message;
import android.support.v4.content.res.TypedArrayUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class ServerReceiver extends Thread {

    Socket socket;
    ServerSocket serverSocket;
    Handler handler;

    public ServerReceiver(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[2048];
        //byte[] buffImg = new byte[76800];
        int byteBuffSize = 0;
        ArrayList<Byte> buff = new ArrayList<>();
        int bytes;

        Log.d("serverLogs", "SerwerClass; Początek run'a");
        try {

            serverSocket = new ServerSocket(3333);

            while(true)
            {
                Log.d("serverLogs", "SerwerClass; Czekam na akceptację socketa!");
                socket = serverSocket.accept();

                DataInputStream dis = new DataInputStream(socket.getInputStream());


                while(true)
                {
                    int length = dis.readInt();
                    if(length == 0)
                        break;
                    Log.d("serverLogs", "SerwerClass; Length: "+ length);
                    byte[] buffImg = new byte[length];
                    dis.readFully(buffImg,0,length);
                    Message m = Message.obtain(handler, CommunicationTestActivity.MESSAGE_READ, length, -1, buffImg);
                    handler.sendMessage(m);
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clear()
    {
        if(socket.isConnected())
        {
            try {
                Log.d("serverLogs", "ClientSender; staram się wszystko pozamykać");
                Log.d("serverLogs", "ClientSender; socket: " + socket.isClosed());

                socket.close();
                handler.getLooper().quit();
                this.interrupt();

                Log.d("serverLogs", "ClientSender; socket: " + socket.isClosed());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
