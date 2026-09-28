package com.example.accurancymobileapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.model.Aporte;

import java.util.List;
import java.util.Locale;

public class AporteAdapter extends RecyclerView.Adapter<AporteAdapter.AporteViewHolder> {

    private final List<Aporte> listaAportes;

    public AporteAdapter(List<Aporte> listaAportes) {
        this.listaAportes = listaAportes;
    }

    @NonNull
    @Override
    public AporteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_investiment, parent, false);

        return new AporteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AporteViewHolder holder, int position) {

        Aporte aporte = listaAportes.get(position);

        // Nome do ativo
        holder.txtNomeAtivo.setText(aporte.getNameAtivo());

        // Código do ativo
        String ativo = aporte.getAtivoAporte();

        if (ativo != null && !ativo.isEmpty()) {
            holder.txtIconAtivo.setText(
                    ativo.substring(0, 1).toUpperCase(Locale.ROOT)
            );
        } else {
            holder.txtIconAtivo.setText("?");
        }

        // Categoria do ativo
        holder.txtSubtituloAtivo.setText(
                aporte.getCategoriaAtivo()
        );

        // Valor do aporte
        holder.txtValorAtivo.setText(
                String.format(
                        Locale.getDefault(),
                        "R$ %.2f",
                        aporte.getValorAporte()
                )
        );

        // Tipo e recorrência
        String tipo = aporte.getTipoAporte();
        String recorrencia = aporte.getRecorrenciaAporte();

        String informacao = "";

        if (tipo != null && !tipo.isEmpty()) {
            informacao = tipo;
        }

        if (recorrencia != null && !recorrencia.isEmpty()) {

            if (!informacao.isEmpty()) {
                informacao += " • ";
            }

            informacao += recorrencia;
        }

        holder.txtVariacaoAtivo.setText(informacao);
    }

    @Override
    public int getItemCount() {
        return listaAportes.size();
    }

    public static class AporteViewHolder extends RecyclerView.ViewHolder {

        TextView txtIconAtivo;
        TextView txtNomeAtivo;
        TextView txtSubtituloAtivo;
        TextView txtValorAtivo;
        TextView txtVariacaoAtivo;

        public AporteViewHolder(@NonNull View itemView) {
            super(itemView);

            txtIconAtivo = itemView.findViewById(R.id.txtIconAtivo);
            txtNomeAtivo = itemView.findViewById(R.id.txtNomeAtivo);
            txtSubtituloAtivo = itemView.findViewById(R.id.txtSubtituloAtivo);
            txtValorAtivo = itemView.findViewById(R.id.txtValorAtivo);
            txtVariacaoAtivo = itemView.findViewById(R.id.txtVariacaoAtivo);
        }
    }
}