package com.example.accurancymobileapp.utils;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.example.accurancymobileapp.R;

/** Marks one row of a list (blue outline plus a short pulse), used when a search opens a list. */
public final class DestaqueLinha {

    private DestaqueLinha() {}

    /**
     * Call from onBindViewHolder for every row: it also puts the normal background back, because
     * rows are recycled.
     */
    public static void aplicar(@NonNull View linha, boolean destacada, @DrawableRes int fundoNormal) {
        linha.animate().cancel();
        linha.setScaleX(1f);
        linha.setScaleY(1f);
        if (!destacada) {
            linha.setBackgroundResource(fundoNormal);
            return;
        }
        linha.setBackgroundResource(R.drawable.bg_card_destaque);
        ObjectAnimator pulso = ObjectAnimator.ofPropertyValuesHolder(linha,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.03f, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.03f, 1f));
        pulso.setDuration(520);
        pulso.setRepeatCount(1);
        pulso.setStartDelay(250);
        pulso.setInterpolator(new AccelerateDecelerateInterpolator());
        pulso.start();
    }
}
