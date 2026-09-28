package com.example.accurancymobileapp.response;

import com.example.accurancymobileapp.model.QuoteData;
import com.example.accurancymobileapp.model.QuoteResult;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class QuoteResponse {

    private List<QuoteResult> result;
    @SerializedName("results")
    private List<QuoteData> results;

    public List<QuoteResult> getResults(){
        return result;
    }

    public List<QuoteData> getData(){
        return results;
    }
}
