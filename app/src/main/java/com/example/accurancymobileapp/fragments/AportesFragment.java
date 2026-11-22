package com.example.accurancymobileapp.fragments;

import static android.widget.Toast.LENGTH_LONG;

import static androidx.core.content.ContextCompat.getSystemService;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.activities.DashboardActivity;
import com.example.accurancymobileapp.activities.MarketListActivity;
import com.example.accurancymobileapp.activities.OpcoesRendaFixaActivity;
import com.example.accurancymobileapp.activities.OpcoesRendaVariavelActivity;
import com.example.accurancymobileapp.model.ActiveSpinner;
import com.example.accurancymobileapp.model.Aporte;
import com.example.accurancymobileapp.model.QuoteData;
import com.example.accurancymobileapp.model.QuoteResult;
import com.example.accurancymobileapp.model.Wallet;
import com.example.accurancymobileapp.model.clsAportes;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.network.repository.WalletRepository;
import com.example.accurancymobileapp.network.service.ApiService;
import com.example.accurancymobileapp.utils.AnimacaoUtils;
import com.example.accurancymobileapp.response.ApiResponse;
import com.example.accurancymobileapp.response.QuoteResponse;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AportesFragment extends Fragment {

    public AportesFragment() {
        super(R.layout.fragment_aportes);
    }

    LinearLayout cardBolsaDeValores, cardRendaFixa, cardTesouroDireto;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        cardBolsaDeValores = view.findViewById(R.id.cardBolsaValores);
        cardRendaFixa = view.findViewById(R.id.cardRendaFixa);

        cardBolsaDeValores.setOnClickListener(v -> {
            Intent it = new Intent(requireContext(), OpcoesRendaVariavelActivity.class);
            startActivity(it);
        });

        cardRendaFixa.setOnClickListener(v -> {
            Intent it = new Intent(requireContext(), OpcoesRendaFixaActivity.class);
            it.putExtra(OpcoesRendaFixaActivity.EXTRA_FILTRO, OpcoesRendaFixaActivity.FILTRO_BANCARIOS);
            startActivity(it);
        });

        // These two cards existed in the layout but had no click handler.
        cardTesouroDireto = view.findViewById(R.id.cardTesouroDireto);
        cardTesouroDireto.setOnClickListener(v -> {
            Intent it = new Intent(requireContext(), OpcoesRendaFixaActivity.class);
            it.putExtra(OpcoesRendaFixaActivity.EXTRA_FILTRO, OpcoesRendaFixaActivity.FILTRO_TESOURO);
            startActivity(it);
        });

        LinearLayout cardCriptomoedas = view.findViewById(R.id.cardCriptomoedasAportes);
        cardCriptomoedas.setOnClickListener(v -> abrirMercado("CRIPTO"));

        LinearLayout cardFuturos = view.findViewById(R.id.cardFuturosAportes);
        cardFuturos.setOnClickListener(v -> abrirMercado("FUTUROS"));

        LinearLayout cardMoedas = view.findViewById(R.id.cardMoedasAportes);
        cardMoedas.setOnClickListener(v -> abrirMercado("MOEDAS"));

        // Press feedback on every product card plus a staggered entrance when the tab opens.
        LinearLayout[] cards = {cardRendaFixa, cardTesouroDireto, cardBolsaDeValores,
                cardCriptomoedas, cardFuturos, cardMoedas};
        for (LinearLayout card : cards) {
            AnimacaoUtils.efeitoPressionar(card);
        }
        AnimacaoUtils.entradaEmCascata(cards);

    }

    private void abrirMercado(String tag) {
        Intent it = new Intent(requireContext(), MarketListActivity.class);
        it.putExtra("TAG", tag);
        startActivity(it);
    }
}
