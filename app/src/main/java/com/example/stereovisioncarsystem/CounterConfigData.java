package com.example.stereovisioncarsystem;

class CounterConfigData {
    private boolean isInterrupted = false;
    private int howManyFramesToCapture = 10;
    private int interval = 3;
    private boolean isRunning = false;


    public boolean isRunning()
    {
        return isRunning;
    }





    public void run() {
        isInterrupted = false;
    }


    public int getInterval() {
        return interval;
    }

    public int getFramesQuantity()
    {
        return howManyFramesToCapture;
    }

    public void interrupt() {
        isInterrupted = true;
    }

    public boolean shouldBeRunning() {
        return isInterrupted ? false : true;
    }

    public void changeConfig(int framesQuantity)
    {
        isInterrupted = true;
        howManyFramesToCapture = framesQuantity;
    }

    public void updateStatus(boolean isRunning)
    {
        this.isRunning = isRunning;
    }

}
