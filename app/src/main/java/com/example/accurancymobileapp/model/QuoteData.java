package com.example.accurancymobileapp.model;

import com.google.gson.annotations.SerializedName;

public class QuoteData {
    @SerializedName("symbol")
    private String symbol;

    @SerializedName("shortName")
    private String shortName;

    @SerializedName("longName")
    private String longName;

    @SerializedName("regularMarketVolume")
    private long regularMarketVolume;

    @SerializedName("regularMarketPrice")
    private double regularMarketPrice;

    @SerializedName("regularMarketChangePercent")
    private double regularMarketChangePercent;

    @SerializedName("regularMarketTime")
    private String regularMarketTime;

    @SerializedName("logourl")
    private String logoUrl;

    @SerializedName("currency")
    private String currency;

    public QuoteData() { }

    public QuoteData(String symbol, String shortName, String longName, long volume,
                     double price, double changePercent, String marketTime,
                     String logoUrl, String currency) {
        this.symbol = symbol;
        this.shortName = shortName;
        this.longName = longName;
        this.regularMarketVolume = volume;
        this.regularMarketPrice = price;
        this.regularMarketChangePercent = changePercent;
        this.regularMarketTime = marketTime;
        this.logoUrl = logoUrl;
        this.currency = currency;
    }

    public String getSymbol() { return symbol; }
    public String getShortName() { return shortName; }
    public String getLongName() { return longName; }
    public long getRegularMarketVolume() { return regularMarketVolume; }
    public double getRegularMarketPrice() { return regularMarketPrice; }
    public double getRegularMarketChangePercent() { return regularMarketChangePercent; }
    public String getRegularMarketTime() { return regularMarketTime; }
    public String getLogoUrl() { return logoUrl; }
    public String getCurrency() { return currency; }
}
