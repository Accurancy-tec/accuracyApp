package com.example.accurancymobileapp.network.repository;

import android.content.Context;

import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.network.service.ApiService;
import com.example.accurancymobileapp.response.AcaoResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AcaoRepository {

    private final ApiService apiService;
    private Call<AcaoResponse> chamadaAtual;

    public AcaoRepository(Context context) {
        Retrofit retrofit = RetrofitClient.getClient(context);
        apiService = retrofit.create(ApiService.class);
    }

    public void listarAcoesNacionais(
            int pagina,
            int limite,
            acaoCallback callback
    ) {
        listarAcoesNacionais(
                pagina,
                limite,
                null,
                null,
                null,
                "volume",
                "desc",
                callback
        );
    }

    public void listarAcoesNacionais(
            int pagina,
            int limite,
            String search,
            String sector,
            String subsector,
            String sortBy,
            String sortOrder,
            acaoCallback callback
    ) {
        if (chamadaAtual != null && !chamadaAtual.isCanceled()) {
            chamadaAtual.cancel();
        }

        Call<AcaoResponse> call = apiService.listarAcoesNacionais(
                pagina,
                limite,
                search,
                sector,
                subsector,
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
                    callback.onError("Erro HTTP ao listar ações: " + response.code());
                    return;
                }

                AcaoResponse resultado = response.body();
                if (resultado == null
                        || resultado.getAcoes() == null
                        || resultado.getPaginacao() == null) {
                    callback.onError("A API retornou dados incompletos das ações.");
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

                callback.onError(
                        erro.getMessage() == null
                                ? "Erro de conexão ao listar ações."
                                : "Erro de conexão: " + erro.getMessage()
                );
            }
        });
    }

    public interface acaoCallback {
        void onSuccess(AcaoResponse resultado);
        void onError(String message);
    }
}
