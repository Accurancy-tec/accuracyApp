package com.example.accurancymobileapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.network.service.ApiService;
import com.example.accurancymobileapp.response.AnaliseIaResponse;
import com.example.accurancymobileapp.response.RelatorioResponse;
import com.example.accurancymobileapp.utils.Aviso;
import com.example.accurancymobileapp.utils.CallbackDaTela;
import com.example.accurancymobileapp.utils.PuxarParaAtualizar;
import com.example.accurancymobileapp.utils.TempoRelativo;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.gson.Gson;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Relatório da conta: números do período, observações, análise com IA e resumo para compartilhar. */
public class RelatorioActivity extends AppCompatActivity {

    private static final int[] PERIODOS = {7, 30, 90, 365};
    private static final String[] ROTULOS_PERIODO = {"7 dias", "30 dias", "90 dias", "12 meses"};
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private final NumberFormat moeda = NumberFormat.getCurrencyInstance(PT_BR);
    private final Handler relogio = new Handler(Looper.getMainLooper());

    private ApiService api;
    private Call<RelatorioResponse> chamada;
    private PuxarParaAtualizar puxar;
    private LayoutInflater inflater;
    private RelatorioResponse relatorio;
    private int dias = 30;

    private View conteudo, estado, progresso;
    private TextView tvEstado, tvPeriodo, tvTextoIa, tvStatusIa, tvAviso;
    private MaterialButton btnGerarIa, btnTentar;
    private View btnCompartilhar;
    private GridLayout gradeNumeros;
    private LinearLayout listaObservacoes, listaDistribuicao, listaMovimentacao, listaCarteiras, listaDestaques;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_relatorio);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.raizRelatorio), (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });

        api = RetrofitClient.getApiService(this);
        inflater = LayoutInflater.from(this);
        conteudo = findViewById(R.id.conteudoRelatorio);
        estado = findViewById(R.id.estadoRelatorio);
        progresso = findViewById(R.id.progressoRelatorio);
        tvEstado = findViewById(R.id.tvEstadoRelatorio);
        btnTentar = findViewById(R.id.btnTentarRelatorio);
        tvPeriodo = findViewById(R.id.tvPeriodoRelatorio);
        tvTextoIa = findViewById(R.id.tvTextoIa);
        tvStatusIa = findViewById(R.id.tvStatusIa);
        tvAviso = findViewById(R.id.tvAvisoRelatorio);
        btnGerarIa = findViewById(R.id.btnGerarIa);
        btnCompartilhar = findViewById(R.id.btnCompartilharRelatorio);
        gradeNumeros = findViewById(R.id.gradeNumerosRelatorio);
        listaObservacoes = findViewById(R.id.listaObservacoes);
        listaDistribuicao = findViewById(R.id.listaDistribuicao);
        listaMovimentacao = findViewById(R.id.listaMovimentacao);
        listaCarteiras = findViewById(R.id.listaCarteirasRelatorio);
        listaDestaques = findViewById(R.id.listaDestaques);

        SwipeRefreshLayout swipe = findViewById(R.id.swipeRelatorio);
        puxar = new PuxarParaAtualizar(swipe, this::carregar);

        findViewById(R.id.btnVoltarRelatorio).setOnClickListener(v -> finish());
        btnCompartilhar.setOnClickListener(v -> compartilhar());
        btnGerarIa.setOnClickListener(v -> gerarAnalise());
        btnTentar.setOnClickListener(v -> carregar());
        montarPeriodos();

        carregar();
    }

    @Override
    protected void onDestroy() {
        if (chamada != null) chamada.cancel();
        relogio.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private void montarPeriodos() {
        ChipGroup grupo = findViewById(R.id.grupoPeriodoRelatorio);
        for (int i = 0; i < PERIODOS.length; i++) {
            Chip chip = (Chip) inflater.inflate(R.layout.item_chip_filtro, grupo, false);
            chip.setId(View.generateViewId());
            chip.setText(ROTULOS_PERIODO[i]);
            chip.setCheckable(true);
            chip.setChecked(PERIODOS[i] == dias);
            final int valor = PERIODOS[i];
            chip.setOnClickListener(v -> {
                if (dias == valor) return;
                dias = valor;
                carregar();
            });
            grupo.addView(chip);
        }
    }

    // ---------------------------------------------------------------- carregar

    private void carregar() {
        if (chamada != null) chamada.cancel();
        if (relatorio == null) mostrarEstado(true, "Montando o relatório...", false);
        puxar.iniciar();

        chamada = api.relatorio(dias);
        chamada.enqueue(new CallbackDaTela<RelatorioResponse>(this) {
            @Override
            public void aoSucesso(@NonNull RelatorioResponse resposta) {
                puxar.concluir();
                relatorio = resposta;
                mostrar(resposta);
            }

            @Override
            public void aoErro(@NonNull String mensagem) {
                puxar.concluir();
                if (relatorio == null) {
                    mostrarEstado(false, mensagem, true);
                } else {
                    Aviso.makeText(RelatorioActivity.this, mensagem, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void mostrarEstado(boolean carregando, String texto, boolean comBotao) {
        conteudo.setVisibility(View.GONE);
        estado.setVisibility(View.VISIBLE);
        progresso.setVisibility(carregando ? View.VISIBLE : View.GONE);
        tvEstado.setText(texto);
        btnTentar.setVisibility(comBotao ? View.VISIBLE : View.GONE);
    }

    private void mostrar(RelatorioResponse r) {
        estado.setVisibility(View.GONE);
        conteudo.setVisibility(View.VISIBLE);
        btnCompartilhar.setEnabled(true);

        if (r.periodo != null) {
            tvPeriodo.setText("De " + data(r.periodo.inicio) + " a " + data(r.periodo.fim)
                    + " · valores atualizados agora.");
        }
        mostrarNumeros(r);
        mostrarObservacoes(r.getObservacoes());
        mostrarIa(r.analiseIa);
        mostrarDistribuicao(r.getDistribuicao());
        mostrarMovimentacao(r.movimentacao);
        mostrarCarteiras(r.getCarteiras());
        mostrarDestaques(r.destaques);
        tvAviso.setText(r.aviso == null ? "" : r.aviso);
    }

    // ---------------------------------------------------------------- partes

    private void mostrarNumeros(RelatorioResponse r) {
        gradeNumeros.removeAllViews();
        RelatorioResponse.Real real = r.real != null ? r.real : new RelatorioResponse.Real();
        RelatorioResponse.Simulado sim = r.simulado != null ? r.simulado : new RelatorioResponse.Simulado();

        numero("Patrimônio real", moeda.format(real.patrimonioTotal),
                moeda.format(r.saldoGeral) + " de saldo disponível", 0);
        numero("Investido nas carteiras reais", moeda.format(real.totalInvestido),
                "Valor atual: " + moeda.format(real.valorMercado), 0);
        numero("Resultado dos ativos reais", moeda.format(real.lucroPrejuizo),
                real.totalInvestido > 0 ? pct(real.rentabilidade) + " sobre o valor aplicado" : "Ainda sem ativos reais",
                corDoSinal(real.lucroPrejuizo));
        numero("Carteiras simuladas", sim.quantidadeCarteiras > 0 ? moeda.format(sim.patrimonioTotal) : "—",
                sim.quantidadeCarteiras > 0 ? pct(sim.rentabilidade) + " · dinheiro fictício" : "Nenhuma carteira simulada",
                0);
    }

    private void numero(String rotulo, String valor, String detalhe, @ColorRes int cor) {
        View cartao = inflater.inflate(R.layout.item_relatorio_numero, gradeNumeros, false);
        ((TextView) cartao.findViewById(R.id.tvRotuloNumero)).setText(rotulo);
        TextView tvValor = cartao.findViewById(R.id.tvValorNumero);
        tvValor.setText(valor);
        if (cor != 0) tvValor.setTextColor(ContextCompat.getColor(this, cor));
        ((TextView) cartao.findViewById(R.id.tvDetalheNumero)).setText(detalhe);

        int margem = Math.round(4 * getResources().getDisplayMetrics().density);
        // FILL na linha: os dois cartões da mesma linha ficam com a mesma altura.
        GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL), GridLayout.spec(GridLayout.UNDEFINED, 1f));
        params.width = 0;
        params.setMargins(margem, margem, margem, margem);
        gradeNumeros.addView(cartao, params);
    }

    private void mostrarObservacoes(List<RelatorioResponse.Observacao> observacoes) {
        listaObservacoes.removeAllViews();
        for (RelatorioResponse.Observacao o : observacoes) {
            View linha = inflater.inflate(R.layout.item_relatorio_observacao, listaObservacoes, false);
            ImageView icone = linha.findViewById(R.id.imgObservacao);
            int cor = R.color.blue_primary;
            int desenho = R.drawable.ic_tendencia;
            if ("atencao".equals(o.tipo)) {
                cor = R.color.atencao_destaque;
                desenho = R.drawable.ic_bandeira;
            } else if ("positivo".equals(o.tipo)) {
                cor = R.color.positivo_destaque;
            } else if ("inicio".equals(o.tipo)) {
                desenho = R.drawable.ic_ia;
            } else if ("info".equals(o.tipo)) {
                desenho = R.drawable.ic_ajuda;
            }
            icone.setImageResource(desenho);
            icone.setColorFilter(ContextCompat.getColor(this, cor));
            ((TextView) linha.findViewById(R.id.tvObservacao)).setText(o.texto);
            listaObservacoes.addView(linha);
        }
    }

    private void mostrarDistribuicao(List<RelatorioResponse.Fatia> fatias) {
        listaDistribuicao.removeAllViews();
        if (fatias.isEmpty()) {
            listaDistribuicao.addView(textoSimples("Sem ativos nas carteiras reais."));
            return;
        }
        for (RelatorioResponse.Fatia f : fatias) {
            View linha = inflater.inflate(R.layout.item_relatorio_barra, listaDistribuicao, false);
            ((TextView) linha.findViewById(R.id.tvCategoriaBarra)).setText(f.categoria);
            ((TextView) linha.findViewById(R.id.tvValorBarra)).setText(
                    pctSemSinal(f.percentual) + " · " + moeda.format(f.valor));
            LinearProgressIndicator barra = linha.findViewById(R.id.barraCategoria);
            barra.setProgressCompat((int) Math.round(Math.max(2, Math.min(100, f.percentual))), true);
            listaDistribuicao.addView(linha);
        }
    }

    private void mostrarMovimentacao(@Nullable RelatorioResponse.Movimentacao m) {
        listaMovimentacao.removeAllViews();
        if (m == null || m.real == null || m.simulada == null) return;
        linhaMovimentacao("", "Reais", "Simuladas", true);
        linhaMovimentacao("Compras", m.real.compras + " · " + moeda.format(m.real.valorComprado),
                m.simulada.compras + " · " + moeda.format(m.simulada.valorComprado), false);
        linhaMovimentacao("Vendas", m.real.vendas + " · " + moeda.format(m.real.valorVendido),
                m.simulada.vendas + " · " + moeda.format(m.simulada.valorVendido), false);
        linhaMovimentacao("Dividendos", m.real.dividendos + " · " + moeda.format(m.real.valorDividendos),
                m.simulada.dividendos + " · " + moeda.format(m.simulada.valorDividendos), false);
        linhaMovimentacao("Depósitos", m.depositos + " · " + moeda.format(m.valorDepositos), "", false);
    }

    private void linhaMovimentacao(String rotulo, String real, String simulada, boolean cabecalho) {
        LinearLayout linha = new LinearLayout(this);
        linha.setOrientation(LinearLayout.HORIZONTAL);
        int espaco = Math.round(5 * getResources().getDisplayMetrics().density);
        linha.setPadding(0, espaco, 0, espaco);

        String[] textos = {rotulo, real, simulada};
        float[] pesos = {0.8f, 1.1f, 1.1f};
        for (int i = 0; i < textos.length; i++) {
            TextView tv = new TextView(this);
            tv.setText(textos[i]);
            tv.setTextSize(cabecalho ? 11.5f : 13f);
            tv.setTextColor(ContextCompat.getColor(this, cabecalho ? R.color.texto_fraco
                    : i == 0 ? R.color.text_secondary : R.color.text_primary));
            tv.setGravity(i == 0 ? android.view.Gravity.START : android.view.Gravity.END);
            linha.addView(tv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, pesos[i]));
        }
        listaMovimentacao.addView(linha);
    }

    private void mostrarCarteiras(List<RelatorioResponse.Carteira> carteiras) {
        listaCarteiras.removeAllViews();
        if (carteiras.isEmpty()) {
            listaCarteiras.addView(textoSimples("Você ainda não tem carteiras."));
            return;
        }
        for (RelatorioResponse.Carteira c : carteiras) {
            View cartao = inflater.inflate(R.layout.item_relatorio_carteira, listaCarteiras, false);
            ((TextView) cartao.findViewById(R.id.tvNomeCarteiraRelatorio)).setText(c.nome);
            ((TextView) cartao.findViewById(R.id.tvTipoCarteiraRelatorio)).setText(c.isSimulada() ? "Simulada" : "Real");
            ((TextView) cartao.findViewById(R.id.tvPatrimonioCarteiraRelatorio)).setText(moeda.format(c.patrimonio));

            TextView resultado = cartao.findViewById(R.id.tvResultadoCarteiraRelatorio);
            resultado.setText(moeda.format(c.resultado) + " (" + pct(c.rentabilidade) + ")");
            int cor = corDoSinal(c.resultado);
            resultado.setTextColor(ContextCompat.getColor(this, cor != 0 ? cor : R.color.text_secondary));

            String meta = c.meta <= 0 ? "Sem meta"
                    : c.metaAtingida ? "Meta atingida (" + moeda.format(c.meta) + ")"
                    : Math.round(c.progressoMeta == null ? 0 : c.progressoMeta) + "% da meta de " + moeda.format(c.meta);
            ((TextView) cartao.findViewById(R.id.tvDetalheCarteiraRelatorio)).setText(
                    "Investido " + moeda.format(c.investido) + " · " + meta);
            listaCarteiras.addView(cartao);
        }
    }

    private void mostrarDestaques(@Nullable RelatorioResponse.Destaques destaques) {
        listaDestaques.removeAllViews();
        List<RelatorioResponse.Ativo> altas = destaques == null ? Collections.emptyList() : destaques.getMelhores();
        List<RelatorioResponse.Ativo> quedas = destaques == null ? Collections.emptyList() : destaques.getPiores();

        if (altas.isEmpty() && quedas.isEmpty()) {
            listaDestaques.addView(textoSimples("Nenhum ativo subiu ou caiu desde a compra."));
            return;
        }
        for (RelatorioResponse.Ativo a : altas) listaDestaques.addView(linhaAtivo(a));
        for (RelatorioResponse.Ativo a : quedas) listaDestaques.addView(linhaAtivo(a));
    }

    private View linhaAtivo(RelatorioResponse.Ativo a) {
        View linha = inflater.inflate(R.layout.item_relatorio_ativo, listaDestaques, false);
        ((TextView) linha.findViewById(R.id.tvSimboloAtivoRelatorio)).setText(a.simbolo);
        ((TextView) linha.findViewById(R.id.tvCarteiraAtivoRelatorio)).setText(
                a.carteira + " · " + (a.simulada ? "simulada" : "real"));
        TextView variacao = linha.findViewById(R.id.tvVariacaoAtivoRelatorio);
        boolean alta = a.rentabilidade >= 0;
        variacao.setText(pct(a.rentabilidade));
        variacao.setBackgroundResource(alta ? R.drawable.bg_tag_positivo : R.drawable.bg_tag_negativo);
        variacao.setTextColor(ContextCompat.getColor(this, alta ? R.color.positivo_destaque : R.color.negativo_destaque));
        return linha;
    }

    // ---------------------------------------------------------------- análise com IA

    private void mostrarIa(@Nullable RelatorioResponse.AnaliseIa ia) {
        relogio.removeCallbacksAndMessages(null);
        if (ia == null || !ia.disponivel) {
            tvTextoIa.setVisibility(View.GONE);
            tvStatusIa.setText("A análise com IA não está ativada neste servidor. As observações acima usam os mesmos números.");
            btnGerarIa.setVisibility(View.GONE);
            return;
        }

        btnGerarIa.setVisibility(View.VISIBLE);
        if (ia.ultima != null) {
            mostrarAnalise(ia.ultima);
        } else {
            tvTextoIa.setVisibility(View.GONE);
            tvStatusIa.setText("Peça um texto que explica o relatório em linguagem simples.");
            liberarBotaoIa("Gerar análise");
        }
    }

    private void mostrarAnalise(RelatorioResponse.Analise analise) {
        tvTextoIa.setText(analise.texto);
        tvTextoIa.setVisibility(View.VISIBLE);
        String quando = TempoRelativo.descrever(analise.geradoEm);
        tvStatusIa.setText(quando.isEmpty() ? "" : "Gerada " + quando + ".");
        esperar(analise.esperaSegundos);
    }

    /** Uma análise nova a cada 30 minutos: o botão volta sozinho quando o tempo passa. */
    private void esperar(int segundos) {
        relogio.removeCallbacksAndMessages(null);
        if (segundos <= 0) {
            liberarBotaoIa("Gerar nova análise");
            return;
        }
        btnGerarIa.setEnabled(false);
        btnGerarIa.setText("Nova análise em " + Math.max(1, (int) Math.ceil(segundos / 60.0)) + " min");
        relogio.postDelayed(() -> liberarBotaoIa("Gerar nova análise"), segundos * 1000L);
    }

    private void liberarBotaoIa(String texto) {
        btnGerarIa.setEnabled(true);
        btnGerarIa.setText(texto);
    }

    private void gerarAnalise() {
        btnGerarIa.setEnabled(false);
        btnGerarIa.setText("Escrevendo...");
        tvStatusIa.setText("Escrevendo a análise. Isso pode levar alguns segundos.");

        Map<String, Object> corpo = new HashMap<>();
        corpo.put("dias", dias);
        api.gerarAnaliseIa(corpo).enqueue(new Callback<AnaliseIaResponse>() {
            @Override
            public void onResponse(@NonNull Call<AnaliseIaResponse> call, @NonNull Response<AnaliseIaResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                AnaliseIaResponse resposta = response.body();
                if (response.isSuccessful() && resposta != null && resposta.ok() && resposta.analise != null) {
                    mostrarAnalise(resposta.analise);
                    return;
                }
                AnaliseIaResponse erro = lerErro(response);
                tvStatusIa.setText(erro != null ? erro.texto("Não foi possível gerar a análise agora.")
                        : "Não foi possível gerar a análise agora.");
                if (erro != null && erro.analise != null) {
                    tvTextoIa.setText(erro.analise.texto);
                    tvTextoIa.setVisibility(View.VISIBLE);
                    esperar(erro.analise.esperaSegundos);
                } else {
                    liberarBotaoIa("Tentar de novo");
                }
            }

            @Override
            public void onFailure(@NonNull Call<AnaliseIaResponse> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                tvStatusIa.setText("Sem conexão com o servidor. Verifique a internet e tente de novo.");
                liberarBotaoIa("Tentar de novo");
            }
        });
    }

    @Nullable
    private static AnaliseIaResponse lerErro(Response<?> response) {
        if (response.errorBody() == null) return null;
        try {
            return new Gson().fromJson(response.errorBody().charStream(), AnaliseIaResponse.class);
        } catch (Exception e) {
            return null;
        }
    }

    // ---------------------------------------------------------------- compartilhar

    private void compartilhar() {
        if (relatorio == null || relatorio.real == null) return;
        RelatorioResponse r = relatorio;
        StringBuilder texto = new StringBuilder()
                .append("Meu relatório no Accuracy (").append(rotuloPeriodo()).append(")\n\n")
                .append("Patrimônio real: ").append(moeda.format(r.real.patrimonioTotal)).append('\n')
                .append("Investido: ").append(moeda.format(r.real.totalInvestido))
                .append(" · resultado ").append(moeda.format(r.real.lucroPrejuizo))
                .append(" (").append(pct(r.real.rentabilidade)).append(")\n");
        if (r.simulado != null && r.simulado.quantidadeCarteiras > 0) {
            texto.append("Simulações: ").append(moeda.format(r.simulado.patrimonioTotal))
                    .append(" (").append(pct(r.simulado.rentabilidade)).append(")\n");
        }
        texto.append('\n');
        for (RelatorioResponse.Observacao o : r.getObservacoes()) texto.append("• ").append(o.texto).append('\n');
        if (r.aviso != null) texto.append('\n').append(r.aviso);

        Intent envio = new Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_SUBJECT, "Relatório Accuracy")
                .putExtra(Intent.EXTRA_TEXT, texto.toString());
        startActivity(Intent.createChooser(envio, "Compartilhar resumo"));
    }

    private String rotuloPeriodo() {
        for (int i = 0; i < PERIODOS.length; i++) {
            if (PERIODOS[i] == dias) return "últimos " + ROTULOS_PERIODO[i];
        }
        return "últimos " + dias + " dias";
    }

    // ---------------------------------------------------------------- formatação

    private TextView textoSimples(String texto) {
        TextView tv = new TextView(this);
        tv.setText(texto);
        tv.setTextSize(13f);
        tv.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        return tv;
    }

    /** "2026-11-26" -> "26/11/2026". */
    private static String data(@Nullable String iso) {
        if (iso == null || !iso.matches("\\d{4}-\\d{2}-\\d{2}")) return "";
        String[] partes = iso.split("-");
        return partes[2] + "/" + partes[1] + "/" + partes[0];
    }

    @ColorRes
    private static int corDoSinal(double valor) {
        if (valor > 0) return R.color.positivo_destaque;
        if (valor < 0) return R.color.negativo_destaque;
        return 0;
    }

    /** +3,15% / -0,12% / 0% */
    private static String pct(double valor) {
        String sinal = valor > 0.004 ? "+" : valor < -0.004 ? "-" : "";
        return sinal + pctSemSinal(Math.abs(valor));
    }

    private static String pctSemSinal(double valor) {
        String texto = String.format(PT_BR, "%.2f", valor);
        texto = texto.replaceAll("0+$", "").replaceAll(",$", "");
        return texto + "%";
    }
}
