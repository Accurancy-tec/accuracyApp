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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accurancymobileapp.adapter.EmphasisAdapter;

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
    private ComposeView ctvChart;
    Button btnAportes,btnDeslogar;
    BottomNavigationView bottomNavigation;
    TextView txtAtivoNome1,txtAtivoNome2,txtAtivoNome3,txtAtivoNome4,
            txtAtivoPreco1, txtAtivoPreco2, txtAtivoPreco3, txtAtivoPreco4;
    RecyclerView recyclerInvestimentos;

    private SessionManager sessionManager;

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

        ctvChart = findViewById(R.id.ctvChart);
        btnDeslogar = (Button) findViewById(R.id.btnDeslogar);
        recyclerInvestimentos = findViewById(R.id.recyclerInvestimentos);



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


        carregarGrafico();
        dashboard();
    }

    private void dashboard(){

        //Volta para a tela de login
        btnDeslogar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               logoutSession();
            }
        });

        recyclerInvestimentos.setLayoutManager(new LinearLayoutManager(this));

        ApiService api = RetrofitClient
                .getClient()
                .create(ApiService.class);

        api.getAportes().enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                if(response.isSuccessful() && response.body() != null ){

                    List<clsAportes> ativo = response.body().getLista();

                    EmphasisAdapter adapter = new EmphasisAdapter(ativo);

                    recyclerInvestimentos.setAdapter(adapter);

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
                        ChartHelper.GraphicConfig(ctvChart, valores);
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