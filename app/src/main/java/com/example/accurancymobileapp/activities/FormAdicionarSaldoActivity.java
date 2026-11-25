package com.example.accurancymobileapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.network.repository.WalletRepository;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class FormAdicionarSaldoActivity extends AppCompatActivity {

    private EditText etValorDeposito;
    private TextView btnValor50, btnValor100, btnValor200, btnValor500, tvValorResumo, tvNovoSaldo, btnContinuarDeposito, tvSaldoAtualDeposito;
    private ImageView btnVoltarSaldo;
    private BigDecimal saldoAtual = BigDecimal.ZERO;
    private final NumberFormat formatarMoeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private boolean atualizandoCampo = false;
    private boolean requisicaoEmAndamento = false;
    private WalletRepository walletRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_form_adicionar_saldo);

        walletRepository = new WalletRepository(this);

        btnVoltarSaldo = findViewById(R.id.btnVoltarSaldo);
        etValorDeposito = findViewById(R.id.etValorDeposito);
        btnValor50 = findViewById(R.id.btnValor50);
        btnValor100 = findViewById(R.id.btnValor100);
        btnValor200 = findViewById(R.id.btnValor200);
        btnValor500 = findViewById(R.id.btnValor500);
        tvValorResumo = findViewById(R.id.tvValorResumo);
        tvNovoSaldo = findViewById(R.id.tvNovoSaldo);
        tvSaldoAtualDeposito = findViewById(R.id.tvSaldoAtualDeposito);
        btnContinuarDeposito = findViewById(R.id.btnContinuarDeposito);

        btnVoltarSaldo.setOnClickListener(v -> finish());

        String saldoRecebido = getIntent().getStringExtra("saldo_atual");

        try {
            if(saldoRecebido != null){
                saldoAtual = new BigDecimal(saldoRecebido);
            }
        } catch (NumberFormatException e) {
            saldoAtual = BigDecimal.ZERO;
        }

        configurarSaldoAtual();
        configurarValoresRapidos();
        configurarCampoValor();
        configurarBotaoContinuar();
        atualizarResumo();

    }

    private void configurarSaldoAtual(){
        tvSaldoAtualDeposito.setText("Saldo atual: " + formatarMoeda.format(saldoAtual));

    }

    private void configurarValoresRapidos(){
        btnValor50.setOnClickListener(v ->
                selecionarValor(btnValor50, "50,00")
        );

        btnValor100.setOnClickListener(v ->
                selecionarValor(btnValor100, "100,00")
        );

        btnValor200.setOnClickListener(v ->
                selecionarValor(btnValor200, "200,00")
        );

        btnValor500.setOnClickListener(v ->
                selecionarValor(btnValor500, "500,00")
        );

        // Mantém R$ 200 selecionado inicialmente
        selecionarVisual(btnValor200);
    }

    private void selecionarValor(TextView selecionado, String valor) {

        limparSelecaoValores();
        selecionarVisual(selecionado);

        atualizandoCampo = true;

        etValorDeposito.setText(valor);
        etValorDeposito.setSelection(
                etValorDeposito.getText().length()
        );

        atualizandoCampo = false;

        atualizarResumo();
    }

    private void selecionarVisual(TextView selecionado) {

        selecionado.setBackgroundResource(
                R.drawable.bg_valor_rapido_selecionado
        );

        selecionado.setTextColor(ContextCompat.getColor(this, R.color.link));

        selecionado.setScaleX(0.94f);
        selecionado.setScaleY(0.94f);

        selecionado.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(150)
                .start();
    }

    private void limparSelecaoValores() {

        TextView[] botoes = {
                btnValor50,
                btnValor100,
                btnValor200,
                btnValor500
        };

        for (TextView botao : botoes) {

            botao.setBackgroundResource(
                    R.drawable.bg_valor_rapido
            );

            botao.setTextColor(ContextCompat.getColor(this, R.color.texto_suave));
            botao.setScaleX(1f);
            botao.setScaleY(1f);
        }
    }

    private void configurarCampoValor() {

        etValorDeposito.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {

                if (!atualizandoCampo) {
                    limparSelecaoValores();
                    atualizarResumo();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private BigDecimal obterValorDigitado() {

        String valor = etValorDeposito.getText()
                .toString()
                .trim();

        if (valor.isEmpty()) {
            return BigDecimal.ZERO;
        }

        if (valor.contains(",")) {
            valor = valor.replace(".", "");
            valor = valor.replace(",", ".");
        }

        try {
            return new BigDecimal(valor);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private void atualizarResumo() {

        BigDecimal valor = obterValorDigitado();

        BigDecimal novoSaldo = saldoAtual.add(valor);

        tvValorResumo.setText(
                formatarMoeda.format(valor)
        );

        tvNovoSaldo.setText(
                formatarMoeda.format(novoSaldo)
        );

        boolean valorValido =
                valor.compareTo(BigDecimal.ZERO) > 0
                        && valor.stripTrailingZeros().scale() <= 2;

        btnContinuarDeposito.setEnabled(
                valorValido && !requisicaoEmAndamento
        );

        btnContinuarDeposito.setAlpha(
                valorValido ? 1f : 0.5f
        );
    }

    private void configurarBotaoContinuar() {

        btnContinuarDeposito.setOnClickListener(v -> {

            BigDecimal valor = obterValorDigitado();

            if (valor.compareTo(BigDecimal.ZERO) <= 0) {

                Toast.makeText(
                        this,
                        "Informe um valor válido.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            if (valor.stripTrailingZeros().scale() > 2) {

                Toast.makeText(
                        this,
                        "Informe no máximo duas casas decimais.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            adicionarSaldo(valor);
        });
    }

    private void adicionarSaldo(BigDecimal valor){
        if(requisicaoEmAndamento){
            return;
        }

        requisicaoEmAndamento = true;

        btnContinuarDeposito.setEnabled(false);
        btnContinuarDeposito.setText("Adicionando...");

        String valorFormatado = valor.setScale(2).toPlainString();

        walletRepository.adicionarSaldo(valorFormatado, new WalletRepository.WalletCallback() {
            @Override
            public void onSuccess(String mensagem) {
                requisicaoEmAndamento = false;

                Toast.makeText(FormAdicionarSaldoActivity.this, "Saldo adicionado com sucesso.", Toast.LENGTH_SHORT).show();

                Intent resultado = new Intent();

                resultado.putExtra("novo_saldo", mensagem);

                setResult(RESULT_OK, resultado);

                finish();

            }

            @Override
            public void onError(String message) {

                requisicaoEmAndamento = false;

                btnContinuarDeposito.setEnabled(true);
                btnContinuarDeposito.setText("Continuar");

                Toast.makeText(
                        FormAdicionarSaldoActivity.this,
                        message,
                        Toast.LENGTH_LONG
                ).show();

                atualizarResumo();
            }
        });
    }


}