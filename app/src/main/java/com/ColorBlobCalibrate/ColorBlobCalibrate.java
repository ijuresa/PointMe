package com.ColorBlobCalibrate;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import com.example.ivanj.pointme.R;

public class ColorBlobCalibrate extends AppCompatActivity {
    private Button gButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_blob_calibrate);

        registerBtnEvents();
    }

    private void registerBtnEvents() {
        gButton = (Button)findViewById(R.id.btnSignIn);
    }
}
