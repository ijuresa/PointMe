package com.ColorBlobCalibrate;

/**
 * Created by ivanj on 16/01/2017.
 */

public class TimeDebounce {
    private long startTime, endTime;
    private boolean isStarted = false;

    public void startDebounce(){
        startTime = System.currentTimeMillis();
        isStarted = true;
    }

    public boolean debounceOver(){
        if(isStarted) {
            if ((System.currentTimeMillis() - startTime) <= 800) {
                return false;
            }
        }
        isStarted = false;
        return true;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public long getStartTime() {
        return startTime;
    }
}
