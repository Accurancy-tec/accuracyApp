package com.example.accurancymobileapp.model;

import com.google.gson.annotations.SerializedName;

public class Wallet {

    // Vem de GET service/buscarCarteiras.php. Ao CRIAR uma carteira fica null e o Gson nem manda no JSON.
    @SerializedName("id_carteira")
    private Integer id_carteira;

    private String nome_carteira;
    private String tipo_carteira;

    public Wallet(String nome_wallet, String tipo_wallet){
        this.nome_carteira = nome_wallet;
        this.tipo_carteira = tipo_wallet;
    }

    public Integer getId_carteira(){
        return id_carteira;
    }

    public String getNome_carteira(){
        return nome_carteira;
    }

    public String getTipo_carteira(){
        return tipo_carteira;
    }
}
