package com.example.accurancymobileapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.model.PerguntaUsuario;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.network.service.ApiService;
import com.example.accurancymobileapp.response.PerguntasResponse;
import com.example.accurancymobileapp.response.RespostaSimples;
import com.example.accurancymobileapp.utils.CallbackDaTela;
import com.example.accurancymobileapp.utils.ConteudoAjuda;
import com.example.accurancymobileapp.utils.TempoRelativo;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Perguntas frequentes e o "Fale com a equipe": quem não encontra a resposta manda a dúvida
 * pelo mesmo canal do site e acompanha aqui a resposta da equipe.
 */
public class AjudaActivity extends AppCompatActivity {

    /** Extra booleano: abre a tela já rolada até o formulário de contato. */
    public static final String EXTRA_ABRIR_CONTATO = "abrir_contato";

    private ApiService api;
    private NestedScrollView scroll;
    private View secaoContato;
    private EditText etAssunto, etMensagem;
    private TextView tvAviso, tvTituloMinhasPerguntas;
    private LinearLayout listaMinhasPerguntas;
    private MaterialButton btnEnviar;
    private boolean enviando = false;

    public static Intent intent(@NonNull Context contexto, boolean abrirContato) {
        return new Intent(contexto, AjudaActivity.class).putExtra(EXTRA_ABRIR_CONTATO, abrirContato);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ajuda);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.raizAjuda), (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });

        api = RetrofitClient.getApiService(this);
        scroll = findViewById(R.id.scrollAjuda);
        secaoContato = findViewById(R.id.secaoContato);
        etAssunto = findViewById(R.id.etAssuntoPergunta);
        etMensagem = findViewById(R.id.etMensagemPergunta);
        tvAviso = findViewById(R.id.tvAvisoPergunta);
        tvTituloMinhasPerguntas = findViewById(R.id.tvTituloMinhasPerguntas);
        listaMinhasPerguntas = findViewById(R.id.listaMinhasPerguntas);
        btnEnviar = findViewById(R.id.btnEnviarPergunta);

        findViewById(R.id.btnVoltarAjuda).setOnClickListener(v -> finish());
        btnEnviar.setOnClickListener(v -> enviarPergunta());
        // Um aviso antigo some assim que a pessoa volta a editar a mensagem.
        TextWatcher limparAviso = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int inicio, int antes, int depois) {}

            @Override
            public void onTextChanged(CharSequence s, int inicio, int antes, int depois) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!enviando && s.length() > 0) tvAviso.setVisibility(View.GONE);
            }
        };
        etAssunto.addTextChangedListener(limparAviso);
        etMensagem.addTextChangedListener(limparAviso);

        montarPerguntasFrequentes(findViewById(R.id.listaPerguntasFrequentes));
        carregarMinhasPerguntas();

        if (savedInstanceState == null && getIntent().getBooleanExtra(EXTRA_ABRIR_CONTATO, false)) {
            scroll.post(() -> {
                scroll.smoothScrollTo(0, secaoContato.getTop());
                etAssunto.requestFocus();
            });
        }
    }

    // ------------------------------------------------------- perguntas frequentes

    private void montarPerguntasFrequentes(LinearLayout destino) {
        LayoutInflater inflater = LayoutInflater.from(this);
        for (ConteudoAjuda.Secao secao : ConteudoAjuda.secoes()) {
            TextView titulo = (TextView) inflater.inflate(R.layout.item_secao_ajuda, destino, false);
            titulo.setText(secao.titulo);
            destino.addView(titulo);

            LinearLayout cartao = (LinearLayout) inflater.inflate(R.layout.item_cartao_ajuda, destino, false);
            List<ConteudoAjuda.Pergunta> perguntas = secao.perguntas;
            for (int i = 0; i < perguntas.size(); i++) {
                View item = inflater.inflate(R.layout.item_pergunta_frequente, cartao, false);
                preencherPergunta(item, perguntas.get(i), i == perguntas.size() - 1);
                cartao.addView(item);
            }
            destino.addView(cartao);
        }
    }

    private void preencherPergunta(View item, ConteudoAjuda.Pergunta pergunta, boolean ultima) {
        TextView tvPergunta = item.findViewById(R.id.tvPerguntaFrequente);
        TextView tvResposta = item.findViewById(R.id.tvRespostaFrequente);
        ImageView seta = item.findViewById(R.id.imgExpandirPergunta);
        View cabecalho = item.findViewById(R.id.cabecalhoPergunta);

        tvPergunta.setText(pergunta.pergunta);
        tvResposta.setText(pergunta.resposta);
        item.findViewById(R.id.divisorPergunta).setVisibility(ultima ? View.GONE : View.VISIBLE);
        cabecalho.setContentDescription(pergunta.pergunta + ". Toque para ver a resposta.");

        cabecalho.setOnClickListener(v -> {
            boolean abrir = tvResposta.getVisibility() != View.VISIBLE;
            tvResposta.setVisibility(abrir ? View.VISIBLE : View.GONE);
            seta.animate().rotation(abrir ? 180f : 0f).setDuration(180).start();
            cabecalho.setContentDescription(pergunta.pergunta
                    + (abrir ? ". Toque para esconder a resposta." : ". Toque para ver a resposta."));
        });
    }

    // -------------------------------------------------------------- contato

    private void enviarPergunta() {
        if (enviando) return;
        String assunto = etAssunto.getText().toString().trim();
        String mensagem = etMensagem.getText().toString().trim();

        if (assunto.isEmpty()) {
            avisar("Escreva um assunto curto para a sua dúvida.", true);
            etAssunto.requestFocus();
            return;
        }
        if (mensagem.length() < 10) {
            avisar("Conte um pouco mais sobre a dúvida (pelo menos 10 caracteres).", true);
            etMensagem.requestFocus();
            return;
        }

        definirEnviando(true);
        Map<String, String> corpo = new HashMap<>();
        corpo.put("assunto", assunto);
        corpo.put("mensagem_pergunta", mensagem);

        api.enviarPergunta(corpo).enqueue(new CallbackDaTela<RespostaSimples>(this) {
            @Override
            public void aoSucesso(@NonNull RespostaSimples resposta) {
                definirEnviando(false);
                etAssunto.setText("");
                etMensagem.setText("");
                esconderTeclado();
                avisar(resposta.texto("Pergunta enviada. Você será avisado quando houver resposta."), false);
                carregarMinhasPerguntas();
            }

            @Override
            public void aoErro(@NonNull String mensagemErro) {
                definirEnviando(false);
                avisar(mensagemErro, true);
            }
        });
    }

    private void definirEnviando(boolean valor) {
        enviando = valor;
        btnEnviar.setEnabled(!valor);
        btnEnviar.setText(valor ? "Enviando..." : "Enviar pergunta");
        etAssunto.setEnabled(!valor);
        etMensagem.setEnabled(!valor);
    }

    private void avisar(String texto, boolean erro) {
        tvAviso.setText(texto);
        tvAviso.setTextColor(ContextCompat.getColor(this, erro ? R.color.negativo_destaque : R.color.green_positive));
        tvAviso.setVisibility(View.VISIBLE);
    }

    private void esconderTeclado() {
        InputMethodManager teclado = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View foco = getCurrentFocus();
        if (teclado != null && foco != null) teclado.hideSoftInputFromWindow(foco.getWindowToken(), 0);
    }

    // ------------------------------------------------------- minhas perguntas

    private void carregarMinhasPerguntas() {
        api.listarPerguntas().enqueue(new CallbackDaTela<PerguntasResponse>(this) {
            @Override
            public void aoSucesso(@NonNull PerguntasResponse resposta) {
                mostrarMinhasPerguntas(resposta.getPerguntas());
            }

            @Override
            public void aoErro(@NonNull String mensagem) {
                // Sem a lista, o formulário continua funcionando; nada a mostrar.
            }
        });
    }

    private void mostrarMinhasPerguntas(List<PerguntaUsuario> perguntas) {
        listaMinhasPerguntas.removeAllViews();
        tvTituloMinhasPerguntas.setVisibility(perguntas.isEmpty() ? View.GONE : View.VISIBLE);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (PerguntaUsuario pergunta : perguntas) {
            View item = inflater.inflate(R.layout.item_minha_pergunta, listaMinhasPerguntas, false);
            ((TextView) item.findViewById(R.id.tvAssuntoMinhaPergunta)).setText(pergunta.getAssunto());
            ((TextView) item.findViewById(R.id.tvTextoMinhaPergunta)).setText(pergunta.getMensagem());
            ((TextView) item.findViewById(R.id.tvDataMinhaPergunta))
                    .setText("Enviada " + TempoRelativo.descrever(pergunta.getCriadoEm()));

            TextView situacao = item.findViewById(R.id.tvSituacaoMinhaPergunta);
            if (pergunta.isRespondida()) {
                situacao.setText("Respondida");
                situacao.setBackgroundResource(R.drawable.bg_situacao_respondida);
                situacao.setTextColor(ContextCompat.getColor(this, R.color.green_positive));
                item.findViewById(R.id.blocoRespostaMinhaPergunta).setVisibility(View.VISIBLE);
                ((TextView) item.findViewById(R.id.tvRespostaMinhaPergunta)).setText(pergunta.getResposta());
            } else {
                situacao.setText("Aguardando");
                situacao.setBackgroundResource(R.drawable.bg_situacao_aguardando);
                situacao.setTextColor(ContextCompat.getColor(this, R.color.aviso));
            }
            listaMinhasPerguntas.addView(item);
        }
    }
}
