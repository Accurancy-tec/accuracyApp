package com.example.accurancymobileapp.utils;

import android.app.Activity;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.ui.TutorialOverlay;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * First-use tour of the main screen: dims the app, points at one element at a time and explains it
 * in a card. Each step may switch the bottom tab first and wait for the element to exist.
 */
public final class TutorialTour {

    /** One card of the tour. {@code navId} 0 keeps the current tab; {@code alvoId} 0 centers the card. */
    private static final class Passo {
        final String titulo;
        final String texto;
        final @IdRes int navId;
        final @IdRes int alvoId;

        Passo(String titulo, String texto, @IdRes int navId, @IdRes int alvoId) {
            this.titulo = titulo;
            this.texto = texto;
            this.navId = navId;
            this.alvoId = alvoId;
        }
    }

    private static final Passo[] PASSOS = new Passo[]{
            new Passo("Bem-vindo ao Accurancy!",
                    "Vamos fazer um tour rápido, em menos de um minuto, para você saber onde fica cada coisa. "
                            + "Você pode pular quando quiser.",
                    R.id.nav_home, 0),
            new Passo("Seu patrimônio",
                    "Aqui você vê o saldo total, quanto já está investido e quanto ainda está livre para investir.",
                    R.id.nav_home, R.id.cardSaldoTotal),
            new Passo("Adicione saldo primeiro",
                    "Para comprar qualquer ativo você precisa ter saldo. Toque em \"Adicionar saldo\" para colocar "
                            + "dinheiro na sua conta.",
                    R.id.nav_home, R.id.cardSaldoDisponivel),
            new Passo("Evolução da carteira",
                    "O gráfico mostra como o seu patrimônio mudou. Troque entre 1M, 6M e 1A para ver períodos "
                            + "diferentes.",
                    R.id.nav_home, R.id.blocoGraficoEvolucao),
            new Passo("Crie sua carteira",
                    "Na aba Carteira você organiza seus investimentos. Toque em \"+ Criar carteira\" para criar a "
                            + "primeira, por exemplo \"Aposentadoria\" ou \"Viagem\".",
                    R.id.nav_wallet, R.id.btnCriarWallet),
            new Passo("Não sabe o que comprar?",
                    "Na aba Aportes, digite o nome de qualquer produto (ação, fundo, cripto, título...). "
                            + "Os resultados aparecem enquanto você digita e, ao tocar, o app abre o ativo.",
                    R.id.nav_aporte, R.id.containerBuscaGeral),
            new Passo("Ou escolha por categoria",
                    "Renda fixa, Tesouro Direto, Bolsa de Valores, Criptomoedas, Contratos Futuros e Moedas. "
                            + "Toque em uma categoria para ver a lista.",
                    R.id.nav_aporte, R.id.containerCardsProdutos),
            new Passo("Seus favoritos",
                    "Toque no coração de um ativo para guardá-lo. Esse botão abre todos os que você favoritou.",
                    R.id.nav_aporte, R.id.btnFavoritosProdutos),
            new Passo("Histórico",
                    "Na aba Histórico ficam o gráfico de rendimento e todas as suas compras e vendas, "
                            + "com filtros por período e por tipo.",
                    R.id.nav_historic, R.id.cardGraficoHistorico),
            new Passo("Seu perfil",
                    "Na aba Perfil você muda nome, foto, telefone, e-mail e senha. "
                            + "Para rever este tour, use o item \"Rever tutorial\" mais abaixo nesta tela.",
                    R.id.nav_profile, R.id.btnEditarPerfil),
            new Passo("Tudo pronto!",
                    "Agora é com você: adicione saldo, crie uma carteira e faça o seu primeiro aporte. "
                            + "Bons investimentos!",
                    R.id.nav_home, 0),
    };

    private static final long INTERVALO_ESPERA_MS = 120L;
    private static final int TENTATIVAS_MAX = 25;

    private final FragmentActivity activity;
    private final BottomNavigationView navegacao;
    private final ViewGroup conteudo;
    private final TutorialOverlay overlay;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final boolean registrarVisto;

    private int indice;
    private int geracao;                       // invalidates waits of previous steps
    private @Nullable View alvoAtual;
    private @Nullable View.OnLayoutChangeListener ouvinteLayout;
    private @Nullable OnBackPressedCallback voltarCallback;
    private boolean encerrado;

    private TutorialTour(FragmentActivity activity, BottomNavigationView navegacao,
                         ViewGroup conteudo, boolean registrarVisto) {
        this.activity = activity;
        this.navegacao = navegacao;
        this.conteudo = conteudo;
        this.registrarVisto = registrarVisto;
        this.overlay = new TutorialOverlay(activity);
    }

    /**
     * Starts the tour on the main screen.
     *
     * @param primeiraVez true when it came from the first access (it is then marked as seen at the
     *                    end); false when the user asked to see it again.
     */
    public static void iniciar(Activity atividade, boolean primeiraVez) {
        if (!(atividade instanceof FragmentActivity)) return;
        if (atividade.isFinishing() || atividade.isDestroyed()) return;
        BottomNavigationView nav = atividade.findViewById(R.id.bottomNavigation);
        ViewGroup conteudo = atividade.findViewById(android.R.id.content);
        if (nav == null || conteudo == null) return;
        if (conteudo.findViewWithTag(TutorialOverlay.TAG) != null) return;   // already running
        new TutorialTour((FragmentActivity) atividade, nav, conteudo, primeiraVez).comecar();
    }

