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
import android.app.Instrumentation;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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
    private ImageView pointerImage;
    private Point centerPoint = new Point(0,0);
    private Point oldCenterPoint;

    //For multiplication threshold
    private double areaThreshold = 0.25;

    private CameraBridgeViewBase mOpenCvCameraView;

    //Shared preferences file
    public static final String PREFERENCE_FILE = "PointMe";

    //Values from SharedPreferences
    double maxArea, minArea;


    final Instrumentation m_Instrumentation = new Instrumentation();

    View.OnClickListener globalButtonListener;


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

        //EditText
        EditText inputText = (EditText)findViewById(R.id.editText);
        //Don't show keyboard when text is in focus
        inputText.setShowSoftInputOnFocus(false);

        //Init Buttons
        initButtons();

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

    /**
     * @name getData
     * @description Read data from SharedPreferences file and save to object for later usage
     */
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

            //Click detected, two blobs of the aprox same size
            if((area2 <= upperBound) || (area2 >= lowerBound)) {
                //Obtain MotionEvent object
                //TODO: Remove when calibration will be done
                long downTime = SystemClock.uptimeMillis();
                long eventTime = SystemClock.uptimeMillis() + 100;

                //40 and 750 are temporary calibration values
                //TODO: Better automatic calibration -> Make user touch screen 4 points
                float x = (float)centerPoint.x + 40;
                float y = (float)centerPoint.y + 750;

               Log.d(ActivityTags.getActivity().getColorBlobDetection(), "Touch found at x: " +
               centerPoint.x + " and y: " + centerPoint.y);

                m_Instrumentation.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(),MotionEvent.ACTION_DOWN,x, y, 0));
                m_Instrumentation.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(),MotionEvent.ACTION_UP,x, y, 0));
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

    /**
     * @name pressedButtonThread
     * @param e: Integer containing KeyEvent code
     * @description Starts new thread which passes argument e as keystroke
     */
    void pressedButtonThread(final int e) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                m_Instrumentation.sendKeyDownUpSync(e);
            }
        }).start();
    }

    /**
     * @name initButtons
     * @description Initializes buttons which serve as keyboard
     *              Initializes OnClickListener for them
     */
    void initButtons() {
        globalButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case (R.id.buttonQ):
                            pressedButtonThread(KeyEvent.KEYCODE_Q);
                        break;

                    case (R.id.buttonW):
                            pressedButtonThread(KeyEvent.KEYCODE_W);
                        break;

                    case (R.id.buttonE):
                            pressedButtonThread(KeyEvent.KEYCODE_E);
                        break;

                    case (R.id.buttonR):
                            pressedButtonThread(KeyEvent.KEYCODE_R);
                        break;

                    case (R.id.buttonT):
                            pressedButtonThread(KeyEvent.KEYCODE_T);
                        break;

                    case (R.id.buttonY):
                            pressedButtonThread(KeyEvent.KEYCODE_Y);
                        break;

                    case (R.id.buttonU):
                            pressedButtonThread(KeyEvent.KEYCODE_U);
                        break;

                    case (R.id.buttonI):
                            pressedButtonThread(KeyEvent.KEYCODE_I);
                        break;

                    case (R.id.buttonO):
                            pressedButtonThread(KeyEvent.KEYCODE_O);
                        break;

                    case (R.id.buttonP):
                            pressedButtonThread(KeyEvent.KEYCODE_P);
                        break;

                    case (R.id.buttonA):
                            pressedButtonThread(KeyEvent.KEYCODE_A);
                        break;

                    case (R.id.buttonS):
                            pressedButtonThread(KeyEvent.KEYCODE_S);
                        break;

                    case (R.id.buttonD):
                            pressedButtonThread(KeyEvent.KEYCODE_D);
                        break;

                    case (R.id.buttonF):
                            pressedButtonThread(KeyEvent.KEYCODE_F);
                        break;

                    case (R.id.buttonG):
                            pressedButtonThread(KeyEvent.KEYCODE_G);
                        break;

                    case (R.id.buttonH):
                            pressedButtonThread(KeyEvent.KEYCODE_H);
                        break;

                    case (R.id.buttonJ):
                            pressedButtonThread(KeyEvent.KEYCODE_J);
                        break;

                    case (R.id.buttonK):
                            pressedButtonThread(KeyEvent.KEYCODE_K);
                        break;

                    case (R.id.buttonL):
                            pressedButtonThread(KeyEvent.KEYCODE_L);
                        break;

                    case (R.id.buttonZ):
                            pressedButtonThread(KeyEvent.KEYCODE_Z);
                        break;

                    case (R.id.buttonX):
                            pressedButtonThread(KeyEvent.KEYCODE_X);
                        break;

                    case (R.id.buttonC):
                            pressedButtonThread(KeyEvent.KEYCODE_C);
                        break;

                    case (R.id.buttonV):
                            pressedButtonThread(KeyEvent.KEYCODE_V);
                        break;

                    case (R.id.buttonB):
                            pressedButtonThread(KeyEvent.KEYCODE_B);
                        break;

                    case (R.id.buttonN):
                            pressedButtonThread(KeyEvent.KEYCODE_N);
                        break;

                    case (R.id.buttonM):
                            pressedButtonThread(KeyEvent.KEYCODE_M);
                        break;

                    case (R.id.buttonBcksp):
                            pressedButtonThread(KeyEvent.KEYCODE_DEL);
                        break;

                    case (R.id.buttonComma):
                            pressedButtonThread(KeyEvent.KEYCODE_COMMA);
                        break;

                    case (R.id.buttonSpace):
                            pressedButtonThread(KeyEvent.KEYCODE_SPACE);
                        break;

                    case (R.id.buttonDot):
                            pressedButtonThread(KeyEvent.KEYCODE_PERIOD);
                        break;
                    }
                }
            };

        //First row
        Button buttonQ = (Button)findViewById(R.id.buttonQ);
        buttonQ.setOnClickListener(globalButtonListener);

        Button buttonW = (Button)findViewById(R.id.buttonW);
        buttonW.setOnClickListener(globalButtonListener);

        Button buttonE = (Button)findViewById(R.id.buttonE);
        buttonE.setOnClickListener(globalButtonListener);

        Button buttonR = (Button)findViewById(R.id.buttonR);
        buttonR.setOnClickListener(globalButtonListener);

        Button buttonT = (Button)findViewById(R.id.buttonT);
        buttonT.setOnClickListener(globalButtonListener);

        Button buttonY = (Button)findViewById(R.id.buttonY);
        buttonY.setOnClickListener(globalButtonListener);

        Button buttonU = (Button)findViewById(R.id.buttonU);
        buttonU.setOnClickListener(globalButtonListener);

        Button buttonI = (Button)findViewById(R.id.buttonI);
        buttonI.setOnClickListener(globalButtonListener);

        Button buttonO = (Button)findViewById(R.id.buttonO);
        buttonO.setOnClickListener(globalButtonListener);

        Button buttonP = (Button)findViewById(R.id.buttonP);
        buttonP.setOnClickListener(globalButtonListener);

        //Second row
        Button buttonA = (Button)findViewById(R.id.buttonA);
        buttonA.setOnClickListener(globalButtonListener);

        Button buttonS = (Button)findViewById(R.id.buttonS);
        buttonS.setOnClickListener(globalButtonListener);

        Button buttonD = (Button)findViewById(R.id.buttonD);
        buttonD.setOnClickListener(globalButtonListener);

        Button buttonF = (Button)findViewById(R.id.buttonF);
        buttonF.setOnClickListener(globalButtonListener);

        Button buttonG = (Button)findViewById(R.id.buttonG);
        buttonG.setOnClickListener(globalButtonListener);

        Button buttonH = (Button)findViewById(R.id.buttonH);
        buttonH.setOnClickListener(globalButtonListener);

        Button buttonJ = (Button)findViewById(R.id.buttonJ);
        buttonJ.setOnClickListener(globalButtonListener);

        Button buttonK = (Button)findViewById(R.id.buttonK);
        buttonK.setOnClickListener(globalButtonListener);

        Button buttonL = (Button)findViewById(R.id.buttonL);
        buttonL.setOnClickListener(globalButtonListener);

        //Third row
        Button buttonZ = (Button)findViewById(R.id.buttonZ);
        buttonZ.setOnClickListener(globalButtonListener);

        Button buttonX = (Button)findViewById(R.id.buttonX);
        buttonX.setOnClickListener(globalButtonListener);

        Button buttonC = (Button)findViewById(R.id.buttonC);
        buttonC.setOnClickListener(globalButtonListener);

        Button buttonV = (Button)findViewById(R.id.buttonV);
        buttonV.setOnClickListener(globalButtonListener);

        Button buttonB = (Button)findViewById(R.id.buttonB);
        buttonB.setOnClickListener(globalButtonListener);

        Button buttonN = (Button)findViewById(R.id.buttonN);
        buttonN.setOnClickListener(globalButtonListener);

        Button buttonM = (Button)findViewById(R.id.buttonM);
        buttonM.setOnClickListener(globalButtonListener);

        Button buttonBcksp = (Button)findViewById(R.id.buttonBcksp);
        buttonBcksp.setOnClickListener(globalButtonListener);

        //Fourth row
        Button buttonComma = (Button)findViewById(R.id.buttonComma);
        buttonComma.setOnClickListener(globalButtonListener);

        Button buttonSpace = (Button)findViewById(R.id.buttonSpace);
        buttonSpace.setOnClickListener(globalButtonListener);

        Button buttonDot = (Button)findViewById(R.id.buttonDot);
        buttonQ.setOnClickListener(globalButtonListener);
    }

    private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }
}