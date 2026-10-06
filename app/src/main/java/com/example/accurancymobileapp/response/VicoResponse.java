package com.example.accurancymobileapp.response;

import java.util.List;

public class VicoResponse {

    private boolean sucesso;
    private String mensagem;

    // Dashboard
    private String periodo;
    private List<Ponto> pontos;

    // Wallet
    private List<Distribuicao> distribuicao;

    public boolean isSucesso() {
        return sucesso;
    }

    public String getMensagem() {
        return mensagem;
    }

    public String getPeriodo() {
        return periodo;
    }

    public List<Ponto> getPontos() {
        return pontos;
    }

    public List<Distribuicao> getDistribuicao() {
        return distribuicao;
    }

    public static class Ponto {

        private String data;
        private double valor;

        public String getData() {
            return data;
        }

        public double getValor() {
            return valor;
        }
    }

    public static class Distribuicao {

        private String categoria;
        private double valor;
        private double percentual;

        public String getCategoria() {
            return categoria;
        }

        public double getValor() {
            return valor;
        }

        public double getPercentual() {
            return percentual;
        }
    }
}