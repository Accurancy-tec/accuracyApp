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

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class QuoteAdapter extends RecyclerView.Adapter<QuoteAdapter.ViewHolder>{
    private List<QuoteData> quoteData;
    NumberFormat formato = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public QuoteAdapter(List<QuoteData> quoteData){
        this.quoteData = quoteData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_investiment,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        QuoteData quoteData = this.quoteData.get(position);

        holder.txtIconAtivo.setText(quoteData.getSymbol());
        holder.txtNomeAtivo.setText(quoteData.getShortName());
        holder.txtSubtituloAtivo.setText(
                String.valueOf(quoteData.getRegularMarketVolume())
        );
        holder.txtVariacaoAtivo.setText(quoteData.getRegularMarketChangePercent() + "%");

        if(quoteData.getRegularMarketChangePercent() > 0){
            holder.txtVariacaoAtivo.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(),R.color.green_positive));
        }
        else{
            holder.txtVariacaoAtivo.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.red_negative)
            );
        }
        holder.txtValorAtivo.setText(formato.format(quoteData.getRegularMarketPrice()));
    }

    @Override
    public int getItemCount(){
        return quoteData == null ? 0 : quoteData.size();
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
