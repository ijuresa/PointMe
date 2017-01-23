package UserData;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import Utilities.ActivityTags;

/**
 * Created by ivanj on 16/01/2017.
 * @Description Class used to test if user input string is correct
 */

public class TestData {
    private String inputString;
    private char[] inputStringChar;

    private char[] currOutputString;
    //Index of every wrong character
    private List<Integer> wrongCharIndex = new ArrayList<Integer>();

    //Status messages
    private static int statusWrong = 1;
    private static int statusCurrOk = 2;
    private static int statusFinOk = 3;

    //Timer
    private long startTime;
    private long endTime;
    private long timeSpent;

    private boolean isStarted = false;

    //Index of the string
    private int index;

    //Backspace
    private int deleteGood = 0;
    private int deleteBad = 0;

    /**
     * @Description Constructor called when spinner sees new string pick
     * @param inputString String taken from spinner - will be used to check if user input string
     *                    is good
     * @param index Number marking index of the string - for later data analysis
     */
    public TestData(String inputString, int index) {
        this.index = index;
        this.inputString = inputString;
        this.inputStringChar = inputString.toCharArray();
    }

    /**
     * @Description When click is detected e.g. When user writes first letter to EditText this
     *              function starts timer
     */
    public void startTimer() {
        if(!isStarted) {
            this.startTime = System.currentTimeMillis();
            isStarted = true;
        }
    }

    /**
     * @Description Ends timer - User wrote string correctly
     */
    public void endTimer() {
        if(isStarted) {
            this.endTime = System.currentTimeMillis();
            isStarted = false;
        }
    }

    /**
     * @Description Calculates time spent writing string
     */
    public void calculateTimeSpent() {
        timeSpent = endTime - startTime;
    }

    /**
     * @Description Called each time EditText is changed
     *              It checks if smallerString == inputString
     *              Depending on outcome this method returns various messages
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
        currOutputString = smallStringChar;

        Log.d(ActivityTags.getActivity().getColorBlobDetection(), "InputString = " +
                smallerString);

        Log.d(ActivityTags.getActivity().getColorBlobDetection(), "NewPrint = " +
                String.valueOf(currOutputString));

        //Check each character
        for(int i = 0; i < smallStringChar.length; i++) {
            //If "i" is bigger then original string then for each character above it's length write
            //one '*' as it means character is wrong
            if(i >= inputStringChar.length) {
                wrongCharIndex.add(i);
                currOutputString[i] = '*';
                returnStatus = statusWrong;
            }
            //Normal case - check for current character accuracy - if it's wrong write '*'
            else if(smallStringChar[i] != inputStringChar[i]) {
                wrongCharIndex.add(i);
                currOutputString[i] = '*';
                returnStatus = statusWrong;
            }
            //Third case - when there are no errors in current string return statusCurrOk
            else {
                if(wrongCharIndex.isEmpty()) returnStatus = statusCurrOk;
            }
        }
        //Check if above for loop returned statusCurrOk and if original string equals user
        //input string in size - then end timer and calculate time
        if((returnStatus == statusCurrOk) && (smallStringChar.length == inputStringChar.length)) {
            returnStatus = statusFinOk;
            endTimer();
            calculateTimeSpent();
        }
        Log.d(ActivityTags.getActivity().getColorBlobDetection(), "NewPrint2222 = " +
                String.valueOf(currOutputString));

        //Return one of three possible outcomes
        return returnStatus;
    }

    /**
     * @Description Called when user writes "Backspace"
     */
    public void setBackspace() {
        //Check if string in EditText is empty return: nothing - error checking
        if(getCurrOutputString().isEmpty()) return;

        //Check if current string ends with '*' - from checkNow method and if it does then it means
        //deleted character is bad (e.g. we deleted wrong character) otherwise deleteGood++
        String temp = getCurrOutputString();
        if(temp.endsWith("*")) deleteBad ++;
        else deleteGood ++;

        Log.d(ActivityTags.getActivity().getColorBlobDetection(), "DeletedBad = " + deleteBad);
        Log.d(ActivityTags.getActivity().getColorBlobDetection(), "DeletedGood = " + deleteGood);
    }

    /**
     * @return Total number of backspaces
     */
    public int getBackspaces() {
        return deleteGood + deleteBad;
    }

    /**
     * @return Total number of wrong backspaces
     */
    public int getWrongBackspaces() {
        return deleteBad;
    }

    /**
     * @return Total time spent writing current string
     */
    public long getTimeSpent() {
        return timeSpent;
    }

    /**
     * @return Index of the current string
     */
    public int getIndex() {
        return index;
    }

    /**
     * @return Current string as char array (for EditText purposes)
     */
    public String getCurrOutputString() {
        return String.valueOf(currOutputString);
    }
}
