package com.example.accurancymobileapp.utils;

import com.example.accurancymobileapp.model.FavoritoAtivo;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * In-memory copy of the user's favorites so every list can draw its hearts instantly.
 * The server is the source of truth: screens reload it and replace this copy.
 */
public final class FavoritosCache {

    private static final Set<String> CHAVES = new HashSet<>();

    private FavoritosCache() {
    }

    /** Maps a category label to the one stored by the API, or null when it cannot be favorited. */
    public static String categoriaCanonica(String categoria) {
        if (categoria == null) return null;
        String limpa = Normalizer.normalize(categoria.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
        if (limpa.startsWith("acoes") || limpa.startsWith("acao")) return "Ações";
        if (limpa.startsWith("fii")) return "FIIs";
        if (limpa.startsWith("cripto")) return "Cripto";
        if (limpa.startsWith("futuro")) return "Futuros";
        if (limpa.startsWith("internacional")) return "Internacional";
        return null;
    }

    private static String chave(String simbolo, String categoria) {
        String canonica = categoriaCanonica(categoria);
        if (simbolo == null || canonica == null) return null;
        return canonica + "|" + simbolo.trim().toUpperCase(Locale.ROOT);
    }

    public static synchronized boolean contem(String simbolo, String categoria) {
        String chave = chave(simbolo, categoria);
        return chave != null && CHAVES.contains(chave);
    }

    public static synchronized void definir(String simbolo, String categoria, boolean favorito) {
        String chave = chave(simbolo, categoria);
        if (chave == null) return;
        if (favorito) {
            CHAVES.add(chave);
        } else {
            CHAVES.remove(chave);
        }
    }

    public static synchronized void substituir(List<FavoritoAtivo> lista) {
        CHAVES.clear();
        if (lista == null) return;
        for (FavoritoAtivo item : lista) {
            String chave = chave(item.getSimbolo(), item.getCategoria());
            if (chave != null) CHAVES.add(chave);
        }
    }

    /** Called on logout so the next user never sees the previous user's hearts. */
    public static synchronized void limpar() {
        CHAVES.clear();
    }
}
