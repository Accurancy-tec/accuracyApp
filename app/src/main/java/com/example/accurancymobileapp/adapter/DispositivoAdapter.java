package com.example.accurancymobileapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.model.SessaoAparelho;
import com.example.accurancymobileapp.utils.TempoRelativo;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/** Aparelhos com a conta aberta; o deste celular vem marcado e o botão dele vira "Sair". */
public class DispositivoAdapter extends RecyclerView.Adapter<DispositivoAdapter.Linha> {

    public interface AoDesconectar {
        void aoDesconectar(@NonNull SessaoAparelho sessao);
    }

    private final List<SessaoAparelho> itens = new ArrayList<>();
    private final AoDesconectar aoDesconectar;
    private boolean habilitado = true;

    public DispositivoAdapter(@NonNull AoDesconectar aoDesconectar) {
        this.aoDesconectar = aoDesconectar;
    }

    public void definir(@NonNull List<SessaoAparelho> sessoes) {
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

    public List<SessaoAparelho> getItens() {
        return itens;
    }

    @NonNull
    @Override
    public Linha onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dispositivo, parent, false);
        return new Linha(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Linha linha, int posicao) {
        SessaoAparelho s = itens.get(posicao);
        String nome = s.getDispositivo();
        boolean celular = !s.isSite() || nome.contains("Android") || nome.contains("iPhone");

        linha.icone.setImageResource(celular ? R.drawable.ic_celular : R.drawable.ic_computador);
        linha.nome.setText(nome);
        linha.esteAparelho.setVisibility(s.isAtual() ? View.VISIBLE : View.GONE);
        linha.uso.setText(descreverUso(s));

        String desde = TempoRelativo.dataCurta(s.getCriadoEm());
        linha.desde.setText(desde.isEmpty() ? "" : "Entrou em " + desde);
        linha.desde.setVisibility(desde.isEmpty() ? View.GONE : View.VISIBLE);

        linha.botao.setText(s.isAtual() ? "Sair" : "Desconectar");
        linha.botao.setEnabled(habilitado);
        linha.botao.setContentDescription((s.isAtual() ? "Sair deste aparelho: " : "Desconectar ") + nome);
        linha.botao.setOnClickListener(v -> aoDesconectar.aoDesconectar(s));
    }

    private static String descreverUso(SessaoAparelho s) {
        String origem = s.isSite() ? "Site" : "Android".equals(s.getPlataforma()) ? "App Android" : "Login";
        if (s.isAtual()) return origem + " · em uso agora";
        String quando = TempoRelativo.descrever(s.getUltimoUsoEm());
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
