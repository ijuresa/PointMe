package com.ColorBlobCalibrate;

/**
 * Created by ivanj on 16/01/2017.
 * @Description: Used to prevent double/triple etc.. clicks by setting 800ms 
 *                  delay after each click
 */

public class TimeDebounce {
    private long startTime, endTime;
    private boolean isStarted = false;

    /**
     * @Description Called right after click
     *               ->Sets timer
     */
    public void startDebounce(){
        startTime = System.currentTimeMillis();
        isStarted = true;
    }

    /**
     * @Description Check if timer is over
     * @return  false - 800ms still hasn't passed
     *          true  - debounce is over - new char can be inputed
     */        
    public boolean debounceOver(){
        if(isStarted) {
            if ((System.currentTimeMillis() - startTime) <= 800) {
                return false;
            }
        }
        isStarted = false;
        return true;
    }
    
    /**
     * @Description Check if timer is running
     * @return  isStarted
     */   
    public boolean isStarted() {
        return isStarted;
    }
    
    /**
     * @Description Getter for startTime
     * @return  startTime
     */   
    public long getStartTime() {
        return startTime;
    }
}
