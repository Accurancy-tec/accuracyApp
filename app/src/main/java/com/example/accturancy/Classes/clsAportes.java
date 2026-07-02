package com.example.accturancy.Classes;

public class clsAportes {
    private String Ativo;
    private double Preco;
    private String Tipo;
    private String Recorrencia;

    public clsAportes(String ativo, double preco, String tipo, String recorrencia) {
        Ativo = ativo;
        Preco = preco;
        Tipo = tipo;
        Recorrencia = recorrencia;
    }

    public String getAtivo() {
        return Ativo;
    }

    public void setAtivo(String ativo) {
        Ativo = ativo;
    }

    public String getTipo() {
        return Tipo;
    }

    public void setTipo(String tipo) {
        Tipo = tipo;
    }

    public double getPreco() {
        return Preco;
    }

    public void setPreco(double preco) {
        Preco = preco;
    }

    public String getRecorrencia() {
        return Recorrencia;
    }

    public void setRecorrencia(String recorrencia) {
        Recorrencia = recorrencia;
    }

}
