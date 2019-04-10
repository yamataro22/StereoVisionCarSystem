package com.example.stereovisioncarsystem;

import android.os.Handler;
import android.util.Log;

class Counter implements Runnable {
    private int interval;
    private int seconds;
    private int  howManyFramesToCapture;

    private boolean isTimerRunning = true;
    private boolean isInterrupted = false;
    private int counter = 0;

    CounterManager manager;
    CounterListener listener;
    Handler handler;


    public void interrupt() {
        isInterrupted = true;
        isTimerRunning = false;
    }


    public interface CounterListener
    {
        void onTick();
        void onUpdate(int seconds, int remaining);
        void onFinish();
    }



    public Counter(CounterManager manager, Handler handler, CounterListener listener)
    {
        this.manager = manager;
        this.handler = handler;
        this.listener=listener;
        initConfig(manager);
    }

    private void initConfig(CounterManager manager) {
        interval = manager.getInterval();
        seconds = interval;
        howManyFramesToCapture = manager.getFramesQuantity();
    }


    @Override
    public void run() {
        if(!isInterrupted)
        {
            if(seconds == 0)
            {
                seconds = interval;
                if(counter < howManyFramesToCapture)
                {
                    counter++;
                    if(counter == howManyFramesToCapture) seconds = 1;
                    listener.onTick();
                }
                else
                {
                    seconds = 0;
                    isTimerRunning = false;
                    manager.updateStatus(isTimerRunning);
                    listener.onFinish();
                }
            }

            if(isTimerRunning)
            {
                listener.onUpdate(seconds, howManyFramesToCapture-counter);

                Log.d("serverLogs","Runner, seconds: " + seconds);
                seconds--;
                handler.postDelayed(this, 1000);
            }
        }
        else
        {
            manager.updateStatus(isTimerRunning);
        }
    }
}
