package com.example.stereovisioncarsystem;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientSender extends Thread {
    Socket socket;
    String hostAddress;
    public Handler clientMsgHandler;
    OutputStream outputStream;
    private int i = 0;

    public ClientSender(InetAddress hostAddress) {
        Log.d("serverLogs", "ClientSender; Tworzę nowego clientServera!");
        socket = new Socket();
        this.hostAddress = hostAddress.getHostAddress();


    }

    @SuppressLint("HandlerLeak")
    @Override
    public void run() {

        Looper.prepare();
        try {
            Log.d("serverLogs", "ClientSender; Próbuję się połączyć z outputStreamem!");

            socket.connect(new InetSocketAddress(hostAddress, 3333), 500);
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }


        if(!socket.isConnected())
        {
            Log.d("serverLogs", "ClientSender, Przerywam!");
            interrupt();
        }

        clientMsgHandler = new Handler() {
            public void handleMessage(Message msg) {
                Log.d("serverLogs", "ClientSender; Jestem w handlerze, zaraz będę wysyłał wiadomość!");
                if (msg.what == 0) {
                    try {
                        outputStream.write((byte[])msg.obj);
                        outputStream.flush();
                        i++;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        };

        Looper.loop();

    }

    public boolean isSocketAlive() {
        return socket == null ? false : socket.isConnected();
    }

    public void clear()
    {
        Log.d("serverLogs", "ClientSender; Jestem w clear!");
        if(socket.isConnected())
        {
            try {
                Log.d("serverLogs", "ClientSender; staram się wszystko pozamykać");
                Log.d("serverLogs", "ClientSender; socket: " + socket.isClosed());

                outputStream.close();
                socket.close();
                clientMsgHandler.getLooper().quit();
                this.interrupt();

                Log.d("serverLogs", "ClientSender; socket: " + socket.isClosed());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}