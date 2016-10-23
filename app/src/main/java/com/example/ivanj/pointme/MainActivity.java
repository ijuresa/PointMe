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

import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {
    User _user;

    //Set
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
            Log.e(this.getClass().getSimpleName(), "Not working");
        } else {
            Log.d(this.getClass().getSimpleName(), "It's Alive");
        }

        addListenerOnButton();
    }

    private void addListenerOnButton() {
        gButton = (Button)findViewById(R.id.btnSignIn);
        gUserName = ((EditText)findViewById(R.id.txtInputName));
        gUserAge = (EditText)findViewById(R.id.txtInputAge);
        gRadioGroup = (RadioGroup)findViewById(R.id.radioGroup2);

        gButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    if(isEmpty(gUserName)) {
                        _user.setName(String.valueOf(gUserName));
                    }
                } catch(Exception e) {
                    Toast.makeText(MainActivity.this,"UserName is mandatory! ", Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    if(isEmpty(gUserAge)) {
                        _user.setAge(String.valueOf(gUserAge));
                    }
                } catch(Exception e) {
                    Toast.makeText(MainActivity.this,"Age is mandatory! ", Toast.LENGTH_LONG).show();
                    return;
                }

                int getRadioBtn = gRadioGroup.getCheckedRadioButtonId();
                gBtnGender = (RadioButton)findViewById(getRadioBtn);
                //_user.setGender(gBtnGender.toString());

                //If everything is okay, start new ACTIVITY
                Intent _intent = new Intent(MainActivity.this, MenuActivity.class);
                MainActivity.this.startActivity(_intent);
            }
        });
    }


    private boolean isEmpty(EditText _editText) {
        return _editText.getText().toString().trim().length() == 0;
    }
}
