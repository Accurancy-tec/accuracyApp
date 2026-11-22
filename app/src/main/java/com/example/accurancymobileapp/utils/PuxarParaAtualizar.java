package com.example.accurancymobileapp.utils;

import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Pull-to-refresh (drag down from the top) with the app's colors. The screen passes what must be
 * reloaded; the spinner stops by itself when the requests it started have finished.
 */
public final class PuxarParaAtualizar {

    private final SwipeRefreshLayout layout;
    private int pendentes;

    public PuxarParaAtualizar(@NonNull SwipeRefreshLayout layout, @NonNull Runnable aoAtualizar) {
        this.layout = layout;
        layout.setColorSchemeColors(0xFF3989FF);
        layout.setProgressBackgroundColorSchemeColor(0xFF0B1729);
        layout.setOnRefreshListener(aoAtualizar::run);
    }

    /** Shows the spinner and reloads, as if the user had dragged (used by "Atualizar" buttons). */
    public void disparar(@NonNull Runnable recarregar) {
        layout.setRefreshing(true);
        recarregar.run();
    }

    public boolean atualizando() {
        return layout.isRefreshing();
    }

    /** One more request started: the spinner stays until {@link #concluir()} is called for it. */
    public void iniciar() {
        pendentes++;
    }

    /** One request started with {@link #iniciar()} has finished (success or failure). */
    public void concluir() {
        pendentes = Math.max(0, pendentes - 1);
        if (pendentes == 0) layout.setRefreshing(false);
    }

    /** Stops the spinner now, for screens that load one thing at a time. */
    public void parar() {
        pendentes = 0;
        layout.setRefreshing(false);
    }

    /** Wraps a Retrofit callback so the spinner is released when the call ends, whatever the outcome. */
    public <T> Callback<T> acompanhar(@NonNull Callback<T> original) {
        iniciar();
        return new Callback<T>() {
            @Override
            public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
                try {
                    original.onResponse(call, response);
                } finally {
                    concluir();
                }
            }

            @Override
            public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
                try {
                    original.onFailure(call, t);
                } finally {
                    concluir();
                }
            }
        };
    }
}
