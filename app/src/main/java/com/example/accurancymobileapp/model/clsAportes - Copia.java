package com.example.accurancymobileapp.model;

public class clsAportes {
    private String ativo_aporte;
    private double preco_aporte;
    private String tipo_aporte;
    private String recorrencia_aporte;

    public clsAportes(String ativo, double preco, String tipo, String recorrencia) {
        ativo_aporte = ativo;
        preco_aporte = preco;
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
        return preco_aporte;
    }

    public void setPreco(double preco) {
        preco_aporte = preco;
    }

    public String getRecorrencia() {
        return recorrencia_aporte;
    }

    public void setRecorrencia(String recorrencia) {
        recorrencia_aporte = recorrencia;
    }

}
