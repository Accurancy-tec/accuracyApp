package com.example.accurancymobileapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.model.QuoteData;
import com.example.accurancymobileapp.model.clsAportes;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class EmphasisAdapter  extends RecyclerView.Adapter<EmphasisAdapter.ViewHolder>{

    private List<clsAportes> aportes;
    NumberFormat formato = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public EmphasisAdapter(List<clsAportes> aportes){
        this.aportes = aportes;
    }

    @NonNull
    @Override
    public EmphasisAdapter.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_investiment,parent,false);

        return new EmphasisAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull EmphasisAdapter.ViewHolder holder,
            int position) {

        clsAportes aportes = this.aportes.get(position);

       holder.txtIconAtivo.setText(aportes.getAtivo());
        holder.txtNomeAtivo.setText(aportes.getAtivo());
        holder.txtSubtituloAtivo.setText(
                String.valueOf(aportes.getAtivo())
        );

        holder.txtVariacaoAtivo.setText("15%");

        //Tem que atualizar o banco de dados para poder puxar esses requisitos
      /*  holder.txtVariacaoAtivo.setText(quoteData.getRegularMarketChangePercent() + "%");

        if(quoteData.getRegularMarketChangePercent() > 0){
            holder.txtVariacaoAtivo.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(),R.color.green_positive));
        }
        else{
            holder.txtVariacaoAtivo.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.red_negative)
            );
        }
        holder.txtValorAtivo.setText(formato.format(quoteData.getRegularMarketPrice()));*/

        holder.txtValorAtivo.setText("R$ 99");
    }

    @Override
    public int getItemCount(){
        return aportes == null ? 0 : aportes.size();
    }
    class ViewHolder extends RecyclerView.ViewHolder{

        TextView txtIconAtivo;
        TextView txtNomeAtivo;
        TextView txtSubtituloAtivo;
        TextView txtValorAtivo;
        TextView txtVariacaoAtivo;

        public ViewHolder(@NonNull View itemView){
            super(itemView);

            txtIconAtivo = itemView.findViewById(R.id.txtIconAtivo);
            txtNomeAtivo = itemView.findViewById(R.id.txtNomeAtivo);
            txtSubtituloAtivo = itemView.findViewById(R.id.txtSubtituloAtivo);
            txtValorAtivo = itemView.findViewById(R.id.txtValorAtivo);
            txtVariacaoAtivo = itemView.findViewById(R.id.txtVariacaoAtivo);
        }

    }
}
