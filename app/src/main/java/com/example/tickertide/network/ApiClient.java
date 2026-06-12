package com.example.tickertide.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

/**
 * ApiClient - Singleton Retrofit builder.
 *
 * Mengkonfigurasi:
 * - Base URL RapidAPI (Financial Modeling Prep)
 * - GsonConverterFactory untuk parsing JSON
 * - OkHttpClient dengan timeout dan logging interceptor
 *
 * Penggunaan:
 *   ApiService service = ApiClient.getApiService();
 *   service.getStockQuotes("AAPL,GOOGL").enqueue(callback);
 */
public class ApiClient {

    private static final String BASE_URL = "https://financial-modeling-prep.p.rapidapi.com/";

    private static Retrofit retrofit = null;
    private static ApiService apiServiceInstance = null;

    /**
     * Mendapatkan instance singleton Retrofit.
     * Lazy initialization - dibuat hanya saat pertama kali dipanggil.
     */
    private static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            // Setup logging interceptor (hanya tampil di DEBUG)
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Bangun OkHttpClient dengan timeout konfigurasi
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            // Bangun Retrofit instance
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();
        }
        return retrofit;
    }

    /**
     * Mendapatkan singleton instance ApiService.
     * Gunakan method ini untuk semua API calls.
     */
    public static ApiService getApiService() {
        if (apiServiceInstance == null) {
            apiServiceInstance = getRetrofitInstance().create(ApiService.class);
        }
        return apiServiceInstance;
    }

    // Private constructor - mencegah instantiasi dari luar
    private ApiClient() { }
}
