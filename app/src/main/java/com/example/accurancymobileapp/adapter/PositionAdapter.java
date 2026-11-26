package com.example.accurancymobileapp.adapter;

import android.content.Context;
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
import com.example.accurancymobileapp.model.Position;
import com.example.accurancymobileapp.utils.AssetLogo;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/** Assets the wallet holds now: quantity, current value and result since the purchase. */
public class PositionAdapter extends RecyclerView.Adapter<PositionAdapter.PositionViewHolder> {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private final List<Position> positions;
    private final int walletId;
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(PT_BR);

    public PositionAdapter(@NonNull List<Position> positions, int walletId) {
        this.positions = positions;
        this.walletId = walletId;
    }

    @NonNull
    @Override
    public PositionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_investiment, parent, false);
        return new PositionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PositionViewHolder holder, int index) {
        Position position = positions.get(index);
        Context context = holder.itemView.getContext();

        AssetLogo.show(holder.icon, position.symbol);
        holder.name.setText(position.symbol);
        holder.subtitle.setText(subtitle(position));
        holder.value.setText(currency.format(position.currentValue));

        boolean gain = position.result >= 0;
        holder.change.setText((gain ? "▲ " : "▼ ") + percent(position.returnPercent));
        holder.change.setTextColor(context.getColor(position.result == 0 ? R.color.text_secondary
                : gain ? R.color.green_positive : R.color.red_negative));

        holder.itemView.setOnClickListener(v -> {
            if (position.isFixedIncome()) return;
            Intent intent = new Intent(context, DetalhesAtivoActivity.class)
                    .putExtra(DetalhesAtivoActivity.EXTRA_TICKER, position.symbol)
                    .putExtra(DetalhesAtivoActivity.EXTRA_NOME, position.name != null ? position.name : position.symbol)
                    .putExtra(DetalhesAtivoActivity.EXTRA_CATEGORIA, position.category != null ? position.category : "Ações")
                    .putExtra(DetalhesAtivoActivity.EXTRA_ID_CARTEIRA, walletId);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return positions.size();
    }

    private String subtitle(Position position) {
        if (position.isFixedIncome()) {
            return position.name + " · aplicado " + currency.format(position.quantity * position.averagePrice);
        }
        return quantity(position.quantity) + " × " + currency.format(position.currentPrice)
                + " · preço médio " + currency.format(position.averagePrice);
    }

    private static String quantity(double value) {
        String text = String.format(PT_BR, "%.8f", value).replaceAll("0+$", "").replaceAll(",$", "");
        return text.isEmpty() ? "0" : text;
    }

    private static String percent(double value) {
        return String.format(PT_BR, "%.2f%%", Math.abs(value));
    }

    static class PositionViewHolder extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView name, subtitle, value, change;

        PositionViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.txtIconAtivo);
            name = itemView.findViewById(R.id.txtNomeAtivo);
            subtitle = itemView.findViewById(R.id.txtSubtituloAtivo);
            value = itemView.findViewById(R.id.txtValorAtivo);
            change = itemView.findViewById(R.id.txtVariacaoAtivo);
        }
    }
}
