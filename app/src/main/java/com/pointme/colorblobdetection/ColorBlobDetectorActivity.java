package com.pointme.colorblobdetection;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.ivanj.pointme.R;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

//Documentation:
//CvCameraViewListener2: http://docs.opencv.org/java/3.0.0/org/opencv/android/CameraBridgeViewBase.CvCameraViewListener2.html

//Should be activity for user TEST application
public class ColorBlobDetectorActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_blob_detector);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        return null;
    }
}
