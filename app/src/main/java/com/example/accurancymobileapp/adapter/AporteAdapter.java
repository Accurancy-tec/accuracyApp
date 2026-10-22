package com.example.accurancymobileapp.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.activities.DetalhesAtivoActivity;
import com.example.accurancymobileapp.model.Aporte;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AporteAdapter extends RecyclerView.Adapter<AporteAdapter.AporteViewHolder> {

    private final List<Aporte> listaAportes = new ArrayList<>();
    private final NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private final Integer idCarteira;

    public AporteAdapter(List<Aporte> listaAportes) {
        this(listaAportes, null);
    }

    public AporteAdapter(List<Aporte> listaAportes, Integer idCarteira) {
        this.idCarteira = idCarteira;
        if (listaAportes != null) this.listaAportes.addAll(listaAportes);
    }

    public void atualizarLista(List<Aporte> novaLista) {
        listaAportes.clear();
        if (novaLista != null) listaAportes.addAll(novaLista);
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
        Aporte aporte = listaAportes.get(position);

        holder.txtNomeAtivo.setText(textoOuPadrao(aporte.getNameAtivo(), textoOuPadrao(aporte.getAtivoAporte(), "Ativo")));
        holder.txtIconAtivo.setImageResource(R.drawable.ic_ativo_placeholder);
        holder.txtSubtituloAtivo.setText(montarSubtitulo(aporte));
        holder.txtValorAtivo.setText(formatoMoeda.format(aporte.getValorAporte()));

        String tipo = textoOuPadrao(aporte.getTipoAporte(), "Movimentação");
        holder.txtVariacaoAtivo.setText(tipo);
        int cor;
        if ("Venda".equalsIgnoreCase(tipo)) {
            cor = R.color.red_negative;
        } else if ("Dividendo".equalsIgnoreCase(tipo)) {
            cor = R.color.blue_primary;
        } else {
            cor = R.color.green_positive;
        }
        holder.txtVariacaoAtivo.setTextColor(holder.itemView.getContext().getColor(cor));

        holder.itemView.setOnClickListener(v -> {
            String ticker = aporte.getAtivoAporte();
            if (ticker == null || ticker.trim().isEmpty()) return;
            Intent intent = new Intent(holder.itemView.getContext(), DetalhesAtivoActivity.class);
            intent.putExtra(DetalhesAtivoActivity.EXTRA_TICKER, ticker);
            intent.putExtra(DetalhesAtivoActivity.EXTRA_NOME, textoOuPadrao(aporte.getNameAtivo(), ticker));
            intent.putExtra(DetalhesAtivoActivity.EXTRA_CATEGORIA, textoOuPadrao(aporte.getCategoriaAtivo(), "Ações"));
            if (idCarteira != null && idCarteira > 0) intent.putExtra(DetalhesAtivoActivity.EXTRA_ID_CARTEIRA, idCarteira.intValue());
            holder.itemView.getContext().startActivity(intent);
        });
    }

    private String montarSubtitulo(Aporte aporte) {
        List<String> partes = new ArrayList<>();
        double quantidade = aporte.getQuantidadeAporte();
        if (quantidade > 0) {
            String quantidadeTexto = quantidade == Math.rint(quantidade)
                    ? String.valueOf((long) quantidade)
                    : String.format(Locale.forLanguageTag("pt-BR"), "%.4f", quantidade).replaceAll("0+$", "").replaceAll(",$", "");
            partes.add(quantidadeTexto + " " + textoOuPadrao(aporte.getCategoriaAtivo(), "un."));
        }
        String dataTexto = formatarData(aporte.getData_aporte());
        if (dataTexto != null) partes.add(dataTexto);
        if (partes.isEmpty()) return textoOuPadrao(aporte.getAtivoAporte(), "Ativo");
        return android.text.TextUtils.join(" • ", partes);
    }

    private String formatarData(String valor) {
        if (valor == null || valor.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(valor).format(DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("pt", "BR")));
        } catch (Exception ignored) {
            return valor;
        }
    }

    private String textoOuPadrao(String valor, String padrao) {
        return valor == null || valor.trim().isEmpty() ? padrao : valor;
    }

    @Override
    public int getItemCount() {
        return listaAportes.size();
    }

    static class AporteViewHolder extends RecyclerView.ViewHolder {
        final ImageView txtIconAtivo;
        final TextView txtNomeAtivo;
        final TextView txtSubtituloAtivo;
        final TextView txtValorAtivo;
        final TextView txtVariacaoAtivo;

        AporteViewHolder(@NonNull View itemView) {
            super(itemView);
            // item_investiment.xml define txtIconAtivo como ImageView, não TextView.
            txtIconAtivo = itemView.findViewById(R.id.txtIconAtivo);
            txtNomeAtivo = itemView.findViewById(R.id.txtNomeAtivo);
            txtSubtituloAtivo = itemView.findViewById(R.id.txtSubtituloAtivo);
            txtValorAtivo = itemView.findViewById(R.id.txtValorAtivo);
            txtVariacaoAtivo = itemView.findViewById(R.id.txtVariacaoAtivo);
        }
    }
}
