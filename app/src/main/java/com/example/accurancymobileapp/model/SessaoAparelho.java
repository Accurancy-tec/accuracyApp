package com.example.accurancymobileapp.model;

import com.google.gson.annotations.SerializedName;

/** Um aparelho (ou navegador) com a conta aberta, como vem de /auth/sessoes. */
public class SessaoAparelho {

    @SerializedName("id_sessao")
    private int id;

    @SerializedName("dispositivo")
    private String dispositivo;

    /** "Android", "Site" ou null para logins antigos. */
    @SerializedName("plataforma")
    private String plataforma;

    @SerializedName("criado_em")
    private String criadoEm;

    @SerializedName("ultimo_uso_em")
    private String ultimoUsoEm;

    /** É o aparelho que fez a consulta. */
    @SerializedName("atual")
    private boolean atual;

    public int getId() { return id; }
    public String getDispositivo() { return dispositivo == null || dispositivo.trim().isEmpty() ? "Aparelho" : dispositivo; }
    public String getPlataforma() { return plataforma; }
    public String getCriadoEm() { return criadoEm; }
    public String getUltimoUsoEm() { return ultimoUsoEm; }
    public boolean isAtual() { return atual; }

    public boolean isSite() {
        return "Site".equals(plataforma);
    }
}
