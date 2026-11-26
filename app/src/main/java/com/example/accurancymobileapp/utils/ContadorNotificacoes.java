package com.example.accurancymobileapp.utils;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.response.NotificacoesResponse;

/** Número de notificações não lidas sobre o sino (início e perfil). */
public final class ContadorNotificacoes {

    private ContadorNotificacoes() {}

    /** Consulta o contador na API. Em caso de erro o sino fica como estava. */
    public static void atualizar(@NonNull Fragment tela, @NonNull View sino, @NonNull TextView badge) {
        RetrofitClient.getApiService(tela.requireContext()).contarNotificacoes()
                .enqueue(new CallbackDaTela<NotificacoesResponse>(tela) {
                    @Override
                    public void aoSucesso(@NonNull NotificacoesResponse resposta) {
                        mostrar(sino, badge, resposta.getNaoLidas());
                    }

                    @Override
                    public void aoErro(@NonNull String mensagem) {
                        // O contador é só um aviso; sem rede, a tela segue normalmente.
                    }
                });
    }

    public static void mostrar(@NonNull View sino, @NonNull TextView badge, int naoLidas) {
        if (naoLidas <= 0) {
            badge.setVisibility(View.GONE);
            sino.setContentDescription("Notificações");
            return;
        }
        badge.setText(naoLidas > 99 ? "99+" : String.valueOf(naoLidas));
        badge.setVisibility(View.VISIBLE);
        sino.setContentDescription(naoLidas == 1
                ? "Notificações, 1 não lida"
                : "Notificações, " + naoLidas + " não lidas");
    }
}
