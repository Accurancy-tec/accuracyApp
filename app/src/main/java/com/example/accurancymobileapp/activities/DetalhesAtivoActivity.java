package com.example.accurancymobileapp.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.compose.ui.platform.ComposeView;
import androidx.core.content.ContextCompat;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.model.QuoteData;
import com.example.accurancymobileapp.model.Wallet;
import com.example.accurancymobileapp.model.clsAportes;
import com.example.accurancymobileapp.network.repository.AporteRepository;
import com.example.accurancymobileapp.network.repository.QuoteRepository;
import com.example.accurancymobileapp.network.repository.WalletRepository;
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

/** Detalhes de um ativo. Usa as rotas PHP existentes/nova rota histórica e registra ordens simuladas. */
public class DetalhesAtivoActivity extends AppCompatActivity {

    public static final String EXTRA_TICKER = "ticker";
    public static final String EXTRA_NOME = "nome_ativo";
    public static final String EXTRA_CATEGORIA = "categoria_ativo";
    public static final String EXTRA_ID_CARTEIRA = "id_carteira";
    public static final String EXTRA_QUANTIDADE = "quantidade_posicao";
    public static final String EXTRA_PRECO_MEDIO = "preco_medio_posicao";
    public static final String EXTRA_SALDO = "saldo_disponivel";

    private final Locale ptBR = Locale.forLanguageTag("pt-BR");
    private final NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(ptBR);
    private final List<String> labelsGrafico = new ArrayList<>();
    private final List<Double> valoresGrafico = new ArrayList<>();

    private TextView tvIconeAtivo, tvTickerAtivo, tvNomeAtivo, tvStatusMercado;
    private TextView tvPrecoAtualAtivo, tvVariacaoPrecoAtivo, tvUltimaAtualizacao;
    private TextView tvPrecoMedio, tvQuantidadeAtivo, tvValorPosicao;
    private TextView tvVariacaoRentabilidade, tvValorRentabilidade, tvSaldoInvestir;
    private View btnAtualizarCotacao, btnComprarAtivo, btnVenderAtivo;
    private TextView filtro1S, filtro1M, filtro3M, filtro6M;
    private ComposeView graficoAtivo;

    private QuoteRepository quoteRepository;
    private String ticker;
    private String nomeAtivo;
    private String categoriaAtivo;
    private String periodoSelecionado = "3mo";
    private String etiquetaPeriodo = "3M";
    private double precoAtual = 0.0;
    private double quantidadePosicao = 0.0;
    private double precoMedio = 0.0;
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
        quantidadePosicao = getIntent().getDoubleExtra(EXTRA_QUANTIDADE, 0.0);
        precoMedio = getIntent().getDoubleExtra(EXTRA_PRECO_MEDIO, 0.0);

