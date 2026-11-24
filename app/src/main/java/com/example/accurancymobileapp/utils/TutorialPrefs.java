package com.example.accurancymobileapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Remembers whether the first-use tutorial is waiting to be shown.
 *
 * It lives in its own preferences file because {@link SessionManager#logout()} wipes the session
 * file: a user who signs out and back in must not see the tour again.
 */
public final class TutorialPrefs {

    private static final String PREF_NAME = "tutorial_prefs";
    private static final String KEY_PENDENTE = "email_pendente";
    private static final String KEY_VISTOS = "vistos";

    private TutorialPrefs() {}

    private static SharedPreferences prefs(Context contexto) {
        return contexto.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private static String normalizar(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    /** The account was just created (or just confirmed): show the tour on the next entry to the app. */
    public static void marcarPendente(Context contexto, String email) {
        String chave = normalizar(email);
        if (chave.isEmpty()) chave = "?";
        prefs(contexto).edit().putString(KEY_PENDENTE, chave).apply();
    }

    public static boolean pendente(Context contexto) {
        return prefs(contexto).getString(KEY_PENDENTE, null) != null;
    }

    /** True when this e-mail already went through (or skipped) the tour on this device. */
    public static boolean jaViu(Context contexto, String email) {
        return prefs(contexto).getStringSet(KEY_VISTOS, new HashSet<>()).contains(normalizar(email));
    }

    /** Finished or skipped: do not show it again to this account. */
    public static void concluir(Context contexto) {
        SharedPreferences p = prefs(contexto);
        String chave = p.getString(KEY_PENDENTE, null);
        Set<String> vistos = new HashSet<>(p.getStringSet(KEY_VISTOS, new HashSet<>()));
        if (chave != null) vistos.add(chave);
        p.edit().putStringSet(KEY_VISTOS, vistos).remove(KEY_PENDENTE).apply();
    }
}
