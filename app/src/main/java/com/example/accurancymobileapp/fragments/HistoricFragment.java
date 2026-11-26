package com.example.accurancymobileapp.fragments;

import com.example.accurancymobileapp.utils.PuxarParaAtualizar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.compose.ui.platform.ComposeView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.adapter.HistoricAdapter;
import com.example.accurancymobileapp.adapter.EsqueletoAdapter;
import com.example.accurancymobileapp.utils.Esqueleto;
import com.example.accurancymobileapp.model.Aporte;
import com.example.accurancymobileapp.network.repository.HistoricRepository;
import com.example.accurancymobileapp.ui.ChartHelper;
import com.example.accurancymobileapp.utils.Aviso;

import java.text.NumberFormat;
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
    private View esqueletoGrafico;
    private boolean carregouAlgumaVez = false;
    private final List<Aporte> listaHistorico = new ArrayList<>();
    private HistoricAdapter adapter;
    private PuxarParaAtualizar puxar;
    private boolean primeiraRetomada = true;

    private enum PeriodoHistorico { SEMANA, MES, ANO }
    private PeriodoHistorico periodoSelecionado = PeriodoHistorico.SEMANA;
    private String tipoSelecionado = "Todos";
    private final Locale ptBR = Locale.forLanguageTag("pt-BR");

    public HistoricFragment() { super(R.layout.fragment_historic); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        puxar = new PuxarParaAtualizar(view.findViewById(R.id.swipeHistorico), this::carregarHistorico);
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
        esqueletoGrafico = view.findViewById(R.id.esqueletoGraficoHistorico);

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

    @Override
    public void onResume() {
        super.onResume();
        // A purchase, sale or dividend made on another screen shows up when coming back.
        if (primeiraRetomada) {
            primeiraRetomada = false;
            return;
        }
        carregarHistorico();
    }

    /** First load (or a reload with nothing on screen): placeholder rows and chart bar. */
    private void mostrarCarregando() {
        if (carregouAlgumaVez || puxar.atualizando()) return;
        EsqueletoAdapter.mostrar(recyclerHistorico, R.layout.item_esqueleto_linha, 6);
        graficoHistorico.setVisibility(View.INVISIBLE);
        esqueletoGrafico.setVisibility(View.VISIBLE);
    }

    private void terminarCarregando() {
        carregouAlgumaVez = true;
        EsqueletoAdapter.ocultar(recyclerHistorico, adapter);
        if (esqueletoGrafico.getVisibility() == View.VISIBLE) {
            esqueletoGrafico.setVisibility(View.GONE);
            graficoHistorico.setVisibility(View.VISIBLE);
            Esqueleto.revelar(graficoHistorico);
        }
    }

    private void carregarHistorico() {
        mostrarCarregando();
        puxar.iniciar();
        new HistoricRepository(requireContext()).buscarHistorico(new HistoricRepository.HistoricCallback() {
            @Override
            public void OnSuccess(List<Aporte> historico) {
                puxar.concluir();
                if (!isAdded()) return;
                listaHistorico.clear();
                if (historico != null) listaHistorico.addAll(historico);
                aplicarFiltros();
                terminarCarregando();
            }

            @Override
            public void onError(String message) {
                puxar.concluir();
                if (!isAdded()) return;
                Log.e("HISTORICO_APORTES", message == null ? "Erro sem mensagem" : message);
                Aviso.makeText(requireContext(), "Não foi possível carregar o histórico.", Toast.LENGTH_SHORT).show();
                aplicarFiltros();
                terminarCarregando();
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
        atualizarResumo(doPeriodo);
        atualizarGrafico(doPeriodo);
    }

    /** Cartões do topo: totais das operações do período escolhido (todas as carteiras). */
    private void atualizarResumo(List<Aporte> doPeriodo) {
        View raiz = getView();
        if (raiz == null) return;
        double compras = 0, vendas = 0, dividendos = 0;
        int qtdCompras = 0, qtdVendas = 0, qtdDividendos = 0;
        for (Aporte aporte : doPeriodo) {
            String tipo = aporte.getTipoAporte() == null ? "" : aporte.getTipoAporte();
            double valor = aporte.getValorAporte();
            if ("Compra".equalsIgnoreCase(tipo)) {
                compras += valor;
                qtdCompras++;
            } else if ("Venda".equalsIgnoreCase(tipo)) {
                vendas += valor;
                qtdVendas++;
            } else if ("Dividendo".equalsIgnoreCase(tipo)) {
                dividendos += valor;
                qtdDividendos++;
            }
        }
        NumberFormat moeda = NumberFormat.getCurrencyInstance(ptBR);
        ((TextView) raiz.findViewById(R.id.tvHistTotalCompras)).setText(moeda.format(compras));
        ((TextView) raiz.findViewById(R.id.tvHistQtdCompras)).setText(contagem(qtdCompras, "compra", "compras", "nenhuma no período"));
        ((TextView) raiz.findViewById(R.id.tvHistTotalVendas)).setText(moeda.format(vendas));
        ((TextView) raiz.findViewById(R.id.tvHistQtdVendas)).setText(contagem(qtdVendas, "venda", "vendas", "nenhuma no período"));
        ((TextView) raiz.findViewById(R.id.tvHistTotalDividendos)).setText(moeda.format(dividendos));
        ((TextView) raiz.findViewById(R.id.tvHistQtdDividendos)).setText(contagem(qtdDividendos, "recebido", "recebidos", "nenhum no período"));
        ((TextView) raiz.findViewById(R.id.tvHistTotalOperacoes)).setText(String.valueOf(doPeriodo.size()));
        ((TextView) raiz.findViewById(R.id.tvHistPeriodoOperacoes)).setText(
                periodoSelecionado == PeriodoHistorico.SEMANA ? "nos últimos 7 dias"
                        : periodoSelecionado == PeriodoHistorico.MES ? "neste mês" : "neste ano");
    }

    private static String contagem(int quantidade, String singular, String plural, String nenhum) {
        return quantidade == 0 ? nenhum : quantidade + " " + (quantidade == 1 ? singular : plural);
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
        marcar(new TextView[]{btnTodos, btnCompra, btnVenda}, selecionado);
    }

    private void selecionarFiltroGrafico(TextView selecionado) {
        marcar(new TextView[]{btnSemana, btnMes, btnAno}, selecionado);
    }

    /** Selecionado: fundo azul e texto branco; os outros: sem fundo e texto secundário. */
    private void marcar(TextView[] filtros, TextView selecionado) {
        for (TextView filtro : filtros) {
            boolean marcado = filtro == selecionado;
            filtro.setBackgroundResource(marcado ? R.drawable.bg_btn_primary : R.drawable.bg_icon_sair);
            filtro.setTextColor(ContextCompat.getColor(requireContext(), marcado ? R.color.white : R.color.text_secondary));
            filtro.setSelected(marcado);
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