        if (ticker == null || ticker.trim().isEmpty()) {
            Toast.makeText(this, "Não foi informado qual ativo abrir.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (nomeAtivo == null || nomeAtivo.trim().isEmpty()) nomeAtivo = ticker;
        if (categoriaAtivo == null || categoriaAtivo.trim().isEmpty()) categoriaAtivo = "Ações";

        vincularViews();
        configurarTelaInicial();
        configurarEventos();

        quoteRepository = new QuoteRepository(this);
        desenharGrafico(true, null);
        carregarCotacao();
        carregarHistorico(periodoSelecionado, etiquetaPeriodo);
    }

    private void vincularViews() {
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

        btnAtualizarCotacao = findViewById(R.id.btnAtualizarCotacao);
        btnComprarAtivo = findViewById(R.id.btnComprarAtivo);
        btnVenderAtivo = findViewById(R.id.btnVenderAtivo);
        filtro1S = findViewById(R.id.filtro1S);
        filtro1M = findViewById(R.id.filtro1M);
        filtro3M = findViewById(R.id.filtro3M);
        filtro6M = findViewById(R.id.filtro6M);

        ImageButton voltar = findViewById(R.id.btnVoltarDetalhes);
        voltar.setOnClickListener(v -> finish());

        FrameLayout containerGrafico = findViewById(R.id.containerGraficoAtivo);
        graficoAtivo = new ComposeView(this);
        graficoAtivo.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        containerGrafico.removeAllViews();
        containerGrafico.addView(graficoAtivo);
    }

    private void configurarTelaInicial() {
        tvIconeAtivo.setText(ticker.length() > 2 ? ticker.substring(0, 2) : ticker);
        tvTickerAtivo.setText(ticker);
        tvNomeAtivo.setText(nomeAtivo);
        tvStatusMercado.setText("Buscando cotação");
        tvPrecoAtualAtivo.setText("—");
        tvVariacaoPrecoAtivo.setText("—");
        tvUltimaAtualizacao.setText("Última atualização: aguardando dados");
        tvPrecoMedio.setText(precoMedio > 0 ? formatoMoeda.format(precoMedio) : "—");
        tvQuantidadeAtivo.setText(quantidadePosicao > 0 ? formatarQuantidade(quantidadePosicao) : "—");
        tvValorPosicao.setText(precoAtual > 0 && quantidadePosicao > 0 ? formatoMoeda.format(precoAtual * quantidadePosicao) : "—");
        tvVariacaoRentabilidade.setText("—");
        tvValorRentabilidade.setText("—");
        double saldo = getIntent().getDoubleExtra(EXTRA_SALDO, -1.0);
        tvSaldoInvestir.setText(saldo >= 0 ? formatoMoeda.format(saldo) : "—");
        selecionarFiltroVisual(filtro3M);
    }

    private void configurarEventos() {
        btnAtualizarCotacao.setOnClickListener(v -> {
            carregarCotacao();
            carregarHistorico(periodoSelecionado, etiquetaPeriodo);
        });
        btnComprarAtivo.setOnClickListener(v -> mostrarDialogoQuantidade("Compra"));
        btnVenderAtivo.setOnClickListener(v -> mostrarDialogoQuantidade("Venda"));
        filtro1S.setOnClickListener(v -> selecionarPeriodo("7d", "1S", filtro1S));
        filtro1M.setOnClickListener(v -> selecionarPeriodo("1mo", "1M", filtro1M));
        filtro3M.setOnClickListener(v -> selecionarPeriodo("3mo", "3M", filtro3M));
        filtro6M.setOnClickListener(v -> selecionarPeriodo("6mo", "6M", filtro6M));
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
            filtro.setBackgroundColor(ContextCompat.getColor(this, ativo ? R.color.btn_primary : android.R.color.transparent));
            filtro.setTextColor(ContextCompat.getColor(this, ativo ? R.color.blue_primary : R.color.text_secondary));
        }
    }

