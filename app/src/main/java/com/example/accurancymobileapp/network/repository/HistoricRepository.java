package com.example.accurancymobileapp.network.repository;

import android.content.Context;
import android.util.Log;

import com.example.accurancymobileapp.model.Aporte;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.network.service.ApiService;
import com.example.accurancymobileapp.response.AporteResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class HistoricRepository {
    private final ApiService apiService;

    public HistoricRepository(Context context) {
        apiService = RetrofitClient.getClient(context).create(ApiService.class);
    }

    public void buscarHistorico(HistoricCallback callback) {
        apiService.buscarHistorico().enqueue(new Callback<AporteResponse>() {
            @Override
            public void onResponse(Call<AporteResponse> call, Response<AporteResponse> response) {
                if (!response.isSuccessful()) {
                    callback.onError("Erro HTTP ao buscar histórico: " + response.code());
                    return;
                }
                AporteResponse resultado = response.body();
                if (resultado == null) {
                    callback.onError("Resposta da API vazia.");
                    return;
                }
                if (!resultado.isSucesso()) {
                    callback.onError("A API não conseguiu buscar o histórico.");
                    return;
                }
                if (resultado.getAtivos() == null) {
                    callback.OnSuccess(java.util.Collections.emptyList());
                    return;
                }
                callback.OnSuccess(resultado.getAtivos());
            }

            @Override
            public void onFailure(Call<AporteResponse> call, Throwable t) {
                Log.e("HISTORICO_APORTES", "Falha ao buscar histórico", t);
                callback.onError(t.getMessage() == null ? "Falha de conexão ao buscar histórico." : t.getMessage());
            }
        });
    }

    public interface HistoricCallback {
        void OnSuccess(List<Aporte> historico);
        void onError(String message);
    }
}
