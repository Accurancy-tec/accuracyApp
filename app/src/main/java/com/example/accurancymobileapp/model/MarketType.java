package com.example.accurancymobileapp.model;

public enum MarketType {

    FIIS("FIIS", "Fundos Imobiliários", "FIIs"),

    ACOES_NACIONAIS("ACOES_NACIONAIS", "Ações Nacionais", "Ações");

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