    private void carregarCotacao() {
        if (carregandoCotacao) return;
        carregandoCotacao = true;
        tvStatusMercado.setText("Atualizando cotação…");
        quoteRepository.buscarCotacoes(ticker, new QuoteRepository.QuoteCallback() {
            @Override
            public void onSucesso(List<QuoteData> quotes) {
                carregandoCotacao = false;
                if (isFinishing() || isDestroyed() || quotes == null || quotes.isEmpty()) return;
                QuoteData quote = quotes.get(0);
                precoAtual = quote.getRegularMarketPrice();
                String nomeDaBrapi = quote.getLongName() != null && !quote.getLongName().trim().isEmpty()
                        ? quote.getLongName() : quote.getShortName();
                if (nomeDaBrapi != null && !nomeDaBrapi.trim().isEmpty()) {
                    nomeAtivo = nomeDaBrapi;
                    tvNomeAtivo.setText(nomeAtivo);
                }
                tvTickerAtivo.setText(quote.getSymbol() == null ? ticker : quote.getSymbol());
                tvPrecoAtualAtivo.setText(formatoMoeda.format(precoAtual));

                double variacao = quote.getRegularMarketChangePercent();
                String seta = variacao > 0 ? "▲ " : variacao < 0 ? "▼ " : "• ";
                tvVariacaoPrecoAtivo.setText(seta + String.format(ptBR, "%.2f%%", Math.abs(variacao)));
                tvVariacaoPrecoAtivo.setTextColor(ContextCompat.getColor(
                        DetalhesAtivoActivity.this,
                        variacao >= 0 ? R.color.green_positive : R.color.red_negative
                ));
                tvStatusMercado.setText("Cotação recebida");
                tvUltimaAtualizacao.setText("Última atualização: " + formatarHorario(quote.getRegularMarketTime()));
                atualizarResumoPosicao();
            }

            @Override
            public void onErro(String mensagem) {
                carregandoCotacao = false;
                if (isFinishing() || isDestroyed()) return;
                tvStatusMercado.setText("Cotação indisponível");
                Toast.makeText(DetalhesAtivoActivity.this, mensagem, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void atualizarResumoPosicao() {
        tvPrecoMedio.setText(precoMedio > 0 ? formatoMoeda.format(precoMedio) : "—");
        tvQuantidadeAtivo.setText(quantidadePosicao > 0 ? formatarQuantidade(quantidadePosicao) : "—");
        if (quantidadePosicao <= 0 || precoMedio <= 0 || precoAtual <= 0) {
            tvValorPosicao.setText(quantidadePosicao > 0 && precoAtual > 0 ? formatoMoeda.format(quantidadePosicao * precoAtual) : "—");
            tvVariacaoRentabilidade.setText("—");
            tvValorRentabilidade.setText("—");
            return;
        }
        double valorPosicao = quantidadePosicao * precoAtual;
        double resultado = (precoAtual - precoMedio) * quantidadePosicao;
        double percentual = ((precoAtual / precoMedio) - 1.0) * 100.0;
        tvValorPosicao.setText(formatoMoeda.format(valorPosicao));
        tvValorRentabilidade.setText(formatoMoeda.format(resultado));
        tvVariacaoRentabilidade.setText(String.format(ptBR, "%s %.2f%%", percentual >= 0 ? "▲" : "▼", Math.abs(percentual)));
        tvVariacaoRentabilidade.setTextColor(ContextCompat.getColor(this, percentual >= 0 ? R.color.green_positive : R.color.red_negative));
        tvValorRentabilidade.setTextColor(ContextCompat.getColor(this, resultado >= 0 ? R.color.green_positive : R.color.red_negative));
    }

    private void carregarHistorico(String range, String etiqueta) {
        carregandoHistorico = true;
        erroHistorico = null;
        desenharGrafico(true, null);
        quoteRepository.buscarHistorico(ticker, range, new QuoteRepository.HistoricoCallback() {
            @Override
            public void onSucesso(List<StockHistoryResponse.Point> pontos) {
                carregandoHistorico = false;
                if (isFinishing() || isDestroyed()) return;
                labelsGrafico.clear();
                valoresGrafico.clear();
                if (pontos != null) {
                    // A brapi normalmente retorna do mais recente para o mais antigo; o gráfico usa crescente.
                    List<StockHistoryResponse.Point> ordenados = new ArrayList<>(pontos);
                    ordenados.sort((a, b) -> Long.compare(a.getDate(), b.getDate()));
                    for (StockHistoryResponse.Point ponto : ordenados) {
                        if (ponto == null) continue;
                        Double fechamento = ponto.getClose() != null ? ponto.getClose() : ponto.getAdjustedClose();
                        if (fechamento == null || fechamento <= 0) continue;
                        String data = Instant.ofEpochSecond(ponto.getDate())
                                .atZone(ZoneId.of("America/Sao_Paulo"))
                                .toLocalDate().toString();
                        labelsGrafico.add(data);
                        valoresGrafico.add(fechamento);
                    }
                }
                desenharGrafico(false, null);
            }

            @Override
            public void onErro(String mensagem) {
                carregandoHistorico = false;
                if (isFinishing() || isDestroyed()) return;
                erroHistorico = mensagem;
                desenharGrafico(false, mensagem);
            }
        });
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

    private void mostrarDialogoQuantidade(String tipo) {
        if (precoAtual <= 0) {
            Toast.makeText(this, "Aguarde uma cotação válida antes de registrar a operação.", Toast.LENGTH_LONG).show();
            carregarCotacao();
            return;
        }

        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setHint("Ex.: 2 ou 0,5");
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding / 2, padding, padding / 2);

        new AlertDialog.Builder(this)
                .setTitle(tipo + " " + ticker)
                .setMessage("Preço de referência: " + formatoMoeda.format(precoAtual) + "\nInforme a quantidade de unidades. A operação será registrada na carteira selecionada.")
                .setView(input)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Continuar", (dialog, which) -> {
                    String valorTexto = input.getText().toString().trim().replace(',', '.');
                    double quantidade;
                    try {
                        quantidade = Double.parseDouble(valorTexto);
                    } catch (Exception e) {
                        Toast.makeText(this, "Digite uma quantidade válida.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (!Double.isFinite(quantidade) || quantidade <= 0) {
                        Toast.makeText(this, "A quantidade precisa ser maior que zero.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    confirmarOperacao(tipo, quantidade);
                })
                .show();
    }

    private void confirmarOperacao(String tipo, double quantidade) {
        if (idCarteira > 0) {
            registrarOperacao(tipo, quantidade, idCarteira);
            return;
        }

        new WalletRepository(this).buscarWallets(new WalletRepository.WalletListCallback() {
            @Override
            public void onSuccess(List<Wallet> wallets) {
                if (isFinishing() || isDestroyed()) return;
                if (wallets == null || wallets.isEmpty()) {
                    Toast.makeText(DetalhesAtivoActivity.this, "Crie uma carteira antes de registrar uma operação.", Toast.LENGTH_LONG).show();
                    return;
                }
                Wallet[] opcoes = wallets.toArray(new Wallet[0]);
                CharSequence[] nomesCarteiras = new CharSequence[opcoes.length];
                for (int i = 0; i < opcoes.length; i++) {
                    nomesCarteiras[i] = opcoes[i].getNome_carteira() + " · " + opcoes[i].getTipo_carteira();
                }
                new AlertDialog.Builder(DetalhesAtivoActivity.this)
                        .setTitle("Escolha a carteira")
                        .setItems(nomesCarteiras, (dialog, which) -> registrarOperacao(tipo, quantidade, opcoes[which].getId_carteira()))
                        .setNegativeButton("Cancelar", null)
                        .show();
            }

            @Override
            public void onError(String message) {
                if (!isFinishing() && !isDestroyed()) Toast.makeText(DetalhesAtivoActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void registrarOperacao(String tipo, double quantidade, int carteira) {
        String nome = nomeAtivo == null || nomeAtivo.trim().isEmpty() ? ticker : nomeAtivo;
        clsAportes aporte = new clsAportes(
                ticker,
                nome,
                categoriaAtivo,
                quantidade,
                quantidade * precoAtual,
                tipo,
                "Único",
                carteira
        );

        btnComprarAtivo.setEnabled(false);
        btnVenderAtivo.setEnabled(false);
        new AporteRepository(this).registrarAporte(aporte, new AporteRepository.RegistroCallback() {
            @Override
            public void onSucesso(String mensagem) {
                btnComprarAtivo.setEnabled(true);
                btnVenderAtivo.setEnabled(true);
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(DetalhesAtivoActivity.this, mensagem + " (operação simulada)", Toast.LENGTH_LONG).show();
                setResult(Activity.RESULT_OK, new Intent().putExtra(EXTRA_ID_CARTEIRA, carteira));
                // Recarrega a cotação; posição/preço médio só são recalculados quando a API da carteira fornece esses campos.
                carregarCotacao();
            }

            @Override
            public void onErro(String mensagem) {
                btnComprarAtivo.setEnabled(true);
                btnVenderAtivo.setEnabled(true);
                if (!isFinishing() && !isDestroyed()) Toast.makeText(DetalhesAtivoActivity.this, mensagem, Toast.LENGTH_LONG).show();
            }
        });
    }

    private String formatarHorario(String valor) {
        if (valor == null || valor.trim().isEmpty()) return "não informado pela API";
        try {
            return OffsetDateTime.parse(valor)
                    .atZoneSameInstant(ZoneId.of("America/Sao_Paulo"))
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", ptBR));
        } catch (Exception ignored) {
            return valor;
        }
    }

    private String formatarQuantidade(double quantidade) {
        return String.format(ptBR, quantidade == Math.rint(quantidade) ? "%.0f" : "%.4f", quantidade);
    }
}
