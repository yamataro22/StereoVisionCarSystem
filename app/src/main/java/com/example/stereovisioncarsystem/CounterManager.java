package com.example.stereovisioncarsystem;

import android.os.Handler;

class CounterManager {
    private int howManyFramesToCapture = 10;
    private int interval = 3;

    private Counter currentCounter;
    private boolean isRunning = false;

    public void runNewCounter(Handler handler, Counter.CounterListener listener)
    {
        interrupt();
        initAndStartCounter(handler, listener);
    }

    public void changeConfig(int framesQuantity)
    {
        if(currentCounter != null) currentCounter.interrupt();
        howManyFramesToCapture = framesQuantity;
    }

    public void initCounter() throws CounterRunningException
    {
        if(isRunning)
        {
            throw new CounterRunningException();
        }
    }

    public int getInterval() {
        return interval;
    }

    public int getFramesQuantity()
    {
        return howManyFramesToCapture;
    }

    public void interrupt() {
        if(currentCounter != null) currentCounter.interrupt();
    }

    public void updateStatus(boolean isRunning)
    {
        this.isRunning = isRunning;
    }


    private void initAndStartCounter(Handler handler, Counter.CounterListener listener) {
        currentCounter = new Counter(this, handler, listener);
        handler.post(currentCounter);
    }


    public class CounterRunningException extends Exception
    {

    }
}
