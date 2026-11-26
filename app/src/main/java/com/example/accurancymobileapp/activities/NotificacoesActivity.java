package com.example.accurancymobileapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
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
import com.example.accurancymobileapp.adapter.EsqueletoAdapter;
import com.example.accurancymobileapp.adapter.NotificacaoAdapter;
import com.example.accurancymobileapp.model.Notificacao;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.network.service.ApiService;
import com.example.accurancymobileapp.response.NotificacoesResponse;
import com.example.accurancymobileapp.utils.CallbackDaTela;
import com.example.accurancymobileapp.utils.PuxarParaAtualizar;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;

/** Avisos da conta (operações, metas, respostas da equipe) com marcação de lidas. */
public class NotificacoesActivity extends AppCompatActivity {

    private static final int LIMITE = 50;

    private ApiService api;
    private NotificacaoAdapter adapter;
    private RecyclerView lista;
    private PuxarParaAtualizar puxar;
    private MaterialButton btnMarcarTodas, btnTentarNovamente;
    private View estadoVazio;
    private ImageView imgEstado;
    private TextView tvResumo, tvTituloEstado, tvTextoEstado;
    private Call<NotificacoesResponse> chamada;
    private int naoLidas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notificacoes);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.raizNotificacoes), (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });

        api = RetrofitClient.getApiService(this);
        lista = findViewById(R.id.listaNotificacoes);
        btnMarcarTodas = findViewById(R.id.btnMarcarTodasLidas);
        btnTentarNovamente = findViewById(R.id.btnTentarNovamenteNotificacoes);
        estadoVazio = findViewById(R.id.estadoVazioNotificacoes);
        imgEstado = findViewById(R.id.imgEstadoNotificacoes);
        tvResumo = findViewById(R.id.tvResumoNotificacoes);
        tvTituloEstado = findViewById(R.id.tvTituloEstadoNotificacoes);
        tvTextoEstado = findViewById(R.id.tvTextoEstadoNotificacoes);

        adapter = new NotificacaoAdapter(this::abrir);
        lista.setLayoutManager(new LinearLayoutManager(this));

        SwipeRefreshLayout swipe = findViewById(R.id.swipeNotificacoes);
        puxar = new PuxarParaAtualizar(swipe, this::carregar);

        findViewById(R.id.btnVoltarNotificacoes).setOnClickListener(v -> finish());
        btnMarcarTodas.setOnClickListener(v -> marcarTodas());
        btnTentarNovamente.setOnClickListener(v -> {
            EsqueletoAdapter.mostrar(lista, R.layout.item_esqueleto_linha, 6);
            estadoVazio.setVisibility(View.GONE);
            carregar();
        });

        EsqueletoAdapter.mostrar(lista, R.layout.item_esqueleto_linha, 6);
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
        chamada = api.listarNotificacoes(LIMITE);
        chamada.enqueue(new CallbackDaTela<NotificacoesResponse>(this) {
            @Override
            public void aoSucesso(@NonNull NotificacoesResponse resposta) {
                puxar.concluir();
                mostrar(resposta.getNotificacoes(), resposta.getNaoLidas());
            }

            @Override
            public void aoErro(@NonNull String mensagem) {
                puxar.concluir();
                if (EsqueletoAdapter.mostrando(lista) || adapter.getItemCount() == 0) {
                    EsqueletoAdapter.ocultar(lista, adapter);
                    mostrarEstado(R.drawable.ic_bell, "Não foi possível carregar", mensagem, true);
                } else {
                    Toast.makeText(NotificacoesActivity.this, mensagem, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void mostrar(List<Notificacao> notificacoes, int totalNaoLidas) {
        adapter.definir(notificacoes);
        EsqueletoAdapter.ocultar(lista, adapter);
        naoLidas = totalNaoLidas;
        atualizarResumo();

        if (notificacoes.isEmpty()) {
            mostrarEstado(R.drawable.ic_bell, "Nenhuma notificação por enquanto",
                    "Compras, vendas, depósitos, metas alcançadas e respostas da equipe aparecem aqui.",
                    false);
        } else {
            estadoVazio.setVisibility(View.GONE);
        }
    }

    private void mostrarEstado(int icone, String titulo, String texto, boolean comBotao) {
        imgEstado.setImageResource(icone);
        tvTituloEstado.setText(titulo);
        tvTextoEstado.setText(texto);
        btnTentarNovamente.setVisibility(comBotao ? View.VISIBLE : View.GONE);
        estadoVazio.setVisibility(View.VISIBLE);
        tvResumo.setVisibility(View.GONE);
    }

    private void atualizarResumo() {
        btnMarcarTodas.setEnabled(naoLidas > 0);
        btnMarcarTodas.setAlpha(naoLidas > 0 ? 1f : 0.45f);
        if (adapter.getItemCount() == 0) {
            tvResumo.setVisibility(View.GONE);
            return;
        }
        tvResumo.setText(naoLidas == 0 ? "Tudo lido"
                : naoLidas == 1 ? "1 notificação não lida" : naoLidas + " notificações não lidas");
        tvResumo.setVisibility(View.VISIBLE);
    }

    /** Tocar numa notificação não lida marca só ela; a tela muda na hora e a API confirma depois. */
    private void abrir(@NonNull Notificacao notificacao, int posicao) {
        if (notificacao.isLida()) return;
        notificacao.setLida(true);
        adapter.notifyItemChanged(posicao);
        naoLidas = Math.max(0, naoLidas - 1);
        atualizarResumo();

        Map<String, Object> corpo = new HashMap<>();
        corpo.put("id_notificacao", notificacao.getId());
        api.marcarNotificacoesLidas(corpo).enqueue(new CallbackDaTela<NotificacoesResponse>(this) {
            @Override
            public void aoSucesso(@NonNull NotificacoesResponse resposta) {
                naoLidas = resposta.getNaoLidas();
                atualizarResumo();
            }

            @Override
            public void aoErro(@NonNull String mensagem) {
                notificacao.setLida(false);
                adapter.notifyItemChanged(posicao);
                naoLidas++;
                atualizarResumo();
            }
        });
    }

    private void marcarTodas() {
        btnMarcarTodas.setEnabled(false);
        api.marcarNotificacoesLidas(new HashMap<>()).enqueue(new CallbackDaTela<NotificacoesResponse>(this) {
            @Override
            public void aoSucesso(@NonNull NotificacoesResponse resposta) {
                for (Notificacao n : adapter.getItens()) n.setLida(true);
                adapter.notifyItemRangeChanged(0, adapter.getItemCount());
                naoLidas = resposta.getNaoLidas();
                atualizarResumo();
            }

            @Override
            public void aoErro(@NonNull String mensagem) {
                atualizarResumo();
                Toast.makeText(NotificacoesActivity.this, mensagem, Toast.LENGTH_LONG).show();
            }
        });
    }
}
