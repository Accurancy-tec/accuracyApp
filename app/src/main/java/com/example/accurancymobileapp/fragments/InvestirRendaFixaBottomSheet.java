package com.example.accurancymobileapp.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.model.TituloRendaFixa;
import com.example.accurancymobileapp.model.Wallet;
import com.example.accurancymobileapp.model.clsAportes;
import com.example.accurancymobileapp.network.repository.AporteRepository;
import com.example.accurancymobileapp.network.repository.FavoritoRepository;
import com.example.accurancymobileapp.network.repository.WalletRepository;
import com.example.accurancymobileapp.utils.AnimacaoUtils;
import com.example.accurancymobileapp.utils.Aviso;
import com.example.accurancymobileapp.utils.EscolhaCarteira;
import com.example.accurancymobileapp.utils.FavoritoUi;
import com.example.accurancymobileapp.utils.FavoritosCache;
import com.example.accurancymobileapp.utils.MascaraMonetaria;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class InvestirRendaFixaBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG_FRAGMENT = "InvestirRendaFixaBottomSheet";
    private static final String ARG_TITULO = "titulo_renda_fixa";
    private static final String TAG = "RF_APLICACAO";

    private static final BigDecimal VALOR_MAXIMO = new BigDecimal("10000000.00");
    @ColorRes private static final int COR_CHIP_SELECIONADO = R.color.link;
    @ColorRes private static final int COR_CHIP_NORMAL = R.color.texto_suave;
    @ColorRes private static final int COR_ERRO = R.color.negativo_destaque;
    @ColorRes private static final int COR_AVISO = R.color.texto_fraco;

    /** Optional listener notified after a successful application. */
    public interface AoInvestirListener {
        void aoInvestir();
    }

    /** Implemented by the host activity to redraw its list when the card closes (hearts may have changed). */
    public interface AoFecharListener {
        void aoFecharSheet();
    }

    public static InvestirRendaFixaBottomSheet nova(TituloRendaFixa titulo) {
        InvestirRendaFixaBottomSheet sheet = new InvestirRendaFixaBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TITULO, titulo);
        sheet.setArguments(args);
        return sheet;
    }

    private final NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    private TituloRendaFixa titulo;
    private BigDecimal aplicacaoMinima = BigDecimal.ZERO;
    /** Balance read from the API; null while loading or when unavailable. */
    private BigDecimal saldoDisponivel;

    private View conteudo;
    private EditText etValor;
    private View campoValor;
    private TextView tvAviso, tvSaldo, btnInvestir;
    private TextView chipSaldoTotal, chipMinimo, chip500, chip1000;
    private ImageView btnFavorito;
    private FavoritoRepository favoritoRepository;

    private boolean requisicaoEmAndamento = false;
    private boolean atualizandoCampo = false;
    private boolean concluido = false;
    private AoInvestirListener listener;

    public void setAoInvestirListener(AoInvestirListener listener) {
        this.listener = listener;
    }

    @Override
    public int getTheme() {
        return R.style.ThemeOverlay_Accurancy_BottomSheet;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(d -> {
            View area = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (area != null) {
                area.setBackgroundColor(Color.TRANSPARENT);
                BottomSheetBehavior<View> comportamento = BottomSheetBehavior.from(area);
                comportamento.setState(BottomSheetBehavior.STATE_EXPANDED);
                comportamento.setSkipCollapsed(true);
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sheet_investir_renda_fixa, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        Object recebido = args == null ? null : args.getSerializable(ARG_TITULO);
        if (!(recebido instanceof TituloRendaFixa)) {
            Log.e(TAG, "Titulo nao informado ao card de aplicacao.");
            Aviso.makeText(requireContext(), "Não foi informado qual título aplicar.", Toast.LENGTH_LONG).show();
            dismissAllowingStateLoss();
            return;
        }
        titulo = (TituloRendaFixa) recebido;
        if (titulo.getAplicacaoMinima() != null) {
            aplicacaoMinima = titulo.getAplicacaoMinima();
        }

        conteudo = view.findViewById(R.id.conteudoSheetRf);
        etValor = view.findViewById(R.id.etValorSheet);
        campoValor = view.findViewById(R.id.campoValorSheet);
        tvAviso = view.findViewById(R.id.tvSheetAviso);
        tvSaldo = view.findViewById(R.id.tvSheetSaldo);
        btnInvestir = view.findViewById(R.id.btnInvestirSheet);
        chipSaldoTotal = view.findViewById(R.id.chipSaldoTotal);
        chipMinimo = view.findViewById(R.id.chipMinimo);
        chip500 = view.findViewById(R.id.chip500);
        chip1000 = view.findViewById(R.id.chip1000);

        preencherTitulo(view);
        configurarFavorito(view);
        configurarChips();
        configurarCampo();
        btnInvestir.setOnClickListener(v -> confirmar());
        AnimacaoUtils.efeitoPressionar(btnInvestir);
        view.findViewById(R.id.btnFecharSheet).setOnClickListener(v -> dismissAllowingStateLoss());

        atualizarEstado();
        carregarSaldo();

        AnimacaoUtils.entradaEmCascata(
                view.findViewById(R.id.blocoTituloSheet),
                view.findViewById(R.id.blocoResumoSheet),
                view.findViewById(R.id.tvSheetPergunta),
                tvSaldo,
                campoValor,
                view.findViewById(R.id.blocoAtalhosSheet),
                view.findViewById(R.id.tvSheetNota),
                btnInvestir);
    }

    private void preencherTitulo(View view) {
        ((TextView) view.findViewById(R.id.tvSheetTipo)).setText(
                titulo.getTipoTitulo() == null ? "Renda fixa" : titulo.getTipoTitulo());
        ((TextView) view.findViewById(R.id.tvSheetNome)).setText(titulo.getNomeTitulo());
        ((TextView) view.findViewById(R.id.tvSheetRentabilidade)).setText(titulo.getRentabilidadeFormatada());
        ((TextView) view.findViewById(R.id.tvSheetVencimento)).setText(titulo.getVencimentoFormatado());
        ((TextView) view.findViewById(R.id.tvSheetLiquidez)).setText(
                titulo.getLiquidez() == null || titulo.getLiquidez().trim().isEmpty()
                        ? "Não informado" : titulo.getLiquidez());
        view.findViewById(R.id.tvSheetSeloFgc).setVisibility(titulo.possuiGarantiaFgc() ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.tvSheetSeloIr).setVisibility(titulo.isIsentoIR() ? View.VISIBLE : View.GONE);
    }

    private void configurarFavorito(View view) {
        btnFavorito = view.findViewById(R.id.btnFavoritoSheet);
        favoritoRepository = new FavoritoRepository(requireContext());
        FavoritoUi.aplicar(btnFavorito,
                FavoritosCache.contem(titulo.getSimboloFavorito(), TituloRendaFixa.CATEGORIA_FAVORITO));
        AnimacaoUtils.efeitoPressionar(btnFavorito);
        btnFavorito.setOnClickListener(v -> favoritoRepository.alternar(
                titulo.getSimboloFavorito(), titulo.getNomeTitulo(), TituloRendaFixa.CATEGORIA_FAVORITO,
                new FavoritoRepository.AlternarCallback() {
                    @Override
                    public void onResultado(boolean favorito) {
                        if (isAdded() && getView() != null) {
                            FavoritoUi.aplicarComAnimacao(btnFavorito, favorito);
                        }
                    }

                    @Override
                    public void onError(boolean estadoAnterior, String mensagem) {
                        if (!isAdded() || getView() == null) {
                            return;
                        }
                        FavoritoUi.aplicar(btnFavorito, estadoAnterior);
                        Aviso.makeText(requireContext(), mensagem, Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (getActivity() instanceof AoFecharListener) {
            ((AoFecharListener) getActivity()).aoFecharSheet();
        }
    }

    private void carregarSaldo() {
        tvSaldo.setText("Saldo disponível: …");
        new WalletRepository(requireContext()).buscarSaldo(new WalletRepository.WalletCallback() {
            @Override
            public void onSuccess(String saldo) {
                if (!isAdded() || getView() == null) {
                    return;
                }
                try {
                    saldoDisponivel = new BigDecimal(saldo.trim());
                } catch (Exception e) {
                    saldoDisponivel = null;
                }
                if (saldoDisponivel == null) {
                    tvSaldo.setText("Saldo indisponível no momento");
                } else {
                    AnimacaoUtils.contarAte(tvSaldo, saldoDisponivel.doubleValue(), "Saldo disponível: ");
                }
                atualizarEstado();
            }

            @Override
            public void onError(String message) {
                if (!isAdded() || getView() == null) {
                    return;
                }
                Log.w(TAG, "Saldo nao carregado: " + message);
                saldoDisponivel = null;
                tvSaldo.setText("Saldo indisponível no momento");
            }
        });
    }

    private void configurarChips() {
        chipSaldoTotal.setOnClickListener(v -> {
            if (saldoDisponivel == null || saldoDisponivel.compareTo(BigDecimal.ZERO) <= 0) {
                mostrarAviso("Você ainda não tem saldo disponível para investir.", true);
                AnimacaoUtils.tremer(campoValor);
                return;
            }
            // Invest the full balance, limited to the per-application maximum.
            BigDecimal valor = saldoDisponivel.min(VALOR_MAXIMO).setScale(2, java.math.RoundingMode.DOWN);
            selecionarValor(chipSaldoTotal, valor);
        });
        chipMinimo.setOnClickListener(v -> selecionarValor(chipMinimo, aplicacaoMinima));
        chip500.setOnClickListener(v -> selecionarValor(chip500, new BigDecimal("500")));
        chip1000.setOnClickListener(v -> selecionarValor(chip1000, new BigDecimal("1000")));
        for (View chip : new View[]{chipSaldoTotal, chipMinimo, chip500, chip1000}) {
            AnimacaoUtils.efeitoPressionar(chip);
        }
    }

    private void selecionarValor(TextView chip, BigDecimal valor) {
        limparSelecaoChips();
        chip.setBackgroundResource(R.drawable.bg_valor_rapido_selecionado);
        chip.setTextColor(cor(COR_CHIP_SELECIONADO));

        atualizandoCampo = true;
        etValor.setText(formatoMoeda.format(valor));
        etValor.setSelection(etValor.getText().length());
        atualizandoCampo = false;

        atualizarEstado();
    }

    private void limparSelecaoChips() {
        TextView[] chips = {chipSaldoTotal, chipMinimo, chip500, chip1000};
        for (TextView chip : chips) {
            chip.setBackgroundResource(R.drawable.bg_valor_rapido);
            chip.setTextColor(cor(COR_CHIP_NORMAL));
        }
    }

    private void configurarCampo() {
        etValor.addTextChangedListener(new MascaraMonetaria(etValor));
        etValor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action required.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No action required.
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!atualizandoCampo) {
                    limparSelecaoChips();
                }
                atualizarEstado();
            }
        });
    }

    private BigDecimal valorDigitado() {
        return MascaraMonetaria.obterValor(etValor);
    }

    /** Returns the message that blocks the application, or null when the amount is valid. */
    private String validar(BigDecimal valor) {
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            return "Informe o valor da aplicação.";
        }
        if (valor.compareTo(aplicacaoMinima) < 0) {
            return "Valor mínimo: " + formatoMoeda.format(aplicacaoMinima);
        }
        if (valor.compareTo(VALOR_MAXIMO) > 0) {
            return "Valor máximo por aplicação: " + formatoMoeda.format(VALOR_MAXIMO);
        }
        if (tituloVencido()) {
            return "Este título já venceu e não aceita novas aplicações.";
        }
        // O saldo é conferido quando a carteira de destino é conhecida (simulada usa o saldo dela).
        return null;
    }

    private boolean tituloVencido() {
        String vencimento = titulo.getVencimento();
        if (vencimento == null || vencimento.trim().isEmpty()) {
            return false;
        }
        try {
            return LocalDate.parse(vencimento.trim()).isBefore(LocalDate.now());
        } catch (Exception ignored) {
            // Unknown format: let the API decide.
            return false;
        }
    }

    private void mostrarAviso(String texto, boolean erro) {
        tvAviso.setText(texto);
        tvAviso.setTextColor(cor(erro ? COR_ERRO : COR_AVISO));
    }

    private void atualizarEstado() {
        if (concluido || tvAviso == null) {
            return;
        }
        BigDecimal valor = valorDigitado();
        String problema = validar(valor);
        boolean valido = problema == null;

        if (valido) {
            mostrarAviso(" ", false);
        } else if (valor.compareTo(BigDecimal.ZERO) > 0 || tituloVencido()) {
            mostrarAviso(problema, true);
        } else {
            mostrarAviso("Aplicação mínima: " + formatoMoeda.format(aplicacaoMinima), false);
        }

        boolean habilitado = valido && !requisicaoEmAndamento;
        btnInvestir.setEnabled(habilitado);
        btnInvestir.setAlpha(habilitado ? 1f : 0.5f);
    }

    private void confirmar() {
        if (requisicaoEmAndamento || concluido) {
            return;
        }
        BigDecimal valor = valorDigitado();
        String problema = validar(valor);
        if (problema != null) {
            mostrarAviso(problema, true);
            AnimacaoUtils.tremer(campoValor);
            return;
        }

        definirEmAndamento(true);
        new WalletRepository(requireContext()).buscarWallets(new WalletRepository.WalletListCallback() {
            @Override
            public void onSuccess(List<Wallet> wallets) {
                if (!isAdded() || getView() == null) {
                    return;
                }
                if (wallets == null || wallets.isEmpty()) {
                    definirEmAndamento(false);
                    mostrarAviso("Crie uma carteira antes de aplicar em renda fixa.", true);
                    AnimacaoUtils.tremer(campoValor);
                    return;
                }
                if (wallets.size() == 1) {
                    conferirSaldoERegistrar(valor, wallets.get(0));
                    return;
                }
                EscolhaCarteira.mostrar(requireContext(), "Aplicar em qual carteira?", wallets, saldoDisponivel,
                        carteira -> conferirSaldoERegistrar(valor, carteira),
                        () -> definirEmAndamento(false));
            }

            @Override
            public void onError(String message) {
                if (!isAdded() || getView() == null) {
                    return;
                }
                Log.e(TAG, "Falha ao listar carteiras: " + message);
                definirEmAndamento(false);
                mostrarAviso(message, true);
                AnimacaoUtils.tremer(campoValor);
            }
        });
    }

    private void conferirSaldoERegistrar(BigDecimal valor, Wallet carteira) {
        String falta = EscolhaCarteira.faltaSaldo(carteira, saldoDisponivel, valor);
        if (falta != null) {
            definirEmAndamento(false);
            mostrarAviso(falta, true);
            AnimacaoUtils.tremer(campoValor);
            return;
        }
        registrar(valor, carteira.getId_carteira());
    }

    private void registrar(BigDecimal valor, int idCarteira) {
        Log.d(TAG, "Enviando aplicacao: titulo=" + titulo.getIdTitulo()
                + " carteira=" + idCarteira + " valor=" + valor.toPlainString());

        clsAportes aporte = clsAportes.paraRendaFixa(
                titulo.getIdTitulo(),
                titulo.getNomeTitulo(),
                valor.doubleValue(),
                idCarteira
        );

        new AporteRepository(requireContext()).registrarAporte(aporte, new AporteRepository.RegistroCallback() {
            @Override
            public void onSucesso(String mensagem) {
                if (!isAdded() || getView() == null) {
                    return;
                }
                Log.d(TAG, "Aplicacao registrada: titulo=" + titulo.getIdTitulo());
                mostrarSucesso(valor);
            }

            @Override
            public void onErro(String mensagem) {
                if (!isAdded() || getView() == null) {
                    return;
                }
                Log.w(TAG, "Aplicacao recusada: " + mensagem);
                definirEmAndamento(false);
                mostrarAviso(mensagem, true);
                AnimacaoUtils.tremer(campoValor);
            }
        });
    }

    private void mostrarSucesso(BigDecimal valor) {
        concluido = true;
        requisicaoEmAndamento = false;
        btnInvestir.setEnabled(false);
        btnInvestir.setAlpha(1f);
        btnInvestir.setBackgroundResource(R.drawable.bg_btn_sucesso);
        btnInvestir.setText("Aplicação realizada ✓");
        AnimacaoUtils.destacar(btnInvestir);
        mostrarAviso("Você aplicou " + formatoMoeda.format(valor) + " em " + titulo.getNomeTitulo()
                + " (operação simulada).", false);
        etValor.setEnabled(false);
        if (listener != null) {
            listener.aoInvestir();
        }
        btnInvestir.postDelayed(() -> {
            if (isAdded()) {
                dismissAllowingStateLoss();
            }
        }, 1400L);
    }

    private void definirEmAndamento(boolean emAndamento) {
        requisicaoEmAndamento = emAndamento;
        btnInvestir.setText(emAndamento ? "Registrando aplicação…" : "Investir");
        atualizarEstado();
    }

    private int cor(@ColorRes int id) {
        return ContextCompat.getColor(requireContext(), id);
    }
}
