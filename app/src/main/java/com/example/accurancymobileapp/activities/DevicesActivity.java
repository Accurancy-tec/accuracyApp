package com.example.accurancymobileapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.adapter.DeviceAdapter;
import com.example.accurancymobileapp.adapter.EsqueletoAdapter;
import com.example.accurancymobileapp.model.DeviceSession;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.network.service.ApiService;
import com.example.accurancymobileapp.response.SessionsResponse;
import com.example.accurancymobileapp.utils.Aviso;
import com.example.accurancymobileapp.utils.CallbackDaTela;
import com.example.accurancymobileapp.utils.PuxarParaAtualizar;
import com.example.accurancymobileapp.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;

/** Onde a conta está aberta: desconecta um aparelho, todos os outros ou todos de uma vez. */
public class DevicesActivity extends AppCompatActivity {

    private ApiService api;
    private DeviceAdapter adapter;
    private RecyclerView lista;
    private PuxarParaAtualizar puxar;
    private View estado, acoes;
    private TextView tvTituloEstado, tvTextoEstado;
    private MaterialButton btnOutros, btnTodos;
    private Call<SessionsResponse> chamada;
    private boolean ocupado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_devices);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.raizDispositivos), (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });

        api = RetrofitClient.getApiService(this);
        lista = findViewById(R.id.listaDispositivos);
        estado = findViewById(R.id.estadoDispositivos);
        acoes = findViewById(R.id.acoesDispositivos);
        tvTituloEstado = findViewById(R.id.tvTituloEstadoDispositivos);
        tvTextoEstado = findViewById(R.id.tvTextoEstadoDispositivos);
        btnOutros = findViewById(R.id.btnDesconectarOutros);
        btnTodos = findViewById(R.id.btnSairDeTodos);

        adapter = new DeviceAdapter(this::confirmarDesconexao);
        lista.setLayoutManager(new LinearLayoutManager(this));
        SwipeRefreshLayout swipe = findViewById(R.id.swipeDispositivos);
        puxar = new PuxarParaAtualizar(swipe, this::carregar);

        findViewById(R.id.btnVoltarDispositivos).setOnClickListener(v -> finish());
        findViewById(R.id.btnTentarNovamenteDispositivos).setOnClickListener(v -> {
            estado.setVisibility(View.GONE);
            EsqueletoAdapter.mostrar(lista, R.layout.item_esqueleto_linha, 3);
            carregar();
        });
        btnOutros.setOnClickListener(v -> confirmarDesconectarOutros());
        btnTodos.setOnClickListener(v -> confirmarSairDeTodos());

        EsqueletoAdapter.mostrar(lista, R.layout.item_esqueleto_linha, 3);
        carregar();
    }

    @Override
    protected void onDestroy() {
        if (chamada != null) chamada.cancel();
        super.onDestroy();
    }

    private void carregar() {
        if (chamada != null) chamada.cancel();
        puxar.iniciar();
        chamada = api.listarSessoes();
        chamada.enqueue(new CallbackDaTela<SessionsResponse>(this) {
            @Override
            public void aoSucesso(@NonNull SessionsResponse resposta) {
                puxar.concluir();
                mostrar(resposta.getSessions());
            }

            @Override
            public void aoErro(@NonNull String mensagem) {
                puxar.concluir();
                if (EsqueletoAdapter.mostrando(lista) || adapter.getItemCount() == 0) {
                    EsqueletoAdapter.ocultar(lista, adapter);
                    acoes.setVisibility(View.GONE);
                    tvTituloEstado.setText("Não foi possível carregar");
                    tvTextoEstado.setText(mensagem);
                    estado.setVisibility(View.VISIBLE);
                } else {
                    Aviso.makeText(DevicesActivity.this, mensagem, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void mostrar(List<DeviceSession> sessoes) {
        adapter.definir(sessoes);
        EsqueletoAdapter.ocultar(lista, adapter);
        estado.setVisibility(View.GONE);

        int outros = 0;
        for (DeviceSession s : sessoes) if (!s.isCurrent()) outros++;
        btnOutros.setEnabled(outros > 0);
        btnOutros.setAlpha(outros > 0 ? 1f : 0.45f);
        btnOutros.setText(outros == 0 ? "Nenhum outro aparelho conectado"
                : outros == 1 ? "Desconectar o outro aparelho" : "Desconectar os outros " + outros + " aparelhos");
        acoes.setVisibility(View.VISIBLE);
    }

    private void confirmarDesconexao(@NonNull DeviceSession sessao) {
        if (ocupado) return;
        if (sessao.isCurrent()) {
            confirmar("Sair deste aparelho?",
                    "Os outros aparelhos continuam conectados. Para voltar aqui, entre com seu e-mail e senha.",
                    "Sair", () -> desconectar(sessao));
            return;
        }
        confirmar("Desconectar " + sessao.getDeviceName() + "?",
                "A conta fecha nesse aparelho na hora. Se você não reconhece esse acesso, troque a senha também.",
                "Desconectar", () -> desconectar(sessao));
    }

    private void confirmarDesconectarOutros() {
        if (ocupado) return;
        confirmar("Desconectar os outros aparelhos?",
                "Só este celular continua com a conta aberta.",
                "Desconectar", () -> desconectarTodos(true));
    }

    private void confirmarSairDeTodos() {
        if (ocupado) return;
        confirmar("Sair de todos os aparelhos?",
                "A conta fecha em todos os lugares, inclusive neste celular.",
                "Sair de todos", () -> desconectarTodos(false));
    }

    private void confirmar(String titulo, String texto, String acao, Runnable aoConfirmar) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(titulo)
                .setMessage(texto)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton(acao, (d, w) -> aoConfirmar.run())
                .show();
    }

    private void desconectar(@NonNull DeviceSession sessao) {
        travar(true);
        Map<String, Object> corpo = new HashMap<>();
        corpo.put("id_sessao", sessao.getId());
        api.encerrarSessao(corpo).enqueue(new CallbackDaTela<SessionsResponse>(this) {
            @Override
            public void aoSucesso(@NonNull SessionsResponse resposta) {
                travar(false);
                if (resposta.isCurrentSessionEnded()) {
                    voltarParaLogin("Você saiu deste aparelho.");
                    return;
                }
                Aviso.makeText(DevicesActivity.this, resposta.texto("Aparelho desconectado."),
                        Toast.LENGTH_SHORT).show();
                carregar();
            }

            @Override
            public void aoErro(@NonNull String mensagem) {
                travar(false);
                Aviso.makeText(DevicesActivity.this, mensagem, Toast.LENGTH_LONG).show();
                carregar();
            }
        });
    }

    private void desconectarTodos(boolean manterEste) {
        travar(true);
        Map<String, Object> corpo = new HashMap<>();
        corpo.put("manter_atual", manterEste);
        api.encerrarSessoes(corpo).enqueue(new CallbackDaTela<SessionsResponse>(this) {
            @Override
            public void aoSucesso(@NonNull SessionsResponse resposta) {
                travar(false);
                if (resposta.isCurrentSessionEnded()) {
                    voltarParaLogin("Você saiu de todos os aparelhos.");
                    return;
                }
                Aviso.makeText(DevicesActivity.this,
                        resposta.texto("Os outros aparelhos foram desconectados."), Toast.LENGTH_SHORT).show();
                carregar();
            }

            @Override
            public void aoErro(@NonNull String mensagem) {
                travar(false);
                Aviso.makeText(DevicesActivity.this, mensagem, Toast.LENGTH_LONG).show();
                carregar();
            }
        });
    }

    private void travar(boolean valor) {
        ocupado = valor;
        adapter.habilitar(!valor);
        btnTodos.setEnabled(!valor);
        if (valor) btnOutros.setEnabled(false);
    }

    private void voltarParaLogin(String aviso) {
        new SessionManager(this).logout();
        Intent it = new Intent(this, LoginActivity.class);
        it.putExtra(LoginActivity.EXTRA_AVISO, aviso);
        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(it);
        finish();
    }
}
