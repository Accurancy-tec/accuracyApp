package com.example.accurancymobileapp.model;

public class clsGrafic {
    private String ativo_aporte;
    private double preco_aporte;

    public String getAtivo() {
        return ativo_aporte;
    }

    public double getPreco() {
        return preco_aporte;
    }


    public clsGrafic(String ativo_aporte, double preco_aporte) {
        this.ativo_aporte = ativo_aporte;
        this.preco_aporte = preco_aporte;
    }

    public void setPreco_aporte(double preco_aporte) {
        this.preco_aporte = preco_aporte;
    }

    public void setAtivo_aporte(String ativo_aporte) {
        this.ativo_aporte = ativo_aporte;
    }
}
