package com.example.accurancymobileapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.model.Noticia;
import com.example.accurancymobileapp.utils.TempoRelativo;

import java.util.ArrayList;
import java.util.List;

/** Cartões de notícia do Início, em lista horizontal. */
public class NoticiaAdapter extends RecyclerView.Adapter<NoticiaAdapter.Cartao> {

    public interface AoAbrir {
        void aoAbrir(@NonNull Noticia noticia);
    }

    private final List<Noticia> noticias = new ArrayList<>();
    private final AoAbrir aoAbrir;

    public NoticiaAdapter(@NonNull AoAbrir aoAbrir) {
        this.aoAbrir = aoAbrir;
    }

    public void definir(@NonNull List<Noticia> novas) {
        noticias.clear();
        noticias.addAll(novas);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Cartao onCreateViewHolder(@NonNull ViewGroup pai, int tipo) {
        return new Cartao(LayoutInflater.from(pai.getContext()).inflate(R.layout.item_noticia, pai, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Cartao cartao, int posicao) {
        Noticia noticia = noticias.get(posicao);
        String quando = TempoRelativo.descrever(noticia.getPublicadaEm());
        String fonte = noticia.getFonte();

        cartao.fonte.setText(quando.isEmpty() ? fonte : fonte + " · " + quando);
        cartao.titulo.setText(noticia.getTitulo());

        String imagem = noticia.getImagem();
        if (imagem != null && !imagem.isEmpty()) {
            cartao.imagem.setVisibility(View.VISIBLE);
            cartao.resumo.setVisibility(View.GONE);
            Glide.with(cartao.imagem)
                    .load(imagem)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .centerCrop()
                    .into(cartao.imagem);
        } else {
            // Sem foto (notícias da equipe): o espaço vai para o resumo.
            Glide.with(cartao.imagem).clear(cartao.imagem);
            cartao.imagem.setVisibility(View.GONE);
            cartao.resumo.setText(noticia.getResumo());
            cartao.resumo.setVisibility(noticia.getResumo().isEmpty() ? View.GONE : View.VISIBLE);
        }

        cartao.itemView.setContentDescription(noticia.getTitulo() + ". " + fonte + (quando.isEmpty() ? "" : ", " + quando));
        cartao.itemView.setOnClickListener(v -> aoAbrir.aoAbrir(noticia));
    }

    @Override
    public int getItemCount() {
        return noticias.size();
    }

    static class Cartao extends RecyclerView.ViewHolder {
        final ImageView imagem;
        final TextView fonte, titulo, resumo;

        Cartao(@NonNull View view) {
            super(view);
            imagem = view.findViewById(R.id.imgNoticia);
            fonte = view.findViewById(R.id.tvFonteNoticia);
            titulo = view.findViewById(R.id.tvTituloNoticia);
            resumo = view.findViewById(R.id.tvResumoNoticia);
        }
    }
}
