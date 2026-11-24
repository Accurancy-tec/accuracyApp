package com.example.accurancymobileapp.fragments;

import static android.widget.Toast.LENGTH_LONG;

import static androidx.core.content.ContextCompat.getSystemService;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Handler;
import android.os.Looper;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.accurancymobileapp.adapter.BuscaGeralAdapter;
import com.example.accurancymobileapp.network.repository.BuscaGeralRepository;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.activities.DashboardActivity;
import com.example.accurancymobileapp.activities.FavoritosActivity;
import com.example.accurancymobileapp.activities.MarketListActivity;
import com.example.accurancymobileapp.activities.OpcoesRendaFixaActivity;
import com.example.accurancymobileapp.activities.OpcoesRendaVariavelActivity;
import com.example.accurancymobileapp.model.Acao;
import com.example.accurancymobileapp.model.ActiveSpinner;
import com.example.accurancymobileapp.model.TituloRendaFixa;
import com.example.accurancymobileapp.model.Aporte;
import com.example.accurancymobileapp.model.QuoteData;
import com.example.accurancymobileapp.model.QuoteResult;
import com.example.accurancymobileapp.model.Wallet;
import com.example.accurancymobileapp.model.clsAportes;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.network.repository.WalletRepository;
import com.example.accurancymobileapp.network.service.ApiService;
import com.example.accurancymobileapp.utils.AnimacaoUtils;
import com.example.accurancymobileapp.response.ApiResponse;
import com.example.accurancymobileapp.response.QuoteResponse;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AportesFragment extends Fragment {

    public AportesFragment() {
        super(R.layout.fragment_aportes);
    }

    LinearLayout cardBolsaDeValores, cardRendaFixa, cardTesouroDireto;

    private static final long ESPERA_BUSCA_MS = 500L;
    private static final int MINIMO_LETRAS = 2;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable buscaPendente;
    private BuscaGeralRepository buscaRepository;
    private BuscaGeralAdapter buscaAdapter;
    private EditText edtBusca;
    private ImageButton btnLimparBusca;
    private View containerCards, containerResultados;
    private RecyclerView rvBusca;
    private TextView tvBuscaMensagem;
    private String ultimaBusca = "";

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        cardBolsaDeValores = view.findViewById(R.id.cardBolsaValores);
        cardRendaFixa = view.findViewById(R.id.cardRendaFixa);

        cardBolsaDeValores.setOnClickListener(v -> {
            Intent it = new Intent(requireContext(), OpcoesRendaVariavelActivity.class);
            startActivity(it);
        });

        cardRendaFixa.setOnClickListener(v -> {
            Intent it = new Intent(requireContext(), OpcoesRendaFixaActivity.class);
            it.putExtra(OpcoesRendaFixaActivity.EXTRA_FILTRO, OpcoesRendaFixaActivity.FILTRO_BANCARIOS);
            startActivity(it);
        });

        // These two cards existed in the layout but had no click handler.
        cardTesouroDireto = view.findViewById(R.id.cardTesouroDireto);
        cardTesouroDireto.setOnClickListener(v -> {
            Intent it = new Intent(requireContext(), OpcoesRendaFixaActivity.class);
            it.putExtra(OpcoesRendaFixaActivity.EXTRA_FILTRO, OpcoesRendaFixaActivity.FILTRO_TESOURO);
            startActivity(it);
        });

        LinearLayout cardCriptomoedas = view.findViewById(R.id.cardCriptomoedasAportes);
        cardCriptomoedas.setOnClickListener(v -> abrirMercado("CRIPTO"));

        View btnFavoritos = view.findViewById(R.id.btnFavoritosProdutos);
        btnFavoritos.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), FavoritosActivity.class)));
        AnimacaoUtils.efeitoPressionar(btnFavoritos);

        LinearLayout cardFuturos = view.findViewById(R.id.cardFuturosAportes);
        cardFuturos.setOnClickListener(v -> abrirMercado("FUTUROS"));

        LinearLayout cardMoedas = view.findViewById(R.id.cardMoedasAportes);
        cardMoedas.setOnClickListener(v -> abrirMercado("MOEDAS"));

        // Press feedback on every product card plus a staggered entrance when the tab opens.
        LinearLayout[] cards = {cardRendaFixa, cardTesouroDireto, cardBolsaDeValores,
                cardCriptomoedas, cardFuturos, cardMoedas};
        for (LinearLayout card : cards) {
            AnimacaoUtils.efeitoPressionar(card);
        }
        AnimacaoUtils.entradaEmCascata(cards);

        configurarBuscaGeral(view);
    }

    // ------------------------------------------------------------ general search

    private void configurarBuscaGeral(View view) {
        edtBusca = view.findViewById(R.id.edtBuscaGeral);
        btnLimparBusca = view.findViewById(R.id.btnLimparBuscaGeral);
        containerCards = view.findViewById(R.id.containerCardsProdutos);
        containerResultados = view.findViewById(R.id.containerResultadosBusca);
        rvBusca = view.findViewById(R.id.rvBuscaGeral);
        tvBuscaMensagem = view.findViewById(R.id.tvBuscaMensagem);
        buscaRepository = new BuscaGeralRepository(requireContext());

        buscaAdapter = new BuscaGeralAdapter(new BuscaGeralAdapter.Ouvinte() {
            @Override
            public void aoAbrirAcao(BuscaGeralRepository.Secao secao, Acao acao) {
                esconderTeclado();
                Intent it = new Intent(requireContext(), MarketListActivity.class);
                it.putExtra(MarketListActivity.EXTRA_MARKET_TYPE, secao.mercado.getTag());
                it.putExtra(MarketListActivity.EXTRA_BUSCA_INICIAL, acao.getStock());
                it.putExtra(MarketListActivity.EXTRA_DESTACAR, acao.getStock());
                startActivity(it);
            }

            @Override
            public void aoAbrirTitulo(BuscaGeralRepository.Secao secao, TituloRendaFixa titulo) {
                esconderTeclado();
                Intent it = new Intent(requireContext(), OpcoesRendaFixaActivity.class);
                it.putExtra(OpcoesRendaFixaActivity.EXTRA_FILTRO, secao.tesouro
                        ? OpcoesRendaFixaActivity.FILTRO_TESOURO : OpcoesRendaFixaActivity.FILTRO_BANCARIOS);
                it.putExtra(OpcoesRendaFixaActivity.EXTRA_DESTACAR_TITULO_ID, titulo.getIdTitulo());
                startActivity(it);
            }

            @Override
            public void aoVerTodos(BuscaGeralRepository.Secao secao) {
                esconderTeclado();
                String termo = ultimaBusca;
                if (secao.ehRendaFixa()) {
                    Intent it = new Intent(requireContext(), OpcoesRendaFixaActivity.class);
                    it.putExtra(OpcoesRendaFixaActivity.EXTRA_FILTRO, secao.tesouro
                            ? OpcoesRendaFixaActivity.FILTRO_TESOURO : OpcoesRendaFixaActivity.FILTRO_BANCARIOS);
                    startActivity(it);
                } else {
                    Intent it = new Intent(requireContext(), MarketListActivity.class);
                    it.putExtra(MarketListActivity.EXTRA_MARKET_TYPE, secao.mercado.getTag());
                    it.putExtra(MarketListActivity.EXTRA_BUSCA_INICIAL, termo);
                    startActivity(it);
                }
            }
        });
        rvBusca.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvBusca.setAdapter(buscaAdapter);

        edtBusca.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Nothing to do before the text changes.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                aoDigitar(s == null ? "" : s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Handled in onTextChanged().
            }
        });
        edtBusca.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                esconderTeclado();
                String termo = edtBusca.getText().toString().trim();
                if (termo.length() >= MINIMO_LETRAS) buscarAgora(termo);
                return true;
            }
            return false;
        });
        btnLimparBusca.setOnClickListener(v -> edtBusca.setText(""));
    }

    private void aoDigitar(String termo) {
        cancelarBuscaPendente();
        btnLimparBusca.setVisibility(termo.isEmpty() ? View.GONE : View.VISIBLE);

        if (termo.isEmpty()) {
            buscaRepository.cancelar();
            ultimaBusca = "";
            containerResultados.setVisibility(View.GONE);
            containerCards.setVisibility(View.VISIBLE);
            return;
        }

        containerCards.setVisibility(View.GONE);
        containerResultados.setVisibility(View.VISIBLE);

        if (termo.length() < MINIMO_LETRAS) {
            buscaRepository.cancelar();
            buscaAdapter.atualizar(new ArrayList<>(), false);
            mostrarMensagem("Digite pelo menos " + MINIMO_LETRAS + " letras para buscar.");
            return;
        }

        // Placeholder rows right away; the request starts when the typing pauses.
        tvBuscaMensagem.setVisibility(View.GONE);
        buscaAdapter.atualizar(new ArrayList<>(), true);
        buscaPendente = () -> {
            buscaPendente = null;
            buscarAgora(termo);
        };
        handler.postDelayed(buscaPendente, ESPERA_BUSCA_MS);
    }

    private void buscarAgora(String termo) {
        cancelarBuscaPendente();
        ultimaBusca = termo;
        containerCards.setVisibility(View.GONE);
        containerResultados.setVisibility(View.VISIBLE);
        tvBuscaMensagem.setVisibility(View.GONE);
        buscaAdapter.atualizar(new ArrayList<>(), true);

        buscaRepository.buscar(termo, (secoes, carregando, falhas) -> {
            if (!isAdded() || getView() == null) return;
            buscaAdapter.atualizar(secoes, carregando);
            if (carregando) {
                tvBuscaMensagem.setVisibility(View.GONE);
            } else if (secoes.isEmpty()) {
                mostrarMensagem(falhas > 0
                        ? "Não foi possível buscar agora. Verifique a conexão e tente de novo."
                        : "Nada encontrado para “" + termo + "”.\nTente o código (ex.: PETR4, BTC) ou parte do nome.");
            } else if (falhas > 0) {
                mostrarMensagem("Algumas categorias não responderam. Os resultados podem estar incompletos.");
            } else {
                tvBuscaMensagem.setVisibility(View.GONE);
            }
        });
    }

    private void mostrarMensagem(String texto) {
        tvBuscaMensagem.setText(texto);
        tvBuscaMensagem.setVisibility(View.VISIBLE);
    }

    private void cancelarBuscaPendente() {
        if (buscaPendente != null) {
            handler.removeCallbacks(buscaPendente);
            buscaPendente = null;
        }
    }

    private void esconderTeclado() {
        if (edtBusca == null) return;
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(edtBusca.getWindowToken(), 0);
    }

    @Override
    public void onDestroyView() {
        cancelarBuscaPendente();
        if (buscaRepository != null) buscaRepository.cancelar();
        super.onDestroyView();
    }

    private void abrirMercado(String tag) {
        Intent it = new Intent(requireContext(), MarketListActivity.class);
        it.putExtra("TAG", tag);
        startActivity(it);
    }
}
