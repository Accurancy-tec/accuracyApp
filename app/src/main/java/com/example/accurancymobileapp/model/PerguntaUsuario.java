package com.example.accurancymobileapp.model;

import com.google.gson.annotations.SerializedName;

/** Dúvida enviada pelo "Fale com a equipe", com a resposta quando houver. */
public class PerguntaUsuario {

    @SerializedName("id_pergunta")
    private int id;

    @SerializedName("assunto")
    private String assunto;

    @SerializedName("mensagem_pergunta")
    private String mensagem;

    @SerializedName("resposta_pergunta")
    private String resposta;

    @SerializedName("respondida_em")
    private String respondidaEm;

    @SerializedName("criado_em")
    private String criadoEm;

    public int getId() {
        return id;
    }

    public String getAssunto() {
        return assunto == null ? "" : assunto;
    }

    public String getMensagem() {
        return mensagem == null ? "" : mensagem;
    }

    public String getResposta() {
        return resposta == null ? "" : resposta;
    }

    public boolean isRespondida() {
        return resposta != null && !resposta.trim().isEmpty();
    }

    public String getRespondidaEm() {
        return respondidaEm;
    }

    public String getCriadoEm() {
        return criadoEm;
    }
}
