package com.example.accurancymobileapp.fragments;

import com.example.accurancymobileapp.utils.PuxarParaAtualizar;
import static android.widget.Toast.LENGTH_LONG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.compose.ui.platform.ComposeView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.activities.FormAdicionarSaldoActivity;
import com.example.accurancymobileapp.adapter.EmphasisAdapter;
import com.example.accurancymobileapp.adapter.EsqueletoAdapter;
import com.example.accurancymobileapp.model.User;
import com.example.accurancymobileapp.response.PerfilResponse;
import com.example.accurancymobileapp.utils.Esqueleto;
import com.example.accurancymobileapp.utils.PerfilCache;
import com.example.accurancymobileapp.utils.PerfilCallback;
import com.example.accurancymobileapp.model.clsAportes;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.network.repository.WalletRepository;
import com.example.accurancymobileapp.network.service.ApiService;
import com.example.accurancymobileapp.network.service.VicoService;
import com.example.accurancymobileapp.response.ApiResponse;
import com.example.accurancymobileapp.response.VicoResponse;
import com.example.accurancymobileapp.ui.AnimationSpinner;
import com.example.accurancymobileapp.ui.ChartHelper;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {

    private ComposeView ctvChart;
    private TextView btn1M, btn6M, btn1A, tvSaldoDisponivel;
    private RecyclerView recyclerInvestimentos;
    private Spinner spFiltroDestaques;
    private TextView tvDestaquesVazio, btnAdicionarSaldo;
    private WalletRepository walletRepository;
    private TextView tvSaldoLivre, tvSaldoTotal, tvInvestido, tvRendimento, tvNomeUsuario;
    private View esqueletoGrafico;
    private Double saldoLivre;
    private Double investido;
    private PuxarParaAtualizar puxar;
    private String filtroDestaque = "Mais caros";
    private boolean primeiraRetomada = true;

    public DashboardFragment() {
        super(R.layout.fragment_dashboard);
    }

    String periodo = "6M";
    String[] filtros = {
            "Mais caros",
            "Mais lucrativos",
            "Maiores perdas"
    };

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        ctvChart = view.findViewById(R.id.ctvChart);
        recyclerInvestimentos = view.findViewById(R.id.recyclerInvestimentos);
        btn1M = view.findViewById(R.id.btn1M);
        btn6M = view.findViewById(R.id.btn6M);
        btn1A = view.findViewById(R.id.btn1A);
        tvDestaquesVazio = view.findViewById(R.id.tvDestaquesVazio);
        spFiltroDestaques = view.findViewById(R.id.spFiltroDestaques);
        btnAdicionarSaldo = view.findViewById(R.id.btnAdicionarSaldo);
        tvSaldoDisponivel = view.findViewById(R.id.tvSaldoDisponivel);
        tvSaldoLivre = view.findViewById(R.id.tvSaldoLivre);
        tvSaldoTotal = view.findViewById(R.id.tvSaldoTotal);
        tvInvestido = view.findViewById(R.id.tvInvestido);
        tvRendimento = view.findViewById(R.id.tvRendimento);
        tvNomeUsuario = view.findViewById(R.id.tvNomeUsuario);
        esqueletoGrafico = view.findViewById(R.id.esqueletoGrafico);
        walletRepository = new WalletRepository(requireContext());
        puxar = new PuxarParaAtualizar(view.findViewById(R.id.swipeDashboard), this::atualizarTudo);

        // Shimmering bars where the numbers will appear; each one is released when its data arrives.
        Esqueleto.iniciar(tvSaldoTotal, tvInvestido, tvSaldoLivre, tvSaldoDisponivel);
        carregarNome();
        carregarGrafico(periodo);
        recicleView();
        timeInvested();
        carregarSaldo();
        carregarInvestido();

        btnAdicionarSaldo.setOnClickListener(v -> {
            Intent it = new Intent(requireContext(), FormAdicionarSaldoActivity.class);
            startActivity(it);
        });

    }

    private void recicleView() {

        recyclerInvestimentos.setLayoutManager(new LinearLayoutManager(requireContext()));

        AnimationSpinner.configurar(
                requireContext(),
                spFiltroDestaques,
                filtros,
                posicao -> {
                    String type = posicao == 0 ? "Mais caros" : posicao == 1 ? "Mais lucrativos" : "Maiores perdas";
                    filtroDestaque = type;
                    carregarDestques(type);
                }
        );
    }

    private void carregarDestques(String type) {
        ApiService api = RetrofitClient.getClient(requireContext()).create(ApiService.class);

        tvDestaquesVazio.setVisibility(View.GONE);
        // Placeholder rows, unless the pull-to-refresh spinner is already showing.
        if (!puxar.atualizando()) {
            EsqueletoAdapter.mostrar(recyclerInvestimentos, R.layout.item_esqueleto_linha, 3);
        }

        api.buscarLista(type).enqueue(puxar.acompanhar(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {

                if (!isAdded()) {
                    return;
                }

                if (!response.isSuccessful() || response.body() == null) {
                    EsqueletoAdapter.ocultar(recyclerInvestimentos, new EmphasisAdapter(new ArrayList<>()));
                    String erro = "";
                    try {
                        if (response.errorBody() != null) erro = response.errorBody().string();
                    } catch (Exception ignored) {
                    }
                    Log.e("DASHBOARD", "Erro HTTP aportes: " + response.code() + " | " + erro);
                    return;
                }

                List<clsAportes> aportes = response.body().getLista();

                if (aportes == null) {
                    Log.e("DASHBOARD", "Lista de aportes nula");
                    EsqueletoAdapter.ocultar(recyclerInvestimentos, new EmphasisAdapter(new ArrayList<>()));
                    return;
                }

                EsqueletoAdapter.ocultar(recyclerInvestimentos, new EmphasisAdapter(aportes));

                if (aportes.isEmpty()) {
                    tvDestaquesVazio.setText(
                            type.equals("Maiores perdas") ? "Sem perdas hoje."
                                    : type.equals("Mais lucrativos") ? "Sem ganhos hoje."
                                    : "Faça seu primeiro aporte para ver seus ativos aqui.");
                    tvDestaquesVazio.setVisibility(View.VISIBLE);
                } else {
                    tvDestaquesVazio.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {

                if (!isAdded()) {
                    return;
                }

                EsqueletoAdapter.ocultar(recyclerInvestimentos, new EmphasisAdapter(new ArrayList<>()));
                Toast.makeText(requireContext(), "Erro ao mostrar aportes", LENGTH_LONG).show();

                Log.e("DASHBOARD", "Erro ao carregar aportes", t);
            }
        }));
    }

    private void carregarGrafico(String periodo) {

        VicoService vico = RetrofitClient.getClient(requireContext()).create(VicoService.class);

        // Placeholder instead of the chart until the new period arrives (not while pulling to refresh).
        if (!puxar.atualizando()) {
            ctvChart.setVisibility(View.INVISIBLE);
            esqueletoGrafico.setVisibility(View.VISIBLE);
        }

        vico.getEvolucao(periodo).enqueue(puxar.acompanhar(new Callback<VicoResponse>() {

            @Override
            public void onResponse(@NonNull Call<VicoResponse> call, @NonNull Response<VicoResponse> response) {

                if (!isAdded()) {
                    return;
                }

                if (!periodo.equals(DashboardFragment.this.periodo)) {
                    return;
                }

                VicoResponse body = response.body();

                if (!response.isSuccessful()
                        || body == null
                        || !body.isSucesso()
                        || body.getPontos() == null) {

                    Log.e("GRAFICO_DASH", "Sem dados para o gráfico. HTTP: " + response.code());
                    mostrarGrafico(new ArrayList<>(), new ArrayList<>(), periodo);
                    return;
                }

                List<String> datas = new ArrayList<>();
                List<Double> valores = new ArrayList<>();

                for (VicoResponse.Ponto ponto : body.getPontos()) {

                    if (ponto == null) {
                        continue;
                    }

                    String data = ponto.getData();
                    double valor = ponto.getValor();

                    if (data == null || data.trim().isEmpty()) {
                        continue;
                    }

                    if (Double.isNaN(valor) || Double.isInfinite(valor)) {
                        continue;
                    }

                    datas.add(data);
                    valores.add(valor);
                }

                mostrarGrafico(datas, valores, periodo);
            }

            @Override
            public void onFailure(@NonNull Call<VicoResponse> call, @NonNull Throwable t) {

                if (!isAdded() || !periodo.equals(DashboardFragment.this.periodo)) {
                    return;
                }

                Log.e("GRAFICO_DASH", "Erro ao carregar evolução", t);

                mostrarGrafico(new ArrayList<>(), new ArrayList<>(), periodo);
            }
        }));
    }

    private void mostrarGrafico(List<String> datas, List<Double> valores, String periodo) {

        boolean eraPlaceholder = esqueletoGrafico.getVisibility() == View.VISIBLE;
        esqueletoGrafico.setVisibility(View.GONE);
        ctvChart.setVisibility(View.VISIBLE);
        if (eraPlaceholder) Esqueleto.revelar(ctvChart);

        ChartHelper.configurarGraficoEvolucao(
                ctvChart,
                datas,
                valores,
                periodo
        );
    }

    private void timeInvested() {

        btn1M.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                periodo = "1M";

                updateColorButtom(btn1M);

                carregarGrafico(periodo);
            }
        });

        btn6M.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                periodo = "6M";

                updateColorButtom(btn6M);
                carregarGrafico(periodo);
            }
        });

        btn1A.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                periodo = "1A";

                updateColorButtom(btn1A);
                carregarGrafico(periodo);
            }
        });
    }

    private void updateColorButtom(TextView btn) {
        btn1M.setBackgroundResource(R.drawable.bg_periodo_normal);
        btn6M.setBackgroundResource(R.drawable.bg_periodo_normal);
        btn1A.setBackgroundResource(R.drawable.bg_periodo_normal);

        btn.setBackgroundResource(R.drawable.bg_periodo_selecionado);
    }

    /** Greeting with the first name; the last profile read is shown at once while it refreshes. */
    private void carregarNome() {
        User conhecido = PerfilCache.obter();
        if (conhecido != null && conhecido.getNome_usuario() != null) {
            mostrarNome(conhecido.getNome_usuario());
        } else {
            Esqueleto.iniciar(tvNomeUsuario);
        }
        RetrofitClient.getApiService(requireContext()).perfilObter().enqueue(new PerfilCallback(this) {
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

    private static String reais(double valor) {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(valor);
    }

    /** Total = free balance + what is invested; only shown when both are known. */
    private void atualizarTotal() {
        if (saldoLivre == null || investido == null) return;
        tvSaldoTotal.setText(reais(saldoLivre + investido));
        Esqueleto.parar(tvSaldoTotal);
    }

    /** What the person holds in assets today, from the portfolio distribution (all wallets). */
    private void carregarInvestido() {
        puxar.iniciar();
        RetrofitClient.getClient(requireContext()).create(VicoService.class)
                .getDistribuicao(null).enqueue(new Callback<VicoResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<VicoResponse> call, @NonNull Response<VicoResponse> response) {
                        puxar.concluir();
                        if (!isAdded() || getView() == null) return;
                        VicoResponse corpo = response.body();
                        if (!response.isSuccessful() || corpo == null || !corpo.isSucesso()) {
                            falhaInvestido();
                            return;
                        }
                        double soma = 0;
                        if (corpo.getDistribuicao() != null) {
                            for (VicoResponse.Distribuicao d : corpo.getDistribuicao()) {
                                if (d != null && Double.isFinite(d.getValor())) soma += d.getValor();
                            }
                        }
                        investido = soma;
                        tvInvestido.setText(reais(soma));
                        Esqueleto.parar(tvInvestido);
                        atualizarTotal();
                    }

                    @Override
                    public void onFailure(@NonNull Call<VicoResponse> call, @NonNull Throwable t) {
                        puxar.concluir();
                        if (!isAdded() || getView() == null) return;
                        falhaInvestido();
                    }
                });
    }

    private void falhaInvestido() {
        if (investido == null) {
            tvInvestido.setText("—");
            tvSaldoTotal.setText("—");
        }
        Esqueleto.parar(tvInvestido, tvSaldoTotal);
    }

    /** Reloads everything on the screen (pull to refresh and return from another screen). */
    private void atualizarTudo() {
        carregarGrafico(periodo);
        carregarDestques(filtroDestaque);
        carregarSaldo();
        carregarInvestido();
    }

    @Override
    public void onResume() {
        super.onResume();
        // After adding balance, buying or selling the numbers change: reload when coming back.
        if (primeiraRetomada) {
            primeiraRetomada = false;
            return;
        }
        atualizarTudo();
    }

    private void carregarSaldo() {
        puxar.iniciar();
        walletRepository.buscarSaldo(new WalletRepository.WalletCallback() {
            @Override
            public void onSuccess(String saldo) {
                puxar.concluir();
                if (!isAdded()) return;
                try {
                    double valor = Double.parseDouble(saldo.trim());
                    saldoLivre = valor;
                    String texto = reais(valor);
                    tvSaldoDisponivel.setText(texto);
                    if (tvSaldoLivre != null) tvSaldoLivre.setText(texto);
                    atualizarTotal();
                } catch (NumberFormatException ignored) {
                    // Keep what is on screen.
                }
                Esqueleto.parar(tvSaldoDisponivel, tvSaldoLivre);
            }

            @Override
            public void onError(String message) {
                puxar.concluir();
                if (!isAdded()) return;
                // Keep the previous value (the server validates the balance on every operation);
                // without one, show a dash instead of an endless placeholder.
                if (saldoLivre == null) {
                    tvSaldoDisponivel.setText("—");
                    tvSaldoLivre.setText("—");
                }
                Esqueleto.parar(tvSaldoDisponivel, tvSaldoLivre);
            }
        });
    }
}