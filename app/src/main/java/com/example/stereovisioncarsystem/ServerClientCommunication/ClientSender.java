package com.example.stereovisioncarsystem.ServerClientCommunication;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.example.stereovisioncarsystem.CameraData;

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
    private CameraData cameraData;
    public final static String TAG = "serverClientCom";

    public void setCameraData(CameraData mat)
    {
        cameraData = mat;
    }



    public ClientSender(InetAddress hostAddress) {
        Log.i(TAG, "ClientSender; Tworzę nowego clientServera!");
        socket = new Socket();
        this.hostAddress = hostAddress.getHostAddress();
    }

    @SuppressLint("HandlerLeak")
    @Override
    public void run() {

        Looper.prepare();
        try {
            Log.i(TAG, "ClientSender; Próbuję się połączyć z outputStreamem!");

            socket.connect(new InetSocketAddress(hostAddress, 3333), 500);
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
            new ReceiveTask().execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(outputStream));



        clientMsgHandler = new Handler() {
            public void handleMessage(Message msg) {
                Log.i(TAG, "ClientSender; Jestem w handlerze");
                switch (msg.what)
                {
                    case ClientHandlerMsg.FRAME_MSG:
                        if(shouldISkipSomeFrames) return;
                        Log.i(TAG, "ClientSender; Jestem w handlerze; wysyłam frame");
                        tryToSendFrame(dos,msg);
                        break;
                    case ClientHandlerMsg.SPECIAL_MSG:
                    {
                        Log.i(TAG, "ClientSender; Jestem w handlerze; wysyłam wiadomość specjalną");
                        tryToSendSpecialMessage(dos,msg);
                    }
                }
            }
        };


        Looper.loop();
        Log.d(TAG, "ClientSender; Jestem już poza loopem");
    }

    private void tryToSendFrame(DataOutputStream dos, Message msg) {
        Log.d(TAG, "ClientSender; próbuję wysłać frame");
        try {
            byte[] photoArray = (byte[]) msg.obj;
            sendMessage(dos,photoArray,msg.arg1,msg.arg2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void tryToSendSpecialMessage(DataOutputStream dos, Message msg) {
        String messageTagString = (String) msg.obj;
        byte[] messageTagBytes = messageTagString.getBytes();

        try {
            sendMessage(dos,messageTagBytes,messageTagBytes.length,1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(DataOutputStream dos, byte[] obj, int arg1, int arg2) throws IOException {
        dos.writeInt(arg1);
        dos.writeInt(arg2);
        dos.write(obj);
        dos.flush();
    }


    public void sendReadyMessageToHandler()
    {
        Log.i(TAG, "ClientSender, wysyłam wiadomość że jestem gotowy");
        sendMessageToHandler(ClientHandlerMsg.SPECIAL_MSG,ClientServerMessages.CLIENT_READY);
    }

    private class ReceiveTask extends AsyncTask<Void,Void,Void>
    {

        @Override
        protected Void doInBackground(Void... voids) {
            try {

                Log.i(TAG, "ClientAsyncReceive; Uruchomiono nowy async task");
                DataInputStream dis = new DataInputStream(inputStream);
                int length = dis.readInt();

                Log.i(TAG, "ClientAsyncReceive; Przeczytano inta o wartości:" + length);

                byte[] buffImg = new byte[length];
                dis.readFully(buffImg,0,length);
                if(length!=1) return null;

                String msg = new String(buffImg, 0, length);
                switch (msg)
                {
                    case ClientServerMessages.SKIP_FRAMES:
                        Log.i(TAG, "ClientAsyncReceive; otrzymano specjalną wiadomość: SKIP_FRAMES");
                        shouldISkipSomeFrames = shouldISkipSomeFrames ? false : true;
                        break;
                    case ClientServerMessages.GET_CAMERA_DATA:
                        Log.i(TAG, "ClientAsyncReceive; otrzymano specjalną wiadomość: GET_CAMERA_DATA");
                        sendMessageToHandler(ClientHandlerMsg.SPECIAL_MSG, cameraData.toString());
                        break;
                    case ClientServerMessages.CONNECTION_FINISHED:
                        clear();
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
        Log.i(TAG, "ClientSender; Jestem w clear!");
        if(socket.isConnected())
        {
            try {
                Log.d(TAG, "ClientSender; staram się wszystko pozamykać");
                Log.d(TAG, "ClientSender; socket: " + socket.isClosed());

                outputStream.close();
                socket.close();
                clientMsgHandler.getLooper().quit();
                this.interrupt();

                Log.d(TAG, "ClientSender; socket: " + socket.isClosed());
                Log.d(TAG, "ClientSender; kończę wątek klienta! ");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendBreakMessageToHandler()
    {
        Log.i(TAG, "ClientSender; Wysyłam żeby przerwać komuniakcję");
        sendMessageToHandler(ClientHandlerMsg.SPECIAL_MSG, ClientServerMessages.CLIENT_DISCONNECED);
    }

    public void sendEndMessage()
    {
        Log.i(TAG, "ClientSender; SendEmptyMessage; Wysyłam żeby zakończyć komuniakcję");
        sendMessageToHandler(ClientHandlerMsg.SPECIAL_MSG,ClientServerMessages.CONNECTION_FINISHED);
    }

    public void sendMessageToHandler(int what, String message)
    {
        if(clientMsgHandler!=null)
        {
            Log.i(TAG, "Client, sendMessageToHandler, handler!=null, obtainuje wiadomosc " + message);
            Message msg = clientMsgHandler.obtainMessage(what, message);
            clientMsgHandler.sendMessage(msg);
        }
    }


}