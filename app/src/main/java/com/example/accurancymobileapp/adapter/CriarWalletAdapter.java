package com.example.accurancymobileapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.model.Wallet;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/** Cartões da aba Carteira: patrimônio, resultado e meta de cada carteira, calculados pela API. */
public class CriarWalletAdapter extends RecyclerView.Adapter<CriarWalletAdapter.WalletViewHolder> {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private List<Wallet> listaWallets;
    private final OnWalletClickListener listener;

    public CriarWalletAdapter(List<Wallet> listaWallets, OnWalletClickListener listener) {
        this.listaWallets = listaWallets;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WalletViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wallet, parent, false);
        return new WalletViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WalletViewHolder holder, int position) {
        Wallet wallet = listaWallets.get(position);
        boolean simulada = wallet.isSimulada();

        // Na simulação o resultado é sobre o dinheiro depositado nela; na real, sobre o investido.
        BigDecimal rentabilidade = simulada ? wallet.getRentabilidadeSimulacao() : wallet.getRentabilidadePercentual();
        boolean temBase = simulada ? wallet.getCapitalSimulado().signum() > 0 : wallet.getTotalInvestido().signum() > 0;
        int sinal = rentabilidade.signum();
        int cor = !temBase || sinal == 0 ? R.color.text_secondary
                : sinal > 0 ? R.color.green_positive : R.color.red_negative;

        holder.tvNomeCarteira.setText(wallet.getNome_carteira());
        holder.tvTipoCarteiraItem.setText(simulada ? "Simulada" : "Real");
        holder.tvSaldoCarteira.setText(moeda(wallet.getPatrimonioAtual()));
        holder.tvRendimentoCarteira.setText(temBase
                ? String.format(PT_BR, "%+.2f%%", rentabilidade.doubleValue())
                : simulada ? "Pronta para simular" : "Sem investimentos");
        holder.tvRendimentoCarteira.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), cor));

        BigDecimal meta = wallet.getMeta_carteira();
        boolean temMeta = meta.signum() > 0;
        holder.tvMetaCarteira.setVisibility(temMeta ? View.VISIBLE : View.GONE);
        holder.tvProgressoCarteira.setVisibility(temMeta ? View.VISIBLE : View.GONE);
        holder.progressoMetaCarteira.setVisibility(temMeta ? View.VISIBLE : View.GONE);
        holder.tvCarteiraSemMeta.setVisibility(temMeta ? View.GONE : View.VISIBLE);
        if (temMeta) {
            int progresso = Math.max(0, Math.min(100, wallet.getProgressoMeta().intValue()));
            holder.tvMetaCarteira.setText(moeda(meta));
            holder.tvProgressoCarteira.setText(wallet.isMetaAtingida() ? "Alcançada" : progresso + "%");
            holder.progressoMetaCarteira.setMax(100);
            holder.progressoMetaCarteira.setProgress(progresso);
        }

        holder.itemView.setContentDescription(wallet.getNome_carteira() + ", carteira " + (simulada ? "simulada" : "real")
                + ", " + holder.tvSaldoCarteira.getText());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onWalletClick(wallet);
        });
    }

    @Override
    public int getItemCount() {
        return listaWallets != null ? listaWallets.size() : 0;
    }

    public void atualizarLista(List<Wallet> novaLista) {
        this.listaWallets = novaLista;
        notifyDataSetChanged();
    }

    private static String moeda(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(PT_BR).format(valor != null ? valor : BigDecimal.ZERO);
    }

    public interface OnWalletClickListener {
        void onWalletClick(Wallet wallet);
    }

    public static class WalletViewHolder extends RecyclerView.ViewHolder {

        final TextView tvNomeCarteira, tvTipoCarteiraItem, tvSaldoCarteira, tvRendimentoCarteira,
                tvMetaCarteira, tvProgressoCarteira, tvCarteiraSemMeta;
        final ProgressBar progressoMetaCarteira;

        public WalletViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNomeCarteira = itemView.findViewById(R.id.tvNomeCarteira);
            tvTipoCarteiraItem = itemView.findViewById(R.id.tvTipoCarteiraItem);
            tvSaldoCarteira = itemView.findViewById(R.id.tvSaldoCarteira);
            tvRendimentoCarteira = itemView.findViewById(R.id.tvRendimentoCarteira);
            tvMetaCarteira = itemView.findViewById(R.id.tvMetaCarteira);
            tvProgressoCarteira = itemView.findViewById(R.id.tvProgressoCarteira);
            tvCarteiraSemMeta = itemView.findViewById(R.id.tvCarteiraSemMeta);
            progressoMetaCarteira = itemView.findViewById(R.id.progressoMetaCarteira);
        }
    }
}
