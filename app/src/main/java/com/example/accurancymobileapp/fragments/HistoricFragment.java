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
import android.widget.Toast;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.adapter.AporteAdapter;
import com.example.accurancymobileapp.model.Aporte;
import com.example.accurancymobileapp.network.repository.AporteRepository;
import com.example.accurancymobileapp.network.repository.HistoricRepository;
import com.example.accurancymobileapp.network.repository.QuoteRepository;

import java.util.List;

public class HistoricFragment extends Fragment {
    RecyclerView recyclerHistorico;

    public HistoricFragment(){
        super(R.layout.fragment_historic);
    }
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        recyclerHistorico = view.findViewById(R.id.recyclerHistorico);
        recyclerHistorico.setLayoutManager(new LinearLayoutManager(requireContext()));


        carregarHistorico();
    }

    public void carregarHistorico(){
        HistoricRepository historicRepository = new HistoricRepository(requireContext());

        historicRepository.buscarHistorico(new HistoricRepository.HistoricCallback() {
            @Override
            public void OnSuccess(List<Aporte> historico) {

                AporteAdapter adapter = new AporteAdapter(historico);

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
}