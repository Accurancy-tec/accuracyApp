package com.example.accurancymobileapp.utils;

import android.app.AlertDialog;
import android.content.Context;

import com.example.accurancymobileapp.model.Aporte;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Formatting helpers for the contribution details returned by the API
 * (detalhe_renda_fixa / detalhe_renda_variavel). Pure presentation: nothing here
 * changes balances or positions.
 */
public final class AporteDetalhes {

    private static final Locale PT_BR = new Locale("pt", "BR");

    private AporteDetalhes() {
    }

    public static String formatarMoeda(double valor) {
        return NumberFormat.getCurrencyInstance(PT_BR).format(valor);
    }

    /** Converts yyyy-MM-dd to dd/MM/yyyy; returns the raw text when it is not an ISO date. */
    public static String formatarData(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(valor.trim()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy", PT_BR));
        } catch (Exception ignored) {
            return valor;
        }
    }

    /** Example: "105% CDI" or "6,1% + IPCA". */
    public static String formatarTaxa(double taxa, String indexador) {
        String valor = BigDecimal.valueOf(taxa).stripTrailingZeros().toPlainString().replace('.', ',');
        String idx = indexador == null ? "" : indexador.trim();
        if (idx.equalsIgnoreCase("IPCA")) {
            return valor + "% + IPCA";
        }
        return idx.isEmpty() ? valor + "%" : valor + "% " + idx;
    }

    /** Subtitle for fixed income rows: "105% CDI • Vence 01/04/2027 • 19/11/2026". */
    public static String subtituloRendaFixa(Aporte aporte) {
        StringBuilder texto = new StringBuilder();
        Aporte.DetalheRendaFixa detalhe = aporte.getDetalheRendaFixa();
        if (detalhe != null) {
            texto.append(formatarTaxa(detalhe.getTaxaContratada(), detalhe.getIndexador()));
            String vencimento = formatarData(detalhe.getVencimento());
            if (vencimento != null) {
                texto.append(" • Vence ").append(vencimento);
            }
        }
        String data = formatarData(aporte.getData_aporte());
        if (data != null) {
            if (texto.length() > 0) {
                texto.append(" • ");
            }
            texto.append(data);
        }
        return texto.length() == 0 ? "Renda Fixa" : texto.toString();
    }

    /** Extra text for variable income rows, e.g. "R$ 38,01/un."; null when there is no detail. */
    public static String precoUnitarioTexto(Aporte aporte) {
        Aporte.DetalheRendaVariavel detalhe = aporte.getDetalheRendaVariavel();
        if (detalhe == null || detalhe.getPrecoUnitario() == null || detalhe.getPrecoUnitario() <= 0.0) {
            return null;
        }
        return formatarMoeda(detalhe.getPrecoUnitario()) + "/un.";
    }

    /** True when there is something to show in the details dialog. */
    public static boolean temDetalhes(Aporte aporte) {
        return aporte.getDetalheRendaFixa() != null || aporte.getDetalheRendaVariavel() != null;
    }

    public static void mostrarDialogo(Context context, Aporte aporte) {
        StringBuilder mensagem = new StringBuilder();
        mensagem.append("Operação: ").append(semNulo(aporte.getTipoAporte(), "Movimentação")).append('\n');
        mensagem.append("Valor: ").append(formatarMoeda(aporte.getValorAporte())).append('\n');

        Aporte.DetalheRendaFixa fixa = aporte.getDetalheRendaFixa();
        Aporte.DetalheRendaVariavel variavel = aporte.getDetalheRendaVariavel();
        if (fixa != null) {
            mensagem.append("Taxa contratada: ").append(formatarTaxa(fixa.getTaxaContratada(), fixa.getIndexador())).append('\n');
            String vencimento = formatarData(fixa.getVencimento());
            if (vencimento != null) {
                mensagem.append("Vencimento: ").append(vencimento).append('\n');
            }
            if (fixa.getLiquidez() != null && !fixa.getLiquidez().trim().isEmpty()) {
                mensagem.append("Resgate: ").append(fixa.getLiquidez().trim()).append('\n');
            }
        } else if (variavel != null) {
            if (variavel.getQuantidade() != null) {
                double quantidade = variavel.getQuantidade();
                String qtd = quantidade == Math.rint(quantidade)
                        ? String.valueOf((long) quantidade)
                        : String.format(PT_BR, "%.4f", quantidade);
                mensagem.append("Quantidade: ").append(qtd).append('\n');
            }
            if (variavel.getPrecoUnitario() != null) {
                mensagem.append("Preço unitário: ").append(formatarMoeda(variavel.getPrecoUnitario())).append('\n');
            }
        }
        String data = formatarData(aporte.getData_aporte());
        if (data != null) {
            mensagem.append("Data: ").append(data);
        }

        new AlertDialog.Builder(context)
                .setTitle(semNulo(aporte.getNameAtivo(), semNulo(aporte.getAtivoAporte(), "Aporte")))
                .setMessage(mensagem.toString().trim())
                .setPositiveButton("Fechar", null)
                .show();
    }

    private static String semNulo(String valor, String padrao) {
        return valor == null || valor.trim().isEmpty() ? padrao : valor;
    }
}
