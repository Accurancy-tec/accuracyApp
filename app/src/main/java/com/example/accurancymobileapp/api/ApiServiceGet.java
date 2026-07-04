package com.example.accurancymobileapp.api;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiServiceGet {
    @GET("insert.php")
    Call<ApiResponse> getAportes();
}
