package com.example.accurancymobileapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.network.service.ApiService;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.response.LoginResponse;
import com.example.accurancymobileapp.response.PerfilResponse;
import com.example.accurancymobileapp.utils.GoogleEntrar;
import com.example.accurancymobileapp.utils.PerfilCallback;
import com.example.accurancymobileapp.utils.ApiErros;
import com.example.accurancymobileapp.model.User;
import com.example.accurancymobileapp.utils.SessionManager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    EditText editEmail;
    EditText editSenha;
    EditText txtNome;
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
    private boolean googleEmAndamento = false;
    private SessionManager sessionManager;

    /** The verification screen answers with the confirmed e-mail so the login field is already filled. */
    private final ActivityResultLauncher<Intent> verificacaoLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            resultado -> {
                if (resultado.getResultCode() == RESULT_OK && resultado.getData() != null) {
                    String confirmado = resultado.getData()
                            .getStringExtra(VerificarEmailActivity.EXTRA_EMAIL);
                    mostrarAbaEntrar();
                    if (confirmado != null) editEmail.setText(confirmado);
                    editSenha.setText("");
                    editSenha.requestFocus();
                    Toast.makeText(this, "E-mail confirmado. Agora é só entrar.", Toast.LENGTH_LONG).show();
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isLoggedVerify();

        editEmail = findViewById(R.id.editEmail);
        editSenha = findViewById(R.id.editSenha);
        txtNome = findViewById(R.id.txtNome);
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

        sessionManager = new SessionManager(this);

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
                String celular = txtCelular.getText().toString().trim();
                String email = txtEmailCadastro.getText().toString().trim();
                String senhaCadastro = txtSenhaCadastro.getText().toString().trim();
                String confirmarSenhaCadastro = txtConfirmarSenha.getText().toString().trim();


                if(nome.isEmpty()){
                    txtNome.setError("Informe o nome");
                    return;
                }
                if(celular.isEmpty()){
                    txtCelular.setError("Informe o Celular.");
                    return;
                }
                if(email.isEmpty()){
                    txtEmailCadastro.setError("Informe o E-mail.");
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

                fazerRegistro(nome, celular, senhaCadastro, email);

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
                mostrarAbaEntrar();
            }
        });


        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entrarComGoogle();
            }
        });


        btnGitHub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
    private void fazerLogin(final String email, String senha) {
        User user = new User();
        user.setEmail_usuario(email);
        user.setSenha_usuario(senha);

        ApiService api = RetrofitClient.getClient(this).create(ApiService.class);
        api.loginVerification(user).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(
                    Call<LoginResponse> call,
                    Response<LoginResponse> response
            ) {
                if (!response.isSuccessful()) {
                    Log.e(
                            "LoginActivity",
                            "Erro HTTP: " + response.code()
                    );

                    Toast.makeText(
                            LoginActivity.this,
                            "Erro no servidor: HTTP " + response.code(),
                            Toast.LENGTH_LONG
                    ).show();

                    return;
                }

                if (response.body() == null) {
                    Toast.makeText(
                            LoginActivity.this,
                            "O servidor retornou uma resposta vazia.",
                            Toast.LENGTH_LONG
                    ).show();

                    return;
                }

                LoginResponse login = response.body();

                if (login.isEmailNaoVerificado()) {
                    // Right password, but the e-mail was never confirmed: ask for the code (a new one is sent).
                    Toast.makeText(LoginActivity.this,
                            login.getMessage() != null ? login.getMessage() : "Confirme seu e-mail para entrar.",
                            Toast.LENGTH_LONG).show();
                    abrirVerificacao(login.getEmailUsuario() != null ? login.getEmailUsuario() : email, true);
                    return;
                }

                if (login.isSuccess()) {
                    User usuario = login.getUser();

                    sessionManager.saveTokens(
                            login.getToken(),
                            login.getRefreshToken());

                    Toast.makeText(
                            LoginActivity.this,
                            "Bem-vindo " + usuario.getNome_usuario(),
                            Toast.LENGTH_LONG
                    ).show();

                    Intent it = new Intent(
                            LoginActivity.this,
                            MainPageActivity.class
                    );

                    startActivity(it);
                    finish();

                } else {
                    Toast.makeText(
                            LoginActivity.this,
                            login.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable throwable) {
                Log.e("LoginActivity","Erro ao Logar",throwable);
                Toast.makeText(LoginActivity.this, "Erro: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    private void fazerRegistro(String nome, String telefone, String senha, String email){
        User user = new User(nome, telefone, senha, email);

        ApiService api = RetrofitClient.getClient(this).create(ApiService.class);
        api.registerNewUser(user).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    // The API answers HTTP 200 even for refusals: the JSON "status" says what happened.
                    JsonObject corpo = lerCorpo(response.body());
                    String status = corpo != null && corpo.has("status") && !corpo.get("status").isJsonNull()
                            ? corpo.get("status").getAsString() : "";
                    if(!"sucesso".equals(status)){
                        Toast.makeText(LoginActivity.this,
                                mensagemDoCorpo(corpo, "Não foi possível concluir o cadastro."),
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    Toast.makeText(LoginActivity.this,
                            mensagemDoCorpo(corpo, "Usuario cadastrado"), Toast.LENGTH_LONG).show();

                    final String emailCadastrado = email;
                    txtNome.setText("");
                    txtCelular.setText("");
                    txtEmailCadastro.setText("");
                    txtSenhaCadastro.setText("");
                    txtConfirmarSenha.setText("");

                    layoutLogin.setVisibility(View.VISIBLE);
                    layoutRegister.setVisibility(View.GONE);

                    tabEntrar.setBackgroundResource(R.drawable.bg_tab_active);
                    tabCadastrar.setBackgroundResource(0);

                    tabCadastrar.setTextColor(getColor(R.color.text_secondary));
                    tabEntrar.setTextColor(getColor(R.color.text_primary));

                    // The API already e-mailed the code: ask for it now.
                    abrirVerificacao(emailCadastrado, false);
                }
                else{
                    JsonObject corpo = null;
                    try {
                        corpo = lerCorpo(response.errorBody());
                    } catch (Exception ignored) {
                        // Falls back to the generic message below.
                    }
                    Toast.makeText(LoginActivity.this,
                            mensagemDoCorpo(corpo, "Não foi possível cadastrar agora (erro " + response.code() + ")."),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                Toast.makeText(LoginActivity.this, "Erro" + throwable.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /** Reads a JSON object from a response body; null when it is empty or not a JSON object. */
    private static JsonObject lerCorpo(ResponseBody corpo) {
        if (corpo == null) return null;
        try {
            JsonElement elemento = JsonParser.parseString(corpo.string());
            return elemento.isJsonObject() ? elemento.getAsJsonObject() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static String mensagemDoCorpo(JsonObject corpo, String padrao) {
        if (corpo != null) {
            for (String chave : new String[]{"mensagem", "message"}) {
                if (corpo.has(chave) && !corpo.get(chave).isJsonNull()) {
                    String texto = corpo.get(chave).getAsString();
                    if (!texto.trim().isEmpty()) return texto;
                }
            }
        }
        return padrao;
    }

    private void mostrarAbaEntrar() {
        layoutLogin.setVisibility(View.VISIBLE);
        layoutRegister.setVisibility(View.GONE);

        tabEntrar.setBackgroundResource(R.drawable.bg_tab_active);
        tabCadastrar.setBackgroundResource(0);

        tabCadastrar.setTextColor(getColor(R.color.text_secondary));
        tabEntrar.setTextColor(getColor(R.color.text_primary));
    }

    private void abrirVerificacao(String email, boolean enviarCodigoAoAbrir) {
        Intent it = new Intent(this, VerificarEmailActivity.class);
        it.putExtra(VerificarEmailActivity.EXTRA_EMAIL, email);
        it.putExtra(VerificarEmailActivity.EXTRA_ENVIAR_AO_ABRIR, enviarCodigoAoAbrir);
        verificacaoLauncher.launch(it);
    }

    /** Google flow: client id from the API, Google account card, id_token to the API, session. */
    private void entrarComGoogle() {
        if (googleEmAndamento) return;
        googleEmAndamento = true;
        btnGoogle.setEnabled(false);
        btnGoogle.setAlpha(0.6f);

        final ApiService api = RetrofitClient.getApiService(this);
        api.googleConfig().enqueue(new PerfilCallback(this) {
            @Override
            public void aoSucesso(PerfilResponse resposta) {
                String clientId = resposta.getClientId();
                if (clientId == null || clientId.trim().isEmpty()) {
                    terminarGoogle("Login com Google indisponível no momento.");
                    return;
                }
                GoogleEntrar.iniciar(LoginActivity.this, clientId.trim(), new GoogleEntrar.Resultado() {
                    @Override
                    public void aoObterToken(String idToken) {
                        enviarTokenGoogle(api, idToken);
                    }

                    @Override
                    public void aoCancelar() {
                        terminarGoogle(null);
                    }

                    @Override
                    public void aoFalhar(String mensagem) {
                        terminarGoogle(mensagem);
                    }
                });
            }

            @Override
            public void aoErro(String mensagem) {
                terminarGoogle(mensagem);
            }
        });
    }

    private void enviarTokenGoogle(ApiService api, String idToken) {
        Map<String, String> corpo = new HashMap<>();
        corpo.put("id_token", idToken);
        api.loginGoogle(corpo).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                LoginResponse login = response.body();
                if (response.isSuccessful() && login != null && login.isSuccess()
                        && login.getToken() != null && login.getRefreshToken() != null) {
                    sessionManager.saveTokens(login.getToken(), login.getRefreshToken());
                    String nome = login.getUser() != null ? login.getUser().getNome_usuario() : null;
                    Toast.makeText(LoginActivity.this,
                            nome != null && !nome.isEmpty() ? "Bem-vindo " + nome : "Bem-vindo!",
                            Toast.LENGTH_LONG).show();
                    startActivity(new Intent(LoginActivity.this, MainPageActivity.class));
                    finish();
                    return;
                }
                String mensagem = login != null && login.getMessage() != null
                        ? login.getMessage()
                        : ApiErros.mensagem(response.errorBody(),
                        "Não foi possível entrar com o Google (erro " + response.code() + ").");
                terminarGoogle(mensagem);
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable erro) {
                if (isFinishing() || isDestroyed()) return;
                terminarGoogle("Sem conexão com o servidor. Verifique a internet e tente de novo.");
            }
        });
    }

    private void terminarGoogle(String mensagemOuNulo) {
        googleEmAndamento = false;
        btnGoogle.setEnabled(true);
        btnGoogle.setAlpha(1f);
        if (mensagemOuNulo != null) {
            Toast.makeText(this, mensagemOuNulo, Toast.LENGTH_LONG).show();
        }
    }

    public void isLoggedVerify(){
        sessionManager = new SessionManager(this);

        if(sessionManager.isLoggedIn()){
            Intent it = new Intent(LoginActivity.this, MainPageActivity.class);
            startActivity(it);
            finish();
        }
        return;
    }
}