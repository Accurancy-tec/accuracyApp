package com.example.accurancymobileapp.response;

import com.example.accurancymobileapp.model.SessaoAparelho;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/** Resposta de /auth/sessoes, /auth/sessaoEncerrar e /auth/sessoesEncerrar. */
public class SessoesResponse extends RespostaSimples {

    @SerializedName("sessoes")
    private List<SessaoAparelho> sessoes;

    /** A sessão deste aparelho foi encerrada junto: o app precisa voltar para o login. */
    @SerializedName("encerrou_atual")
    private boolean encerrouAtual;

    @SerializedName("encerradas")
    private int encerradas;

    public List<SessaoAparelho> getSessoes() {
        return sessoes == null ? new ArrayList<>() : sessoes;
    }

    public boolean isEncerrouAtual() {
        return encerrouAtual;
    }

    public int getEncerradas() {
        return encerradas;
    }
}
