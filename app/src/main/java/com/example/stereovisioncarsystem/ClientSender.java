package com.example.stereovisioncarsystem;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
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

        final DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(outputStream));




        clientMsgHandler = new Handler() {
            public void handleMessage(Message msg) {

                Log.d("serverLogs", "ClientSender; Jestem w handlerze, zaraz będę wysyłał wiadomość!");
                Log.d("serverLogs", "ClientSender; Sprawdzam stan socketa: isbound:  " + socket.isBound());
                Log.d("serverLogs", "ClientSender; Sprawdzam stan socketa: isConnected:  " + socket.isConnected());
                Log.d("serverLogs", "ClientSender; Sprawdzam stan socketa: isOutputShutdown:  " + socket.isOutputShutdown());
                Log.d("serverLogs", "ClientSender; Sprawdzam stan socketa: isInputShutdown:  " + socket.isInputShutdown());
                Log.d("serverLogs", "ClientSender; Sprawdzam stan socketa: isClosed:  " + socket.isClosed());

                if (msg.what == 0) {
                    try {
                        dos.writeInt(76800);
                        dos.write((byte[])msg.obj);
                        dos.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else if(msg.what == 1)
                {
                    Log.d("serverLogs", "ClientSender; Jestem w handlerze, what jest równe 0!");
                    try {
                        String message = (String)msg.obj;
                        Log.d("serverLogs", "ClientSender; Wysyłam wiadomośc "+ message);
                        byte[] byteArray = message.getBytes();
                        Log.d("serverLogs", "ClientSender; Wysyłam wiadomośc długości "+ byteArray.length);
                        dos.writeInt(byteArray.length);
                        Log.d("serverLogs", "ClientSender; Mesoda writeInt zwróciła "+ byteArray.length);
                        dos.write(message.getBytes());
                        dos.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };


        Looper.loop();
        Log.d("serverLogs", "ClientSender; Jestem już poza loopem");
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
                Log.d("serverLogs", "ClientSender; kończę wątek klienta! ");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}