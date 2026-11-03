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

    @SerializedName("estilo_carteira")
    private String estilo_carteira;

    @SerializedName("meta_carteira")
    private BigDecimal meta_carteira;

    @SerializedName("total_investido")
    private BigDecimal totalAportado = BigDecimal.ZERO;

    @SerializedName("patrimonio_atual")
    private BigDecimal patrimonioAtual = BigDecimal.ZERO;

    @SerializedName("lucro_prejuizo")
    private BigDecimal lucroPrejuizo = BigDecimal.ZERO;

    @SerializedName("rentabilidade_percentual")
    private BigDecimal rentabilidadePercentual = BigDecimal.ZERO;



    public Wallet(){

    }

    public Wallet(int id_carteira, String nome_wallet, String tipo_wallet, BigDecimal meta_carteira, String estilo_carteira){
        this.id_carteira = id_carteira;
        this.nome_carteira = nome_wallet;
        this.tipo_carteira = tipo_wallet;
        this.estilo_carteira = estilo_carteira;
        this.meta_carteira = meta_carteira;
    }

    public Wallet(String nome_carteira, String tipo_carteira, BigDecimal meta_carteira, String estilo_carteira){
        this.nome_carteira = nome_carteira;
        this.tipo_carteira = tipo_carteira;
        this.meta_carteira = meta_carteira;
        this.estilo_carteira = estilo_carteira;
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

    public BigDecimal getPatrimonioAtual(){
        return patrimonioAtual;
    }

    public BigDecimal getLucroPrejuizo(){
        return lucroPrejuizo;
    }

    public BigDecimal getRentabilidadePercentual(){
        return rentabilidadePercentual;
    }
    public void setTotalAportado(BigDecimal totalAportado){
        this.totalAportado = totalAportado != null ? totalAportado : BigDecimal.ZERO;
    }

    public BigDecimal getMeta_carteira(){
        return meta_carteira;
    }

    public String getEstilo_carteira() {
        return estilo_carteira;
    }

    @Override
    public String toString() {
        return nome_carteira;
    }
}
