
package com.example.accurancymobileapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.model.clsAportes;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class EmphasisAdapter
        extends RecyclerView.Adapter<EmphasisAdapter.ViewHolder> {

    private final List<clsAportes> aportes;

    private final NumberFormat formato =
            NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public EmphasisAdapter(List<clsAportes> aportes) {
        this.aportes = aportes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ativo_destaque, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        clsAportes aporte = aportes.get(position);

        String ativo = aporte.getAtivo();
        String nome = aporte.getName();
        String categoria = aporte.getCategoria();
        String tipo = aporte.getTipo();
        String recorrencia = aporte.getRecorrencia();

        // Ícone com a primeira letra do ativo.
        holder.txtIconAtivo.setText(
                ativo != null && !ativo.trim().isEmpty()
                        ? ativo.substring(0, 1).toUpperCase(Locale.ROOT)
                        : "?"
        );

        // Nome e subtítulo.
        holder.txtNomeAtivo.setText(
                nome != null && !nome.trim().isEmpty()
                        ? nome
                        : ativo != null ? ativo : "Ativo"
        );

        holder.txtSubtituloAtivo.setText(
                (ativo != null ? ativo : "")
                        + (categoria != null && !categoria.isEmpty()
                        ? " • " + categoria
                        : "")
        );

        // Valor principal.
        holder.txtValorAtivo.setText(
                formato.format(aporte.getPreco())
        );

        String tipoNormalizado = tipo == null
                ? ""
                : tipo.trim();

        String recorrenciaNormalizada = recorrencia == null
                ? ""
                : recorrencia.trim();

        boolean compraUnica =
                tipoNormalizado.equalsIgnoreCase("Compra")
                        && (
                        recorrenciaNormalizada.equalsIgnoreCase("Único")
                                || recorrenciaNormalizada.equalsIgnoreCase("Única")
                                || recorrenciaNormalizada.equalsIgnoreCase("Unico")
                                || recorrenciaNormalizada.equalsIgnoreCase("Unica")
                );

        boolean venda = tipoNormalizado.equalsIgnoreCase("Venda");

        holder.txtVariacaoAtivo.setVisibility(View.VISIBLE);

// Compra única: mostra o texto em vez da porcentagem.
        if (compraUnica) {
            holder.txtVariacaoAtivo.setText("Compra única");
            holder.txtVariacaoAtivo.setTextColor(
                    ContextCompat.getColor(
                            holder.itemView.getContext(),
                            R.color.text_secondary
                    )
            );
            return;
        }

// Venda: mostra o tipo da operação em vez da porcentagem.
        if (venda) {
            holder.txtVariacaoAtivo.setText("Venda");
            holder.txtVariacaoAtivo.setTextColor(
                    ContextCompat.getColor(
                            holder.itemView.getContext(),
                            R.color.text_secondary
                    )
            );
            return;
        }

// Para os investimentos com lucro ou perda, mostra o percentual.
        Double rendimento = aporte.getRendimentoPercentual();

        if (rendimento == null
                || rendimento.isNaN()
                || rendimento.isInfinite()) {
            rendimento = 0.0;
        }

        String sinal = rendimento > 0 ? "+" : "";

        holder.txtVariacaoAtivo.setText(
                sinal + String.format(
                        Locale.forLanguageTag("pt-BR"),
                        "%.2f%%",
                        rendimento
                )
        );

        int cor = rendimento > 0
                ? R.color.green_positive
                : rendimento < 0
                ? R.color.red_negative
                : R.color.text_secondary;

        holder.txtVariacaoAtivo.setTextColor(
                ContextCompat.getColor(
                        holder.itemView.getContext(),
                        cor
                )
        );

        android.util.Log.d(
                "EmphasisAdapter",
                "Ativo=" + aporte.getAtivo()
                        + " | Tipo=" + aporte.getTipo()
                        + " | Recorrencia=" + aporte.getRecorrencia()
                        + " | Rendimento=" + aporte.getRendimentoPercentual()
        );
    }

    @Override
    public int getItemCount() {
        return aportes == null ? 0 : aportes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtIconAtivo;
        TextView txtNomeAtivo;
        TextView txtSubtituloAtivo;
        TextView txtValorAtivo;
        TextView txtVariacaoAtivo;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtIconAtivo = itemView.findViewById(R.id.txtIconAtivo);
            txtNomeAtivo = itemView.findViewById(R.id.txtNomeAtivo);
            txtSubtituloAtivo = itemView.findViewById(R.id.txtSubtituloAtivo);
            txtValorAtivo = itemView.findViewById(R.id.txtValorAtivo);
            txtVariacaoAtivo = itemView.findViewById(R.id.txtVariacaoAtivo);
        }
    }
}