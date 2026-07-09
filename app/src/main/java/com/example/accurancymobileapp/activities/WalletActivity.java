package com.example.accurancymobileapp.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.adapter.QuoteAdapter;
import com.example.accurancymobileapp.model.QuoteData;
import com.example.accurancymobileapp.model.QuoteResult;
import com.example.accurancymobileapp.network.client.BrapiClient;
import com.example.accurancymobileapp.network.service.BrapiService;
import com.example.accurancymobileapp.response.QuoteResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WalletActivity extends AppCompatActivity {

    RecyclerView recyclerInvestimentos;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_wallet);

        recyclerInvestimentos = findViewById(R.id.recyclerInvestimentos);

        recyclerInvestimentos.setLayoutManager(new LinearLayoutManager(this));

        BrapiService api = BrapiClient.getBrapiClient().create(BrapiService.class);
        api.getQuote("ITUB4,VALE3,PETR4").enqueue(new Callback<QuoteResponse>() {
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
    }
}