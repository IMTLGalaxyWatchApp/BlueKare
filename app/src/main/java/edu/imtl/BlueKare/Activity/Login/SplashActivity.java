package edu.imtl.BlueKare.Activity.Login;

import androidx.appcompat.app.AppCompatActivity;


import edu.imtl.BlueKare.R;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(() -> {
            Intent i = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        }, 3*1000); // wait for 5 seconds
    }
}