    private void comecar() {
        overlay.definirAcoes(new TutorialOverlay.Acoes() {
            @Override public void aoProximo() { avancar(); }
            @Override public void aoVoltar() { recuar(); }
            @Override public void aoPular() { encerrar(); }
        });
        conteudo.addView(overlay, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlay.aparecer();
        ouvirVoltar();
        mostrar(0);
    }

    /** Back button skips the tour instead of leaving the app underneath it. */
    private void ouvirVoltar() {
        voltarCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                encerrar();
            }
        };
        activity.getOnBackPressedDispatcher().addCallback(activity, voltarCallback);
    }

    private void avancar() {
        if (indice >= PASSOS.length - 1) {
            encerrar();
        } else {
            mostrar(indice + 1);
        }
    }

    private void recuar() {
        if (indice > 0) mostrar(indice - 1);
    }

    private void mostrar(int novo) {
        if (encerrado) return;
        indice = novo;
        geracao++;
        soltarAlvo();
        final int minhaGeracao = geracao;
        final Passo passo = PASSOS[novo];

        if (passo.navId != 0 && navegacao.getSelectedItemId() != passo.navId) {
            navegacao.setSelectedItemId(passo.navId);
        }

        if (passo.alvoId == 0) {
            // Centered card; give the tab a moment to appear behind it.
            handler.postDelayed(() -> {
                if (minhaGeracao != geracao || encerrado) return;
                overlay.mostrarPasso(novo, PASSOS.length, passo.titulo, passo.texto, null,
                        novo == 0, novo == PASSOS.length - 1);
            }, 320L);
            return;
        }
        aguardarAlvo(passo, minhaGeracao, 0);
    }

    private void aguardarAlvo(Passo passo, int minhaGeracao, int tentativa) {
        if (minhaGeracao != geracao || encerrado) return;
        View alvo = procurarAlvo(passo.alvoId);
        if (alvo != null && alvo.isShown() && alvo.getWidth() > 0 && alvo.getHeight() > 0) {
            garantirVisivel(passo, alvo, minhaGeracao);
            return;
        }
        if (tentativa >= TENTATIVAS_MAX) {
            // The element never showed up (slow screen, changed layout): explain without pointing.
            overlay.mostrarPasso(indice, PASSOS.length, passo.titulo, passo.texto, null,
                    indice == 0, indice == PASSOS.length - 1);
            return;
        }
        handler.postDelayed(() -> aguardarAlvo(passo, minhaGeracao, tentativa + 1), INTERVALO_ESPERA_MS);
    }

    private @Nullable View procurarAlvo(@IdRes int id) {
        Fragment atual = activity.getSupportFragmentManager().findFragmentById(R.id.frameContent);
        View raiz = atual != null ? atual.getView() : null;
        return raiz != null ? raiz.findViewById(id) : null;
    }

    /** Scrolls the target into view when it is cut off, then shows the step. */
    private void garantirVisivel(Passo passo, View alvo, int minhaGeracao) {
        Rect visivel = new Rect();
        boolean enxerga = alvo.getGlobalVisibleRect(visivel);
        boolean cortado = !enxerga || visivel.height() < alvo.getHeight() - 2;
        if (cortado) {
            alvo.requestRectangleOnScreen(new Rect(0, 0, alvo.getWidth(), alvo.getHeight()), false);
            handler.postDelayed(() -> {
                if (minhaGeracao != geracao || encerrado) return;
                exibirNoAlvo(passo, alvo);
            }, 420L);
        } else {
            exibirNoAlvo(passo, alvo);
        }
    }

    private void exibirNoAlvo(Passo passo, View alvo) {
        alvoAtual = alvo;
        overlay.mostrarPasso(indice, PASSOS.length, passo.titulo, passo.texto, retanguloDe(alvo),
                indice == 0, indice == PASSOS.length - 1);
        // Values arriving from the server can resize the element: keep the hole on it.
        ouvinteLayout = (v, l, t, r, b, ol, ot, or, ob) -> {
            if (encerrado || alvoAtual != v) return;
            overlay.atualizarAlvo(retanguloDe(v));
        };
        alvo.addOnLayoutChangeListener(ouvinteLayout);
    }

    private void soltarAlvo() {
        if (alvoAtual != null && ouvinteLayout != null) {
            alvoAtual.removeOnLayoutChangeListener(ouvinteLayout);
        }
        alvoAtual = null;
        ouvinteLayout = null;
    }

    private RectF retanguloDe(View alvo) {
        int[] noAlvo = new int[2];
        alvo.getLocationInWindow(noAlvo);
        int[] noOverlay = overlay.posicaoNaJanela();
        float dp = activity.getResources().getDisplayMetrics().density;
        float folga = 6 * dp;
        float esquerda = noAlvo[0] - noOverlay[0] - folga;
        float topo = noAlvo[1] - noOverlay[1] - folga;
        return new RectF(esquerda, topo,
                esquerda + alvo.getWidth() + 2 * folga,
                topo + alvo.getHeight() + 2 * folga);
    }

    private void encerrar() {
        if (encerrado) return;
        encerrado = true;
        geracao++;
        soltarAlvo();
        handler.removeCallbacksAndMessages(null);
        if (voltarCallback != null) voltarCallback.remove();
        if (registrarVisto) TutorialPrefs.concluir(activity);
        // Leaves the user on the first tab, where the next steps (add balance, create a wallet) start.
        overlay.fechar(() -> {
            if (!activity.isFinishing() && !activity.isDestroyed()
                    && navegacao.getSelectedItemId() != R.id.nav_home) {
                navegacao.setSelectedItemId(R.id.nav_home);
            }
        });
    }
}
