package com.example.accurancymobileapp.network.repository;

import android.content.Context;

import com.example.accurancymobileapp.model.TituloRendaFixa;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.network.service.ApiService;
import com.example.accurancymobileapp.response.TitulosRendaFixaResponse;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RendaFixaRepository {

    private final ApiService apiService;

    public RendaFixaRepository(Context context){
        apiService = RetrofitClient.getClient(context).create(ApiService.class);
    }

    public interface RendaFixaCallback{
        void onSuccess(List<TituloRendaFixa> titulos);
        void onError(String message);
    }

    public void listarTitulosRendaFixa(RendaFixaCallback callback){

        apiService.listarTitulosRendaFixa().enqueue(new Callback<TitulosRendaFixaResponse>() {
            @Override
            public void onResponse(Call<TitulosRendaFixaResponse> call, Response<TitulosRendaFixaResponse> response) {
                if(!response.isSuccessful()){
                    callback.onError("Erro ao consultar os titulos. Código: " + response.code());
                    return;
                }

                TitulosRendaFixaResponse resultado = response.body();

                if(resultado == null){
                    callback.onError("A API retornou uma resposta vazia");
                    return;
                }

                if(!resultado.isSuccess()){
                    callback.onError(resultado.getMessage() != null ? resultado.getMessage() : "Não foi possível carregar os títulos");
                    return;
                }

                List<TituloRendaFixa> titulos = resultado.getTitulos();

                if(titulos == null){
                    titulos = Collections.emptyList();
                }

                callback.onSuccess(titulos);
            }

            @Override
            public void onFailure(Call<TitulosRendaFixaResponse> call, Throwable t) {
                callback.onError(
                        "Falha na conexão: "
                                + (t.getMessage() != null
                                ? t.getMessage()
                                : "erro desconhecido")
                );
            }
        });
    }
}
