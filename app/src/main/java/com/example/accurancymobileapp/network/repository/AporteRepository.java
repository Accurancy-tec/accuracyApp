package com.example.accurancymobileapp.network.repository;

import android.content.Context;

import com.example.accurancymobileapp.model.QuoteData;
import com.example.accurancymobileapp.model.clsAportes;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.network.service.ApiService;
import com.example.accurancymobileapp.response.ApiResponse;
import com.example.accurancymobileapp.response.QuoteResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AporteRepository {
    private ApiService apiService;

    public AporteRepository(Context context){
        Retrofit retrofit = RetrofitClient.getClient(context);

        apiService = retrofit.create(ApiService.class);
    }

    public void buscarAportes(aporteCallback callback){
        apiService.getAportes().enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if(response.isSuccessful() && response.body() != null){
                    ApiResponse result = response.body();

                    callback.onSucesso(result.getLista());
                }
                else {
                    callback.onErro("Erro ao buscar aitvos. Codigo:" + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                callback.onErro(t.getMessage());
            }
        });

    }



    public interface aporteCallback {
        void onSucesso(List<clsAportes> aportes);

        void onErro(String mensagem);
    }
}
