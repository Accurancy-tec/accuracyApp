package com.example.accurancymobileapp.utils;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.accurancymobileapp.response.RespostaSimples;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Callback que transforma qualquer resultado em aoSucesso ou aoErro(mensagem).
 * Nada é entregue depois que a tela dona (fragment ou activity) foi fechada.
 */
public abstract class CallbackDaTela<T extends RespostaSimples> implements Callback<T> {

    private final Object dono;

    public CallbackDaTela(@NonNull Fragment dono) {
        this.dono = dono;
    }

    public CallbackDaTela(@NonNull Activity dono) {
        this.dono = dono;
    }

    public abstract void aoSucesso(@NonNull T resposta);

    public abstract void aoErro(@NonNull String mensagem);

    private boolean donoAtivo() {
        if (dono instanceof Fragment) {
            Fragment f = (Fragment) dono;
            return f.isAdded() && f.getView() != null;
        }
        Activity a = (Activity) dono;
        return !a.isFinishing() && !a.isDestroyed();
    }

    @Override
    public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
        if (!donoAtivo()) return;
        if (response.isSuccessful()) {
            T corpo = response.body();
            if (corpo == null) {
                aoErro("O servidor retornou uma resposta vazia.");
            } else if (corpo.ok()) {
                aoSucesso(corpo);
            } else {
                aoErro(corpo.texto("Não foi possível concluir agora."));
            }
            return;
        }
        aoErro(ApiErros.mensagem(response.errorBody(),
                "Não foi possível concluir agora (erro " + response.code() + ")."));
    }

    @Override
    public void onFailure(@NonNull Call<T> call, @NonNull Throwable erro) {
        if (call.isCanceled() || !donoAtivo()) return;
        aoErro("Sem conexão com o servidor. Verifique a internet e tente de novo.");
    }
}
