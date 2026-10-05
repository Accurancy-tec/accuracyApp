package com.example.accurancymobileapp.fragments;

import static android.widget.Toast.LENGTH_LONG;

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
import com.example.accurancymobileapp.adapter.EmphasisAdapter;
import com.example.accurancymobileapp.model.clsAportes;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.network.service.ApiService;
import com.example.accurancymobileapp.network.service.VicoService;
import com.example.accurancymobileapp.response.ApiResponse;
import com.example.accurancymobileapp.response.VicoResponse;
import com.example.accurancymobileapp.ui.ChartHelper;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {

    private ComposeView ctvChart;
    private RecyclerView recyclerInvestimentos;

    public DashboardFragment() {
        super(R.layout.fragment_dashboard);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ctvChart = view.findViewById(R.id.ctvChart);
        recyclerInvestimentos = view.findViewById(R.id.recyclerInvestimentos);

        carregarGrafico();
        dashboard();
    }

    private void dashboard() {
        recyclerInvestimentos.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        ApiService api = RetrofitClient
                .getClient(requireContext())
                .create(ApiService.class);

        api.getAportes().enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<ApiResponse> call,
                    @NonNull Response<ApiResponse> response
            ) {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e("DASHBOARD", "Erro HTTP aportes: " + response.code());
                    return;
                }

                List<clsAportes> aportes = response.body().getLista();
                if (aportes == null) {
                    Log.e("DASHBOARD", "Lista de aportes nula");
                    return;
                }

                recyclerInvestimentos.setAdapter(new EmphasisAdapter(aportes));
            }

            @Override
            public void onFailure(
                    @NonNull Call<ApiResponse> call,
                    @NonNull Throwable t
            ) {
                if (!isAdded()) {
                    return;
                }

                Toast.makeText(
                        requireContext(),
                        "Erro ao mostrar aportes",
                        LENGTH_LONG
                ).show();
                Log.e("DASHBOARD", "Erro ao carregar aportes", t);
            }
        });
    }

    private void carregarGrafico() {
        VicoService vico = RetrofitClient
                .getClient(requireContext())
                .create(VicoService.class);

        vico.getEvolucao("6M").enqueue(new Callback<VicoResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<VicoResponse> call,
                    @NonNull Response<VicoResponse> response
            ) {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e("GRAFICO_DASH", "Erro HTTP: " + response.code());
                    return;
                }

                VicoResponse body = response.body();
                if (!body.isSucesso() || body.getPontos() == null || body.getPontos().isEmpty()) {
                    Log.e("GRAFICO_DASH", "Sem dados: " + body.getMensagem());
                    return;
                }

                List<String> datas = new ArrayList<>();
                List<Double> valores = new ArrayList<>();

                for (VicoResponse.Ponto ponto : body.getPontos()) {
                    if (ponto == null) {
                        continue;
                    }

                    datas.add(ponto.getData());
                    valores.add(ponto.getValor());
                }

                if (!valores.isEmpty()) {
                    ChartHelper.configurarGraficoEvolucao(
                            ctvChart,
                            datas,
                            valores
                    );
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<VicoResponse> call,
                    @NonNull Throwable t
            ) {
                Log.e("GRAFICO_DASH", "Erro ao carregar evolução", t);
            }
        });
    }
}
