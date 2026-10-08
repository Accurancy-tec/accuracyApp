package com.example.accurancymobileapp.fragments;

import static android.widget.Toast.LENGTH_LONG;

import static androidx.core.content.ContextCompat.getSystemService;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
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
import com.example.accurancymobileapp.model.Aporte;
import com.example.accurancymobileapp.model.QuoteData;
import com.example.accurancymobileapp.model.QuoteResult;
import com.example.accurancymobileapp.model.Wallet;
import com.example.accurancymobileapp.model.clsAportes;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.network.repository.WalletRepository;
import com.example.accurancymobileapp.network.service.ApiService;
import com.example.accurancymobileapp.response.ApiResponse;
import com.example.accurancymobileapp.response.QuoteResponse;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AportesFragment extends Fragment {

    Spinner spnAtivo, spnTipo, spnRecorrencia,spnCategoria, spnCarteiras;
    EditText txtPreco,txtQuantidade;
    MaterialButton btnEnviar, btnValorOutro, btnSelecionado, btnValor100, btnValor250, btnValor500, btnValor750, btnValor1k;
    WalletRepository walletRepository;

    List<ActiveSpinner> active = new ArrayList<>();
    ArrayAdapter<ActiveSpinner> adapterActive;

    ArrayAdapter<Wallet> spinnerAdapter;
    private boolean atualizandoCampo = false; //Evita loop do TextWatcher
    private final Locale ptBR = new Locale("pt", "BR");


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
        spnCarteiras = (Spinner) view.findViewById(R.id.spnCarteiras);
        btnEnviar = (MaterialButton) view.findViewById(R.id.btnEnviar);
        btnValorOutro = (MaterialButton) view.findViewById(R.id.btnValorOutro);
        btnValor100 = (MaterialButton) view.findViewById(R.id.btnValor100);
        btnValor250 = (MaterialButton) view.findViewById(R.id.btnValor250);
        btnValor500 = (MaterialButton) view.findViewById(R.id.btnValor500);
        btnValor750 = (MaterialButton) view.findViewById(R.id.btnValor750);
        btnValor1k = (MaterialButton) view.findViewById(R.id.btnValor1k);
        txtPreco = (EditText) view.findViewById(R.id.txtPreco);
        txtQuantidade = (EditText) view.findViewById(R.id.txtQuantidade);

        walletRepository = new WalletRepository(requireContext());

        String[] Categoria = {"Ações","Cripto","Renda Fixa","FIIs","Internacional"};
        String[] Tipo = {"Escolha o Tipo", "Compra", "Venda","Dividendo"};
        String[] Recorrencia = {"Escolha a Recorrência", "Único","Diário", "Semanal", "Mensal", "Anual"};
        List<Wallet> listaCarteiras = new ArrayList<>();


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

        spinnerAdapter = new ArrayAdapter<>(requireContext(), R.layout.my_select_item, listaCarteiras);

        adapterActive = new ArrayAdapter<>(requireContext(),
                R.layout.my_select_item,
                active);

        adapterTipo.setDropDownViewResource(R.layout.my_dropdown_item);
        adapterRecorrencia.setDropDownViewResource(R.layout.my_dropdown_item);
        adapterActive.setDropDownViewResource(R.layout.my_dropdown_item);
        adapterCategoria.setDropDownViewResource(R.layout.my_dropdown_item);
        spinnerAdapter.setDropDownViewResource(R.layout.my_dropdown_item);

        spnTipo.setAdapter(adapterTipo);
        spnRecorrencia.setAdapter(adapterRecorrencia);
        spnAtivo.setAdapter(adapterActive);
        spnCategoria.setAdapter(adapterCategoria);
        spnCarteiras.setAdapter(spinnerAdapter);


        spnCarteiras.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        Wallet carteiraSelecionada = (Wallet) parent.getItemAtPosition(position);

                        int idCarteira = carteiraSelecionada.getId_carteira();

                        String nomeCarteira = carteiraSelecionada.getNome_carteira();

                        Log.d("CARTEIRA", "ID: " + idCarteira);
                        Log.d("CARTEIRA", "Nome: " + nomeCarteira);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                }
        );

        carregarAtivos();
        aportes();
        buscarCarteiras();
        configurarCampoValor();
        configurarValoresRapidos();

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

                Wallet carteiraSelecionada = (Wallet) spnCarteiras.getSelectedItem();

                int id_carteira = carteiraSelecionada.getId_carteira();

                Log.d("APORTE_DEBUG",
                        "Carteira selecionada: "
                                + carteiraSelecionada.getNome_carteira()
                                + " | ID: "
                                + id_carteira
                );

                if (symbol.isEmpty() ||name.equals("Escolha o Ativo") || SQuantidade.isEmpty() ||Tipo.equals("Escolha o Tipo") || Recorrencia.equals("Escolha a Recorrência") || SPreco.isEmpty()) {
                    Toast.makeText(requireContext(),
                            "Por favor preencha todos os campo",
                            LENGTH_LONG).show();
                    return;
                }
                double Preco = converterDecimal(SPreco);
                double Quantidade = converterDecimal(SQuantidade);

                if(symbol.isEmpty() || name.equals("Escolha o ativo") || SQuantidade.isEmpty()
                        || Tipo.equals("Escolha o tipo") || Recorrencia.equals("Escolha a recorrencia")
                        || SPreco.isEmpty()){
                    Toast.makeText(requireContext(), "Por favor preencha todos os campo", LENGTH_LONG).show();
                    return;
                }

                if(Quantidade <= 0 || Preco <= 0){
                    Toast.makeText(requireContext(), "Digite valores válidos", LENGTH_LONG).show();
                    return;
                }

                ApiService api = RetrofitClient
                        .getClient(requireContext())
                        .create(ApiService.class);


                clsAportes aporte = new clsAportes(symbol,name,Categoria,Quantidade ,Preco, Tipo, Recorrencia, id_carteira);
                Log.d(
                        "APORTE_DEBUG",
                        "ID carteira no aporte: "
                                + aporte.getId_Carteira()
                );

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

    private void buscarCarteiras() {
        walletRepository.buscarWallets(new WalletRepository.WalletListCallback() {
            @Override
            public void onSuccess(List<Wallet> wallets) {
                spinnerAdapter.clear();
                spinnerAdapter.addAll(wallets);

                spinnerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), message, LENGTH_LONG).show();
            }
        });
    }
    private void setValorNoCampo(double valor) {
        atualizandoCampo = true;
        txtPreco.setText(String.format(ptBR, "%.2f", valor));
        txtPreco.setSelection(txtPreco.getText().length());
        atualizandoCampo = false;
    }

    private void configurarCampoValor() {
        txtPreco.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) { }
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { }

            @Override
            public void afterTextChanged(Editable s) {
                // Se o usuário digitou, marca o chip "Outro"
                if (!atualizandoCampo && btnSelecionado != btnValorOutro) {
                    selecionarBotao(btnValorOutro);
                }
            }
        });
    }

    private double lerValor() {
        return converterDecimal(txtPreco.getText().toString());
    }

    private void selecionarBotao(MaterialButton novo) {
        if (btnSelecionado != null) estilizarChip(btnSelecionado, false);
        estilizarChip(novo, true);
        btnSelecionado = novo;
    }

    private void configurarValoresRapidos() {
        Log.d("APORTE", "configurarValoresRapidos chamado");
        View root = requireView();

        MaterialButton[] botoes = {
                root.findViewById(R.id.btnValor100),
                root.findViewById(R.id.btnValor250),
                root.findViewById(R.id.btnValor500), root.findViewById(R.id.btnValor750),
                root.findViewById(R.id.btnValor1k)
        };
        double[] valores = {100, 250, 500, 750, 1000};

        for (int i = 0; i < botoes.length; i++) {
            final MaterialButton botao = botoes[i];
            final double valor = valores[i];
            botao.setOnClickListener(v -> {
                setValorNoCampo(valor);
                selecionarBotao(botao);
                txtPreco.clearFocus();
            });
        }

        btnSelecionado = root.findViewById(R.id.btnValor500); // já vem selecionado no layout

        // "Outro": limpa o campo e abre o teclado
        btnValorOutro.setOnClickListener(v -> {
            atualizandoCampo = true;
            txtPreco.setText("");
            atualizandoCampo = false;
            selecionarBotao(btnValorOutro);
            txtPreco.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(requireContext(), InputMethodManager.class);
            if (imm != null) imm.showSoftInput(txtPreco, InputMethodManager.SHOW_IMPLICIT);
        });

    }

    private void estilizarChip(MaterialButton b, boolean selecionado) {
        b.setBackgroundTintList(ColorStateList.valueOf(
                Color.parseColor(selecionado ? "#1D3A8A" : "#111A2B")));
        b.setStrokeColor(ColorStateList.valueOf(
                Color.parseColor(selecionado ? "#3B82F6" : "#1E3A8A")));
        b.setTextColor(Color.parseColor(selecionado ? "#FFFFFF" : "#60A5FA"));
    }

    /*private void atualizarContagemAtivos() {
        int ativos = 0;
        for (AporteRecorrente r : recorrentes) if (r.isAtivo()) ativos++;
        txtQtdAtivos.setText(ativos + (ativos == 1 ? " ativo" : " ativos"));
    }*/

    private void confirmarAporte() {
        double valor = lerValor();
        if (valor <= 0) {
            txtPreco.setError("Informe um valor válido");
            txtPreco.requestFocus();
            return;
        }

        /*String carteira = spnCarteiras.getSelectedItem().toString();
        String ativoCompleto = spnAtivo.getSelectedItem().toString();
        String tipo = spnTipo.getSelectedItem().toString();
        String recorrencia = spnRecorrencia.getSelectedItem().toString();
        String nome = ativoCompleto.contains(" — ") ? ativoCompleto.split(" — ")[0] : ativoCompleto;

        // Só compras entram no "aportado este mês"
        if (tipo.equals("Compra")) {
            totalMes += valor;
            txtAportadoMes.setText(moeda.format(totalMes).replace('\u00A0', ' '));
        }

        // Aporte recorrente entra na lista
        if (!recorrencia.equals("Única")) {
            Calendar cal = Calendar.getInstance();
            String frequencia;
            if (recorrencia.equals("Semanal")) {
                frequencia = "Semanal · " + new SimpleDateFormat("EEEE", ptBR).format(cal.getTime());
                cal.add(Calendar.DAY_OF_MONTH, 7);
            } else {
                frequencia = "Mensal · dia " + cal.get(Calendar.DAY_OF_MONTH);
                cal.add(Calendar.MONTH, 1);
            }
            String proxima = new SimpleDateFormat("dd/MM/yyyy", ptBR).format(cal.getTime());
            String sigla = nome.length() > 3 ? nome.substring(0, 2).toUpperCase() : nome;

            recorrentes.add(0, new AporteRecorrente(sigla, nome, frequencia, valor, proxima, true));
            adapter.notifyItemInserted(0);
            atualizarContagemAtivos();*/
        }

    private double converterDecimal(String texto) {
        String t = texto.trim();
        if (t.isEmpty()) return -1;
        if (t.contains(",")) {
            t = t.replace(".", "").replace(',', '.');
        }
        try {
            return Double.parseDouble(t);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}