package com.example.accurancymobileapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

/**
 * Carteira que a pessoa abriu antes de ir investir ("Simular investimento" / "Investir").
 * A tela de compra usa para deixar essa carteira já marcada na hora de escolher onde registrar.
 */
public final class CarteiraEmFoco {

    private static final String PREFERENCIAS = "carteira_em_foco";
    private static final String CHAVE_ID = "id_carteira";
    private static final String CHAVE_DEFINIDA_EM = "definida_em";
    /** Depois de meia hora a escolha deixa de valer, para não surpreender numa compra futura. */
    private static final long VALIDADE_MS = 30 * 60 * 1000L;

    private CarteiraEmFoco() {}

    private static SharedPreferences prefs(Context contexto) {
        return contexto.getApplicationContext().getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE);
    }

    public static void definir(@NonNull Context contexto, int idCarteira) {
        prefs(contexto).edit()
                .putInt(CHAVE_ID, idCarteira)
                .putLong(CHAVE_DEFINIDA_EM, System.currentTimeMillis())
                .apply();
    }

    /** Id da carteira em foco, ou -1 se não houver (ou se a escolha já expirou). */
    public static int obter(@NonNull Context contexto) {
        SharedPreferences p = prefs(contexto);
        long definidaEm = p.getLong(CHAVE_DEFINIDA_EM, 0L);
        if (System.currentTimeMillis() - definidaEm > VALIDADE_MS) return -1;
        return p.getInt(CHAVE_ID, -1);
    }

    public static void limpar(@NonNull Context contexto) {
        prefs(contexto).edit().clear().apply();
    }
}
