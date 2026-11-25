package com.example.accurancymobileapp.activities;

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
import androidx.annotation.ColorRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.model.TituloRendaFixa;
import com.example.accurancymobileapp.model.Wallet;
import com.example.accurancymobileapp.model.clsAportes;
import com.example.accurancymobileapp.network.repository.AporteRepository;
import com.example.accurancymobileapp.network.repository.WalletRepository;
import com.example.accurancymobileapp.utils.EscolhaCarteira;
import com.example.accurancymobileapp.utils.MascaraMonetaria;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/**
 * Screen to apply an amount in a fixed income title (CDB, LCI, LCA, Treasury...).
 * Only the title id and the amount are sent: the API reads the name, rate, maturity and
 * minimum from its own title table and enforces the balance rule.
 */
public class AplicarRendaFixaActivity extends AppCompatActivity {

    public static final String EXTRA_TITULO = "titulo_renda_fixa";

    private static final String TAG = "RF_APLICACAO";
    private static final BigDecimal VALOR_MAXIMO = new BigDecimal("10000000.00");
    @ColorRes private static final int COR_CHIP_SELECIONADO = R.color.link;
    @ColorRes private static final int COR_CHIP_NORMAL = R.color.texto_suave;
    @ColorRes private static final int COR_ERRO = R.color.negativo_destaque;
    @ColorRes private static final int COR_AVISO = R.color.texto_fraco;

    private final NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    private TituloRendaFixa titulo;
    private BigDecimal aplicacaoMinima = BigDecimal.ZERO;

    private EditText etValorAplicacao;
    private TextView tvAplicarAviso, tvResumoValorAplicacao, tvResumoTaxa, btnConfirmarAplicacao;
    private TextView btnAplicarMinimo, btnAplicar500, btnAplicar1000, btnAplicar5000;

