package com.example.accurancymobileapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.compose.ui.platform.ComposeView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.adapter.QuoteAdapter;
import com.example.accurancymobileapp.model.QuoteData;
import com.example.accurancymobileapp.model.QuoteResult;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.network.service.ApiService;
import com.example.accurancymobileapp.response.QuoteResponse;
import com.example.accurancymobileapp.ui.ChartHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WalletActivity extends AppCompatActivity {

    RecyclerView recyclerInvestimentos;
    BottomNavigationView bottomNavigation;
    ComposeView ctvEvoCarteira;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_wallet);

        recyclerInvestimentos = findViewById(R.id.recyclerInvestimentos);
        ctvEvoCarteira = findViewById(R.id.ctvEvoCarteira);

        //Trecho do menu
        bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_wallet);

        bottomNavigation.setOnItemSelectedListener(item -> {
            if(item.getItemId() == R.id.nav_home){
                Intent it = new Intent(WalletActivity.this, Dashboard.class);
                startActivity(it);
                return true;
            }
            if(item.getItemId() == R.id.nav_wallet){
                Intent it = new Intent(WalletActivity.this, WalletActivity.class);
                startActivity(it);
                return true;
            }
            if(item.getItemId() == R.id.nav_aporte){
                Intent it = new Intent(WalletActivity.this, Aportes.class);
                startActivity(it);
                return true;
            }
            return false;
        });

        recyclerInvestimentos.setLayoutManager(new LinearLayoutManager(this));

        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        api.getQuote("ITUB4,VALE3,PETR4,PETR4,PETR4").enqueue(new Callback<QuoteResponse>() {
            @Override
            public void onResponse(Call<QuoteResponse> call, Response<QuoteResponse> response) {

                if(response.isSuccessful() && response.body() != null){
                    List<QuoteData> quotes = new ArrayList<>();
                    for(QuoteResult result : response.body().getResults()){
                        quotes.add(result.getData());
                    }

                    QuoteAdapter adapter = new QuoteAdapter(quotes);

                    recyclerInvestimentos.setAdapter(adapter);
                }
                else{
                    Toast.makeText(WalletActivity.this, "Erro: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<QuoteResponse> call, Throwable t) {

                Log.e("API", "Erro completo", t);
                Toast.makeText(WalletActivity.this, "Erro: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        carregarGrafico();
    }

    public void carregarGrafico(){
        ArrayList<Number> valores = new ArrayList<>();

        valores.add(1000.0);
        valores.add(1150.0);
        valores.add(1080.0);
        valores.add(1300.0);
        valores.add(1450.0);

        ChartHelper.configurarGraficoWallet(
                ctvEvoCarteira,
                valores
        );
    }
}