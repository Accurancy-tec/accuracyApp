package com.example.accurancymobileapp.model;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class Wallet {
    @SerializedName("id_carteira")
    private int id_carteira;

    @SerializedName("nome_carteira")
    private String nome_carteira;

    @SerializedName("tipo_carteira")
    private String tipo_carteira;

    //@SerializedName("meta_carteira")
    //private double meta_carteira;

    @SerializedName("total_aportado")
    private BigDecimal totalAportado = BigDecimal.ZERO;

    public Wallet(){

    }

    public Wallet(int id_carteira, String nome_wallet, String tipo_wallet /*double meta_carteira*/){
        this.id_carteira = id_carteira;
        this.nome_carteira = nome_wallet;
        this.tipo_carteira = tipo_wallet;
        //this.meta_carteira = meta_carteira;
    }

    public Wallet(String nome_carteira, String tipo_carteira /*double meta_carteira*/){
        this.nome_carteira = nome_carteira;
        this.tipo_carteira = tipo_carteira;
        //this.meta_carteira = meta_carteira;
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

    public BigDecimal getTotalAportado(){
        return totalAportado != null ? totalAportado : BigDecimal.ZERO;
    }

    public void setTotalAportado(BigDecimal totalAportado){
        this.totalAportado = totalAportado != null ? totalAportado : BigDecimal.ZERO;
    }
    /*public double getMeta_carteira(){
        return meta_carteira;
    }*/

    @Override
    public String toString() {
        return nome_carteira;
    }
}
