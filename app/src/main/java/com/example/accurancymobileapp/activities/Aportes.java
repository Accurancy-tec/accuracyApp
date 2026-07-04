package com.example.accurancymobileapp.activities;

import static android.widget.Toast.LENGTH_LONG;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.accurancymobileapp.api.ApiResponse;
import com.example.accurancymobileapp.api.ApiServicePost;
import com.example.accurancymobileapp.api.RetrofitClient;
import com.example.accurancymobileapp.model.clsAportes;
import com.example.accurancymobileapp.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Aportes extends AppCompatActivity {


    Spinner spnAtivo, spnTipo,spnRecorrencia;
    EditText txtPreco;
    Button btnEnviar, btnHome;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_aportes);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Linkando com os componentes respectivos
    spnAtivo = (Spinner) findViewById(R.id.spnAtivo);
    spnRecorrencia = (Spinner) findViewById(R.id.spnRecorrencia);
    spnTipo = (Spinner) findViewById(R.id.spnTipo);
    btnEnviar = (Button) findViewById(R.id.btnEnviar);
    txtPreco = (EditText) findViewById(R.id.txtPreco);
    btnHome = (Button) findViewById(R.id.btnHome);

    //Listas para os Spinners
    String[] Ativo = {"Escolha o Ativo","Petobras", "Bitcoin", "Dolar"};
    String[] Tipo = {"Escolha o Tipo","Compra"};
    String[] Recorrencia = {"Escolha a Recorrência","Diário","Semanal","Mensal","Anual"};

    //Inserindo escolhas
        ArrayAdapter<String> adapterAtivo = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                Ativo
        );
        ArrayAdapter<String> adapterTipo = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                Tipo
        );
        ArrayAdapter<String> adapterRecorrencia = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                Recorrencia
        );

        adapterAtivo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterRecorrencia.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spnAtivo.setAdapter(adapterAtivo);
        spnTipo.setAdapter(adapterTipo);
        spnRecorrencia.setAdapter(adapterRecorrencia);

        //Puxando a função que contem as outras partes do código
        //Em tese não muda nada mas facilita a manutenção
        aportes();
    }
    private void aportes(){

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
                if(Ativo.equals("Escolha o Ativo") || Tipo.equals("Escolha o Tipo") || Recorrencia.equals("Escolha a Recorrência") || SPreco.isEmpty()){
                    Toast.makeText(Aportes.this,
                            "Por favor preencha todos os campo",
                            LENGTH_LONG).show();
                    return;
                }

                //Converte o preço para double depois da verificação
                Double Preco = Double.parseDouble(SPreco);

                //Cria o objeto api, responsável por enviar os dados para o banco de dados
                ApiServicePost api = RetrofitClient
                        .getClient()
                        .create(ApiServicePost.class);

                //Cria a classe aporte e em seguida passa os seus dados para api
                clsAportes aporte = new clsAportes(Ativo,Preco,Tipo,Recorrencia);
                api.registerAporte(aporte).enqueue(new Callback<ApiResponse>() {

                    //Lógica que verifica se deu tudo certo ao enviar as informações
                    @Override
                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                        if (response.isSuccessful() && response.body() != null) {

                            Toast.makeText(
                                    Aportes.this,
                                    response.body().getMensagem(),
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }
                        Toast.makeText(Aportes.this,
                                "Erro ao buscar a resposta da API",
                                LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse> call, Throwable t) {
                        //Mostra o erro se por algum motivo ocorrer um erro
                        Toast.makeText(
                                Aportes.this,
                                "Erro: " + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
            }
        });

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent home = new Intent(Aportes.this,
                        Dashboard.class);
                startActivity(home);
            }
        });
    }
}