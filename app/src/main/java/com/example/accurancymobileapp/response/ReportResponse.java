package com.example.accurancymobileapp.response;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/** GET relatorios/resumo: account numbers, notes and the Accuracy assistant's analysis. */
public class ReportResponse extends RespostaSimples {

    @SerializedName("periodo") public Period period;
    @SerializedName("saldo_geral") public double availableBalance;
    @SerializedName("real") public Real real;
    @SerializedName("simulado") public Simulated simulated;
    @SerializedName("carteiras") private List<Wallet> wallets;
    @SerializedName("distribuicao") private List<CategoryShare> distribution;
    @SerializedName("destaques") public Highlights highlights;
    @SerializedName("movimentacao") public Transactions transactions;
    @SerializedName("observacoes") private List<Note> notes;
    @SerializedName("aviso") public String disclaimer;
    @SerializedName("analysis") public Analysis analysis;

    public List<Wallet> getWallets() { return wallets == null ? new ArrayList<>() : wallets; }
    public List<CategoryShare> getDistribution() { return distribution == null ? new ArrayList<>() : distribution; }
    public List<Note> getNotes() { return notes == null ? new ArrayList<>() : notes; }

    public static class Period {
        @SerializedName("inicio") public String start;
        @SerializedName("fim") public String end;
        @SerializedName("dias") public int days;
    }

    public static class Real {
        @SerializedName("patrimonio_total") public double netWorth;
        @SerializedName("valor_mercado") public double marketValue;
        @SerializedName("total_investido") public double invested;
        @SerializedName("lucro_prejuizo") public double result;
        @SerializedName("rentabilidade_percentual") public double returnPercent;
        @SerializedName("quantidade_carteiras") public int walletCount;
    }

    public static class Simulated {
        @SerializedName("patrimonio_total") public double netWorth;
        @SerializedName("resultado") public double result;
        @SerializedName("rentabilidade_percentual") public double returnPercent;
        @SerializedName("quantidade_carteiras") public int walletCount;
    }

    public static class Wallet {
        @SerializedName("nome_carteira") public String name;
        @SerializedName("tipo_carteira") public String type;
        @SerializedName("patrimonio_atual") public double netWorth;
        @SerializedName("total_investido") public double invested;
        @SerializedName("lucro_prejuizo") public double result;
        @SerializedName("rentabilidade_percentual") public double returnPercent;
        @SerializedName("meta_carteira") public double goal;
        @SerializedName("progresso_meta") public Double goalProgress;
        @SerializedName("meta_atingida") public boolean goalReached;

        public boolean isSimulated() { return "Simulada".equals(type); }
    }

    public static class CategoryShare {
        @SerializedName("categoria") public String category;
        @SerializedName("valor") public double value;
        @SerializedName("percentual") public double percent;
    }

    public static class Asset {
        @SerializedName("simbolo") public String symbol;
        @SerializedName("carteira") public String wallet;
        @SerializedName("simulada") public boolean simulated;
        @SerializedName("rentabilidade_percentual") public double returnPercent;
    }

    public static class Highlights {
        @SerializedName("melhores") private List<Asset> best;
        @SerializedName("piores") private List<Asset> worst;

        public List<Asset> getBest() { return best == null ? new ArrayList<>() : best; }
        public List<Asset> getWorst() { return worst == null ? new ArrayList<>() : worst; }
    }

    public static class TransactionGroup {
        @SerializedName("compras") public int purchases;
        @SerializedName("vendas") public int sales;
        @SerializedName("dividendos") public int dividends;
        @SerializedName("valor_comprado") public double purchasedValue;
        @SerializedName("valor_vendido") public double soldValue;
        @SerializedName("valor_dividendos") public double dividendValue;
    }

    public static class Transactions {
        @SerializedName("real") public TransactionGroup real;
        @SerializedName("simulada") public TransactionGroup simulated;
        @SerializedName("depositos") public int deposits;
        @SerializedName("valor_depositos") public double depositValue;
    }

    public static class Note {
        /** positivo, atencao, info or inicio. */
        @SerializedName("tipo") public String type;
        @SerializedName("texto") public String text;
    }

    public static class Analysis {
        @SerializedName("engine") public String engine;
        @SerializedName("paragraphs") private List<String> paragraphs;
        @SerializedName("diagnosis") public Diagnosis diagnosis;
        @SerializedName("study") private List<StudyTrail> study;

        public List<String> getParagraphs() { return paragraphs == null ? new ArrayList<>() : paragraphs; }
        public List<StudyTrail> getStudy() { return study == null ? new ArrayList<>() : study; }
    }

    public static class Diagnosis {
        @SerializedName("diversification") public Diversification diversification;
        @SerializedName("risk") public Risk risk;
        @SerializedName("idle_cash_percent") public Double idleCashPercent;
    }

    public static class Diversification {
        @SerializedName("score") public Integer score;
        @SerializedName("label") public String label;
        @SerializedName("basis") public String basis;
    }

    public static class Risk {
        @SerializedName("level") public Double level;
        @SerializedName("label") public String label;
    }

    public static class StudyTrail {
        @SerializedName("trail_id") public int trailId;
        @SerializedName("trail") public String trail;
        @SerializedName("completed") public int completed;
        @SerializedName("total") public int total;
        @SerializedName("reason") public String reason;
    }
}
