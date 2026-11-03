package com.example.accurancymobileapp.activities;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
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
    private MaterialCardView cardCarteiraSimulado, cardCarteiraReal;
    private WalletRepository walletRepository;
    private String tipoCarteira = "Renda Variavel";
    private String estiloCarteira = "Real";

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
        cardCarteiraSimulado = findViewById(R.id.cardCarteiraSimulada);
        cardCarteiraReal = findViewById(R.id.cardCarteiraReal);
        btnCriarCarteira = findViewById(R.id.btnCriarCarteira);
        walletRepository = new WalletRepository(this);

        btnVoltarNovaCarteira.setOnClickListener(v -> finish());

        btnRendaFixa.setOnClickListener(v -> {
            tipoCarteira = "Renda Fixa";
        });

        btnRendaVariavel.setOnClickListener(v -> {
            tipoCarteira = "Renda Variavel";
        });

        btnCripto.setOnClickListener(v -> {
            tipoCarteira = "Cripto";
        });

        btnInternacional.setOnClickListener(v -> {
            tipoCarteira = "Internacional";
        });

        cardCarteiraReal.setOnClickListener(v -> {
            estiloCarteira = "Real";
        });

        cardCarteiraSimulado.setOnClickListener(v -> {
            estiloCarteira = "Simulada";
        });

        btnCriarCarteira.setOnClickListener(v -> {

            String nome = edtNomeCarteira.getText().toString().trim();
            BigDecimal meta = MascaraMonetaria.obterValor(edtMetaCarteira);

            criarWallet(nome, tipoCarteira, meta, estiloCarteira);
        });

        edtMetaCarteira.addTextChangedListener(new MascaraMonetaria(edtMetaCarteira));

    }

    private void criarWallet(String nome, String tipo, BigDecimal meta, String estilo){
        walletRepository.criarNovaWallet(nome, tipo, meta, estilo, new WalletRepository.WalletCallback() {
            @Override
            public void onSuccess(String mensagem) {
                Toast.makeText(
                        FormCriarWalletActivity.this,
                        mensagem,
                        Toast.LENGTH_SHORT
                ).show();


            }

            @Override
            public void onError(String message) {
                Toast.makeText(
                        FormCriarWalletActivity.this,
                        message,
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        Log.d("CARTEIRA", "Nome:" + nome + "| Tipo: " + tipo);
    }
}