package com.example.accurancymobileapp.network.repository;

import android.content.Context;
import android.util.Log;

import com.example.accurancymobileapp.model.Aporte;
import com.example.accurancymobileapp.model.clsAportes;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.network.service.ApiService;
import com.example.accurancymobileapp.response.ApiResponse;
import com.example.accurancymobileapp.response.AporteResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AporteRepository {
    private final ApiService apiService;

    public AporteRepository(Context context) {
        apiService = RetrofitClient.getClient(context).create(ApiService.class);
    }

    public void buscarAportes(int idCarteira, aporteCallback callback) {
        Log.d("TESTE_APORTE", "ID carteira enviado: " + idCarteira);
        apiService.buscarAportes(idCarteira).enqueue(new Callback<AporteResponse>() {
            @Override
            public void onResponse(Call<AporteResponse> call, Response<AporteResponse> response) {
                if (!response.isSuccessful()) {
                    callback.onErro("Erro HTTP: " + response.code());
                    return;
                }
                AporteResponse resultado = response.body();
                if (resultado == null) {
                    callback.onErro("Resposta da API vazia.");
                    return;
                }
                if (!resultado.isSucesso()) {
                    callback.onErro("A API não conseguiu buscar os aportes.");
                    return;
                }
                if (resultado.getAtivos() == null) {
                    Log.e("TESTE_APORTE", "A lista de aportes veio nula.");
                    callback.onErro("A lista de aportes não foi retornada.");
                    return;
                }
                callback.onSucesso(resultado.getAtivos());
            }

            @Override
            public void onFailure(Call<AporteResponse> call, Throwable t) {
                callback.onErro(mensagemErro(t));
            }
        });
    }

    public void registrarAporte(clsAportes aporte, RegistroCallback callback) {
        apiService.registerAporte(aporte).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (!response.isSuccessful()) {
                    callback.onErro("Erro HTTP ao registrar aporte: " + response.code());
                    return;
                }
                ApiResponse body = response.body();
                if (body == null) {
                    callback.onErro("A API retornou uma resposta vazia.");
                    return;
                }
                if (!body.isSucesso()) {
                    callback.onErro(body.getMensagem() == null ? "Não foi possível registrar a operação." : body.getMensagem());
                    return;
                }
                callback.onSucesso(body.getMensagem() == null ? "Operação registrada." : body.getMensagem());
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                callback.onErro(mensagemErro(t));
            }
        });
    }

    private String mensagemErro(Throwable t) {
        return t.getMessage() == null || t.getMessage().trim().isEmpty()
                ? "Falha de conexão com o servidor."
                : "Falha de conexão: " + t.getMessage();
    }

    public interface aporteCallback {
        void onSucesso(List<Aporte> aportes);
        void onErro(String mensagem);
    }

    public interface RegistroCallback {
        void onSucesso(String mensagem);
        void onErro(String mensagem);
    }
}
