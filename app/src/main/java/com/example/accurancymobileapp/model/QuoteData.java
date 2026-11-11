package com.example.accurancymobileapp.model;

import com.google.gson.annotations.SerializedName;

public class QuoteData {
    @SerializedName("symbol") private String symbol;
    @SerializedName("shortName") private String shortName;
    @SerializedName("longName") private String longName;
    @SerializedName("regularMarketVolume") private Long regularMarketVolume;
    @SerializedName("regularMarketPrice") private Double regularMarketPrice;
    @SerializedName("regularMarketChangePercent") private Double regularMarketChangePercent;
    @SerializedName("regularMarketTime") private String regularMarketTime;
    @SerializedName("regularMarketOpen") private Double regularMarketOpen;
    @SerializedName("regularMarketDayHigh") private Double regularMarketDayHigh;
    @SerializedName("fiftyTwoWeekHigh") private Double fiftyTwoWeekHigh;
    @SerializedName("fiftyTwoWeekLow") private Double fiftyTwoWeekLow;
    @SerializedName("logourl") private String logoUrl;
    @SerializedName("currency") private String currency;

    public QuoteData() { }

    public QuoteData(String symbol, String shortName, String longName, Long volume,
                     Double price, Double changePercent, String marketTime,
                     String logoUrl, String currency) {
        this(symbol, shortName, longName, volume, price, changePercent, marketTime,
                logoUrl, currency, null, null, null, null);
    }

    public QuoteData(String symbol, String shortName, String longName, Long volume,
                     Double price, Double changePercent, String marketTime,
                     String logoUrl, String currency, Double regularMarketOpen,
                     Double regularMarketDayHigh, Double fiftyTwoWeekHigh,
                     Double fiftyTwoWeekLow) {
        this.symbol = symbol;
        this.shortName = shortName;
        this.longName = longName;
        this.regularMarketVolume = volume;
        this.regularMarketPrice = price;
        this.regularMarketChangePercent = changePercent;
        this.regularMarketTime = marketTime;
        this.logoUrl = logoUrl;
        this.currency = currency;
        this.regularMarketOpen = regularMarketOpen;
        this.regularMarketDayHigh = regularMarketDayHigh;
        this.fiftyTwoWeekHigh = fiftyTwoWeekHigh;
        this.fiftyTwoWeekLow = fiftyTwoWeekLow;
    }

    public String getSymbol() { return symbol; }
    public String getShortName() { return shortName; }
    public String getLongName() { return longName; }
    public Long getRegularMarketVolume() { return regularMarketVolume; }
    public Double getRegularMarketPrice() { return regularMarketPrice; }
    public Double getRegularMarketChangePercent() { return regularMarketChangePercent; }
    public String getRegularMarketTime() { return regularMarketTime; }
    public String getLogoUrl() { return logoUrl; }
    public String getCurrency() { return currency; }
    public Double getRegularMarketOpen() { return regularMarketOpen; }
    public Double getRegularMarketDayHigh() { return regularMarketDayHigh; }
    public Double getFiftyTwoWeekHigh() { return fiftyTwoWeekHigh; }
    public Double getFiftyTwoWeekLow() { return fiftyTwoWeekLow; }
}
