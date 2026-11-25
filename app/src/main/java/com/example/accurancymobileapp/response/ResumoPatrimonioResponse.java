package com.example.accurancymobileapp.response;

import com.example.accurancymobileapp.model.Wallet;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * GET /carteiras/resumo: saldo geral, totais das carteiras reais e das simuladas (separados,
 * para não somar dinheiro de teste ao patrimônio) e cada carteira já calculada.
 */
public class ResumoPatrimonioResponse extends RespostaSimples {

    @SerializedName("saldo_geral")
    private double saldoGeral;

    @SerializedName("real")
    private Real real;

    @SerializedName("simulado")
    private Simulado simulado;

    @SerializedName("carteiras")
    private List<Wallet> carteiras;

    @SerializedName("atualizado_em")
    private String atualizadoEm;

    public double getSaldoGeral() {
        return saldoGeral;
    }

    public Real getReal() {
        return real != null ? real : new Real();
    }

    public Simulado getSimulado() {
        return simulado != null ? simulado : new Simulado();
    }

    public List<Wallet> getCarteiras() {
        return carteiras != null ? carteiras : new ArrayList<>();
    }

    public String getAtualizadoEm() {
        return atualizadoEm;
    }

    /** Carteiras reais: entram no patrimônio total junto com o saldo geral. */
    public static class Real {
        @SerializedName("patrimonio_total")
        public double patrimonioTotal;
        @SerializedName("valor_mercado")
        public double valorMercado;
        @SerializedName("total_investido")
        public double totalInvestido;
        @SerializedName("lucro_prejuizo")
        public double lucroPrejuizo;
        @SerializedName("rentabilidade_percentual")
        public double rentabilidadePercentual;
        @SerializedName("resultado_vendas")
        public double resultadoVendas;
        @SerializedName("dividendos_recebidos")
        public double dividendosRecebidos;
        @SerializedName("quantidade_carteiras")
        public int quantidadeCarteiras;
    }

    /** Carteiras simuladas: dinheiro fictício de cada simulação, mostrado à parte. */
    public static class Simulado {
        @SerializedName("patrimonio_total")
        public double patrimonioTotal;
        @SerializedName("capital_simulado")
        public double capitalSimulado;
        @SerializedName("saldo_disponivel")
        public double saldoDisponivel;
        @SerializedName("resultado")
        public double resultado;
        @SerializedName("rentabilidade_percentual")
        public double rentabilidadePercentual;
        @SerializedName("quantidade_carteiras")
        public int quantidadeCarteiras;
    }
}
