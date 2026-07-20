package com.example.accurancymobileapp.activities;

import static android.widget.Toast.LENGTH_LONG;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.compose.ui.platform.ComposeView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.accurancymobileapp.model.clsGrafic;
import com.example.accurancymobileapp.ui.ChartHelper;

import com.example.accurancymobileapp.network.service.VicoService;
import com.example.accurancymobileapp.response.ApiResponse;
import com.example.accurancymobileapp.network.service.ApiService;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.model.clsAportes;
import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.response.VicoResponse;
import com.example.accurancymobileapp.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;


import java.util.ArrayList;
import java.util.List;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Dashboard extends AppCompatActivity {
    private ComposeView ctvEvoCarteira;
    Button btnAportes,btnDeslogar;
    BottomNavigationView bottomNavigation;
    TextView txtAtivoNome1,txtAtivoNome2,txtAtivoNome3,txtAtivoNome4,
            txtAtivoPreco1, txtAtivoPreco2, txtAtivoPreco3, txtAtivoPreco4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ctvEvoCarteira = findViewById(R.id.ctvEvoCarteira);
        btnAportes = (Button) findViewById(R.id.btnAportes);
        btnDeslogar = (Button) findViewById(R.id.btnDeslogar);
        txtAtivoNome1 = (TextView) findViewById(R.id.txtAtivoNome1);
        txtAtivoNome2 = (TextView) findViewById(R.id.txtAtivoNome2);
        txtAtivoNome3 = (TextView) findViewById(R.id.txtAtivoNome3);
        txtAtivoNome4 = (TextView) findViewById(R.id.txtAtivoNome4);
        txtAtivoPreco1 = (TextView) findViewById(R.id.txtAtivoPreco1);
        txtAtivoPreco2 = (TextView) findViewById(R.id.txtAtivoPreco2);
        txtAtivoPreco3 = (TextView) findViewById(R.id.txtAtivoPreco3);
        txtAtivoPreco4 = (TextView) findViewById(R.id.txtAtivoPreco4);

        carregarGrafico();
        dashboard();

        //Trecho do menu
        bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_home);

        bottomNavigation.setOnItemSelectedListener(item -> {
            if(item.getItemId() == R.id.nav_home){
                Intent it = new Intent(Dashboard.this, Dashboard.class);
                startActivity(it);
                return true;
            }
            if(item.getItemId() == R.id.nav_wallet){
                Intent it = new Intent(Dashboard.this, WalletActivity.class);
                startActivity(it);
                return true;
            }
            if(item.getItemId() == R.id.nav_aporte){
                Intent it = new Intent(Dashboard.this, Aportes.class);
                startActivity(it);
                return true;
            }
            return false;
        });
    }

    private void dashboard(){

        txtAtivoNome1.setVisibility(View.INVISIBLE);
        txtAtivoNome2.setVisibility(View.INVISIBLE);
        txtAtivoNome3.setVisibility(View.INVISIBLE);
        txtAtivoNome4.setVisibility(View.INVISIBLE);

        txtAtivoPreco1.setVisibility(View.INVISIBLE);
        txtAtivoPreco2.setVisibility(View.INVISIBLE);
        txtAtivoPreco3.setVisibility(View.INVISIBLE);
        txtAtivoPreco4.setVisibility(View.INVISIBLE);

        //Vai para os aportes
        btnAportes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pag = new Intent(Dashboard.this,
                        Aportes.class);
                startActivity(pag);
            }
        });

        //Volta para a tela de login
        btnDeslogar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               logoutSession();
            }
        });


        ApiService api = RetrofitClient
                .getClient()
                .create(ApiService.class);

        api.getAportes().enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                if(response.isSuccessful() && response.body() != null ){

                    ArrayList<clsAportes> ativo = response.body().getLista();
                    if(!ativo.isEmpty()) {

                        int tamanho = ativo.size();

                        txtAtivoNome1.setVisibility(View.VISIBLE);
                        txtAtivoPreco1.setVisibility(View.VISIBLE);

                        txtAtivoNome1.setText(ativo.get(0).getAtivo());
                        txtAtivoPreco1.setText(String.valueOf(ativo.get(0).getPreco()));
                        if(tamanho  == 2){
                            txtAtivoNome2.setVisibility(View.VISIBLE);
                            txtAtivoPreco2.setVisibility(View.VISIBLE);

                            txtAtivoNome2.setText(ativo.get(1).getAtivo());
                            txtAtivoPreco2.setText(String.valueOf(ativo.get(1).getPreco()));
                        }if (tamanho == 3) {
                            txtAtivoNome3.setVisibility(View.VISIBLE);
                            txtAtivoPreco3.setVisibility(View.VISIBLE);

                            txtAtivoNome3.setText(ativo.get(2).getAtivo());
                            txtAtivoPreco3.setText(String.valueOf(ativo.get(2).getPreco()));
                        }if(tamanho == 4){
                            txtAtivoNome4.setVisibility(View.VISIBLE);
                            txtAtivoPreco4.setVisibility(View.VISIBLE);

                            txtAtivoNome4.setText(ativo.get(3).getAtivo());
                            txtAtivoPreco4.setText(String.valueOf(ativo.get(3).getPreco()));
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(Dashboard.this,
                        "Nenhuma resposta chegou",
                        LENGTH_LONG).show();
            }
        });
    };
    private void carregarGrafico(){

        VicoService vico = RetrofitClient.getClient().create(VicoService.class);
        vico.getGrafic().enqueue(new Callback<VicoResponse>() {
            @Override
            public void onResponse(Call<VicoResponse> call, Response<VicoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    ArrayList<Number> valores = new ArrayList<>();

                    for(clsGrafic item : response.body().getResults()){
                        valores.add(item.getPreco());
                    }

                    if(!valores.isEmpty()){
                        ChartHelper.configurarGrafico(ctvEvoCarteira, valores);
                    }

                    Toast.makeText(Dashboard.this,
                            "Quantidade: " + valores.size(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<VicoResponse> call, Throwable t) {
                Toast.makeText(Dashboard.this,
                        "Erro " + t.getMessage(),
                        LENGTH_LONG).show();
            }
        });
    }

    public void logoutSession(){
        SessionManager sessionManager = new SessionManager(this);

        sessionManager.logout();

        Intent it = new Intent(Dashboard.this, LoginActivity.class);
        it.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(it);
        finish();

    }
}