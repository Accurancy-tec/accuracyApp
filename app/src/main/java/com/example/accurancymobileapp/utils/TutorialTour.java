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

    /** O caminho completo: saldo, carteiras, comprar, vender, histórico e perfil. */
    private static final Passo[] PASSOS = new Passo[]{
            new Passo("Seu patrimônio",
                    "Patrimônio total é o saldo livre somado ao valor atual das carteiras reais. "
                            + "Investido é quanto você pagou pelos ativos que ainda tem; Resultado é quanto "
                            + "eles ganharam ou perderam desde a compra.",
                    R.id.nav_home, R.id.cardSaldoTotal),
            new Passo("Saldo para investir",
                    "O dinheiro do Accuracy é fictício. Toque em \"Adicionar saldo\" para colocar dinheiro "
                            + "na conta: ele paga as compras das carteiras reais.",
                    R.id.nav_home, R.id.cardSaldoDisponivel),
            new Passo("Dois tipos de carteira",
                    "Real: usa o saldo da conta e entra no patrimônio. Simulada: tem um valor só dela, "
                            + "escolhido na criação, para testar estratégias sem mexer no seu saldo. "
                            + "Toque aqui para criar a primeira.",
                    R.id.nav_home, R.id.cardCriarCarteira),
            new Passo("Suas carteiras",
                    "Cada cartão mostra se a carteira é real ou simulada, o patrimônio e o resultado. "
                            + "Ao abrir uma, você vê as operações dela; numa simulada também dá para depositar "
                            + "mais dinheiro e tocar em \"Simular investimento\".",
                    R.id.nav_wallet, R.id.btnCriarWallet),
            new Passo("Encontre um ativo",
                    "Busque pelo código ou pelo nome (PETR4, MXRF11, Bitcoin, Tesouro...). "
                            + "Os resultados aparecem por categoria enquanto você digita.",
                    R.id.nav_aporte, R.id.containerBuscaGeral),
            new Passo("Ou escolha por categoria",
                    "Renda fixa, Tesouro Direto, Bolsa de Valores, Criptomoedas, Contratos Futuros e "
                            + "Moedas: cada categoria tem sua lista, com filtros e ordenação.",
                    R.id.nav_aporte, R.id.containerCardsProdutos),
            new Passo("Como comprar",
                    "1. Abra o ativo e toque em \"Comprar\".\n"
                            + "2. Escolha a quantidade: o app mostra o total estimado, já com as taxas.\n"
                            + "3. Confirme e escolha a carteira. Cada opção mostra quanto dinheiro tem: o saldo "
                            + "geral nas reais e o saldo da própria simulação nas simuladas.",
                    R.id.nav_aporte, 0),
            new Passo("Como vender",
                    "Abra o ativo e toque em \"Vender\". Primeiro você escolhe a carteira (só aparecem as "
                            + "que têm o ativo) e depois a quantidade. O valor da venda volta para o saldo de "
                            + "onde o dinheiro saiu.",
                    R.id.nav_aporte, 0),
            new Passo("Seu histórico",
                    "Aqui ficam os totais de compras, vendas e dividendos do período (1S, 1M ou 1A), o "
                            + "gráfico das movimentações e a lista de todas as operações, com filtro por tipo.",
                    R.id.nav_historic, R.id.resumoHistorico),
            new Passo("Ajuda e preferências",
                    "No Perfil você troca o tema entre claro e escuro, vê as perguntas frequentes, fala "
                            + "com a equipe e pode rever este tour quando quiser.",
                    R.id.nav_profile, R.id.itemAparencia),
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
