package com.example.accurancymobileapp.utils;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import java.util.Locale;

/**
 * Small, consistent animations used by the app screens: soft entrance, press feedback,
 * validation shake and a balance count-up. All of them are short (under 400 ms) and skip
 * silently when the view is not attached anymore.
 */
public final class AnimacaoUtils {

    private static final long DURACAO_ENTRADA = 320L;
    private static final long ATRASO_ENTRE_ITENS = 55L;

    private AnimacaoUtils() {
    }

    private static float dp(View view, float valor) {
        return valor * view.getResources().getDisplayMetrics().density;
    }

    /** Fades the views in while sliding them up a few dp, one after another. */
    public static void entradaEmCascata(View... views) {
        long atraso = 0L;
        for (View view : views) {
            if (view == null) {
                continue;
            }
            // Keep the alpha the view already had (e.g. 0.5 for a disabled button).
            final float alphaFinal = view.getAlpha();
            view.setAlpha(0f);
            view.setTranslationY(dp(view, 14));
            view.animate()
                    .alpha(alphaFinal)
                    .translationY(0f)
                    .setStartDelay(atraso)
                    .setDuration(DURACAO_ENTRADA)
                    .setInterpolator(new DecelerateInterpolator(1.6f))
                    .start();
            atraso += ATRASO_ENTRE_ITENS;
        }
    }

    /** Entrance for a list row; only the first rows are staggered so scrolling never feels slow. */
    public static void entradaItemLista(View item, int posicao) {
        item.animate().cancel();
        item.setAlpha(0f);
        item.setTranslationY(dp(item, 18));
        item.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(Math.min(posicao, 6) * 45L)
                .setDuration(DURACAO_ENTRADA)
                .setInterpolator(new DecelerateInterpolator(1.4f))
                .start();
    }

    /** Slightly shrinks the view while it is pressed. The click is still delivered normally. */
    @SuppressLint("ClickableViewAccessibility")
    public static void efeitoPressionar(View view) {
        view.setOnTouchListener((v, evento) -> {
            switch (evento.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(90L).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(140L)
                            .setInterpolator(new OvershootInterpolator(2f)).start();
                    break;
                default:
                    break;
            }
            return false;
        });
    }

    /** Quick horizontal shake used when an input is invalid. */
    public static void tremer(View view) {
        ObjectAnimator animador = ObjectAnimator.ofFloat(view, View.TRANSLATION_X,
                0f, -dp(view, 8), dp(view, 8), -dp(view, 5), dp(view, 5), 0f);
        animador.setDuration(360L);
        animador.setInterpolator(new AccelerateDecelerateInterpolator());
        animador.start();
    }

    /** Pop animation used to confirm a successful operation. */
    public static void destacar(View view) {
        view.setScaleX(0.92f);
        view.setScaleY(0.92f);
        view.animate().scaleX(1f).scaleY(1f).setDuration(320L)
                .setInterpolator(new OvershootInterpolator(3f)).start();
    }

    /** Counts a money value up to its final amount (e.g. the available balance). */
    public static void contarAte(TextView texto, double valorFinal, String prefixo) {
        ValueAnimator animador = ValueAnimator.ofFloat(0f, (float) valorFinal);
        animador.setDuration(650L);
        animador.setInterpolator(new DecelerateInterpolator(2f));
        animador.addUpdateListener(animacao -> {
            if (!texto.isAttachedToWindow()) {
                animacao.cancel();
                return;
            }
            float atual = (float) animacao.getAnimatedValue();
            texto.setText(prefixo + String.format(new Locale("pt", "BR"), "R$ %,.2f", atual));
        });
        animador.start();
    }
}
