package com.example.accurancymobileapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.model.NovaCarteiraRequest;
import com.example.accurancymobileapp.network.repository.WalletRepository;
import com.example.accurancymobileapp.utils.MascaraMonetaria;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.ChipGroup;

import java.math.BigDecimal;

/**
 * Nova carteira. A real usa o saldo geral da conta; a simulada recebe aqui um saldo inicial
 * próprio, que não sai do saldo geral. Em caso de erro o formulário continua aberto.
 */
public class FormCriarWalletActivity extends AppCompatActivity {

    /** Extra opcional: abre o formulário com "Simulada" marcada. */
    public static final String EXTRA_SIMULADA = "simulada";

    private static final String ESTADO_TIPO = "tipo";
    private static final BigDecimal VALOR_MAXIMO = new BigDecimal("10000000");

    private EditText edtNome, edtMeta, edtSaldoInicial;
    private MaterialCardView cardReal, cardSimulada;
    private View blocoSaldoInicial;
    private ChipGroup grupoEstilos;
    private TextView tvDescricaoEstilo, tvErro;
    private MaterialButton btnCriar;
    private ProgressBar progresso;
    private WalletRepository walletRepository;
    private String tipo = NovaCarteiraRequest.TIPO_REAL;
    private boolean enviando = false;

    public static Intent intent(@NonNull Context contexto, boolean simulada) {
        return new Intent(contexto, FormCriarWalletActivity.class).putExtra(EXTRA_SIMULADA, simulada);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_form_criar_wallet);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.raizNovaCarteira), (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });

        edtNome = findViewById(R.id.edtNomeCarteira);
        edtMeta = findViewById(R.id.edtMetaCarteira);
        edtSaldoInicial = findViewById(R.id.edtSaldoInicial);
        cardReal = findViewById(R.id.cardCarteiraReal);
        cardSimulada = findViewById(R.id.cardCarteiraSimulada);
        blocoSaldoInicial = findViewById(R.id.blocoSaldoInicial);
        grupoEstilos = findViewById(R.id.grupoEstilosCarteira);
        tvDescricaoEstilo = findViewById(R.id.tvDescricaoEstilo);
        tvErro = findViewById(R.id.tvErroNovaCarteira);
        btnCriar = findViewById(R.id.btnCriarCarteira);
        progresso = findViewById(R.id.progressCriarCarteira);
        walletRepository = new WalletRepository(this);

        findViewById(R.id.btnVoltarNovaCarteira).setOnClickListener(v -> finish());
        cardReal.setOnClickListener(v -> selecionarTipo(NovaCarteiraRequest.TIPO_REAL));
        cardSimulada.setOnClickListener(v -> selecionarTipo(NovaCarteiraRequest.TIPO_SIMULADA));
        grupoEstilos.setOnCheckedStateChangeListener((grupo, marcados) -> mostrarDescricaoEstilo());
        edtMeta.addTextChangedListener(new MascaraMonetaria(edtMeta));
        edtSaldoInicial.addTextChangedListener(new MascaraMonetaria(edtSaldoInicial));
        btnCriar.setOnClickListener(v -> criar());

        boolean simulada = savedInstanceState != null
                ? NovaCarteiraRequest.TIPO_SIMULADA.equals(savedInstanceState.getString(ESTADO_TIPO))
                : getIntent().getBooleanExtra(EXTRA_SIMULADA, false);
        selecionarTipo(simulada ? NovaCarteiraRequest.TIPO_SIMULADA : NovaCarteiraRequest.TIPO_REAL);
        mostrarDescricaoEstilo();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle estado) {
        super.onSaveInstanceState(estado);
        estado.putString(ESTADO_TIPO, tipo);
    }

    private void selecionarTipo(String novoTipo) {
        tipo = novoTipo;
        boolean simulada = NovaCarteiraRequest.TIPO_SIMULADA.equals(novoTipo);
        marcarCartao(cardReal, !simulada);
        marcarCartao(cardSimulada, simulada);
        blocoSaldoInicial.setVisibility(simulada ? View.VISIBLE : View.GONE);
        esconderErro();
    }

    private void marcarCartao(MaterialCardView cartao, boolean marcado) {
        cartao.setChecked(marcado);
        cartao.setCardBackgroundColor(ContextCompat.getColor(this,
                marcado ? R.color.superficie_selecionada : R.color.card_background));
        cartao.setStrokeColor(ContextCompat.getColor(this, marcado ? R.color.primary_blue : R.color.borda));
        cartao.setStrokeWidth(Math.round((marcado ? 1.5f : 1f) * getResources().getDisplayMetrics().density));
    }

    private String estiloSelecionado() {
        int marcado = grupoEstilos.getCheckedChipId();
        if (marcado == R.id.chipRendaFixa) return "Renda Fixa";
        if (marcado == R.id.chipCripto) return "Cripto";
        if (marcado == R.id.chipInternacional) return "Internacional";
        if (marcado == R.id.chipMista) return "Mista";
        return "Renda Variavel";
    }

    private void mostrarDescricaoEstilo() {
        String descricao;
        switch (estiloSelecionado()) {
            case "Renda Fixa":
                descricao = "Foco em Tesouro Direto, CDB, LCI, LCA e outros títulos.";
                break;
            case "Cripto":
                descricao = "Foco em criptomoedas, como Bitcoin e Ethereum.";
                break;
            case "Internacional":
                descricao = "Foco em ativos de fora do Brasil, como BDRs e moedas.";
                break;
            case "Mista":
                descricao = "Um pouco de tudo, sem um foco definido.";
                break;
            default:
                descricao = "Foco em ações, FIIs e ETFs da bolsa brasileira.";
        }
        tvDescricaoEstilo.setText(descricao + " O estilo só organiza: qualquer ativo pode entrar.");
    }

    private void criar() {
        if (enviando) return;
        String nome = edtNome.getText().toString().trim().replaceAll("\\s+", " ");
        BigDecimal meta = MascaraMonetaria.obterValor(edtMeta);
        BigDecimal saldoInicial = MascaraMonetaria.obterValor(edtSaldoInicial);
        boolean simulada = NovaCarteiraRequest.TIPO_SIMULADA.equals(tipo);

        if (nome.isEmpty()) {
            mostrarErro("Dê um nome para a carteira.");
            edtNome.requestFocus();
            return;
        }
        if (simulada && (saldoInicial == null || saldoInicial.signum() <= 0)) {
            mostrarErro("Informe quanto a simulação vai ter para investir.");
            edtSaldoInicial.requestFocus();
            return;
        }
        if (simulada && saldoInicial.compareTo(VALOR_MAXIMO) > 0) {
            mostrarErro("O saldo inicial pode ser de até R$ 10.000.000,00.");
            edtSaldoInicial.requestFocus();
            return;
        }

        esconderErro();
        definirEnviando(true);
        NovaCarteiraRequest pedido = new NovaCarteiraRequest(nome, tipo, estiloSelecionado(), meta, saldoInicial);
        walletRepository.criarNovaWallet(pedido, new WalletRepository.WalletCallback() {
            @Override
            public void onSuccess(String mensagem) {
                if (isFinishing() || isDestroyed()) return;
                definirEnviando(false);
                Toast.makeText(FormCriarWalletActivity.this,
                        simulada ? "Simulação criada. Bons testes!" : "Carteira criada.", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(String mensagem) {
                if (isFinishing() || isDestroyed()) return;
                definirEnviando(false);
                mostrarErro(mensagem);
            }
        });
    }

    private void mostrarErro(String mensagem) {
        tvErro.setText(mensagem);
        tvErro.setVisibility(View.VISIBLE);
    }

    private void esconderErro() {
        tvErro.setVisibility(View.GONE);
    }

    private void definirEnviando(boolean valor) {
        enviando = valor;
        btnCriar.setEnabled(!valor);
        btnCriar.setText(valor ? "" : "Criar carteira");
        progresso.setVisibility(valor ? View.VISIBLE : View.GONE);
    }
}
