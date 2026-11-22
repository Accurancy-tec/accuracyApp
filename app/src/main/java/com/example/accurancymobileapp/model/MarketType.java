package com.example.accurancymobileapp.model;

public enum MarketType {

    FIIS("FIIS", "Fundos Imobiliários", "FIIs"),

    ACOES_NACIONAIS("ACOES_NACIONAIS", "Ações Nacionais", "Ações"),
    BDRS_ETFS("BDRS_ETFS", "Ações e ETFs Internacionais", "Ações"),
    FUTUROS("FUTUROS", "Contratos Futuros", "Futuros"),
    CRIPTO("CRIPTO", "Criptomoedas", "Cripto"),
    MOEDAS("MOEDAS", "Moedas Estrangeiras", "Internacional");

    private final String tag;
    private final String title;
    private final String detailCategory;

    MarketType(String tag, String title, String detailCategory) {
        this.tag = tag;
        this.title = title;
        this.detailCategory = detailCategory;
    }

    public String getTag() {
        return tag;
    }

    public String getTitle() {
        return title;
    }

    public String getDetailCategory() {
        return detailCategory;
    }

    /** Crypto and currency markets share a simple catalog: only rank/name sorting, no volume. */
    public boolean isCatalogoSimples() {
        return this == CRIPTO || this == MOEDAS;
    }

    public static MarketType fromTag(String tag) {
        if (tag == null) return null;

        for (MarketType type : values()) {
            if (type.tag.equalsIgnoreCase(tag)) {
                return type;
            }
        }

        return null;
    }
}