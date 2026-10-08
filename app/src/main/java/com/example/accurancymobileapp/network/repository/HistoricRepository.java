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
import retrofit2.Retrofit;

public class HistoricRepository {
    private ApiService apiService;

    public HistoricRepository(Context context){
        Retrofit retrofit = RetrofitClient.getClient(context);

        apiService = retrofit.create(ApiService.class);
    }

    public void buscarHistorico(HistoricCallback callback){
        apiService.buscarHistorico().enqueue(new Callback<AporteResponse>() {
            @Override
            public void onResponse(Call<AporteResponse> call, Response<AporteResponse> response) {
                if(!response.isSuccessful()){

                    callback.onError("Erro HTTP" + response.code());

                    return;
                }

                AporteResponse resultado = response.body();

                if(resultado == null){
                    callback.onError("Resposta da API vazia");
                    return;
                }

                if(resultado.getAtivos() == null){
                    callback.onError("Lista de histórico vazia.");
                    return;
                }

                callback.OnSuccess(resultado.getAtivos());

            }

            @Override
            public void onFailure(Call<AporteResponse> call, Throwable t) {
                Log.e(
                        "Teste Historico:",
                        "Falha" + t.getMessage()
                );
            }
        });

    }

    public interface HistoricCallback{
        void OnSuccess(List<Aporte> historico);
        void onError(String message);
    }
}
