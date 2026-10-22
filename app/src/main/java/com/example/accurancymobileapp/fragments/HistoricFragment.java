package com.example.accurancymobileapp.fragments;

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

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.adapter.HistoricAdapter;
import com.example.accurancymobileapp.model.Aporte;
import com.example.accurancymobileapp.network.repository.HistoricRepository;
import com.example.accurancymobileapp.ui.ChartHelper;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistoricFragment extends Fragment {

    private RecyclerView recyclerHistorico;
    private TextView btnCompra, btnVenda, btnTodos;
    private TextView btnSemana, btnMes, btnAno;
    private ComposeView graficoHistorico;
    private final List<Aporte> listaHistorico = new ArrayList<>();
    private HistoricAdapter adapter;

    private enum PeriodoHistorico { SEMANA, MES, ANO }
    private PeriodoHistorico periodoSelecionado = PeriodoHistorico.SEMANA;
    private String tipoSelecionado = "Todos";
    private final Locale ptBR = Locale.forLanguageTag("pt-BR");

    public HistoricFragment() { super(R.layout.fragment_historic); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerHistorico = view.findViewById(R.id.recyclerHistorico);
        recyclerHistorico.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerHistorico.setNestedScrollingEnabled(false);
        adapter = new HistoricAdapter(requireContext(), new ArrayList<>());
        recyclerHistorico.setAdapter(adapter);

        btnTodos = view.findViewById(R.id.btnTodos);
        btnCompra = view.findViewById(R.id.btnCompra);
        btnVenda = view.findViewById(R.id.btnVenda);
        btnSemana = view.findViewById(R.id.btnSemana);
        btnMes = view.findViewById(R.id.btnMes);
        btnAno = view.findViewById(R.id.btnAno);
        graficoHistorico = view.findViewById(R.id.graficoHistorico);

        btnSemana.setOnClickListener(v -> selecionarPeriodo(PeriodoHistorico.SEMANA, btnSemana));
        btnMes.setOnClickListener(v -> selecionarPeriodo(PeriodoHistorico.MES, btnMes));
        btnAno.setOnClickListener(v -> selecionarPeriodo(PeriodoHistorico.ANO, btnAno));
        btnTodos.setOnClickListener(v -> selecionarTipo("Todos", btnTodos));
        btnCompra.setOnClickListener(v -> selecionarTipo("Compra", btnCompra));
        btnVenda.setOnClickListener(v -> selecionarTipo("Venda", btnVenda));

        selecionarFiltroGrafico(btnSemana);
        selecionarFiltro(btnTodos);
        carregarHistorico();
    }

    private void carregarHistorico() {
        new HistoricRepository(requireContext()).buscarHistorico(new HistoricRepository.HistoricCallback() {
            @Override
            public void OnSuccess(List<Aporte> historico) {
                if (!isAdded()) return;
                listaHistorico.clear();
                if (historico != null) listaHistorico.addAll(historico);
                aplicarFiltros();
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                Log.e("HISTORICO_APORTES", message == null ? "Erro sem mensagem" : message);
                Toast.makeText(requireContext(), "Não foi possível carregar o histórico.", Toast.LENGTH_SHORT).show();
                aplicarFiltros();
            }
        });
    }

    private void selecionarPeriodo(PeriodoHistorico periodo, TextView selecionado) {
        periodoSelecionado = periodo;
        selecionarFiltroGrafico(selecionado);
        aplicarFiltros();
    }

    private void selecionarTipo(String tipo, TextView selecionado) {
        tipoSelecionado = tipo;
        selecionarFiltro(selecionado);
        aplicarFiltros();
    }

    private void aplicarFiltros() {
        List<Aporte> doPeriodo = filtrarHistoricoPorPeriodo(listaHistorico, periodoSelecionado);
        List<Aporte> paraLista = new ArrayList<>();
        for (Aporte aporte : doPeriodo) {
            if ("Todos".equalsIgnoreCase(tipoSelecionado)
                    || (aporte.getTipoAporte() != null && aporte.getTipoAporte().equalsIgnoreCase(tipoSelecionado))) {
                paraLista.add(aporte);
            }
        }
        adapter.atualizarLista(paraLista);
        atualizarGrafico(doPeriodo);
    }

    private List<Aporte> filtrarHistoricoPorPeriodo(List<Aporte> historico, PeriodoHistorico periodo) {
        LocalDate hoje = LocalDate.now();
        LocalDate inicio;
        switch (periodo) {
            case SEMANA: inicio = hoje.minusDays(6); break;
            case ANO: inicio = LocalDate.of(hoje.getYear(), 1, 1); break;
            case MES:
            default: inicio = hoje.withDayOfMonth(1); break;
        }
        List<Aporte> filtrados = new ArrayList<>();
        for (Aporte aporte : historico) {
            try {
                if (aporte.getData_aporte() == null) continue;
                LocalDate data = LocalDate.parse(aporte.getData_aporte(), DateTimeFormatter.ISO_LOCAL_DATE);
                if (!data.isBefore(inicio) && !data.isAfter(hoje)) filtrados.add(aporte);
            } catch (Exception erro) {
                Log.w("HISTORICO_DATA", "Data de aporte inválida; registro ignorado.");
            }
        }
        return filtrados;
    }

    private void selecionarFiltro(TextView selecionado) {
        TextView[] filtros = {btnTodos, btnCompra, btnVenda};
        for (TextView filtro : filtros) {
            filtro.setBackgroundResource(filtro == selecionado ? R.drawable.bg_btn_primary : R.drawable.bg_icon_sair);
        }
    }

    private void selecionarFiltroGrafico(TextView selecionado) {
        TextView[] filtros = {btnSemana, btnMes, btnAno};
        for (TextView filtro : filtros) {
            filtro.setBackgroundResource(filtro == selecionado ? R.drawable.bg_btn_primary : R.drawable.bg_icon_sair);
        }
    }

    private void atualizarGrafico(List<Aporte> aportes) {
        LocalDate hoje = LocalDate.now();
        List<String> rotulos = new ArrayList<>();
        List<Double> compras = new ArrayList<>();
        List<Double> vendas = new ArrayList<>();
        Map<String, double[]> valoresPorPeriodo = new LinkedHashMap<>();
        DateTimeFormatter formatoMes = DateTimeFormatter.ofPattern("MMM", ptBR);

        if (periodoSelecionado == PeriodoHistorico.SEMANA) {
            for (int i = 6; i >= 0; i--) {
                LocalDate dia = hoje.minusDays(i);
                rotulos.add(dia.getDayOfWeek().getDisplayName(TextStyle.SHORT, ptBR).replace(".", ""));
                valoresPorPeriodo.put(dia.toString(), new double[2]);
            }
        } else if (periodoSelecionado == PeriodoHistorico.MES) {
            LocalDate primeiroDia = hoje.withDayOfMonth(1);
            for (LocalDate dia = primeiroDia; !dia.isAfter(hoje); dia = dia.plusDays(1)) {
                rotulos.add(String.valueOf(dia.getDayOfMonth()));
                valoresPorPeriodo.put(dia.toString(), new double[2]);
            }
        } else {
            for (int mes = 1; mes <= 12; mes++) {
                YearMonth ym = YearMonth.of(hoje.getYear(), mes);
                rotulos.add(ym.format(formatoMes));
                valoresPorPeriodo.put(ym.toString(), new double[2]);
            }
        }

        for (Aporte aporte : aportes) {
            try {
                LocalDate data = LocalDate.parse(aporte.getData_aporte());
                String chave = periodoSelecionado == PeriodoHistorico.ANO ? YearMonth.from(data).toString() : data.toString();
                double[] valores = valoresPorPeriodo.get(chave);
                if (valores == null) continue;
                double valor = aporte.getValorAporte();
                String tipo = aporte.getTipoAporte();
                if ("Compra".equalsIgnoreCase(tipo)) valores[0] += valor;
                else if ("Venda".equalsIgnoreCase(tipo)) valores[1] += valor;
            } catch (Exception erro) {
                Log.w("HISTORICO_GRAFICO", "Não foi possível agregar uma transação ao gráfico.");
            }
        }

        for (double[] valores : valoresPorPeriodo.values()) {
            compras.add(valores[0]);
            vendas.add(valores[1]);
        }
        ChartHelper.configurarGraficoHistorico(graficoHistorico, rotulos, compras, vendas);
    }

}