    private boolean requisicaoEmAndamento = false;
    private boolean atualizandoCampo = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_aplicar_renda_fixa);

        View raiz = findViewById(R.id.rootAplicarRendaFixa);
        final int paddingTopInicial = raiz.getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(raiz, (view, windowInsets) -> {
            Insets barras = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(barras.left, paddingTopInicial + barras.top, barras.right, barras.bottom);
            return windowInsets;
        });

        Object recebido = getIntent().getSerializableExtra(EXTRA_TITULO);
        if (!(recebido instanceof TituloRendaFixa)) {
            Log.e(TAG, "Titulo nao informado na Intent.");
            Toast.makeText(this, "Não foi informado qual título aplicar.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        titulo = (TituloRendaFixa) recebido;
        if (titulo.getAplicacaoMinima() != null) {
            aplicacaoMinima = titulo.getAplicacaoMinima();
        }

        vincularViews();
        preencherTitulo();
        configurarValoresRapidos();
        configurarCampoValor();
        configurarBotoes();
        atualizarResumo();
    }

    private void vincularViews() {
        etValorAplicacao = findViewById(R.id.etValorAplicacao);
        tvAplicarAviso = findViewById(R.id.tvAplicarAviso);
        tvResumoValorAplicacao = findViewById(R.id.tvResumoValorAplicacao);
        tvResumoTaxa = findViewById(R.id.tvResumoTaxa);
        btnConfirmarAplicacao = findViewById(R.id.btnConfirmarAplicacao);
        btnAplicarMinimo = findViewById(R.id.btnAplicarMinimo);
        btnAplicar500 = findViewById(R.id.btnAplicar500);
        btnAplicar1000 = findViewById(R.id.btnAplicar1000);
        btnAplicar5000 = findViewById(R.id.btnAplicar5000);
    }

    private void preencherTitulo() {
        ((TextView) findViewById(R.id.tvAplicarTipo)).setText(
                titulo.getTipoTitulo() == null ? "Renda fixa" : titulo.getTipoTitulo());
        ((TextView) findViewById(R.id.tvAplicarNome)).setText(titulo.getNomeTitulo());
        ((TextView) findViewById(R.id.tvAplicarRentabilidade)).setText(titulo.getRentabilidadeFormatada());
        ((TextView) findViewById(R.id.tvAplicarVencimento)).setText(titulo.getVencimentoFormatado());
        ((TextView) findViewById(R.id.tvAplicarLiquidez)).setText(
                titulo.getLiquidez() == null || titulo.getLiquidez().trim().isEmpty()
                        ? "Não informado" : titulo.getLiquidez());
        ((TextView) findViewById(R.id.tvAplicarMinima)).setText(formatoMoeda.format(aplicacaoMinima));
        tvResumoTaxa = findViewById(R.id.tvResumoTaxa);
        tvResumoTaxa.setText(titulo.getRentabilidadeFormatada());

        findViewById(R.id.tvAplicarSeloFgc).setVisibility(titulo.possuiGarantiaFgc() ? View.VISIBLE : View.GONE);
        findViewById(R.id.tvAplicarSeloIR).setVisibility(titulo.isIsentoIR() ? View.VISIBLE : View.GONE);

        ImageView voltar = findViewById(R.id.btnVoltarAplicarRF);
        voltar.setOnClickListener(v -> finish());
    }

    private void configurarValoresRapidos() {
        btnAplicarMinimo.setOnClickListener(v -> selecionarValor(btnAplicarMinimo, aplicacaoMinima));
        btnAplicar500.setOnClickListener(v -> selecionarValor(btnAplicar500, new BigDecimal("500")));
        btnAplicar1000.setOnClickListener(v -> selecionarValor(btnAplicar1000, new BigDecimal("1000")));
        btnAplicar5000.setOnClickListener(v -> selecionarValor(btnAplicar5000, new BigDecimal("5000")));
    }

    private void selecionarValor(TextView chip, BigDecimal valor) {
        limparSelecaoChips();
        chip.setBackgroundResource(R.drawable.bg_valor_rapido_selecionado);
        chip.setTextColor(cor(COR_CHIP_SELECIONADO));

        atualizandoCampo = true;
        etValorAplicacao.setText(formatoMoeda.format(valor));
        etValorAplicacao.setSelection(etValorAplicacao.getText().length());
        atualizandoCampo = false;

        atualizarResumo();
    }

    private void limparSelecaoChips() {
        TextView[] chips = {btnAplicarMinimo, btnAplicar500, btnAplicar1000, btnAplicar5000};
        for (TextView chip : chips) {
            chip.setBackgroundResource(R.drawable.bg_valor_rapido);
            chip.setTextColor(cor(COR_CHIP_NORMAL));
        }
    }

    private void configurarCampoValor() {
        etValorAplicacao.addTextChangedListener(new MascaraMonetaria(etValorAplicacao));
        etValorAplicacao.addTextChangedListener(new TextWatcher() {
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
                if (!atualizandoCampo) {
                    limparSelecaoChips();
                }
                atualizarResumo();
            }
        });
    }

    private void configurarBotoes() {
        btnConfirmarAplicacao.setOnClickListener(v -> confirmarAplicacao());
    }

    private BigDecimal valorDigitado() {
        return MascaraMonetaria.obterValor(etValorAplicacao);
    }

    /** Returns the message that blocks the application, or null when the amount is valid. */
    private String validar(BigDecimal valor) {
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            return "Informe o valor da aplicação.";
        }
        if (valor.compareTo(aplicacaoMinima) < 0) {
            return "Valor mínimo: " + formatoMoeda.format(aplicacaoMinima);
        }
        if (valor.compareTo(VALOR_MAXIMO) > 0) {
            return "Valor máximo por aplicação: " + formatoMoeda.format(VALOR_MAXIMO);
        }
        if (tituloVencido()) {
            return "Este título já venceu e não aceita novas aplicações.";
        }
        return null;
    }

    private boolean tituloVencido() {
        String vencimento = titulo.getVencimento();
        if (vencimento == null || vencimento.trim().isEmpty()) {
            return false;
        }
        try {
            return LocalDate.parse(vencimento.trim()).isBefore(LocalDate.now());
        } catch (Exception ignored) {
            // Unknown format: let the API decide.
            return false;
        }
    }

    private void atualizarResumo() {
        BigDecimal valor = valorDigitado();
        tvResumoValorAplicacao.setText(formatoMoeda.format(valor));

        String problema = validar(valor);
        boolean valido = problema == null;

        if (valido) {
            tvAplicarAviso.setText(" ");
            tvAplicarAviso.setTextColor(cor(COR_AVISO));
        } else if (valor.compareTo(BigDecimal.ZERO) > 0 || tituloVencido()) {
            tvAplicarAviso.setText(problema);
            tvAplicarAviso.setTextColor(cor(COR_ERRO));
        } else {
            tvAplicarAviso.setText("Aplicação mínima: " + formatoMoeda.format(aplicacaoMinima));
            tvAplicarAviso.setTextColor(cor(COR_AVISO));
        }

        boolean habilitado = valido && !requisicaoEmAndamento;
        btnConfirmarAplicacao.setEnabled(habilitado);
        btnConfirmarAplicacao.setAlpha(habilitado ? 1f : 0.5f);
    }

    private void confirmarAplicacao() {
        if (requisicaoEmAndamento) {
            return;
        }
        BigDecimal valor = valorDigitado();
        String problema = validar(valor);
        if (problema != null) {
            Toast.makeText(this, problema, Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(AplicarRendaFixaActivity.this,
                            "Crie uma carteira antes de aplicar em renda fixa.", Toast.LENGTH_LONG).show();
                    return;
                }
                if (wallets.size() == 1) {
                    conferirSaldoERegistrar(valor, wallets.get(0));
                    return;
                }
                EscolhaCarteira.mostrar(AplicarRendaFixaActivity.this, "Aplicar em qual carteira?", wallets, null,
                        carteira -> conferirSaldoERegistrar(valor, carteira),
                        () -> definirEmAndamento(false));
            }

            @Override
            public void onError(String message) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                Log.e(TAG, "Falha ao listar carteiras: " + message);
                definirEmAndamento(false);
                Toast.makeText(AplicarRendaFixaActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    /** Na simulação, confere o saldo dela antes de enviar; nas reais quem confere é o servidor. */
    private void conferirSaldoERegistrar(BigDecimal valor, Wallet carteira) {
        String falta = EscolhaCarteira.faltaSaldo(carteira, null, valor);
        if (falta != null) {
            definirEmAndamento(false);
            Toast.makeText(this, falta, Toast.LENGTH_LONG).show();
            return;
        }
        registrar(valor, carteira.getId_carteira());
    }

    private void registrar(BigDecimal valor, int idCarteira) {
        Log.d(TAG, "Enviando aplicacao: titulo=" + titulo.getIdTitulo()
                + " carteira=" + idCarteira + " valor=" + valor.toPlainString());

        clsAportes aporte = clsAportes.paraRendaFixa(
                titulo.getIdTitulo(),
                titulo.getNomeTitulo(),
                valor.doubleValue(),
                idCarteira
        );

        new AporteRepository(this).registrarAporte(aporte, new AporteRepository.RegistroCallback() {
            @Override
            public void onSucesso(String mensagem) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                Log.d(TAG, "Aplicacao registrada: titulo=" + titulo.getIdTitulo());
                Toast.makeText(AplicarRendaFixaActivity.this,
                        "Aplicação realizada com sucesso (operação simulada).", Toast.LENGTH_LONG).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onErro(String mensagem) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                Log.w(TAG, "Aplicacao recusada: " + mensagem);
                definirEmAndamento(false);
                Toast.makeText(AplicarRendaFixaActivity.this, mensagem, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void definirEmAndamento(boolean emAndamento) {
        requisicaoEmAndamento = emAndamento;
        btnConfirmarAplicacao.setText(emAndamento ? "Registrando aplicação…" : "Confirmar aplicação");
        atualizarResumo();
    }

    private int cor(@ColorRes int id) {
        return ContextCompat.getColor(this, id);
    }
}
