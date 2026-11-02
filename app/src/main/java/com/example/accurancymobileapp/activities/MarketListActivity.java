package com.example.accurancymobileapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.adapter.AcoesAdapter;
import com.example.accurancymobileapp.model.Acao;
import com.example.accurancymobileapp.model.MarketType;
import com.example.accurancymobileapp.network.repository.MarketListRepository;
import com.example.accurancymobileapp.response.AcaoResponse;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;

public class MarketListActivity extends AppCompatActivity {

    private static final String TAG = "MarketListActivity";
    public static final String EXTRA_MARKET_TYPE = "TAG";

    private static final int LIMITE_POR_PAGINA = 20;
    private static final long DEBOUNCE_DELAY_MS = 450L;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private MarketType marketType;
    private MarketListRepository marketListRepository;
    private AcoesAdapter adapter;

    private int paginaAtual = 1;
    private int totalPaginas = 1;
    private int requestVersion = 0;

    private boolean carregando;
    private boolean temProximaPagina;

    private String searchTerm;
    private String segmentType;
    private String sortBy;
    private String sortOrder = "desc";

    private Runnable pesquisaPendente;

    private ImageButton btnVoltar;
    private ImageButton btnFiltros;
    private Button btnPaginaAnterior;
    private Button btnProximaPagina;

    private TextView tvPaginaAnterior;
    private TextView tvPaginaAtual;
    private TextView tvProximaPagina;
    private TextView tvTitulo;

