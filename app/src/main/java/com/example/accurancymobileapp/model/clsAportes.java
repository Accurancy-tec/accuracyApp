package com.example.accurancymobileapp.model;

public class clsAportes {
    private String name_ativo;
    private String categoria_ativo;
    private double quantidade_aporte;
    private String ativo_aporte;
    private double valor_aporte;
    private String tipo_aporte;
    private String recorrencia_aporte;

    public clsAportes(String ativo,String name,String categoria,double quantidade,double preco, String tipo, String recorrencia) {
        ativo_aporte = ativo;
        name_ativo = name;
        categoria_ativo = categoria;
        quantidade_aporte = quantidade;
        valor_aporte = preco;
        tipo_aporte = tipo;
        recorrencia_aporte = recorrencia;
    }

    public String getAtivo() {
        return ativo_aporte;
    }

    public void setAtivo(String ativo) {
        ativo_aporte = ativo;
    }

    public String getTipo() {
        return tipo_aporte;
    }

    public void setTipo(String tipo) {
        tipo_aporte = tipo;
    }

    public double getPreco() {
        return valor_aporte;
    }

    public void setPreco(double preco) {
        valor_aporte = preco;
    }

    public String getRecorrencia() {
        return recorrencia_aporte;
    }

    public void setRecorrencia(String recorrencia) {
        recorrencia_aporte = recorrencia;
    }

    public String getName() {
        return name_ativo;
    }

    public void setName(String name) {
        this.name_ativo = name;
    }

    public String getCategoria() {
        return categoria_ativo;
    }

    public void setCategoria(String categoria) {
        this.categoria_ativo = categoria;
    }

    public double getQuantidade() {
        return quantidade_aporte;
    }

    public void setQuantidade(double quantidade) {
        this.quantidade_aporte = quantidade;
    }
}
