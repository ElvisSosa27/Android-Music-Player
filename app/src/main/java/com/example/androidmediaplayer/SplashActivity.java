package com.example.androidmediaplayer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ComponentActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        getSupportActionBar().hide();

        Handler handler = new Handler();
        Intent intent = new Intent(this, AllSongsActivity.class);
        handler.postDelayed(() -> {
            startActivity(intent);
            finish();
        }, 2000);
    }
}
