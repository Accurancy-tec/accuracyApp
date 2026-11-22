package com.example.accurancymobileapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.model.FavoritoAtivo;
import com.example.accurancymobileapp.utils.AnimacaoUtils;
import com.example.accurancymobileapp.utils.FavoritoUi;
import com.example.accurancymobileapp.utils.SiglaIcone;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** List of the user's favorite assets (symbol, name, category and a filled heart to remove). */
public class FavoritosAdapter extends RecyclerView.Adapter<FavoritosAdapter.ViewHolder> {

    public interface Listener {
        void aoAbrir(FavoritoAtivo favorito);

        void aoRemover(FavoritoAtivo favorito);
    }

    private final List<FavoritoAtivo> itens = new ArrayList<>();
    private final Listener listener;

    public FavoritosAdapter(Listener listener) {
        this.listener = listener;
    }

    public void atualizar(List<FavoritoAtivo> novos) {
        itens.clear();
        if (novos != null) itens.addAll(novos);
        notifyDataSetChanged();
    }

    /** Removes the row with a slide animation; returns false when the asset is not listed. */
    public boolean remover(FavoritoAtivo favorito) {
        int posicao = itens.indexOf(favorito);
        if (posicao < 0) return false;
        itens.remove(posicao);
        notifyItemRemoved(posicao);
        return true;
    }

    public int quantidade() {
        return itens.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorito, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FavoritoAtivo favorito = itens.get(position);
        String simbolo = favorito.getSimbolo() == null ? "—" : favorito.getSimbolo();
        holder.tvSigla.setText(sigla(simbolo, favorito.getCategoria()));
        holder.tvSimbolo.setText(simbolo);
        holder.tvNome.setText(favorito.getNome());
        holder.tvCategoria.setText(FavoritoUi.rotuloCategoria(favorito.getCategoria()).toUpperCase(Locale.ROOT));

        FavoritoUi.aplicar(holder.btnRemover, true);
        holder.btnRemover.setOnClickListener(v -> {
            int atual = holder.getBindingAdapterPosition();
            if (atual != RecyclerView.NO_POSITION) listener.aoRemover(itens.get(atual));
        });
        holder.itemView.setOnClickListener(v -> {
            int atual = holder.getBindingAdapterPosition();
            if (atual != RecyclerView.NO_POSITION) listener.aoAbrir(itens.get(atual));
        });
        AnimacaoUtils.entradaItemLista(holder.itemView, position);
    }

    private String sigla(String simbolo, String categoria) {
        String sigla = SiglaIcone.sigla(simbolo);
        return sigla == null ? "•" : sigla;
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvSigla;
        final TextView tvSimbolo;
        final TextView tvNome;
        final TextView tvCategoria;
        final ImageView btnRemover;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSigla = itemView.findViewById(R.id.tvSiglaFavorito);
            tvSimbolo = itemView.findViewById(R.id.tvSimboloFavorito);
            tvNome = itemView.findViewById(R.id.tvNomeFavorito);
            tvCategoria = itemView.findViewById(R.id.tvCategoriaFavorito);
            btnRemover = itemView.findViewById(R.id.btnRemoverFavorito);
        }
    }
}
