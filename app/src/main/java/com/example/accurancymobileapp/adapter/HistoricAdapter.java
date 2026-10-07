package com.example.accurancymobileapp.adapter;
import android.icu.util.LocaleData;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.model.Aporte;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
public class HistoricAdapter extends RecyclerView.Adapter<HistoricAdapter.AporteViewHolder> {

    private List<Aporte> historicList;

    public HistoricAdapter(List<Aporte> historicList) {
        this.historicList = historicList;
    }

    public void atualizarLista(List<Aporte> novalista){
        this.historicList = novalista;
        notifyDataSetChanged();
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

        Aporte aporte = historicList.get(position);

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
        holder.txtVariacaoAtivo.setText(
                aporte.getTipoAporte()
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
        double quantidade = aporte.getQuantidadeAporte();
        String dataAporte = aporte.getData_aporte();
        String categoriaAtivo = aporte.getCategoriaAtivo();

        String informacao = "";

        if(quantidade != 0){
            informacao = String.valueOf((int) quantidade) + " " + categoriaAtivo;
        }

        if (dataAporte != null && !dataAporte.isEmpty()) {

            LocalDate data = LocalDate.parse(dataAporte);

            DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyyy");

            dataAporte = data.format(formato);

            if (!informacao.isEmpty()) {
                informacao += " • ";
            }

            informacao += dataAporte;
        }

        holder.txtSubtituloAtivo.setText(informacao);
    }

    @Override
    public int getItemCount() {
        return historicList.size();
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
