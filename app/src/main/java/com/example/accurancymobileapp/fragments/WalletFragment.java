package com.example.accurancymobileapp.fragments;

import android.os.Bundle;

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
import com.example.accurancymobileapp.adapter.AporteAdapter;
import com.example.accurancymobileapp.model.Aporte;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.network.repository.AporteRepository;
import com.example.accurancymobileapp.network.service.VicoService;
import com.example.accurancymobileapp.response.VicoResponse;
import com.example.accurancymobileapp.ui.ChartHelper;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WalletFragment extends Fragment {

    private RecyclerView recyclerInvestimentos;
    private ComposeView carteiraChart;

    public WalletFragment() {
        super(R.layout.fragment_wallet);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        recyclerInvestimentos = view.findViewById(R.id.recyclerInvestimentos);
        carteiraChart = view.findViewById(R.id.carteiraChart);

        recyclerInvestimentos.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        carregarInvestimentos();
        carregarGraficoDistribuicao();
    }

    private void carregarGraficoDistribuicao() {
        VicoService vico = RetrofitClient
                .getClient(requireContext())
                .create(VicoService.class);

        vico.getDistribuicao(null).enqueue(new Callback<VicoResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<VicoResponse> call,
                    @NonNull Response<VicoResponse> response
            ) {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e("GRAFICO_CARTEIRA", "Erro HTTP: " + response.code());
                    return;
                }

                VicoResponse body = response.body();
                if (!body.isSucesso() || body.getDistribuicao() == null || body.getDistribuicao().isEmpty()) {
                    Log.e("GRAFICO_CARTEIRA", "Sem dados: " + body.getMensagem());
                    return;
                }

                List<String> categorias = new ArrayList<>();
                List<Double> valores = new ArrayList<>();

                for (VicoResponse.Distribuicao item : body.getDistribuicao()) {
                    if (item == null) {
                        continue;
                    }

                    categorias.add(item.getCategoria());
                    valores.add(item.getValor());
                }

                if (!valores.isEmpty()) {
                    ChartHelper.configurarGraficoDistribuicao(
                            carteiraChart,
                            categorias,
                            valores
                    );
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<VicoResponse> call,
                    @NonNull Throwable t
            ) {
                Log.e("GRAFICO_CARTEIRA", "Erro ao carregar distribuição", t);
            }
        });
    }

    private void carregarInvestimentos() {
        AporteRepository aporteRepository = new AporteRepository(requireContext());

        aporteRepository.buscarAportes(new AporteRepository.aporteCallback() {
            @Override
            public void onSucesso(List<Aporte> aportes) {
                recyclerInvestimentos.setAdapter(new AporteAdapter(aportes));
            }

            @Override
            public void onErro(String mensagem) {
                Log.e("APORTES_TESTE", "Erro: " + mensagem);

                if (!isAdded()) {
                    return;
                }

                Toast.makeText(
                        requireContext(),
                        "Erro ao carregar aportes.",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}
