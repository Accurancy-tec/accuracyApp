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
import com.example.accurancymobileapp.utils.FavoritoUi;
import com.example.accurancymobileapp.utils.FavoritosCache;
import com.example.accurancymobileapp.utils.SiglaIcone;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AcoesAdapter extends RecyclerView.Adapter<AcoesAdapter.ViewHolder> {

    public interface OnAcaoClickListener {
        void onAcaoClick(Acao acao);
    }

    /** Called when the heart of a row is tapped; the host changes the favorite and the row redraws. */
    public interface OnFavoritoClickListener {
        void onFavoritoClick(Acao acao, int posicao);
    }

    private final List<Acao> acoes = new ArrayList<>();
    private OnFavoritoClickListener favoritoListener;
    private String categoriaFavoritos;
    private final NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
    private final OnAcaoClickListener listener;

    public AcoesAdapter(List<Acao> acoes) {
        this(acoes, null);
    }

    public AcoesAdapter(List<Acao> acoes, OnAcaoClickListener listener) {
        if (acoes != null) this.acoes.addAll(acoes);
        this.listener = listener;
    }

    /** Shows a heart on every row, using the given category to identify the asset. */
    public void habilitarFavoritos(String categoria, OnFavoritoClickListener listener) {
        this.categoriaFavoritos = categoria;
        this.favoritoListener = listener;
        notifyDataSetChanged();
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

        // No logo: the initials of the symbol (or the old icon when there are none); the logo replaces it when it loads.
        SiglaIcone.aplicar(holder.txtIconAtivo, acao.getStock(), R.drawable.ic_ativo_placeholder);
        SvgLogoLoader.carregar(acao.getLogo(), holder.txtIconAtivo);

        Double variacao = acao.getChange();
        if (variacao == null || !Double.isFinite(variacao)) {
            holder.txtVariacaoAtivo.setText("—");
            holder.txtVariacaoAtivo.setTextColor(ContextCompat.getColor(
                    holder.itemView.getContext(), R.color.text_secondary
            ));
        } else {
            double valorVariacao = variacao;
            String seta = valorVariacao > 0 ? "▲" : valorVariacao < 0 ? "▼" : "•";
            holder.txtVariacaoAtivo.setText(String.format(
                    Locale.forLanguageTag("pt-BR"), "%s %.2f%%", seta, Math.abs(valorVariacao)
            ));
            int cor = valorVariacao < 0 ? R.color.red_negative
                    : valorVariacao > 0 ? R.color.green_positive
                    : R.color.text_secondary;
            holder.txtVariacaoAtivo.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), cor));
        }

        String rotuloPreco = acao.getPriceLabel();
        Double fechamento = acao.getClose();
        if (rotuloPreco != null && !rotuloPreco.trim().isEmpty()) {
            holder.txtValorAtivo.setText(rotuloPreco);
        } else {
            holder.txtValorAtivo.setText(
                    fechamento == null || !Double.isFinite(fechamento) || fechamento <= 0
                            ? "—"
                            : formatoMoeda.format(fechamento)
            );
        }
        if (favoritoListener == null) {
            holder.btnFavoritoItem.setVisibility(View.GONE);
        } else {
            holder.btnFavoritoItem.setVisibility(View.VISIBLE);
            FavoritoUi.aplicar(holder.btnFavoritoItem,
                    FavoritosCache.contem(acao.getStock(), categoriaFavoritos));
            holder.btnFavoritoItem.setOnClickListener(v -> {
                int posicaoAtual = holder.getBindingAdapterPosition();
                if (posicaoAtual == RecyclerView.NO_POSITION) return;
                favoritoListener.onFavoritoClick(acoes.get(posicaoAtual), posicaoAtual);
                // The cache is updated before the request is sent, so the heart can change at once.
                FavoritoUi.aplicarComAnimacao(holder.btnFavoritoItem,
                        FavoritosCache.contem(acao.getStock(), categoriaFavoritos));
            });
        }
        holder.itemView.setOnClickListener(v -> {
            int posicaoAtual = holder.getBindingAdapterPosition();
            if (listener != null && posicaoAtual != RecyclerView.NO_POSITION) {
                listener.onAcaoClick(acoes.get(posicaoAtual));
            }
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
        final ImageView btnFavoritoItem;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtIconAtivo = itemView.findViewById(R.id.txtIconAtivo);
            txtNomeAtivo = itemView.findViewById(R.id.txtNomeAtivo);
            txtSubtituloAtivo = itemView.findViewById(R.id.txtSubtituloAtivo);
            txtValorAtivo = itemView.findViewById(R.id.txtValorAtivo);
            txtVariacaoAtivo = itemView.findViewById(R.id.txtVariacaoAtivo);
            btnFavoritoItem = itemView.findViewById(R.id.btnFavoritoItem);
        }
    }
}
