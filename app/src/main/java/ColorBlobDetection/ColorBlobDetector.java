package ColorBlobDetection;

/**
 * Created by ivanj on 26/10/2016.
 */

import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

//OpenCV
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

//Local files
import Utilities.ActivityTags;

public class ColorBlobDetector {
    // Lower and Upper bounds for range checking in HSV color space
    private Scalar mLowerBound = new Scalar(0);
    private Scalar mUpperBound = new Scalar(0);

    // Color radius for range checking in HSV color space
    private Scalar mColorRadius = new Scalar(20,25,25,0);
    private Mat mSpectrum = new Mat();
    private List<MatOfPoint> mContours = new ArrayList<MatOfPoint>();

    //Variables used for HSV threshold
    private static int MINHSV = 0;
    private static int MAXHSV = 255;

    private int minHue = 216;
    private int maxHue = 244;

    private int minSat = 30;
    private int maxSat = 117;

    private int minVal = 118;
    private int maxVal = 255;

    // Cache
    Mat mPyrDownMat = new Mat();
    Mat mHsvMat = new Mat();
    Mat mMask = new Mat();
    Mat mDilatedMask = new Mat();
    Mat mHierarchy = new Mat();

    //Values from SEEK bar
    private double _minArea = 10, _maxArea = 910;
    private double _defaultArea = 10;

    private String stringHSV;

    public void setColorRadius(Scalar radius) {
        mColorRadius = radius;
    }

    /**
     * @Description Calculate spectrum of HSV color
     * @param hsvColor - HSV color which is used for later processing
     */
    public void setHsvColor(Scalar hsvColor) {
        double minH = (hsvColor.val[0] >= mColorRadius.val[0]) ? hsvColor.val[0]- mColorRadius.val[0] : 0;
        double maxH = (hsvColor.val[0]+ mColorRadius.val[0] <= 255) ? hsvColor.val[0]+ mColorRadius.val[0] : 255;

        mLowerBound.val[0] = minHue;
        Log.i(ActivityTags.getActivity().getColorBlobDetection(), "mLowerBound val[0] = " + mLowerBound.val[0]);

        mUpperBound.val[0] = maxHue;
        Log.i(ActivityTags.getActivity().getColorBlobDetection(), "mUpperBound val[0] = " + mUpperBound.val[0]);

        mLowerBound.val[1] = minSat;
        Log.i(ActivityTags.getActivity().getColorBlobDetection(), "mLowerBound val[1] = " + mLowerBound.val[1]);

        mUpperBound.val[1] = maxSat;
        Log.i(ActivityTags.getActivity().getColorBlobDetection(), "mUpperBound val[1] = " + mUpperBound.val[1]);

        mLowerBound.val[2] = minVal;
        Log.i(ActivityTags.getActivity().getColorBlobDetection(), "mLowerBound val[2] = " + mLowerBound.val[2]);

        mUpperBound.val[2] = maxVal;
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

    /**
     * @return Spectrum
     */
    public Mat getSpectrum() {
        return mSpectrum;
    }

    public void process(Mat rgbaImage) {
        Imgproc.pyrDown(rgbaImage, mPyrDownMat);
        Imgproc.pyrDown(mPyrDownMat, mPyrDownMat);

        Imgproc.cvtColor(mPyrDownMat, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL);

        Core.inRange(mHsvMat, mLowerBound, mUpperBound, mMask);
        Imgproc.dilate(mMask, mDilatedMask, new Mat());

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Imgproc.findContours(mDilatedMask, contours, mHierarchy, Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_SIMPLE);

        // Filter contours by area and resize to fit the original image size
        mContours.clear();

        // Find max contour area
        Iterator<MatOfPoint> each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint contour = each.next();
            if ((Imgproc.contourArea(contour) >= _minArea) && (Imgproc.contourArea(contour)
                    <= _maxArea)) {
                Core.multiply(contour, new Scalar(4,4), contour);

                mContours.add(contour);
                Vector<Point> kontura;
                kontura = new Vector<Point>();

                Converters.Mat_to_vector_Point(contour,kontura);
            }
        }
    }

    /**
     * @return List consisting of contours
     */
    public List<MatOfPoint> getContours() {
        return mContours;
    }

    /**
     * @Description Set minArea for color blob detection
     * @param minArea Current minArea - taken from seekBar
     */
    public void setMinArea(double minArea) {
        _minArea = _defaultArea + (minArea * 10);
    }

    /**
     * @Description Set maxArea for color blob detection
     * @param maxArea Current axArea - taken from seekBar
     */
    public void setMaxArea(double maxArea) {
        _maxArea = _defaultArea + (maxArea * 50);
    }

    /**
     * @Description Same HSV color to string - for sharedPreference file
     */
    public void setColorHSV() {
        for(int i = 0; i < 4; i ++) {
            stringHSV += mColorRadius.val[i] + ",";
        }
    }

    /**
     * @return String with HSV values
     */
    public String getColorHSV() {
        return stringHSV;
    }

    /**
     * @Description Used to find center of blob which is used for tracking
     * @param oldDot Takes in oldDot in case there are more then 1 contours
     * @return oldDot - when there are more then 1 contours
     *         currDot - new dot calculated
     */
    public Point getCenterPoint(Point oldDot) {
        Vector<Point> currContour = new Vector<Point>();

        double currContourSumX = 0;
        double currContourSumY = 0;

        Point currDot = new Point(0, 0);

        //Check if there are more contours then desired/needed - if there are - return oldDot
        //Ultimately it means that pointer won't move
        if(mContours.size() > 1) {
            return oldDot;
        }
        //If there are no contours - also return oldDot
        else if(mContours.isEmpty()) return oldDot;

        //Get only contour from list and save it to vector of Points
        Converters.Mat_to_vector_Point(mContours.get(0),currContour);

        //Sum all x and y points
        for(int i = 0; i < currContour.size(); i ++) {
            currContourSumX += currContour.get(i).x;
            currContourSumY += currContour.get(i).y;
            //Log.i(ActivityTags.getActivity().getColorBlobDetection(), "Tocka: " + currContour.get(i));
        }
        //Get average values
        currContourSumX /= currContour.size();
        currContourSumY /= currContour.size();

        //Add averaged X and Y to new dot
        currDot.x = (int)currContourSumX;
        currDot.y = (int)currContourSumY;

        return currDot;
    }
}
