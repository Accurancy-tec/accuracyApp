package com.example.accurancymobileapp.response;

import com.google.gson.annotations.SerializedName;

/** POST /carteiras/depositar-simulacao. */
public class DepositoSimulacaoResponse extends RespostaSimples {

    /** Saldo livre da simulação depois do depósito. */
    @SerializedName("saldo_simulacao")
    private double saldoSimulacao;

    public double getSaldoSimulacao() {
        return saldoSimulacao;
    }
}
