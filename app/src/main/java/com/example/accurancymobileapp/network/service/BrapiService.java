package com.example.accurancymobileapp.network.service;

import com.example.accurancymobileapp.response.QuoteResponse;
import com.example.accurancymobileapp.response.TickerResponse;

import retrofit2.http.GET;
import retrofit2.Call;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BrapiService {
    @GET("v2/stocks/quote")
    Call<QuoteResponse> getQuote(
            @Query("symbols") String symbols
    );

    @GET("v2/tickers")
    Call<TickerResponse> getService();
}
