package com.example.accurancymobileapp.fragments;

import static android.widget.Toast.LENGTH_LONG;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.compose.ui.platform.ComposeView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.activities.LoginActivity;
import com.example.accurancymobileapp.adapter.EmphasisAdapter;
import com.example.accurancymobileapp.model.clsAportes;
import com.example.accurancymobileapp.model.clsGrafic;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.network.service.ApiService;
import com.example.accurancymobileapp.network.service.VicoService;
import com.example.accurancymobileapp.response.ApiResponse;
import com.example.accurancymobileapp.response.VicoResponse;
import com.example.accurancymobileapp.ui.ChartHelper;
import com.example.accurancymobileapp.utils.SessionManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DashboardFragment extends Fragment {

    private ComposeView ctvChart;
    RecyclerView recyclerInvestimentos;



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

    private void dashboard(){

        recyclerInvestimentos.setLayoutManager(new LinearLayoutManager(requireContext()));

        ApiService api = RetrofitClient
                .getClient(requireContext())
                .create(ApiService.class);

        api.getAportes().enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                if (response.isSuccessful() && response.body() != null) {

                    List<clsAportes> ativo = response.body().getLista();

                    if (ativo == null) {
                        Log.e("ERRO", "A lista não mostrou nada");
                        return;
                    }

                    EmphasisAdapter adapter = new EmphasisAdapter(ativo);
                    recyclerInvestimentos.setAdapter(adapter);
                    return;
                }
                Log.e("ERRO","Erro HTTP aportes: " + response.code() + " " + response.message());
                Toast.makeText(requireContext(),"Erro ao carregar aportes",LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {

                Toast.makeText(requireContext(),"Erro ao mostrar aportes",LENGTH_LONG).show();
                Log.e("ERRO", "msg: " + t.getMessage());
            }
        });
    };

    private void carregarGrafico(){

        VicoService vico = RetrofitClient.getClient(requireContext()).create(VicoService.class);
        vico.getGrafic().enqueue(new Callback<VicoResponse>() {
            @Override
            public void onResponse(Call<VicoResponse> call, Response<VicoResponse> response) {

                if (!response.isSuccessful()) {
                    String corpo = "N/A";
                    try {
                        if (response.errorBody() != null) {
                            corpo = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        corpo = "erro ao ler body: " + e.getMessage();
                    }
                    Log.e("ERRO", "Erro HTTP " + response.code() + " - corpo: " + corpo);
                    return;
                }

                if (response.body() == null) {
                    Log.e("ERRO", "BODY NULL");
                    return;
                }

                if (response.body().getResults() == null) {
                    Log.e("ERRO", "RESULTS NULL");
                    return;
                }

                ArrayList<Number> valores = new ArrayList<>();

                for (clsGrafic item : response.body().getResults()) {
                    if (item != null) {
                        valores.add(item.getPreco());
                    }
                }

                if (!valores.isEmpty()) {
                    ChartHelper.GraphicConfig(ctvChart, valores);
                }
            }

            @Override
            public void onFailure(Call<VicoResponse> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }

                Log.e("ERRO","msg " + t.getMessage());
                Toast.makeText(requireContext(),"Erro: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}