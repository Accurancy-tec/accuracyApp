package com.example.accurancymobileapp.network.repository;

import android.content.Context;
import android.util.Log;

import com.example.accurancymobileapp.model.Wallet;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.network.service.ApiService;
import com.example.accurancymobileapp.response.ApiResponse;
import com.example.accurancymobileapp.response.BuscarWalletsResponse;
import com.example.accurancymobileapp.response.CriarWalletResponse;

import java.math.BigDecimal;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class WalletRepository {

    private final ApiService apiService;

    public WalletRepository(Context context) {

        Retrofit retrofit = RetrofitClient.getClient(context);

        apiService = retrofit.create(ApiService.class);
    }

    public void criarNovaWallet(String nomeWallet, String tipoWallet, BigDecimal metaWallet, String estiloWallet, WalletCallback callback) {

        Wallet wallet = new Wallet(nomeWallet, tipoWallet, metaWallet, estiloWallet);

        apiService.criarNovaWallet(wallet).enqueue(new Callback<CriarWalletResponse>() {

            @Override public void onResponse(Call<CriarWalletResponse> call, Response<CriarWalletResponse> response) {

                if (!response.isSuccessful()) {

                    callback.onError("Erro HTTP: " + response.code());
                    return;
                }

                CriarWalletResponse resultado = response.body();

                if (resultado == null) {

                    callback.onError("Resposta da API vazia");
                    return;
                }

                if (!resultado.isSuccess()) {

                    Log.e("CARTEIRA", "API retornou erro. Mensagem: " + resultado.getMessage());

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

    public void buscarWallets(WalletListCallback callback) {

        apiService.buscarWallets().enqueue(new Callback<BuscarWalletsResponse>() {

            @Override
            public void onResponse(Call<BuscarWalletsResponse> call, Response<BuscarWalletsResponse> response) {

                if (!response.isSuccessful()) {

                    callback.onError(
                            "Erro HTTP: " + response.code());
                    return;
                }

                BuscarWalletsResponse resultado = response.body();

                if (resultado == null) {
                    callback.onError("Resposta da API vazia");

                    return;
                }

                if (!resultado.isSuccess()) {

                    callback.onError(resultado.getMessage());

                    return;
                }

                callback.onSuccess(resultado.getCarteiras());
            }

            @Override public void onFailure(Call<BuscarWalletsResponse> call,Throwable t) {

                callback.onError("Erro de conexão: " + t.getMessage()
                );
            }
        }
        );
    }

    public void deletarWallet(int idCarteira, WalletCallback callback){
        apiService.excluirCarteira(idCarteira).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                if(!response.isSuccessful()){
                    callback.onError("Erro HTTP: " + response.code());
                    return;
                }

                ApiResponse resultado = response.body();
                String mensagem = resultado.getMensagem();

                if(resultado == null){
                    callback.onError("Reposta da API vazia.");
                    return;
                }

                callback.onSuccess(resultado.getMensagem());
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                callback.onError("Erro ao excluir a carteira." + t.getMessage());
            }
        });

    }

    public interface WalletCallback {

        void onSuccess(String mensagem);

        void onError(String message);
    }

    public interface WalletListCallback {

        void onSuccess(List<Wallet> wallets);

        void onError(String message);
    }
}