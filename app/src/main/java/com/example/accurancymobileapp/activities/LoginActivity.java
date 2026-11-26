package com.example.accurancymobileapp.activities;

import android.content.Intent;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.widget.CheckBox;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
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
import com.example.accurancymobileapp.fragments.RecuperarSenhaBottomSheet;
import com.example.accurancymobileapp.network.service.ApiService;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.response.LoginResponse;
import com.example.accurancymobileapp.response.PerfilResponse;
import com.example.accurancymobileapp.utils.GoogleEntrar;
import com.example.accurancymobileapp.utils.TutorialPrefs;
import com.example.accurancymobileapp.utils.PerfilCallback;
import com.example.accurancymobileapp.utils.ApiErros;
import com.example.accurancymobileapp.model.User;
import com.example.accurancymobileapp.utils.Aparelho;
import com.example.accurancymobileapp.utils.Aviso;
import com.example.accurancymobileapp.utils.SessionManager;
import com.example.accurancymobileapp.utils.TermsGate;

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

    /** Message shown when the screen opens (session ended on this device, signed out everywhere...). */
    public static final String EXTRA_AVISO = "aviso";

    EditText editEmail;
    EditText editSenha;
    EditText txtNome;
    EditText txtCelular;
    EditText txtEmailCadastro;
    EditText txtSenhaCadastro;
    EditText txtConfirmarSenha;
    Button   btnLogin;
    Button btnRegister;
    CheckBox checkTerms;
    TextView btnEsqueciSenha;
    ImageView btnToggleSenha;
    TextView tabEntrar;
    TextView tabCadastrar;
    LinearLayout btnGoogle;
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
                    // New account just confirmed: the tour opens on the first entry.
                    TutorialPrefs.marcarPendente(this, confirmado);
                    mostrarAbaEntrar();
                    if (confirmado != null) editEmail.setText(confirmado);
                    editSenha.setText("");
                    editSenha.requestFocus();
                    Aviso.makeText(this, "E-mail confirmado. Agora é só entrar.", Toast.LENGTH_LONG).show();
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
        checkTerms = findViewById(R.id.checkTerms);
        setUpTermsText(findViewById(R.id.tvTermsText));
        btnEsqueciSenha = findViewById(R.id.btnEsqueciSenha);
        btnToggleSenha = findViewById(R.id.btnToggleSenha);
        tabEntrar = findViewById(R.id.tabEntrar);
        tabCadastrar = findViewById(R.id.tabCadastrar);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnIrCadastro = findViewById(R.id.btnIrCadastro);
        layoutLogin = findViewById(R.id.layoutLogin);
        layoutRegister = findViewById(R.id.layoutRegister);

        sessionManager = new SessionManager(this);

        configurarListeners();

        // "Esqueci a senha" answers with the e-mail used, so the user only types the new password.
        getSupportFragmentManager().setFragmentResultListener(RecuperarSenhaBottomSheet.RESULTADO_CHAVE, this,
                (chave, resultado) -> {
                    mostrarAbaEntrar();
                    editEmail.setText(resultado.getString(RecuperarSenhaBottomSheet.RESULTADO_EMAIL, ""));
                    editSenha.setText("");
                    editSenha.requestFocus();
                });

        String aviso = getIntent().getStringExtra(EXTRA_AVISO);
        if (savedInstanceState == null && aviso != null && !aviso.isEmpty()) {
            Aviso.makeText(this, aviso, Toast.LENGTH_LONG).show();
        }
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
                // Celular é opcional; se preenchido, precisa ter DDD + número.
                String digitosCelular = celular.replaceAll("\\D", "");
                if(!celular.isEmpty() && (digitosCelular.length() < 10 || digitosCelular.length() > 11)){
                    txtCelular.setError("Use DDD + número (10 ou 11 dígitos) ou deixe em branco.");
                    return;
                }
                if(email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    txtEmailCadastro.setError("Informe um e-mail válido.");
                    return;
                }
                if(senhaCadastro.length() < 8){
                    txtSenhaCadastro.setError("A senha precisa ter pelo menos 8 caracteres.");
                    return;
                }
                celular = digitosCelular;
                if(confirmarSenhaCadastro.isEmpty()){
                    txtConfirmarSenha.setError("Confirme a senha.");
                    return;
                }

                if(!senhaCadastro.equals(confirmarSenhaCadastro)){
                    txtConfirmarSenha.setError("As senhas não são iguais.");
                    return;
                }

                if(!checkTerms.isChecked()){
                    Aviso.makeText(LoginActivity.this,
                            "Para criar a conta, aceite os Termos de Uso e a Política de Privacidade.",
                            Toast.LENGTH_LONG).show();
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


        btnEsqueciSenha.setOnClickListener(v -> {
            if (getSupportFragmentManager().findFragmentByTag(RecuperarSenhaBottomSheet.TAG_FRAGMENT) != null) return;
            RecuperarSenhaBottomSheet.nova(editEmail.getText().toString())
                    .show(getSupportFragmentManager(), RecuperarSenhaBottomSheet.TAG_FRAGMENT);
        });


        tabCadastrar.setOnClickListener(v -> mostrarAbaCadastrar());

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


        // "Não tem conta? Cadastre-se" e "Já tem conta? Entrar" trocam de aba nesta mesma tela.
        btnIrCadastro.setOnClickListener(v -> {
            mostrarAbaCadastrar();
            txtNome.requestFocus();
        });
        findViewById(R.id.btnIrLogin).setOnClickListener(v -> {
            mostrarAbaEntrar();
            editEmail.requestFocus();
        });
    }


    // Método de login
    private void fazerLogin(final String email, String senha) {
        User user = new User();
        user.setEmail_usuario(email);
        user.setSenha_usuario(senha);
        user.setDispositivo(Aparelho.nome());

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

                    Aviso.makeText(
                            LoginActivity.this,
                            ApiErros.mensagem(response.errorBody(),
                                    "Não foi possível entrar agora (erro " + response.code() + ")."),
                            Toast.LENGTH_LONG
                    ).show();

                    return;
                }

                if (response.body() == null) {
                    Aviso.makeText(
                            LoginActivity.this,
                            "O servidor retornou uma resposta vazia.",
                            Toast.LENGTH_LONG
                    ).show();

                    return;
                }

                LoginResponse login = response.body();

                if (login.isEmailNaoVerificado()) {
                    // Right password, but the e-mail was never confirmed: ask for the code (a new one is sent).
                    Aviso.makeText(LoginActivity.this,
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

                    Aviso.makeText(
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
                    Aviso.makeText(
                            LoginActivity.this,
                            login.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable throwable) {
                Log.e("LoginActivity","Erro ao Logar",throwable);
                Aviso.makeText(LoginActivity.this, "Erro: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    /** "Termos de Uso" and "Política de Privacidade" open the documents; the rest of the text toggles the checkbox. */
    private void setUpTermsText(TextView text) {
        String full = text.getText().toString();
        SpannableString spannable = new SpannableString(full);
        linkPart(spannable, full, "Termos de Uso", TermsGate.TERMS_URL);
        linkPart(spannable, full, "Política de Privacidade", TermsGate.PRIVACY_URL);
        text.setText(spannable);
        text.setMovementMethod(LinkMovementMethod.getInstance());
        text.setOnClickListener(v -> checkTerms.toggle());
    }

    private void linkPart(SpannableString spannable, String full, String part, String url) {
        int start = full.indexOf(part);
        if (start < 0) return;
        spannable.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                TermsGate.open(LoginActivity.this, url);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint paint) {
                super.updateDrawState(paint);
                paint.setColor(ContextCompat.getColor(LoginActivity.this, R.color.primary_blue));
                paint.setUnderlineText(false);
                paint.setFakeBoldText(true);
            }
        }, start, start + part.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void fazerRegistro(String nome, String telefone, String senha, String email){
        User user = new User(nome, telefone, senha, email);
        user.setAceiteTermos(true);

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
                        Aviso.makeText(LoginActivity.this,
                                mensagemDoCorpo(corpo, "Não foi possível concluir o cadastro."),
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    Aviso.makeText(LoginActivity.this,
                            mensagemDoCorpo(corpo, "Usuario cadastrado"), Toast.LENGTH_LONG).show();

                    final String emailCadastrado = email;
                    txtNome.setText("");
                    txtCelular.setText("");
                    txtEmailCadastro.setText("");
                    txtSenhaCadastro.setText("");
                    txtConfirmarSenha.setText("");

                    mostrarAbaEntrar();

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
                    Aviso.makeText(LoginActivity.this,
                            mensagemDoCorpo(corpo, "Não foi possível cadastrar agora (erro " + response.code() + ")."),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                Aviso.makeText(LoginActivity.this, "Erro" + throwable.toString(), Toast.LENGTH_LONG).show();
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

    private void mostrarAbaCadastrar() {
        layoutLogin.setVisibility(View.GONE);
        layoutRegister.setVisibility(View.VISIBLE);

        tabCadastrar.setBackgroundResource(R.drawable.bg_tab_active);
        tabEntrar.setBackgroundResource(0);

        tabCadastrar.setTextColor(getColor(R.color.white));
        tabEntrar.setTextColor(getColor(R.color.text_secondary));
    }

    private void mostrarAbaEntrar() {
        layoutLogin.setVisibility(View.VISIBLE);
        layoutRegister.setVisibility(View.GONE);

        tabEntrar.setBackgroundResource(R.drawable.bg_tab_active);
        tabCadastrar.setBackgroundResource(0);

        tabCadastrar.setTextColor(getColor(R.color.text_secondary));
        tabEntrar.setTextColor(getColor(R.color.white));
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
        corpo.put("dispositivo", Aparelho.nome());
        api.loginGoogle(corpo).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                LoginResponse login = response.body();
                if (response.isSuccessful() && login != null && login.isSuccess()
                        && login.getToken() != null && login.getRefreshToken() != null) {
                    sessionManager.saveTokens(login.getToken(), login.getRefreshToken());
                    String nome = login.getUser() != null ? login.getUser().getNome_usuario() : null;
                    // The API does not say whether the Google account is new, so the tour is offered
                    // once per account on this device.
                    String emailGoogle = login.getUser() != null ? login.getUser().getEmail_usuario() : null;
                    if (emailGoogle != null && !emailGoogle.isEmpty()
                            && !TutorialPrefs.jaViu(LoginActivity.this, emailGoogle)) {
                        TutorialPrefs.marcarPendente(LoginActivity.this, emailGoogle);
                    }
                    Aviso.makeText(LoginActivity.this,
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
            Aviso.makeText(this, mensagemOuNulo, Toast.LENGTH_LONG).show();
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