package com.example.accurancymobileapp.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.accurancymobileapp.R;

/**
 * Full-screen dimmed layer for the first-use tour: one rounded "hole" over the element being
 * explained (pulsing blue ring around it) and a card with title, text, progress dots and buttons.
 * It only draws and reports button taps; {@code TutorialTour} decides what each step shows.
 */
public class TutorialOverlay extends FrameLayout {

    public interface Acoes {
        void aoProximo();
        void aoVoltar();
        void aoPular();
    }

    public static final String TAG = "tutorial_overlay";

    private static final int COR_FUNDO = 0xD9040B16;
    private static final int COR_ANEL = 0xFF3B82F6;

    private final Spot spot;
    private final View card;
    private final TextView tvContador;
    private final TextView tvTitulo;
    private final TextView tvTexto;
    private final TextView btnPular;
    private final TextView btnVoltar;
    private final TextView btnProximo;
    private final LinearLayout llPontos;
    private final float dp;
    private Acoes acoes;
    private boolean fechando;

    public TutorialOverlay(@NonNull Context contexto) {
        super(contexto);
        dp = contexto.getResources().getDisplayMetrics().density;
        setTag(TAG);
        setClickable(true);
        setFocusable(true);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);

        spot = new Spot(contexto);
        addView(spot, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        card = LayoutInflater.from(contexto).inflate(R.layout.view_tutorial_card, this, false);
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.TOP;
        int margem = (int) (20 * dp);
        lp.leftMargin = margem;
        lp.rightMargin = margem;
        addView(card, lp);

        tvContador = card.findViewById(R.id.tvTutorialContador);
        tvTitulo = card.findViewById(R.id.tvTutorialTitulo);
        tvTexto = card.findViewById(R.id.tvTutorialTexto);
        btnPular = card.findViewById(R.id.btnTutorialPular);
        btnVoltar = card.findViewById(R.id.btnTutorialVoltar);
        btnProximo = card.findViewById(R.id.btnTutorialProximo);
        llPontos = card.findViewById(R.id.llTutorialPontos);

        btnPular.setOnClickListener(v -> { if (acoes != null) acoes.aoPular(); });
        btnVoltar.setOnClickListener(v -> { if (acoes != null) acoes.aoVoltar(); });
        btnProximo.setOnClickListener(v -> { if (acoes != null) acoes.aoProximo(); });

        card.setVisibility(INVISIBLE);
    }

    public void definirAcoes(Acoes acoes) {
        this.acoes = acoes;
    }

    /** Fades the layer in. Call once after adding it to the window. */
    public void aparecer() {
        setAlpha(0f);
        animate().alpha(1f).setDuration(250).start();
    }

