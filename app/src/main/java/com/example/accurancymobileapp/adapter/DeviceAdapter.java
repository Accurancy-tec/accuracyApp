package com.example.accurancymobileapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.model.DeviceSession;
import com.example.accurancymobileapp.utils.TempoRelativo;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/** Aparelhos com a conta aberta; o deste celular vem marcado e o botão dele vira "Sair". */
public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.Linha> {

    public interface AoDesconectar {
        void aoDesconectar(@NonNull DeviceSession sessao);
    }

    private final List<DeviceSession> itens = new ArrayList<>();
    private final AoDesconectar aoDesconectar;
    private boolean habilitado = true;

    public DeviceAdapter(@NonNull AoDesconectar aoDesconectar) {
        this.aoDesconectar = aoDesconectar;
    }

    public void definir(@NonNull List<DeviceSession> sessoes) {
        itens.clear();
        itens.addAll(sessoes);
        notifyDataSetChanged();
    }

    /** Trava os botões enquanto um pedido de desconexão está em andamento. */
    public void habilitar(boolean valor) {
        if (habilitado == valor) return;
        habilitado = valor;
        notifyItemRangeChanged(0, itens.size());
    }

    public List<DeviceSession> getItens() {
        return itens;
    }

    @NonNull
    @Override
    public Linha onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device, parent, false);
        return new Linha(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Linha linha, int posicao) {
        DeviceSession s = itens.get(posicao);
        String nome = s.getDeviceName();
        boolean celular = !s.isSite() || nome.contains("Android") || nome.contains("iPhone");

        linha.icone.setImageResource(celular ? R.drawable.ic_phone : R.drawable.ic_computer);
        linha.nome.setText(nome);
        linha.esteAparelho.setVisibility(s.isCurrent() ? View.VISIBLE : View.GONE);
        linha.uso.setText(descreverUso(s));

        String desde = TempoRelativo.dataCurta(s.getCreatedAt());
        linha.desde.setText(desde.isEmpty() ? "" : "Entrou em " + desde);
        linha.desde.setVisibility(desde.isEmpty() ? View.GONE : View.VISIBLE);

        linha.botao.setText(s.isCurrent() ? "Sair" : "Desconectar");
        linha.botao.setEnabled(habilitado);
        linha.botao.setContentDescription((s.isCurrent() ? "Sair deste aparelho: " : "Desconectar ") + nome);
        linha.botao.setOnClickListener(v -> aoDesconectar.aoDesconectar(s));
    }

    private static String descreverUso(DeviceSession s) {
        String origem = s.isSite() ? "Site" : "Android".equals(s.getPlatform()) ? "App Android" : "Login";
        if (s.isCurrent()) return origem + " · em uso agora";
        String quando = TempoRelativo.descrever(s.getLastUsedAt());
        if (quando.isEmpty()) return origem;
        return "agora".equals(quando) ? origem + " · usado agora" : origem + " · último uso " + quando;
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class Linha extends RecyclerView.ViewHolder {
        final ImageView icone;
        final TextView nome, esteAparelho, uso, desde;
        final MaterialButton botao;

        Linha(@NonNull View view) {
            super(view);
            icone = view.findViewById(R.id.imgDispositivo);
            nome = view.findViewById(R.id.tvNomeDispositivo);
            esteAparelho = view.findViewById(R.id.tvEsteAparelho);
            uso = view.findViewById(R.id.tvUsoDispositivo);
            desde = view.findViewById(R.id.tvDesdeDispositivo);
            botao = view.findViewById(R.id.btnDesconectarDispositivo);
        }
    }
}
