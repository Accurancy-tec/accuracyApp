package com.example.accurancymobileapp.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.model.Wallet;
import com.example.accurancymobileapp.model.clsAportes;
import com.example.accurancymobileapp.network.repository.AporteRepository;
import com.example.accurancymobileapp.network.repository.WalletRepository;
import com.example.accurancymobileapp.utils.MascaraMonetaria;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Registers a dividend, JCP or FII income received for an asset the user holds.
 * Quantity and unit price do not apply: only the received amount is sent. The API keeps the
 * position unchanged and credits the amount through the same wallet/balance rules as other operations.
 */
public class RegistrarDividendoActivity extends AppCompatActivity {

    private static final String TAG = "DIVIDENDO";
    private static final BigDecimal VALOR_MAXIMO = new BigDecimal("10000000.00");
    private static final int COR_CHIP_SELECIONADO = 0xFF60A5FA;
    private static final int COR_CHIP_NORMAL = 0xFFB3C7D9;
    private static final int COR_ERRO = 0xFFFF5265;
    private static final int COR_AVISO = 0xFF829BB9;

    private final NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    private String ticker;
    private String nomeAtivo;
    private String categoria;
    private int idCarteiraPadrao;
    private String tipoProvento = "Dividendos";

    private EditText etValor;
    private TextView tvAviso, btnConfirmar, chipDividendos, chipJcp, chipRendimento;
    private boolean requisicaoEmAndamento = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registrar_dividendo);

        View raiz = findViewById(R.id.rootRegistrarDividendo);
        final int paddingTopInicial = raiz.getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(raiz, (view, windowInsets) -> {
            Insets barras = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(barras.left, paddingTopInicial + barras.top, barras.right, barras.bottom);
            return windowInsets;
        });

        ticker = getIntent().getStringExtra(DetalhesAtivoActivity.EXTRA_TICKER);
        nomeAtivo = getIntent().getStringExtra(DetalhesAtivoActivity.EXTRA_NOME);
        categoria = getIntent().getStringExtra(DetalhesAtivoActivity.EXTRA_CATEGORIA);
        idCarteiraPadrao = getIntent().getIntExtra(DetalhesAtivoActivity.EXTRA_ID_CARTEIRA, -1);

        if (ticker == null || ticker.trim().isEmpty()) {
            Log.e(TAG, "Ativo nao informado na Intent.");
            Toast.makeText(this, "Não foi informado de qual ativo é o dividendo.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (nomeAtivo == null || nomeAtivo.trim().isEmpty()) {
            nomeAtivo = ticker;
        }
        if (categoria == null || categoria.trim().isEmpty()) {
            categoria = "Ações";
        }

        vincularViews();
        ((TextView) findViewById(R.id.tvDividendoTicker)).setText(ticker);
        ((TextView) findViewById(R.id.tvDividendoNome)).setText(nomeAtivo);
        ((ImageView) findViewById(R.id.btnVoltarDividendo)).setOnClickListener(v -> finish());

        // FII income is called "rendimento"; stocks pay dividends or JCP.
        boolean fii = categoria.toLowerCase(Locale.ROOT).contains("fii");
        selecionarTipo(fii ? "Rendimento" : "Dividendos");

        chipDividendos.setOnClickListener(v -> selecionarTipo("Dividendos"));
        chipJcp.setOnClickListener(v -> selecionarTipo("JCP"));
        chipRendimento.setOnClickListener(v -> selecionarTipo("Rendimento"));

        etValor.addTextChangedListener(new MascaraMonetaria(etValor));
        etValor.addTextChangedListener(new TextWatcher() {
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
                atualizarEstado();
            }
        });
        btnConfirmar.setOnClickListener(v -> confirmar());
        atualizarEstado();
    }

    private void vincularViews() {
        etValor = findViewById(R.id.etValorDividendo);
        tvAviso = findViewById(R.id.tvDividendoAviso);
        btnConfirmar = findViewById(R.id.btnConfirmarDividendo);
        chipDividendos = findViewById(R.id.chipDividendos);
        chipJcp = findViewById(R.id.chipJcp);
        chipRendimento = findViewById(R.id.chipRendimento);
    }

    private void selecionarTipo(String tipo) {
        tipoProvento = tipo;
        TextView[] chips = {chipDividendos, chipJcp, chipRendimento};
        String[] nomes = {"Dividendos", "JCP", "Rendimento"};
        for (int i = 0; i < chips.length; i++) {
            boolean selecionado = nomes[i].equals(tipo);
            chips[i].setBackgroundResource(selecionado
                    ? R.drawable.bg_valor_rapido_selecionado : R.drawable.bg_valor_rapido);
            chips[i].setTextColor(selecionado ? COR_CHIP_SELECIONADO : COR_CHIP_NORMAL);
        }
    }

    private BigDecimal valorDigitado() {
        return MascaraMonetaria.obterValor(etValor);
    }

    /** Returns the message that blocks the registration, or null when the amount is valid. */
    private String validar(BigDecimal valor) {
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            return "Informe o valor recebido.";
        }
        if (valor.compareTo(VALOR_MAXIMO) > 0) {
            return "Valor máximo por registro: " + formatoMoeda.format(VALOR_MAXIMO);
        }
        return null;
    }

    private void atualizarEstado() {
        BigDecimal valor = valorDigitado();
        String problema = validar(valor);
        if (problema == null) {
            tvAviso.setText(" ");
            tvAviso.setTextColor(COR_AVISO);
        } else if (valor.compareTo(BigDecimal.ZERO) > 0) {
            tvAviso.setText(problema);
            tvAviso.setTextColor(COR_ERRO);
        } else {
            tvAviso.setText("Informe o valor líquido recebido");
            tvAviso.setTextColor(COR_AVISO);
        }
        boolean habilitado = problema == null && !requisicaoEmAndamento;
        btnConfirmar.setEnabled(habilitado);
        btnConfirmar.setAlpha(habilitado ? 1f : 0.5f);
    }

    private void confirmar() {
        if (requisicaoEmAndamento) {
            return;
        }
        BigDecimal valor = valorDigitado();
        String problema = validar(valor);
        if (problema != null) {
            Toast.makeText(this, problema, Toast.LENGTH_SHORT).show();
            return;
        }

        if (idCarteiraPadrao > 0) {
            // The details screen already knows the wallet that holds this asset.
            registrar(valor, idCarteiraPadrao);
            return;
        }

        definirEmAndamento(true);
        new WalletRepository(this).buscarWallets(new WalletRepository.WalletListCallback() {
            @Override
            public void onSuccess(List<Wallet> wallets) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                if (wallets == null || wallets.isEmpty()) {
                    definirEmAndamento(false);
                    Toast.makeText(RegistrarDividendoActivity.this,
                            "Crie uma carteira antes de registrar o dividendo.", Toast.LENGTH_LONG).show();
                    return;
                }
                if (wallets.size() == 1) {
                    registrar(valor, wallets.get(0).getId_carteira());
                    return;
                }
                Wallet[] opcoes = wallets.toArray(new Wallet[0]);
                CharSequence[] nomes = new CharSequence[opcoes.length];
                for (int i = 0; i < opcoes.length; i++) {
                    nomes[i] = opcoes[i].getNome_carteira() + " · " + opcoes[i].getTipo_carteira();
                }
                new AlertDialog.Builder(RegistrarDividendoActivity.this)
                        .setTitle("Escolha a carteira")
                        .setItems(nomes, (dialog, which) -> {
                            if (which >= 0 && which < opcoes.length) {
                                registrar(valor, opcoes[which].getId_carteira());
                            } else {
                                definirEmAndamento(false);
                            }
                        })
                        .setNegativeButton("Cancelar", (dialog, which) -> definirEmAndamento(false))
                        .setOnCancelListener(dialog -> definirEmAndamento(false))
                        .show();
            }

            @Override
            public void onError(String message) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                Log.e(TAG, "Falha ao listar carteiras: " + message);
                definirEmAndamento(false);
                Toast.makeText(RegistrarDividendoActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void registrar(BigDecimal valor, int idCarteira) {
        definirEmAndamento(true);
        Log.d(TAG, "Enviando dividendo: ativo=" + ticker + " carteira=" + idCarteira
                + " tipo=" + tipoProvento + " valor=" + valor.toPlainString());

        clsAportes aporte = clsAportes.paraDividendo(
                ticker, nomeAtivo, categoria, valor.doubleValue(), tipoProvento, idCarteira);

        new AporteRepository(this).registrarAporte(aporte, new AporteRepository.RegistroCallback() {
            @Override
            public void onSucesso(String mensagem) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                Log.d(TAG, "Dividendo registrado: ativo=" + ticker);
                Toast.makeText(RegistrarDividendoActivity.this,
                        "Dividendo registrado com sucesso (operação simulada).", Toast.LENGTH_LONG).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onErro(String mensagem) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                Log.w(TAG, "Dividendo recusado: " + mensagem);
                definirEmAndamento(false);
                Toast.makeText(RegistrarDividendoActivity.this, mensagem, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void definirEmAndamento(boolean emAndamento) {
        requisicaoEmAndamento = emAndamento;
        btnConfirmar.setText(emAndamento ? "Registrando…" : "Registrar dividendo");
        atualizarEstado();
    }
}
