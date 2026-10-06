package com.example.accurancymobileapp.response;

import com.example.accurancymobileapp.model.Wallet;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BuscarWalletsResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;
    @SerializedName("carteiras")
    private List<Wallet> carteiras;

    public boolean isSuccess(){
        return success;
    }

    public String getMessage(){
        return message;
    }

    public List<Wallet> getCarteiras(){
        return carteiras;
    }
}
