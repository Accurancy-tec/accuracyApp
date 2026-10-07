package com.example.accurancymobileapp.model;

public class Wallet {
    private int id_carteira;
    private String nome_carteira;
    private String tipo_carteira;

    public Wallet(int id_carteira, String nome_wallet, String tipo_wallet){
        this.id_carteira = id_carteira;
        this.nome_carteira = nome_wallet;
        this.tipo_carteira = tipo_wallet;
    }

    public Wallet(String nome_carteira, String tipo_carteira){
        this.nome_carteira = nome_carteira;
        this.tipo_carteira = tipo_carteira;
    }

    public int getId_carteira(){
        return id_carteira;
    }
    public String getNome_carteira(){
        return nome_carteira;
    }

    public String getTipo_carteira(){
        return tipo_carteira;
    }

    @Override
    public String toString() {
        return nome_carteira;
    }
}
