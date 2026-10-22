package com.example.accurancymobileapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.model.Acao;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AcoesAdapter extends RecyclerView.Adapter<AcoesAdapter.ViewHolder> {

    public interface OnAcaoClickListener {
        void onAcaoClick(Acao acao);
    }

    private final List<Acao> acoes = new ArrayList<>();
    private final NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
    private final OnAcaoClickListener listener;

    public AcoesAdapter(List<Acao> acoes) {
        this(acoes, null);
    }

    public AcoesAdapter(List<Acao> acoes, OnAcaoClickListener listener) {
        if (acoes != null) this.acoes.addAll(acoes);
        this.listener = listener;
    }

    public void atualizarLista(List<Acao> novasAcoes) {
        acoes.clear();
        if (novasAcoes != null) acoes.addAll(novasAcoes);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_investiment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Acao acao = acoes.get(position);
        String ticker = textoOuPadrao(acao.getStock(), "—");
        holder.txtNomeAtivo.setText(ticker);
        holder.txtSubtituloAtivo.setText(textoOuPadrao(acao.getName(), "Nome não informado"));
        holder.txtIconAtivo.setImageResource(R.drawable.ic_ativo_placeholder);
        if (acao.getLogo() != null && !acao.getLogo().trim().isEmpty()) {
            SvgLogoLoader.carregar(acao.getLogo(), holder.txtIconAtivo);
        }

        Double variacao = acao.getChange();
        double valorVariacao = variacao == null ? 0.0 : variacao;
        holder.txtVariacaoAtivo.setText(String.format(Locale.forLanguageTag("pt-BR"), "%s %.2f%%", valorVariacao >= 0 ? "▲" : "▼", Math.abs(valorVariacao)));
        holder.txtVariacaoAtivo.setTextColor(ContextCompat.getColor(
                holder.itemView.getContext(), valorVariacao >= 0 ? R.color.green_positive : R.color.red_negative
        ));
        Double fechamento = acao.getClose();
        holder.txtValorAtivo.setText(fechamento == null ? "—" : formatoMoeda.format(fechamento));

        holder.itemView.setOnClickListener(v -> {
            int posicaoAtual = holder.getBindingAdapterPosition();
            if (listener != null && posicaoAtual != RecyclerView.NO_POSITION) listener.onAcaoClick(acoes.get(posicaoAtual));
        });
    }

    private String textoOuPadrao(String valor, String padrao) {
        return valor == null || valor.trim().isEmpty() ? padrao : valor;
    }

    @Override
    public int getItemCount() {
        return acoes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView txtIconAtivo;
        final TextView txtNomeAtivo;
        final TextView txtSubtituloAtivo;
        final TextView txtValorAtivo;
        final TextView txtVariacaoAtivo;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtIconAtivo = itemView.findViewById(R.id.txtIconAtivo);
            txtNomeAtivo = itemView.findViewById(R.id.txtNomeAtivo);
            txtSubtituloAtivo = itemView.findViewById(R.id.txtSubtituloAtivo);
            txtValorAtivo = itemView.findViewById(R.id.txtValorAtivo);
            txtVariacaoAtivo = itemView.findViewById(R.id.txtVariacaoAtivo);
        }
    }
}
