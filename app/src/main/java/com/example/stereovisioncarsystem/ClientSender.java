package com.example.stereovisioncarsystem;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientSender extends Thread {
    Socket socket;
    String hostAddress;
    public Handler clientMsgHandler;
    OutputStream outputStream;
    InputStream inputStream;
    boolean shouldISkipSomeFrames = false;
    public static int PHOTO_MESSAGE_TYPE = 0;
    public static int STRING_MESSAGE_TYPE = 1;


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
            inputStream = socket.getInputStream();
            Log.d("receiveTask", "próbuję uruchomić receive taska");
            new ReceiveTask().execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(outputStream));



        clientMsgHandler = new Handler() {
            public void handleMessage(Message msg) {

                if (msg.what == PHOTO_MESSAGE_TYPE) {
                    if(!shouldISkipSomeFrames) {
                        try {
                            byte[] photoArray = (byte[]) msg.obj;
                            dos.writeInt(msg.arg1);
                            dos.writeInt(msg.arg2);
                            dos.write(photoArray);
                            dos.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                else if(msg.what == STRING_MESSAGE_TYPE)
                {
                    Log.d("serverLogs", "ClientSender; Jestem w handlerze, what jest równe 0!");
                    try {
                        String message = (String)msg.obj;
                        Log.d("serverLogs", "ClientSender; Wysyłam wiadomośc "+ message);
                        byte[] byteArray = message.getBytes();
                        Log.d("serverLogs", "ClientSender; Wysyłam wiadomośc długości "+ byteArray.length);
                        dos.writeInt(byteArray.length);
                        dos.writeInt(1);
                        Log.d("serverLogs", "ClientSender; Metoda writeInt zwróciła "+ byteArray.length);
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

    private class ReceiveTask extends AsyncTask<Void,Void,Void>
    {

        @Override
        protected Void doInBackground(Void... voids) {
            try {

                Log.d("receiveTask", "Uruchomiono nowy async task");
                DataInputStream dis = new DataInputStream(inputStream);

                Log.d("receiveTask", "SerwerClass; Czekam na przeczytanie inta!");

                int rows = dis.readInt();
                int cols = dis.readInt();

                Log.d("receiveTask", "Przeczytano inta o wartości:" + rows);
                int length = rows * cols;
                byte[] buffImg = new byte[rows*cols];
                dis.readFully(buffImg,0,length);

                if(rows == 1)
                {
                    String msg = new String(buffImg, 0, rows);
                    Log.d("receiveTask", "SerwerClass; otrzymano specjalną wiadomość: " + msg);
                    shouldISkipSomeFrames = shouldISkipSomeFrames ? false : true;
                }
                else
                {
                    Log.d("receiveTask", "SerwerClass; Length: "+ length);

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(socket == null) return;
            if(!socket.isClosed()) new ReceiveTask().execute();
        }
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

    public void sendBreakMessage()
    {
        Log.d("serverLogs", "ReceiveFramesActivity; SendEmptyMessage; Wysyłam żeby zakończyć komuniakcję");
        sendMessage(STRING_MESSAGE_TYPE, "b");
    }

    public void sendEndMessage()
    {
        Log.d("serverLogs", "ReceiveFramesActivity; SendEmptyMessage; Wysyłam żeby zakończyć komuniakcję");
        sendMessage(STRING_MESSAGE_TYPE,"e");
    }

    public void sendMessage(int what, String message)
    {
        if(clientMsgHandler!=null)
        {
            Message msg = clientMsgHandler.obtainMessage(what, message);
            clientMsgHandler.sendMessage(msg);
        }
    }


}