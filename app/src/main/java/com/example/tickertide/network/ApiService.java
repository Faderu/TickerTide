package com.example.tickertide.network;

import com.example.tickertide.model.Stock;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

/**
 * ApiService - Interface endpoint Retrofit untuk Financial Modeling Prep via RapidAPI.
 *
 * Endpoint yang tersedia:
 * 1. getStockQuotes()     - Mendapatkan data quote beberapa saham sekaligus
 * 2. getStockProfile()    - Mendapatkan profil lengkap satu perusahaan
 * 3. getMarketGainers()   - Mendapatkan saham yang naik paling besar hari ini
 * 4. getMarketLosers()    - Mendapatkan saham yang turun paling besar hari ini
 *
 * Header wajib:
 * - x-rapidapi-host  : host RapidAPI
 * - x-rapidapi-key   : API key dari RapidAPI dashboard
 *
 * CATATAN: Ganti nilai RAPIDAPI_HOST dan RAPIDAPI_KEY di kelas ini sesuai
 *          dengan API key Anda dari https://rapidapi.com
 */
public interface ApiService {

    // Mengambil nilai host dan API key dari BuildConfig (local.properties)
    String RAPIDAPI_HOST = com.example.tickertide.BuildConfig.RAPIDAPI_HOST;
    String RAPIDAPI_KEY  = com.example.tickertide.BuildConfig.RAPIDAPI_KEY;

    /**
     * GET /v3/quote/{symbols}
     * Mendapatkan data quote real-time untuk satu atau beberapa simbol saham.
     * Contoh: symbols = "AAPL,GOOGL,MSFT"
     *
     * Response: List<Stock> (array JSON)
     */
    @GET("v3/quote/{symbols}")
    Call<List<Stock>> getStockQuotes(
            @retrofit2.http.Path("symbols") String symbols,
            @Header("x-rapidapi-host") String host,
            @Header("x-rapidapi-key") String apiKey
    );

    /**
     * GET /v3/quote/{symbol}
     * Mendapatkan profil/quote detail satu perusahaan.
     * Digunakan di StockDetailFragment.
     */
    @GET("v3/quote/{symbol}")
    Call<List<Stock>> getStockProfile(
            @retrofit2.http.Path("symbol") String symbol,
            @Header("x-rapidapi-host") String host,
            @Header("x-rapidapi-key") String apiKey
    );

    /**
     * GET /v3/stock_market/gainers
     * Mendapatkan daftar saham dengan kenaikan terbesar hari ini.
     */
    @GET("v3/stock_market/gainers")
    Call<List<Stock>> getMarketGainers(
            @Header("x-rapidapi-host") String host,
            @Header("x-rapidapi-key") String apiKey
    );

    /**
     * GET /v3/stock_market/losers
     * Mendapatkan daftar saham dengan penurunan terbesar hari ini.
     */
    @GET("v3/stock_market/losers")
    Call<List<Stock>> getMarketLosers(
            @Header("x-rapidapi-host") String host,
            @Header("x-rapidapi-key") String apiKey
    );

    /**
     * GET /v3/search
     * Pencarian saham berdasarkan query (nama atau simbol).
     */
    @GET("v3/search")
    Call<List<Stock>> searchStocks(
            @Query("query") String query,
            @Query("limit") int limit,
            @Header("x-rapidapi-host") String host,
            @Header("x-rapidapi-key") String apiKey
    );
}
