package com.example.accurancymobileapp.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.compose.ui.platform.ComposeView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.activities.FormAdicionarSaldoActivity;
import com.example.accurancymobileapp.activities.FormCriarWalletActivity;
import com.example.accurancymobileapp.activities.NotificacoesActivity;
import com.example.accurancymobileapp.adapter.EmphasisAdapter;
import com.example.accurancymobileapp.adapter.EsqueletoAdapter;
import com.example.accurancymobileapp.adapter.NoticiaAdapter;
import com.example.accurancymobileapp.model.Noticia;
import com.example.accurancymobileapp.model.User;
import com.example.accurancymobileapp.model.clsAportes;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.network.service.ApiService;
import com.example.accurancymobileapp.network.service.VicoService;
import com.example.accurancymobileapp.response.ApiResponse;
import com.example.accurancymobileapp.response.NoticiasResponse;
import com.example.accurancymobileapp.response.PerfilResponse;
import com.example.accurancymobileapp.response.ResumoPatrimonioResponse;
import com.example.accurancymobileapp.response.VicoResponse;
import com.example.accurancymobileapp.ui.AnimationSpinner;
import com.example.accurancymobileapp.ui.ChartHelper;
import com.example.accurancymobileapp.utils.CallbackDaTela;
import com.example.accurancymobileapp.utils.ContadorNotificacoes;
import com.example.accurancymobileapp.utils.Esqueleto;
import com.example.accurancymobileapp.utils.PerfilCache;
import com.example.accurancymobileapp.utils.PerfilCallback;
import com.example.accurancymobileapp.utils.PuxarParaAtualizar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Início: patrimônio das carteiras reais (com o saldo geral), simulações à parte, atalho para
 * criar carteira, notícias do mercado, evolução do patrimônio e os destaques da carteira.
 */
public class DashboardFragment extends Fragment {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final int LIMITE_NOTICIAS = 10;
    private static final String[] FILTROS = {"Mais caros", "Mais lucrativos", "Maiores perdas"};

    private ApiService api;
    private PuxarParaAtualizar puxar;

    private TextView tvNomeUsuario, tvSaldoTotal, tvInvestido, tvSaldoLivre, tvRendimento, tvBadgeTotal,
            tvSaldoDisponivel, tvTituloSimulacoes, tvValorSimulacoes, tvResultadoSimulacoes,
            tvTextoCriarCarteira, tvNoticiasVazio, tvDestaquesVazio, badgeNotificacoes;
    private TextView btn1M, btn6M, btn1A;
    private View cardSimulacoes, esqueletoGrafico, sino;
    private ComposeView ctvChart;
    private RecyclerView rvNoticias, recyclerInvestimentos;
    private NoticiaAdapter noticiaAdapter;

    private String periodo = "6M";
    private String filtroDestaque = FILTROS[0];
    private boolean resumoCarregado = false;
    private boolean primeiraRetomada = true;

