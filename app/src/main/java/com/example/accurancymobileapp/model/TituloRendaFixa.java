package com.example.accurancymobileapp.model;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class TituloRendaFixa {
    @SerializedName("id_titulo")
    private int idTitulo;

    @SerializedName("nome_titulo")
    String nomeTitulo;

    @SerializedName("tipo_titulo")
    String tipoTitulo;

    @SerializedName("rentabilidade")
    private BigDecimal rentabilidade;

    @SerializedName("indexador")
    private String indexador;

    @SerializedName("aplicacao_minima")
    private BigDecimal aplicacaoMinima;

    @SerializedName("vencimento")
    private String vencimento;

    @SerializedName("liquidez")
    private String liquidez;

    @SerializedName("taxa_administracao")
    private BigDecimal taxaAdministracao;

    @SerializedName("possui_garantia_fgc")
    private boolean possuiGarantiaFgc;

    @SerializedName("descricao")
    private String descricao;

    public int getIdTitulo() {
        return idTitulo;
    }

    public String getNomeTitulo() {
        return nomeTitulo;
    }

    public String getTipoTitulo() {
        return tipoTitulo;
    }

    public BigDecimal getRentabilidade() {
        return rentabilidade;
    }

    public String getIndexador() {
        return indexador;
    }

    public BigDecimal getAplicacaoMinima() {
        return aplicacaoMinima;
    }

    public String getVencimento() {
        return vencimento;
    }

    public String getLiquidez() {
        return liquidez;
    }

    public BigDecimal getTaxaAdministracao() {
        return taxaAdministracao;
    }

    public boolean possuiGarantiaFgc() {
        return possuiGarantiaFgc;
    }

    public String getDescricao() {
        return descricao;
    }
}
