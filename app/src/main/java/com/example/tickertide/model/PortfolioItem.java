package com.example.tickertide.model;

public class PortfolioItem {

    public static final String TABLE_NAME = "portfolio";
    public static final String COL_ID = "id";
    public static final String COL_SYMBOL = "symbol";
    public static final String COL_SHARES = "shares";
    public static final String COL_AVG_BUY_PRICE = "avg_buy_price";

    private String symbol;
    private double shares;
    private double avgBuyPrice;
    
    // Non-DB field for UI calculations
    private double currentPrice;
    private String companyName;

    public PortfolioItem() {}

    public PortfolioItem(String symbol, double shares, double avgBuyPrice) {
        this.symbol = symbol;
        this.shares = shares;
        this.avgBuyPrice = avgBuyPrice;
    }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public double getShares() { return shares; }
    public void setShares(double shares) { this.shares = shares; }

    public double getAvgBuyPrice() { return avgBuyPrice; }
    public void setAvgBuyPrice(double avgBuyPrice) { this.avgBuyPrice = avgBuyPrice; }

    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    // Helper methods for calculations
    public double getTotalValue() {
        return shares * currentPrice;
    }

    public double getTotalCost() {
        return shares * avgBuyPrice;
    }

    public double getProfitLoss() {
        return getTotalValue() - getTotalCost();
    }

    public double getProfitLossPercent() {
        if (getTotalCost() == 0) return 0;
        return (getProfitLoss() / getTotalCost()) * 100;
    }
}