    private View containerBusca;
    private EditText edtBuscarAtivo;
    private RecyclerView rvMarket;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_market_list);

        marketType = MarketType.fromTag(
                getIntent().getStringExtra(EXTRA_MARKET_TYPE)
        );

        if (marketType == null) {
            Toast.makeText(
                    this,
                    "Tipo de mercado inválido.",
                    Toast.LENGTH_SHORT
            ).show();
            finish();
            return;
        }

        inicializarEstadoInicial();
        bindViews();

        marketListRepository = new MarketListRepository(this);

        containerBusca.setVisibility(View.VISIBLE);

        rvMarket.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AcoesAdapter(new ArrayList<>(), this::abrirDetalhes);
        rvMarket.setAdapter(adapter);

        configurarBotoes();
        configurarPesquisa();

        atualizarControles();
        carregarPagina(1);
        animarPaginaAtual();
    }

    private void inicializarEstadoInicial() {
        searchTerm = null;
        segmentType = null;
        sortOrder = "desc";
        sortBy = marketType == MarketType.FIIS
                ? "totalInvestors"
                : "volume";
    }

    private void bindViews() {
        btnVoltar = findViewById(R.id.btnVoltarAtivosNacionais);
        btnFiltros = findViewById(R.id.btnFiltrosAtivos);

        tvTitulo = findViewById(R.id.tvTituloListaAtivos);
        containerBusca = findViewById(R.id.containerBuscaTitulo);
        edtBuscarAtivo = findViewById(R.id.edtBuscarTitulo);

        btnPaginaAnterior = findViewById(R.id.btnPaginaAnterior);
        btnProximaPagina = findViewById(R.id.btnProximaPagina);

        tvPaginaAnterior = findViewById(R.id.tvPaginaAnterior);
        tvPaginaAtual = findViewById(R.id.tvPaginaAtual);
        tvProximaPagina = findViewById(R.id.tvProximaPagina);

        rvMarket = findViewById(R.id.rvTitulosAcoesNacionais);

        tvTitulo.setText(marketType.getTitle());
    }

    private void configurarBotoes() {
        btnVoltar.setOnClickListener(v -> finish());
        btnFiltros.setOnClickListener(v -> abrirPainelFiltros());

        btnPaginaAnterior.setOnClickListener(v -> {
            if (!carregando && paginaAtual > 1) {
                carregarPagina(paginaAtual - 1);
            }
        });

        btnProximaPagina.setOnClickListener(v -> {
            if (!carregando && temProximaPagina) {
                carregarPagina(paginaAtual + 1);
            }
        });
    }

    private void configurarPesquisa() {
        edtBuscarAtivo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after
            ) {
                // No action is needed before the text changes.
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count
            ) {
                String texto = s == null ? "" : s.toString().trim();
                searchTerm = texto.isEmpty() ? null : texto;
                agendarPesquisa();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // The search is handled in onTextChanged().
            }
        });

        edtBuscarAtivo.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                executarPesquisaImediatamente();
                return true;
            }
            return false;
        });
    }

    private void agendarPesquisa() {
        cancelarPesquisaPendente();

        requestVersion++;
        paginaAtual = 1;
        temProximaPagina = false;
        carregando = true;
        atualizarControles();

        pesquisaPendente = () -> {
            pesquisaPendente = null;
            carregarPagina(1);
        };

        handler.postDelayed(pesquisaPendente, DEBOUNCE_DELAY_MS);
    }

    private void executarPesquisaImediatamente() {
        cancelarPesquisaPendente();
        paginaAtual = 1;
        carregarPagina(1);
    }

    private void cancelarPesquisaPendente() {
        if (pesquisaPendente != null) {
            handler.removeCallbacks(pesquisaPendente);
            pesquisaPendente = null;
        }
    }

    private void abrirDetalhes(Acao ativo) {
        Intent intent = new Intent(this, DetalhesAtivoActivity.class);
        intent.putExtra(DetalhesAtivoActivity.EXTRA_TICKER, ativo.getStock());
        intent.putExtra(DetalhesAtivoActivity.EXTRA_NOME, ativo.getName());
        intent.putExtra(
                DetalhesAtivoActivity.EXTRA_CATEGORIA,
                marketType.getDetailCategory()
        );
        intent.putExtra("logo_ativo", ativo.getLogo());
        startActivity(intent);
    }

    private void carregarPagina(int pagina) {
        cancelarPesquisaPendente();

        final int currentRequest = ++requestVersion;
        final int requestedPage = Math.max(1, pagina);

        carregando = true;
        atualizarControles();

        marketListRepository.listarAtivos(
                marketType,
                requestedPage,
                LIMITE_POR_PAGINA,
                searchTerm,
                segmentType,
                sortBy,
                sortOrder,
                new MarketListRepository.MarketListCallback() {
                    @Override
                    public void onSuccess(AcaoResponse resultado) {
                        if (currentRequest != requestVersion) {
                            return;
                        }

                        carregando = false;

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (resultado == null
                                || resultado.getPaginacao() == null
                                || resultado.getAcoes() == null) {
                            Toast.makeText(
                                    MarketListActivity.this,
                                    "A API retornou dados incompletos.",
                                    Toast.LENGTH_LONG
                            ).show();
                            temProximaPagina = false;
                            atualizarControles();
                            return;
                        }

                        AcaoResponse.Paginacao paginacao = resultado.getPaginacao();

                        paginaAtual = Math.max(1, paginacao.getPaginaAtual());
                        totalPaginas = Math.max(
                                paginaAtual,
                                paginacao.getTotalPaginas()
                        );
                        temProximaPagina = paginacao.isTemProximaPagina()
                                || paginaAtual < totalPaginas;

                        adapter.atualizarLista(resultado.getAcoes());
                        atualizarControles();
                        animarPaginaAtual();
                    }

                    @Override
                    public void onError(String mensagem) {
                        if (currentRequest != requestVersion) {
                            return;
                        }

                        carregando = false;

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        Log.e(TAG, mensagem);
                        Toast.makeText(
                                MarketListActivity.this,
                                mensagem,
                                Toast.LENGTH_LONG
                        ).show();
                        atualizarControles();
                    }
                }
        );
    }

    private void abrirPainelFiltros() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View content = getLayoutInflater().inflate(
                R.layout.dialog_market_filters,
                null,
                false
        );

        TextView tvTituloFiltros = content.findViewById(R.id.tvTituloFiltros);
        TextView tvDescricaoSegmento = content.findViewById(R.id.tvDescricaoSegmento);
        RadioGroup rgSegmentType = content.findViewById(R.id.rgSegmentType);
        RadioGroup rgSortBy = content.findViewById(R.id.rgSortBy);
        RadioGroup rgSortOrder = content.findViewById(R.id.rgSortOrder);

        Button btnLimpar = content.findViewById(R.id.btnLimparFiltros);
        Button btnAplicar = content.findViewById(R.id.btnAplicarFiltros);

        tvTituloFiltros.setText("Filtros e ordenação");

        boolean isFii = marketType == MarketType.FIIS;
        rgSegmentType.setVisibility(isFii ? View.VISIBLE : View.GONE);
        tvDescricaoSegmento.setVisibility(isFii ? View.VISIBLE : View.GONE);

        configurarOpcoesDeOrdenacao(content, isFii);
        selecionarSegmentoAtual(rgSegmentType);
        selecionarOrdenacaoAtual(rgSortBy, isFii);
        rgSortOrder.check(
                "asc".equals(sortOrder)
                        ? R.id.rbSortOrderAsc
                        : R.id.rbSortOrderDesc
        );

        btnLimpar.setOnClickListener(v -> {
            segmentType = null;
            sortBy = isFii ? "totalInvestors" : "volume";
            sortOrder = "desc";
            dialog.dismiss();
            paginaAtual = 1;
            temProximaPagina = false;
            carregarPagina(1);
        });

        btnAplicar.setOnClickListener(v -> {
            segmentType = isFii
                    ? obterSegmentoSelecionado(rgSegmentType)
                    : null;
            sortBy = obterOrdenacaoSelecionada(rgSortBy, isFii);
            sortOrder = rgSortOrder.getCheckedRadioButtonId() == R.id.rbSortOrderAsc
                    ? "asc"
                    : "desc";

            dialog.dismiss();
            paginaAtual = 1;
            temProximaPagina = false;
            carregarPagina(1);
        });

        dialog.setContentView(content);
        dialog.setOnShowListener(ignored -> {
            View bottomSheet = dialog.findViewById(
                    com.google.android.material.R.id.design_bottom_sheet
            );
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        dialog.show();
    }

    private void configurarOpcoesDeOrdenacao(View content, boolean isFii) {
        int[] fiiOnlyIds = {
                R.id.rbSortInvestors,
                R.id.rbSortDividendYield,
                R.id.rbSortPvp
        };
        int[] stockOnlyIds = {
                R.id.rbSortChange,
                R.id.rbSortVolume,
                R.id.rbSortMarketCap
        };

        for (int id : fiiOnlyIds) {
            content.findViewById(id).setVisibility(isFii ? View.VISIBLE : View.GONE);
        }
        for (int id : stockOnlyIds) {
            content.findViewById(id).setVisibility(isFii ? View.GONE : View.VISIBLE);
        }
    }

    private void selecionarSegmentoAtual(RadioGroup group) {
        int id;
        if (segmentType == null || segmentType.isEmpty()) {
            id = R.id.rbSegmentAll;
        } else if ("papel".equals(segmentType)) {
            id = R.id.rbSegmentPapel;
        } else if ("tijolo".equals(segmentType)) {
            id = R.id.rbSegmentTijolo;
        } else if ("hibrido".equals(segmentType)) {
            id = R.id.rbSegmentHibrido;
        } else if ("fof".equals(segmentType)) {
            id = R.id.rbSegmentFof;
        } else {
            id = R.id.rbSegmentAll;
        }
        group.check(id);
    }

    private String obterSegmentoSelecionado(RadioGroup group) {
        int selectedId = group.getCheckedRadioButtonId();
        if (selectedId == R.id.rbSegmentPapel) return "papel";
        if (selectedId == R.id.rbSegmentTijolo) return "tijolo";
        if (selectedId == R.id.rbSegmentHibrido) return "hibrido";
        if (selectedId == R.id.rbSegmentFof) return "fof";
        return null;
    }

    private void selecionarOrdenacaoAtual(RadioGroup group, boolean isFii) {
        int id;

        if (isFii) {
            switch (sortBy) {
                case "dividendYield12m":
                    id = R.id.rbSortDividendYield;
                    break;
                case "price":
                    id = R.id.rbSortPrice;
                    break;
                case "priceToNav":
                    id = R.id.rbSortPvp;
                    break;
                case "name":
                case "symbol":
                    id = R.id.rbSortName;
                    break;
                case "totalInvestors":
                default:
                    id = R.id.rbSortInvestors;
                    break;
            }
        } else {
            switch (sortBy) {
                case "name":
                    id = R.id.rbSortName;
                    break;
                case "close":
                    id = R.id.rbSortPrice;
                    break;
                case "change":
                case "change_abs":
                    id = R.id.rbSortChange;
                    break;
                case "market_cap_basic":
                    id = R.id.rbSortMarketCap;
                    break;
                case "volume":
                default:
                    id = R.id.rbSortVolume;
                    break;
            }
        }

        group.check(id);
    }

    private String obterOrdenacaoSelecionada(RadioGroup group, boolean isFii) {
        int selectedId = group.getCheckedRadioButtonId();

        if (selectedId == R.id.rbSortName) return "name";
        if (selectedId == R.id.rbSortPrice) return isFii ? "price" : "close";

        if (isFii) {
            if (selectedId == R.id.rbSortDividendYield) return "dividendYield12m";
            if (selectedId == R.id.rbSortPvp) return "priceToNav";
            return "totalInvestors";
        }

        if (selectedId == R.id.rbSortChange) return "change";
        if (selectedId == R.id.rbSortMarketCap) return "market_cap_basic";
        return "volume";
    }

    private void atualizarControles() {
        if (btnPaginaAnterior == null || btnProximaPagina == null) {
            return;
        }

        btnPaginaAnterior.setEnabled(!carregando && paginaAtual > 1);
        btnProximaPagina.setEnabled(!carregando && temProximaPagina);

        tvPaginaAtual.setText(String.valueOf(paginaAtual));
        tvPaginaAnterior.setText(
                paginaAtual > 1 ? String.valueOf(paginaAtual - 1) : ""
        );
        tvProximaPagina.setText(
                temProximaPagina ? String.valueOf(paginaAtual + 1) : ""
        );
    }

    @Override
    protected void onDestroy() {
        cancelarPesquisaPendente();
        requestVersion++;
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
    private void animarPaginaAtual() {

        tvPaginaAtual.animate().cancel();

        tvPaginaAtual.setScaleX(0.88f);
        tvPaginaAtual.setScaleY(0.88f);

        tvPaginaAtual.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(170)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }
}
