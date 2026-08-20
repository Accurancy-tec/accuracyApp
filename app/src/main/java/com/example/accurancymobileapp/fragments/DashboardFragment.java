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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.activities.Dashboard;
import com.example.accurancymobileapp.activities.LoginActivity;
import com.example.accurancymobileapp.adapter.EmphasisAdapter;
import com.example.accurancymobileapp.model.clsAportes;
import com.example.accurancymobileapp.model.clsGrafic;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.network.repository.QuoteRepository;
import com.example.accurancymobileapp.network.service.ApiService;
import com.example.accurancymobileapp.network.service.VicoService;
import com.example.accurancymobileapp.response.ApiResponse;
import com.example.accurancymobileapp.response.VicoResponse;
import com.example.accurancymobileapp.ui.ChartHelper;
import com.example.accurancymobileapp.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DashboardFragment extends Fragment {

    private ComposeView ctvChart;
    Button btnDeslogar;
    RecyclerView recyclerInvestimentos;


    public DashboardFragment() {
        super(R.layout.fragment_dashboard);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        ctvChart = view.findViewById(R.id.ctvChart);
        btnDeslogar = view.findViewById(R.id.btnDeslogar);
        recyclerInvestimentos = view.findViewById(R.id.recyclerInvestimentos);

        carregarGrafico();
        dashboard();

    }

    private void dashboard(){

        //Volta para a tela de login
        btnDeslogar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutSession();
            }
        });

        recyclerInvestimentos.setLayoutManager(new LinearLayoutManager(requireContext()));

        ApiService api = RetrofitClient
                .getClient(requireContext())
                .create(ApiService.class);

        api.getAportes().enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                if(response.isSuccessful() && response.body() != null ){

                    List<clsAportes> ativo = response.body().getLista();

                    EmphasisAdapter adapter = new EmphasisAdapter(ativo);

                    recyclerInvestimentos.setAdapter(adapter);

                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(requireContext(),
                        "Nenhuma resposta chegou",
                        LENGTH_LONG).show();
            }
        });
    };

    private void carregarGrafico(){

        VicoService vico = RetrofitClient.getClient(requireContext()).create(VicoService.class);
        vico.getGrafic().enqueue(new Callback<VicoResponse>() {
            @Override
            public void onResponse(Call<VicoResponse> call, Response<VicoResponse> response
            ) {
                // Evita que o require contexto seja chamado depois que o fragment foi removido
                if (!isAdded()) {
                    return;
                }

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(requireContext(),"Erro ao carregar o gráfico", Toast.LENGTH_LONG).show();
                    return;
                }

                if (response.body().getResults() == null) {
                    Toast.makeText(
                            requireContext(),"A API não retornou dados", Toast.LENGTH_LONG).show();
                    return;
                }

                ArrayList<Number> valores = new ArrayList<>();

                for (clsGrafic item : response.body().getResults()) {
                    if (item != null) {
                        valores.add(item.getPreco());
                    }
                }

                if (!valores.isEmpty() && ctvChart != null) {
                    ChartHelper.GraphicConfig(ctvChart, valores);
                }

                Toast.makeText(requireContext(), "Quantidade: " + valores.size(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<VicoResponse> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }

                Toast.makeText(requireContext(),"Erro: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void logoutSession(){
        SessionManager sessionManager = new SessionManager(requireContext());

        sessionManager.logout();

        Intent it = new Intent(requireContext(), LoginActivity.class);
        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(it);

    }
}