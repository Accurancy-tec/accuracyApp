package com.example.accurancymobileapp.network.service;

import com.example.accurancymobileapp.response.ApiResponse;
import com.example.accurancymobileapp.response.LoginResponse;
import com.example.accurancymobileapp.model.User;
import com.example.accurancymobileapp.model.clsAportes;
import com.example.accurancymobileapp.response.QuoteResponse;
import com.example.accurancymobileapp.response.TickerResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

//Classe criada para enviar e buscar as informações do banco de dados atráves do php
public interface ApiService {

    @POST("user/registerNewUser.php")
    Call<ResponseBody> registerNewUser(@Body User user);

    @POST("user/login.php")
    Call<LoginResponse> loginVerification(@Body User user);

    @POST("user/aportes.php")
    Call<ApiResponse> registerAporte(@Body clsAportes aporte);

    @GET("user/dashboard.php")
    Call<ApiResponse> getAportes();

    @GET("quotes/getQuote.php")
    Call<QuoteResponse> getQuote(
            @Query("symbol") String symbol
    );

    @GET("v2/tickers")
    Call<TickerResponse> getService();
}
