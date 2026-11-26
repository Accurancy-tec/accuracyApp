package com.example.accurancymobileapp.utils;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.accurancymobileapp.R;

/**
 * Aviso curto do app, no lugar do Toast: entra deslizando de baixo, some sozinho e pode ser
 * fechado com um toque. Usa a mesma assinatura do Toast ({@code makeText(...).show()}).
 * Fora de uma Activity visível (ex.: contexto da aplicação) cai no Toast do sistema.
 */
public final class Aviso {

    private static final Object MARCA = new Object();
    private static final long DURACAO_CURTA_MS = 2200L;
    private static final long DURACAO_LONGA_MS = 3800L;
    private static final long ENTRADA_MS = 220L;
    private static final long SAIDA_MS = 180L;

    /** Até quando (uptime) só a tela liberada pode mostrar avisos; ver {@link #silenciarExceto}. */
    private static volatile long silencioAte = 0L;
    private static volatile Class<?> telaLiberada = null;

    private final Context contexto;
    private final CharSequence texto;
    private final int duracao;

    private Aviso(Context contexto, CharSequence texto, int duracao) {
        this.contexto = contexto;
        this.texto = texto == null ? "" : texto;
        this.duracao = duracao;
    }

    /** Mesmo uso do {@link Toast#makeText}: {@code Aviso.makeText(this, "Salvo.", Toast.LENGTH_SHORT).show()}. */
    public static Aviso makeText(@NonNull Context contexto, @Nullable CharSequence texto, int duracao) {
        return new Aviso(contexto, texto, duracao);
    }

    /**
     * Mostra no próximo ciclo da tela: se a Activity foi fechada logo depois da chamada
     * (ex.: "Carteira criada." seguido de finish()), o aviso vira um Toast e não se perde.
     */
    public void show() {
        new Handler(Looper.getMainLooper()).post(this::exibir);
    }

    /**
     * Quando a API encerra a sessão deste aparelho, os pedidos que estavam em andamento voltam
     * com erro em telas que já estão fechando. Por alguns segundos, só {@code tela} (o login,
     * que explica o que aconteceu) mostra avisos.
     */
    public static void silenciarExceto(@NonNull Class<? extends Activity> tela, long duracaoMs) {
        telaLiberada = tela;
        silencioAte = SystemClock.uptimeMillis() + duracaoMs;
    }

    private void exibir() {
        Activity atividade = atividadeDe(contexto);
        if (SystemClock.uptimeMillis() < silencioAte
                && (atividade == null || telaLiberada == null || !telaLiberada.isInstance(atividade))) {
            return;
        }
        if (atividade == null || atividade.isFinishing() || atividade.isDestroyed()) {
            Toast.makeText(contexto.getApplicationContext(), texto, duracao).show();
            return;
        }

        ViewGroup raiz = atividade.findViewById(android.R.id.content);
        View anterior = raiz.findViewWithTag(MARCA);
        if (anterior != null) raiz.removeView(anterior);

        TextView aviso = (TextView) LayoutInflater.from(atividade).inflate(R.layout.view_aviso, raiz, false);
        aviso.setText(texto);
        aviso.setTag(MARCA);
        aviso.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);

        float dp = atividade.getResources().getDisplayMetrics().density;
        FrameLayout.LayoutParams posicao = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        posicao.bottomMargin = distanciaDoFundo(atividade, raiz) + Math.round(16 * dp);
        posicao.leftMargin = Math.round(24 * dp);
        posicao.rightMargin = Math.round(24 * dp);
        raiz.addView(aviso, posicao);

        aviso.setAlpha(0f);
        aviso.setTranslationY(24 * dp);
        aviso.animate().alpha(1f).translationY(0f).setDuration(ENTRADA_MS)
                .setInterpolator(new DecelerateInterpolator()).start();

        Runnable esconder = () -> esconder(raiz, aviso, dp);
        aviso.setOnClickListener(v -> esconder.run());
        aviso.postDelayed(esconder, duracao == Toast.LENGTH_LONG ? DURACAO_LONGA_MS : DURACAO_CURTA_MS);
    }

    private static void esconder(ViewGroup raiz, View aviso, float dp) {
        if (aviso.getParent() == null || !aviso.isEnabled()) return;
        aviso.setEnabled(false);
        aviso.animate().alpha(0f).translationY(12 * dp).setDuration(SAIDA_MS)
                .setInterpolator(new AccelerateInterpolator())
                .withEndAction(() -> raiz.removeView(aviso))
                .start();
    }

    /** Fica acima da barra de navegação do sistema e da barra de abas do app, quando houver. */
    private static int distanciaDoFundo(Activity atividade, View raiz) {
        int distancia = 0;
        WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(raiz);
        if (insets != null) {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            distancia += barras.bottom;
        }
        View abas = atividade.findViewById(R.id.bottomNavigation);
        if (abas != null && abas.getVisibility() == View.VISIBLE) distancia += abas.getHeight();
        return distancia;
    }

    @Nullable
    private static Activity atividadeDe(Context contexto) {
        Context atual = contexto;
        while (atual instanceof ContextWrapper) {
            if (atual instanceof Activity) return (Activity) atual;
            atual = ((ContextWrapper) atual).getBaseContext();
        }
        return null;
    }
}
