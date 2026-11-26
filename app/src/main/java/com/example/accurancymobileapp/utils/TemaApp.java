package com.example.accurancymobileapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Tema do aplicativo (claro ou escuro), escolhido no card "Aparência" do perfil.
 *
 * Fica num arquivo de preferências próprio: sair da conta não deve trocar o tema.
 * O padrão é o escuro, que é a identidade visual original do Accuracy.
 */
public final class TemaApp {

    private static final String PREFERENCIAS = "aparencia_prefs";
    private static final String CHAVE_TEMA = "tema";
    private static final String CLARO = "claro";
    private static final String ESCURO = "escuro";

    private TemaApp() {}

    private static SharedPreferences prefs(Context contexto) {
        return contexto.getApplicationContext().getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE);
    }

    public static boolean claroAtivo(Context contexto) {
        return CLARO.equals(prefs(contexto).getString(CHAVE_TEMA, ESCURO));
    }

    /** Aplica o tema salvo. Chamado ao abrir o app, antes da primeira tela. */
    public static void aplicarSalvo(Context contexto) {
        AppCompatDelegate.setDefaultNightMode(claroAtivo(contexto)
                ? AppCompatDelegate.MODE_NIGHT_NO
                : AppCompatDelegate.MODE_NIGHT_YES);
    }

    /** Troca entre claro e escuro; as telas abertas são recriadas com a nova paleta. */
    public static void alternar(Context contexto) {
        prefs(contexto).edit().putString(CHAVE_TEMA, claroAtivo(contexto) ? ESCURO : CLARO).apply();
        aplicarSalvo(contexto);
    }

    public static String nomeAtual(Context contexto) {
        return claroAtivo(contexto) ? "Tema claro" : "Tema escuro";
    }
}
