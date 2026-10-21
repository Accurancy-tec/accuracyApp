package com.example.accurancymobileapp.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.List;

public class AcoesNacionaisActivity extends AppCompatActivity {

    ImageButton btnVoltarAtivosNacionais;
    private static final int LIMITE_POR_PAGINA = 20;
    private int paginaAtual = 1;
    private int paginaAntrior = paginaAtual - 1;
    private int proximaPagina = paginaAtual + 1;
    private boolean carregando = false;
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
        tvPaginaAnterior = findViewById(R.id.btnPaginaAnterior);
        tvPaginaAtual = findViewById(R.id.tvPaginaAtual);
        tvProximaPagina = findViewById(R.id.tvProximaPagina);
        rvTitulosAcoesNacionais = findViewById(R.id.rvTitulosAcoesNacionais);

        rvTitulosAcoesNacionais.setLayoutManager(new LinearLayoutManager(this));

        AcoesAdapter adapter = new AcoesAdapter(new ArrayList<>());
        rvTitulosAcoesNacionais.setAdapter(adapter);

        btnVoltarAtivosNacionais.setOnClickListener(v -> finish());
    }

    private carregarPagina(int pagina){
        AcaoRepository acaoRepository = new AcaoRepository(this);

        if(!carregando && pagina < 1){
            return;
        }

        carregando = true;
        atualizarControles();

        acaoRepository.listarAcoesNacionais(pagina, LIMITE_POR_PAGINA, new AcaoRepository.acaoCallback() {
            @Override
            public void onSuccess(List<Acao> acoes) {
                if(acoes != null){
                    acoes = new ArrayList<>();
                }
            }

            @Override
            public void onError(String message) {

            }
        });
    }

}