package com.example.accurancymobileapp.network.service;

import com.example.accurancymobileapp.response.QuoteResponse;

import retrofit2.http.GET;
import retrofit2.Call;
import retrofit2.http.Path;

public interface BrapiService {
    @GET("quote/{ticker}")
    Call<QuoteResponse> getQuote(
            @Path("ticker") String ticker
    );

    @GET("v2/tickers")
    Call<QuoteResponse> getService();
}
