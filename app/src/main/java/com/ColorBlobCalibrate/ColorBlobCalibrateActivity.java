package com.ColorBlobCalibrate;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

//Local imports
import com.example.ivanj.pointme.R;
import ColorBlobDetection.ColorBlobDetector;
import Utilities.ActivityTags;

//OpenCV imports
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;

import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Size;

import java.util.List;

/**
* @Description Main Activity for color calibration
*
*              The logic of this activity is taken from OpenCV ColorBlob example. Originally it 
*              reacts to touch events. When particular part of *surface view* is touched it 
*              calculates its HSV average color for further tracking.
*                       
*              As we were working with IR light and not with regular visible light there were 
*              many problems, where many of them still persists. When camera sees IR light it is
*              already in visible spectrum so there isn’t any way for its isolation, so we can 
*              track it. Part of the solution is to put visible light blocker (diskette) so we can
*              eliminate unwanted light. 
*
*              Camera has its own problems. Depending on incoming light it auto calibrates white
*              balance, exposure compensation etc.... Unfortunately OpenCV works with old Camera
*              library and it doesn't have a lot of options. Best try is to lock *WhiteBalance*
*              and *ExposureCompensation* parameters.  
*
*              HSV color is set manually as: 
*                       private static int MINHSV = 0;
*                       private static int MAXHSV = 255;
*
*                       private int minHue = 216;
*                       private int maxHue = 244;
*
*                       private int minSat = 30;
*                       private int maxSat = 117;
*
*                       private int minVal = 118;
*                       private int maxVal = 255;
*
*              Values can be checked: https://en.wikipedia.org/wiki/HSL_and_HSV
*
*              When testing there still should be some light in the room but it shouldn’t 
*              be pointed directly to the camera as it messes with the tracking. 
*
*              Most of the code should be self-explanatory. 
*/   

