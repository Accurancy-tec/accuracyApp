package com.example.accurancymobileapp.activities;

import com.example.accurancymobileapp.adapter.EsqueletoAdapter;
import com.example.accurancymobileapp.utils.PuxarParaAtualizar;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.adapter.FavoritosAdapter;
import com.example.accurancymobileapp.model.FavoritoAtivo;
import com.example.accurancymobileapp.network.repository.FavoritoRepository;
import com.example.accurancymobileapp.utils.AnimacaoUtils;
import com.example.accurancymobileapp.utils.Aviso;
import com.example.accurancymobileapp.utils.FavoritosCache;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

/** The user's favorite assets, with a category filter, tap to open and heart to remove. */
public class FavoritosActivity extends AppCompatActivity {

    private static final String[] ROTULOS = {"Todos", "Ações", "FIIs", "Cripto", "Futuros", "Moedas", "Renda fixa"};
    // Category stored by the API for each chip (null = all).
    private static final String[] CATEGORIAS = {null, "Ações", "FIIs", "Cripto", "Futuros", "Internacional", "Renda Fixa"};

    private FavoritoRepository repositorio;
    private FavoritosAdapter adapter;
    private final List<FavoritoAtivo> todos = new ArrayList<>();
    private String categoriaFiltro;

    private ChipGroup chips;
    private RecyclerView lista;
    private ProgressBar progresso;
    private PuxarParaAtualizar puxar;
    private LinearLayout estadoVazio;
    private TextView tvTituloVazio;
    private TextView tvMensagemVazio;
    private TextView btnTentarNovamente;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favoritos);

        repositorio = new FavoritoRepository(this);

        ImageButton voltar = findViewById(R.id.btnVoltarFavoritos);
        voltar.setOnClickListener(v -> finish());
        chips = findViewById(R.id.chipsFavoritos);
        lista = findViewById(R.id.rvFavoritos);
        puxar = new PuxarParaAtualizar(findViewById(R.id.swipeFavoritos), this::carregar);
        progresso = findViewById(R.id.progressFavoritos);
        estadoVazio = findViewById(R.id.estadoVazioFavoritos);
        tvTituloVazio = findViewById(R.id.tvTituloVazioFavoritos);
        tvMensagemVazio = findViewById(R.id.tvMensagemVazioFavoritos);
        btnTentarNovamente = findViewById(R.id.btnTentarNovamenteFavoritos);
        btnTentarNovamente.setOnClickListener(v -> carregar());
        AnimacaoUtils.efeitoPressionar(btnTentarNovamente);

        adapter = new FavoritosAdapter(new FavoritosAdapter.Listener() {
            @Override
            public void aoAbrir(FavoritoAtivo favorito) {
                abrirDetalhes(favorito);
            }

            @Override
            public void aoRemover(FavoritoAtivo favorito) {
                remover(favorito);
            }
        });
        lista.setLayoutManager(new LinearLayoutManager(this));
        lista.setAdapter(adapter);

        criarChips();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregar();
    }

    private void criarChips() {
        int[][] estados = {{android.R.attr.state_checked}, {}};
        for (int i = 0; i < ROTULOS.length; i++) {
            Chip chip = new Chip(this);
            chip.setId(View.generateViewId());
            chip.setText(ROTULOS[i]);
            chip.setTag(CATEGORIAS[i]);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(false);
            chip.setTextSize(12f);
            chip.setEnsureMinTouchTargetSize(false);
            chip.setChipBackgroundColor(new ColorStateList(estados, new int[]{cor(R.color.primary_blue), cor(R.color.superficie_campo)}));
            chip.setChipStrokeColor(new ColorStateList(estados, new int[]{cor(R.color.link), cor(R.color.borda)}));
            chip.setChipStrokeWidth(getResources().getDisplayMetrics().density);
            chip.setTextColor(new ColorStateList(estados, new int[]{Color.WHITE, cor(R.color.text_primary)}));
            chips.addView(chip);
        }
        ((Chip) chips.getChildAt(0)).setChecked(true);
        chips.setOnCheckedStateChangeListener((grupo, ids) -> {
            if (ids.isEmpty()) return;
            Chip escolhido = grupo.findViewById(ids.get(0));
            Object tag = escolhido == null ? null : escolhido.getTag();
            categoriaFiltro = tag == null ? null : tag.toString();
            mostrarLista();
        });
    }

    private void carregar() {
        if (todos.isEmpty()) {
            mostrarCarregando();
        }
        repositorio.carregar(new FavoritoRepository.ListaCallback() {
            @Override
            public void onSuccess(List<FavoritoAtivo> favoritos) {
                puxar.parar();
                if (isFinishing() || isDestroyed()) return;
                todos.clear();
                todos.addAll(favoritos);
                mostrarLista();
            }

            @Override
            public void onError(String mensagem) {
                puxar.parar();
                if (isFinishing() || isDestroyed()) return;
                if (todos.isEmpty()) {
                    mostrarEstado("Não foi possível carregar", mensagem, true);
                } else {
                    Aviso.makeText(FavoritosActivity.this, mensagem, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void mostrarCarregando() {
        progresso.setVisibility(View.GONE);
        estadoVazio.setVisibility(View.GONE);
        lista.setVisibility(View.VISIBLE);
        EsqueletoAdapter.mostrar(lista, R.layout.item_esqueleto_linha, 6);
    }

    private void mostrarLista() {
        progresso.setVisibility(View.GONE);
        List<FavoritoAtivo> filtrados = new ArrayList<>();
        for (FavoritoAtivo item : todos) {
            if (categoriaFiltro == null
                    || categoriaFiltro.equals(FavoritosCache.categoriaCanonica(item.getCategoria()))) {
                filtrados.add(item);
            }
        }
        adapter.atualizar(filtrados);
        EsqueletoAdapter.ocultar(lista, adapter);
        if (filtrados.isEmpty()) {
            if (todos.isEmpty()) {
                mostrarEstado("Nenhum favorito ainda",
                        "Toque no coração de um ativo para guardá-lo aqui.", false);
            } else {
                mostrarEstado("Nada nesta categoria",
                        "Você ainda não favoritou ativos desta categoria.", false);
            }
            return;
        }
        estadoVazio.setVisibility(View.GONE);
        lista.setVisibility(View.VISIBLE);
    }

    private void mostrarEstado(String titulo, String mensagem, boolean comTentarNovamente) {
        EsqueletoAdapter.ocultar(lista, adapter);
        progresso.setVisibility(View.GONE);
        lista.setVisibility(View.GONE);
        tvTituloVazio.setText(titulo);
        tvMensagemVazio.setText(mensagem);
        btnTentarNovamente.setVisibility(comTentarNovamente ? View.VISIBLE : View.GONE);
        estadoVazio.setVisibility(View.VISIBLE);
        AnimacaoUtils.entradaEmCascata(estadoVazio);
    }

    private void abrirDetalhes(FavoritoAtivo favorito) {
        if ("Renda Fixa".equals(FavoritosCache.categoriaCanonica(favorito.getCategoria()))) {
            abrirTituloRendaFixa(favorito);
            return;
        }
        Intent intent = new Intent(this, DetalhesAtivoActivity.class);
        intent.putExtra(DetalhesAtivoActivity.EXTRA_TICKER, favorito.getSimbolo());
        intent.putExtra(DetalhesAtivoActivity.EXTRA_NOME, favorito.getNome());
        intent.putExtra(DetalhesAtivoActivity.EXTRA_CATEGORIA, favorito.getCategoria());
        startActivity(intent);
    }

    /** Fixed income favorites are stored as RF-<id>; open the list with that title's card. */
    private void abrirTituloRendaFixa(FavoritoAtivo favorito) {
        String simbolo = favorito.getSimbolo() == null ? "" : favorito.getSimbolo().trim();
        int id = 0;
        if (simbolo.toUpperCase(java.util.Locale.ROOT).startsWith("RF-")) {
            try {
                id = Integer.parseInt(simbolo.substring(3));
            } catch (NumberFormatException ignored) {
                id = 0;
            }
        }
        Intent intent = new Intent(this, OpcoesRendaFixaActivity.class);
        if (id > 0) {
            intent.putExtra(OpcoesRendaFixaActivity.EXTRA_ABRIR_TITULO_ID, id);
        }
        startActivity(intent);
    }

    private void remover(FavoritoAtivo favorito) {
        repositorio.definir(favorito.getSimbolo(), favorito.getNome(), favorito.getCategoria(), false,
                new FavoritoRepository.AlternarCallback() {
                    @Override
                    public void onResultado(boolean ehFavorito) {
                        if (ehFavorito) return;
                        todos.remove(favorito);
                        adapter.remover(favorito);
                        if (adapter.quantidade() == 0) {
                            mostrarLista();
                        }
                    }

                    @Override
                    public void onError(boolean estadoAnterior, String mensagem) {
                        if (isFinishing() || isDestroyed()) return;
                        Aviso.makeText(FavoritosActivity.this, mensagem, Toast.LENGTH_SHORT).show();
                        carregar();
                    }
                });
    }

    private int cor(@ColorRes int id) {
        return ContextCompat.getColor(this, id);
    }
}
