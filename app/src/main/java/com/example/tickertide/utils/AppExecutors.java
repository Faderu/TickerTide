package com.example.tickertide.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * AppExecutors - Utility class untuk mengelola thread pools.
 *
 * Menyediakan dua Executor:
 * 1. diskIO     : Single thread executor untuk operasi SQLite (baca/tulis DB)
 *                 SQLite tidak thread-safe untuk multiple writers, jadi gunakan
 *                 single thread agar tidak ada race condition.
 *
 * 2. mainThread : Executor yang menjalankan Runnable di Main (UI) Thread
 *                 Digunakan untuk mengupdate UI setelah operasi background selesai.
 *
 * Penggunaan standar:
 * <pre>
 *   AppExecutors executors = AppExecutors.getInstance();
 *
 *   // Jalankan operasi DB di background thread
 *   executors.diskIO().execute(() -> {
 *       List<Stock> stocks = dbHelper.getAllWatchlistStocks();
 *
 *       // Kembali ke main thread untuk update UI
 *       executors.mainThread().execute(() -> {
 *           adapter.submitList(stocks);
 *           loadingView.setVisibility(View.GONE);
 *       });
 *   });
 * </pre>
 */
public class AppExecutors {

    private static AppExecutors instance;

    private final Executor diskIO;
    private final Executor mainThread;

    /**
     * Mendapatkan singleton instance AppExecutors.
     */
    public static synchronized AppExecutors getInstance() {
        if (instance == null) {
            instance = new AppExecutors();
        }
        return instance;
    }

    private AppExecutors() {
        // Single thread untuk operasi SQLite - hindari race condition
        this.diskIO = Executors.newSingleThreadExecutor();

        // Main thread executor menggunakan Handler dari Looper.getMainLooper()
        this.mainThread = new MainThreadExecutor();
    }

    /**
     * Executor untuk operasi disk/database.
     * Gunakan ini untuk semua operasi SQLite.
     */
    public Executor diskIO() {
        return diskIO;
    }

    /**
     * Executor untuk operasi di Main (UI) Thread.
     * Gunakan ini untuk mengupdate UI setelah operasi background.
     */
    public Executor mainThread() {
        return mainThread;
    }

    /**
     * Inner class - implementasi Executor yang berjalan di Main Thread.
     */
    private static class MainThreadExecutor implements Executor {
        private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable command) {
            mainThreadHandler.post(command);
        }
    }
}
