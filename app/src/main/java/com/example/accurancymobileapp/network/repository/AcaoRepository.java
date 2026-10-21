package com.example.accurancymobileapp.network.repository;

import android.content.Context;

import com.example.accurancymobileapp.model.Acao;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.network.service.ApiService;
import com.example.accurancymobileapp.response.AcaoResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AcaoRepository {

    private ApiService apiService;

    public AcaoRepository(Context context){
        Retrofit retrofit = RetrofitClient.getClient(context);

        apiService = retrofit.create(ApiService.class);
    }

    public void listarAcoesNacionais(int pagina, int limiteDePaginas, acaoCallback callback){
        apiService.listarAcoesNacionais(pagina, limiteDePaginas).enqueue(new Callback<AcaoResponse>() {
            @Override
            public void onResponse(Call<AcaoResponse> call, Response<AcaoResponse> response) {
                if(!response.isSuccessful() && response.body() == null){
                    callback.onError("Erro HTTP: " + response.code());
                    return;
                }

                AcaoResponse resultado = response.body();

                if(resultado == null){
                    callback.onError("Reposta da API vazia");
                    return;
                }

                if(resultado.getAcoes() == null){
                    callback.onError("Lista de ações vazia");
                    return;
                }

                callback.onSuccess(resultado.getAcoes());
            }

            @Override
            public void onFailure(Call<AcaoResponse> call, Throwable t) {
                callback.onError("Erro de conxão: " + t.getMessage());
            }
        });
    }

    public interface acaoCallback{
        void onSuccess(List<Acao> acoes);
        void onError(String message);
    }
}
