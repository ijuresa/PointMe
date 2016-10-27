package com.example.ivanj.pointme;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import UserData.User;
import Utilities.ActivityTags;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {
    private Button gButton;
    private EditText gUserName, gUserAge;
    private RadioGroup gRadioGroup;
    private RadioButton gBtnGender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Check if OpenCV is loaded
        if (!OpenCVLoader.initDebug()) {
            Log.e(ActivityTags.getActivity().getMain(), "OpenCv failed to initialize");
        } else {
            Log.d(ActivityTags.getActivity().getMain(), "OpenCv, initialized");
        }
        addListenerOnButton();
    }

    private void addListenerOnButton() {
        gButton = (Button)findViewById(R.id.btnSignIn);
        gUserName = (EditText)findViewById(R.id.txtInputName);
        gUserAge = (EditText)findViewById(R.id.txtInputAge);
        gRadioGroup = (RadioGroup)findViewById(R.id.radioGroup2);

        gButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    if(isFilled(gUserName)) {
                        User.getUser().setName(gUserName.getText().toString());
                        Log.i(ActivityTags.getActivity().getMain(), "UserName: " + User.getUser().getName());
                    } else {
                        RuntimeException e = new RuntimeException();
                        throw e;
                    }
                } catch(Exception e) {
                    Toast.makeText(MainActivity.this,"UserName is mandatory! ", Toast.LENGTH_LONG).show();
                    return;
                }

                try {
                    if(isFilled(gUserAge)) {
                        User.getUser().setAge(gUserAge.getText().toString());
                        Log.i(ActivityTags.getActivity().getMain(), "UserAge: " + User.getUser().getAge());
                    } else {
                        RuntimeException e = new RuntimeException();
                        throw e;
                    }
                } catch(Exception e) {
                    Toast.makeText(MainActivity.this,"Age is mandatory! ", Toast.LENGTH_LONG).show();
                    return;
                }

                int getRadioBtn = gRadioGroup.getCheckedRadioButtonId();
                gBtnGender = (RadioButton)findViewById(getRadioBtn);

                User.getUser().setGender(gBtnGender.getText().toString());
                Log.i(ActivityTags.getActivity().getMain(), "UserGender: " + User.getUser().getGender());

                //If everything is okay, start new ACTIVITY
                Intent _intent = new Intent(MainActivity.this, MenuActivity.class);
                MainActivity.this.startActivity(_intent);
            }
        });
    }

    //Check if EditText field isFilled, e.g it has something in it
    private boolean isFilled(EditText _editText) {
        if(_editText.getText().toString().isEmpty()) {
            return false;
        }
        return true;
    }
}
