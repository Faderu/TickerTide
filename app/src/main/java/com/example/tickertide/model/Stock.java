package com.example.tickertide.model;

import com.google.gson.annotations.SerializedName;

/**
 * Stock - Model object (POJO) yang merepresentasikan satu data saham.
 * Digunakan sebagai:
 * 1. Entity untuk penyimpanan SQLite (tabel watchlist)
 * 2. Model data di dalam RecyclerView list
 *
 * Enkapsulasi dengan getter & setter.
 */
public class Stock {

    // Kolom untuk SQLite
    public static final String TABLE_NAME       = "watchlist";
    public static final String COL_ID           = "_id";
    public static final String COL_SYMBOL       = "symbol";
    public static final String COL_NAME         = "company_name";
    public static final String COL_PRICE        = "current_price";
    public static final String COL_CHANGE       = "change_percent";
    public static final String COL_HIGH         = "day_high";
    public static final String COL_LOW          = "day_low";
    public static final String COL_VOLUME       = "volume";
    public static final String COL_MARKET_CAP   = "market_cap";
    public static final String COL_TIMESTAMP    = "timestamp";

    // Field - mapped dari JSON via @SerializedName
    @SerializedName("symbol")
    private String symbol;

    @SerializedName("name")
    private String companyName;

    @SerializedName("price")
    private double currentPrice;

    @SerializedName("changesPercentage")
    private double changePercent;

    @SerializedName("dayHigh")
    private double dayHigh;

    @SerializedName("dayLow")
    private double dayLow;

    @SerializedName("volume")
    private double volume;

    @SerializedName("marketCap")
    private double marketCap;

    // Field tambahan untuk local DB tracking
    private long timestamp;

    // -------------------------
    // Constructors
    // -------------------------

    public Stock() {
        // Empty constructor diperlukan untuk Gson
    }

    public Stock(String symbol, String companyName, double currentPrice,
                 double changePercent, double dayHigh, double dayLow,
                 double volume, double marketCap) {
        this.symbol       = symbol;
        this.companyName  = companyName;
        this.currentPrice = currentPrice;
        this.changePercent = changePercent;
        this.dayHigh      = dayHigh;
        this.dayLow       = dayLow;
        this.volume       = volume;
        this.marketCap    = marketCap;
        this.timestamp    = System.currentTimeMillis();
    }

    // -------------------------
    // Getters & Setters
    // -------------------------

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }

    public double getChangePercent() { return changePercent; }
    public void setChangePercent(double changePercent) { this.changePercent = changePercent; }

    public double getDayHigh() { return dayHigh; }
    public void setDayHigh(double dayHigh) { this.dayHigh = dayHigh; }

    public double getDayLow() { return dayLow; }
    public void setDayLow(double dayLow) { this.dayLow = dayLow; }

    public double getVolume() { return volume; }
    public void setVolume(double volume) { this.volume = volume; }

    public double getMarketCap() { return marketCap; }
    public void setMarketCap(double marketCap) { this.marketCap = marketCap; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    /**
     * Cek apakah perubahan harga positif (naik)
     */
    public boolean isPositiveChange() {
        return changePercent >= 0;
    }

    @Override
    public String toString() {
        return "Stock{symbol='" + symbol + "', price=" + currentPrice +
               ", change=" + changePercent + "%}";
    }
}
