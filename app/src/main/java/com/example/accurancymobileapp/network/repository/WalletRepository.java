package com.example.accurancymobileapp.network.repository;

import android.content.Context;
import android.util.Log;

import com.example.accurancymobileapp.model.Wallet;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.network.service.ApiService;
import com.example.accurancymobileapp.response.CriarWalletResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class WalletRepository {
    private ApiService apiService;

    public WalletRepository(Context context){
        Retrofit retrofit = RetrofitClient.getClient(context);
        apiService = retrofit.create(ApiService.class);
    }

    public void criarNovaWallet(String nomeWallet, String tipoWallet, WalletCallback callback){
        Wallet wallet = new Wallet(nomeWallet, tipoWallet);

        apiService.criarNovaWallet(wallet).enqueue(new Callback<CriarWalletResponse>() {
            @Override
            public void onResponse(Call<CriarWalletResponse> call, Response<CriarWalletResponse> response) {
                if(!response.isSuccessful()){
                    callback.onError("Erro HTTP: " + response.code());
                    return;
                }

                CriarWalletResponse resultado = response.body();

                Log.d("RESPONSE BODY", "Conteudo body: " + response.body());
                if(resultado == null){
                    callback.onError("Resposta da API vazia");
                    return;
                }

                if(!resultado.isSuccess()){
                    callback.onError(resultado.getMessage());
                    return;
                }

                callback.onSuccess(resultado.getMessage());

            }

            @Override
            public void onFailure(Call<CriarWalletResponse> call, Throwable t) {
                callback.onError("Erro de conexão: " + t.getMessage());
            }
        });
    }

    public interface WalletCallback{
        void onSuccess(String mensagem);
        void onError(String message);
    }
}
