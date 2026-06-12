package com.example.tickertide.model;

public class PriceAlert {

    public static final String TABLE_NAME = "alerts";
    public static final String COL_ID = "id";
    public static final String COL_SYMBOL = "symbol";
    public static final String COL_TARGET_PRICE = "target_price";
    public static final String COL_IS_ABOVE = "is_above"; // 1 if waiting for price to go above, 0 if below
    public static final String COL_IS_ACTIVE = "is_active";

    private int id;
    private String symbol;
    private double targetPrice;
    private boolean isAbove;
    private boolean isActive;

    public PriceAlert() {}

    public PriceAlert(String symbol, double targetPrice, boolean isAbove, boolean isActive) {
        this.symbol = symbol;
        this.targetPrice = targetPrice;
        this.isAbove = isAbove;
        this.isActive = isActive;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public double getTargetPrice() { return targetPrice; }
    public void setTargetPrice(double targetPrice) { this.targetPrice = targetPrice; }

    public boolean isAbove() { return isAbove; }
    public void setAbove(boolean above) { isAbove = above; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
