package UserData;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import Utilities.ActivityTags;

/**
 * Created by ivanj on 16/01/2017.
 */

public class TestData {
    private String inputString;
    private char[] inputStringChar;
    private char[] currUserInputString;

    private char[] currOutputString;
    private List<Integer> wrongCharIndex = new ArrayList<Integer>();

    private static int statusWrong = 1;
    private static int statusCurrOk = 2;
    private static int statusFinOk = 3;

    private long startTime;
    private long endTime;
    private long timeSpent;
    private boolean isStarted = false;

    private int deleteGood = 0;
    private int deleteBad = 0;


    public TestData(String inputString) {
        this.inputString = inputString;
        this.inputStringChar = inputString.toCharArray();
    }

    public void startTimer() {
        if(!isStarted) {
            this.startTime = System.currentTimeMillis();
            isStarted = true;
        }
    }

    public void endTimer() {
        if(isStarted) {
            this.endTime = System.currentTimeMillis();
            isStarted = false;
        }
    }

    public void calculateTimeSpent() {
        timeSpent = endTime - startTime;
    }

    /**
     *
     * @param smallerString: String to be compared to input String
     * @return 1: Mistake
     * @return 2: No mistake, not finished
     * @return 2: No mistake, finished
     */
    public int checkNow(String smallerString) {
        int returnStatus = 0;
        wrongCharIndex.clear();

        char[] smallStringChar = smallerString.toCharArray();
        currUserInputString = smallStringChar;

        currOutputString = currUserInputString;

        Log.d(ActivityTags.getActivity().getColorBlobDetection(), "Text = " + currOutputString);

        for(int i = 0; i < smallStringChar.length; i++) {
            if(smallStringChar[i] != inputStringChar[i]) {
                wrongCharIndex.add(i);
                currOutputString[i] = '*';
                returnStatus = statusWrong;
            }else {
                returnStatus = statusCurrOk;
            }
        }
        if((returnStatus == statusCurrOk) && (smallStringChar.length == inputStringChar.length)) {
            returnStatus = statusFinOk;
            endTimer();
            calculateTimeSpent();
        }
        return returnStatus;
    }

    public void setBackspace() {
        //wrongCharIndex.get(wrongCharIndex.size())
    }

    public long getTimeSpent() {
        return timeSpent;
    }

    public String getCurrOutputString() {
        return currOutputString.toString();
    }
}
