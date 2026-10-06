package com.example.accurancymobileapp.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.adapter.AporteAdapter;
import com.example.accurancymobileapp.model.Aporte;
import com.example.accurancymobileapp.network.repository.AporteRepository;
import com.example.accurancymobileapp.network.repository.HistoricRepository;
import com.example.accurancymobileapp.network.repository.QuoteRepository;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class HistoricFragment extends Fragment {
    RecyclerView recyclerHistorico;
    TextView btnTodos;
    TextView btnCompra;
    TextView btnVenda;
    private List<Aporte> listaHistorico = new ArrayList<>();
    private AporteAdapter adapter;

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

                adapter = new AporteAdapter(listaHistorico);

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
}