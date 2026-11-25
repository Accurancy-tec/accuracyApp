package com.example.accurancymobileapp.utils;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.ui.ShimmerDrawable;

/**
 * Turns existing views into loading placeholders (a shimmering bar where the text or image will be)
 * and gives them back, with a soft fade, once the data arrives. Works on any TextView, ImageView or
 * plain View without changing the layout.
 */
public final class Esqueleto {

    private static final int LARGURA_MINIMA_TEXTO_DP = 72;

    private Esqueleto() {}

    private static final class Original {
        Drawable fundo;
        ColorStateList corTexto;
        int larguraMinima;
        int alphaImagem;
    }

    /** Starts the placeholder on each view (calling it twice on the same view does nothing). */
    public static void iniciar(@NonNull View... views) {
        for (View v : views) {
            if (v == null || v.getTag(R.id.tag_esqueleto) != null) continue;
            Original o = new Original();
            o.fundo = v.getBackground();
            float densidade = v.getResources().getDisplayMetrics().density;
            if (v instanceof TextView) {
                TextView t = (TextView) v;
                o.corTexto = t.getTextColors();
                o.larguraMinima = t.getMinWidth();
                t.setTextColor(Color.TRANSPARENT);
                t.setMinWidth(Math.max(o.larguraMinima, Math.round(LARGURA_MINIMA_TEXTO_DP * densidade)));
            } else if (v instanceof ImageView) {
                ImageView i = (ImageView) v;
                o.alphaImagem = i.getImageAlpha();
                i.setImageAlpha(0);
            }
            v.setTag(R.id.tag_esqueleto, o);
            ShimmerDrawable shimmer = new ShimmerDrawable(6f * densidade);
            v.setBackground(shimmer);
            shimmer.start();
        }
    }

    /** Gives the views their normal look back and fades them in. Safe to call when not started. */
    public static void parar(@NonNull View... views) {
        for (View v : views) {
            if (v == null) continue;
            Object guardado = v.getTag(R.id.tag_esqueleto);
            if (!(guardado instanceof Original)) continue;
            Original o = (Original) guardado;
            Drawable atual = v.getBackground();
            if (atual instanceof ShimmerDrawable) ((ShimmerDrawable) atual).stop();
            v.setBackground(o.fundo);
            if (v instanceof TextView) {
                TextView t = (TextView) v;
                if (o.corTexto != null) t.setTextColor(o.corTexto);
                t.setMinWidth(o.larguraMinima);
            } else if (v instanceof ImageView) {
                ((ImageView) v).setImageAlpha(o.alphaImagem);
            }
            v.setTag(R.id.tag_esqueleto, null);
            revelar(v);
        }
    }

    public static boolean ativo(@NonNull View v) {
        return v.getTag(R.id.tag_esqueleto) instanceof Original;
    }

    /** Soft entrance for something that just finished loading. */
    public static void revelar(@NonNull View v) {
        v.animate().cancel();
        v.setAlpha(0f);
        v.animate().alpha(1f).setDuration(220).start();
    }
}
