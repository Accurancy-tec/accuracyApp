package com.example.accurancymobileapp.model;

// Criei essa classe para testes, depois da pra trocar pela que ja tem o AporteAdapter esta usando essa classe
public class Aporte {
    private int id_aporte;
    private String ativo_aporte;
    private String name_ativo;
    private String categoria_ativo;
    private double quantidade_aporte;
    private double valor_aporte;
    private String tipo_aporte;
    private String recorrencia_aporte;
    private String data_aporte;

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
