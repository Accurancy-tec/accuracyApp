package com.example.accurancymobileapp.response;

import com.example.accurancymobileapp.model.TickerResult;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TickerResponse {
    @SerializedName("results")
    private List<TickerResult> results;

    public List<TickerResult> getResults(){
        return results;
    }
}
