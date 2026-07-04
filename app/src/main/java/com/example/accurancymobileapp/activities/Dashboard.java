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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.accurancymobileapp.api.ApiResponse;
import com.example.accurancymobileapp.api.ApiServiceGet;
import com.example.accurancymobileapp.api.RetrofitClient;
import com.example.accurancymobileapp.model.clsAportes;
import com.example.accurancymobileapp.R;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Dashboard extends AppCompatActivity {

    Button btnAportes;
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

        btnAportes = (Button) findViewById(R.id.btnAportes);
        txtAtivoNome1 = (TextView) findViewById(R.id.txtAtivoNome1);
        txtAtivoNome2 = (TextView) findViewById(R.id.txtAtivoNome2);
        txtAtivoNome3 = (TextView) findViewById(R.id.txtAtivoNome3);
        txtAtivoNome4 = (TextView) findViewById(R.id.txtAtivoNome4);
        txtAtivoPreco1 = (TextView) findViewById(R.id.txtAtivoPreco1);
        txtAtivoPreco2 = (TextView) findViewById(R.id.txtAtivoPreco2);
        txtAtivoPreco3 = (TextView) findViewById(R.id.txtAtivoPreco3);
        txtAtivoPreco4 = (TextView) findViewById(R.id.txtAtivoPreco4);

        dashboard();
    }

    private void dashboard(){
        btnAportes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pag = new Intent(Dashboard.this,
                        Aportes.class);
                startActivity(pag);
            }
        });



        ApiServiceGet api = RetrofitClient
                .getClient()
                .create(ApiServiceGet.class);

        api.getAportes().enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                if(response.isSuccessful() && response.body() != null ){

                    ArrayList<clsAportes> ativo = response.body().getLista();
                    if(!ativo.isEmpty()) {

                        int tamanho = ativo.size();

                        txtAtivoNome1.setText(ativo.get(0).getAtivo());
                        txtAtivoPreco1.setText(String.valueOf(ativo.get(0).getPreco()));
                        if(tamanho  == 2){
                            txtAtivoNome2.setText(ativo.get(1).getAtivo());
                            txtAtivoPreco2.setText(String.valueOf(ativo.get(1).getPreco()));
                        }if (tamanho == 3) {
                            txtAtivoNome3.setText(ativo.get(2).getAtivo());
                            txtAtivoPreco3.setText(String.valueOf(ativo.get(2).getPreco()));
                        }if(tamanho == 4){
                            txtAtivoNome4.setText(ativo.get(3).getAtivo());
                            txtAtivoPreco4.setText(String.valueOf(ativo.get(3).getPreco()));
                        }

                        return;
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
}