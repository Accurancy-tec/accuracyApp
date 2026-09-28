package com.example.accurancymobileapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.model.Wallet;

import java.util.List;

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

        holder.txtNomeWallet.setText(wallet.getNome_carteira());
        holder.txtTipoWallet.setText(wallet.getTipo_carteira());

        holder.itemView.setOnClickListener(v -> {

            if (listener != null) {
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

    public interface OnWalletClickListener {
        void onWalletClick(Wallet wallet);
    }

    public static class WalletViewHolder extends RecyclerView.ViewHolder {

        TextView txtNomeWallet;
        TextView txtTipoWallet;

        public WalletViewHolder(@NonNull View itemView) {
            super(itemView);

            txtNomeWallet = itemView.findViewById(R.id.txtNomeWallet);
            txtTipoWallet = itemView.findViewById(R.id.txtTipoWallet);
        }
    }
}