package com.example.accurancymobileapp.network.service;

import com.example.accurancymobileapp.response.VicoResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface VicoService {

    @GET("vico/evolucao-carteira")
    Call<VicoResponse> getEvolucao(
            @Query("periodo") String periodo
    );

    @GET("carteiras/distribuicao")
    Call<VicoResponse> getDistribuicao(
            @Query("id_carteira") Integer idCarteira
    );
}
