package com.example.accurancymobileapp.fragments;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.adapter.AporteAdapter;
import com.example.accurancymobileapp.adapter.EsqueletoAdapter;
import com.example.accurancymobileapp.adapter.PositionAdapter;
import com.example.accurancymobileapp.model.Aporte;
import com.example.accurancymobileapp.model.Position;
import com.example.accurancymobileapp.model.Wallet;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.network.repository.AporteRepository;
import com.example.accurancymobileapp.network.repository.WalletRepository;
import com.example.accurancymobileapp.response.ResumoPatrimonioResponse;
import com.example.accurancymobileapp.utils.Aviso;
import com.example.accurancymobileapp.utils.CallbackDaTela;
import com.example.accurancymobileapp.utils.CarteiraEmFoco;
import com.example.accurancymobileapp.utils.Esqueleto;
import com.example.accurancymobileapp.utils.MascaraMonetaria;
import com.example.accurancymobileapp.utils.PuxarParaAtualizar;
import com.example.accurancymobileapp.utils.TempoRelativo;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A wallet: net worth and result calculated by the API, actions (invest, deposit in the
 * simulation, rename, delete), the assets it holds now and the operations registered in it.
 */
public class WalletFragment extends Fragment {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final BigDecimal DEPOSITO_MAXIMO = new BigDecimal("10000000");

    private int idCarteira;
    private String nomeCarteira;
    private boolean simulada;
    @Nullable
    private Wallet carteira;

    private WalletRepository walletRepository;
    private PuxarParaAtualizar puxar;
    private RecyclerView recyclerInvestimentos;
    private TextView txtCarteiraVazia, tvTitulo, tvTipo, tvRotuloPatrimonio, tvValor, tvBadge, tvInvestido,
            tvSaldoLivre, tvResultado, tvMeta, tvQuantidade;
    private View colunaSaldoSimulacao, blocoMeta;
    private LinearProgressIndicator progressoMeta;
    private MaterialButton btnInvestir, btnDepositar;
    private boolean primeiraRetomada = true;
    private boolean carregouOperacoes = false;
    private String atualizadoEm;
    private int quantidadeOperacoes = -1;
    @Nullable
    private List<Aporte> operations;
    private boolean showingPositions = true;

    public WalletFragment() {
        super(R.layout.fragment_wallet);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        idCarteira = args != null ? args.getInt("id_carteira", -1) : -1;
        nomeCarteira = args != null ? args.getString("nome_wallet", "Carteira") : "Carteira";
        simulada = args != null && "Simulada".equalsIgnoreCase(args.getString("tipo_carteira"));
        walletRepository = new WalletRepository(requireContext());

        tvTitulo = view.findViewById(R.id.tvTituloCarteira);
        tvTipo = view.findViewById(R.id.tvTipoCarteira);
        tvRotuloPatrimonio = view.findViewById(R.id.tvRotuloPatrimonio);
        tvValor = view.findViewById(R.id.tvValorAcumulado);
        tvBadge = view.findViewById(R.id.tvRentabilidadePercentual);
        tvInvestido = view.findViewById(R.id.tvInvestidoCarteira);
        tvSaldoLivre = view.findViewById(R.id.tvSaldoLivreCarteira);
        tvResultado = view.findViewById(R.id.tvRentabilidadeValor);
        tvMeta = view.findViewById(R.id.tvMetaCarteira);
        tvQuantidade = view.findViewById(R.id.tvQuantidadeAtivos);
        colunaSaldoSimulacao = view.findViewById(R.id.colunaSaldoSimulacao);
        blocoMeta = view.findViewById(R.id.blocoMetaCarteira);
        progressoMeta = view.findViewById(R.id.progressoMetaCarteira);
        btnInvestir = view.findViewById(R.id.btnInvestirCarteira);
        btnDepositar = view.findViewById(R.id.btnDepositarSimulacao);
        recyclerInvestimentos = view.findViewById(R.id.recyclerInvestimentos);
        txtCarteiraVazia = view.findViewById(R.id.txtCarteiraVazia);
        recyclerInvestimentos.setLayoutManager(new LinearLayoutManager(requireContext()));

        puxar = new PuxarParaAtualizar(view.findViewById(R.id.swipeCarteira), this::carregarTudo);
        view.findViewById(R.id.btnVoltar).setOnClickListener(v -> getParentFragmentManager().popBackStack());
        view.findViewById(R.id.btnExcluirCarteira).setOnClickListener(v -> confirmarExclusao());
        view.findViewById(R.id.btnRenameWallet).setOnClickListener(v -> askNewName());
        setUpListTabs(view.findViewById(R.id.walletListTabs));
        view.findViewById(R.id.btnAtualizar).setOnClickListener(v -> puxar.disparar(this::carregarTudo));
        btnInvestir.setOnClickListener(v -> irInvestir());
        btnDepositar.setOnClickListener(v -> abrirDeposito());

        tvTitulo.setText(nomeCarteira);
        mostrarTipo(null);
        Esqueleto.iniciar(tvValor, tvInvestido, tvSaldoLivre, tvResultado);

        if (idCarteira <= 0) {
            Aviso.makeText(requireContext(), "Não foi possível identificar a carteira.", Toast.LENGTH_LONG).show();
            getParentFragmentManager().popBackStack();
            return;
        }
        carregarTudo();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Volta de uma compra, venda ou dividendo: os números e a lista mudaram.
        if (primeiraRetomada) {
            primeiraRetomada = false;
            return;
        }
        carregarTudo();
    }

