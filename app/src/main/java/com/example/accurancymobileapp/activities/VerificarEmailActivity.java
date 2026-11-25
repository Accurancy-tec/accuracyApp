package com.example.accurancymobileapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.network.service.ApiService;
import com.example.accurancymobileapp.response.PerfilResponse;
import com.example.accurancymobileapp.utils.PerfilCallback;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;

/**
 * Asks for the 6 digit code the API e-mailed. Opened after the sign-up and when the login says the
 * e-mail is not confirmed yet. Finishes with RESULT_OK (and the e-mail) when the code is accepted.
 */
public class VerificarEmailActivity extends AppCompatActivity {

    public static final String EXTRA_EMAIL = "email";
    /** When true a new code is requested as soon as the screen opens (used when coming from the login). */
    public static final String EXTRA_ENVIAR_AO_ABRIR = "enviar_ao_abrir";

    private static final long ESPERA_REENVIO_MS = 60_000L;

    private String email;
    private EditText etCodigo;
    private TextView tvErro, btnReenviar;
    private Button btnConfirmar;
    private CountDownTimer contagem;
    private boolean ocupado = false;
    private boolean reenviando = false;
    private ApiService api;
    private Call<PerfilResponse> chamadaAtual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verificar_email);

        email = getIntent().getStringExtra(EXTRA_EMAIL);
        if (email == null || email.trim().isEmpty()) {
            Toast.makeText(this, "E-mail não informado.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        email = email.trim();
        api = RetrofitClient.getApiService(this);

        etCodigo = findViewById(R.id.etCodigoVerificacao);
        tvErro = findViewById(R.id.tvVerificarErro);
        btnReenviar = findViewById(R.id.btnReenviarCodigo);
        btnConfirmar = findViewById(R.id.btnConfirmarCodigo);

        ((TextView) findViewById(R.id.tvVerificarEmailInfo)).setText(
                "Enviamos um código de 6 dígitos para " + email + ". Ele vale por 10 minutos.");

        etCodigo.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                tvErro.setVisibility(android.view.View.GONE);
                if (s.length() == 6) confirmar();
            }
        });
        btnConfirmar.setOnClickListener(v -> confirmar());
        btnReenviar.setOnClickListener(v -> reenviar());
        findViewById(R.id.btnVoltarLogin).setOnClickListener(v -> finish());

        if (getIntent().getBooleanExtra(EXTRA_ENVIAR_AO_ABRIR, false)) {
            reenviar();
        } else {
            iniciarContagem();
        }
    }

    private void confirmar() {
        if (ocupado) return;
        String codigo = etCodigo.getText().toString().trim();
        if (!codigo.matches("[0-9]{6}")) {
            mostrarErro("Digite os 6 números do código.");
            return;
        }
        definirOcupado(true);
        Map<String, String> corpo = new HashMap<>();
        corpo.put("email_usuario", email);
        corpo.put("codigo", codigo);
        chamadaAtual = api.verificarEmail(corpo);
        chamadaAtual.enqueue(new PerfilCallback(this) {
            @Override
            public void aoSucesso(PerfilResponse resposta) {
                Toast.makeText(VerificarEmailActivity.this,
                        resposta.texto("E-mail confirmado!"), Toast.LENGTH_LONG).show();
                Intent resultado = new Intent();
                resultado.putExtra(EXTRA_EMAIL, email);
                setResult(RESULT_OK, resultado);
                finish();
            }

            @Override
            public void aoErro(String mensagem) {
                definirOcupado(false);
                mostrarErro(mensagem);
            }
        });
    }

    private void reenviar() {
        if (reenviando || ocupado) return;
        reenviando = true;
        btnReenviar.setEnabled(false);
        btnReenviar.setText("Enviando...");
        Map<String, String> corpo = new HashMap<>();
        corpo.put("email_usuario", email);
        api.reenviarVerificacao(corpo).enqueue(new PerfilCallback(this) {
            @Override
            public void aoSucesso(PerfilResponse resposta) {
                reenviando = false;
                Toast.makeText(VerificarEmailActivity.this,
                        "Enviamos um novo código para o seu e-mail.", Toast.LENGTH_SHORT).show();
                iniciarContagem();
            }

            @Override
            public void aoErro(String mensagem) {
                reenviando = false;
                mostrarErro(mensagem);
                iniciarContagem();
            }
        });
    }

    private void iniciarContagem() {
        if (contagem != null) contagem.cancel();
        btnReenviar.setEnabled(false);
        contagem = new CountDownTimer(ESPERA_REENVIO_MS, 1000L) {
            @Override
            public void onTick(long restante) {
                btnReenviar.setText("Reenviar código em " + (restante / 1000L + 1) + "s");
            }

            @Override
            public void onFinish() {
                btnReenviar.setText("Reenviar código");
                btnReenviar.setEnabled(true);
            }
        }.start();
    }

    private void definirOcupado(boolean valor) {
        ocupado = valor;
        btnConfirmar.setEnabled(!valor);
        etCodigo.setEnabled(!valor);
        btnConfirmar.setText(valor ? "Confirmando..." : "Confirmar e-mail");
    }

    private void mostrarErro(String mensagem) {
        tvErro.setText(mensagem);
        tvErro.setVisibility(android.view.View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        if (contagem != null) contagem.cancel();
        if (chamadaAtual != null) chamadaAtual.cancel();
        super.onDestroy();
    }
}
