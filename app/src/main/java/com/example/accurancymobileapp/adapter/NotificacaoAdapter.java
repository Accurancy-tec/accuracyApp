package com.example.accurancymobileapp.adapter;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.model.Notificacao;
import com.example.accurancymobileapp.utils.TempoRelativo;

import java.util.ArrayList;
import java.util.List;

/** Lista de notificações: as não lidas ficam destacadas e com um ponto azul. */
public class NotificacaoAdapter extends RecyclerView.Adapter<NotificacaoAdapter.Linha> {

    public interface AoTocar {
        void aoTocar(@NonNull Notificacao notificacao, int posicao);
    }

    private final List<Notificacao> itens = new ArrayList<>();
    private final AoTocar aoTocar;

    public NotificacaoAdapter(@NonNull AoTocar aoTocar) {
        this.aoTocar = aoTocar;
    }

    public void definir(@NonNull List<Notificacao> novas) {
        itens.clear();
        itens.addAll(novas);
        notifyDataSetChanged();
    }

    public List<Notificacao> getItens() {
        return itens;
    }

    @DrawableRes
    private static int iconeDoTipo(String tipo) {
        switch (tipo) {
            case "Aporte":
                return R.drawable.ic_troca;
            case "Meta":
                return R.drawable.ic_bandeira;
            case "Preço":
                return R.drawable.ic_tendencia;
            case "Notícia":
                return R.drawable.ic_noticia;
            case "Aprendizado":
                return R.drawable.ic_tutorial;
            default:
                return R.drawable.ic_bell;
        }
    }

    @NonNull
    @Override
    public Linha onCreateViewHolder(@NonNull ViewGroup pai, int tipo) {
        View view = LayoutInflater.from(pai.getContext()).inflate(R.layout.item_notificacao, pai, false);
        return new Linha(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Linha linha, int posicao) {
        Notificacao n = itens.get(posicao);
        boolean naoLida = !n.isLida();

        linha.icone.setImageResource(iconeDoTipo(n.getTipo()));
        linha.titulo.setText(n.getTitulo());
        linha.titulo.setTypeface(null, naoLida ? Typeface.BOLD : Typeface.NORMAL);
        linha.mensagem.setText(n.getMensagem());
        linha.quando.setText(TempoRelativo.descrever(n.getCriadoEm()));
        linha.ponto.setVisibility(naoLida ? View.VISIBLE : View.GONE);
        linha.itemView.setBackgroundResource(naoLida
                ? R.drawable.bg_notificacao_nao_lida
                : R.drawable.bg_card_perfil);
        linha.itemView.setContentDescription((naoLida ? "Não lida. " : "")
                + n.getTitulo() + ". " + n.getMensagem() + ". " + linha.quando.getText());
        linha.itemView.setOnClickListener(v -> {
            int atual = linha.getBindingAdapterPosition();
            if (atual != RecyclerView.NO_POSITION) aoTocar.aoTocar(itens.get(atual), atual);
        });
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class Linha extends RecyclerView.ViewHolder {
        final ImageView icone;
        final TextView titulo, mensagem, quando;
        final View ponto;

        Linha(@NonNull View view) {
            super(view);
            icone = view.findViewById(R.id.imgTipoNotificacao);
            titulo = view.findViewById(R.id.tvTituloNotificacao);
            mensagem = view.findViewById(R.id.tvMensagemNotificacao);
            quando = view.findViewById(R.id.tvQuandoNotificacao);
            ponto = view.findViewById(R.id.pontoNaoLida);
        }
    }
}