    private void carregarTudo() {
        carregarResumo();
        carregarOperacoes();
    }

    private static String reais(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(PT_BR).format(valor);
    }

    private static String reaisComSinal(BigDecimal valor) {
        String texto = reais(valor.abs());
        int sinal = valor.setScale(2, RoundingMode.HALF_UP).signum();
        return sinal > 0 ? "+" + texto : sinal < 0 ? "−" + texto : texto;
    }

    private static String porcentagem(BigDecimal valor) {
        return String.format(PT_BR, "%.2f%%", valor.abs().doubleValue());
    }

    private int cor(int id) {
        return ContextCompat.getColor(requireContext(), id);
    }

    // ---------------------------------------------------------------- resumo

    private void carregarResumo() {
        puxar.iniciar();
        RetrofitClient.getApiService(requireContext()).resumoPatrimonio(idCarteira)
                .enqueue(new CallbackDaTela<ResumoPatrimonioResponse>(this) {
                    @Override
                    public void aoSucesso(@NonNull ResumoPatrimonioResponse resposta) {
                        puxar.concluir();
                        List<Wallet> carteiras = resposta.getCarteiras();
                        if (carteiras.isEmpty()) {
                            aoErro("Carteira não encontrada.");
                            return;
                        }
                        atualizadoEm = resposta.getAtualizadoEm();
                        mostrarCarteira(carteiras.get(0));
                    }

                    @Override
                    public void aoErro(@NonNull String mensagem) {
                        puxar.concluir();
                        if (carteira == null) {
                            for (TextView campo : new TextView[]{tvValor, tvInvestido, tvSaldoLivre, tvResultado}) {
                                campo.setText("—");
                            }
                                            }
                        Aviso.makeText(requireContext(), mensagem, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void mostrarTipo(@Nullable String estilo) {
        String rotuloEstilo = estilo == null ? "" : " · " + ("Renda Variavel".equals(estilo) ? "Renda variável"
                : "Renda Fixa".equals(estilo) ? "Renda fixa" : estilo);
        tvTipo.setText((simulada ? "Carteira simulada" : "Carteira real") + rotuloEstilo);
        tvRotuloPatrimonio.setText(simulada ? "PATRIMÔNIO DA SIMULAÇÃO" : "PATRIMÔNIO DA CARTEIRA");
        colunaSaldoSimulacao.setVisibility(simulada ? View.VISIBLE : View.GONE);
        btnDepositar.setVisibility(simulada ? View.VISIBLE : View.GONE);
        btnInvestir.setText(simulada ? "Simular investimento" : "Investir");
    }

    private void mostrarCarteira(Wallet dados) {
        Esqueleto.parar(tvValor, tvInvestido, tvSaldoLivre, tvResultado);
        carteira = dados;
        simulada = dados.isSimulada();
        nomeCarteira = dados.getNome_carteira();
        tvTitulo.setText(nomeCarteira);
        mostrarTipo(dados.getEstilo_carteira());

        tvValor.setText(reais(dados.getPatrimonioAtual()));
        tvInvestido.setText(reais(dados.getTotalInvestido()));
        tvSaldoLivre.setText(reais(dados.getSaldoLivre()));

        // Na simulação o resultado compara o patrimônio com o dinheiro depositado nela;
        // na carteira real, o valor de mercado com o custo dos ativos.
        BigDecimal resultado = simulada ? dados.getResultadoSimulacao() : dados.getLucroPrejuizo();
        BigDecimal rentabilidade = simulada ? dados.getRentabilidadeSimulacao() : dados.getRentabilidadePercentual();
        boolean temBase = simulada ? dados.getCapitalSimulado().signum() > 0 : dados.getTotalInvestido().signum() > 0;
        boolean ganho = resultado.signum() >= 0;

        tvResultado.setText(reaisComSinal(resultado));
        tvResultado.setTextColor(cor(!temBase || resultado.signum() == 0 ? R.color.white
                : ganho ? R.color.positivo_sobre_destaque : R.color.negativo_sobre_destaque));

        if (temBase) {
            String base = simulada ? " sobre " + reais(dados.getCapitalSimulado()) + " depositados" : " sobre o investido";
            tvBadge.setText((ganho ? "▲ " : "▼ ") + porcentagem(rentabilidade) + base);
            tvBadge.setVisibility(View.VISIBLE);
        } else {
            tvBadge.setVisibility(View.GONE);
        }

        BigDecimal meta = dados.getMeta_carteira();
        if (meta.signum() > 0) {
            int progresso = dados.getProgressoMeta().max(BigDecimal.ZERO).min(new BigDecimal(100)).intValue();
            tvMeta.setText(dados.isMetaAtingida()
                    ? "Meta de " + reais(meta) + " alcançada"
                    : "Meta de " + reais(meta) + " · " + progresso + "%");
            progressoMeta.setProgressCompat(progresso, true);
            blocoMeta.setVisibility(View.VISIBLE);
        } else {
            blocoMeta.setVisibility(View.GONE);
        }

        Esqueleto.parar(tvValor, tvInvestido, tvSaldoLivre, tvResultado);
        if (showingPositions) showList();
    }

    // ------------------------------------------------------------ operações

    private void carregarOperacoes() {
        if (!carregouOperacoes && !puxar.atualizando()) {
            txtCarteiraVazia.setVisibility(View.GONE);
            recyclerInvestimentos.setVisibility(View.VISIBLE);
            EsqueletoAdapter.mostrar(recyclerInvestimentos, R.layout.item_esqueleto_linha, 4);
        }
        puxar.iniciar();
        new AporteRepository(requireContext()).buscarAportes(idCarteira, new AporteRepository.aporteCallback() {
            @Override
            public void onSucesso(List<Aporte> aportes) {
                puxar.concluir();
                if (!isAdded() || getView() == null) return;
                operations = aportes != null ? aportes : new ArrayList<>();
                carregouOperacoes = true;
                quantidadeOperacoes = operations.size();
                if (!showingPositions) showList();
            }

            @Override
            public void onErro(String mensagem) {
                puxar.concluir();
                if (!isAdded() || getView() == null) return;
                if (showingPositions) return;
                EsqueletoAdapter.ocultar(recyclerInvestimentos, new AporteAdapter(new ArrayList<>(), idCarteira));
                showEmptyState(true, "Não foi possível carregar as operações agora.");
            }
        });
    }

    private void setUpListTabs(ChipGroup tabs) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        String[] labels = {"Ativos", "Movimentações"};
        for (int i = 0; i < labels.length; i++) {
            Chip chip = (Chip) inflater.inflate(R.layout.item_chip_filtro, tabs, false);
            chip.setId(View.generateViewId());
            chip.setText(labels[i]);
            chip.setCheckable(true);
            chip.setChecked(i == 0);
            final boolean positionsTab = i == 0;
            chip.setOnClickListener(v -> {
                if (showingPositions == positionsTab) return;
                showingPositions = positionsTab;
                showList();
            });
            tabs.addView(chip);
        }
    }

    /** Assets held now (sold ones only show up in the operations) or every operation of the wallet. */
    private void showList() {
        if (showingPositions) {
            if (carteira == null) return;
            List<Position> positions = carteira.getPositions();
            EsqueletoAdapter.ocultar(recyclerInvestimentos, new PositionAdapter(positions, idCarteira));
            showEmptyState(positions.isEmpty(), simulada
                    ? "Nenhum ativo nesta simulação agora. Toque em Simular investimento para começar; o que foi vendido aparece em Movimentações."
                    : "Nenhum ativo nesta carteira agora. Toque em Investir para começar; o que foi vendido aparece em Movimentações.");
        } else {
            if (operations == null) {
                EsqueletoAdapter.mostrar(recyclerInvestimentos, R.layout.item_esqueleto_linha, 4);
                showEmptyState(false, "");
                return;
            }
            EsqueletoAdapter.ocultar(recyclerInvestimentos, new AporteAdapter(operations, idCarteira));
            showEmptyState(operations.isEmpty(), simulada
                    ? "Nenhuma operação nesta simulação ainda. Toque em Simular investimento para começar."
                    : "Nenhuma operação nesta carteira ainda. Toque em Investir para fazer a primeira.");
        }
        mostrarQuantidade();
    }

    private void showEmptyState(boolean empty, String text) {
        recyclerInvestimentos.setVisibility(empty ? View.GONE : View.VISIBLE);
        txtCarteiraVazia.setText(text);
        txtCarteiraVazia.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private void mostrarQuantidade() {
        String quantity;
        if (showingPositions) {
            if (carteira == null) return;
            int count = carteira.getPositions().size();
            quantity = count == 1 ? "1 ativo" : count + " ativos";
        } else {
            if (quantidadeOperacoes < 0) return;
            quantity = quantidadeOperacoes == 1 ? "1 operação" : quantidadeOperacoes + " operações";
        }
        String hora = TempoRelativo.hora(atualizadoEm);
        tvQuantidade.setText(hora.isEmpty() ? quantity : quantity + " · atualizado às " + hora);
    }

    // --------------------------------------------------------------- ações

    /** Vai para Aportes com esta carteira já escolhida para a próxima compra. */
    private void irInvestir() {
        CarteiraEmFoco.definir(requireContext(), idCarteira);
        Aviso.makeText(requireContext(), simulada
                ? "Escolha um ativo. A compra vai para a simulação \"" + nomeCarteira + "\"."
                : "Escolha um ativo para investir em \"" + nomeCarteira + "\".", Toast.LENGTH_LONG).show();
        BottomNavigationView navegacao = requireActivity().findViewById(R.id.bottomNavigation);
        getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        if (navegacao != null) navegacao.setSelectedItemId(R.id.nav_aporte);
    }

    private void abrirDeposito() {
        View conteudo = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_deposito_simulacao, null, false);
        EditText campo = conteudo.findViewById(R.id.edtValorDepositoSimulacao);
        TextView erro = conteudo.findViewById(R.id.tvErroDepositoSimulacao);
        campo.setInputType(InputType.TYPE_CLASS_NUMBER);
        campo.addTextChangedListener(new MascaraMonetaria(campo));

        AlertDialog dialogo = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Depositar na simulação")
                .setView(conteudo)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Depositar", null)
                .create();
        dialogo.setOnShowListener(d -> dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    BigDecimal valor = MascaraMonetaria.obterValor(campo);
                    if (valor.signum() <= 0 || valor.compareTo(DEPOSITO_MAXIMO) > 0) {
                        erro.setText("Informe um valor entre R$ 0,01 e R$ 10.000.000,00.");
                        erro.setVisibility(View.VISIBLE);
                        return;
                    }
                    v.setEnabled(false);
                    walletRepository.depositarSimulacao(idCarteira, valor, new WalletRepository.WalletCallback() {
                        @Override
                        public void onSuccess(String saldo) {
                            if (!isAdded() || getView() == null) return;
                            dialogo.dismiss();
                            Aviso.makeText(requireContext(), reais(valor) + " depositados na simulação.",
                                    Toast.LENGTH_SHORT).show();
                            carregarResumo();
                        }

                        @Override
                        public void onError(String mensagem) {
                            if (!isAdded() || getView() == null) return;
                            v.setEnabled(true);
                            erro.setText(mensagem);
                            erro.setVisibility(View.VISIBLE);
                        }
                    });
                }));
        dialogo.show();
    }

    private void askNewName() {
        TextInputLayout layout = new TextInputLayout(requireContext());
        int padding = Math.round(20 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding / 2, padding, 0);
        layout.setCounterEnabled(true);
        layout.setCounterMaxLength(60);
        TextInputEditText field = new TextInputEditText(layout.getContext());
        field.setSingleLine(true);
        field.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        field.setText(nomeCarteira);
        field.selectAll();
        layout.addView(field);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Renomear carteira")
                .setView(layout)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar", null)
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = field.getText() == null ? "" : field.getText().toString().trim().replaceAll("\\s+", " ");
            if (name.isEmpty() || name.length() > 60) {
                layout.setError("Dê um nome de até 60 caracteres.");
                return;
            }
            v.setEnabled(false);
            walletRepository.renameWallet(idCarteira, name, new WalletRepository.WalletCallback() {
                @Override
                public void onSuccess(String message) {
                    if (!isAdded() || getView() == null) return;
                    dialog.dismiss();
                    nomeCarteira = name;
                    tvTitulo.setText(name);
                    Aviso.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String message) {
                    if (!isAdded() || getView() == null) return;
                    v.setEnabled(true);
                    layout.setError(message);
                }
            });
        }));
        dialog.show();
    }

    private void confirmarExclusao() {
        String mensagem = simulada
                ? "Os ativos da simulação \"" + nomeCarteira + "\" serão vendidos pelo preço atual, com dinheiro fictício, "
                + "e ela sai da lista. As operações continuam no histórico."
                : "Os ativos da carteira \"" + nomeCarteira + "\" serão vendidos pelo preço atual e o dinheiro volta "
                + "para o seu saldo. As operações continuam no histórico. Carteiras com renda fixa não podem ser excluídas.";
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Excluir carteira?")
                .setMessage(mensagem)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (d, w) -> excluirCarteira())
                .show();
    }

    private void excluirCarteira() {
        walletRepository.deletarWallet(idCarteira, new WalletRepository.WalletCallback() {
            @Override
            public void onSuccess(String mensagem) {
                if (!isAdded()) return;
                if (CarteiraEmFoco.obter(requireContext()) == idCarteira) CarteiraEmFoco.limpar(requireContext());
                Aviso.makeText(requireContext(), mensagem, Toast.LENGTH_LONG).show();
                getParentFragmentManager().popBackStack();
            }

            @Override
            public void onError(String mensagem) {
                if (!isAdded()) return;
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Não foi possível excluir")
                        .setMessage(mensagem)
                        .setPositiveButton("Entendi", null)
                        .show();
            }
        });
    }
}
