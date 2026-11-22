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
import com.example.accurancymobileapp.model.FavoritoAtivo;
import com.example.accurancymobileapp.model.MarketType;
import com.example.accurancymobileapp.network.repository.FavoritoRepository;
import com.example.accurancymobileapp.network.repository.MarketListRepository;
import com.example.accurancymobileapp.response.AcaoResponse;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

public class MarketListActivity extends AppCompatActivity {

    private static final String TAG = "MarketListActivity";
    public static final String EXTRA_MARKET_TYPE = "TAG";

    private static final int LIMITE_POR_PAGINA = 20;
    private static final long DEBOUNCE_DELAY_MS = 450L;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private MarketType marketType;
    private MarketListRepository marketListRepository;
    private AcoesAdapter adapter;
    private FavoritoRepository favoritoRepository;

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
        favoritoRepository = new FavoritoRepository(this);
        adapter.habilitarFavoritos(marketType.getDetailCategory(), this::alternarFavorito);
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
        sortOrder = marketType == MarketType.FUTUROS ? "asc" : "desc";
        sortBy = marketType == MarketType.FIIS
                ? "totalInvestors"
                : marketType == MarketType.FUTUROS ? "expirationDate"
                : marketType.isCatalogoSimples() ? "rank" : "volume";
    }

    private void bindViews() {
        btnVoltar = findViewById(R.id.btnVoltarAtivosNacionais);
        btnFiltros = findViewById(R.id.btnFiltrosAtivos);

        tvTitulo = findViewById(R.id.tvTituloListaAtivos);
        containerBusca = findViewById(R.id.containerBuscaTitulo);
        edtBuscarAtivo = findViewById(R.id.edtBuscarTitulo);
        if (marketType == MarketType.FUTUROS) {
            edtBuscarAtivo.setHint("Buscar contrato (ex.: WIN, DI1, BGI)");
        } else if (marketType == MarketType.CRIPTO) {
            edtBuscarAtivo.setHint("Buscar criptomoeda (ex.: BTC, ETH, SOL)");
        } else if (marketType == MarketType.MOEDAS) {
            edtBuscarAtivo.setHint("Buscar moeda (ex.: USD, EUR, GBP)");
        }

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
        boolean isFuturo = marketType == MarketType.FUTUROS;
        boolean temSegmento = isFii || isFuturo; // crypto has no segment filter
        rgSegmentType.setVisibility(temSegmento ? View.VISIBLE : View.GONE);
        tvDescricaoSegmento.setVisibility(temSegmento ? View.VISIBLE : View.GONE);
        tvDescricaoSegmento.setText(isFuturo ? "Segmento do contrato" : "Tipo de fundo");
        configurarOpcoesDeSegmento(content, isFuturo);

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
            sortBy = isFii ? "totalInvestors" : isFuturo ? "expirationDate"
                    : marketType.isCatalogoSimples() ? "rank" : "volume";
            sortOrder = isFuturo ? "asc" : "desc";
            dialog.dismiss();
            paginaAtual = 1;
            temProximaPagina = false;
            carregarPagina(1);
        });

        btnAplicar.setOnClickListener(v -> {
            segmentType = temSegmento
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

    private void configurarOpcoesDeSegmento(View content, boolean isFuturo) {
        int[] fiiIds = {
                R.id.rbSegmentPapel,
                R.id.rbSegmentTijolo,
                R.id.rbSegmentHibrido,
                R.id.rbSegmentFof
        };
        for (int id : fiiIds) {
            content.findViewById(id).setVisibility(isFuturo ? View.GONE : View.VISIBLE);
        }
        content.findViewById(R.id.rbSegmentFinancial)
                .setVisibility(isFuturo ? View.VISIBLE : View.GONE);
        content.findViewById(R.id.rbSegmentAgro)
                .setVisibility(isFuturo ? View.VISIBLE : View.GONE);
    }

    private void configurarOpcoesDeOrdenacao(View content, boolean isFii) {
        if (marketType.isCatalogoSimples()) {
            // Crypto and currency lists support only provider ranking and alphabetical order.
            int[] ocultarIds = {
                    R.id.rbSortPrice,
                    R.id.rbSortInvestors,
                    R.id.rbSortDividendYield,
                    R.id.rbSortPvp,
                    R.id.rbSortChange,
                    R.id.rbSortMarketCap,
                    R.id.rbSortExpiration,
                    R.id.rbSortSymbol,
                    R.id.rbSortUnderlying
            };
            for (int id : ocultarIds) {
                content.findViewById(id).setVisibility(View.GONE);
            }
            android.widget.RadioButton porNome = content.findViewById(R.id.rbSortName);
            android.widget.RadioButton porRanking = content.findViewById(R.id.rbSortVolume);
            porNome.setVisibility(View.VISIBLE);
            porNome.setText("Código (A-Z)");
            porRanking.setVisibility(View.VISIBLE);
            porRanking.setText("Ranking de mercado");
            return;
        }

        if (marketType == MarketType.FUTUROS) {
            int[] ocultarIds = {
                    R.id.rbSortName,
                    R.id.rbSortPrice,
                    R.id.rbSortInvestors,
                    R.id.rbSortDividendYield,
                    R.id.rbSortPvp,
                    R.id.rbSortChange,
                    R.id.rbSortVolume,
                    R.id.rbSortMarketCap
            };
            for (int id : ocultarIds) {
                content.findViewById(id).setVisibility(View.GONE);
            }
            content.findViewById(R.id.rbSortExpiration).setVisibility(View.VISIBLE);
            content.findViewById(R.id.rbSortSymbol).setVisibility(View.VISIBLE);
            content.findViewById(R.id.rbSortUnderlying).setVisibility(View.VISIBLE);
            return;
        }

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
        } else if ("financial".equals(segmentType)) {
            id = R.id.rbSegmentFinancial;
        } else if ("agribusiness".equals(segmentType)) {
            id = R.id.rbSegmentAgro;
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
        if (selectedId == R.id.rbSegmentFinancial) return "financial";
        if (selectedId == R.id.rbSegmentAgro) return "agribusiness";
        return null;
    }

    private void selecionarOrdenacaoAtual(RadioGroup group, boolean isFii) {
        int id;

        if (marketType.isCatalogoSimples()) {
            group.check("name".equals(sortBy) ? R.id.rbSortName : R.id.rbSortVolume);
            return;
        }

        if (marketType == MarketType.FUTUROS) {
            if ("symbol".equals(sortBy)) {
                id = R.id.rbSortSymbol;
            } else if ("underlyingAsset".equals(sortBy)) {
                id = R.id.rbSortUnderlying;
            } else {
                id = R.id.rbSortExpiration;
            }
            group.check(id);
            return;
        }

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

        if (marketType.isCatalogoSimples()) {
            return selectedId == R.id.rbSortName ? "name" : "rank";
        }

        if (marketType == MarketType.FUTUROS) {
            if (selectedId == R.id.rbSortSymbol) return "symbol";
            if (selectedId == R.id.rbSortUnderlying) return "underlyingAsset";
            return "expirationDate";
        }

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
    protected void onResume() {
        super.onResume();
        // Hearts may have changed on the details screen; the server copy wins.
        favoritoRepository.carregar(new FavoritoRepository.ListaCallback() {
            @Override
            public void onSuccess(List<FavoritoAtivo> favoritos) {
                if (!isFinishing() && !isDestroyed()) {
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(String mensagem) {
                // Keep the hearts already on screen.
            }
        });
    }

    private void alternarFavorito(Acao ativo, int posicao) {
        favoritoRepository.alternar(
                ativo.getStock(), ativo.getName(), marketType.getDetailCategory(),
                new FavoritoRepository.AlternarCallback() {
                    @Override
                    public void onResultado(boolean favorito) {
                        // The row already redrew its heart.
                    }

                    @Override
                    public void onError(boolean estadoAnterior, String mensagem) {
                        if (isFinishing() || isDestroyed()) return;
                        adapter.notifyItemChanged(posicao);
                        Toast.makeText(MarketListActivity.this, mensagem, Toast.LENGTH_SHORT).show();
                    }
                });
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
