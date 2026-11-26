package com.example.accurancymobileapp.network.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.accurancymobileapp.model.NovaCarteiraRequest;
import com.example.accurancymobileapp.model.Wallet;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.network.service.ApiService;
import com.example.accurancymobileapp.response.AdicionarSaldoResponse;
import com.example.accurancymobileapp.response.BuscarWalletsResponse;
import com.example.accurancymobileapp.response.CriarWalletResponse;
import com.example.accurancymobileapp.response.DepositoSimulacaoResponse;
import com.example.accurancymobileapp.response.RespostaSimples;
import com.example.accurancymobileapp.utils.ApiErros;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Chamadas de carteira e saldo. Os erros chegam com a mensagem que a API escreveu. */
public class WalletRepository {

    private static final String SEM_CONEXAO = "Sem conexão com o servidor. Verifique a internet e tente de novo.";

    private final ApiService apiService;

    public WalletRepository(Context context) {
        apiService = RetrofitClient.getClient(context).create(ApiService.class);
    }

    private static String erroHttp(Response<?> response, String padrao) {
        return ApiErros.mensagem(response.errorBody(), padrao + " (erro " + response.code() + ").");
    }

    public void criarNovaWallet(@NonNull NovaCarteiraRequest carteira, @NonNull WalletCallback callback) {
        apiService.criarNovaWallet(carteira).enqueue(new Callback<CriarWalletResponse>() {
            @Override
            public void onResponse(@NonNull Call<CriarWalletResponse> call, @NonNull Response<CriarWalletResponse> response) {
                CriarWalletResponse resultado = response.body();
                if (!response.isSuccessful() || resultado == null) {
                    callback.onError(erroHttp(response, "Não foi possível criar a carteira"));
                    return;
                }
                if (!resultado.isSuccess()) {
                    callback.onError(resultado.getMessage());
                    return;
                }
                callback.onSuccess(resultado.getMessage());
            }

            @Override
            public void onFailure(@NonNull Call<CriarWalletResponse> call, @NonNull Throwable t) {
                callback.onError(SEM_CONEXAO);
            }
        });
    }

    public void buscarWallets(@NonNull WalletListCallback callback) {
        apiService.buscarWallets().enqueue(new Callback<BuscarWalletsResponse>() {
            @Override
            public void onResponse(@NonNull Call<BuscarWalletsResponse> call, @NonNull Response<BuscarWalletsResponse> response) {
                BuscarWalletsResponse resultado = response.body();
                if (!response.isSuccessful() || resultado == null) {
                    callback.onError(erroHttp(response, "Não foi possível carregar as carteiras"));
                    return;
                }
                if (!resultado.isSuccess()) {
                    callback.onError(resultado.getMessage());
                    return;
                }
                callback.onSuccess(resultado.getCarteiras());
            }

            @Override
            public void onFailure(@NonNull Call<BuscarWalletsResponse> call, @NonNull Throwable t) {
                callback.onError(SEM_CONEXAO);
            }
        });
    }

    /** Carteira real com ativos não é excluída: a API responde 409 e a mensagem explica o motivo. */
    public void deletarWallet(int idCarteira, @NonNull WalletCallback callback) {
        apiService.excluirCarteira(idCarteira).enqueue(new Callback<RespostaSimples>() {
            @Override
            public void onResponse(@NonNull Call<RespostaSimples> call, @NonNull Response<RespostaSimples> response) {
                RespostaSimples resultado = response.body();
                if (!response.isSuccessful() || resultado == null) {
                    callback.onError(erroHttp(response, "Não foi possível excluir a carteira"));
                    return;
                }
                if (!resultado.ok()) {
                    callback.onError(resultado.texto("Não foi possível excluir a carteira."));
                    return;
                }
                callback.onSuccess(resultado.texto("Carteira excluída."));
            }

            @Override
            public void onFailure(@NonNull Call<RespostaSimples> call, @NonNull Throwable t) {
                callback.onError(SEM_CONEXAO);
            }
        });
    }

    /** Coloca dinheiro fictício numa carteira simulada; onSuccess recebe o novo saldo da simulação. */
    public void depositarSimulacao(int idCarteira, @NonNull BigDecimal valor, @NonNull WalletCallback callback) {
        Map<String, Object> corpo = new HashMap<>();
        corpo.put("id_carteira", idCarteira);
        corpo.put("valor", valor);
        apiService.depositarSimulacao(corpo).enqueue(new Callback<DepositoSimulacaoResponse>() {
            @Override
            public void onResponse(@NonNull Call<DepositoSimulacaoResponse> call,
                                   @NonNull Response<DepositoSimulacaoResponse> response) {
                DepositoSimulacaoResponse resultado = response.body();
                if (!response.isSuccessful() || resultado == null) {
                    callback.onError(erroHttp(response, "Não foi possível depositar na simulação"));
                    return;
                }
                if (!resultado.ok()) {
                    callback.onError(resultado.texto("Não foi possível depositar na simulação."));
                    return;
                }
                callback.onSuccess(String.valueOf(resultado.getSaldoSimulacao()));
            }

            @Override
            public void onFailure(@NonNull Call<DepositoSimulacaoResponse> call, @NonNull Throwable t) {
                callback.onError(SEM_CONEXAO);
            }
        });
    }

    public void adicionarSaldo(String valor, @NonNull WalletCallback callback) {
        apiService.adicionarSaldo(valor).enqueue(new Callback<AdicionarSaldoResponse>() {
            @Override
            public void onResponse(@NonNull Call<AdicionarSaldoResponse> call, @NonNull Response<AdicionarSaldoResponse> response) {
                AdicionarSaldoResponse resultado = response.body();
                if (!response.isSuccessful() || resultado == null) {
                    callback.onError(erroHttp(response, "Não foi possível adicionar saldo"));
                    return;
                }
                if (!resultado.isSuccess()) {
                    callback.onError(resultado.getMessage());
                    return;
                }
                callback.onSuccess(resultado.getSaldoAtual());
            }

            @Override
            public void onFailure(@NonNull Call<AdicionarSaldoResponse> call, @NonNull Throwable t) {
                callback.onError(SEM_CONEXAO);
            }
        });
    }

    /** Reads the current balance; onSuccess receives the amount as a plain decimal string. */
    public void buscarSaldo(@NonNull WalletCallback callback) {
        apiService.buscarSaldo().enqueue(new Callback<AdicionarSaldoResponse>() {
            @Override
            public void onResponse(@NonNull Call<AdicionarSaldoResponse> call, @NonNull Response<AdicionarSaldoResponse> response) {
                AdicionarSaldoResponse resultado = response.body();
                if (!response.isSuccessful() || resultado == null || !resultado.isSuccess()
                        || resultado.getSaldoAtual() == null) {
                    callback.onError("Não foi possível carregar o saldo.");
                    return;
                }
                callback.onSuccess(resultado.getSaldoAtual());
            }

            @Override
            public void onFailure(@NonNull Call<AdicionarSaldoResponse> call, @NonNull Throwable t) {
                callback.onError("Sem conexão para carregar o saldo.");
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
