package com.example.accurancymobileapp.utils;

import android.widget.ImageView;

import com.example.accurancymobileapp.R;
import androidx.core.content.ContextCompat;

/** Draws the heart used on lists and details, and names categories for the user. */
public final class FavoritoUi {

    private static final int COR_FAVORITO = 0xFFFF4D6D;

    private FavoritoUi() {
    }

    public static void aplicar(ImageView coracao, boolean favorito) {
        coracao.setImageResource(favorito ? R.drawable.ic_favorito_cheio : R.drawable.ic_favorito_vazio);
        coracao.setColorFilter(favorito ? COR_FAVORITO : ContextCompat.getColor(coracao.getContext(), R.color.text_secondary));
        coracao.setContentDescription(favorito ? "Remover dos favoritos" : "Adicionar aos favoritos");
    }

    /** Same as {@link #aplicar} but pops the heart when it was just favorited. */
    public static void aplicarComAnimacao(ImageView coracao, boolean favorito) {
        aplicar(coracao, favorito);
        if (favorito) {
            AnimacaoUtils.pulsar(coracao);
        }
    }

    /** User-facing category name ("Internacional" holds the foreign currencies). */
    public static String rotuloCategoria(String categoria) {
        String canonica = FavoritosCache.categoriaCanonica(categoria);
        if (canonica == null) return categoria == null ? "" : categoria;
        return "Internacional".equals(canonica) ? "Moedas" : canonica;
    }

    public static boolean categoriaFavoritavel(String categoria) {
        return FavoritosCache.categoriaCanonica(categoria) != null;
    }
}
