package com.example.accurancymobileapp.model;

import com.google.gson.annotations.SerializedName;

/** An asset the wallet holds now, priced by the API (carteiras/resumo, field "ativos"). */
public class Position {

    @SerializedName("simbolo_ativo") public String symbol;
    @SerializedName("nome_ativo") public String name;
    @SerializedName("categoria_ativo") public String category;
    @SerializedName("quantidade") public double quantity;
    @SerializedName("preco_medio") public double averagePrice;
    @SerializedName("preco_atual") public double currentPrice;
    @SerializedName("valor_atual") public double currentValue;
    @SerializedName("lucro_prejuizo") public double result;
    @SerializedName("rentabilidade_percentual") public double returnPercent;

    public boolean isFixedIncome() {
        return "Renda Fixa".equals(category);
    }
}
