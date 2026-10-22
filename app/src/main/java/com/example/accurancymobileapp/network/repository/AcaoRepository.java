
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

    public AcaoRepository(Context context) {
        Retrofit retrofit = RetrofitClient.getClient(context);
        apiService = retrofit.create(ApiService.class);
    }

    public void listarAcoesNacionais(
            int pagina,
            int limite,
            acaoCallback callback
    ) {
        apiService.listarAcoesNacionais(pagina, limite)
                .enqueue(new Callback<AcaoResponse>() {

                    @Override
                    public void onResponse(
                            Call<AcaoResponse> call,
                            Response<AcaoResponse> response
                    ) {
                        if (!response.isSuccessful()) {
                            callback.onError(
                                    "Erro HTTP: " + response.code()
                            );
                            return;
                        }

                        AcaoResponse resultado = response.body();

                        if (resultado == null) {
                            callback.onError("Resposta da API vazia");
                            return;
                        }

                        if (resultado.getAcoes() == null) {
                            callback.onError(
                                    "A lista de ações não foi retornada"
                            );
                            return;
                        }

                        if (resultado.getPaginacao() == null) {
                            callback.onError(
                                    "Os dados de paginação não foram retornados"
                            );
                            return;
                        }

                        callback.onSuccess(resultado);
                    }

                    @Override
                    public void onFailure(
                            Call<AcaoResponse> call,
                            Throwable t
                    ) {
                        callback.onError(
                                "Erro de conexão: " + t.getMessage()
                        );
                    }
                });
    }

    public interface acaoCallback {
        void onSuccess(AcaoResponse resultado);
        void onError(String message);
    }
}
