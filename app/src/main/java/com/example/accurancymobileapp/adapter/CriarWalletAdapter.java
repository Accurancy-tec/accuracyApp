package com.example.accurancymobileapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.model.Wallet;

import org.w3c.dom.Text;

import java.math.BigDecimal;
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
        BigDecimal total = wallet.getTotalAportado();

        holder.tvSaldoCarteira.setText(formatadorMoeda(total));
        holder.tvNomeCarteira.setText(wallet.getNome_carteira());

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

        return formato.format(valor);
    }

    public interface OnWalletClickListener {
        void onWalletClick(Wallet wallet);
    }

    public static class WalletViewHolder extends RecyclerView.ViewHolder {

        TextView tvNomeCarteira;
        TextView tvSaldoCarteira;
        TextView tvRendimentoCarteira;
        //TextView tvMetaCarteira;
        //TextView tvProgressoCarteira;

        public WalletViewHolder(@NonNull View itemView) {
            super(itemView);

            tvNomeCarteira = itemView.findViewById(R.id.tvNomeCarteira);
            tvSaldoCarteira = itemView.findViewById(R.id.tvSaldoCarteira);
            tvRendimentoCarteira = itemView.findViewById(R.id.tvRendimentoCarteira);
            //tvMetaCarteira = itemView.findViewById(R.id.tvMetaCarteira);
            //tvProgressoCarteira = itemView.findViewById(R.id.tvProgressoCarteira);
        }
    }
}