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
    User _user = new User();

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
        gUserName = (EditText)findViewById(R.id.txtInputName);
        gUserAge = (EditText)findViewById(R.id.txtInputAge);
        gRadioGroup = (RadioGroup)findViewById(R.id.radioGroup2);


        gButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    if(isFilled(gUserName)) {
                        _user.setName(gUserName.getText().toString());

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
                        _user.setAge(gUserAge.getText().toString());
                        Log.d(_user.getAge(), "User Age:");
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
                _user.setGender(gBtnGender.getText().toString());
                Log.d(_user.getGender(), "User Gender:");

                //If everything is okay, start new ACTIVITY
                Intent _intent = new Intent(MainActivity.this, MenuActivity.class);
                _intent.putExtra("UserObject", _user);
                MainActivity.this.startActivity(_intent);
            }
        });
    }


    private boolean isFilled(EditText _editText) {
        if(_editText.getText().toString().isEmpty()) {
            return false;
        }
        return true;
    }
}
