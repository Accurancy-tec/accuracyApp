package com.example.accurancymobileapp.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
import com.example.accurancymobileapp.network.service.ApiService;
import com.example.accurancymobileapp.response.AcaoResponse;

import java.util.ArrayList;
import java.util.List;

public class AcoesNacionaisActivity extends AppCompatActivity {

    ImageButton btnVoltarAtivosNacionais;
    private static final int LIMITE_POR_PAGINA = 20;
    private int paginaAtual = 1;
    private int totalPaginas = 1;
    private int paginaAntrior = paginaAtual - 1;
    private int proximaPagina = paginaAtual + 1;
    private boolean carregando = false;
    private boolean temProximaPagina = false;
    AcaoRepository acaoRepository;
    AcoesAdapter adapter;
    Button btnPaginaAnterior, btnProximaPagina;
    TextView tvPaginaAnterior, tvPaginaAtual, tvProximaPagina;
    RecyclerView rvTitulosAcoesNacionais;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_acoes_nacionais);

        btnVoltarAtivosNacionais = findViewById(R.id.btnVoltarAtivosNacionais);
        btnPaginaAnterior = findViewById(R.id.btnPaginaAnterior);
        btnProximaPagina = findViewById(R.id.btnProximaPagina);
        tvPaginaAnterior = findViewById(R.id.tvPaginaAnterior);
        tvPaginaAtual = findViewById(R.id.tvPaginaAtual);
        tvProximaPagina = findViewById(R.id.tvProximaPagina);
        rvTitulosAcoesNacionais = findViewById(R.id.rvTitulosAcoesNacionais);

        rvTitulosAcoesNacionais.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AcoesAdapter(new ArrayList<>());
        rvTitulosAcoesNacionais.setAdapter(adapter);

        acaoRepository = new AcaoRepository(this);
        btnVoltarAtivosNacionais.setOnClickListener(v -> finish());

        btnPaginaAnterior.setOnClickListener(v -> {
            if(!carregando && paginaAtual > 1){
                carregarPagina(paginaAtual - 1);
                return;
            }
        });

        btnProximaPagina.setOnClickListener(v -> {
            if(!carregando && paginaAtual < totalPaginas){
                carregarPagina(paginaAtual + 1);
                return;
            }
        });

        atualizarControles();

        carregarPagina(1);

    }

    private void carregarPagina(int pagina) {

        acaoRepository.listarAcoesNacionais(
                pagina,
                LIMITE_POR_PAGINA,
                new AcaoRepository.acaoCallback() {

                    @Override
                    public void onSuccess(AcaoResponse resultado) {

                        AcaoResponse.Paginacao paginacao =
                                resultado.getPaginacao();

                        paginaAtual = paginacao.getPaginaAtual();
                        totalPaginas = paginacao.getTotalPaginas();
                        temProximaPagina = paginacao.isTemProximaPagina();

                        adapter.atualizarLista(resultado.getAcoes());

                        atualizarControles();
                    }

                    @Override
                    public void onError(String message) {
                        Log.e("ACOES_NACIONAIS", message);
                        Toast.makeText(
                                AcoesNacionaisActivity.this,
                                message,
                                Toast.LENGTH_SHORT
                        ).show();

                        atualizarControles();
                    }
                }
        );
    }

    private void atualizarControles(){
        btnPaginaAnterior.setEnabled(!carregando && paginaAtual > 1);

        btnProximaPagina.setEnabled(!carregando && paginaAtual < totalPaginas);

        tvPaginaAtual.setText("" + paginaAtual);

        if(tvPaginaAnterior != null){
            tvPaginaAnterior.setText(paginaAtual > 1 ? String.valueOf(paginaAtual -1) : "");
        }

        if(tvProximaPagina != null){
            tvProximaPagina.setText(paginaAtual < totalPaginas ? String.valueOf(paginaAtual + 1) : "");
        }
    }

}