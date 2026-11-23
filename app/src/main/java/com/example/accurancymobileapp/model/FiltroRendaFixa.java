package com.example.accurancymobileapp.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Filter and sort choices applied to the fixed income / Treasury title lists. */
public class FiltroRendaFixa implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Ordem {
        PADRAO("Padrão"),
        MAIOR_RENTABILIDADE("Maior rentabilidade"),
        MENOR_MINIMO("Menor aplicação mínima"),
        VENCIMENTO("Vencimento mais próximo");

        private final String rotulo;

        Ordem(String rotulo) {
            this.rotulo = rotulo;
        }

        public String getRotulo() {
            return rotulo;
        }
    }

    /** Title type such as "CDB" or "LCI"; null means all. */
    public String tipo;
    /** Indexer such as "CDI" or "IPCA"; null means all. */
    public String indexador;
    public boolean somenteFgc;
    public boolean somenteIsentoIR;
    public Ordem ordem = Ordem.PADRAO;

    /** Number of active choices, shown as a badge on the filter button. */
    public int quantidadeAtiva() {
        int total = 0;
        if (tipo != null) total++;
        if (indexador != null) total++;
        if (somenteFgc) total++;
        if (somenteIsentoIR) total++;
        if (ordem != Ordem.PADRAO) total++;
        return total;
    }

    public boolean estaAtivo() {
        return quantidadeAtiva() > 0;
    }

    /** Type label used by the chips: the title type as stored ("CDB", "LCI", "Tesouro Selic"...). */
    public static String rotuloTipo(TituloRendaFixa titulo) {
        String tipo = titulo.getTipoTitulo();
        if (tipo == null || tipo.trim().isEmpty()) return null;
        return tipo.trim();
    }

    /** Distinct, sorted type labels present in the given titles. */
    public static List<String> tiposDisponiveis(List<TituloRendaFixa> titulos) {
        java.util.TreeSet<String> valores = new java.util.TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (TituloRendaFixa titulo : titulos) {
            String rotulo = rotuloTipo(titulo);
            if (rotulo != null) valores.add(rotulo);
        }
        return new ArrayList<>(valores);
    }

    /** Distinct, sorted indexers present in the given titles. */
    public static List<String> indexadoresDisponiveis(List<TituloRendaFixa> titulos) {
        java.util.TreeSet<String> valores = new java.util.TreeSet<>();
        for (TituloRendaFixa titulo : titulos) {
            String idx = titulo.getIndexador();
            if (idx != null && !idx.trim().isEmpty()) valores.add(idx.trim().toUpperCase(Locale.ROOT));
        }
        return new ArrayList<>(valores);
    }

    public boolean aceita(TituloRendaFixa titulo) {
        if (tipo != null && !tipo.equalsIgnoreCase(rotuloTipo(titulo))) return false;
        if (indexador != null) {
            String idx = titulo.getIndexador();
            if (idx == null || !indexador.equalsIgnoreCase(idx.trim())) return false;
        }
        if (somenteFgc && !titulo.possuiGarantiaFgc()) return false;
        return !somenteIsentoIR || titulo.isIsentoIR();
    }

    /** Returns a new list with the accepted titles, sorted by the chosen order. */
    public List<TituloRendaFixa> aplicar(List<TituloRendaFixa> origem) {
        List<TituloRendaFixa> resultado = new ArrayList<>();
        for (TituloRendaFixa titulo : origem) {
            if (aceita(titulo)) resultado.add(titulo);
        }
        Comparator<TituloRendaFixa> comparador = null;
        if (ordem == Ordem.MAIOR_RENTABILIDADE) {
            comparador = (a, b) -> compararNumero(a.getRentabilidade(), b.getRentabilidade(), true);
        } else if (ordem == Ordem.MENOR_MINIMO) {
            comparador = (a, b) -> compararNumero(a.getAplicacaoMinima(), b.getAplicacaoMinima(), false);
        } else if (ordem == Ordem.VENCIMENTO) {
            comparador = (a, b) -> {
                String va = a.getVencimento() == null ? "" : a.getVencimento().trim();
                String vb = b.getVencimento() == null ? "" : b.getVencimento().trim();
                if (va.isEmpty() && vb.isEmpty()) return 0;
                if (va.isEmpty()) return 1;
                if (vb.isEmpty()) return -1;
                return va.compareTo(vb);
            };
        }
        if (comparador != null) {
            Collections.sort(resultado, comparador);
        }
        return resultado;
    }

    /** Numeric comparison (ascending or descending) where missing values always go last. */
    private static int compararNumero(BigDecimal a, BigDecimal b, boolean decrescente) {
        if (a == null && b == null) return 0;
        if (a == null) return 1;
        if (b == null) return -1;
        return decrescente ? b.compareTo(a) : a.compareTo(b);
    }
}
