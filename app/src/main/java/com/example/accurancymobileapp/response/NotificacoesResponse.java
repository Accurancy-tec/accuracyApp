package com.example.accurancymobileapp.response;

import com.example.accurancymobileapp.model.Notificacao;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/** Resposta de /notificacoes/listar, /contador e /marcar-lidas. */
public class NotificacoesResponse extends RespostaSimples {

    @SerializedName("nao_lidas")
    private int naoLidas;

    @SerializedName("notificacoes")
    private List<Notificacao> notificacoes;

    public int getNaoLidas() {
        return naoLidas;
    }

    public List<Notificacao> getNotificacoes() {
        return notificacoes == null ? new ArrayList<>() : notificacoes;
    }
}
