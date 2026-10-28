package com.example.accurancymobileapp.network.repository;

import android.content.Context;

import com.example.accurancymobileapp.response.AssetPositionResponse;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.network.service.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssetPositionRepository {

    private final ApiService apiService;

    public AssetPositionRepository(Context context) {
        apiService = RetrofitClient.getClient(context).create(ApiService.class);
    }

    public void getAssetPosition(String symbol, PositionCallback callback) {
        String ticker = symbol == null ? "" : symbol.trim();

        if (ticker.isEmpty()) {
            callback.onError("Ticker do ativo não informado.");
            return;
        }

        apiService.getAssetPosition(ticker).enqueue(
                new Callback<AssetPositionResponse>() {
                    @Override
                    public void onResponse(
                            Call<AssetPositionResponse> call,
                            Response<AssetPositionResponse> response
                    ) {
                        if (!response.isSuccessful()) {
                            callback.onError(
                                    "Erro HTTP ao consultar posição: "
                                            + response.code()
                            );
                            return;
                        }

                        AssetPositionResponse resultado = response.body();

                        if (resultado == null) {
                            callback.onError(
                                    "A API não retornou os dados da posição."
                            );
                            return;
                        }

                        if (!resultado.isSuccess()) {
                            String mensagem = resultado.getMessage();

                            callback.onError(
                                    mensagem == null || mensagem.trim().isEmpty()
                                            ? "Não foi possível consultar a posição do ativo."
                                            : mensagem
                            );
                            return;
                        }

                        callback.onSuccess(resultado);
                    }

                    @Override
                    public void onFailure(
                            Call<AssetPositionResponse> call,
                            Throwable erro
                    ) {
                        String mensagem = erro.getMessage();

                        callback.onError(
                                mensagem == null || mensagem.trim().isEmpty()
                                        ? "Não foi possível conectar à API para consultar a posição."
                                        : "Erro de conexão ao consultar posição: " + mensagem
                        );
                    }
                }
        );
    }

    public interface PositionCallback {
        void onSuccess(AssetPositionResponse position);

        void onError(String message);
    }
}