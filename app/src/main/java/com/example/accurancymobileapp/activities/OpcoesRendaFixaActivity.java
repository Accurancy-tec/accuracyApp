package com.example.accurancymobileapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.adapter.TituloRendaFixaAdapter;
import com.example.accurancymobileapp.model.TituloRendaFixa;
import com.example.accurancymobileapp.network.repository.RendaFixaRepository;

import java.util.List;

public class OpcoesRendaFixaActivity extends AppCompatActivity {

    ImageButton btnVoltarRendaFixa;
    RecyclerView rvTitulosRendaFixa;
    RendaFixaRepository repositoryRF;
    TituloRendaFixaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opcoes_renda_fixa);

        btnVoltarRendaFixa = findViewById(R.id.btnVoltarRendaFixa);
        rvTitulosRendaFixa = findViewById(R.id.rvTitulosRendaFixa);
        repositoryRF = new RendaFixaRepository(this);

        rvTitulosRendaFixa.setLayoutManager(new LinearLayoutManager(this));
        btnVoltarRendaFixa.setOnClickListener(v -> finish());

        adapter = new TituloRendaFixaAdapter();
        rvTitulosRendaFixa.setAdapter(adapter);

        carregarTitulosRendaFixa();
    }

    private void carregarTitulosRendaFixa(){
        repositoryRF.listarTitulosRendaFixa(new RendaFixaRepository.RendaFixaCallback() {
            @Override
            public void onSuccess(List<TituloRendaFixa> titulos) {
                adapter.atualizarLista(titulos);

                if(titulos.isEmpty()){
                    Toast.makeText(OpcoesRendaFixaActivity.this, "Nenhum titulo de renda fixa disponivel", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(OpcoesRendaFixaActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}