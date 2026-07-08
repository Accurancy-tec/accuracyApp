package com.example.accurancymobileapp.network.service;

import com.example.accurancymobileapp.response.ApiResponse;
import com.example.accurancymobileapp.response.LoginResponse;
import com.example.accurancymobileapp.model.User;
import com.example.accurancymobileapp.model.clsAportes;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

//Classe criada para enviar e buscar as informações do banco de dados atráves do php
public interface ApiService {

    @POST("user/registerNewUser.php")
    Call<ResponseBody> registerNewUser(@Body User user);

    @POST("user/login.php")
    Call<LoginResponse> loginVerification(@Body User user);

    @POST("user/insert.php")
    Call<ApiResponse> registerAporte(@Body clsAportes aporte);

    @GET("user/insert.php")
    Call<ApiResponse> getAportes();
}
