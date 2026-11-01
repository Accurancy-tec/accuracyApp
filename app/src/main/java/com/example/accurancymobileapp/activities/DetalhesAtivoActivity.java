package com.example.accurancymobileapp.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.compose.ui.platform.ComposeView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.model.QuoteData;
import com.example.accurancymobileapp.network.repository.AssetPositionRepository;
import com.example.accurancymobileapp.network.repository.QuoteRepository;
import com.example.accurancymobileapp.response.AssetPositionResponse;
import com.example.accurancymobileapp.response.StockHistoryResponse;
import com.example.accurancymobileapp.ui.StockChartHelper;

import java.text.NumberFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DetalhesAtivoActivity extends AppCompatActivity {

    public static final String EXTRA_TICKER = "ticker";
    public static final String EXTRA_NOME = "nome_ativo";
    public static final String EXTRA_CATEGORIA = "categoria_ativo";
    public static final String EXTRA_ID_CARTEIRA = "id_carteira";
    public static final String EXTRA_SALDO = "saldo_disponivel";
    public static final String EXTRA_PRECO_ATUAL_COMPRA = "preco_atual_compra";
    public static final String EXTRA_VARIACAO_DIARIA_COMPRA = "variacao_diaria_compra";
    public static final String EXTRA_ULTIMA_ATUALIZACAO_COMPRA = "ultima_atualizacao_compra";
    public static final String EXTRA_QUANTIDADE_DISPONIVEL_COMPRA = "quantidade_disponivel_compra";

    private static final int CHART_BASE_HEIGHT_DP = 190;
    private static final int CHART_MAX_HEIGHT_DP = 320;

    private final Locale ptBR = Locale.forLanguageTag("pt-BR");
    private final NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(ptBR);
    private final List<String> labelsGrafico = new ArrayList<>();
    private final List<Double> valoresGrafico = new ArrayList<>();

    private TextView tvIconeAtivo, tvTickerAtivo, tvNomeAtivo, tvStatusMercado;
    private TextView tvPrecoAtualAtivo, tvVariacaoPrecoAtivo, tvUltimaAtualizacao;
    private TextView tvPrecoMedio, tvQuantidadeAtivo, tvValorPosicao;
    private TextView tvVariacaoRentabilidade, tvValorRentabilidade, tvSaldoInvestir;
    private TextView tvTituloEstatisticas;

    private View btnAtualizarCotacao;
    private View btnComprarAtivo;
    private View btnVenderAtivo;

    private TextView filtro1S;
    private TextView filtro1M;
    private TextView filtro3M;
    private TextView filtro6M;

    private View detalhesAtivoRoot;
    private ScrollView scrollDetalhesAtivo;
    private FrameLayout containerGraficoAtivo;
    private ComposeView graficoAtivo;

    private AssetPositionRepository assetPositionRepository;
    private int assetPositionRequestId;
    private QuoteRepository quoteRepository;

    private String ticker;
    private String nomeAtivo;
    private String categoriaAtivo;
    private String periodoSelecionado = "3mo";
    private String etiquetaPeriodo = "3M";

    private double precoAtual = 0.0;
    private double quantidadePosicao = 0.0;
    private double precoMedio = 0.0;
    private double variacaoDiaria = Double.NaN;

    private int idCarteira = -1;

    private boolean carregandoCotacao = false;
    private boolean carregandoHistorico = false;
    private String erroHistorico;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_ativo);

        ticker = getIntent().getStringExtra(EXTRA_TICKER);
        nomeAtivo = getIntent().getStringExtra(EXTRA_NOME);
        categoriaAtivo = getIntent().getStringExtra(EXTRA_CATEGORIA);
        idCarteira = getIntent().getIntExtra(EXTRA_ID_CARTEIRA, -1);

        if (ticker == null || ticker.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    "Não foi informado qual ativo abrir.",
                    Toast.LENGTH_LONG
            ).show();
            finish();
            return;
        }

        if (nomeAtivo == null || nomeAtivo.trim().isEmpty()) {
            nomeAtivo = ticker;
        }

        if (categoriaAtivo == null || categoriaAtivo.trim().isEmpty()) {
            categoriaAtivo = "Ações";
        }

        vincularViews();
        configurarBarrasSistema();
        configurarTelaInicial();
        configurarEventos();
        configurarGraficoResponsivo();

        quoteRepository = new QuoteRepository(this);
        assetPositionRepository = new AssetPositionRepository(this);

        desenharGrafico(true, null);
        carregarCotacao();
        carregarHistorico(periodoSelecionado, etiquetaPeriodo);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAssetPosition();
    }

    private void vincularViews() {
        detalhesAtivoRoot = findViewById(R.id.detalhesAtivoRoot);
        scrollDetalhesAtivo = findViewById(R.id.scrollDetalhesAtivo);
        containerGraficoAtivo = findViewById(R.id.containerGraficoAtivo);

        tvIconeAtivo = findViewById(R.id.tvIconeAtivo);
        tvTickerAtivo = findViewById(R.id.tvTickerAtivo);
        tvNomeAtivo = findViewById(R.id.tvNomeAtivo);
        tvStatusMercado = findViewById(R.id.tvStatusMercado);
        tvPrecoAtualAtivo = findViewById(R.id.tvPrecoAtualAtivo);
        tvVariacaoPrecoAtivo = findViewById(R.id.tvVariacaoPrecoAtivo);
        tvUltimaAtualizacao = findViewById(R.id.tvUltimaAtualizacao);
        tvPrecoMedio = findViewById(R.id.tvPrecoMedio);
        tvQuantidadeAtivo = findViewById(R.id.tvQuantidadeAtivo);
        tvValorPosicao = findViewById(R.id.tvValorPosicao);
        tvVariacaoRentabilidade = findViewById(R.id.tvVariacaoRentabilidade);
        tvValorRentabilidade = findViewById(R.id.tvValorRentabilidade);
        tvSaldoInvestir = findViewById(R.id.tvSaldoInvestir);
        tvTituloEstatisticas = findViewById(R.id.tvTituloEstatisticas);

        btnAtualizarCotacao = findViewById(R.id.btnAtualizarCotacao);
        btnComprarAtivo = findViewById(R.id.btnComprarAtivo);
        btnVenderAtivo = findViewById(R.id.btnVenderAtivo);

        filtro1S = findViewById(R.id.filtro1S);
        filtro1M = findViewById(R.id.filtro1M);
        filtro3M = findViewById(R.id.filtro3M);
        filtro6M = findViewById(R.id.filtro6M);

        ImageButton btnVoltarDetalhes = findViewById(R.id.btnVoltarDetalhes);
        btnVoltarDetalhes.setOnClickListener(view -> finish());

        graficoAtivo = new ComposeView(this);
        graficoAtivo.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        containerGraficoAtivo.removeAllViews();
        containerGraficoAtivo.addView(graficoAtivo);

        tvTituloEstatisticas.setText("Estatísticas de " + ticker);
    }

    private void configurarTelaInicial() {
        tvIconeAtivo.setText(ticker.length() > 2 ? ticker.substring(0, 2) : ticker);
        tvTickerAtivo.setText(ticker);
        tvNomeAtivo.setText(nomeAtivo);
        tvStatusMercado.setText("Buscando cotação");
        tvPrecoAtualAtivo.setText("R$ 0");
        tvVariacaoPrecoAtivo.setText("0%");
        tvUltimaAtualizacao.setText("Última atualização: aguardando dados");

        tvPrecoMedio.setText(precoMedio > 0
                ? formatoMoeda.format(precoMedio)
                : "R$ 0");

        tvQuantidadeAtivo.setText(quantidadePosicao > 0
                ? formatarQuantidade(quantidadePosicao)
                : "0");

        tvValorPosicao.setText(precoAtual > 0 && quantidadePosicao > 0
                ? formatoMoeda.format(precoAtual * quantidadePosicao)
                : "—");

        tvVariacaoRentabilidade.setText("0%");
        tvValorRentabilidade.setText("0%");

        double saldo = getIntent().getDoubleExtra(EXTRA_SALDO, -1.0);
        tvSaldoInvestir.setText(saldo >= 0
                ? formatoMoeda.format(saldo)
                : "R$ 0");

        tvTituloEstatisticas.setText("Estatísticas de " + ticker);
        selecionarFiltroVisual(filtro3M);
    }

    private void configurarEventos() {
        btnAtualizarCotacao.setOnClickListener(view -> {
            carregarCotacao();
            carregarHistorico(periodoSelecionado, etiquetaPeriodo);
        });

        btnComprarAtivo.setOnClickListener(view -> abrirTelaOperacao("Compra"));
        btnVenderAtivo.setOnClickListener(view -> abrirTelaOperacao("Venda"));

        filtro1S.setOnClickListener(view -> selecionarPeriodo("7d", "1S", filtro1S));
        filtro1M.setOnClickListener(view -> selecionarPeriodo("1mo", "1M", filtro1M));
        filtro3M.setOnClickListener(view -> selecionarPeriodo("3mo", "3M", filtro3M));
        filtro6M.setOnClickListener(view -> selecionarPeriodo("6mo", "6M", filtro6M));
    }

    private void selecionarPeriodo(String range, String etiqueta, TextView filtro) {
        periodoSelecionado = range;
        etiquetaPeriodo = etiqueta;
        selecionarFiltroVisual(filtro);
        carregarHistorico(range, etiqueta);
    }

    private void selecionarFiltroVisual(TextView selecionado) {
        TextView[] filtros = {filtro1S, filtro1M, filtro3M, filtro6M};

        for (TextView filtro : filtros) {
            boolean ativo = filtro == selecionado;

            filtro.setBackgroundColor(ContextCompat.getColor(
                    this,
                    ativo ? R.color.btn_primary : android.R.color.transparent
            ));

            filtro.setTextColor(ContextCompat.getColor(
                    this,
                    ativo ? R.color.blue_primary : R.color.text_secondary
            ));
        }
    }

    private void carregarCotacao() {
        if (carregandoCotacao) {
            return;
        }

        carregandoCotacao = true;
        tvStatusMercado.setText("Atualizando cotação…");

        quoteRepository.buscarCotacoes(
                ticker,
                categoriaAtivo,
                new QuoteRepository.QuoteCallback() {
                    @Override
                    public void onSucesso(List<QuoteData> quotes) {
                        carregandoCotacao = false;

                        if (isFinishing() || isDestroyed()
                                || quotes == null || quotes.isEmpty()) {
                            return;
                        }

                        QuoteData quote = quotes.get(0);
                        Double preco = quote.getRegularMarketPrice();

                        if (preco == null || !Double.isFinite(preco) || preco <= 0) {
                            tvStatusMercado.setText("Cotação indisponível");
                            tvPrecoAtualAtivo.setText("R$ 0");
                            tvVariacaoPrecoAtivo.setText("0%");
                            tvUltimaAtualizacao.setText(
                                    "Última atualização: cotação não fornecida pela API"
                            );
                            atualizarResumoPosicao();
                            return;
                        }

                        precoAtual = preco;

                        String nomeDaBrapi = quote.getLongName() != null
                                && !quote.getLongName().trim().isEmpty()
                                ? quote.getLongName()
                                : quote.getShortName();

                        if (nomeDaBrapi != null && !nomeDaBrapi.trim().isEmpty()) {
                            nomeAtivo = nomeDaBrapi;
                            tvNomeAtivo.setText(nomeAtivo);
                        }

                        tvTickerAtivo.setText(
                                quote.getSymbol() == null ? ticker : quote.getSymbol()
                        );
                        tvPrecoAtualAtivo.setText(formatoMoeda.format(precoAtual));

                        Double variacao = quote.getRegularMarketChangePercent();
                        variacaoDiaria = variacao != null && Double.isFinite(variacao)
                                ? variacao
                                : Double.NaN;

                        if (variacao == null || !Double.isFinite(variacao)) {
                            tvVariacaoPrecoAtivo.setText("0%");
                            tvVariacaoPrecoAtivo.setTextColor(ContextCompat.getColor(
                                    DetalhesAtivoActivity.this,
                                    R.color.text_secondary
                            ));
                        } else {
                            String seta = variacao > 0
                                    ? "▲ "
                                    : variacao < 0 ? "▼ " : "• ";

                            tvVariacaoPrecoAtivo.setText(seta + String.format(
                                    ptBR,
                                    "%.2f%%",
                                    Math.abs(variacao)
                            ));

                            tvVariacaoPrecoAtivo.setTextColor(ContextCompat.getColor(
                                    DetalhesAtivoActivity.this,
                                    variacao < 0
                                            ? R.color.red_negative
                                            : variacao > 0
                                            ? R.color.green_positive
                                            : R.color.text_secondary
                            ));
                        }

                        tvStatusMercado.setText("Cotação recebida");
                        tvUltimaAtualizacao.setText(
                                "Última atualização: "
                                        + formatarHorario(quote.getRegularMarketTime())
                        );

                        atualizarResumoPosicao();
                    }

                    @Override
                    public void onErro(String mensagem) {
                        carregandoCotacao = false;

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        tvStatusMercado.setText("Cotação indisponível");
                        tvPrecoAtualAtivo.setText("R$ 0");
                        tvVariacaoPrecoAtivo.setText("0%");
                        tvUltimaAtualizacao.setText(
                                "Última atualização: sem cotação disponível"
                        );

                        atualizarResumoPosicao();

                        Toast.makeText(
                                DetalhesAtivoActivity.this,
                                mensagem,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void configurarBarrasSistema() {
        Window window = getWindow();

        WindowCompat.setDecorFitsSystemWindows(window, false);

        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);

        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(
                window,
                window.getDecorView()
        );

        controller.setAppearanceLightStatusBars(false);
        controller.setAppearanceLightNavigationBars(false);

        final int initialLeft = detalhesAtivoRoot.getPaddingLeft();
        final int initialTop = detalhesAtivoRoot.getPaddingTop();
        final int initialRight = detalhesAtivoRoot.getPaddingRight();
        final int initialBottom = detalhesAtivoRoot.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(
                detalhesAtivoRoot,
                (view, windowInsets) -> {
                    Insets systemBars = windowInsets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    view.setPadding(
                            initialLeft + systemBars.left,
                            initialTop + systemBars.top,
                            initialRight + systemBars.right,
                            initialBottom + systemBars.bottom
                    );

                    return windowInsets;
                }
        );

        ViewCompat.requestApplyInsets(detalhesAtivoRoot);
    }

    private void configurarGraficoResponsivo() {
        View conteudo = scrollDetalhesAtivo.getChildAt(0);

        if (conteudo != null) {
            conteudo.addOnLayoutChangeListener(
                    (view, left, top, right, bottom,
                     oldLeft, oldTop, oldRight, oldBottom) ->
                            scrollDetalhesAtivo.post(this::ajustarAlturaGrafico)
            );
        }

        scrollDetalhesAtivo.addOnLayoutChangeListener(
                (view, left, top, right, bottom,
                 oldLeft, oldTop, oldRight, oldBottom) ->
                        scrollDetalhesAtivo.post(this::ajustarAlturaGrafico)
        );

        scrollDetalhesAtivo.post(this::ajustarAlturaGrafico);
    }

    private void ajustarAlturaGrafico() {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        int viewportHeight = scrollDetalhesAtivo.getHeight();
        int viewportWidth = scrollDetalhesAtivo.getWidth();
        View conteudo = scrollDetalhesAtivo.getChildAt(0);

        if (viewportHeight <= 0 || viewportWidth <= 0 || conteudo == null) {
            return;
        }

        int baseHeight = dp(CHART_BASE_HEIGHT_DP);
        int maxHeight = dp(CHART_MAX_HEIGHT_DP);

        ViewGroup.LayoutParams params = containerGraficoAtivo.getLayoutParams();

        int currentHeight = params.height > 0 ? params.height : baseHeight;

        conteudo.measure(
                View.MeasureSpec.makeMeasureSpec(
                        viewportWidth,
                        View.MeasureSpec.EXACTLY
                ),
                View.MeasureSpec.makeMeasureSpec(
                        0,
                        View.MeasureSpec.UNSPECIFIED
                )
        );

        int naturalContentHeight = conteudo.getMeasuredHeight()
                - currentHeight
                + baseHeight;

        int availableExtraHeight = Math.max(
                0,
                viewportHeight - naturalContentHeight
        );

        int targetHeight = Math.min(
                maxHeight,
                baseHeight + availableExtraHeight
        );

        if (targetHeight != currentHeight) {
            params.height = targetHeight;
            containerGraficoAtivo.setLayoutParams(params);
        }
    }

    private int dp(int value) {
        return Math.round(
                value * getResources().getDisplayMetrics().density
        );
    }

    private void atualizarResumoPosicao() {
        tvPrecoMedio.setText(precoMedio > 0
                ? formatoMoeda.format(precoMedio)
                : "R$ 0");

        tvQuantidadeAtivo.setText(quantidadePosicao > 0
                ? formatarQuantidade(quantidadePosicao)
                : "—");

        if (quantidadePosicao <= 0 || precoMedio <= 0 || precoAtual <= 0) {
            tvValorPosicao.setText(
                    quantidadePosicao > 0 && precoAtual > 0
                            ? formatoMoeda.format(quantidadePosicao * precoAtual)
                            : "—"
            );
            tvVariacaoRentabilidade.setText("0%");
            tvValorRentabilidade.setText("0%");
            return;
        }

        double valorPosicao = quantidadePosicao * precoAtual;
        double resultado = (precoAtual - precoMedio) * quantidadePosicao;
        double percentual = ((precoAtual / precoMedio) - 1.0) * 100.0;

        tvValorPosicao.setText(formatoMoeda.format(valorPosicao));
        tvValorRentabilidade.setText(formatoMoeda.format(resultado));

        tvVariacaoRentabilidade.setText(String.format(
                ptBR,
                "%s %.2f%%",
                percentual >= 0 ? "▲" : "▼",
                Math.abs(percentual)
        ));

        tvVariacaoRentabilidade.setTextColor(ContextCompat.getColor(
                this,
                percentual >= 0 ? R.color.green_positive : R.color.red_negative
        ));

        tvValorRentabilidade.setTextColor(ContextCompat.getColor(
                this,
                resultado >= 0 ? R.color.green_positive : R.color.red_negative
        ));
    }

    private void carregarHistorico(String range, String etiqueta) {
        carregandoHistorico = true;
        erroHistorico = null;

        desenharGrafico(true, null);

        quoteRepository.buscarHistorico(
                ticker,
                range,
                categoriaAtivo,
                new QuoteRepository.HistoricoCallback() {
                    @Override
                    public void onSucesso(List<StockHistoryResponse.Point> pontos) {
                        carregandoHistorico = false;

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        labelsGrafico.clear();
                        valoresGrafico.clear();

                        if (pontos != null) {
                            List<StockHistoryResponse.Point> ordenados =
                                    new ArrayList<>(pontos);

                            ordenados.sort((a, b) -> Long.compare(
                                    a.getDate(),
                                    b.getDate()
                            ));

                            for (StockHistoryResponse.Point ponto : ordenados) {
                                if (ponto == null) {
                                    continue;
                                }

                                Double fechamento = ponto.getClose() != null
                                        ? ponto.getClose()
                                        : ponto.getAdjustedClose();

                                if (fechamento == null || fechamento <= 0) {
                                    continue;
                                }

                                String data = Instant.ofEpochSecond(ponto.getDate())
                                        .atZone(ZoneId.of("America/Sao_Paulo"))
                                        .toLocalDate()
                                        .toString();

                                labelsGrafico.add(data);
                                valoresGrafico.add(fechamento);
                            }
                        }

                        desenharGrafico(false, null);
                    }

                    @Override
                    public void onErro(String mensagem) {
                        carregandoHistorico = false;

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        erroHistorico = mensagem;
                        desenharGrafico(false, mensagem);
                    }
                }
        );
    }

    private void desenharGrafico(boolean loading, String erro) {
        StockChartHelper.configurar(
                graficoAtivo,
                new ArrayList<>(labelsGrafico),
                new ArrayList<>(valoresGrafico),
                etiquetaPeriodo,
                loading,
                erro
        );
    }

    private void abrirTelaOperacao(String tipo) {
        if (!Double.isFinite(precoAtual) || precoAtual <= 0.0) {
            Toast.makeText(
                    this,
                    "Aguarde uma cotação válida antes de registrar a operação.",
                    Toast.LENGTH_LONG
            ).show();

            carregarCotacao();
            return;
        }

        if ("Venda".equals(tipo) && quantidadePosicao <= 0.0) {
            Toast.makeText(
                    this,
                    "Você não possui unidades deste ativo disponíveis para venda.",
                    Toast.LENGTH_LONG
            ).show();

            loadAssetPosition();
            return;
        }

        Intent asset = new Intent(
                DetalhesAtivoActivity.this,
                BuyAssetActivity.class
        );

        asset.putExtra(EXTRA_TICKER, ticker);
        asset.putExtra(EXTRA_NOME, nomeAtivo);
        asset.putExtra(EXTRA_CATEGORIA, categoriaAtivo);
        asset.putExtra(EXTRA_ID_CARTEIRA, idCarteira);
        asset.putExtra(
                EXTRA_SALDO,
                getIntent().getDoubleExtra(EXTRA_SALDO, -1.0)
        );
        asset.putExtra(EXTRA_PRECO_ATUAL_COMPRA, precoAtual);
        asset.putExtra(EXTRA_VARIACAO_DIARIA_COMPRA, variacaoDiaria);
        asset.putExtra(
                EXTRA_ULTIMA_ATUALIZACAO_COMPRA,
                tvUltimaAtualizacao.getText().toString()
        );
        asset.putExtra(EXTRA_QUANTIDADE_DISPONIVEL_COMPRA, quantidadePosicao);
        asset.putExtra(BuyAssetActivity.EXTRA_TIPO_OPERACAO, tipo);

        startActivity(asset);
    }

    private String formatarHorario(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return "não informado pela API";
        }

        try {
            return OffsetDateTime.parse(valor)
                    .atZoneSameInstant(ZoneId.of("America/Sao_Paulo"))
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", ptBR));
        } catch (Exception ignored) {
            return valor;
        }
    }

    private String formatarQuantidade(double quantidade) {
        return String.format(
                ptBR,
                quantidade == Math.rint(quantidade) ? "%.0f" : "%.4f",
                quantidade
        );
    }

    private void limparResumoPosicao() {
        quantidadePosicao = 0.0;
        precoMedio = 0.0;

        tvPrecoMedio.setText("—");
        tvQuantidadeAtivo.setText("—");
        tvValorPosicao.setText("—");
        tvVariacaoRentabilidade.setText("—");
        tvValorRentabilidade.setText("—");
    }

    private void loadAssetPosition() {
        if (ticker == null
                || ticker.trim().isEmpty()
                || assetPositionRepository == null) {
            return;
        }

        int requestId = ++assetPositionRequestId;

        assetPositionRepository.getAssetPosition(
                ticker,
                new AssetPositionRepository.PositionCallback() {
                    @Override
                    public void onSuccess(AssetPositionResponse position) {
                        if (requestId != assetPositionRequestId
                                || isFinishing()
                                || isDestroyed()) {
                            return;
                        }

                        Double quantity = position.getQuantity();
                        Double averagePrice = position.getAveragePrice();

                        quantidadePosicao = quantity != null
                                && Double.isFinite(quantity)
                                && quantity > 0
                                ? quantity
                                : 0.0;

                        precoMedio = averagePrice != null
                                && Double.isFinite(averagePrice)
                                && averagePrice > 0
                                ? averagePrice
                                : 0.0;

                        atualizarResumoPosicao();
                    }

                    @Override
                    public void onError(String message) {
                        if (requestId != assetPositionRequestId
                                || isFinishing()
                                || isDestroyed()) {
                            return;
                        }

                        Log.w(
                                "ASSET_POSITION",
                                "Unable to load position for " + ticker + ": " + message
                        );

                        limparResumoPosicao();

                        Toast.makeText(
                                DetalhesAtivoActivity.this,
                                "Não foi possível consultar sua posição.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }
}
