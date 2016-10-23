package com.example.ivanj.pointme;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(!OpenCVLoader.initDebug()) {
            Log.e(this.getClass().getSimpleName(), "Not working");
        } else {
            Log.d(this.getClass().getSimpleName(), "It's Alive");
        }
    }
}
