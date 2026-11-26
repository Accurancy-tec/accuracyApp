package com.example.accurancymobileapp.model;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

/**
 * Carteira como a API devolve em /carteiras/buscar-carteiras e /carteiras/resumo,
 * já com patrimônio e rentabilidade calculados no servidor.
 */
public class Wallet {

    @SerializedName("id_carteira")
    private int id_carteira;

    @SerializedName("nome_carteira")
    private String nome_carteira;

    /** Real ou Simulada. */
    @SerializedName("tipo_carteira")
    private String tipo_carteira;

    /** Renda Fixa, Renda Variavel, Cripto, Internacional ou Mista. */
    @SerializedName("estilo_carteira")
    private String estilo_carteira;

    @SerializedName("meta_carteira")
    private BigDecimal meta_carteira;

    /** Custo dos ativos que ainda estão na carteira (quantidade × preço médio). */
    @SerializedName("total_investido")
    private BigDecimal totalInvestido;

    @SerializedName("valor_mercado")
    private BigDecimal valorMercado;

    @SerializedName("patrimonio_atual")
    private BigDecimal patrimonioAtual;

    @SerializedName("lucro_prejuizo")
    private BigDecimal lucroPrejuizo;

    @SerializedName("rentabilidade_percentual")
    private BigDecimal rentabilidadePercentual;

    /** Dinheiro ainda não investido da simulação (sempre 0 nas carteiras reais). */
    @SerializedName("saldo_livre_carteira")
    private BigDecimal saldoLivre;

    /** Total depositado na simulação (saldo inicial + depósitos). */
    @SerializedName("capital_simulado")
    private BigDecimal capitalSimulado;

    @SerializedName("resultado_simulacao")
    private BigDecimal resultadoSimulacao;

    @SerializedName("rentabilidade_simulacao")
    private BigDecimal rentabilidadeSimulacao;

    @SerializedName("progresso_meta")
    private BigDecimal progressoMeta;

    @SerializedName("meta_atingida")
    private boolean metaAtingida;

    @SerializedName("quantidade_ativos")
    private int quantidadeAtivos;

    private static BigDecimal valor(BigDecimal numero) {
        return numero != null ? numero : BigDecimal.ZERO;
    }

    public int getId_carteira() {
        return id_carteira;
    }

    public String getNome_carteira() {
        return nome_carteira;
    }

    public String getTipo_carteira() {
        return tipo_carteira;
    }

    public String getEstilo_carteira() {
        return estilo_carteira;
    }

    public boolean isSimulada() {
        return NovaCarteiraRequest.TIPO_SIMULADA.equalsIgnoreCase(tipo_carteira);
    }

    public BigDecimal getMeta_carteira() {
        return valor(meta_carteira);
    }

    public BigDecimal getTotalInvestido() {
        return valor(totalInvestido);
    }

    public BigDecimal getValorMercado() {
        return valor(valorMercado);
    }

    public BigDecimal getPatrimonioAtual() {
        return valor(patrimonioAtual);
    }

    public BigDecimal getLucroPrejuizo() {
        return valor(lucroPrejuizo);
    }

    public BigDecimal getRentabilidadePercentual() {
        return valor(rentabilidadePercentual);
    }

    public BigDecimal getSaldoLivre() {
        return valor(saldoLivre);
    }

    public BigDecimal getCapitalSimulado() {
        return valor(capitalSimulado);
    }

    public BigDecimal getResultadoSimulacao() {
        return valor(resultadoSimulacao);
    }

    public BigDecimal getRentabilidadeSimulacao() {
        return valor(rentabilidadeSimulacao);
    }

    public BigDecimal getProgressoMeta() {
        return valor(progressoMeta);
    }

    public boolean isMetaAtingida() {
        return metaAtingida;
    }

    public int getQuantidadeAtivos() {
        return quantidadeAtivos;
    }

    @Override
    public String toString() {
        return nome_carteira;
    }
}
