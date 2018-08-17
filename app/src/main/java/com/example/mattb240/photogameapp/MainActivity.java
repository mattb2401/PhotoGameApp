package com.example.mattb240.photogameapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button signInbtn;
    Button signUpbtn;
    SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        signInbtn = (Button)findViewById(R.id.signin_btn);
        signUpbtn = (Button)findViewById(R.id.signup_btn);
        sharedPrefs = getSharedPreferences("photoGameApp", MODE_PRIVATE);
        if(sharedPrefs.getBoolean("isLoggedIn", false)){
            Intent feed = new Intent(MainActivity.this, Feed.class);
            startActivity(feed);
            finish();
        }

        signInbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signIn = new Intent(MainActivity.this, SignIn.class);
                startActivity(signIn);
                finish();
            }
        });

        signUpbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signUp = new Intent(MainActivity.this, SignUp.class);
                startActivity(signUp);
                finish();
            }
        });
    }
}
