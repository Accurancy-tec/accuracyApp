package com.example.accurancymobileapp.model;

import java.io.Serializable;

// Criei essa classe para testes, depois da pra trocar pela que ja tem o AporteAdapter esta usando essa classe
public class Aporte {

    private int id_carteira;
    private int id_aporte;
    private String ativo_aporte;
    private String name_ativo;
    private String categoria_ativo;
    private double quantidade_aporte;
    private double valor_aporte;
    private String tipo_aporte;
    private String recorrencia_aporte;
    private String data_aporte;
    private DetalheRendaFixa detalhe_renda_fixa;
    private DetalheRendaVariavel detalhe_renda_variavel;

    /** Conditions contracted at the time of a fixed income application (null for other categories). */
    public static class DetalheRendaFixa implements Serializable {
        private int id_titulo;
        private double taxa_contratada;
        private String indexador;
        private String vencimento;
        private String liquidez;

        public int getIdTitulo() { return id_titulo; }
        public double getTaxaContratada() { return taxa_contratada; }
        public String getIndexador() { return indexador; }
        public String getVencimento() { return vencimento; }
        public String getLiquidez() { return liquidez; }
    }

    /** Quantity and unit price of a variable income order (null for other categories). */
    public static class DetalheRendaVariavel implements Serializable {
        private Double quantidade;
        private Double preco_unitario;

        public Double getQuantidade() { return quantidade; }
        public Double getPrecoUnitario() { return preco_unitario; }
    }

    public DetalheRendaFixa getDetalheRendaFixa() { return detalhe_renda_fixa; }

    public DetalheRendaVariavel getDetalheRendaVariavel() { return detalhe_renda_variavel; }

    public boolean isRendaFixa() {
        return detalhe_renda_fixa != null || "Renda Fixa".equalsIgnoreCase(categoria_ativo);
    }

    public int getIdAporte() {
        return id_aporte;
    }

    public String getAtivoAporte() {
        return ativo_aporte;
    }

    public String getNameAtivo() {
        return name_ativo;
    }

    public String getCategoriaAtivo() {
        return categoria_ativo;
    }

    public double getQuantidadeAporte() {
        return quantidade_aporte;
    }

    public double getValorAporte() {
        return valor_aporte;
    }

    public String getTipoAporte() {
        return tipo_aporte;
    }

    public String getRecorrenciaAporte() {
        return recorrencia_aporte;
    }
    public String getData_aporte() {
        return data_aporte;
    }
}
