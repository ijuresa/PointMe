package com.ColorBlobCalibrate;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.example.ivanj.pointme.R;
import Utilities.ActivityTags;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class ColorBlobCalibrateActivity extends AppCompatActivity implements View.OnTouchListener,
        CameraBridgeViewBase.CvCameraViewListener2 {

    private Button gButton;
    private CameraBridgeViewBase gOpenCvCameraView;

    private Mat gRgba;
    private Mat gRgbaF;
    private Mat gRbgaT;

    private Scalar gBlobColorRgba;
    private Scalar gBlobColorHsv;

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
        Core.transpose(gRgba, gRbgaT);
        Imgproc.resize(gRbgaT, gRgbaF, gRgbaF.size(), 0, 0, 0);
        Core.flip(gRgbaF, gRgba, -1);

        return gRgba;
    }
}
