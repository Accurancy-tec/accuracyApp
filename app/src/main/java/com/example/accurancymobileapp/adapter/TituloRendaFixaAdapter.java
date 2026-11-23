package com.example.accurancymobileapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.model.TituloRendaFixa;
import com.example.accurancymobileapp.utils.AnimacaoUtils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TituloRendaFixaAdapter extends RecyclerView.Adapter<TituloRendaFixaAdapter.TituloViewHolder>  {
    /** Called when the user taps a title card. */
    public interface OnTituloClickListener {
        void onTituloClick(TituloRendaFixa titulo);
    }

    private final List<TituloRendaFixa> titulos = new ArrayList<>();
    private final OnTituloClickListener clickListener;

    public TituloRendaFixaAdapter() {
        this(null);
    }

    public TituloRendaFixaAdapter(OnTituloClickListener clickListener) {
        this.clickListener = clickListener;
    }

    private final NumberFormat formatoMoeda =
            NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    /** Highest row position that already played its entrance animation. */
    private int ultimaPosicaoAnimada = -1;

    public void atualizarLista(List<TituloRendaFixa> novaLista) {
        titulos.clear();
        ultimaPosicaoAnimada = -1;

        if (novaLista != null) {
            titulos.addAll(novaLista);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TituloViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_titulo_renda_fixa, parent, false);

        return new TituloViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull TituloViewHolder holder,
            int position
    ) {
        TituloRendaFixa titulo = titulos.get(position);

        holder.tvNomeTitulo.setText(titulo.getNomeTitulo());

        String indexador = titulo.getIndexador();

        if (indexador == null || indexador.trim().isEmpty()) {
            indexador = "Não informado";
        }

        holder.tvIndexador.setText(indexador);

        BigDecimal rentabilidade = titulo.getRentabilidade();

        if (rentabilidade != null) {
            String valor = rentabilidade.stripTrailingZeros().toPlainString()
                    .replace('.', ',');

            holder.tvRentabilidade.setText(valor + "% " + indexador);
        } else {
            holder.tvRentabilidade.setText("Não informada");
        }

        holder.tvLiquidez.setText(titulo.getLiquidez());

        BigDecimal aplicacaoMinima = titulo.getAplicacaoMinima();

        if (aplicacaoMinima != null) {
            holder.tvAplicacaoMinima.setText(
                    formatoMoeda.format(aplicacaoMinima)
            );
        } else {
            holder.tvAplicacaoMinima.setText("Não informada");
        }

        if (titulo.possuiGarantiaFgc()) {
            holder.tvGarantiaFgc.setVisibility(View.VISIBLE);
        } else {
            holder.tvGarantiaFgc.setVisibility(View.GONE);
        }

        holder.tvIsentoIR.setVisibility(titulo.isIsentoIR() ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onTituloClick(titulo);
            }
        });
        AnimacaoUtils.efeitoPressionar(holder.itemView);

        if (position > ultimaPosicaoAnimada) {
            ultimaPosicaoAnimada = position;
            AnimacaoUtils.entradaItemLista(holder.itemView, position);
        } else {
            // Recycled row scrolling back into view: make sure it is fully visible.
            holder.itemView.animate().cancel();
            holder.itemView.setAlpha(1f);
            holder.itemView.setTranslationY(0f);
        }
    }

    @Override
    public int getItemCount() {
        return titulos.size();
    }

    static class TituloViewHolder extends RecyclerView.ViewHolder {

        TextView tvNomeTitulo;
        TextView tvIndexador;
        TextView tvGarantiaFgc;
        TextView tvIsentoIR;
        TextView tvRentabilidade;
        TextView tvLiquidez;
        TextView tvAplicacaoMinima;

        TituloViewHolder(@NonNull View itemView) {
            super(itemView);

            tvNomeTitulo = itemView.findViewById(R.id.tvNomeTitulo);
            tvIndexador = itemView.findViewById(R.id.tvIndexador);
            tvGarantiaFgc = itemView.findViewById(R.id.tvGarantiaFgc);
            tvIsentoIR = itemView.findViewById(R.id.tvIsentoIR);
            tvRentabilidade = itemView.findViewById(R.id.tvRentabilidade);
            tvLiquidez = itemView.findViewById(R.id.tvLiquidez);
            tvAplicacaoMinima = itemView.findViewById(R.id.tvAplicacaoMinima);
        }
    }
}
