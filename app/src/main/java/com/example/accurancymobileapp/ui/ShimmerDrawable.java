package com.example.accurancymobileapp.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.example.accurancymobileapp.R;

/**
 * Rounded bar with a light band sweeping across it: the "loading" placeholder.
 * Every shimmer shares the same clock, so bars side by side move together.
 */
public class ShimmerDrawable extends Drawable implements Animatable {

    private static final long PERIODO_MS = 1300L;

    private final Paint pincel = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF retangulo = new RectF();
    private final Matrix matriz = new Matrix();
    private final float raio;
    private final int corBase;
    private final int corBrilho;
    private LinearGradient gradiente;
    private float larguraBanda = 1f;
    @Nullable
    private ValueAnimator animador;

    /** @param raioPx corner radius in pixels; a value above half the size makes a pill/circle */
    public ShimmerDrawable(Context contexto, float raioPx) {
        this.raio = raioPx;
        this.corBase = ContextCompat.getColor(contexto, R.color.esqueleto_base);
        this.corBrilho = ContextCompat.getColor(contexto, R.color.esqueleto_brilho);
    }

    @Override
    protected void onBoundsChange(@NonNull android.graphics.Rect limites) {
        super.onBoundsChange(limites);
        retangulo.set(limites);
        larguraBanda = Math.max(limites.width(), 120f);
        gradiente = new LinearGradient(0, 0, larguraBanda, 0,
                new int[]{corBase, corBrilho, corBase},
                new float[]{0f, 0.5f, 1f}, Shader.TileMode.CLAMP);
        pincel.setShader(gradiente);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (gradiente == null || retangulo.isEmpty()) return;
        float fase = (SystemClock.uptimeMillis() % PERIODO_MS) / (float) PERIODO_MS;
        // The band goes from fully left of the bar to fully right of it.
        float deslocamento = -larguraBanda + (retangulo.width() + larguraBanda) * fase + retangulo.left;
        matriz.setTranslate(deslocamento, 0);
        gradiente.setLocalMatrix(matriz);
        float r = Math.min(raio, Math.min(retangulo.width(), retangulo.height()) / 2f);
        canvas.drawRoundRect(retangulo, r, r, pincel);
    }

    @Override
    public void start() {
        if (animador != null) return;
        animador = ValueAnimator.ofFloat(0f, 1f);
        animador.setDuration(PERIODO_MS);
        animador.setRepeatCount(ValueAnimator.INFINITE);
        animador.setInterpolator(new LinearInterpolator());
        animador.addUpdateListener(a -> {
            // A bar that lost its view must not keep a timer alive.
            if (getCallback() == null) {
                stop();
                return;
            }
            invalidateSelf();
        });
        animador.start();
    }

    @Override
    public void stop() {
        if (animador != null) {
            animador.cancel();
            animador = null;
        }
    }

    @Override
    public boolean isRunning() {
        return animador != null;
    }

    @Override
    public void setAlpha(int alpha) {
        pincel.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter filtro) {
        pincel.setColorFilter(filtro);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
