package com.example.ivanj.pointme;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import UserData.User;

public class MenuActivity extends AppCompatActivity {
    Button gButtonTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        //Example how to get UserData
        String userName = User.getUser().getName();
        Log.d(userName, "User Name:");

        gButtonTest = (Button)findViewById(R.id.btnTest);

        gButtonTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent _intent = new Intent(MenuActivity.this, TestActivity.class);
                MenuActivity.this.startActivity(_intent);

            }
        });
    }
}
