package com.example.accurancymobileapp.network.repository;

import android.content.Context;

import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.network.service.ApiService;
import com.example.accurancymobileapp.response.AcaoResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FIIsRepository {
    private final ApiService apiService;

    public FIIsRepository(Context context) {
        apiService = RetrofitClient.getClient(context).create(ApiService.class);
    }

    public void listarFIIs(int pagina, int limite, FiiCallback callback) {
        apiService.listarFIIs(pagina, limite).enqueue(new Callback<AcaoResponse>() {
            @Override
            public void onResponse(Call<AcaoResponse> call, Response<AcaoResponse> response) {
                if (!response.isSuccessful()) {
                    callback.onError("Erro HTTP ao listar FIIs: " + response.code());
                    return;
                }

                AcaoResponse resultado = response.body();
                if (resultado == null) {
                    callback.onError("A API retornou uma resposta vazia ao listar FIIs.");
                    return;
                }
                if (resultado.getAcoes() == null || resultado.getPaginacao() == null) {
                    callback.onError("A API retornou dados incompletos dos FIIs.");
                    return;
                }

                callback.onSuccess(resultado);
            }

            @Override
            public void onFailure(Call<AcaoResponse> call, Throwable t) {
                String detalhe = t.getMessage();
                callback.onError(detalhe == null || detalhe.trim().isEmpty()
                        ? "Não foi possível conectar à API para carregar os FIIs."
                        : "Erro de conexão ao listar FIIs: " + detalhe);
            }
        });
    }

    public interface FiiCallback {
        void onSuccess(AcaoResponse resultado);
        void onError(String mensagem);
    }
}
