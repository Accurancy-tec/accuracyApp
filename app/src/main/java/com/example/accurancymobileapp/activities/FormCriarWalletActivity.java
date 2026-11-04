package com.example.accurancymobileapp.activities;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.network.repository.WalletRepository;
import com.example.accurancymobileapp.utils.MascaraMonetaria;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.math.BigDecimal;

public class FormCriarWalletActivity extends AppCompatActivity {

    private ImageButton btnVoltarNovaCarteira;
    private MaterialButton btnCriarCarteira, btnRendaFixa, btnRendaVariavel, btnCripto, btnInternacional;
    private EditText edtNomeCarteira, edtMetaCarteira;
    private MaterialCardView cardCarteiraSimulada, cardCarteiraReal;
    private WalletRepository walletRepository;
    private String tipoCarteira = "Renda Variavel";
    private String estiloCarteira = "Real";
    private ProgressBar progressCriarCarteira;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_form_criar_wallet);

        btnVoltarNovaCarteira = findViewById(R.id.btnVoltarNovaCarteira);
        btnRendaFixa = findViewById(R.id.btnRendaFixa);
        btnRendaVariavel = findViewById(R.id.btnRendaVariavel);
        btnCripto = findViewById(R.id.btnCripto);
        btnInternacional = findViewById(R.id.btnInternacional);
        edtNomeCarteira = findViewById(R.id.edtNomeCarteira);
        edtMetaCarteira = findViewById(R.id.edtMetaCarteira);
        cardCarteiraSimulada = findViewById(R.id.cardCarteiraSimulada);
        cardCarteiraReal = findViewById(R.id.cardCarteiraReal);
        btnCriarCarteira = findViewById(R.id.btnCriarCarteira);
        progressCriarCarteira = findViewById(R.id.progressCriarCarteira);
        walletRepository = new WalletRepository(this);

        btnVoltarNovaCarteira.setOnClickListener(v -> {
            animarClique(btnVoltarNovaCarteira);
            finish();
        });


        btnRendaFixa.setOnClickListener(v -> {
            tipoCarteira = "Renda Fixa";
            selecionarEstilo(btnRendaFixa);
        });

        btnRendaVariavel.setOnClickListener(v -> {
            tipoCarteira = "Renda Variavel";
            selecionarEstilo(btnRendaVariavel);
        });

        btnCripto.setOnClickListener(v -> {
            tipoCarteira = "Cripto";
            selecionarEstilo(btnCripto);
        });

        btnInternacional.setOnClickListener(v -> {
            tipoCarteira = "Internacional";
            selecionarEstilo(btnInternacional);
        });

        cardCarteiraReal.setOnClickListener(v -> {
            estiloCarteira = "Real";
            selecionarTipo(cardCarteiraReal);
        });

        cardCarteiraSimulada.setOnClickListener(v -> {
            estiloCarteira = "Simulada";
            selecionarTipo(cardCarteiraSimulada);
        });

        btnCriarCarteira.setOnClickListener(v -> {

            String nome = edtNomeCarteira.getText().toString().trim();
            BigDecimal meta = MascaraMonetaria.obterValor(edtMetaCarteira);

            criarWallet(nome, tipoCarteira, meta, estiloCarteira);
        });

        edtMetaCarteira.addTextChangedListener(new MascaraMonetaria(edtMetaCarteira));

    }

    private void criarWallet(String nome, String tipo, BigDecimal meta, String estilo){
        iniciarCarregamento();
        walletRepository.criarNovaWallet(nome, tipo, meta, estilo, new WalletRepository.WalletCallback() {
            @Override
            public void onSuccess(String mensagem) {
                finalizarCarregamento();
                Toast.makeText(
                        FormCriarWalletActivity.this,
                        mensagem,
                        Toast.LENGTH_SHORT
                ).show();

                finish();
            }

            @Override
            public void onError(String message) {
                finalizarCarregamento();
                Toast.makeText(
                        FormCriarWalletActivity.this,
                        message,
                        Toast.LENGTH_SHORT
                ).show();
                finish();
            }
        });

        Log.d("CARTEIRA", "Nome:" + nome + "| Tipo: " + tipo);
    }

    private void animarClique(View view) {
        view.animate()
                .scaleX(0.96f)
                .scaleY(0.96f)
                .setDuration(80)
                .withEndAction(() -> {
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start();
                })
                .start();
    }

    private void selecionarEstilo(MaterialButton selecionado) {

        MaterialButton[] botoes = {
                btnRendaFixa,
                btnRendaVariavel,
                btnCripto,
                btnInternacional,
        };

        for (MaterialButton botao : botoes) {

            boolean ativo = botao == selecionado;

            if (ativo) {
                botao.setBackgroundTintList(
                        ColorStateList.valueOf(Color.parseColor("#142B50"))
                );

                botao.setStrokeColor(
                        ColorStateList.valueOf(Color.parseColor("#2463EB"))
                );

                botao.setTextColor(Color.parseColor("#3B82F6"));

            } else {
                botao.setBackgroundTintList(
                        ColorStateList.valueOf(Color.parseColor("#081426"))
                );

                botao.setStrokeColor(
                        ColorStateList.valueOf(Color.parseColor("#263B56"))
                );

                botao.setTextColor(Color.parseColor("#B8C7DA"));
            }
        }
        animarClique(selecionado);
    }


    private void selecionarTipo(MaterialCardView selecionado) {

        MaterialCardView[] cards = {
                cardCarteiraSimulada,
                cardCarteiraReal
        };

        for (MaterialCardView card : cards) {

            boolean ativo = card == selecionado;

            if (ativo) {
                card.setCardBackgroundColor(
                        Color.parseColor("#142B50")
                );

                card.setStrokeColor(
                        Color.parseColor("#2463EB")
                );

            } else {
                card.setCardBackgroundColor(
                        Color.parseColor("#102036")
                );

                card.setStrokeColor(
                        Color.parseColor("#263B56")
                );
            }
        }

        animarClique(selecionado);
    }

    private void iniciarCarregamento() {
        btnCriarCarteira.setEnabled(false);
        btnCriarCarteira.setText("");
        progressCriarCarteira.setVisibility(View.VISIBLE);

        btnCriarCarteira.animate()
                .alpha(0.8f)
                .setDuration(150)
                .start();
    }

    private void finalizarCarregamento() {
        progressCriarCarteira.setVisibility(View.GONE);
        btnCriarCarteira.setText("Criar carteira");
        btnCriarCarteira.setEnabled(true);

        btnCriarCarteira.animate()
                .alpha(1f)
                .setDuration(150)
                .start();
    }
}