    /** Fades out and removes itself from its parent. */
    public void fechar(@Nullable Runnable aoTerminar) {
        if (fechando) return;
        fechando = true;
        spot.parar();
        animate().alpha(0f).setDuration(200).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ViewGroup pai = (ViewGroup) getParent();
                if (pai != null) pai.removeView(TutorialOverlay.this);
                if (aoTerminar != null) aoTerminar.run();
            }
        }).start();
    }

    /** Offset of this layer inside the window, to convert window coordinates of a target. */
    public int[] posicaoNaJanela() {
        int[] p = new int[2];
        getLocationInWindow(p);
        return p;
    }

    /**
     * Shows one step.
     *
     * @param alvo hole position in this layer's coordinates, or null for a centered card.
     */
    public void mostrarPasso(int indice, int total, String titulo, String texto,
                             @Nullable RectF alvo, boolean primeiro, boolean ultimo) {
        tvContador.setText(indice + 1 + " de " + total);
        tvTitulo.setText(titulo);
        tvTexto.setText(texto);
        btnVoltar.setVisibility(primeiro ? INVISIBLE : VISIBLE);
        btnPular.setVisibility(ultimo ? INVISIBLE : VISIBLE);
        btnProximo.setText(ultimo ? "Concluir" : (primeiro ? "Começar" : "Próximo"));
        desenharPontos(indice, total);
        spot.irPara(alvo);
        posicionarCard(alvo, true);
    }

    /** The target moved (layout finished, data arrived): follow it without changing the text. */
    public void atualizarAlvo(@Nullable RectF alvo) {
        spot.irPara(alvo);
        posicionarCard(alvo, false);
    }

    private void desenharPontos(int ativo, int total) {
        llPontos.removeAllViews();
        for (int i = 0; i < total; i++) {
            boolean atual = i == ativo;
            View ponto = new View(getContext());
            GradientDrawable fundo = new GradientDrawable();
            fundo.setCornerRadius(8 * dp);
            fundo.setColor(atual ? 0xFF3B82F6 : (i < ativo ? 0xFF1E4FA8 : 0xFF334155));
            ponto.setBackground(fundo);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    (int) ((atual ? 20 : 7) * dp), (int) (7 * dp));
            lp.leftMargin = (int) (3 * dp);
            lp.rightMargin = (int) (3 * dp);
            llPontos.addView(ponto, lp);
        }
    }

    private void posicionarCard(@Nullable RectF alvo, boolean animar) {
        final int largura = getWidth();
        final int altura = getHeight();
        if (largura == 0 || altura == 0) {
            // Not laid out yet: try again as soon as it is.
            post(() -> posicionarCard(alvo, animar));
            return;
        }
        int margem = (int) (20 * dp);
        card.measure(
                MeasureSpec.makeMeasureSpec(largura - 2 * margem, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        int alturaCard = card.getMeasuredHeight();

        int topoSeguro = (int) (12 * dp);
        int baseSegura = altura - (int) (12 * dp);
        WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(this);
        if (insets != null) {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            topoSeguro += barras.top;
            baseSegura -= barras.bottom;
        }

        float y;
        if (alvo == null) {
            y = (topoSeguro + baseSegura - alturaCard) / 2f;
        } else {
            float folga = 14 * dp;
            float abaixo = alvo.bottom + folga;
            float acima = alvo.top - folga - alturaCard;
            if (abaixo + alturaCard <= baseSegura) {
                y = abaixo;
            } else if (acima >= topoSeguro) {
                y = acima;
            } else {
                // Neither side has room (very tall target): park the card at the bottom.
                y = baseSegura - alturaCard;
            }
        }
        y = Math.max(topoSeguro, Math.min(y, baseSegura - alturaCard));

        card.setVisibility(VISIBLE);
        if (animar) {
            card.setAlpha(0f);
            card.setTranslationY(y - card.getTop() + 18 * dp);
            card.animate().alpha(1f).translationY(y - card.getTop())
                    .setDuration(260).setInterpolator(new DecelerateInterpolator()).start();
        } else {
            card.animate().cancel();
            card.setAlpha(1f);
            card.animate().translationY(y - card.getTop()).setDuration(180).start();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent evento) {
        // Everything behind the tour is off limits until it ends.
        return true;
    }

    /** Draws the dim layer with the hole and the pulsing ring. */
    private final class Spot extends View {
        private final Paint pincelFundo = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint pincelFuro = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint pincelAnel = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF atual = new RectF();
        private RectF destino;
        private boolean temAlvo;
        private float intensidadeAlvo;   // 0 = no hole, 1 = hole fully shown
        private float pulso;             // 0..1 loop of the ring
        private ValueAnimator animTransicao;
        private final ValueAnimator animPulso;

        Spot(Context contexto) {
            super(contexto);
            setLayerType(LAYER_TYPE_HARDWARE, null);
            pincelFundo.setColor(COR_FUNDO);
            pincelFuro.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            pincelAnel.setStyle(Paint.Style.STROKE);
            pincelAnel.setColor(COR_ANEL);
            pincelAnel.setStrokeWidth(2.5f * dp);

            animPulso = ValueAnimator.ofFloat(0f, 1f);
            animPulso.setDuration(1400);
            animPulso.setRepeatCount(ValueAnimator.INFINITE);
            animPulso.addUpdateListener(a -> {
                pulso = (float) a.getAnimatedValue();
                if (temAlvo) invalidate();
            });
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            if (!animPulso.isStarted()) animPulso.start();
        }

        @Override
        protected void onDetachedFromWindow() {
            parar();
            super.onDetachedFromWindow();
        }

        void parar() {
            animPulso.cancel();
            if (animTransicao != null) animTransicao.cancel();
        }

        void irPara(@Nullable RectF novo) {
            if (animTransicao != null) animTransicao.cancel();
            if (novo == null) {
                temAlvo = false;
                destino = null;
                invalidate();
                return;
            }
            final RectF origem = new RectF(atual);
            final boolean veioDeAlvo = temAlvo;
            destino = new RectF(novo);
            temAlvo = true;
            if (!veioDeAlvo) {
                atual.set(novo);
                animTransicao = ValueAnimator.ofFloat(0f, 1f);
                animTransicao.setDuration(260);
                animTransicao.addUpdateListener(a -> {
                    intensidadeAlvo = (float) a.getAnimatedValue();
                    invalidate();
                });
                animTransicao.start();
                return;
            }
            intensidadeAlvo = 1f;
            animTransicao = ValueAnimator.ofFloat(0f, 1f);
            animTransicao.setDuration(260);
            animTransicao.setInterpolator(new DecelerateInterpolator());
            animTransicao.addUpdateListener(a -> {
                float t = (float) a.getAnimatedValue();
                atual.set(
                        origem.left + (destino.left - origem.left) * t,
                        origem.top + (destino.top - origem.top) * t,
                        origem.right + (destino.right - origem.right) * t,
                        origem.bottom + (destino.bottom - origem.bottom) * t);
                invalidate();
            });
            animTransicao.start();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawRect(0, 0, getWidth(), getHeight(), pincelFundo);
            if (!temAlvo) return;
            float raio = 14 * dp;
            float folga = (1f - intensidadeAlvo) * 24 * dp;
            RectF r = new RectF(atual.left - folga, atual.top - folga, atual.right + folga, atual.bottom + folga);
            canvas.drawRoundRect(r, raio, raio, pincelFuro);

            pincelAnel.setAlpha((int) (255 * intensidadeAlvo));
            canvas.drawRoundRect(r, raio, raio, pincelAnel);

            // Expanding halo that fades out: draws the eye to the hole.
            float expansao = pulso * 10 * dp;
            RectF halo = new RectF(r.left - expansao, r.top - expansao, r.right + expansao, r.bottom + expansao);
            pincelAnel.setAlpha((int) (140 * (1f - pulso) * intensidadeAlvo));
            canvas.drawRoundRect(halo, raio + expansao, raio + expansao, pincelAnel);
        }
    }
}
