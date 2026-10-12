package com.example.accurancymobileapp.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.compose.ui.platform.ComposeView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.adapter.AporteAdapter;
import com.example.accurancymobileapp.adapter.HistoricAdapter;
import com.example.accurancymobileapp.model.Aporte;
import com.example.accurancymobileapp.network.repository.AporteRepository;
import com.example.accurancymobileapp.network.repository.HistoricRepository;
import com.example.accurancymobileapp.network.repository.QuoteRepository;
import com.example.accurancymobileapp.ui.ChartHelper;

import org.w3c.dom.Text;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class HistoricFragment extends Fragment {
    RecyclerView recyclerHistorico;
    TextView btnTodos;
    TextView btnCompra;
    TextView btnVenda;
    ComposeView graficoHistorico;
    private List<Aporte> listaHistorico = new ArrayList<>();
    private HistoricAdapter adapter;

    public HistoricFragment(){
        super(R.layout.fragment_historic);
    }
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        recyclerHistorico = view.findViewById(R.id.recyclerHistorico);
        recyclerHistorico.setLayoutManager(new LinearLayoutManager(requireContext()));
        btnTodos = view.findViewById(R.id.btnTodos);
        btnCompra = view.findViewById(R.id.btnCompra);
        btnVenda = view.findViewById(R.id.btnVenda);
        graficoHistorico = view.findViewById(R.id.graficoHistorico);


        btnTodos.setOnClickListener(v -> {
            filtrarPorTipo("Todos");

            selecionarFiltro(btnTodos);
        });

        btnCompra.setOnClickListener(v -> {
            filtrarPorTipo("Compra");

            selecionarFiltro(btnCompra);
        });

        btnVenda.setOnClickListener(v -> {
            filtrarPorTipo("Venda");

            selecionarFiltro(btnVenda);
        });

        carregarHistorico();
    }

    public void carregarHistorico(){
        HistoricRepository historicRepository = new HistoricRepository(requireContext());

        historicRepository.buscarHistorico(new HistoricRepository.HistoricCallback() {
            @Override
            public void OnSuccess(List<Aporte> historico) {
                listaHistorico = historico;

                atualizarGrafico(listaHistorico);

                adapter = new HistoricAdapter(requireContext(), listaHistorico);

                recyclerHistorico.setAdapter(adapter);
            }

            @Override
            public void onError(String message) {

                if(message == null || message.trim().isEmpty()){
                    message = "Não foi possível buscar o histórico";
                }

                Toast.makeText(requireContext(), "Erro ao buscar o histórico", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void filtrarPorTipo(String tipo){
        List<Aporte> listaFiltrada = new ArrayList<>();

        if(tipo.equals("Todos")){
            listaFiltrada.addAll(listaHistorico);
        }
        else{
            for(Aporte aporte : listaHistorico){
                if(aporte.getTipoAporte() != null && aporte.getTipoAporte().equalsIgnoreCase(tipo)){
                    listaFiltrada.add(aporte);
                }
            }
        }

        adapter.atualizarLista(listaFiltrada);
    }

    private void selecionarFiltro(TextView selecionado){
        selecionado.setBackgroundResource(R.drawable.bg_btn_primary);

        TextView[] filtros = {
                btnTodos,
                btnCompra,
                btnVenda
        };
        for(TextView filtro : filtros){
            if (filtro == selecionado) {
                filtro.setBackgroundResource(R.drawable.bg_btn_primary);
            } else {
                filtro.setBackgroundResource(R.drawable.bg_icon_sair);
            }
        }

    }

    private void graficHistoric(){
        List<String> meses = Arrays.asList(
                "Jan", "Fev", "Mar", "Abr", "Mai", "Jun"
        );

        List<Double> compras = Arrays.asList(
                320.0, 470.0, 210.0, 550.0, 380.0, 720.0
        );

        List<Double> vendas = Arrays.asList(
                -30.0, -40.0, -130.0, -25.0, -55.0, -60.0
        );

        ChartHelper.configurarGraficoHistorico(
                graficoHistorico,
                meses,
                compras,
                vendas
        );
    }

    private void atualizarGrafico(List<Aporte> aportes) {
        Map<YearMonth, double[]> valoresPorMes = new TreeMap<>();

        DateTimeFormatter formatoData =
                DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Aporte aporte : aportes) {
            try {
                LocalDate data = LocalDate.parse(
                        aporte.getData_aporte(),
                        formatoData
                );

                YearMonth mes = YearMonth.from(data);

                double valor = aporte.getValorAporte();


                double[] valores = valoresPorMes.computeIfAbsent(
                        mes, chave -> new double[2]
                );

                if ("Compra".equalsIgnoreCase(aporte.getTipoAporte())) {
                    valores[0] += valor;
                } else if ("Venda".equalsIgnoreCase(aporte.getTipoAporte())) {
                    valores[1] -= valor;
                }

            } catch (Exception e) {
                Log.e("GraficoHistorico",
                        "Erro ao processar aporte", e);
            }
        }

        List<String> meses = new ArrayList<>();
        List<Double> compras = new ArrayList<>();
        List<Double> vendas = new ArrayList<>();

        DateTimeFormatter formatoMes =
                DateTimeFormatter.ofPattern("MMM", new Locale("pt", "BR"));

        for (Map.Entry<YearMonth, double[]> entrada
                : valoresPorMes.entrySet()) {

            meses.add(
                    entrada.getKey()
                            .atDay(1)
                            .format(formatoMes)
            );

            compras.add(entrada.getValue()[0]);
            vendas.add(entrada.getValue()[1]);
        }

        ComposeView grafico =
                requireView().findViewById(R.id.graficoHistorico);

        ChartHelper.configurarGraficoHistorico(
                grafico,
                meses,
                compras,
                vendas
        );
    }
}