package com.example.accurancymobileapp.activities;

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
import com.example.accurancymobileapp.network.repository.AcaoRepository;
import com.example.accurancymobileapp.response.AcaoResponse;

import java.util.ArrayList;

public class AcoesNacionaisActivity extends AppCompatActivity {

    private static final String TAG = "ACOES_NACIONAIS";
    private static final int LIMITE_POR_PAGINA = 20;

    private int paginaAtual = 1;
    private int totalPaginas = 1;
    private boolean carregando = false;
    private boolean temProximaPagina = false;

    private AcaoRepository acaoRepository;
    private AcoesAdapter adapter;
    private Button btnPaginaAnterior, btnProximaPagina;
    private TextView tvPaginaAnterior, tvPaginaAtual, tvProximaPagina;
    private RecyclerView rvTitulosAcoesNacionais;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_acoes_nacionais);

        ImageButton btnVoltar = findViewById(R.id.btnVoltarAtivosNacionais);
        btnPaginaAnterior = findViewById(R.id.btnPaginaAnterior);
        btnProximaPagina = findViewById(R.id.btnProximaPagina);
        tvPaginaAnterior = findViewById(R.id.tvPaginaAnterior);
        tvPaginaAtual = findViewById(R.id.tvPaginaAtual);
        tvProximaPagina = findViewById(R.id.tvProximaPagina);
        rvTitulosAcoesNacionais = findViewById(R.id.rvTitulosAcoesNacionais);

        rvTitulosAcoesNacionais.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AcoesAdapter(new ArrayList<>(), this::abrirDetalhes);
        rvTitulosAcoesNacionais.setAdapter(adapter);
        acaoRepository = new AcaoRepository(this);

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

    private void abrirDetalhes(Acao acao) {
        Intent intent = new Intent(this, DetalhesAtivoActivity.class);
        intent.putExtra(DetalhesAtivoActivity.EXTRA_TICKER, acao.getStock());
        intent.putExtra(DetalhesAtivoActivity.EXTRA_NOME, acao.getName());
        intent.putExtra(DetalhesAtivoActivity.EXTRA_CATEGORIA, "Ações");
        intent.putExtra("logo_ativo", acao.getLogo());
        startActivity(intent);
    }

    private void carregarPagina(int pagina) {
        if (carregando) return;
        carregando = true;
        atualizarControles();

        acaoRepository.listarAcoesNacionais(pagina, LIMITE_POR_PAGINA,
                new AcaoRepository.acaoCallback() {
                    @Override
                    public void onSuccess(AcaoResponse resultado) {
                        carregando = false;
                        if (isFinishing() || isDestroyed()) return;

                        AcaoResponse.Paginacao paginacao = resultado.getPaginacao();
                        if (paginacao == null || resultado.getAcoes() == null) {
                            Toast.makeText(AcoesNacionaisActivity.this, "A API retornou dados incompletos.", Toast.LENGTH_LONG).show();
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
                    public void onError(String message) {
                        carregando = false;
                        Log.e(TAG, message);
                        if (!isFinishing() && !isDestroyed()) {
                            Toast.makeText(AcoesNacionaisActivity.this, message, Toast.LENGTH_LONG).show();
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
        if (tvPaginaAnterior != null) tvPaginaAnterior.setText(paginaAtual > 1 ? String.valueOf(paginaAtual - 1) : "");
        if (tvProximaPagina != null) tvProximaPagina.setText(temProximaPagina ? String.valueOf(paginaAtual + 1) : "");
    }
}
