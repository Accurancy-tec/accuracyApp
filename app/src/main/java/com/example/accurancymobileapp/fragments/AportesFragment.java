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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.activities.Aportes;
import com.example.accurancymobileapp.activities.Dashboard;
import com.example.accurancymobileapp.activities.LoginActivity;
import com.example.accurancymobileapp.activities.WalletActivity;
import com.example.accurancymobileapp.adapter.EmphasisAdapter;
import com.example.accurancymobileapp.model.QuoteResult;
import com.example.accurancymobileapp.model.clsAportes;
import com.example.accurancymobileapp.model.clsGrafic;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.network.service.ApiService;
import com.example.accurancymobileapp.network.service.VicoService;
import com.example.accurancymobileapp.response.ApiResponse;
import com.example.accurancymobileapp.response.QuoteResponse;
import com.example.accurancymobileapp.response.VicoResponse;
import com.example.accurancymobileapp.ui.ChartHelper;
import com.example.accurancymobileapp.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AportesFragment extends Fragment {

    Spinner spnAtivo, spnTipo, spnRecorrencia;
    EditText txtPreco;
    Button btnEnviar, btnHome;


    public AportesFragment() {
        super(R.layout.fragment_aportes);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        //Linkando com os componentes respectivos
        spnAtivo = (Spinner) view.findViewById(R.id.spnAtivo);
        spnRecorrencia = (Spinner) view.findViewById(R.id.spnRecorrencia);
        spnTipo = (Spinner) view.findViewById(R.id.spnTipo);
        btnEnviar = (Button) view.findViewById(R.id.btnEnviar);
        txtPreco = (EditText) view.findViewById(R.id.txtPreco);
        btnHome = (Button) view.findViewById(R.id.btnHome);


        //Listas fixas
        String[] Tipo = {"Escolha o Tipo", "Compra"};
        String[] Recorrencia = {"Escolha a Recorrência", "Diário", "Semanal", "Mensal", "Anual"};

        ArrayAdapter<String> adapterTipo = new ArrayAdapter<String>(requireContext(),
                R.layout.my_select_item,
                Tipo
        );
        ArrayAdapter<String> adapterRecorrencia = new ArrayAdapter<String>(requireContext(),
                R.layout.my_select_item,
                Recorrencia
        );

        adapterTipo.setDropDownViewResource(R.layout.my_dropdown_item);
        adapterRecorrencia.setDropDownViewResource(R.layout.my_dropdown_item);

        spnTipo.setAdapter(adapterTipo);
        spnRecorrencia.setAdapter(adapterRecorrencia);


        carregarAtivos();
        aportes();

    }

    private void aportes() {

        //Executa ao clicar no botão de Enviar
        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Puxa os dados nos campos
                String Ativo = spnAtivo.getSelectedItem().toString();
                String Tipo = spnTipo.getSelectedItem().toString();
                String Recorrencia = spnRecorrencia.getSelectedItem().toString();
                String SPreco = txtPreco.getText().toString();

                //Verifica se foram preenchidos
                if (Ativo.equals("Escolha o Ativo") || Tipo.equals("Escolha o Tipo") || Recorrencia.equals("Escolha a Recorrência") || SPreco.isEmpty()) {
                    Toast.makeText(requireContext(),
                            "Por favor preencha todos os campo",
                            LENGTH_LONG).show();
                    return;
                }

                //Converte o preço para double depois da verificação
                Double Preco = Double.parseDouble(SPreco);

                //Cria o objeto api, responsável por enviar os dados para o banco de dados
                ApiService api = RetrofitClient
                        .getClient(requireContext())
                        .create(ApiService.class);

                //Cria a classe aporte e em seguida passa os seus dados para api
                clsAportes aporte = new clsAportes(Ativo, Preco, Tipo, Recorrencia);
                api.registerAporte(aporte).enqueue(new Callback<ApiResponse>() {

                    //Lógica que verifica se deu tudo certo ao enviar as informações
                    @Override
                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

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
                    }

                    @Override
                    public void onFailure(Call<ApiResponse> call, Throwable t) {
                        //Mostra o erro se por algum motivo ocorrer um erro
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

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent home = new Intent(requireContext(),
                        Dashboard.class);
                startActivity(home);
            }
        });
    }

    private void carregarAtivos() {
        ApiService service = RetrofitClient.getClient(requireContext()).create(ApiService.class);

        service.getService().enqueue(new Callback<QuoteResponse>() {

            @Override
            public void onResponse(Call<QuoteResponse> call, Response<QuoteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    List<String> simbolos = new ArrayList<>();
                    simbolos.add("Escolha o ativo");


                    for (QuoteResult result : response.body().getResults()) {
                        simbolos.add(result.getSymbol());
                    }

                    ArrayAdapter<String> adapterAtivo = new ArrayAdapter<String>(requireContext(),
                            R.layout.my_select_item,
                            simbolos
                    );

                    adapterAtivo.setDropDownViewResource(R.layout.my_dropdown_item);

                    spnAtivo.setAdapter(adapterAtivo);
                    return;
                }
                Toast.makeText(requireContext(),
                        "N achamos nd",
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