package com.example.accurancymobileapp.response;

import com.example.accurancymobileapp.model.QuoteData;
import com.example.accurancymobileapp.model.QuoteResult;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class QuoteResponse {

    @SerializedName("results")
    private List<QuoteResult> results;

    private List<QuoteData> result;

    public List<QuoteResult> getResults(){
        return results;
    }
}
