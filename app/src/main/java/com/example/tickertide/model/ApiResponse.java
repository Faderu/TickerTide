package com.example.tickertide.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * ApiResponse - Model wrapper untuk menerima balikan JSON dari Retrofit.
 * Sesuai dengan format response dari Financial Modeling Prep / RapidAPI:
 *
 * Contoh response JSON:
 * [
 *   { "symbol": "AAPL", "name": "Apple Inc.", "price": 189.84, ... },
 *   { "symbol": "GOOGL", "name": "Alphabet Inc.", "price": 140.12, ... }
 * ]
 *
 * Atau bisa berupa object wrapper:
 * {
 *   "status": "OK",
 *   "results": [ { ... }, { ... } ]
 * }
 */
public class ApiResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("count")
    private int count;

    @SerializedName("results")
    private List<Stock> results;

    // -------------------------
    // Constructors
    // -------------------------

    public ApiResponse() { }

    public ApiResponse(String status, List<Stock> results) {
        this.status  = status;
        this.results = results;
        this.count   = results != null ? results.size() : 0;
    }

    // -------------------------
    // Getters & Setters
    // -------------------------

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public List<Stock> getResults() { return results; }
    public void setResults(List<Stock> results) {
        this.results = results;
        this.count   = results != null ? results.size() : 0;
    }

    public boolean isSuccess() {
        return "OK".equalsIgnoreCase(status) || results != null;
    }
}
