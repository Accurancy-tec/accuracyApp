package com.example.accurancymobileapp.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.accurancymobileapp.R;

/** A plain view that shows the shimmer placeholder while it is on screen. */
public class ShimmerView extends View {

    private final ShimmerDrawable shimmer;

    public ShimmerView(Context contexto) {
        this(contexto, null);
    }

    public ShimmerView(Context contexto, @Nullable AttributeSet atributos) {
        super(contexto, atributos);
        float raio = 8f * getResources().getDisplayMetrics().density;
        if (atributos != null) {
            TypedArray a = contexto.obtainStyledAttributes(atributos, R.styleable.ShimmerView);
            try {
                raio = a.getDimension(R.styleable.ShimmerView_raioShimmer, raio);
            } finally {
                a.recycle();
            }
        }
        shimmer = new ShimmerDrawable(contexto, raio);
        setBackground(shimmer);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        shimmer.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        shimmer.stop();
        super.onDetachedFromWindow();
    }
}
