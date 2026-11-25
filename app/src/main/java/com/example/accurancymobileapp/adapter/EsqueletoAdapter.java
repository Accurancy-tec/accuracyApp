package com.example.accurancymobileapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accurancymobileapp.R;

/**
 * Fixed number of shimmering rows shown in a RecyclerView while the real list loads.
 * Use {@link #mostrar} before the request and {@link #ocultar} when it ends (success or failure).
 */
public class EsqueletoAdapter extends RecyclerView.Adapter<EsqueletoAdapter.Linha> {

    private final int layoutLinha;
    private final int quantidade;

    public EsqueletoAdapter(@LayoutRes int layoutLinha, int quantidade) {
        this.layoutLinha = layoutLinha;
        this.quantidade = quantidade;
    }

    /** Swaps the list for placeholder rows. Calling it again while they show does nothing. */
    public static void mostrar(@NonNull RecyclerView lista, @LayoutRes int layoutLinha, int quantidade) {
        RecyclerView.Adapter<?> atual = lista.getAdapter();
        if (atual instanceof EsqueletoAdapter) return;
        lista.setLayoutAnimation(null);
        lista.setAdapter(new EsqueletoAdapter(layoutLinha, quantidade));
    }

    /**
     * Puts the real adapter back. When the placeholder was showing, the real rows slide in.
     * Passing the adapter that is already set only (re)plays nothing.
     */
    public static void ocultar(@NonNull RecyclerView lista, @NonNull RecyclerView.Adapter<?> real) {
        boolean eraEsqueleto = lista.getAdapter() instanceof EsqueletoAdapter;
        if (lista.getAdapter() != real) lista.setAdapter(real);
        if (eraEsqueleto) {
            lista.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(
                    lista.getContext(), R.anim.layout_entrada_lista));
            lista.scheduleLayoutAnimation();
        }
    }

    public static boolean mostrando(@NonNull RecyclerView lista) {
        return lista.getAdapter() instanceof EsqueletoAdapter;
    }

    @NonNull
    @Override
    public Linha onCreateViewHolder(@NonNull ViewGroup pai, int tipo) {
        return new Linha(LayoutInflater.from(pai.getContext()).inflate(layoutLinha, pai, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Linha holder, int posicao) {
        // Nothing to bind: the rows are only shapes.
    }

    @Override
    public int getItemCount() {
        return quantidade;
    }

    static class Linha extends RecyclerView.ViewHolder {
        Linha(@NonNull View itemView) {
            super(itemView);
        }
    }
}
