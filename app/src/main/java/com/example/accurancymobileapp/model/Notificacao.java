package com.example.accurancymobileapp.model;

import com.google.gson.annotations.SerializedName;

/** Aviso da conta: operação registrada, meta alcançada, resposta da equipe etc. */
public class Notificacao {

    @SerializedName("id_notificacao")
    private int id;

    @SerializedName("titulo")
    private String titulo;

    @SerializedName("mensagem")
    private String mensagem;

    /** Aporte, Preço, Notícia, Sistema, Aprendizado ou Meta. */
    @SerializedName("tipo")
    private String tipo;

    @SerializedName("lida")
    private boolean lida;

    /** Data em UTC no formato ISO (2026-11-25T14:03:00Z). */
    @SerializedName("criado_em")
    private String criadoEm;

    public int getId() {
        return id;
    }

    public String getTitulo() {
        return titulo == null ? "" : titulo;
    }

    public String getMensagem() {
        return mensagem == null ? "" : mensagem;
    }

    public String getTipo() {
        return tipo == null ? "" : tipo;
    }

    public boolean isLida() {
        return lida;
    }

    public void setLida(boolean lida) {
        this.lida = lida;
    }

    public String getCriadoEm() {
        return criadoEm;
    }
}
