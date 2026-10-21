package com.example.accurancymobileapp.response;

import com.example.accurancymobileapp.model.Acao;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AcaoResponse {
    @SerializedName("success")
    private boolean sucesso;

    @SerializedName("stocks")
    private List<Acao> acoes;

    @SerializedName("paginacao")
    private Paginacao paginacao;

    public boolean isSucesso() {
        return sucesso;
    }

    public List<Acao> getAcoes() {
        return acoes;
    }

    public Paginacao getPaginacao() {
        return paginacao;
    }


    public static class Paginacao {

        @SerializedName("paginaAtual")
        private int paginaAtual;

        @SerializedName("totalPaginas")
        private int totalPaginas;

        @SerializedName("totalAtivos")
        private int totalAtivos;

        @SerializedName("temProximaPagina")
        private boolean temProximaPagina;

        public int getPaginaAtual() {
            return paginaAtual;
        }

        public int getTotalPaginas() {
            return totalPaginas;
        }

        public int getTotalAtivos() {
            return totalAtivos;
        }

        public boolean isTemProximaPagina() {
            return temProximaPagina;
        }
    }
}
