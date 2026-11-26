package com.example.accurancymobileapp.response;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class AssetPositionResponse {

    @SerializedName(value = "success", alternate = {"sucesso"})
    private boolean success;

    @SerializedName("symbol")
    private String symbol;

    @SerializedName(value = "quantidade", alternate = {"quantity"})
    private Double quantity;

    @SerializedName(value = "precoMedio", alternate = {"averagePrice"})
    private Double averagePrice;

    @SerializedName("hasPosition")
    private boolean hasPosition;

    @SerializedName(value = "mensagem", alternate = {"message"})
    private String message;

    /** Quanto o usuário tem do ativo em cada carteira (só as que têm alguma unidade). */
    @SerializedName("carteiras")
    private List<PosicaoCarteira> carteiras;

    public static class PosicaoCarteira {
        @SerializedName("id_carteira")
        private int idCarteira;
        @SerializedName("nome_carteira")
        private String nome;
        @SerializedName("tipo_carteira")
        private String tipo;
        @SerializedName("quantidade")
        private double quantidade;
        @SerializedName("preco_medio")
        private double precoMedio;

        public int getIdCarteira() {
            return idCarteira;
        }

        public String getNome() {
            return nome == null ? "" : nome;
        }

        public boolean isSimulada() {
            return "Simulada".equalsIgnoreCase(tipo);
        }

        public double getQuantidade() {
            return quantidade;
        }

        public double getPrecoMedio() {
            return precoMedio;
        }
    }

    public List<PosicaoCarteira> getCarteiras() {
        return carteiras == null ? new ArrayList<>() : carteiras;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getSymbol() {
        return symbol;
    }

    public Double getQuantity() {
        return quantity;
    }

    public Double getAveragePrice() {
        return averagePrice;
    }

    public boolean hasPosition() {
        return hasPosition;
    }

    public String getMessage() {
        return message;
    }
}