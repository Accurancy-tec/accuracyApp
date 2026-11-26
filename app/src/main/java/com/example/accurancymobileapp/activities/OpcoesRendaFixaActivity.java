package com.example.accurancymobileapp.activities;

import com.example.accurancymobileapp.utils.PuxarParaAtualizar;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.view.View;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.fragments.FiltrosRendaFixaBottomSheet;
import com.example.accurancymobileapp.fragments.InvestirRendaFixaBottomSheet;
import com.example.accurancymobileapp.model.FiltroRendaFixa;
import com.example.accurancymobileapp.utils.AnimacaoUtils;
import com.example.accurancymobileapp.adapter.EsqueletoAdapter;
import com.example.accurancymobileapp.adapter.TituloRendaFixaAdapter;
import com.example.accurancymobileapp.model.FavoritoAtivo;
import com.example.accurancymobileapp.model.TituloRendaFixa;
import com.example.accurancymobileapp.network.repository.FavoritoRepository;
import android.widget.Toast;
import com.example.accurancymobileapp.network.repository.RendaFixaRepository;
import com.example.accurancymobileapp.utils.Aviso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OpcoesRendaFixaActivity extends AppCompatActivity
        implements FiltrosRendaFixaBottomSheet.Listener, InvestirRendaFixaBottomSheet.AoFecharListener {

    /** Optional Intent extra: which group of titles to list (default: all). */
    public static final String EXTRA_FILTRO = "filtro_titulos";
    public static final String FILTRO_BANCARIOS = "BANCARIOS";
    public static final String FILTRO_TESOURO = "TESOURO";
    /** Optional Intent extra: id of a title whose application card opens as soon as the list loads. */
    public static final String EXTRA_ABRIR_TITULO_ID = "abrir_titulo_id";
    /** Optional Intent extra: id of a title to scroll to and mark in the list (used by the general search). */
    public static final String EXTRA_DESTACAR_TITULO_ID = "destacar_titulo_id";

    private final List<TituloRendaFixa> todosTitulos = new ArrayList<>();
    private String filtroGrupo;
    private EditText edtBuscarTitulo;
    private FiltroRendaFixa filtroAtual = new FiltroRendaFixa();
    private TextView tvBadgeFiltros;

    ImageButton btnVoltarRendaFixa;
    RecyclerView rvTitulosRendaFixa;
    ProgressBar progressRendaFixa;
    View estadoVazio;
    TextView tvEstadoVazioTitulo, tvEstadoVazioMensagem, btnTentarNovamente;
    RendaFixaRepository repositoryRF;
    private FavoritoRepository favoritoRepository;
    private int tituloParaAbrir;
    private int tituloParaDestacar;
    private boolean carregandoTitulos = false;
    TituloRendaFixaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opcoes_renda_fixa);

        btnVoltarRendaFixa = findViewById(R.id.btnVoltarRendaFixa);
        rvTitulosRendaFixa = findViewById(R.id.rvTitulosRendaFixa);
        progressRendaFixa = findViewById(R.id.progressRendaFixa);
        puxar = new PuxarParaAtualizar(findViewById(R.id.swipeRendaFixa), this::carregarTitulosRendaFixa);
        estadoVazio = findViewById(R.id.estadoVazioRendaFixa);
        tvEstadoVazioTitulo = findViewById(R.id.tvEstadoVazioTituloRf);
        tvEstadoVazioMensagem = findViewById(R.id.tvEstadoVazioMensagemRf);
        btnTentarNovamente = findViewById(R.id.btnTentarNovamenteRf);
        btnTentarNovamente.setOnClickListener(v -> carregarTitulosRendaFixa());
        repositoryRF = new RendaFixaRepository(this);
        favoritoRepository = new FavoritoRepository(this);
        tituloParaAbrir = getIntent().getIntExtra(EXTRA_ABRIR_TITULO_ID, 0);
        tituloParaDestacar = getIntent().getIntExtra(EXTRA_DESTACAR_TITULO_ID, 0);

        rvTitulosRendaFixa.setLayoutManager(new LinearLayoutManager(this));
        btnVoltarRendaFixa.setOnClickListener(v -> finish());

        adapter = new TituloRendaFixaAdapter(this::abrirAplicacao);
        adapter.habilitarFavoritos(this::alternarFavorito);
        rvTitulosRendaFixa.setAdapter(adapter);

        filtroGrupo = getIntent().getStringExtra(EXTRA_FILTRO);
        if (FILTRO_TESOURO.equals(filtroGrupo)) {
            ((TextView) findViewById(R.id.tvTituloRendaFixa)).setText("Tesouro Direto");
        }

        tvBadgeFiltros = findViewById(R.id.tvBadgeFiltrosRf);
        ImageButton btnFiltros = findViewById(R.id.btnFiltrosRendaFixa);
        btnFiltros.setOnClickListener(v -> abrirFiltros());
        AnimacaoUtils.efeitoPressionar(btnFiltros);

        edtBuscarTitulo = findViewById(R.id.edtBuscarTitulo);
        edtBuscarTitulo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action required.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No action required.
            }

            @Override
            public void afterTextChanged(Editable s) {
                aplicarFiltros();
            }
        });

        carregarTitulosRendaFixa();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Hearts may have changed elsewhere (Favoritos screen); the server copy wins.
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

    @Override
    public void aoFecharSheet() {
        if (!isFinishing() && !isDestroyed()) {
            adapter.notifyDataSetChanged();
        }
    }

    private void alternarFavorito(TituloRendaFixa titulo, int posicao) {
        favoritoRepository.alternar(
                titulo.getSimboloFavorito(), titulo.getNomeTitulo(), TituloRendaFixa.CATEGORIA_FAVORITO,
                new FavoritoRepository.AlternarCallback() {
                    @Override
                    public void onResultado(boolean favorito) {
                        // The row already redrew its heart.
                    }

                    @Override
                    public void onError(boolean estadoAnterior, String mensagem) {
                        if (isFinishing() || isDestroyed()) return;
                        adapter.notifyItemChanged(posicao);
                        Aviso.makeText(OpcoesRendaFixaActivity.this, mensagem, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /** Opens the application card of the title requested through EXTRA_ABRIR_TITULO_ID, once. */
    private void abrirTituloPedido() {
        if (tituloParaAbrir == 0) return;
        int procurado = tituloParaAbrir;
        tituloParaAbrir = 0;
        for (TituloRendaFixa titulo : todosTitulos) {
            if (titulo.getIdTitulo() == procurado) {
                abrirAplicacao(titulo);
                return;
            }
        }
        Aviso.makeText(this, "Este título não está mais disponível.", Toast.LENGTH_SHORT).show();
    }

    private void abrirAplicacao(TituloRendaFixa titulo) {
        if (getSupportFragmentManager().findFragmentByTag(InvestirRendaFixaBottomSheet.TAG_FRAGMENT) != null) {
            return;
        }
        InvestirRendaFixaBottomSheet.nova(titulo)
                .show(getSupportFragmentManager(), InvestirRendaFixaBottomSheet.TAG_FRAGMENT);
    }

    private void abrirFiltros() {
        if (getSupportFragmentManager().findFragmentByTag(FiltrosRendaFixaBottomSheet.TAG_FRAGMENT) != null) {
            return;
        }
        // Offer only the types/indexers that exist in the group currently shown.
        List<TituloRendaFixa> doGrupo = new ArrayList<>();
        for (TituloRendaFixa titulo : todosTitulos) {
            if (pertenceAoGrupo(titulo)) {
                doGrupo.add(titulo);
            }
        }
        FiltrosRendaFixaBottomSheet.nova(
                filtroAtual,
                FiltroRendaFixa.tiposDisponiveis(doGrupo),
                FiltroRendaFixa.indexadoresDisponiveis(doGrupo)
        ).show(getSupportFragmentManager(), FiltrosRendaFixaBottomSheet.TAG_FRAGMENT);
    }

    @Override
    public void aoAplicarFiltros(FiltroRendaFixa filtro) {
        filtroAtual = filtro == null ? new FiltroRendaFixa() : filtro;
        int ativos = filtroAtual.quantidadeAtiva();
        tvBadgeFiltros.setText(String.valueOf(ativos));
        tvBadgeFiltros.setVisibility(ativos > 0 ? View.VISIBLE : View.GONE);
        if (ativos > 0) {
            AnimacaoUtils.destacar(tvBadgeFiltros);
        }
        aplicarFiltros();
    }

    private boolean pertenceAoGrupo(TituloRendaFixa titulo) {
        if (FILTRO_TESOURO.equals(filtroGrupo)) return ehTesouro(titulo);
        if (FILTRO_BANCARIOS.equals(filtroGrupo)) return !ehTesouro(titulo);
        return true;
    }

    private static boolean ehTesouro(TituloRendaFixa titulo) {
        String tipo = titulo.getTipoTitulo();
        return tipo != null && tipo.trim().toLowerCase(Locale.ROOT).startsWith("tesouro");
    }

    /** Applies the group (bank titles or Treasury) and the search text over the loaded titles. */
    private void aplicarFiltros() {
        if (carregandoTitulos) return; // the placeholder rows are showing
        String busca = edtBuscarTitulo == null
                ? ""
                : edtBuscarTitulo.getText().toString().trim().toLowerCase(Locale.ROOT);
        List<TituloRendaFixa> doGrupo = new ArrayList<>();
        for (TituloRendaFixa titulo : todosTitulos) {
            if (!pertenceAoGrupo(titulo)) {
                continue;
            }
            if (!busca.isEmpty()) {
                String nome = titulo.getNomeTitulo() == null ? "" : titulo.getNomeTitulo().toLowerCase(Locale.ROOT);
                String tipo = titulo.getTipoTitulo() == null ? "" : titulo.getTipoTitulo().toLowerCase(Locale.ROOT);
                if (!nome.contains(busca) && !tipo.contains(busca)) {
                    continue;
                }
            }
            doGrupo.add(titulo);
        }
        // Filter choices (type, indexer, FGC, IR) and the sort order come last.
        List<TituloRendaFixa> filtrados = filtroAtual.aplicar(doGrupo);
        adapter.atualizarLista(filtrados);

        boolean vazio = filtrados.isEmpty();
        if (vazio && !todosTitulos.isEmpty()) {
            mostrarEstadoVazio("Nenhum título encontrado",
                    filtroAtual.estaAtivo() ? "Nenhum título combina com os filtros escolhidos. Tente limpar os filtros."
                            : "Tente outra busca.", false);
        } else if (vazio) {
            mostrarEstadoVazio("Nenhum título disponível",
                    "Ainda não há títulos de renda fixa cadastrados para esta opção.", true);
        } else {
            estadoVazio.setVisibility(View.GONE);
        }
        rvTitulosRendaFixa.setVisibility(vazio ? View.INVISIBLE : View.VISIBLE);
    }

    private void mostrarEstadoVazio(String titulo, String mensagem, boolean comTentarNovamente) {
        tvEstadoVazioTitulo.setText(titulo);
        tvEstadoVazioMensagem.setText(mensagem);
        btnTentarNovamente.setVisibility(comTentarNovamente ? View.VISIBLE : View.GONE);
        if (estadoVazio.getVisibility() != View.VISIBLE) {
            estadoVazio.setVisibility(View.VISIBLE);
            AnimacaoUtils.entradaEmCascata(tvEstadoVazioTitulo, tvEstadoVazioMensagem);
        }
    }

    private PuxarParaAtualizar puxar;

    /** Scrolls to and marks the title a search pointed at, once. */
    private void destacarTituloPedido() {
        if (tituloParaDestacar == 0) return;
        int id = tituloParaDestacar;
        tituloParaDestacar = 0;
        int posicao = adapter.posicaoDe(id);
        if (posicao < 0) return; // hidden by a filter: nothing to mark
        adapter.destacar(id);
        adapter.notifyItemChanged(posicao);
        RecyclerView.LayoutManager gerente = rvTitulosRendaFixa.getLayoutManager();
        if (gerente instanceof LinearLayoutManager) {
            rvTitulosRendaFixa.post(() -> ((LinearLayoutManager) gerente).scrollToPositionWithOffset(posicao, 0));
        }
    }

    private void carregarTitulosRendaFixa(){
        estadoVazio.setVisibility(View.GONE);
        progressRendaFixa.setVisibility(View.GONE);
        // When the user pulled to refresh, the pull spinner is already on screen.
        if (puxar == null || !puxar.atualizando()) {
            carregandoTitulos = true;
            rvTitulosRendaFixa.setVisibility(View.VISIBLE);
            EsqueletoAdapter.mostrar(rvTitulosRendaFixa, R.layout.item_esqueleto_card, 5);
        } else {
            carregandoTitulos = true;
        }
        repositoryRF.listarTitulosRendaFixa(new RendaFixaRepository.RendaFixaCallback() {
            @Override
            public void onSuccess(List<TituloRendaFixa> titulos) {
                puxar.parar();
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                carregandoTitulos = false;
                todosTitulos.clear();
                if (titulos != null) {
                    todosTitulos.addAll(titulos);
                }
                aplicarFiltros();
                EsqueletoAdapter.ocultar(rvTitulosRendaFixa, adapter);
                destacarTituloPedido();
                abrirTituloPedido();
            }

            @Override
            public void onError(String message) {
                puxar.parar();
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                carregandoTitulos = false;
                todosTitulos.clear();
                adapter.atualizarLista(new ArrayList<>());
                EsqueletoAdapter.ocultar(rvTitulosRendaFixa, adapter);
                rvTitulosRendaFixa.setVisibility(View.INVISIBLE);
                mostrarEstadoVazio("Não foi possível carregar os títulos",
                        message == null || message.trim().isEmpty()
                                ? "Verifique sua conexão e tente novamente." : message, true);
            }
        });
    }
}