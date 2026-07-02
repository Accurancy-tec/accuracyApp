package com.example.accurancymobileapp.activities;

import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.api.ApiService;
import com.example.accurancymobileapp.api.RetrofitClient;
import com.example.accurancymobileapp.model.User;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    EditText editEmail;
    EditText editSenha;
    Button   btnEntrar;
    TextView btnEsqueciSenha;
    ImageView btnToggleSenha;
    TextView tabEntrar;
    TextView tabCadastrar;
    LinearLayout btnGoogle;
    LinearLayout btnGitHub;
    TextView btnIrCadastro;

    private boolean senhaVisivel = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editEmail       = findViewById(R.id.editEmail);
        editSenha       = findViewById(R.id.editSenha);
        btnEntrar       = findViewById(R.id.btnEntrar);
        btnEsqueciSenha = findViewById(R.id.btnEsqueciSenha);
        btnToggleSenha  = findViewById(R.id.btnToggleSenha);
        tabEntrar       = findViewById(R.id.tabEntrar);
        tabCadastrar    = findViewById(R.id.tabCadastrar);
        btnGoogle       = findViewById(R.id.btnGoogle);
        btnGitHub       = findViewById(R.id.btnGitHub);
        btnIrCadastro   = findViewById(R.id.btnIrCadastro);

        configurarListeners();
    }


    private void configurarListeners() {

        btnEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editEmail.getText().toString().trim();
                String senha = editSenha.getText().toString().trim();

                // Validação dos campos
                if (email.isEmpty()) {
                    editEmail.setError("Informe seu e-mail");
                    return;
                }
                if (senha.isEmpty()) {
                    editSenha.setError("Informe sua senha");
                    return;
                }

                // chamar API aqui via Retrofit
                fazerLogin(email, senha);
            }
        });


        // Mostrar / ocultar senha
        btnToggleSenha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (senhaVisivel) {
                    // Oculta a senha
                    editSenha.setTransformationMethod(
                            PasswordTransformationMethod.getInstance()
                    );
                    senhaVisivel = false;
                } else {
                    // Mostra a senha
                    editSenha.setTransformationMethod(
                            HideReturnsTransformationMethod.getInstance()
                    );
                    senhaVisivel = true;
                }
                // Mantém o cursor no final do texto após trocar o modo
                editSenha.setSelection(editSenha.getText().length());
            }
        });


        btnEsqueciSenha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: navegar para tela de recuperação de senha
                Toast.makeText(LoginActivity.this,
                        "Tela de recuperação de senha (em breve)",
                        Toast.LENGTH_SHORT).show();
            }
        });


        tabCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegar para a tela de cadastro
                // Intent intent = new Intent(LoginActivity.this, CadastroActivity.class);
                // startActivity(intent);

                tabEntrar.setBackgroundResource(0); // remove background ativo
                tabEntrar.setTextColor(getColor(R.color.text_secondary));
                tabCadastrar.setBackgroundResource(R.drawable.bg_tab_active);
                tabCadastrar.setTextColor(getColor(R.color.text_primary));
            }
        });

        tabEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tabCadastrar.setBackgroundResource(0);
                tabCadastrar.setTextColor(getColor(R.color.text_secondary));
                tabEntrar.setBackgroundResource(R.drawable.bg_tab_active);
                tabEntrar.setTextColor(getColor(R.color.text_primary));
            }
        });


        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: implementar Google Sign-In
                Toast.makeText(LoginActivity.this,
                        "Login com Google (em breve)", Toast.LENGTH_SHORT).show();
            }
        });


        btnGitHub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: implementar GitHub OAuth
                Toast.makeText(LoginActivity.this,
                        "Login com GitHub (em breve)", Toast.LENGTH_SHORT).show();
            }
        });


        btnIrCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  navegar para CadastroActivity
                // Intent intent = new Intent(LoginActivity.this, CadastroActivity.class);
                // startActivity(intent);
                Toast.makeText(LoginActivity.this,
                        "Indo para o cadastro...", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Método de login
    private void fazerLogin(String email, String senha) {
        User user = new User(email, senha);

        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        api.registerNewUser(user).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    Toast.makeText(LoginActivity.this, "Redirecionando para dashboard: em breve", Toast.LENGTH_LONG).show();

                }
                else{
                    Toast.makeText(LoginActivity.this, "Erro: " + response.code(),Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
            }
        });

    }
}