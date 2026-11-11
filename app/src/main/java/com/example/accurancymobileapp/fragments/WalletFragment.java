package com.example.accurancymobileapp.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.compose.ui.platform.ComposeView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.adapter.AporteAdapter;
import com.example.accurancymobileapp.model.Aporte;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.network.repository.AporteRepository;
import com.example.accurancymobileapp.network.repository.WalletRepository;
import com.example.accurancymobileapp.network.service.VicoService;
import com.example.accurancymobileapp.response.VicoResponse;
import com.example.accurancymobileapp.ui.ChartHelper;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WalletFragment extends Fragment {

    private RecyclerView recyclerInvestimentos;
    private TextView txtCarteiraVazia;
    private ImageButton btnExcluirCarteira;
    WalletRepository walletRepository;

    public WalletFragment() {
        super(R.layout.fragment_wallet);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerInvestimentos = view.findViewById(R.id.recyclerInvestimentos);
        btnExcluirCarteira = view.findViewById(R.id.btnExcluirCarteira);
        txtCarteiraVazia = view.findViewById(R.id.txtCarteiraVazia);

        recyclerInvestimentos.setLayoutManager(new LinearLayoutManager(requireContext()));

        btnExcluirCarteira.setOnClickListener(v -> {
            excluirCarteira();
        });

        carregarInvestimentos();
    }

    private void carregarInvestimentos() {
        AporteRepository aporteRepository = new AporteRepository(requireContext());

        Bundle bundle = getArguments();
        int idCarteira = bundle.getInt("id_carteira");

        if(bundle != null){
            Log.d("CARTEIRA", "ID carteira recebido: " + idCarteira);
        }

        aporteRepository.buscarAportes(idCarteira, new AporteRepository.aporteCallback() {
            @Override
            public void onSucesso(List<Aporte> aportes) {
                if (!isAdded()) {
                    return;
                }
                if(aportes == null){
                    aportes = new ArrayList<>();
                }

                atualizarEstadoLista(aportes.size());

                recyclerInvestimentos.setAdapter(new AporteAdapter(aportes));
            }

            @Override
            public void onErro(String mensagem) {
                Log.e("APORTES_TESTE", "Erro: " + mensagem);

                if (!isAdded()) {
                    return;
                }

                Toast.makeText(
                        requireContext(),
                        "Erro ao carregar aportes.",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void atualizarEstadoLista(int quantidadeAportes){
        if (quantidadeAportes == 0) {
            recyclerInvestimentos.setVisibility(View.GONE);
            txtCarteiraVazia.setVisibility(View.VISIBLE);
        } else {
            recyclerInvestimentos.setVisibility(View.VISIBLE);
            txtCarteiraVazia.setVisibility(View.GONE);
        }
    }

    private void excluirCarteira(){
        walletRepository = new WalletRepository(requireContext());

        Bundle bundle = getArguments();

        if(bundle == null){
            Toast.makeText(requireContext(), "Não foi possível identificar a carteira", Toast.LENGTH_LONG).show();
            return;
        }

        int id_carteira = bundle.getInt("id_carteira", -1);

        if(id_carteira <= 0){
            Toast.makeText(requireContext(), "ID da carteira inválido.", Toast.LENGTH_LONG).show();
            return;
        }

        walletRepository.deletarWallet(id_carteira, new WalletRepository.WalletCallback() {
            @Override
            public void onSuccess(String mensagem) {
                if(!isAdded()) return;

                Toast.makeText(requireContext(), "Carteira excluida com sucesso", Toast.LENGTH_LONG).show();

                getParentFragmentManager().popBackStack();
            }

            @Override
            public void onError(String message) {
                if(!isAdded()) return;



                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
