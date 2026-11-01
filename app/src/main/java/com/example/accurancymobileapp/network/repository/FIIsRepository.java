package com.example.accurancymobileapp.network.repository;

import android.content.Context;

import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.network.service.ApiService;
import com.example.accurancymobileapp.response.AcaoResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FIIsRepository {

    private final ApiService apiService;
    private Call<AcaoResponse> chamadaAtual;

    public FIIsRepository(Context context) {
        apiService = RetrofitClient.getClient(context).create(ApiService.class);
    }

    public void listarFIIs(int pagina, int limite, FiiCallback callback) {
        listarFIIs(
                pagina,
                limite,
                null,
                null,
                "totalInvestors",
                "desc",
                callback
        );
    }

    public void listarFIIs(
            int pagina,
            int limite,
            String search,
            String segmentType,
            String sortBy,
            String sortOrder,
            FiiCallback callback
    ) {
        if (chamadaAtual != null && !chamadaAtual.isCanceled()) {
            chamadaAtual.cancel();
        }

        Call<AcaoResponse> call = apiService.listarFIIs(
                pagina,
                limite,
                search,
                segmentType,
                sortBy,
                sortOrder
        );
        chamadaAtual = call;

        call.enqueue(new Callback<AcaoResponse>() {
            @Override
            public void onResponse(
                    Call<AcaoResponse> currentCall,
                    Response<AcaoResponse> response
            ) {
                if (currentCall.isCanceled()) {
                    return;
                }
                if (chamadaAtual == currentCall) {
                    chamadaAtual = null;
                }

                if (!response.isSuccessful()) {
                    callback.onError("Erro HTTP ao listar FIIs: " + response.code());
                    return;
                }

                AcaoResponse resultado = response.body();
                if (resultado == null
                        || resultado.getAcoes() == null
                        || resultado.getPaginacao() == null) {
                    callback.onError("A API retornou dados incompletos dos FIIs.");
                    return;
                }

                callback.onSuccess(resultado);
            }

            @Override
            public void onFailure(Call<AcaoResponse> currentCall, Throwable erro) {
                if (currentCall.isCanceled()) {
                    return;
                }
                if (chamadaAtual == currentCall) {
                    chamadaAtual = null;
                }

                String mensagem = erro.getMessage();
                callback.onError(
                        mensagem == null || mensagem.trim().isEmpty()
                                ? "Não foi possível conectar à API para carregar os FIIs."
                                : "Erro de conexão ao listar FIIs: " + mensagem
                );
            }
        });
    }

    public interface FiiCallback {
        void onSuccess(AcaoResponse resultado);
        void onError(String mensagem);
    }
}
