package com.example.accurancymobileapp.model;

import com.google.gson.annotations.SerializedName;

public class QuoteResult {
    @SerializedName("requestedSymbol")
    private String requestedSymbol;

    @SerializedName("symbol")
    private String symbol;

    @SerializedName("changed")
    private boolean changed;

    @SerializedName("data")
    private QuoteData data;

    public String getRequestedSymbol() {
        return requestedSymbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public boolean isChanged() {
        return changed;
    }

    public QuoteData getData() {
        return data;
    }
}
