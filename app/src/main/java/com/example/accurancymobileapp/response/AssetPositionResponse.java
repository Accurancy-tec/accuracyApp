package com.example.accurancymobileapp.response;

import com.google.gson.annotations.SerializedName;

public class AssetPositionResponse {

    @SerializedName(value = "success", alternate = {"sucesso"})
    private boolean success;

    @SerializedName("symbol")
    private String symbol;

    @SerializedName(value = "quantidade", alternate = {"quantity"})
    private Double quantity;

    @SerializedName(value = "precoMedio", alternate = {"averagePrice"})
    private Double averagePrice;

    @SerializedName("hasPosition")
    private boolean hasPosition;

    @SerializedName(value = "mensagem", alternate = {"message"})
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public String getSymbol() {
        return symbol;
    }

    public Double getQuantity() {
        return quantity;
    }

    public Double getAveragePrice() {
        return averagePrice;
    }

    public boolean hasPosition() {
        return hasPosition;
    }

    public String getMessage() {
        return message;
    }
}