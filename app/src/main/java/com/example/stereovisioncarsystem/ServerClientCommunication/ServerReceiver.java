package com.example.stereovisioncarsystem.ServerClientCommunication;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerReceiver extends Thread {

    Socket socket;
    ServerSocket serverSocket;
    Handler handler;
    InputStream inputStream;
    OutputStream outputStream;
    boolean shouldIFinish = false;
    DataInputStream dis;

    public final static String TAG = "serverClientCom";


    public ServerReceiver(Handler handler) {
        this.handler = handler;
    }

    @SuppressLint("StaticFieldLeak")
    public void sendMsgToClient(String message)
    {
        new AsyncTask<String,Void,Void>()
        {
            @Override
            protected Void doInBackground(String... strings) {
                try {
                    Log.d(TAG, "ServerAsync; Próbuję się połączyć z outputStreamem!");
                    outputStream = socket.getOutputStream();
                    final DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(outputStream));
                    String message = strings[0];
                    Log.d(TAG, "ServerAsync; Wysyłam wiadomość "+ message);
                    byte[] byteArray = message.getBytes();
                    dos.writeInt(byteArray.length);
                    dos.write(message.getBytes());
                    dos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                Log.d(TAG, "ServerAsync; on post execute");
                super.onPostExecute(aVoid);
            }
        }.execute(message);

}



    @Override
    public void run() {

        Log.d(TAG, "SerwerClass; Początek run'a");

        try {

            serverSocket = new ServerSocket(3333);

            while(true)
            {
                Log.d(TAG, "SerwerClass; Czekam na akceptację socketa!");
                if(!shouldIFinish)
                {
                    socket = serverSocket.accept();
                }
                else
                {
                    Log.d(TAG, "SerwerClass; Nie akceptujemy socketa!");
                    break;
                }
                inputStream = socket.getInputStream();
                dis = new DataInputStream(inputStream);


                while(socket.isBound())
                {
                    Log.d(TAG, "SerwerClass; Jestem w pętli");
                    int rows = dis.readInt();
                    int cols = dis.readInt();
                    Log.d(TAG, "SerwerClass; Jestem za dis.readInt, rows: " + rows);
                    Log.d(TAG, "SerwerClass; Jestem za dis.readInt, cols: " + cols);
                    if  ( Thread.interrupted() )
                    {
                        shouldIFinish = true;
                        Log.d(TAG, "SerwerClass; Wykryto interrupt!");
                    }


                    int length = rows * cols;
                    byte[] buffImg = new byte[rows*cols];
                    dis.readFully(buffImg,0,length);

                    Message m;
                    if(cols == 1)
                    {
                        String msg = new String(buffImg, 0, rows);
                        boolean isClientDisconnected = false;
                        Log.d(TAG, "SerwerClass; otrzymano specjalną wiadomość: " + msg);


                        switch(msg)
                        {
                            case ClientServerMessages.CLIENT_DISCONNECED:
                                Log.d(TAG, "SerwerClass; wychodzimy z pętli");
                                isClientDisconnected = true;
                                break;
                            case ClientServerMessages.CONNECTION_FINISHED:
                                Log.d(TAG, "SerwerClass; kończymy wątek");
                                shouldIFinish = true;
                                break;
                            case ClientServerMessages.CLIENT_READY:
                                Log.d(TAG, "SerwerClass, otrzymano sygnał, że client jest gotowy");
                                m = Message.obtain(handler, ServerHandlerMsg.CLIENT_READY_MSG);
                                handler.sendMessage(m);
                                break;
                            default:
                                Log.d(TAG, "SerwerClass, otrzymano macierz!");
                                m = Message.obtain(handler, ServerHandlerMsg.CAMERA_DATA_RECEIVED_MSG, msg);
                                handler.sendMessage(m);
                        }
                        if(isClientDisconnected) break;
                    }
                    else
                    {
                        m = Message.obtain(handler, ServerHandlerMsg.FRAME_MSG, rows, cols, buffImg);
                        handler.sendMessage(m);
                    }

                }
                Log.d(TAG, "SerwerClass; wyszedłem z pętli");
            }
            Log.d(TAG, "SerwerClass; Koniec wątku!");

        } catch (IOException e) {
            Log.d(TAG, "SerwerClass; Złapano wyjątek w głównej pętli, kończę wątek serwera!");
            e.printStackTrace();
        }
    }

    public void closeServer() throws IOException {
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
