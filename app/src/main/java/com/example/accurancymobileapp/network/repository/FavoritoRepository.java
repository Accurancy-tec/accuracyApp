package com.example.accurancymobileapp.network.repository;

import android.content.Context;

import com.example.accurancymobileapp.model.FavoritoAtivo;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.network.service.ApiService;
import com.example.accurancymobileapp.response.FavoritosResponse;
import com.example.accurancymobileapp.utils.FavoritosCache;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Loads and changes the user's favorite assets (optimistic updates with rollback). */
public class FavoritoRepository {

    public interface ListaCallback {
        void onSuccess(List<FavoritoAtivo> favoritos);

        void onError(String mensagem);
    }

    public interface AlternarCallback {
        /** Called right away with the new state (optimistic). */
        void onResultado(boolean favorito);

        /** The server refused or was unreachable; the state went back to what it was. */
        void onError(boolean estadoAnterior, String mensagem);
    }

    // Keys currently being sent, so a double tap does not send two opposite requests.
    private static final Set<String> EM_ANDAMENTO = new HashSet<>();

    private final ApiService apiService;

    public FavoritoRepository(Context context) {
        apiService = RetrofitClient.getClient(context.getApplicationContext()).create(ApiService.class);
    }

    public void carregar(ListaCallback callback) {
        apiService.listarFavoritos().enqueue(new Callback<FavoritosResponse>() {
            @Override
            public void onResponse(Call<FavoritosResponse> call, Response<FavoritosResponse> response) {
                FavoritosResponse corpo = response.body();
                if (!response.isSuccessful() || corpo == null || !corpo.isSuccess()
                        || corpo.getFavoritos() == null) {
                    callback.onError("Não foi possível carregar os favoritos.");
                    return;
                }
                FavoritosCache.substituir(corpo.getFavoritos());
                callback.onSuccess(new ArrayList<>(corpo.getFavoritos()));
            }

            @Override
            public void onFailure(Call<FavoritosResponse> call, Throwable t) {
                callback.onError("Sem conexão para carregar os favoritos.");
            }
        });
    }

    /** Flips the favorite state of an asset. The screen can redraw its heart immediately. */
    public void alternar(String simbolo, String nome, String categoria, AlternarCallback callback) {
        definir(simbolo, nome, categoria, !FavoritosCache.contem(simbolo, categoria), callback);
    }

    /** Sets the favorite state explicitly (used when the screen knows what the user wants). */
    public void definir(String simbolo, String nome, String categoria, boolean desejado,
                        AlternarCallback callback) {
        String canonica = FavoritosCache.categoriaCanonica(categoria);
        if (simbolo == null || simbolo.trim().isEmpty() || canonica == null) {
            callback.onError(false, "Este tipo de ativo não pode ser favoritado.");
            return;
        }
        String chave = canonica + "|" + simbolo.trim().toUpperCase(Locale.ROOT);
        synchronized (EM_ANDAMENTO) {
            if (!EM_ANDAMENTO.add(chave)) {
                return;
            }
        }

        final boolean estadoAnterior = FavoritosCache.contem(simbolo, canonica);
        final boolean novoEstado = desejado;
        FavoritosCache.definir(simbolo, canonica, novoEstado);
        callback.onResultado(novoEstado);

        Map<String, String> corpo = new HashMap<>();
        corpo.put("symbol", simbolo.trim());
        corpo.put("name", nome == null ? "" : nome.trim());
        corpo.put("category", canonica);

        Call<FavoritosResponse> chamada = novoEstado
                ? apiService.adicionarFavorito(corpo)
                : apiService.removerFavorito(corpo);
        chamada.enqueue(new Callback<FavoritosResponse>() {
            @Override
            public void onResponse(Call<FavoritosResponse> call, Response<FavoritosResponse> response) {
                liberar(chave);
                FavoritosResponse resposta = response.body();
                if (response.isSuccessful() && resposta != null && resposta.isSuccess()) {
                    return;
                }
                FavoritosCache.definir(simbolo, canonica, estadoAnterior);
                callback.onError(estadoAnterior, mensagemDeErro(response, novoEstado));
            }

            @Override
            public void onFailure(Call<FavoritosResponse> call, Throwable t) {
                liberar(chave);
                FavoritosCache.definir(simbolo, canonica, estadoAnterior);
                callback.onError(estadoAnterior, "Sem conexão. Não foi possível atualizar seus favoritos.");
            }
        });
    }

    private void liberar(String chave) {
        synchronized (EM_ANDAMENTO) {
            EM_ANDAMENTO.remove(chave);
        }
    }

    private String mensagemDeErro(Response<FavoritosResponse> response, boolean adicionando) {
        try {
            if (response.errorBody() != null) {
                FavoritosResponse erro = new Gson().fromJson(response.errorBody().charStream(), FavoritosResponse.class);
                if (erro != null && erro.getMessage() != null && !erro.getMessage().trim().isEmpty()
                        && response.code() == 422) {
                    return erro.getMessage();
                }
            }
        } catch (Exception ignored) {
            // Falls back to the generic message below.
        }
        return adicionando
                ? "Não foi possível adicionar aos favoritos."
                : "Não foi possível remover dos favoritos.";
    }
}
