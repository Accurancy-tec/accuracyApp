package com.example.accurancymobileapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OpcoesRendaFixaActivity extends AppCompatActivity {

    /** Optional Intent extra: which group of titles to list (default: all). */
    public static final String EXTRA_FILTRO = "filtro_titulos";
    public static final String FILTRO_BANCARIOS = "BANCARIOS";
    public static final String FILTRO_TESOURO = "TESOURO";

    private final List<TituloRendaFixa> todosTitulos = new ArrayList<>();
    private String filtroGrupo;
    private EditText edtBuscarTitulo;

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

        adapter = new TituloRendaFixaAdapter(this::abrirAplicacao);
        rvTitulosRendaFixa.setAdapter(adapter);

        filtroGrupo = getIntent().getStringExtra(EXTRA_FILTRO);
        if (FILTRO_TESOURO.equals(filtroGrupo)) {
            ((TextView) findViewById(R.id.tvTituloRendaFixa)).setText("Tesouro Direto");
        }

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

    private void abrirAplicacao(TituloRendaFixa titulo) {
        Intent intent = new Intent(this, AplicarRendaFixaActivity.class);
        intent.putExtra(AplicarRendaFixaActivity.EXTRA_TITULO, titulo);
        startActivity(intent);
    }

    private static boolean ehTesouro(TituloRendaFixa titulo) {
        String tipo = titulo.getTipoTitulo();
        return tipo != null && tipo.trim().toLowerCase(Locale.ROOT).startsWith("tesouro");
    }

    /** Applies the group (bank titles or Treasury) and the search text over the loaded titles. */
    private void aplicarFiltros() {
        String busca = edtBuscarTitulo == null
                ? ""
                : edtBuscarTitulo.getText().toString().trim().toLowerCase(Locale.ROOT);
        List<TituloRendaFixa> filtrados = new ArrayList<>();
        for (TituloRendaFixa titulo : todosTitulos) {
            if (FILTRO_TESOURO.equals(filtroGrupo) && !ehTesouro(titulo)) {
                continue;
            }
            if (FILTRO_BANCARIOS.equals(filtroGrupo) && ehTesouro(titulo)) {
                continue;
            }
            if (!busca.isEmpty()) {
                String nome = titulo.getNomeTitulo() == null ? "" : titulo.getNomeTitulo().toLowerCase(Locale.ROOT);
                String tipo = titulo.getTipoTitulo() == null ? "" : titulo.getTipoTitulo().toLowerCase(Locale.ROOT);
                if (!nome.contains(busca) && !tipo.contains(busca)) {
                    continue;
                }
            }
            filtrados.add(titulo);
        }
        adapter.atualizarLista(filtrados);
    }

    private void carregarTitulosRendaFixa(){
        repositoryRF.listarTitulosRendaFixa(new RendaFixaRepository.RendaFixaCallback() {
            @Override
            public void onSuccess(List<TituloRendaFixa> titulos) {
                todosTitulos.clear();
                if (titulos != null) {
                    todosTitulos.addAll(titulos);
                }
                aplicarFiltros();

                if(adapter.getItemCount() == 0){
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