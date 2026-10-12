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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class HistoricFragment extends Fragment {
    RecyclerView recyclerHistorico;
    TextView btnCompra, btnVenda, btnTodos;
    TextView btnSemana, btnMes, btnAno;
    ComposeView graficoHistorico;
    private List<Aporte> listaHistorico = new ArrayList<>();
    private List<Aporte> historicoCompleto = new ArrayList<>();
    private HistoricAdapter adapter;
    private enum PeriodoHistorico {
        SEMANA, MES, ANO
    }
    private PeriodoHistorico periodoSelecionado = PeriodoHistorico.SEMANA;

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
        btnSemana = view.findViewById(R.id.btnSemana);
        btnMes = view.findViewById(R.id.btnMes);
        btnAno = view.findViewById(R.id.btnAno);
        graficoHistorico = view.findViewById(R.id.graficoHistorico);

        //filtro grafico
        btnSemana.setOnClickListener(v -> {
            periodoSelecionado = PeriodoHistorico.SEMANA;
            aplicarFiltroHistorico();

            selecionarFiltroGrafico(btnSemana);
        });

        btnMes.setOnClickListener(v -> {
            periodoSelecionado = PeriodoHistorico.MES;
            aplicarFiltroHistorico();

            selecionarFiltroGrafico(btnMes);
        });

        btnAno.setOnClickListener(v -> {
            periodoSelecionado = PeriodoHistorico.ANO;
            aplicarFiltroHistorico();

            selecionarFiltroGrafico(btnAno);
        });

        //filtro historico
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

                aplicarFiltroHistorico();

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

        TextView[] filtrosHistorico = {
                btnTodos,
                btnCompra,
                btnVenda
        };

        for(TextView filtro : filtrosHistorico){
            if (filtro == selecionado) {
                filtro.setBackgroundResource(R.drawable.bg_btn_primary);
            } else {
                filtro.setBackgroundResource(R.drawable.bg_icon_sair);
            }
        }
    }

    private void selecionarFiltroGrafico(TextView selecionado){
        selecionado.setBackgroundResource(R.drawable.bg_btn_primary);

        TextView[] filtroGrafico = {
                btnSemana,
                btnMes,
                btnAno
        };

        for(TextView filtro : filtroGrafico){
            if(filtro == selecionado){
                filtro.setBackgroundResource(R.drawable.bg_btn_primary);
            }
            else {
                filtro.setBackgroundResource(R.drawable.bg_icon_sair);
            }
        }
    }

    private void atualizarGrafico(List<Aporte> aportes) {
        LocalDate hoje = LocalDate.now();

        List<String> rotulos = new ArrayList<>();
        List<Double> compras = new ArrayList<>();
        List<Double> vendas = new ArrayList<>();

        Map<String, double[]> valoresPorPeriodo = new LinkedHashMap<>();

        DateTimeFormatter formatoData =
                DateTimeFormatter.ISO_LOCAL_DATE;

        DateTimeFormatter formatoMes =
                DateTimeFormatter.ofPattern(
                        "MMM", new Locale("pt", "BR")
                );

        // Define os períodos que aparecerão no eixo X.
        if (periodoSelecionado == PeriodoHistorico.SEMANA) {
            for (int i = 6; i >= 0; i--) {
                LocalDate dia = hoje.minusDays(i);
                String chave = dia.toString();

                String diaSemana = dia.getDayOfWeek()
                        .getDisplayName(
                                java.time.format.TextStyle.SHORT,
                                new Locale("pt", "BR")
                        )
                        .replace(".", "");

                rotulos.add(diaSemana + ".");
                valoresPorPeriodo.put(chave, new double[2]);
            }

        } else if (periodoSelecionado == PeriodoHistorico.MES) {
            LocalDate primeiroDia = hoje.withDayOfMonth(1);

            for (LocalDate dia = primeiroDia;
                 !dia.isAfter(hoje);
                 dia = dia.plusDays(1)) {

                rotulos.add(String.valueOf(dia.getDayOfMonth()));
                valoresPorPeriodo.put(dia.toString(), new double[2]);
            }

        } else {
            for (int mes = 1; mes <= hoje.getMonthValue(); mes++) {
                YearMonth periodo = YearMonth.of(hoje.getYear(), mes);

                rotulos.add(
                        periodo.atDay(1).format(formatoMes)
                );

                valoresPorPeriodo.put(
                        periodo.toString(), new double[2]
                );
            }
        }

        // Soma compras e vendas em cada período.
        for (Aporte aporte : aportes) {
            try {
                LocalDate data = LocalDate.parse(
                        aporte.getData_aporte(), formatoData
                );

                if (data.isAfter(hoje)) {
                    continue;
                }

                String chave;

                if (periodoSelecionado == PeriodoHistorico.ANO) {
                    chave = YearMonth.from(data).toString();
                } else {
                    chave = data.toString();
                }

                double[] valores = valoresPorPeriodo.get(chave);

                if (valores == null) {
                    continue;
                }

                double valor = aporte.getValorAporte();

                if ("Compra".equalsIgnoreCase(
                        aporte.getTipoAporte())) {
                    valores[0] += valor;

                } else if ("Venda".equalsIgnoreCase(
                        aporte.getTipoAporte())) {
                    valores[1] -= valor;
                }

            } catch (Exception e) {
                Log.e("HistoricoGrafico",
                        "Erro ao processar aporte", e);
            }
        }

        // Monta as séries na mesma ordem dos rótulos.
        for (double[] valores : valoresPorPeriodo.values()) {
            compras.add(valores[0]);
            vendas.add(valores[1]);
        }

        ComposeView grafico =
                requireView().findViewById(R.id.graficoHistorico);

        ChartHelper.configurarGraficoHistorico(
                grafico, rotulos, compras, vendas
        );
    }

    private List<Aporte> filtrarHistorico(List<Aporte> historico, PeriodoHistorico periodo){
        LocalDate hoje = LocalDate.now();

        LocalDate inicio;

        switch(periodo){
            case SEMANA:
                inicio = hoje.minusDays(6);
                break;
            case ANO:
                inicio = LocalDate.of(hoje.getYear(), 1, 1);
                break;

            case MES:
            default:
                inicio = hoje.withDayOfMonth(1);
                break;

        }

        List<Aporte> filtrados = new ArrayList<>();

        for (Aporte aportes : historico){
            try {
                LocalDate data = LocalDate.parse(aportes.getData_aporte());

                if(!data.isBefore(inicio) && !data.isAfter(hoje)){
                    filtrados.add(aportes);
                }
            }
            catch(Exception e){
                Log.e("Historico filtrado", "Erro ao interpretar a data do aporte");
            }
        }

        return filtrados;
    }

    private void aplicarFiltroHistorico(){
        List<Aporte> filtrados = filtrarHistorico(listaHistorico, periodoSelecionado);
        historicoCompleto = filtrados;

        adapter = new HistoricAdapter(requireContext(), historicoCompleto);

        recyclerHistorico.setAdapter(adapter);

        atualizarGrafico(historicoCompleto);
    }
}