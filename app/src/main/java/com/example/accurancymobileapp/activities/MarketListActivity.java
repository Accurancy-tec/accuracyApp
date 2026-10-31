package com.example.accurancymobileapp.activities;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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
import com.example.accurancymobileapp.network.repository.AcaoRepository;
import com.example.accurancymobileapp.network.repository.FIIsRepository;
import com.example.accurancymobileapp.network.repository.MarketListRepository;
import com.example.accurancymobileapp.response.AcaoResponse;

import java.util.ArrayList;

public class MarketListActivity extends AppCompatActivity {
    private MarketType marketType;
    private static final String TAG = "MarketListActivity";
    public static final String EXTRA_MARKET_TYPE = "TAG";
    private MarketListRepository marketListRepository;
    private static final int LIMITE_POR_PAGINA = 20;

    private int paginaAtual = 1;
    private int totalPaginas = 1;
    private boolean carregando;
    private boolean temProximaPagina;

    private AcoesAdapter adapter;
    private ImageButton btnVoltar;

    private Button btnPaginaAnterior;
    private Button btnProximaPagina;
    private TextView tvPaginaAnterior;
    private TextView tvPaginaAtual;
    private TextView tvProximaPagina;
    private TextView tvTitulo;
    private View containerBusca;
    private RecyclerView rvFiis;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_acoes_nacionais);

        marketType = MarketType.fromTag(
                getIntent().getStringExtra(EXTRA_MARKET_TYPE)
        );

        if (marketType == null) {
            Toast.makeText(this, "Tipo de mercado inválido.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();

        marketListRepository = new MarketListRepository(this);

        if (containerBusca != null) {
            containerBusca.setVisibility(View.GONE);
        }

        rvFiis.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AcoesAdapter(new ArrayList<>(), this::abrirDetalhes);
        rvFiis.setAdapter(adapter);



        btnVoltar.setOnClickListener(v -> finish());

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

        atualizarControles();
        carregarPagina(1);
    }

    private void bindViews(){
        btnVoltar = findViewById(R.id.btnVoltarAtivosNacionais);
        tvTitulo = findViewById(R.id.tvTituloListaAtivos);
        containerBusca = findViewById(R.id.containerBuscaTitulo);

        btnPaginaAnterior = findViewById(R.id.btnPaginaAnterior);
        btnProximaPagina = findViewById(R.id.btnProximaPagina);
        tvPaginaAnterior = findViewById(R.id.tvPaginaAnterior);
        tvPaginaAtual = findViewById(R.id.tvPaginaAtual);
        tvProximaPagina = findViewById(R.id.tvProximaPagina);
        rvFiis = findViewById(R.id.rvTitulosAcoesNacionais);

        tvTitulo.setText(marketType.getTitle());
    }
    private void abrirDetalhes(Acao fii) {
        Intent intent = new Intent(this, DetalhesAtivoActivity.class);
        intent.putExtra(DetalhesAtivoActivity.EXTRA_TICKER, fii.getStock());
        intent.putExtra(DetalhesAtivoActivity.EXTRA_NOME, fii.getName());
        intent.putExtra(
                DetalhesAtivoActivity.EXTRA_CATEGORIA,
                marketType.getDetailCategory()
        );
        intent.putExtra("logo_ativo", fii.getLogo());
        startActivity(intent);
    }

    private void atualizarControles() {
        if (btnPaginaAnterior == null || btnProximaPagina == null) {
            return;
        }

        btnPaginaAnterior.setEnabled(!carregando && paginaAtual > 1);
        btnProximaPagina.setEnabled(!carregando && temProximaPagina);

        tvPaginaAtual.setText(String.valueOf(paginaAtual));

        if (tvPaginaAnterior != null) {
            tvPaginaAnterior.setText(
                    paginaAtual > 1 ? String.valueOf(paginaAtual - 1) : ""
            );
        }

        if (tvProximaPagina != null) {
            tvProximaPagina.setText(
                    temProximaPagina ? String.valueOf(paginaAtual + 1) : ""
            );
        }
    }

    private void carregarPagina(int pagina) {

        if (carregando) {
            return;
        }

        carregando = true;
        atualizarControles();

        marketListRepository.listarAtivos(
                marketType,
                pagina,
                LIMITE_POR_PAGINA,
                new MarketListRepository.MarketListCallback() {

                    @Override
                    public void onSuccess(AcaoResponse resultado) {

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
                    }

                    @Override
                    public void onError(String mensagem) {

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
}
