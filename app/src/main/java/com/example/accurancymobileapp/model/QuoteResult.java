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
    @SerializedName("regularMarketVolume") private Long volume;
    @SerializedName("regularMarketPrice") private Double price;
    @SerializedName("regularMarketChangePercent") private Double changePercent;
    @SerializedName("regularMarketTime") private String marketTime;
    @SerializedName("logourl") private String logoUrl;
    @SerializedName("currency") private String currency;

    public String getRequestedSymbol() { return requestedSymbol; }
    public String getSymbol() { return symbol; }
    public boolean isChanged() { return changed; }
    public QuoteData getData() { return data; }

    public QuoteData toQuoteData() {
        if (data != null) return data;
        String nomeCurto = shortName != null ? shortName : name;
        String nomeLongo = longName != null ? longName : name;
        return new QuoteData(symbol != null ? symbol : requestedSymbol,
                nomeCurto, nomeLongo, volume, price, changePercent, marketTime, logoUrl, currency);
    }
}
