package com.example.accurancymobileapp.fragments;

import static android.widget.Toast.LENGTH_LONG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.activities.DashboardActivity;
import com.example.accurancymobileapp.model.ActiveSpinner;
import com.example.accurancymobileapp.model.QuoteData;
import com.example.accurancymobileapp.model.QuoteResult;
import com.example.accurancymobileapp.model.clsAportes;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.network.service.ApiService;
import com.example.accurancymobileapp.response.ApiResponse;
import com.example.accurancymobileapp.response.QuoteResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AportesFragment extends Fragment {

    Spinner spnAtivo, spnTipo, spnRecorrencia,spnCategoria;
    EditText txtPreco,txtQuantidade;
    Button btnEnviar;

    List<ActiveSpinner> active = new ArrayList<>();
    ArrayAdapter<ActiveSpinner> adapterActive;


    public AportesFragment() {
        super(R.layout.fragment_aportes);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        spnAtivo = (Spinner) view.findViewById(R.id.spnAtivo);
        spnRecorrencia = (Spinner) view.findViewById(R.id.spnRecorrencia);
        spnTipo = (Spinner) view.findViewById(R.id.spnTipo);
        spnCategoria = (Spinner) view.findViewById(R.id.spnCategoria);
        btnEnviar = (Button) view.findViewById(R.id.btnEnviar);
        txtPreco = (EditText) view.findViewById(R.id.txtPreco);
        txtQuantidade = (EditText) view.findViewById(R.id.txtQuantidade);

        String[] Categoria = {"Ações","Cripto","Renda Fixa","FIIs","Internacional"};
        String[] Tipo = {"Escolha o Tipo", "Compra", "Venda","Dividendo"};
        String[] Recorrencia = {"Escolha a Recorrência", "Único","Diário", "Semanal", "Mensal", "Anual"};

        active.add(new ActiveSpinner("","Escolha o Ativo"));

        ArrayAdapter<String> adapterTipo = new ArrayAdapter<String>(requireContext(),
                R.layout.my_select_item,
                Tipo
        );
        ArrayAdapter<String> adapterRecorrencia = new ArrayAdapter<String>(requireContext(),
                R.layout.my_select_item,
                Recorrencia
        );
        ArrayAdapter<String> adapterCategoria = new ArrayAdapter<String>(requireContext(),
                R.layout.my_select_item,
                Categoria
        );

        adapterActive = new ArrayAdapter<>(requireContext(),
                R.layout.my_select_item,
                active);

        adapterTipo.setDropDownViewResource(R.layout.my_dropdown_item);
        adapterRecorrencia.setDropDownViewResource(R.layout.my_dropdown_item);
        adapterActive.setDropDownViewResource(R.layout.my_dropdown_item);
        adapterCategoria.setDropDownViewResource(R.layout.my_dropdown_item);

        spnTipo.setAdapter(adapterTipo);
        spnRecorrencia.setAdapter(adapterRecorrencia);
        spnAtivo.setAdapter(adapterActive);
        spnCategoria.setAdapter(adapterCategoria);

        carregarAtivos();
        aportes();

    }

    private void aportes() {

        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ActiveSpinner selectedActive = (ActiveSpinner) spnAtivo.getSelectedItem();

                String symbol = selectedActive.getSymbol();
                String name = selectedActive.getName();

                String Categoria = spnCategoria.getSelectedItem().toString();
                String SQuantidade = txtQuantidade.getText().toString();
                String Tipo = spnTipo.getSelectedItem().toString();
                String Recorrencia = spnRecorrencia.getSelectedItem().toString();
                String SPreco = txtPreco.getText().toString();

                if (symbol.isEmpty() ||name.equals("Escolha o Ativo") || SQuantidade.isEmpty() ||Tipo.equals("Escolha o Tipo") || Recorrencia.equals("Escolha a Recorrência") || SPreco.isEmpty()) {
                    Toast.makeText(requireContext(),
                            "Por favor preencha todos os campo",
                            LENGTH_LONG).show();
                    return;
                }
                Double Preco, Quantidade;
                try {
                    Quantidade = Double.parseDouble(SQuantidade);
                    Preco = Double.parseDouble(SPreco);
                }catch (NumberFormatException e) {

                    Toast.makeText(
                            requireContext(),
                            "Digite valores válidos",
                            LENGTH_LONG
                    ).show();
                    return;
                }

                ApiService api = RetrofitClient
                        .getClient(requireContext())
                        .create(ApiService.class);

                clsAportes aporte = new clsAportes(symbol,name,Categoria,Quantidade ,Preco, Tipo, Recorrencia);
                api.registerAporte(aporte).enqueue(new Callback<ApiResponse>() {

                    @Override
                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                        try {

                        if (response.isSuccessful() && response.body() != null) {

                            Toast.makeText(
                                    requireContext(),
                                    response.body().getMensagem(),
                                    LENGTH_LONG
                            ).show();
                            return;
                        }
                        Toast.makeText(requireContext(),
                                "Erro ao buscar a resposta da API",
                                LENGTH_LONG).show();
                        }catch (Exception e){
                        Log.e("Erro", "Erro " + e.getMessage());
                    } }

                    @Override
                    public void onFailure(Call<ApiResponse> call, Throwable t) {

                        Toast.makeText(
                                requireContext(),
                                "Erro: " + t.getMessage(),
                                LENGTH_LONG
                        ).show();

                        Log.e("Erro","Mensagem: " + t.getMessage());
                    }
                });
            }
        });
    }

    private void carregarAtivos() {
        ApiService service = RetrofitClient.getClient(requireContext()).create(ApiService.class);

        service.getService().enqueue(new Callback<QuoteResponse>() {

            @Override
            public void onResponse(Call<QuoteResponse> call, Response<QuoteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    for (QuoteData result : response.body().getData()) {
                        ActiveSpinner actives = new ActiveSpinner(result.getSymbol(), result.getLongName());
                        active.add(actives);
                    }

                    adapterActive.notifyDataSetChanged();
                    return;
                }
                Toast.makeText(requireContext(),
                        "Não achamos nada",
                        LENGTH_LONG).show();

            }

            @Override
            public void onFailure(Call<QuoteResponse> call, Throwable t) {
                Toast.makeText(requireContext(),
                        "Erro " + t.getMessage(),
                        LENGTH_LONG).show();
            }
        });
    }
}