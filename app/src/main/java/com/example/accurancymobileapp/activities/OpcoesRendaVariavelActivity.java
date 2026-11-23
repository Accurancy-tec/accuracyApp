package com.example.accurancymobileapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.utils.AnimacaoUtils;

/**
 * "Bolsa de Valores" screen: stocks, real-estate funds and BDRs/ETFs, all as same-size rows.
 * Futures, crypto and currencies have their own cards on the Produtos tab.
 */
public class OpcoesRendaVariavelActivity extends AppCompatActivity {

    ImageButton btnVoltarRendaVariavel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opcoes_renda_variavel);

        btnVoltarRendaVariavel = findViewById(R.id.btnVoltarRendaVariavel);
        btnVoltarRendaVariavel.setOnClickListener(v -> finish());

        LinearLayout cardAcoes = findViewById(R.id.cardAcoesNacionais);
        LinearLayout cardFiis = findViewById(R.id.cardFundosImobiliarios);
        LinearLayout cardBdrs = findViewById(R.id.cardAcoesETFsInternacionais);

        cardAcoes.setOnClickListener(v -> abrirTela("ACOES_NACIONAIS"));
        cardFiis.setOnClickListener(v -> abrirTela("FIIS"));
        cardBdrs.setOnClickListener(v -> abrirTela("BDRS_ETFS"));

        View[] cards = {cardAcoes, cardFiis, cardBdrs};
        for (View card : cards) {
            AnimacaoUtils.efeitoPressionar(card);
        }
        AnimacaoUtils.entradaEmCascata(cards);
    }

    private void abrirTela(String tag) {
        Intent it = new Intent(this, MarketListActivity.class);
        it.putExtra("TAG", tag);
        startActivity(it);
    }
}
