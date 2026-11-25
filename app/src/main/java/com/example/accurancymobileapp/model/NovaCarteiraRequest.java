package com.example.accurancymobileapp.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

/** Corpo de POST /carteiras/criar-carteira. */
public class NovaCarteiraRequest {

    public static final String TIPO_REAL = "Real";
    public static final String TIPO_SIMULADA = "Simulada";

    @SerializedName("nome_carteira")
    private final String nome;

    /** Real ou Simulada. */
    @SerializedName("tipo_carteira")
    private final String tipo;

    /** Renda Fixa, Renda Variavel, Cripto, Internacional ou Mista. */
    @SerializedName("estilo_carteira")
    private final String estilo;

    @SerializedName("meta_carteira")
    private final BigDecimal meta;

    /** Dinheiro próprio da simulação. Fica fora do JSON (null) nas carteiras reais. */
    @SerializedName("saldo_inicial")
    private final BigDecimal saldoInicial;

    public NovaCarteiraRequest(String nome, String tipo, String estilo,
                               @Nullable BigDecimal meta, @Nullable BigDecimal saldoInicial) {
        this.nome = nome;
        this.tipo = tipo;
        this.estilo = estilo;
        this.meta = meta == null ? BigDecimal.ZERO : meta;
        this.saldoInicial = TIPO_SIMULADA.equals(tipo) ? saldoInicial : null;
    }
}
