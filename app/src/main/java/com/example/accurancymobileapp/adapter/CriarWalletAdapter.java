package com.example.accurancymobileapp.adapter;

import android.content.Context;
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

import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CriarWalletAdapter extends RecyclerView.Adapter<CriarWalletAdapter.WalletViewHolder> {

    private List<Wallet> listaWallets;
    private OnWalletClickListener listener;

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
        BigDecimal patrimonio = wallet.getPatrimonioAtual();
        BigDecimal meta = wallet.getMeta_carteira();
        BigDecimal rentabilidade = wallet.getRentabilidadePercentual();
        String texto = (rentabilidade.compareTo(BigDecimal.ZERO) > 0 ? "+" : "")
                + rentabilidade.toPlainString() + "%";
        int progresso = 0;

        if(meta != null && meta.compareTo(BigDecimal.ZERO) > 0 && patrimonio != null){
            progresso = patrimonio.multiply(BigDecimal.valueOf(100))
                    .divide(meta, 0, RoundingMode.HALF_UP)
                    .intValue();
        }

        int progressoBarra = Math.max(0, Math.min(progresso, 100));

        if(rentabilidade != null){
            if(rentabilidade.compareTo(BigDecimal.ZERO) > 0){
                holder.tvRendimentoCarteira.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.green_positive));
            }
            else if(rentabilidade.compareTo(BigDecimal.ZERO) < 0){
                holder.tvRendimentoCarteira.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.red_negative));
            }
            else{
                holder.tvRendimentoCarteira.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
            }
        }
        holder.tvSaldoCarteira.setText(formatadorMoeda(patrimonio));
        holder.tvNomeCarteira.setText(wallet.getNome_carteira());
        holder.tvRendimentoCarteira.setText(texto);
        holder.tvMetaCarteira.setText(formatadorMoeda(meta));
        holder.tvProgressoCarteira.setText(progresso + "%");

        holder.progressoMetaCarteira.setMax(100);
        holder.progressoMetaCarteira.setProgress(progressoBarra);

        holder.itemView.setOnClickListener(v -> {

            if (listener != null) {
                int idCarteira = wallet.getId_carteira();
                listener.onWalletClick(wallet);
            }

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

    public String formatadorMoeda(BigDecimal valor){
        NumberFormat formato = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        if (valor == null) {
            valor = BigDecimal.ZERO;
        }

        return formato.format(valor);
    }

    public interface OnWalletClickListener {
        void onWalletClick(Wallet wallet);
    }

    public static class WalletViewHolder extends RecyclerView.ViewHolder {

        TextView tvNomeCarteira;
        TextView tvSaldoCarteira;
        TextView tvRendimentoCarteira;
        TextView tvMetaCarteira;
        TextView tvProgressoCarteira;
        ProgressBar progressoMetaCarteira;

        public WalletViewHolder(@NonNull View itemView) {
            super(itemView);

            tvNomeCarteira = itemView.findViewById(R.id.tvNomeCarteira);
            tvSaldoCarteira = itemView.findViewById(R.id.tvSaldoCarteira);
            tvRendimentoCarteira = itemView.findViewById(R.id.tvRendimentoCarteira);
            tvMetaCarteira = itemView.findViewById(R.id.tvMetaCarteira);
            tvProgressoCarteira = itemView.findViewById(R.id.tvProgressoCarteira);
            progressoMetaCarteira = itemView.findViewById(R.id.progressoMetaCarteira);
        }
    }
}