    public DashboardFragment() {
        super(R.layout.fragment_dashboard);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        api = RetrofitClient.getApiService(requireContext());

        tvNomeUsuario = view.findViewById(R.id.tvNomeUsuario);
        tvSaldoTotal = view.findViewById(R.id.tvSaldoTotal);
        tvInvestido = view.findViewById(R.id.tvInvestido);
        tvSaldoLivre = view.findViewById(R.id.tvSaldoLivre);
        tvRendimento = view.findViewById(R.id.tvRendimento);
        tvBadgeTotal = view.findViewById(R.id.tvBadgeTotal);
        tvSaldoDisponivel = view.findViewById(R.id.tvSaldoDisponivel);
        cardSimulacoes = view.findViewById(R.id.cardSimulacoes);
        tvTituloSimulacoes = view.findViewById(R.id.tvTituloSimulacoes);
        tvValorSimulacoes = view.findViewById(R.id.tvValorSimulacoes);
        tvResultadoSimulacoes = view.findViewById(R.id.tvResultadoSimulacoes);
        tvTextoCriarCarteira = view.findViewById(R.id.tvTextoCriarCarteira);
        tvNoticiasVazio = view.findViewById(R.id.tvNoticiasVazio);
        tvDestaquesVazio = view.findViewById(R.id.tvDestaquesVazio);
        badgeNotificacoes = view.findViewById(R.id.tvBadgeNotificacoes);
        sino = view.findViewById(R.id.btnNotificacoes);
        btn1M = view.findViewById(R.id.btn1M);
        btn6M = view.findViewById(R.id.btn6M);
        btn1A = view.findViewById(R.id.btn1A);
        esqueletoGrafico = view.findViewById(R.id.esqueletoGrafico);
        ctvChart = view.findViewById(R.id.ctvChart);
        rvNoticias = view.findViewById(R.id.rvNoticias);
        recyclerInvestimentos = view.findViewById(R.id.recyclerInvestimentos);

        puxar = new PuxarParaAtualizar(view.findViewById(R.id.swipeDashboard), () -> {
            atualizarTudo();
            carregarNoticias();
        });

        sino.setOnClickListener(v -> startActivity(new Intent(requireContext(), NotificacoesActivity.class)));
        view.findViewById(R.id.btnAdicionarSaldo).setOnClickListener(
                v -> startActivity(new Intent(requireContext(), FormAdicionarSaldoActivity.class)));
        view.findViewById(R.id.cardCriarCarteira).setOnClickListener(
                v -> startActivity(new Intent(requireContext(), FormCriarWalletActivity.class)));
        cardSimulacoes.setOnClickListener(v -> abrirAbaCarteiras());

        rvNoticias.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        noticiaAdapter = new NoticiaAdapter(this::abrirNoticia);

        recyclerInvestimentos.setLayoutManager(new LinearLayoutManager(requireContext()));
        AnimationSpinner.configurar(requireContext(), view.<Spinner>findViewById(R.id.spFiltroDestaques), FILTROS,
                posicao -> {
                    filtroDestaque = FILTROS[posicao];
                    carregarDestaques(filtroDestaque);
                });

        configurarPeriodos();

        // Barras piscando onde os números vão aparecer; cada uma some quando o dado chega.
        Esqueleto.iniciar(tvSaldoTotal, tvInvestido, tvSaldoLivre, tvRendimento, tvSaldoDisponivel);
        carregarNome();
        carregarResumo();
        carregarNoticias();
        carregarGrafico(periodo);
    }

    @Override
    public void onResume() {
        super.onResume();
        ContadorNotificacoes.atualizar(this, sino, badgeNotificacoes);
        // Depois de adicionar saldo, comprar ou vender, os números mudam: recarrega ao voltar.
        if (primeiraRetomada) {
            primeiraRetomada = false;
            return;
        }
        atualizarTudo();
    }

    /** Recarrega números, gráfico e destaques (puxar para atualizar e volta de outra tela). */
    private void atualizarTudo() {
        carregarResumo();
        carregarGrafico(periodo);
        carregarDestaques(filtroDestaque);
    }

    private static String reais(double valor) {
        return NumberFormat.getCurrencyInstance(PT_BR).format(valor);
    }

    /** "+R$ 120,00" ou "−R$ 30,00". */
    private static String reaisComSinal(double valor) {
        String texto = reais(Math.abs(valor));
        if (valor > 0.004) return "+" + texto;
        if (valor < -0.004) return "−" + texto;
        return texto;
    }

    private static String porcentagem(double valor) {
        return String.format(PT_BR, "%.2f%%", Math.abs(valor));
    }

    private int cor(int id) {
        return ContextCompat.getColor(requireContext(), id);
    }

    // ---------------------------------------------------------------- resumo

