package com.example.accurancymobileapp.response;

import com.example.accurancymobileapp.model.TituloRendaFixa;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TitulosRendaFixaResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("titulos")
    private List<TituloRendaFixa> titulos;

    @SerializedName("message")
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public List<TituloRendaFixa> getTitulos() {
        return titulos;
    }

    public String getMessage() {
        return message;
    }
}
