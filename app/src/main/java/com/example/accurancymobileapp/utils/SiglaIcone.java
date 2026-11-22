package com.example.accurancymobileapp.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Icon used by the asset rows when there is no logo: the first two letters of the symbol
 * (WDOZ26 -> "WD", PETR4 -> "PE", BTC -> "BT"). Without any letter or digit the old icon stays.
 */
public final class SiglaIcone {

    private static final int COR_TEXTO = Color.parseColor("#57A6FF");
    private static final Pattern PARECE_SIMBOLO = Pattern.compile("^[A-Z0-9.\\-=^]{2,12}$");

    private SiglaIcone() { }

    /** First two letters/digits of the symbol in upper case, or null when it has none. */
    @Nullable
    public static String sigla(@Nullable String simbolo) {
        if (simbolo == null) return null;
        StringBuilder sigla = new StringBuilder(2);
        for (int i = 0; i < simbolo.length() && sigla.length() < 2; i++) {
            char c = simbolo.charAt(i);
            if (Character.isLetterOrDigit(c)) sigla.append(c);
        }
        return sigla.length() == 0 ? null : sigla.toString().toUpperCase(Locale.ROOT);
    }

    /** Shows the initials of the symbol, or the placeholder icon when there are none. */
    public static void aplicar(@NonNull ImageView imageView, @Nullable String simbolo, @DrawableRes int placeholder) {
        String sigla = sigla(simbolo);
        if (sigla == null) {
            imageView.setImageResource(placeholder);
        } else {
            imageView.setImageDrawable(new SiglaDrawable(sigla));
        }
    }

    /**
     * Same as {@link #aplicar}, but only for texts that look like a ticker (upper case, no spaces):
     * names such as "CDB Banco X" keep the placeholder icon.
     */
    public static void aplicarSeSimbolo(@NonNull ImageView imageView, @Nullable String texto, @DrawableRes int placeholder) {
        String limpo = texto == null ? null : texto.trim();
        aplicar(imageView, limpo != null && PARECE_SIMBOLO.matcher(limpo).matches() ? limpo : null, placeholder);
    }

    /** Draws the initials centered; it scales with the ImageView (fitCenter) so it stays sharp. */
    private static final class SiglaDrawable extends Drawable {
        private static final int TAMANHO = 96;
        private final String texto;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        SiglaDrawable(String texto) {
            this.texto = texto;
            paint.setColor(COR_TEXTO);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            paint.setTextAlign(Paint.Align.CENTER);
        }

        @Override public int getIntrinsicWidth() { return TAMANHO; }
        @Override public int getIntrinsicHeight() { return TAMANHO; }

        @Override
        public void draw(@NonNull Canvas canvas) {
            int largura = getBounds().width();
            int altura = getBounds().height();
            if (largura <= 0 || altura <= 0) return;
            paint.setTextSize(altura * 0.42f);
            float centroY = getBounds().top + altura / 2f - (paint.descent() + paint.ascent()) / 2f;
            canvas.drawText(texto, getBounds().left + largura / 2f, centroY, paint);
        }

        @Override public void setAlpha(int alpha) { paint.setAlpha(alpha); invalidateSelf(); }
        @Override public void setColorFilter(@Nullable ColorFilter filtro) { paint.setColorFilter(filtro); invalidateSelf(); }
        @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    }
}
