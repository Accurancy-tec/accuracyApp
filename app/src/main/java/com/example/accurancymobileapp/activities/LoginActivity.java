package com.example.accurancymobileapp.activities;

import android.content.Intent;
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
import com.example.accurancymobileapp.model.LoginResponse;
import com.example.accurancymobileapp.model.User;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    EditText editEmail;
    EditText editSenha;
    EditText txtNome;
    EditText txtCpf;
    EditText txtCelular;
    EditText txtEmailCadastro;
    EditText txtSenhaCadastro;
    EditText txtConfirmarSenha;
    Button   btnLogin;
    Button btnRegister;
    TextView btnEsqueciSenha;
    ImageView btnToggleSenha;
    TextView tabEntrar;
    TextView tabCadastrar;
    LinearLayout btnGoogle;
    LinearLayout btnGitHub;
    TextView btnIrCadastro;
    View layoutLogin;
    View layoutRegister;

    private boolean senhaVisivel = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        editEmail = findViewById(R.id.editEmail);
        editSenha = findViewById(R.id.editSenha);
        txtNome = findViewById(R.id.txtNome);
        txtCpf = findViewById(R.id.txtCpf);
        txtCelular = findViewById(R.id.txtCelular);
        txtEmailCadastro = findViewById(R.id.txtEmailCadastro);
        txtSenhaCadastro = findViewById(R.id.txtSenhaCadastro);
        txtConfirmarSenha = findViewById(R.id.txtConfirmarSenha);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnEsqueciSenha = findViewById(R.id.btnEsqueciSenha);
        btnToggleSenha = findViewById(R.id.btnToggleSenha);
        tabEntrar = findViewById(R.id.tabEntrar);
        tabCadastrar = findViewById(R.id.tabCadastrar);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnGitHub = findViewById(R.id.btnGitHub);
        btnIrCadastro = findViewById(R.id.btnIrCadastro);
        layoutLogin = findViewById(R.id.layoutLogin);
        layoutRegister = findViewById(R.id.layoutRegister);

        configurarListeners();
    }


    private void configurarListeners() {

        btnLogin.setOnClickListener(new View.OnClickListener() {
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

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nome = txtNome.getText().toString().trim();
                String cpf = txtCpf.getText().toString().trim();
                String celular = txtCelular.getText().toString().trim();
                String email = txtEmailCadastro.getText().toString().trim();
                String senhaCadastro = txtSenhaCadastro.getText().toString().trim();
                String confirmarSenhaCadastro = txtConfirmarSenha.getText().toString().trim();


                if(nome.isEmpty()){
                    txtNome.setError("Informe o nome");
                    return;
                }
                if(cpf.isEmpty()){
                    txtCpf.setError("Informe o CPF");
                    return;
                }
                if(celular.isEmpty()){
                    txtCelular.setError("Informe o Celular.");
                    return;
                }
                if(email.isEmpty()){
                    editEmail.setError("Informe o E-mail.");
                    return;
                }
                if(senhaCadastro.isEmpty()){
                    txtSenhaCadastro.setError("Informe a senha.");
                    return;
                }
                if(confirmarSenhaCadastro.isEmpty()){
                    txtConfirmarSenha.setError("Confirme a senha.");
                    return;
                }

                if(!senhaCadastro.equals(confirmarSenhaCadastro)){
                    txtConfirmarSenha.setError("As senhas não são iguais.");
                    return;
                }

                fazerRegistro(nome, celular, cpf, senhaCadastro, email);

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

                layoutLogin.setVisibility(View.GONE);
                layoutRegister.setVisibility(View.VISIBLE);

                tabCadastrar.setBackgroundResource(R.drawable.bg_tab_active);
                tabEntrar.setBackgroundResource(0);

                tabCadastrar.setTextColor(getColor(R.color.text_primary));
                tabEntrar.setTextColor(getColor(R.color.text_secondary));
            }
        });

        tabEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutLogin.setVisibility(View.VISIBLE);
                layoutRegister.setVisibility(View.GONE);

                tabEntrar.setBackgroundResource(R.drawable.bg_tab_active);
                tabCadastrar.setBackgroundResource(0);

                tabCadastrar.setTextColor(getColor(R.color.text_secondary));
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
        User user = new User();
        user.setEmail_usuario(email);
        user.setSenha_usuario(senha);

        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        api.loginVerification(user).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {

                if(response.isSuccessful() && response.body() != null){
                    LoginResponse login = response.body();

                    if(login.isSuccess()){

                        User usuario = login.getUser();

                        Toast.makeText(LoginActivity.this, "Bem vindo " + usuario.getNome_usuario(), Toast.LENGTH_LONG).show();
                    }
                    else{
                        Toast.makeText(LoginActivity.this, login.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable throwable) {
                Toast.makeText(LoginActivity.this, "Erro: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    private void fazerRegistro(String nome, String telefone, String cpf, String senha, String email){
        User user = new User(nome, telefone, cpf, senha, email);

        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        api.registerNewUser(user).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    Toast.makeText(LoginActivity.this, "Usuario cadastrado", Toast.LENGTH_LONG).show();

                }
                else{
                    Toast.makeText(LoginActivity.this, "Erro: " + response.code(),Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                Toast.makeText(LoginActivity.this, "Erro" + throwable.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }
}