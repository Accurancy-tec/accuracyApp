package com.example.accurancymobileapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.accurancymobileapp.R;

public class OpcoesRendaVariavelActivity extends AppCompatActivity {

    LinearLayout cardAcoesNacionais, cardFundosImobiliarios, cardContratosFuturos, cardAcoesETFsInternacionais;
    ImageButton btnVoltarRendaVariavel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opcoes_renda_variavel);

        btnVoltarRendaVariavel = findViewById(R.id.btnVoltarRendaVariavel);

        cardAcoesNacionais = findViewById(R.id.cardAcoesNacionais);
        cardFundosImobiliarios = findViewById(R.id.cardFundosImobiliarios);
        cardContratosFuturos = findViewById(R.id.cardContratosFuturos);
        cardAcoesETFsInternacionais = findViewById(R.id.cardAcoesETFsInternacionais);

        //Voltar para a tela inicial
        btnVoltarRendaVariavel.setOnClickListener(v -> finish());

        //Abrir telas dos cards
        cardAcoesNacionais.setOnClickListener(v -> abrirTela(MarketListActivity.class,"ACOES_NACIONAIS"));

        cardFundosImobiliarios.setOnClickListener(v -> abrirTela(MarketListActivity.class,"FIIS"));

        //cardContratosFuturos.setOnClickListener(v -> abrirTela(ContratosFuturosActivity.class));

    }

    private void abrirTela(Class<?> activityClass,String screen){
        Intent it = new Intent(this, activityClass);
        it.putExtra("TAG",screen);
        startActivity(it);
    }
}