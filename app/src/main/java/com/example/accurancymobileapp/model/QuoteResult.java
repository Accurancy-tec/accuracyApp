package com.example.accurancymobileapp.model;

import com.google.gson.annotations.SerializedName;

public class QuoteResult {
    @SerializedName("requestedSymbol") private String requestedSymbol;
    @SerializedName("symbol") private String symbol;
    @SerializedName("changed") private boolean changed;
    @SerializedName("data") private QuoteData data;
    @SerializedName("name") private String name;
    @SerializedName("shortName") private String shortName;
    @SerializedName("longName") private String longName;
    @SerializedName("regularMarketVolume") private long volume;
    @SerializedName("regularMarketPrice") private double price;
    @SerializedName("regularMarketChangePercent") private double changePercent;
    @SerializedName("regularMarketTime") private String marketTime;
    @SerializedName("logourl") private String logoUrl;

    public String getRequestedSymbol() { return requestedSymbol; }
    public String getSymbol() { return symbol; }
    public boolean isChanged() { return changed; }
    public QuoteData getData() { return data; }

    public QuoteData toQuoteData() {
        if (data != null) return data;
        return new QuoteData(
                symbol,
                shortName != null ? shortName : name,
                longName != null ? longName : name,
                volume,
                price,
                changePercent,
                marketTime,
                logoUrl,
                "BRL"
        );
    }
}
