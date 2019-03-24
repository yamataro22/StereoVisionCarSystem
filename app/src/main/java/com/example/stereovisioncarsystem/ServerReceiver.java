package com.example.stereovisioncarsystem;

import android.os.Handler;
import android.os.Message;
import android.support.v4.content.res.TypedArrayUtils;
import android.util.Log;

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
        byte[] buffer = new byte[76800];
        byte[] buffImg = new byte[76800];
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
                //inputStream = socket.getInputStream();
                Log.d("serverLogs", "SerwerClass; Zaakceptowano socketa!");


                while ((bytes = socket.getInputStream().read(buffer)) > 0)
                {
                    byte[] temp = Arrays.copyOfRange(buffer,0,bytes);
                    for(int i = 0; i < bytes; i++)
                    {
                        buffImg[byteBuffSize+i]=temp[i];
                    }
                    byteBuffSize += bytes;


                    if(byteBuffSize == 76800)
                    {

                        //Message m = Message.obtain(handler, CommunicationTestActivity.MESSAGE_READ, bytes, -1, buffer);
                        Message m = Message.obtain(handler, CommunicationTestActivity.MESSAGE_READ, byteBuffSize, -1, buffImg);
                        byteBuffSize = 0;
                        handler.sendMessage(m);
                    }
                    Log.d("serverLogs", "Otrzymano " + bytes + " bajtów");


                }
                Log.d("serverLogs", "SerwerClass; zamykam socketa");
                socket.close();

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
