package com.example.accurancymobileapp.response;

import java.util.List;

/**
 * Resposta dos endpoints usados pelos dois gráficos.
 */
public class VicoResponse {

    private boolean sucesso;
    private String mensagem;
    private String periodo;
    private List<Ponto> pontos;
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