    private void carregarResumo() {
        puxar.iniciar();
        api.resumoPatrimonio(null).enqueue(new CallbackDaTela<ResumoPatrimonioResponse>(this) {
            @Override
            public void aoSucesso(@NonNull ResumoPatrimonioResponse resposta) {
                puxar.concluir();
                mostrarResumo(resposta);
            }

            @Override
            public void aoErro(@NonNull String mensagem) {
                puxar.concluir();
                if (!resumoCarregado) {
                    for (TextView campo : new TextView[]{tvSaldoTotal, tvInvestido, tvSaldoLivre, tvRendimento, tvSaldoDisponivel}) {
                        campo.setText("—");
                    }
                            }
                Toast.makeText(requireContext(), mensagem, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarResumo(ResumoPatrimonioResponse resposta) {
        Esqueleto.parar(tvSaldoTotal, tvInvestido, tvSaldoLivre, tvRendimento, tvSaldoDisponivel);
        ResumoPatrimonioResponse.Real real = resposta.getReal();
        ResumoPatrimonioResponse.Simulado simulado = resposta.getSimulado();

        tvSaldoTotal.setText(reais(real.patrimonioTotal));
        tvInvestido.setText(reais(real.totalInvestido));
        tvSaldoLivre.setText(reais(resposta.getSaldoGeral()));
        tvSaldoDisponivel.setText(reais(resposta.getSaldoGeral()));

        if (real.totalInvestido > 0) {
            boolean ganho = real.lucroPrejuizo >= 0;
            tvRendimento.setText(reaisComSinal(real.lucroPrejuizo));
            tvRendimento.setTextColor(cor(ganho ? R.color.positivo_sobre_destaque : R.color.negativo_sobre_destaque));
            tvBadgeTotal.setText((ganho ? "▲ " : "▼ ") + porcentagem(real.rentabilidadePercentual) + " nos seus investimentos");
            tvBadgeTotal.setVisibility(View.VISIBLE);
        } else {
            tvRendimento.setText(reais(0));
            tvRendimento.setTextColor(cor(R.color.white));
            tvBadgeTotal.setVisibility(View.GONE);
        }

        if (simulado.quantidadeCarteiras > 0) {
            boolean ganho = simulado.resultado >= 0;
            tvTituloSimulacoes.setText(simulado.quantidadeCarteiras == 1
                    ? "1 carteira simulada" : simulado.quantidadeCarteiras + " carteiras simuladas");
            tvValorSimulacoes.setText(reais(simulado.patrimonioTotal) + " em simulação · fora do patrimônio");
            tvResultadoSimulacoes.setText((ganho ? "+" : "−") + porcentagem(simulado.rentabilidadePercentual));
            tvResultadoSimulacoes.setTextColor(cor(ganho ? R.color.positivo_destaque : R.color.negativo_destaque));
            cardSimulacoes.setVisibility(View.VISIBLE);
        } else {
            cardSimulacoes.setVisibility(View.GONE);
        }

        boolean semCarteiras = real.quantidadeCarteiras + simulado.quantidadeCarteiras == 0;
        tvTextoCriarCarteira.setText(semCarteiras
                ? "Comece pela sua primeira carteira: real ou simulada"
                : "Real, com o seu saldo, ou simulada, com o valor que você escolher");

        Esqueleto.parar(tvSaldoTotal, tvInvestido, tvSaldoLivre, tvRendimento, tvSaldoDisponivel);
        resumoCarregado = true;
    }

    private void abrirAbaCarteiras() {
        BottomNavigationView navegacao = requireActivity().findViewById(R.id.bottomNavigation);
        if (navegacao != null) navegacao.setSelectedItemId(R.id.nav_wallet);
    }

    // -------------------------------------------------------------- notícias

    private void carregarNoticias() {
        tvNoticiasVazio.setVisibility(View.GONE);
        if (noticiaAdapter.getItemCount() == 0) {
            rvNoticias.setVisibility(View.VISIBLE);
            EsqueletoAdapter.mostrar(rvNoticias, R.layout.item_esqueleto_noticia, 3);
        }
        puxar.iniciar();
        api.listarNoticias(LIMITE_NOTICIAS).enqueue(new CallbackDaTela<NoticiasResponse>(this) {
            @Override
            public void aoSucesso(@NonNull NoticiasResponse resposta) {
                puxar.concluir();
                List<Noticia> noticias = resposta.getNoticias();
                noticiaAdapter.definir(noticias);
                EsqueletoAdapter.ocultar(rvNoticias, noticiaAdapter);
                if (noticias.isEmpty()) {
                    mostrarNoticiasVazio("Nenhuma notícia por agora. Puxe a tela para atualizar mais tarde.");
                }
            }

            @Override
            public void aoErro(@NonNull String mensagem) {
                puxar.concluir();
                if (noticiaAdapter.getItemCount() == 0) {
                    EsqueletoAdapter.ocultar(rvNoticias, noticiaAdapter);
                    mostrarNoticiasVazio("Não foi possível carregar as notícias agora.");
                }
            }
        });
    }

    private void mostrarNoticiasVazio(String texto) {
        rvNoticias.setVisibility(View.GONE);
        tvNoticiasVazio.setText(texto);
        tvNoticiasVazio.setVisibility(View.VISIBLE);
    }

    /** Notícia de portal abre no navegador; notícia da equipe (sem link) abre num cartão. */
    private void abrirNoticia(@NonNull Noticia noticia) {
        String url = noticia.getUrl();
        if (url != null && url.startsWith("https://")) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(requireContext(), "Nenhum navegador disponível para abrir a notícia.", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        String texto = noticia.getConteudo().isEmpty() ? noticia.getResumo() : noticia.getConteudo();
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(noticia.getTitulo())
                .setMessage(texto)
                .setPositiveButton("Fechar", null)
                .show();
    }

    // --------------------------------------------------------------- gráfico

    private void configurarPeriodos() {
        btn1M.setOnClickListener(v -> trocarPeriodo("1M", btn1M));
        btn6M.setOnClickListener(v -> trocarPeriodo("6M", btn6M));
        btn1A.setOnClickListener(v -> trocarPeriodo("1A", btn1A));
        marcarPeriodo(btn6M);
    }

    private void trocarPeriodo(String novo, TextView botao) {
        if (novo.equals(periodo)) return;
        periodo = novo;
        marcarPeriodo(botao);
        carregarGrafico(periodo);
    }

    private void marcarPeriodo(TextView selecionado) {
        for (TextView botao : new TextView[]{btn1M, btn6M, btn1A}) {
            boolean marcado = botao == selecionado;
            botao.setBackgroundResource(marcado ? R.drawable.bg_periodo_selecionado : R.drawable.bg_periodo_normal);
            botao.setTextColor(cor(marcado ? R.color.white : R.color.text_secondary));
            botao.setSelected(marcado);
        }
    }

    private void carregarGrafico(String periodoPedido) {
        // Esqueleto no lugar do gráfico até o novo período chegar (não ao puxar para atualizar).
        if (!puxar.atualizando()) {
            ctvChart.setVisibility(View.INVISIBLE);
            esqueletoGrafico.setVisibility(View.VISIBLE);
        }

        VicoService vico = RetrofitClient.getClient(requireContext()).create(VicoService.class);
        vico.getEvolucao(periodoPedido).enqueue(puxar.acompanhar(new Callback<VicoResponse>() {
            @Override
            public void onResponse(@NonNull Call<VicoResponse> call, @NonNull Response<VicoResponse> response) {
                if (!isAdded() || getView() == null || !periodoPedido.equals(periodo)) return;

                VicoResponse corpo = response.body();
                List<String> datas = new ArrayList<>();
                List<Double> valores = new ArrayList<>();
                if (response.isSuccessful() && corpo != null && corpo.isSucesso() && corpo.getPontos() != null) {
                    for (VicoResponse.Ponto ponto : corpo.getPontos()) {
                        if (ponto == null || ponto.getData() == null || ponto.getData().trim().isEmpty()) continue;
                        if (!Double.isFinite(ponto.getValor())) continue;
                        datas.add(ponto.getData());
                        valores.add(ponto.getValor());
                    }
                }
                mostrarGrafico(datas, valores, periodoPedido);
            }

            @Override
            public void onFailure(@NonNull Call<VicoResponse> call, @NonNull Throwable t) {
                if (!isAdded() || getView() == null || !periodoPedido.equals(periodo)) return;
                mostrarGrafico(new ArrayList<>(), new ArrayList<>(), periodoPedido);
            }
        }));
    }

    private void mostrarGrafico(List<String> datas, List<Double> valores, String periodoMostrado) {
        boolean eraEsqueleto = esqueletoGrafico.getVisibility() == View.VISIBLE;
        esqueletoGrafico.setVisibility(View.GONE);
        ctvChart.setVisibility(View.VISIBLE);
        if (eraEsqueleto) Esqueleto.revelar(ctvChart);
        ChartHelper.configurarGraficoEvolucao(ctvChart, datas, valores, periodoMostrado);
    }

    // ------------------------------------------------------------- destaques

    private void carregarDestaques(String tipo) {
        tvDestaquesVazio.setVisibility(View.GONE);
        // Linhas de esqueleto, a menos que o indicador de "puxar para atualizar" já esteja na tela.
        if (!puxar.atualizando()) {
            EsqueletoAdapter.mostrar(recyclerInvestimentos, R.layout.item_esqueleto_linha, 3);
        }

        api.buscarLista(tipo).enqueue(puxar.acompanhar(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                if (!isAdded() || getView() == null) return;
                ApiResponse corpo = response.body();
                List<clsAportes> aportes = response.isSuccessful() && corpo != null && corpo.getLista() != null
                        ? corpo.getLista() : new ArrayList<>();
                EsqueletoAdapter.ocultar(recyclerInvestimentos, new EmphasisAdapter(aportes));

                if (aportes.isEmpty()) {
                    tvDestaquesVazio.setText(
                            tipo.equals("Maiores perdas") ? "Nenhum ativo seu está em queda hoje."
                                    : tipo.equals("Mais lucrativos") ? "Nenhum ativo seu está em alta hoje."
                                    : "Faça seu primeiro aporte para ver seus ativos aqui.");
                    tvDestaquesVazio.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                if (!isAdded() || getView() == null) return;
                EsqueletoAdapter.ocultar(recyclerInvestimentos, new EmphasisAdapter(new ArrayList<>()));
                tvDestaquesVazio.setText("Não foi possível carregar seus ativos agora.");
                tvDestaquesVazio.setVisibility(View.VISIBLE);
            }
        }));
    }

    // ------------------------------------------------------------------ nome

    /** Saudação com o primeiro nome; o último perfil lido aparece na hora enquanto atualiza. */
    private void carregarNome() {
        User conhecido = PerfilCache.obter();
        if (conhecido != null && conhecido.getNome_usuario() != null) {
            mostrarNome(conhecido.getNome_usuario());
        } else {
            Esqueleto.iniciar(tvNomeUsuario);
        }
        api.perfilObter().enqueue(new PerfilCallback(this) {
            @Override
            public void aoSucesso(PerfilResponse resposta) {
                User usuario = resposta.getUser();
                if (usuario == null) {
                    aoErro("");
                    return;
                }
                PerfilCache.guardar(usuario);
                mostrarNome(usuario.getNome_usuario());
            }

            @Override
            public void aoErro(String mensagem) {
                if (Esqueleto.ativo(tvNomeUsuario)) {
                    tvNomeUsuario.setText("Olá");
                    Esqueleto.parar(tvNomeUsuario);
                }
            }
        });
    }

    private void mostrarNome(String nomeCompleto) {
        String nome = nomeCompleto == null ? "" : nomeCompleto.trim();
        String primeiro = nome.isEmpty() ? "" : nome.split("\\s+")[0];
        tvNomeUsuario.setText(primeiro.isEmpty() ? "Olá" : "Olá, " + primeiro);
        Esqueleto.parar(tvNomeUsuario);
    }
}
