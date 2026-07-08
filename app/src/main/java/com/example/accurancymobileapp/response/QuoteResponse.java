package com.example.accurancymobileapp.response;

import com.example.accurancymobileapp.model.Quote;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class QuoteResponse {

    @SerializedName("results")
    private List<Quote> results;

    public List<Quote> getResults(){
        return results;
    }
}
