package com.example.accurancymobileapp.response;

import com.example.accurancymobileapp.model.QuoteData;
import com.example.accurancymobileapp.model.QuoteResult;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class QuoteResponse {
    @SerializedName("results")
    private List<QuoteResult> results;

    public List<QuoteResult> getResults() {
        return results;
    }

    public List<QuoteData> getData() {
        List<QuoteData> dados = new ArrayList<>();
        if (results == null) return dados;
        for (QuoteResult result : results) {
            if (result != null && result.getData() != null) dados.add(result.toQuoteData());
        }
        return dados;
    }
}
