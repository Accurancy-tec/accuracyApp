package com.example.accurancymobileapp.utils;

import android.os.Build;

import java.util.Locale;

/** Nome deste celular, enviado no login para aparecer em "Aparelhos conectados". */
public final class Aparelho {

    private Aparelho() {}

    /** "Samsung SM-A546E · Android 14". Modelos que já começam pela marca não a repetem. */
    public static String nome() {
        String marca = Build.MANUFACTURER == null ? "" : Build.MANUFACTURER.trim();
        String modelo = Build.MODEL == null ? "" : Build.MODEL.trim();

        String nome;
        if (marca.isEmpty() || modelo.toLowerCase(Locale.ROOT).startsWith(marca.toLowerCase(Locale.ROOT))) {
            nome = modelo;
        } else {
            nome = marca.substring(0, 1).toUpperCase(Locale.ROOT) + marca.substring(1) + " " + modelo;
        }
        nome = nome.trim();
        if (nome.isEmpty()) nome = "Celular Android";
        return nome + " · Android " + Build.VERSION.RELEASE;
    }
}
