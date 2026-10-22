package com.example.accurancymobileapp.adapter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.os.Looper;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.caverock.androidsvg.SVG;
import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.model.Acao;
import com.example.accurancymobileapp.model.QuoteData;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Handler;

public class AcoesAdapter extends RecyclerView.Adapter<AcoesAdapter.ViewHolder>{

    private List<Acao> acao;
    NumberFormat formato = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public AcoesAdapter(List<Acao> acao){
        this.acao = acao;
    }


    public void atualizarLista(List<Acao> novasAcoes) {
        this.acao.clear();
        this.acao.addAll(novasAcoes);
        notifyDataSetChanged();
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

        Acao acao = this.acao.get(position);

        holder.txtNomeAtivo.setText(acao.getStock());
        holder.txtSubtituloAtivo.setText(acao.getName());
        holder.txtVariacaoAtivo.setText(acao.getChange() + "%");

        if(acao.getChange() > 0){
            holder.txtVariacaoAtivo.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(),R.color.green_positive));
        }
        else{
            holder.txtVariacaoAtivo.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.red_negative)
            );
        }
        holder.txtValorAtivo.setText(formato.format(acao.getClose()));

        SvgLogoLoader.carregar(acao.getLogo(), holder.txtIconAtivo);
    }

    @Override
    public int getItemCount(){
        return acao == null ? 0 : acao.size();
    }
    class ViewHolder extends RecyclerView.ViewHolder{

        ImageView txtIconAtivo;
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

