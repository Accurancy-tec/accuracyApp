package com.example.accurancymobileapp.network.service;

import com.example.accurancymobileapp.response.VicoResponse;


import retrofit2.Call;
import retrofit2.http.GET;

public interface VicoService {
    @GET("service/VicoService.php")
    Call<VicoResponse> getGrafic();
}
