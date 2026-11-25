package com.example.accurancymobileapp.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/** Notícia de mercado (portais por RSS) ou publicada pela equipe Accuracy. */
public class Noticia {

    @SerializedName("id")
    private String id;

    @SerializedName("titulo")
    private String titulo;

    @SerializedName("resumo")
    private String resumo;

    @SerializedName("conteudo")
    private String conteudo;

    @SerializedName("fonte")
    private String fonte;

    /** Link da matéria; null nas notícias da equipe. */
    @SerializedName("url")
    private String url;

    @SerializedName("imagem")
    private String imagem;

    @SerializedName("publicada_em")
    private String publicadaEm;

    public String getId() {
        return id;
    }

    public String getTitulo() {
        return titulo == null ? "" : titulo;
    }

    public String getResumo() {
        return resumo == null ? "" : resumo;
    }

    public String getConteudo() {
        return conteudo == null ? "" : conteudo;
    }

    public String getFonte() {
        return fonte == null ? "" : fonte;
    }

    @Nullable
    public String getUrl() {
        return url;
    }

    @Nullable
    public String getImagem() {
        return imagem;
    }

    public String getPublicadaEm() {
        return publicadaEm;
    }
}
