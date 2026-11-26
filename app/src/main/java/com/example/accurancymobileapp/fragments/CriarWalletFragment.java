package com.example.accurancymobileapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.activities.FormCriarWalletActivity;
import com.example.accurancymobileapp.adapter.CriarWalletAdapter;
import com.example.accurancymobileapp.adapter.EspacamentoCarteiras;
import com.example.accurancymobileapp.model.Wallet;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.response.ResumoPatrimonioResponse;
import com.example.accurancymobileapp.utils.Aviso;
import com.example.accurancymobileapp.utils.CallbackDaTela;
import com.example.accurancymobileapp.utils.Esqueleto;
import com.example.accurancymobileapp.utils.PuxarParaAtualizar;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Aba Carteira: patrimônio das carteiras reais (simulações à parte), as carteiras em grade
 * e o atalho para criar uma nova. Tudo vem de /carteiras/resumo.
 */
public class CriarWalletFragment extends Fragment {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private CriarWalletAdapter walletAdapter;
    private PuxarParaAtualizar puxar;
    private final List<Wallet> listaWallets = new ArrayList<>();
    private TextView tvTotal, tvRendimento, tvPercentual, tvSimulacoes;
    private View listaVazia, swipe;
    private boolean carregou = false;

    public CriarWalletFragment() {
        super(R.layout.fragment_criar_wallet);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvTotal = view.findViewById(R.id.tvTotalCarteiras);
        tvRendimento = view.findViewById(R.id.tvRendimentoTotal);
        tvPercentual = view.findViewById(R.id.tvPercentualTotal);
        tvSimulacoes = view.findViewById(R.id.tvResumoSimulacoes);
        listaVazia = view.findViewById(R.id.layoutCarteirasVazio);
        swipe = view.findViewById(R.id.swipeRefreshCarteiras);

        RecyclerView rvMinhasWallets = view.findViewById(R.id.rvMinhasWallets);
        rvMinhasWallets.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rvMinhasWallets.addItemDecoration(new EspacamentoCarteiras(dpParaPx(12), dpParaPx(12)));
        walletAdapter = new CriarWalletAdapter(listaWallets, this::abrirWallet);
        rvMinhasWallets.setAdapter(walletAdapter);

        puxar = new PuxarParaAtualizar((SwipeRefreshLayout) swipe, this::carregarCarteiras);

        View.OnClickListener criar = v -> startActivity(new Intent(requireContext(), FormCriarWalletActivity.class));
        view.findViewById(R.id.btnCriarWallet).setOnClickListener(criar);
        view.findViewById(R.id.btnCriarPrimeiraCarteira).setOnClickListener(criar);

        Esqueleto.iniciar(tvTotal, tvRendimento);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Também cobre a volta do formulário de nova carteira e da tela de uma carteira.
        carregarCarteiras();
    }

    private static String reais(double valor) {
        return NumberFormat.getCurrencyInstance(PT_BR).format(valor);
    }

    private void carregarCarteiras() {
        puxar.iniciar();
        RetrofitClient.getApiService(requireContext()).resumoPatrimonio(null)
                .enqueue(new CallbackDaTela<ResumoPatrimonioResponse>(this) {
                    @Override
                    public void aoSucesso(@NonNull ResumoPatrimonioResponse resposta) {
                        puxar.concluir();
                        mostrar(resposta);
                    }

                    @Override
                    public void aoErro(@NonNull String mensagem) {
                        puxar.concluir();
                        if (!carregou) {
                            tvTotal.setText("—");
                            tvRendimento.setText("");
                                            }
                        Aviso.makeText(requireContext(), mensagem, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void mostrar(ResumoPatrimonioResponse resposta) {
        Esqueleto.parar(tvTotal, tvRendimento);
        ResumoPatrimonioResponse.Real real = resposta.getReal();
        ResumoPatrimonioResponse.Simulado simulado = resposta.getSimulado();

        tvTotal.setText(reais(real.valorMercado));
        if (real.totalInvestido > 0) {
            boolean ganho = real.lucroPrejuizo >= 0;
            int cor = ContextCompat.getColor(requireContext(), ganho ? R.color.positivo_destaque : R.color.negativo_destaque);
            tvRendimento.setText((ganho ? "▲ " : "▼ ") + reais(Math.abs(real.lucroPrejuizo))
                    + (ganho ? " de ganho" : " de perda") + " sobre " + reais(real.totalInvestido) + " investidos");
            tvRendimento.setTextColor(cor);
            tvPercentual.setText(String.format(PT_BR, "%+.2f%% de rentabilidade", real.rentabilidadePercentual));
            tvPercentual.setTextColor(cor);
            tvPercentual.setBackgroundResource(ganho ? R.drawable.bg_carteira_percentual : R.drawable.bg_tag_negativo);
            tvPercentual.setVisibility(View.VISIBLE);
        } else {
            tvRendimento.setText(real.quantidadeCarteiras > 0
                    ? "Nenhum ativo comprado nas carteiras reais ainda"
                    : "Crie uma carteira real para acompanhar seus investimentos");
            tvRendimento.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            tvPercentual.setVisibility(View.GONE);
        }

        if (simulado.quantidadeCarteiras > 0) {
            tvSimulacoes.setText((simulado.quantidadeCarteiras == 1 ? "1 simulação" : simulado.quantidadeCarteiras + " simulações")
                    + ": " + reais(simulado.patrimonioTotal) + " · fora do patrimônio");
            tvSimulacoes.setVisibility(View.VISIBLE);
        } else {
            tvSimulacoes.setVisibility(View.GONE);
        }
        Esqueleto.parar(tvTotal, tvRendimento);

        listaWallets.clear();
        listaWallets.addAll(resposta.getCarteiras());
        walletAdapter.notifyDataSetChanged();
        boolean vazio = listaWallets.isEmpty();
        listaVazia.setVisibility(vazio ? View.VISIBLE : View.GONE);
        swipe.setVisibility(vazio ? View.INVISIBLE : View.VISIBLE);
        carregou = true;
    }

    private void abrirWallet(Wallet wallet) {
        Bundle bundle = new Bundle();
        bundle.putInt("id_carteira", wallet.getId_carteira());
        bundle.putString("nome_wallet", wallet.getNome_carteira());
        bundle.putString("tipo_carteira", wallet.getTipo_carteira());

        WalletFragment fragment = new WalletFragment();
        fragment.setArguments(bundle);

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameContent, fragment)
                .addToBackStack(null)
                .commit();
    }

    private int dpParaPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }
}
