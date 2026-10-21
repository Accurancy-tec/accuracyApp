package com.example.accurancymobileapp.model;

import com.google.gson.annotations.SerializedName;

public class Acao {
    @SerializedName("stock")
    private String stock;

    @SerializedName("symbol")
    private String symbol;

    @SerializedName("shortName")
    private String shortName;

    @SerializedName("close")
    private Double close;

    @SerializedName("regularMarketPrice")
    private Double regularMarketPrice;
    @SerializedName("regularMarketChangePercent")
    private Double regularMarketChangePercent;

    @SerializedName("regularMarketVolume")
    private Long regularMarketVolume;

    @SerializedName("logourl")
    private String logourl;

    public String getStock() {
        return stock;
    }

    public String getSymbol(){
        return symbol;
    }

    public String getShortName() {
        return shortName;
    }

    public Double getClose() {
        return close;
    }

    public Double getRegularMarketChangePercent() {
        return regularMarketChangePercent;
    }

    public Long getRegularMarketVolume() {
        return regularMarketVolume;
    }

    public String getLogourl(){
        return logourl;
    }

    public Double getRegularMarketPrice(){
        return regularMarketPrice;
    }
}
