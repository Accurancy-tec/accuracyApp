package com.example.accurancymobileapp.activities;

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
import com.example.accurancymobileapp.fragments.InvestirRendaFixaBottomSheet;
import com.example.accurancymobileapp.utils.AnimacaoUtils;
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
    ProgressBar progressRendaFixa;
    View estadoVazio;
    TextView tvEstadoVazioTitulo, tvEstadoVazioMensagem, btnTentarNovamente;
    RendaFixaRepository repositoryRF;
    TituloRendaFixaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opcoes_renda_fixa);

        btnVoltarRendaFixa = findViewById(R.id.btnVoltarRendaFixa);
        rvTitulosRendaFixa = findViewById(R.id.rvTitulosRendaFixa);
        progressRendaFixa = findViewById(R.id.progressRendaFixa);
        estadoVazio = findViewById(R.id.estadoVazioRendaFixa);
        tvEstadoVazioTitulo = findViewById(R.id.tvEstadoVazioTituloRf);
        tvEstadoVazioMensagem = findViewById(R.id.tvEstadoVazioMensagemRf);
        btnTentarNovamente = findViewById(R.id.btnTentarNovamenteRf);
        btnTentarNovamente.setOnClickListener(v -> carregarTitulosRendaFixa());
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
        if (getSupportFragmentManager().findFragmentByTag(InvestirRendaFixaBottomSheet.TAG_FRAGMENT) != null) {
            return;
        }
        InvestirRendaFixaBottomSheet.nova(titulo)
                .show(getSupportFragmentManager(), InvestirRendaFixaBottomSheet.TAG_FRAGMENT);
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

        boolean vazio = filtrados.isEmpty();
        if (progressRendaFixa.getVisibility() == View.VISIBLE) {
            // Still loading: keep the spinner only, no empty message yet.
            estadoVazio.setVisibility(View.GONE);
        } else if (vazio && !todosTitulos.isEmpty()) {
            mostrarEstadoVazio("Nenhum título encontrado", "Tente outra busca.", false);
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

    private void carregarTitulosRendaFixa(){
        estadoVazio.setVisibility(View.GONE);
        progressRendaFixa.setVisibility(View.VISIBLE);
        repositoryRF.listarTitulosRendaFixa(new RendaFixaRepository.RendaFixaCallback() {
            @Override
            public void onSuccess(List<TituloRendaFixa> titulos) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                progressRendaFixa.setVisibility(View.GONE);
                todosTitulos.clear();
                if (titulos != null) {
                    todosTitulos.addAll(titulos);
                }
                aplicarFiltros();
            }

            @Override
            public void onError(String message) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                progressRendaFixa.setVisibility(View.GONE);
                todosTitulos.clear();
                adapter.atualizarLista(new ArrayList<>());
                rvTitulosRendaFixa.setVisibility(View.INVISIBLE);
                mostrarEstadoVazio("Não foi possível carregar os títulos",
                        message == null || message.trim().isEmpty()
                                ? "Verifique sua conexão e tente novamente." : message, true);
            }
        });
    }
}