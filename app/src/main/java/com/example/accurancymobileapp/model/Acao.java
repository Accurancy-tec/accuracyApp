package com.example.accurancymobileapp.model;

import com.google.gson.annotations.SerializedName;

public class Acao {
    @SerializedName(value = "stock", alternate = {"symbol"})
    private String stock;

    @SerializedName(value = "name", alternate = {"longName", "shortName"})
    private String name;

    @SerializedName(value = "close", alternate = {"price", "regularMarketPrice"})
    private Double close;


    @SerializedName(value = "change", alternate = {"regularMarketChangePercent"})
    private Double change;

    @SerializedName(value = "volume", alternate = {"regularMarketVolume"})
    private Long volume;

    @SerializedName(value = "logo", alternate = {"logourl", "logoUrl"})
    private String logo;

    public String getStock() {
        return stock;
    }

    public String getName() {
        return name;
    }

    public Double getChange() {
        return change;
    }

    public Double getClose() {
        return close;
    }

    public Long getVolume() {
        return volume;
    }

    public String getLogo() {
        return logo;
    }

}
