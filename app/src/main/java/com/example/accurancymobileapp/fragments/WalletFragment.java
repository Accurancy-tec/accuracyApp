package com.example.accurancymobileapp.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.compose.ui.platform.ComposeView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.adapter.QuoteAdapter;
import com.example.accurancymobileapp.model.QuoteData;
import com.example.accurancymobileapp.network.repository.QuoteRepository;
import com.example.accurancymobileapp.ui.ChartHelper;

import java.util.ArrayList;
import java.util.List;


public class WalletFragment extends Fragment {
    RecyclerView recyclerInvestimentos;
    ComposeView carteiraChart;

    QuoteRepository quoteRepository;


    public WalletFragment() {
        super(R.layout.fragment_wallet);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        recyclerInvestimentos = view.findViewById(R.id.recyclerInvestimentos);

        carteiraChart = view.findViewById(R.id.carteiraChart);

        recyclerInvestimentos.setLayoutManager(new LinearLayoutManager(requireContext()));

        quoteRepository = new QuoteRepository();

        carregarCotacoes();
        carregarGraficoPizza();
    }

    public void carregarGraficoPizza(){
        ArrayList<Number> valores = new ArrayList<>();

        valores.add(933.0);
        valores.add(422.0);
        valores.add(1380.0);
        valores.add(600.0);
        valores.add(1450.0);

        ChartHelper.configurarGraficoPizza(
                carteiraChart,
                valores
        );
    }

    private void carregarCotacoes() {
        String tickers = "VALE3, PETR4, ITUB4, PETR4, PETR4";

        quoteRepository.buscarCotacoes(tickers, new QuoteRepository.QuoteCallback() {
            @Override
            public void onSucesso(List<QuoteData> quotes) {
                QuoteAdapter adapter = new QuoteAdapter(quotes);

                recyclerInvestimentos.setAdapter(adapter);
            }

            @Override
            public void onErro(String mensagem) {
                Log.e("API: ", mensagem);

                Toast.makeText(requireContext(), "Erro: " + mensagem, Toast.LENGTH_LONG).show();
            }
        });
    }

}