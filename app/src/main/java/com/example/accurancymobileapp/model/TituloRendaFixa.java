package com.example.accurancymobileapp.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Locale;

public class TituloRendaFixa implements Serializable {
    private static final long serialVersionUID = 1L;

    @SerializedName("id_titulo")
    private int idTitulo;

    @SerializedName("nome_titulo")
    String nomeTitulo;

    @SerializedName("tipo_titulo")
    String tipoTitulo;

    @SerializedName("rentabilidade")
    private BigDecimal rentabilidade;

    @SerializedName("indexador")
    private String indexador;

    @SerializedName("aplicacao_minima")
    private BigDecimal aplicacaoMinima;

    @SerializedName("vencimento")
    private String vencimento;

    @SerializedName("liquidez")
    private String liquidez;

    @SerializedName("taxa_administracao")
    private BigDecimal taxaAdministracao;

    @SerializedName("possui_garantia_fgc")
    private boolean possuiGarantiaFgc;

    @SerializedName("descricao")
    private String descricao;

    /** Category under which a title is stored in the favorites. */
    public static final String CATEGORIA_FAVORITO = "Renda Fixa";

    /** Favorite key of this title: the same RF-<id> symbol the contributions use. */
    public String getSimboloFavorito() {
        return "RF-" + idTitulo;
    }

    public int getIdTitulo() {
        return idTitulo;
    }

    public String getNomeTitulo() {
        return nomeTitulo;
    }

    public String getTipoTitulo() {
        return tipoTitulo;
    }

    public BigDecimal getRentabilidade() {
        return rentabilidade;
    }

    public String getIndexador() {
        return indexador;
    }

    public BigDecimal getAplicacaoMinima() {
        return aplicacaoMinima;
    }

    public String getVencimento() {
        return vencimento;
    }

    public String getLiquidez() {
        return liquidez;
    }

    public BigDecimal getTaxaAdministracao() {
        return taxaAdministracao;
    }

    public boolean possuiGarantiaFgc() {
        return possuiGarantiaFgc;
    }

    public String getDescricao() {
        return descricao;
    }

    /** Fixed income titles from LCI/LCA are exempt from income tax for individuals. */
    public boolean isIsentoIR() {
        String tipo = tipoTitulo == null ? "" : tipoTitulo.trim().toUpperCase(Locale.ROOT);
        return tipo.startsWith("LCI") || tipo.startsWith("LCA");
    }

    /** Example: "105% CDI", "12,8% PRE" or "6,1% + IPCA". */
    public String getRentabilidadeFormatada() {
        if (rentabilidade == null) {
            return "Não informada";
        }
        String valor = rentabilidade.stripTrailingZeros().toPlainString().replace('.', ',');
        String idx = indexador == null ? "" : indexador.trim();
        if (idx.equalsIgnoreCase("IPCA")) {
            return valor + "% + IPCA";
        }
        return idx.isEmpty() ? valor + "%" : valor + "% " + idx;
    }

    /** Converts the ISO maturity date (yyyy-MM-dd) to dd/MM/yyyy; returns the raw text if it is not ISO. */
    public String getVencimentoFormatado() {
        if (vencimento == null || vencimento.trim().isEmpty()) {
            return "Não informado";
        }
        String texto = vencimento.trim();
        if (texto.length() >= 10 && texto.charAt(4) == '-' && texto.charAt(7) == '-') {
            return texto.substring(8, 10) + "/" + texto.substring(5, 7) + "/" + texto.substring(0, 4);
        }
        return texto;
    }
}
