package com.example.accurancymobileapp.utils;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.accurancymobileapp.model.Wallet;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Escolha da carteira de destino de uma operação. Mostra quanto cada carteira tem para investir
 * (saldo geral nas reais, saldo da simulação nas simuladas) e deixa marcada a carteira em foco.
 */
public final class EscolhaCarteira {

    public interface AoEscolher {
        void aoEscolher(@NonNull Wallet carteira);
    }

    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));

    private EscolhaCarteira() {}

    /** Dinheiro disponível para compras nessa carteira; null quando não se sabe (o servidor confere). */
    @Nullable
    public static BigDecimal disponivel(@NonNull Wallet carteira, @Nullable BigDecimal saldoGeral) {
        return carteira.isSimulada() ? carteira.getSaldoLivre() : saldoGeral;
    }

    public static String descrever(@NonNull Wallet carteira, @Nullable BigDecimal saldoGeral) {
        if (carteira.isSimulada()) {
            return carteira.getNome_carteira() + " · simulada (" + MOEDA.format(carteira.getSaldoLivre()) + " livres)";
        }
        return saldoGeral != null
                ? carteira.getNome_carteira() + " · real (saldo geral " + MOEDA.format(saldoGeral) + ")"
                : carteira.getNome_carteira() + " · real";
    }

    /** Mensagem de saldo insuficiente para o valor, ou null quando o dinheiro da carteira basta. */
    @Nullable
    public static String faltaSaldo(@NonNull Wallet carteira, @Nullable BigDecimal saldoGeral, @NonNull BigDecimal valor) {
        BigDecimal disponivel = disponivel(carteira, saldoGeral);
        if (disponivel == null || valor.compareTo(disponivel) <= 0) return null;
        return (carteira.isSimulada() ? "Saldo da simulação insuficiente. " : "Saldo insuficiente. ")
                + "Disponível em \"" + carteira.getNome_carteira() + "\": " + MOEDA.format(disponivel) + ".";
    }

    public static void mostrar(@NonNull Context contexto, @NonNull String titulo, @NonNull List<Wallet> carteiras,
                               @Nullable BigDecimal saldoGeral, @NonNull AoEscolher aoEscolher,
                               @NonNull Runnable aoCancelar) {
        Wallet[] opcoes = carteiras.toArray(new Wallet[0]);
        int emFoco = CarteiraEmFoco.obter(contexto);
        int marcada = 0;
        CharSequence[] nomes = new CharSequence[opcoes.length];
        for (int i = 0; i < opcoes.length; i++) {
            nomes[i] = descrever(opcoes[i], saldoGeral);
            if (opcoes[i].getId_carteira() == emFoco) marcada = i;
        }
        final int[] escolhida = {marcada};

        new MaterialAlertDialogBuilder(contexto)
                .setTitle(titulo)
                .setSingleChoiceItems(nomes, marcada, (dialogo, qual) -> escolhida[0] = qual)
                .setPositiveButton("Confirmar", (dialogo, qual) -> aoEscolher.aoEscolher(opcoes[escolhida[0]]))
                .setNegativeButton("Cancelar", (dialogo, qual) -> aoCancelar.run())
                .setOnCancelListener(dialogo -> aoCancelar.run())
                .show();
    }
}
