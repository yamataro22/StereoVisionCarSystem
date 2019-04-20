package com.example.stereovisioncarsystem;

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
import java.net.InetSocketAddress;
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
                    Log.d("sendMsgToClient", "Server; Próbuję się połączyć z outputStreamem!");
                    outputStream = socket.getOutputStream();
                    final DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(outputStream));
                    String message = strings[0];
                    Log.d("sendMsgToClient", "ClientSender; Wysyłam wiadomośc "+ message);
                    byte[] byteArray = message.getBytes();
                    Log.d("sendMsgToClient", "ClientSender; Wysyłam wiadomośc długości "+ byteArray.length);
                    dos.writeInt(byteArray.length);
                    dos.writeInt(1);
                    Log.d("sendMsgToClient", "ClientSender; Metoda writeInt zwróciła "+ byteArray.length);
                    dos.write(message.getBytes());
                    dos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPreExecute() {
                Log.d("sendMsgToClient", "on post execute");
                super.onPreExecute();
            }
        }.execute(message);

        Log.d("serverLogs", "server, niby wysłane!");
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
                    int rows = dis.readInt();
                    int cols = dis.readInt();
                    Log.d("serverLogs", "SerwerClass; Jestem za dis.readInt, rows: " + rows);
                    Log.d("serverLogs", "SerwerClass; Jestem za dis.readInt, cols: " + cols);
                    if  ( Thread.interrupted() )
                    {
                        shouldIFinish = true;
                        Log.d("serverLogs", "SerwerClass; Wykryto interrupt!");
                    }


                    int length = rows * cols;
                    byte[] buffImg = new byte[rows*cols];
                    dis.readFully(buffImg,0,length);

                    if(cols == 1)
                    {
                        String msg = new String(buffImg, 0, rows);
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
                        else if(msg.equals("r"))
                        {
                            Log.d("receiveTask", "SerwerClass, otrzymano sygnał, że client jest gotowy");
                            Message m = Message.obtain(handler, ServerDualCameraCalibrationActivity.SPECIAL_MESSAGE, "r");
                            handler.sendMessage(m);
                        }
                        else
                        {
                            Log.d("receiveTask", "SerwerClass, otrzymano macierz!");
                            Message m = Message.obtain(handler, ServerDualCameraCalibrationActivity.SPECIAL_MESSAGE, msg);
                            handler.sendMessage(m);
                        }
                    }
                    else
                    {
                        Log.d("serverLogs", "SerwerClass; Length: "+ length);
                        Message m = Message.obtain(handler, CommunicationTestActivity.MESSAGE_READ, rows, cols, buffImg);
                        handler.sendMessage(m);
                    }

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
