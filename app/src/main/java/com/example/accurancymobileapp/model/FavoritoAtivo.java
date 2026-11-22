package com.example.accurancymobileapp.model;

import com.google.gson.annotations.SerializedName;

/** One favorite asset as returned by favoritos/listar. */
public class FavoritoAtivo {

    @SerializedName("simbolo_ativo")
    private String simbolo;

    @SerializedName("nome_ativo")
    private String nome;

    @SerializedName("categoria_ativo")
    private String categoria;

    public FavoritoAtivo() {
    }

    public FavoritoAtivo(String simbolo, String nome, String categoria) {
        this.simbolo = simbolo;
        this.nome = nome;
        this.categoria = categoria;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public String getNome() {
        return nome == null || nome.trim().isEmpty() ? simbolo : nome;
    }

    public String getCategoria() {
        return categoria;
    }
}
