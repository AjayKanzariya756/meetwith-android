package com.example.zoom_clone.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zoom_clone.R;
import com.example.zoom_clone.data.FirebaseManager;
import com.example.zoom_clone.ui.auth.LoginActivity;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseManager fm = FirebaseManager.getInstance();
            if (fm.isLoggedIn()) {
                fm.fetchSelfData().addOnCompleteListener(task -> {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                });
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            }
        }, 1500);
    }
}
