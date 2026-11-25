package com.example.accurancymobileapp.utils;

import android.app.Activity;

import androidx.fragment.app.Fragment;

import com.example.accurancymobileapp.response.PerfilResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Callback for the profile routes: turns every outcome into either aoSucesso or aoErro(message).
 * Nothing is delivered after the owner (fragment or activity) is gone.
 */
public abstract class PerfilCallback implements Callback<PerfilResponse> {

    private final Object dono;

    public PerfilCallback(Fragment dono) {
        this.dono = dono;
    }

    public PerfilCallback(Activity dono) {
        this.dono = dono;
    }

    public abstract void aoSucesso(PerfilResponse resposta);

    public abstract void aoErro(String mensagem);

    private boolean donoAtivo() {
        if (dono instanceof Fragment) {
            Fragment f = (Fragment) dono;
            return f.isAdded() && f.getView() != null;
        }
        if (dono instanceof Activity) {
            Activity a = (Activity) dono;
            return !a.isFinishing() && !a.isDestroyed();
        }
        return true;
    }

    @Override
    public void onResponse(Call<PerfilResponse> call, Response<PerfilResponse> response) {
        if (!donoAtivo()) return;
        if (response.isSuccessful()) {
            PerfilResponse corpo = response.body();
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
    public void onFailure(Call<PerfilResponse> call, Throwable erro) {
        if (call.isCanceled() || !donoAtivo()) return;
        aoErro("Sem conexão com o servidor. Verifique a internet e tente de novo.");
    }
}
