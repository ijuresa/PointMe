package ColorBlobDetection;

/**
 * Created by ivanj on 26/10/2016.
 */

import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//OpenCV
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

//Local files
import Utilities.ActivityTags;


public class ColorBlobDetector {
    // Lower and Upper bounds for range checking in HSV color space
    private Scalar mLowerBound = new Scalar(0);
    private Scalar mUpperBound = new Scalar(0);

    // Minimum contour area in percent for contours filtering
    private static double mMinContourArea = 0.1;

    // Color radius for range checking in HSV color space
    private Scalar mColorRadius = new Scalar(25,50,50,0);
    private Mat mSpectrum = new Mat();
    private List<MatOfPoint> mContours = new ArrayList<MatOfPoint>();

    //Variables used for HSV threshold
    private static int MINHSV = 0;
    private static int SENSITIVITY = 15;
    private static int MAXHSV = 255;

    // Cache
    Mat mPyrDownMat = new Mat();
    Mat mHsvMat = new Mat();
    Mat mMask = new Mat();
    Mat mDilatedMask = new Mat();
    Mat mHierarchy = new Mat();

    public void setColorRadius(Scalar radius) {
        mColorRadius = radius;
    }

    /*
     * Fixed:  Problem with false random detection
     * Stayed: Problem with light detection from light bulb
     * Color is set as default
     */
    public void setHsvColor(Scalar hsvColor) {
        double minH = (hsvColor.val[0] >= mColorRadius.val[0]) ? hsvColor.val[0]- mColorRadius.val[0] : 0;
        double maxH = (hsvColor.val[0]+ mColorRadius.val[0] <= 255) ? hsvColor.val[0]+ mColorRadius.val[0] : 255;

        //TODO: Try to aprox.
        //mLowerBound.val[0] = minH;
        mLowerBound.val[0] = MINHSV;
        Log.i(ActivityTags.getActivity().getColorBlobDetection(), "mLowerBound val[0] = " + mLowerBound.val[0]);

        //mUpperBound.val[0] = maxH;
        mUpperBound.val[0] = MAXHSV;
        Log.i(ActivityTags.getActivity().getColorBlobDetection(), "mUpperBound val[0] = " + mUpperBound.val[0]);

        //mLowerBound.val[1] = hsvColor.val[1] - mColorRadius.val[1];
        mLowerBound.val[1] = MINHSV;
        Log.i(ActivityTags.getActivity().getColorBlobDetection(), "mLowerBound val[1] = " + mLowerBound.val[1]);

        //mUpperBound.val[1] = hsvColor.val[1] + mColorRadius.val[1];
        mUpperBound.val[1] = SENSITIVITY;
        Log.i(ActivityTags.getActivity().getColorBlobDetection(), "mUpperBound val[1] = " + mUpperBound.val[1]);

        //mLowerBound.val[2] = hsvColor.val[2] - mColorRadius.val[2];
        mLowerBound.val[2] = MAXHSV - SENSITIVITY;
        Log.i(ActivityTags.getActivity().getColorBlobDetection(), "mLowerBound val[2] = " + mLowerBound.val[2]);

        //mUpperBound.val[2] = hsvColor.val[2] + mColorRadius.val[2];
        mUpperBound.val[2] = MINHSV;
        Log.i(ActivityTags.getActivity().getColorBlobDetection(), "mUpperBound val[2] = " + mUpperBound.val[2]);

        mLowerBound.val[3] = MINHSV;
        mUpperBound.val[3] = MAXHSV;


        Mat spectrumHsv = new Mat(1, (int)(maxH-minH), CvType.CV_8UC3);

        for (int j = 0; j < maxH-minH; j++) {
            byte[] tmp = {(byte)(minH+j), (byte)255, (byte)255};
            spectrumHsv.put(0, j, tmp);
        }

        Imgproc.cvtColor(spectrumHsv, mSpectrum, Imgproc.COLOR_HSV2RGB_FULL, 4);
    }

    public Mat getSpectrum() {
        return mSpectrum;
    }

    public void setMinContourArea(double area) {
        mMinContourArea = area;
    }

    public void process(Mat rgbaImage) {
        Imgproc.pyrDown(rgbaImage, mPyrDownMat);
        Imgproc.pyrDown(mPyrDownMat, mPyrDownMat);

        Imgproc.cvtColor(mPyrDownMat, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);

        Core.inRange(mHsvMat, mLowerBound, mUpperBound, mMask);
        Imgproc.dilate(mMask, mDilatedMask, new Mat());

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Imgproc.findContours(mDilatedMask, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Find max contour area
        double maxArea = 0;
        Iterator<MatOfPoint> each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint wrapper = each.next();
            double area = Imgproc.contourArea(wrapper);
            if (area > maxArea)
                maxArea = area;
        }

        // Filter contours by area and resize to fit the original image size
        mContours.clear();
        each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint contour = each.next();
            if (Imgproc.contourArea(contour) > mMinContourArea*maxArea) {
                Core.multiply(contour, new Scalar(4,4), contour);
                mContours.add(contour);
            }
        }
    }

    public List<MatOfPoint> getContours() {
        return mContours;
    }
}
