package com.example.accurancymobileapp.model;

import com.google.gson.annotations.SerializedName;

public class QuoteData {
    @SerializedName("symbol")
    private String symbol;

    @SerializedName("shortName")
    private String shortName;

    @SerializedName("regularMarketVolume")
    private int regularMarketVolume;

    @SerializedName("regularMarketPrice")
    private double regularMarketPrice;

    @SerializedName("regularMarketChangePercent")
    private double regularMarketChangePercent;

    public String getSymbol() {
        return symbol;
    }

    public String getShortName() {
        return shortName;
    }

    public int getRegularMarketVolume(){
        return regularMarketVolume;
    }

    public double getRegularMarketPrice() {
        return regularMarketPrice;
    }

    public double getRegularMarketChangePercent() {
        return regularMarketChangePercent;
    }
}
