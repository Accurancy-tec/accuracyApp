package com.example.accurancymobileapp.response;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/** Resposta de GET /relatorios/resumo: números da conta, observações e a última análise com IA. */
public class RelatorioResponse extends RespostaSimples {

    @SerializedName("periodo") public Periodo periodo;
    @SerializedName("saldo_geral") public double saldoGeral;
    @SerializedName("real") public Real real;
    @SerializedName("simulado") public Simulado simulado;
    @SerializedName("carteiras") private List<Carteira> carteiras;
    @SerializedName("distribuicao") private List<Fatia> distribuicao;
    @SerializedName("destaques") public Destaques destaques;
    @SerializedName("movimentacao") public Movimentacao movimentacao;
    @SerializedName("observacoes") private List<Observacao> observacoes;
    @SerializedName("aviso") public String aviso;
    @SerializedName("analise_ia") public AnaliseIa analiseIa;

    public List<Carteira> getCarteiras() { return carteiras == null ? new ArrayList<>() : carteiras; }
    public List<Fatia> getDistribuicao() { return distribuicao == null ? new ArrayList<>() : distribuicao; }
    public List<Observacao> getObservacoes() { return observacoes == null ? new ArrayList<>() : observacoes; }

    public static class Periodo {
        @SerializedName("inicio") public String inicio;
        @SerializedName("fim") public String fim;
        @SerializedName("dias") public int dias;
    }

    public static class Real {
        @SerializedName("patrimonio_total") public double patrimonioTotal;
        @SerializedName("valor_mercado") public double valorMercado;
        @SerializedName("total_investido") public double totalInvestido;
        @SerializedName("lucro_prejuizo") public double lucroPrejuizo;
        @SerializedName("rentabilidade_percentual") public double rentabilidade;
        @SerializedName("quantidade_carteiras") public int quantidadeCarteiras;
    }

    public static class Simulado {
        @SerializedName("patrimonio_total") public double patrimonioTotal;
        @SerializedName("resultado") public double resultado;
        @SerializedName("rentabilidade_percentual") public double rentabilidade;
        @SerializedName("quantidade_carteiras") public int quantidadeCarteiras;
    }

    public static class Carteira {
        @SerializedName("nome_carteira") public String nome;
        @SerializedName("tipo_carteira") public String tipo;
        @SerializedName("patrimonio_atual") public double patrimonio;
        @SerializedName("total_investido") public double investido;
        @SerializedName("lucro_prejuizo") public double resultado;
        @SerializedName("rentabilidade_percentual") public double rentabilidade;
        @SerializedName("meta_carteira") public double meta;
        @SerializedName("progresso_meta") public Double progressoMeta;
        @SerializedName("meta_atingida") public boolean metaAtingida;

        public boolean isSimulada() { return "Simulada".equals(tipo); }
    }

    public static class Fatia {
        @SerializedName("categoria") public String categoria;
        @SerializedName("valor") public double valor;
        @SerializedName("percentual") public double percentual;
    }

    public static class Ativo {
        @SerializedName("simbolo") public String simbolo;
        @SerializedName("carteira") public String carteira;
        @SerializedName("simulada") public boolean simulada;
        @SerializedName("rentabilidade_percentual") public double rentabilidade;
    }

    public static class Destaques {
        @SerializedName("melhores") private List<Ativo> melhores;
        @SerializedName("piores") private List<Ativo> piores;

        public List<Ativo> getMelhores() { return melhores == null ? new ArrayList<>() : melhores; }
        public List<Ativo> getPiores() { return piores == null ? new ArrayList<>() : piores; }
    }

    public static class Grupo {
        @SerializedName("compras") public int compras;
        @SerializedName("vendas") public int vendas;
        @SerializedName("dividendos") public int dividendos;
        @SerializedName("valor_comprado") public double valorComprado;
        @SerializedName("valor_vendido") public double valorVendido;
        @SerializedName("valor_dividendos") public double valorDividendos;
    }

    public static class Movimentacao {
        @SerializedName("real") public Grupo real;
        @SerializedName("simulada") public Grupo simulada;
        @SerializedName("depositos") public int depositos;
        @SerializedName("valor_depositos") public double valorDepositos;
    }

    public static class Observacao {
        /** positivo, atencao, info ou inicio. */
        @SerializedName("tipo") public String tipo;
        @SerializedName("texto") public String texto;
    }

    public static class Analise {
        @SerializedName("texto") public String texto;
        @SerializedName("gerado_em") public String geradoEm;
        @SerializedName("espera_segundos") public int esperaSegundos;
    }

    public static class AnaliseIa {
        @SerializedName("disponivel") public boolean disponivel;
        @SerializedName("ultima") public Analise ultima;
    }
}
