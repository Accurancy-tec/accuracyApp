package com.example.accturancy.Api;

import com.example.accturancy.Classes.clsAportes;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

//Classe criada para enviar as informações do Aporte para o php
public interface ApiServicePost {

    @POST("insert.php")
    Call<ApiResponse> registerAporte(@Body clsAportes aporte);



}
