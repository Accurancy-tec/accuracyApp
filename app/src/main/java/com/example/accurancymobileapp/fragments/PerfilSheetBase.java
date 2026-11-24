package com.example.accurancymobileapp.fragments;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.network.service.ApiService;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * Slide-up card with a title, up to three fields, an error line and two buttons.
 * Subclasses fill it in onPronto() and drive the steps. When something changes they tell the
 * profile screen through a fragment result (RESULTADO_CHAVE).
 */
public abstract class PerfilSheetBase extends BottomSheetDialogFragment {

    /** Fragment result key the profile screen listens to. */
    public static final String RESULTADO_CHAVE = "perfil_resultado";
    /** Bundle key of the result: ACAO_RECARREGAR or ACAO_SAIR. */
    public static final String RESULTADO_ACAO = "acao";
    public static final String ACAO_RECARREGAR = "recarregar";
    public static final String ACAO_SAIR = "sair";

    protected ApiService api;
    protected TextView tvTitulo, tvSub, tvErro, btnSecundario;
    protected Button btnPrimario;
    private final TextView[] rotulos = new TextView[3];
    private final EditText[] campos = new EditText[3];
    private String textoPrimario = "";
    private boolean ocupado = false;

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
        return inflater.inflate(R.layout.sheet_perfil_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        api = RetrofitClient.getApiService(requireContext());
        tvTitulo = view.findViewById(R.id.tvPerfilSheetTitulo);
        tvSub = view.findViewById(R.id.tvPerfilSheetSub);
        tvErro = view.findViewById(R.id.tvPerfilErro);
        btnPrimario = view.findViewById(R.id.btnPerfilPrimario);
        btnSecundario = view.findViewById(R.id.btnPerfilSecundario);
        rotulos[0] = view.findViewById(R.id.tvPerfilRotulo1);
        rotulos[1] = view.findViewById(R.id.tvPerfilRotulo2);
        rotulos[2] = view.findViewById(R.id.tvPerfilRotulo3);
        campos[0] = view.findViewById(R.id.etPerfilCampo1);
        campos[1] = view.findViewById(R.id.etPerfilCampo2);
        campos[2] = view.findViewById(R.id.etPerfilCampo3);
        esconderCampos();
        onPronto(savedInstanceState);
    }

    protected abstract void onPronto(@Nullable Bundle savedInstanceState);

    /** Shows field n (0..2) with a label, a hint, an input type and a maximum length. */
    protected void campo(int n, String rotulo, String dica, int inputType, int maxLen) {
        rotulos[n].setText(rotulo);
        rotulos[n].setVisibility(View.VISIBLE);
        EditText campo = campos[n];
        campo.setVisibility(View.VISIBLE);
        campo.setHint(dica);
        campo.setInputType(inputType);
        campo.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLen)});
        campo.setText("");
    }

    protected void esconderCampos() {
        for (int i = 0; i < 3; i++) {
            rotulos[i].setVisibility(View.GONE);
            campos[i].setVisibility(View.GONE);
        }
    }

    protected EditText edit(int n) {
        return campos[n];
    }

    protected String texto(int n) {
        return campos[n].getText().toString().trim();
    }

    protected void botaoPrimario(String texto, View.OnClickListener acao) {
        textoPrimario = texto;
        btnPrimario.setText(texto);
        btnPrimario.setOnClickListener(v -> {
            if (!ocupado) acao.onClick(v);
        });
    }

    protected void botaoSecundario(@Nullable String texto, @Nullable View.OnClickListener acao) {
        if (texto == null) {
            btnSecundario.setVisibility(View.GONE);
            return;
        }
        btnSecundario.setText(texto);
        btnSecundario.setVisibility(View.VISIBLE);
        btnSecundario.setOnClickListener(v -> {
            if (!ocupado && acao != null) acao.onClick(v);
        });
    }

    protected void erro(@Nullable String mensagem) {
        if (mensagem == null || mensagem.isEmpty()) {
            tvErro.setVisibility(View.GONE);
        } else {
            tvErro.setText(mensagem);
            tvErro.setVisibility(View.VISIBLE);
        }
    }

    protected void ocupado(boolean valor) {
        ocupado = valor;
        btnPrimario.setEnabled(!valor);
        btnPrimario.setText(valor ? "Aguarde..." : textoPrimario);
        btnSecundario.setEnabled(!valor);
        for (EditText c : campos) c.setEnabled(!valor);
        setCancelable(!valor);
    }

    /** Tells the profile screen what to do and closes the card. */
    protected void concluir(String acao) {
        Bundle resultado = new Bundle();
        resultado.putString(RESULTADO_ACAO, acao);
        getParentFragmentManager().setFragmentResult(RESULTADO_CHAVE, resultado);
        dismissAllowingStateLoss();
    }
}
