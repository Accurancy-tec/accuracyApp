package com.example.accurancymobileapp.model;

public class Wallet {
    private String nome_carteira;
    private String tipo_carteira;

    public Wallet(String nome_wallet, String tipo_wallet){
        this.nome_carteira = nome_wallet;
        this.tipo_carteira = tipo_wallet;
    }

    public String getNome_carteira(){
        return nome_carteira;
    }

    public String getTipo_carteira(){
        return tipo_carteira;
    }
}
