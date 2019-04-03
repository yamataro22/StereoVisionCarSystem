package com.example.stereovisioncarsystem;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerReceiver extends Thread {

    Socket socket;
    ServerSocket serverSocket;
    Handler handler;
    InputStream inputStream;
    boolean shouldIFinish = false;
    DataInputStream dis;

    public ServerReceiver(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void run() {
        Log.d("serverLogs", "SerwerClass; Początek run'a");

        try {

            serverSocket = new ServerSocket(3333);

            while(true)
            {
                Log.d("serverLogs", "SerwerClass; Czekam na akceptację socketa!");
                if(!shouldIFinish)
                {
                    socket = serverSocket.accept();
                }
                else
                {
                    Log.d("serverLogs", "SerwerClass; Nie akceptujemy socketa!");
                    break;
                }
                inputStream = socket.getInputStream();
                dis = new DataInputStream(inputStream);


                while(socket.isBound())
                {
                    Log.d("serverLogs", "SerwerClass; Jestem w pętli");
                    int length = dis.readInt();
                    Log.d("serverLogs", "SerwerClass; Jestem za dis.readInt, length: " + length);
                    if  ( Thread.interrupted() )
                    {
                        shouldIFinish = true;
                        Log.d("serverLogs", "SerwerClass; Wykryto interrupt!");
                    }
                    byte[] buffImg = new byte[length];
                    dis.readFully(buffImg,0,length);

                    if(length == 1)
                    {
                        String msg = new String(buffImg, 0, length);
                        Log.d("serverLogs", "SerwerClass; otrzymano specjalną wiadomość: " + msg);
                        if(msg.equals("b"))
                        {
                            Log.d("serverLogs", "SerwerClass; wychodzimy z pętli");
                            break;
                        }
                        else if(msg.equals("e"))
                        {
                            Log.d("serverLogs", "SerwerClass; kończymy wątek");
                            shouldIFinish = true;
                        }
                    }
                    else
                    {
                        Log.d("serverLogs", "SerwerClass; Length: "+ length);

                    }
                    Message m = Message.obtain(handler, CommunicationTestActivity.MESSAGE_READ, length, -1, buffImg);
                    handler.sendMessage(m);
                }
                Log.d("serverLogs", "SerwerClass; wyszedłem z pętli");
            }
            Log.d("serverLogs", "SerwerClass; Koniec wątku!");

        } catch (IOException e) {
            Log.d("serverLogs", "SerwerClass; Złapano wyjątek w głównej pętli, kończę wątek serwera!");
            e.printStackTrace();
        }
    }

    void closeServer() throws IOException {
        Log.d("serverLogs", "ServerReceive; On Destroy; Usuwam serwer");
        try {
            if (inputStream != null)
            {
                Log.d("serverLogs", "ServerReceive; On Destroy; Zamykam inputstreama, status ");
                inputStream.close();
                Log.d("serverLogs", "ServerReceive; On Destroy; ImputStream zamknięty");
                Log.d("serverLogs", "ServerReceive; On Destroy; Zamykam DIS");
                dis.close();
                Log.d("serverLogs", "ServerReceive; On Destroy; DIS Zamknięty");
            }
        }catch(IOException e1){
            Log.d("serverLogs", "ServerReceive; On Destroy; Wyjebało wyjątek");
                e1.printStackTrace();
            }

        if (serverSocket!= null && !serverSocket.isClosed())
        {
            Log.d("serverLogs", "ServerReceive; On Destroy; Zamykam też serversocketa, status "+serverSocket.isClosed());
            serverSocket.close();
            Log.d("serverLogs", "ServerReceive; On Destroy; Zamknięto, status: " + serverSocket.isClosed());
        }
        if (socket!= null && !socket.isClosed())
        {
            Log.d("serverLogs", "ServerReceive; On Destroy; Zamykam też socketa, status: " + socket.isClosed());
            socket.close();
            Log.d("serverLogs", "ServerReceive; On Destroy; Zamknięto, status: " + socket.isClosed());
        }


        this.interrupt();
    }

}
