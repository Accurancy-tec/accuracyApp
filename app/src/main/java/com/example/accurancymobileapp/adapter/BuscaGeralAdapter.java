package com.example.accurancymobileapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.model.Acao;
import com.example.accurancymobileapp.model.TituloRendaFixa;
import com.example.accurancymobileapp.network.repository.BuscaGeralRepository.Secao;
import com.example.accurancymobileapp.utils.SiglaIcone;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Results of the general search: a header per category, a row per product, shimmer rows while loading. */
public class BuscaGeralAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface Ouvinte {
        void aoAbrirAcao(Secao secao, Acao acao);

        void aoAbrirTitulo(Secao secao, TituloRendaFixa titulo);

        void aoVerTodos(Secao secao);
    }

    private static final int TIPO_CABECALHO = 0;
    private static final int TIPO_LINHA = 1;
    private static final int TIPO_ESQUELETO = 2;

    private static final class Item {
        final int tipo;
        final Secao secao;
        final Acao acao;
        final TituloRendaFixa titulo;

        Item(int tipo, Secao secao, Acao acao, TituloRendaFixa titulo) {
            this.tipo = tipo;
            this.secao = secao;
            this.acao = acao;
            this.titulo = titulo;
        }
    }

    private final List<Item> itens = new ArrayList<>();
    private final Ouvinte ouvinte;
    private final NumberFormat moeda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));

    public BuscaGeralAdapter(Ouvinte ouvinte) {
        this.ouvinte = ouvinte;
    }

    public void atualizar(List<Secao> secoes, boolean carregando) {
        itens.clear();
        for (Secao secao : secoes) {
            itens.add(new Item(TIPO_CABECALHO, secao, null, null));
            if (secao.ehRendaFixa()) {
                for (TituloRendaFixa t : secao.titulos) itens.add(new Item(TIPO_LINHA, secao, null, t));
            } else {
                for (Acao a : secao.acoes) itens.add(new Item(TIPO_LINHA, secao, a, null));
            }
        }
        if (carregando) {
            // Nothing yet: a few rows; something already shown: one row saying "more is coming".
            int linhas = secoes.isEmpty() ? 4 : 1;
            for (int i = 0; i < linhas; i++) itens.add(new Item(TIPO_ESQUELETO, null, null, null));
        }
        notifyDataSetChanged();
    }

    public boolean vazio() {
        return itens.isEmpty();
    }

    @Override
    public int getItemViewType(int posicao) {
        return itens.get(posicao).tipo;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup pai, int tipo) {
        LayoutInflater inflater = LayoutInflater.from(pai.getContext());
        if (tipo == TIPO_CABECALHO) {
            return new Cabecalho(inflater.inflate(R.layout.item_busca_cabecalho, pai, false));
        }
        if (tipo == TIPO_ESQUELETO) {
            return new Esqueleto(inflater.inflate(R.layout.item_esqueleto_linha, pai, false));
        }
        return new Linha(inflater.inflate(R.layout.item_investiment, pai, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int posicao) {
        Item item = itens.get(posicao);
        if (holder instanceof Cabecalho) {
            Cabecalho c = (Cabecalho) holder;
            c.titulo.setText(item.secao.titulo.toUpperCase(Locale.forLanguageTag("pt-BR")));
            c.verTodos.setOnClickListener(v -> ouvinte.aoVerTodos(item.secao));
        } else if (holder instanceof Linha) {
            Linha l = (Linha) holder;
            l.btnFavorito.setVisibility(View.GONE);
            if (item.titulo != null) vincularTitulo(l, item);
            else vincularAcao(l, item);
        }
    }

    private void vincularAcao(Linha l, Item item) {
        Acao acao = item.acao;
        String simbolo = vazioPara(acao.getStock(), "—");
        l.nome.setText(simbolo);
        l.subtitulo.setText(vazioPara(acao.getName(), "Nome não informado"));
        SiglaIcone.aplicar(l.icone, acao.getStock(), R.drawable.ic_ativo_placeholder);
        SvgLogoLoader.carregar(acao.getLogo(), l.icone);

        String rotulo = acao.getPriceLabel();
        Double fechamento = acao.getClose();
        if (rotulo != null && !rotulo.trim().isEmpty()) {
            l.valor.setText(rotulo);
        } else {
            l.valor.setText(fechamento == null || !Double.isFinite(fechamento) || fechamento <= 0
                    ? "—" : moeda.format(fechamento));
        }

        Double variacao = acao.getChange();
        if (variacao == null || !Double.isFinite(variacao)) {
            l.variacao.setText("");
        } else {
            String seta = variacao > 0 ? "▲" : variacao < 0 ? "▼" : "•";
            l.variacao.setText(String.format(Locale.forLanguageTag("pt-BR"), "%s %.2f%%", seta, Math.abs(variacao)));
            int cor = variacao < 0 ? R.color.red_negative : variacao > 0 ? R.color.green_positive : R.color.text_secondary;
            l.variacao.setTextColor(ContextCompat.getColor(l.itemView.getContext(), cor));
        }
        l.itemView.setOnClickListener(v -> ouvinte.aoAbrirAcao(item.secao, acao));
    }

    private void vincularTitulo(Linha l, Item item) {
        TituloRendaFixa t = item.titulo;
        l.nome.setText(vazioPara(t.getNomeTitulo(), "Título"));
        String tipo = vazioPara(t.getTipoTitulo(), "Renda fixa");
        String indexador = vazioPara(t.getIndexador(), "");
        l.subtitulo.setText(indexador.isEmpty() ? tipo : tipo + " · " + indexador);
        SiglaIcone.aplicarSeSimbolo(l.icone, item.secao.tesouro ? "TD" : "RF", R.drawable.ic_ativo_placeholder);
        SvgLogoLoader.carregar(null, l.icone); // forget a logo that a recycled row may still be loading

        BigDecimal rentabilidade = t.getRentabilidade();
        if (rentabilidade != null) {
            String valor = rentabilidade.stripTrailingZeros().toPlainString().replace('.', ',');
            l.valor.setText(valor + "%");
        } else {
            l.valor.setText("—");
        }
        l.variacao.setText(t.getVencimentoFormatado());
        l.variacao.setTextColor(ContextCompat.getColor(l.itemView.getContext(), R.color.text_secondary));
        l.itemView.setOnClickListener(v -> ouvinte.aoAbrirTitulo(item.secao, t));
    }

    private static String vazioPara(String valor, String padrao) {
        return valor == null || valor.trim().isEmpty() ? padrao : valor;
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class Cabecalho extends RecyclerView.ViewHolder {
        final TextView titulo;
        final TextView verTodos;

        Cabecalho(@NonNull View v) {
            super(v);
            titulo = v.findViewById(R.id.tvBuscaSecao);
            verTodos = v.findViewById(R.id.btnBuscaVerTodos);
        }
    }

    static class Linha extends RecyclerView.ViewHolder {
        final ImageView icone;
        final TextView nome, subtitulo, valor, variacao;
        final View btnFavorito;

        Linha(@NonNull View v) {
            super(v);
            icone = v.findViewById(R.id.txtIconAtivo);
            nome = v.findViewById(R.id.txtNomeAtivo);
            subtitulo = v.findViewById(R.id.txtSubtituloAtivo);
            valor = v.findViewById(R.id.txtValorAtivo);
            variacao = v.findViewById(R.id.txtVariacaoAtivo);
            btnFavorito = v.findViewById(R.id.btnFavoritoItem);
        }
    }

    static class Esqueleto extends RecyclerView.ViewHolder {
        Esqueleto(@NonNull View v) {
            super(v);
        }
    }
}
