package com.example.accurancymobileapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.model.Aporte;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoricAdapter extends RecyclerView.Adapter<HistoricAdapter.AporteViewHolder> {

    private final Context context;
    private final List<Aporte> historicList = new ArrayList<>();
    private final NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private final DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("pt", "BR"));

    public HistoricAdapter(Context context, List<Aporte> historicList) {
        this.context = context;
        if (historicList != null) this.historicList.addAll(historicList);
    }

    public void atualizarLista(List<Aporte> novaLista) {
        historicList.clear();
        if (novaLista != null) historicList.addAll(novaLista);
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

        holder.txtNomeAtivo.setText(textoOuPadrao(aporte.getNameAtivo(), textoOuPadrao(aporte.getAtivoAporte(), "Ativo")));
        holder.txtIconAtivo.setImageResource(R.drawable.ic_ativo_placeholder);
        holder.txtVariacaoAtivo.setText(textoOuPadrao(aporte.getTipoAporte(), "Movimentação"));
        holder.txtValorAtivo.setText(formatoMoeda.format(aporte.getValorAporte()));
        holder.txtSubtituloAtivo.setText(montarSubtitulo(aporte));

        String tipo = aporte.getTipoAporte();
        int cor = R.color.green_positive;
        if ("Venda".equalsIgnoreCase(tipo)) {
            cor = R.color.red_negative;
        } else if ("Dividendo".equalsIgnoreCase(tipo)) {
            cor = R.color.blue_primary;
        }
        holder.txtVariacaoAtivo.setTextColor(ContextCompat.getColor(context, cor));
        holder.itemView.setOnClickListener(null);
    }

    private String montarSubtitulo(Aporte aporte) {
        List<String> partes = new ArrayList<>();
        double quantidade = aporte.getQuantidadeAporte();
        if (quantidade > 0) {
            String qtd = quantidade == Math.rint(quantidade)
                    ? String.valueOf((long) quantidade)
                    : String.format(Locale.forLanguageTag("pt-BR"), "%.4f", quantidade).replaceAll("0+$", "").replaceAll(",$", "");
            partes.add(qtd + " " + textoOuPadrao(aporte.getCategoriaAtivo(), "un."));
        }
        String data = formatarData(aporte.getData_aporte());
        if (data != null) partes.add(data);
        return partes.isEmpty() ? textoOuPadrao(aporte.getAtivoAporte(), "Ativo") : android.text.TextUtils.join(" • ", partes);
    }

    private String formatarData(String valor) {
        if (valor == null || valor.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(valor).format(formatoData);
        } catch (Exception ignored) {
            // Não derruba a tela se a API retornar uma data fora do formato ISO.
            return valor;
        }
    }

    private String textoOuPadrao(String valor, String padrao) {
        return valor == null || valor.trim().isEmpty() ? padrao : valor;
    }

    @Override
    public int getItemCount() {
        return historicList.size();
    }

    public static class AporteViewHolder extends RecyclerView.ViewHolder {
        final ImageView txtIconAtivo;
        final TextView txtNomeAtivo;
        final TextView txtSubtituloAtivo;
        final TextView txtValorAtivo;
        final TextView txtVariacaoAtivo;

        public AporteViewHolder(@NonNull View itemView) {
            super(itemView);
            // item_investiment.xml define txtIconAtivo como ImageView.
            txtIconAtivo = itemView.findViewById(R.id.txtIconAtivo);
            txtNomeAtivo = itemView.findViewById(R.id.txtNomeAtivo);
            txtSubtituloAtivo = itemView.findViewById(R.id.txtSubtituloAtivo);
            txtValorAtivo = itemView.findViewById(R.id.txtValorAtivo);
            txtVariacaoAtivo = itemView.findViewById(R.id.txtVariacaoAtivo);
        }
    }
}
