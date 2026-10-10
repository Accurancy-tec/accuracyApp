package com.example.accurancymobileapp.fragments;

import static android.widget.Toast.LENGTH_LONG;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.compose.ui.platform.ComposeView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.Spinner;
import com.example.accurancymobileapp.ui.AnimationSpinner;

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
import java.util.concurrent.atomic.AtomicReference;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {

    private ComposeView ctvChart;
    private TextView btn1M,btn6M,btn1A;
    private RecyclerView recyclerInvestimentos;
    private Spinner spFiltroDestaques;

    public DashboardFragment() {
        super(R.layout.fragment_dashboard);
    }
    String periodo = "6M";
    String[] filtros = {
            "Mais caros",
            "Mais lucrativos",
            "Maiores perdas"
    };

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        ctvChart = view.findViewById(R.id.ctvChart);
        recyclerInvestimentos = view.findViewById(R.id.recyclerInvestimentos);
        btn1M = view.findViewById(R.id.btn1M);
        btn6M = view.findViewById(R.id.btn6M);
        btn1A = view.findViewById(R.id.btn1A);
        spFiltroDestaques = view.findViewById(R.id.spFiltroDestaques);

        ctvChart.setVisibility(View.GONE);



        carregarGrafico(periodo);
        recicleView();
        timeInvested();


    }

    private void recicleView() {

        recyclerInvestimentos.setLayoutManager(new LinearLayoutManager(requireContext()));

        AnimationSpinner.configurar(
                requireContext(),
                spFiltroDestaques,
                filtros,
                posicao -> {
                    String type = posicao == 0 ? "Mais caros" : posicao == 1 ? "Mais lucrativos" : "Maiores perdas";
                    carregarDestques(type);
                }
        );
    }

    private void carregarDestques(String type){
        ApiService api = RetrofitClient.getClient(requireContext()).create(ApiService.class);

        api.buscarLista(type).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {

                if (!isAdded()) {
                    return;
                }

                if (!response.isSuccessful() || response.body() == null) {
                    String erro = "";
                    try {
                        if (response.errorBody() != null) erro = response.errorBody().string();
                    } catch (Exception ignored) {}
                    Log.e("DASHBOARD", "Erro HTTP aportes: " + response.code() + " | " + erro);
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
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {

                if (!isAdded()) {
                    return;
                }

                Toast.makeText(requireContext(), "Erro ao mostrar aportes", LENGTH_LONG).show();

                Log.e("DASHBOARD", "Erro ao carregar aportes", t);
            }
        }
        );
    }

    private void carregarGrafico(String periodo) {

        VicoService vico = RetrofitClient.getClient(requireContext()).create(VicoService.class);

        vico.getEvolucao(periodo).enqueue(new Callback<VicoResponse>() {

                    @Override
                    public void onResponse(@NonNull Call<VicoResponse> call, @NonNull Response<VicoResponse> response) {

                        if (!isAdded()) {
                            return;
                        }

                        if (!response.isSuccessful() || response.body() == null) {
                            Log.e("GRAFICO_DASH", "Erro HTTP: " + response.code());

                            esconderGrafico();
                            return;
                        }

                        VicoResponse body = response.body();

                        if (!body.isSucesso() || body.getPontos() == null || body.getPontos().isEmpty()) {
                            Log.d("GRAFICO_DASH", "Sem dados para gráfico: " + body.getMensagem());

                            esconderGrafico();
                            return;
                        }

                        List<String> datas = new ArrayList<>();

                        List<Double> valores = new ArrayList<>();

                        for (VicoResponse.Ponto ponto : body.getPontos()) {

                            if (ponto == null) {
                                continue;
                            }

                            String data = ponto.getData();

                            double valor = ponto.getValor();

                            if (data == null || data.trim().isEmpty()) {
                                continue;
                            }

                            if (Double.isNaN(valor) || Double.isInfinite(valor)) {
                                continue;
                            }

                            datas.add(data);
                            valores.add(valor);
                        }

                        int primeiroPontoReal = -1;

                        for (int i = 0; i < valores.size(); i++) {

                            if (valores.get(i) > 0.0) {
                                primeiroPontoReal = i;
                                break;
                            }
                        }

                        if (primeiroPontoReal == -1) {
                            esconderGrafico();
                            return;
                        }

                        if (primeiroPontoReal > 0) {

                            datas = new ArrayList<>(datas.subList(primeiroPontoReal, datas.size()));

                            valores = new ArrayList<>(valores.subList(primeiroPontoReal, valores.size()));
                        }

                        if (valores.size() < 2) {
                            Log.d("GRAFICO_DASH", "Menos de 2 pontos úteis");

                            esconderGrafico();
                            return;
                        }

                        boolean possuiValorReal = false;

                        for (Double valor : valores) {

                            if (valor != null && valor > 0.0) {
                                possuiValorReal = true;
                                break;
                            }
                        }

                        if (!possuiValorReal) {
                            esconderGrafico();
                            return;
                        }

                        ctvChart.setVisibility(View.VISIBLE);

                        ChartHelper.configurarGraficoEvolucao(
                                ctvChart,
                                datas,
                                valores
                        );
                    }

                    @Override
                    public void onFailure(@NonNull Call<VicoResponse> call, @NonNull Throwable t) {

                        if (!isAdded()) {
                            return;
                        }

                        Log.e("GRAFICO_DASH", "Erro ao carregar evolução", t);

                        esconderGrafico();
                    }
                }
        );
    }

    private void esconderGrafico() {

        if (ctvChart != null) {
            ctvChart.setVisibility(View.GONE);
        }
    }

    private void timeInvested(){

        btn1M.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                periodo = "1M";

                updateColorButtom(btn1M);

                carregarGrafico(periodo);
            }
        });

        btn6M.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                periodo = "6M";

                updateColorButtom(btn6M);
                carregarGrafico(periodo);
            }
        });

        btn1A.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                periodo = "1A";

                updateColorButtom(btn1A);
                carregarGrafico(periodo);
            }
        });
    }

    private void updateColorButtom(TextView btn){
        btn1M.setBackgroundResource(R.drawable.bg_periodo_normal);
        btn6M.setBackgroundResource(R.drawable.bg_periodo_normal);
        btn1A.setBackgroundResource(R.drawable.bg_periodo_normal);

        btn.setBackgroundResource(R.drawable.bg_periodo_selecionado);
    }

    private void name(){
        //TODO: adicionar logica para buscar nome do usuario
    }
}