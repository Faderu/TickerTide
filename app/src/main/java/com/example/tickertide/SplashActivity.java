package com.example.tickertide;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

/**
 * SplashActivity - Activity pertama yang tampil saat aplikasi dibuka.
 * Menampilkan branding selama 2 detik, kemudian berpindah ke MainActivity
 * menggunakan Intent dan memanggil finish() agar tidak bisa kembali ke splash.
 */
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 2000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Handler untuk delay 2 detik sebelum pindah ke MainActivity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            // Panggil finish() agar user tidak bisa back ke splash screen
            finish();
        }, SPLASH_DELAY_MS);
    }
}