public class ColorBlobCalibrateActivity extends AppCompatActivity implements View.OnTouchListener,
        CameraBridgeViewBase.CvCameraViewListener2 {

    private Button gButton;
    private CameraBridgeViewBase gOpenCvCameraView;
    private boolean gIsColorSelected = false;

    private Mat gRgba;
    private Mat gRgbaF;
    private Mat gRbgaT;

    private Mat gSpectrum;
    private Size SPECTRUM_SIZE;
    private Scalar CONTOUR_COLOR;

    private ColorBlobDetector gBlobDetector;

    private Scalar gBlobColorRgba;
    private Scalar gBlobColorHsv;

    //Shared preferences file
    public static final String PREFERENCE_FILE = "PointMe";

    //SeekBars - min and max blob area
    SeekBar seekBarMinArea, seekBarMaxArea;

    //Buttons
    Button buttonSave;
    boolean isTouched = false;

    //Check OpenCV status
    private BaseLoaderCallback gLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch(status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(ActivityTags.getActivity().getColorBlobDetection(),
                            "OpenCV Loaded Successfully");
                    gOpenCvCameraView.enableView();
                    gOpenCvCameraView.setOnTouchListener(ColorBlobCalibrateActivity.this);
                } break;
                default: {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(ActivityTags.getActivity().getColorBlobDetection(), "Called OnCreate");

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_color_blob_calibrate);

        //Restore preferences from previous sessions
        final SharedPreferences pSettings = getSharedPreferences(PREFERENCE_FILE, 0);
        final SharedPreferences.Editor pEditor = pSettings.edit();

        seekBarMinArea = (SeekBar)findViewById(R.id.seekBarMinArea);
        seekBarMaxArea = (SeekBar)findViewById(R.id.seekBarMaxArea);
        seekBarMaxArea.setProgress(pSettings.getInt("maxArea",15));
        seekBarMinArea.setProgress(pSettings.getInt("minArea",0));

        //SeekBar for minArea - minimal surface of the blob to be tracked
        seekBarMinArea.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean b) {
                gBlobDetector.setMinArea(progressValue);
                Log.i(ActivityTags.getActivity().getColorBlobDetection(), "Current Min: "
                        + progressValue);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //SeekBar for maxArea - maximal surface of the blob to be tracked
        seekBarMaxArea.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean b) {
                gBlobDetector.setMaxArea(progressValue);
                Log.i(ActivityTags.getActivity().getColorBlobDetection(), "Current Max: "
                        + progressValue);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //Botun - Saves preferences
        buttonSave = (Button) findViewById(R.id.buttonSave);

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Check if color is selected and save data to sharedPreferences
                if(isTouched) {
                    pEditor.putInt("maxArea", seekBarMaxArea.getProgress());
                    pEditor.putInt("minArea", seekBarMinArea.getProgress());

                    pEditor.putInt("hsvColor0", (int) gBlobColorHsv.val[0]);
                    pEditor.putInt("hsvColor1", (int) gBlobColorHsv.val[1]);
                    pEditor.putInt("hsvColor2", (int) gBlobColorHsv.val[2]);
                    pEditor.putInt("hsvColor3", (int) gBlobColorHsv.val[3]);

                    pEditor.commit();
                    if(pSettings.contains("maxArea")) Toast.makeText(getApplicationContext(),
                            "Saved", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Select Color.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        //Set camera to surface
        gOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_blob_detection_activity_surface_view);
        gOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        gOpenCvCameraView.setCvCameraViewListener(this);

        registerBtnEvents();
    }

    //Sign in button
    private void registerBtnEvents() {
        gButton = (Button)findViewById(R.id.btnSignIn);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        //TODO: This is on tablet, needs more testing
        int cols = gRgba.cols();
        int rows = gRgba.rows();
        Log.i(ActivityTags.getActivity().getColorBlobDetection(), "COLS = " + cols + " and ROWS = " + rows);


        int xOffset = (gOpenCvCameraView.getWidth() - cols) / 2;
        int yOffset = (gOpenCvCameraView.getHeight() - rows) / 2;
        Log.i(ActivityTags.getActivity().getColorBlobDetection(), "Offset X = " + xOffset +  " and Offset Y = " + yOffset);

        int x = (int)motionEvent.getX() - xOffset;
        int y = (int)motionEvent.getY() - yOffset;
        Log.i(ActivityTags.getActivity().getColorBlobDetection(), "Touch coordinates:" + x + " , " + y);

        Rect touchedRect = new Rect();

        touchedRect.x = (x > 4) ? (x - 4) : 0;
        touchedRect.y = (y > 4) ? (y - 4) : 0;
        Log.i(ActivityTags.getActivity().getColorBlobDetection(), "Rectangle coordinates: " + touchedRect.x + " , " + touchedRect.y);

        touchedRect.width = (x + 4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
        Log.i(ActivityTags.getActivity().getColorBlobDetection(), "Rect WIDTH: " + touchedRect.width);

        touchedRect.height = (y + 4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;
        Log.i(ActivityTags.getActivity().getColorBlobDetection(), "Rect HEIGHT: " + touchedRect.height);

        //Extracts a rectangular submatrix
        Mat touchedRegionRgba = gRgba.submat(touchedRect);

        Mat touchedRegionHsv = new Mat();
        //Converts image from one color space to another
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

        //Calculate average color of touched region
        //Sum of points by H S V values -> output 4 value array
        gBlobColorHsv = Core.sumElems(touchedRegionHsv);

        Log.i(ActivityTags.getActivity().getColorBlobDetection(), "gBlobColorHsv sum = " + gBlobColorHsv);

        //Get number of points from calculated rectangle
        int lPointCount = touchedRect.width * touchedRect.height;
        Log.i(ActivityTags.getActivity().getColorBlobDetection(), "Point Count = " + lPointCount);


        for(int i = 0; i < gBlobColorHsv.val.length; i ++) {
            gBlobColorHsv.val[i] /= lPointCount;
            Log.i(ActivityTags.getActivity().getColorBlobDetection(), i + " = " + gBlobColorHsv.val[i] + " Place");
        }

        //Convert back to RGB-a color
        gBlobColorRgba = converScalarHsv2Rgba(gBlobColorHsv);

        Log.i(ActivityTags.getActivity().getColorBlobDetection(), "Touched rgba color: (" + gBlobColorRgba.val[0] + ", " + gBlobColorRgba.val[1] +
                ", " + gBlobColorRgba.val[2] + ", " + gBlobColorRgba.val[3] + ")");

        //Save color for tracking
        gBlobDetector.setHsvColor(gBlobColorHsv);

        Imgproc.resize(gBlobDetector.getSpectrum(), gSpectrum, SPECTRUM_SIZE);
        Log.i(ActivityTags.getActivity().getColorBlobDetection(), "Spectrum!?!?: " + gBlobDetector.getSpectrum());
        Log.i(ActivityTags.getActivity().getColorBlobDetection(), "Color Contour!?!?: " + gBlobDetector.getContours());

        gIsColorSelected = true;

        touchedRegionRgba.release();
        touchedRegionHsv.release();

        isTouched = true;
        return false;
    }

    @Override
    public void onPause() {
        super.onPause();
        if(gOpenCvCameraView != null) gOpenCvCameraView.disableView();
        Log.i(ActivityTags.getActivity().getColorBlobDetection(), "Paused");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(ActivityTags.getActivity().getColorBlobDetection(),
                    "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this, gLoaderCallback);
        } else {
            Log.d(ActivityTags.getActivity().getColorBlobDetection(),
                    "OpenCV library found inside package. Using it!");
            gLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(gOpenCvCameraView != null) gOpenCvCameraView.disableView();
        Log.i(ActivityTags.getActivity().getColorBlobDetection(), "Destroyed");
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    //Camera
    ///////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onCameraViewStarted(int width, int height) {
        gRgba = new Mat(height, width, CvType.CV_8UC4);

        gBlobDetector = new ColorBlobDetector();

        gBlobColorHsv = new Scalar(255);
        gBlobColorRgba = new Scalar(255);

        gSpectrum = new Mat();

        SPECTRUM_SIZE = new Size(200, 64);
        CONTOUR_COLOR = new Scalar(255, 0, 0, 255);

        //Used for aligning screen
        gRgbaF = new Mat(height, width, CvType.CV_8UC4);
        gRbgaT = new Mat(height, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        gRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        gRgba = inputFrame.rgba();

        //Rotate screen by 90 degrees counter clockwise
        //TODO: CHECK this rotation, doesn't work always well
        Core.transpose(gRgba, gRbgaT);
        Imgproc.resize(gRbgaT, gRgbaF, gRgbaF.size(), 0, 0, 0);
        Core.flip(gRgbaF, gRgba, -1);

        if(gIsColorSelected) {
            gBlobDetector.process(gRgba);
            List<MatOfPoint> contours = gBlobDetector.getContours();
            Log.e(ActivityTags.getActivity().getColorBlobDetection(), "Contours count: " + contours.size());
            Imgproc.drawContours(gRgba, contours, -1, CONTOUR_COLOR);

            Mat colorLabel = gRgba.submat(4, 68, 4, 68);
            colorLabel.setTo(gBlobColorRgba);

            Mat spectrumLabel = gRgba.submat(4, 4 + gSpectrum.rows(), 70, 70 + gSpectrum.cols());
            gSpectrum.copyTo(spectrumLabel);
        }

        return gRgba;
    }

    private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat lPointMatRgba = new Mat();
        Mat lPointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(lPointMatHsv, lPointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(lPointMatRgba.get(0, 0));
    }
}

