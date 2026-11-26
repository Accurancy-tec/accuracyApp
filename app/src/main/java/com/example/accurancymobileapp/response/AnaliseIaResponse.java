package com.example.accurancymobileapp.response;

import com.google.gson.annotations.SerializedName;

/** Resposta de POST /relatorios/analise (também vem no erro 429, com a última análise). */
public class AnaliseIaResponse extends RespostaSimples {

    @SerializedName("analise")
    public RelatorioResponse.Analise analise;
}
