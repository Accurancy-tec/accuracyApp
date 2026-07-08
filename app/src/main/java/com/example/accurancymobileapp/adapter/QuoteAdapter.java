package com.example.accurancymobileapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.model.Quote;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class QuoteAdapter extends RecyclerView.Adapter<QuoteAdapter.ViewHolder>{
    private List<Quote> quotes;
    NumberFormat formato = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public QuoteAdapter(List<Quote> quotes){
        this.quotes = quotes;
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

        Quote quote = quotes.get(position);

        holder.txtIconAtivo.setText(quote.getSymbol());
        holder.txtNomeAtivo.setText(quote.getShortName());
        holder.txtSubtituloAtivo.setText(
                String.valueOf(quote.getRegularMarketVolume())
        );
        holder.txtVariacaoAtivo.setText(quote.getRegularMarketChangePercent() + "%");

        if(quote.getRegularMarketChangePercent() > 0){
            holder.txtVariacaoAtivo.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(),R.color.green_positive));
        }
        else{
            holder.txtVariacaoAtivo.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.red_negative)
            );
        }
        holder.txtValorAtivo.setText(formato.format(quote.getRegularMarketPrice()));
    }

    @Override
    public int getItemCount(){
        return quotes == null ? 0 : quotes.size();
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
