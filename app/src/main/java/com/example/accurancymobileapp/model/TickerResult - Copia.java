package com.example.accurancymobileapp.model;

import com.google.gson.annotations.SerializedName;

public class TickerResult {
    @SerializedName("symbol")
    private String symbol;

    @SerializedName("name")
    private String name;


    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }


}
