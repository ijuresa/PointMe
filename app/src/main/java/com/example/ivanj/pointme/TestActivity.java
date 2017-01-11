package com.example.ivanj.pointme;

import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.SurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.CameraBridgeViewBase;

import ColorBlobDetection.ColorBlobDetector;
import Utilities.ActivityTags;

public class TestActivity extends AppCompatActivity implements View.OnTouchListener,
        CameraBridgeViewBase.CvCameraViewListener2 {

    private Mat                  mRgba;
    private Scalar               mBlobColorRgba;
    private Scalar               mBlobColorHsv;
    private ColorBlobDetector    gBlobDetector;
    private Mat                  mSpectrum;
    private Size                 SPECTRUM_SIZE;
    private Scalar               CONTOUR_COLOR;

    //Rotation
    private Mat gRgbaF;
    private Mat gRbgaT;

    //Pointer variables
    private TranslateAnimation movePointer;
    ImageView pointerImage;
    private Point centerPoint = new Point(0,0);
    Point oldCenterPoint;
    private double areaThreshold = 0.25;

    private CameraBridgeViewBase mOpenCvCameraView;

    //Shared preferences file
    public static final String PREFERENCE_FILE = "PointMe";

    //Values from SharedPreferences
    double maxArea, minArea;


    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(ActivityTags.getActivity().getColorBlobDetection(),
                            "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    //mOpenCvCameraView.setOnTouchListener(TestActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_test);

        //Pointer image
        pointerImage = (ImageView)findViewById(R.id.imageViewPoint);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_blob_detection_activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setAlpha(0);

    }
    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(ActivityTags.getActivity().getColorBlobDetection(),
                    "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this, mLoaderCallback);
        } else {
            Log.d(ActivityTags.getActivity().getColorBlobDetection(),
                    "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }
    public void getData() {
        //Get data
        SharedPreferences pSettings = getSharedPreferences(PREFERENCE_FILE, 0);
        gBlobDetector.setMaxArea(pSettings.getInt("maxArea", 10));
        gBlobDetector.setMinArea(pSettings.getInt("minArea", 0));

        //Save HSV color from SharedPreferences to object
        for(int i = 0; i < 4; i ++) {
            mBlobColorHsv.val[i] = pSettings.getInt("hsvColor" + i, 0);
        }
        gBlobDetector.setHsvColor(mBlobColorHsv);
        gBlobDetector.setColorHSV();
        Toast.makeText(getApplicationContext(), "Tracking with color " + gBlobDetector.getColorHSV(),
                Toast.LENGTH_SHORT).show();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        gBlobDetector = new ColorBlobDetector();
        mSpectrum = new Mat();
        mBlobColorRgba = new Scalar(255);
        mBlobColorHsv = new Scalar(255);
        SPECTRUM_SIZE = new Size(200, 64);
        CONTOUR_COLOR = new Scalar(255,0,0,255);

        //Used for aligning screen
        gRgbaF = new Mat(height, width, CvType.CV_8UC4);
        gRbgaT = new Mat(height, width, CvType.CV_8UC4);

        getData();
        Imgproc.resize(gBlobDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    //Don't open, dead inside.
    public boolean onTouch(View v, MotionEvent event) {
        /*
        int cols = mRgba.cols();
        int rows = mRgba.rows();

        int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
        int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

        int x = (int)event.getX() - xOffset;
        int y = (int)event.getY() - yOffset;

        Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

        Rect touchedRect = new Rect();

        touchedRect.x = (x>4) ? x-4 : 0;
        touchedRect.y = (y>4) ? y-4 : 0;

        touchedRect.width = (x+4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
        touchedRect.height = (y+4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;

        Mat touchedRegionRgba = mRgba.submat(touchedRect);

        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

        // Calculate average color of touched region
        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width*touchedRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= pointCount;

        mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);

        Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " + mBlobColorRgba.val[1] +
                ", " + mBlobColorRgba.val[2] + ", " + mBlobColorRgba.val[3] + ")");

        gBlobDetector.setHsvColor(mBlobColorHsv);

        Imgproc.resize(gBlobDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

        touchedRegionRgba.release();
        touchedRegionHsv.release();

        return false; // don't need subsequent touch events
        */
        return false;
    }


    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        Log.i(ActivityTags.getActivity().getColorBlobDetection(), "mRgba " + mRgba);
        //Rotate screen by 90 degrees counter clockwise
        //TODO: CHECK this rotation, doesn't work always well
        Core.transpose(mRgba, gRbgaT);
        Imgproc.resize(gRbgaT, gRgbaF, gRgbaF.size(), 0, 0, 0);
        Core.flip(gRgbaF, mRgba, -1);

        gBlobDetector.process(mRgba);
        List<MatOfPoint> contours = gBlobDetector.getContours();
        Log.e(ActivityTags.getActivity().getColorBlobDetection(),
                "Contours count: " + contours.size());
        Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR);

        Mat colorLabel = mRgba.submat(4, 68, 4, 68);
        colorLabel.setTo(mBlobColorRgba);

        Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70, 70 + mSpectrum.cols());
        mSpectrum.copyTo(spectrumLabel);

        //Check if click is detected
        if(gBlobDetector.getContours().size() == 2) {
            double area1 = Imgproc.contourArea(gBlobDetector.getContours().get(0));
            double area2 = Imgproc.contourArea(gBlobDetector.getContours().get(1));

            //Create threshold values according to area1
            double lowerBound = area1 * 1 - areaThreshold;
            double upperBound = area1 * 1 + areaThreshold;

            //Click detected
            //TODO: Add actions
            if((area2 <= upperBound) || (area2 >= lowerBound)) {

            }
        } else {
            //Will be used to check if point has moved
            oldCenterPoint = centerPoint;
            centerPoint = gBlobDetector.getCenterPoint(centerPoint);
            Log.i(ActivityTags.getActivity().getColorBlobDetection(),"Koordinate tocke "
                    + centerPoint.x + " " + centerPoint.y);

            //Check if point has moved
            //If it has -> draw animation
            if((oldCenterPoint.x != centerPoint.x) || (oldCenterPoint.y != centerPoint.x)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        movePointer = new TranslateAnimation((float)oldCenterPoint.x,
                                (float)centerPoint.x, (float)oldCenterPoint.y, (float)centerPoint.y);
                        movePointer.setDuration(10);
                        movePointer.setFillAfter(true);

                        pointerImage.startAnimation(movePointer);
                    }
                });
            }

        }
        return mRgba;
    }

    private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }
}