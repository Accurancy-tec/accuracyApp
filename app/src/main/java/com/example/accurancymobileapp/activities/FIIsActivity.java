package com.example.accurancymobileapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.adapter.AcoesAdapter;
import com.example.accurancymobileapp.model.Acao;
import com.example.accurancymobileapp.network.repository.AcaoRepository;
import com.example.accurancymobileapp.network.repository.FIIsRepository;
import com.example.accurancymobileapp.response.AcaoResponse;

import java.util.ArrayList;

public class FIIsActivity extends AppCompatActivity {

    private static final String TAG = "FIIS";
    private static final int LIMITE_POR_PAGINA = 20;

    private int paginaAtual = 1;
    private int totalPaginas = 1;
    private boolean carregando = false;
    private boolean temProximaPagina = false;

    private FIIsRepository fiisRepository;
    private AcoesAdapter adapter;
    private Button btnPaginaAnterior;
    private Button btnProximaPagina;
    private TextView tvPaginaAnterior;
    private TextView tvPaginaAtual;
    private TextView tvProximaPagina;
    private RecyclerView rvFiis;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_acoes_nacionais);

        ImageButton btnVoltar = findViewById(R.id.btnVoltarAtivosNacionais);
        TextView tvTitulo = findViewById(R.id.tvTituloListaAtivos);
        btnPaginaAnterior = findViewById(R.id.btnPaginaAnterior);
        btnProximaPagina = findViewById(R.id.btnProximaPagina);
        tvPaginaAnterior = findViewById(R.id.tvPaginaAnterior);
        tvPaginaAtual = findViewById(R.id.tvPaginaAtual);
        tvProximaPagina = findViewById(R.id.tvProximaPagina);
        rvFiis = findViewById(R.id.rvTitulosAcoesNacionais);

        if (tvTitulo != null) tvTitulo.setText("Fundos Imobiliários");

        rvFiis.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AcoesAdapter(new ArrayList<>(), this::abrirDetalhes);
        rvFiis.setAdapter(adapter);
        fiisRepository = new FIIsRepository(this);

        btnVoltar.setOnClickListener(v -> finish());
        btnPaginaAnterior.setOnClickListener(v -> {
            if (!carregando && paginaAtual > 1) carregarPagina(paginaAtual - 1);
        });
        btnProximaPagina.setOnClickListener(v -> {
            if (!carregando && temProximaPagina) carregarPagina(paginaAtual + 1);
        });

        atualizarControles();
        carregarPagina(1);
    }

    private void abrirDetalhes(Acao fii) {
        Intent intent = new Intent(this, DetalhesAtivoActivity.class);
        intent.putExtra(DetalhesAtivoActivity.EXTRA_TICKER, fii.getStock());
        intent.putExtra(DetalhesAtivoActivity.EXTRA_NOME, fii.getName());
        intent.putExtra(DetalhesAtivoActivity.EXTRA_CATEGORIA, "FIIs");
        intent.putExtra("logo_ativo", fii.getLogo());
        startActivity(intent);
    }

    private void carregarPagina(int pagina) {
        if (carregando) return;
        carregando = true;
        atualizarControles();

        fiisRepository.listarFIIs(pagina, LIMITE_POR_PAGINA, new FIIsRepository.FiiCallback() {
            @Override
            public void onSuccess(AcaoResponse resultado) {
                carregando = false;
                if (isFinishing() || isDestroyed()) return;

                AcaoResponse.Paginacao paginacao = resultado.getPaginacao();
                if (paginacao == null || resultado.getAcoes() == null) {
                    Toast.makeText(FIIsActivity.this,
                            "A API retornou dados incompletos dos FIIs.", Toast.LENGTH_LONG).show();
                    atualizarControles();
                    return;
                }

                paginaAtual = Math.max(1, paginacao.getPaginaAtual());
                totalPaginas = Math.max(paginaAtual, paginacao.getTotalPaginas());
                temProximaPagina = paginacao.isTemProximaPagina() || paginaAtual < totalPaginas;
                adapter.atualizarLista(resultado.getAcoes());
                atualizarControles();
            }

            @Override
            public void onError(String mensagem) {
                carregando = false;
                Log.e(TAG, mensagem);
                if (!isFinishing() && !isDestroyed()) {
                    Toast.makeText(FIIsActivity.this, mensagem, Toast.LENGTH_LONG).show();
                }
                atualizarControles();
            }
        });
    }

    private void atualizarControles() {
        if (btnPaginaAnterior == null) return;
        btnPaginaAnterior.setEnabled(!carregando && paginaAtual > 1);
        btnProximaPagina.setEnabled(!carregando && temProximaPagina);
        tvPaginaAtual.setText(String.valueOf(paginaAtual));
        if (tvPaginaAnterior != null) {
            tvPaginaAnterior.setText(paginaAtual > 1 ? String.valueOf(paginaAtual - 1) : "");
        }
        if (tvProximaPagina != null) {
            tvProximaPagina.setText(temProximaPagina ? String.valueOf(paginaAtual + 1) : "");
        }
    }
}