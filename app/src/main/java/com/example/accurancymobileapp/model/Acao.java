package com.example.accurancymobileapp.model;

import com.google.gson.annotations.SerializedName;

public class Acao {
    @SerializedName("stock")
    private String stock;

    @SerializedName("name")
    private String name;

    @SerializedName("close")
    private Double close;

    @SerializedName("change")
    private Double change;

    @SerializedName("volume")
    private Long volume;

    @SerializedName("logo")
    private String logo;

    public String getStock() {
        return stock;
    }

    public String getName() {
        return name;
    }

    public Double getChange(){
        return change;
    }

    public Double getClose() {
        return close;
    }

    public Long getVolume() {
        return volume;
    }

    public String getLogo(){
        return logo;
    }

}
