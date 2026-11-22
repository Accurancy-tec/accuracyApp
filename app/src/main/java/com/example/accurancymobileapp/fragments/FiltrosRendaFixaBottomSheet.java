package com.example.accurancymobileapp.fragments;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.model.FiltroRendaFixa;
import com.example.accurancymobileapp.utils.AnimacaoUtils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Slide-up card with the filter and sort choices of the fixed income / Treasury lists.
 * The host activity must implement {@link Listener}; the choices are passed as a Serializable.
 */
public class FiltrosRendaFixaBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG_FRAGMENT = "FiltrosRendaFixaBottomSheet";
    private static final String ARG_FILTRO = "filtro_atual";
    private static final String ARG_TIPOS = "tipos_disponiveis";
    private static final String ARG_INDEXADORES = "indexadores_disponiveis";

    private static final int COR_FUNDO_NORMAL = 0xFF0B1729;
    private static final int COR_FUNDO_SELECIONADO = 0xFF2563EB;
    private static final int COR_BORDA_NORMAL = 0xFF293950;
    private static final int COR_BORDA_SELECIONADO = 0xFF3989FF;
    private static final int COR_TEXTO_NORMAL = 0xFFD1DCEB;

    /** Implemented by the activity that owns the list. */
    public interface Listener {
        void aoAplicarFiltros(FiltroRendaFixa filtro);
    }

    public static FiltrosRendaFixaBottomSheet nova(FiltroRendaFixa atual, List<String> tipos,
                                                   List<String> indexadores) {
        FiltrosRendaFixaBottomSheet sheet = new FiltrosRendaFixaBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_FILTRO, atual);
        args.putStringArrayList(ARG_TIPOS, new ArrayList<>(tipos));
        args.putStringArrayList(ARG_INDEXADORES, new ArrayList<>(indexadores));
        sheet.setArguments(args);
        return sheet;
    }

    private ChipGroup chipsTipo, chipsIndexador, chipsCaracteristicas, chipsOrdem;
    private Chip chipFgc, chipIsentoIR;
    private final List<String> tipos = new ArrayList<>();
    private final List<String> indexadores = new ArrayList<>();

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
        return inflater.inflate(R.layout.sheet_filtros_renda_fixa, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        FiltroRendaFixa atual = new FiltroRendaFixa();
        if (args != null) {
            Object recebido = args.getSerializable(ARG_FILTRO);
            if (recebido instanceof FiltroRendaFixa) {
                atual = (FiltroRendaFixa) recebido;
            }
            List<String> t = args.getStringArrayList(ARG_TIPOS);
            if (t != null) tipos.addAll(t);
            List<String> i = args.getStringArrayList(ARG_INDEXADORES);
            if (i != null) indexadores.addAll(i);
        }

        chipsTipo = view.findViewById(R.id.chipsTipo);
        chipsIndexador = view.findViewById(R.id.chipsIndexador);
        chipsCaracteristicas = view.findViewById(R.id.chipsCaracteristicas);
        chipsOrdem = view.findViewById(R.id.chipsOrdem);

        // Single-choice groups: the first chip means "all".
        preencherGrupo(chipsTipo, "Todos", tipos, atual.tipo);
        preencherGrupo(chipsIndexador, "Todos", indexadores, atual.indexador);

        chipFgc = novoChip("Garantia FGC");
        chipFgc.setChecked(atual.somenteFgc);
        chipIsentoIR = novoChip("Isento de IR");
        chipIsentoIR.setChecked(atual.somenteIsentoIR);
        chipsCaracteristicas.addView(chipFgc);
        chipsCaracteristicas.addView(chipIsentoIR);

        for (FiltroRendaFixa.Ordem ordem : FiltroRendaFixa.Ordem.values()) {
            Chip chip = novoChip(ordem.getRotulo());
            chip.setTag(ordem);
            chip.setChecked(ordem == atual.ordem);
            chipsOrdem.addView(chip);
        }

        view.findViewById(R.id.btnLimparFiltrosRf).setOnClickListener(v -> {
            definirFiltro(new FiltroRendaFixa());
        });
        view.findViewById(R.id.btnAplicarFiltrosRf).setOnClickListener(v -> aplicar());

        AnimacaoUtils.efeitoPressionar(view.findViewById(R.id.btnLimparFiltrosRf));
        AnimacaoUtils.efeitoPressionar(view.findViewById(R.id.btnAplicarFiltrosRf));
        AnimacaoUtils.entradaEmCascata(chipsTipo, chipsIndexador, chipsCaracteristicas, chipsOrdem);
    }

    private void preencherGrupo(ChipGroup grupo, String rotuloTodos, List<String> valores,
                                @Nullable String selecionado) {
        grupo.removeAllViews();
        Chip todos = novoChip(rotuloTodos);
        todos.setTag(null);
        grupo.addView(todos);
        boolean achou = false;
        for (String valor : valores) {
            Chip chip = novoChip(valor);
            chip.setTag(valor);
            chip.setChecked(valor.equalsIgnoreCase(selecionado));
            achou = achou || chip.isChecked();
            grupo.addView(chip);
        }
        todos.setChecked(!achou);
    }

    private Chip novoChip(String texto) {
        Chip chip = new Chip(requireContext());
        chip.setId(View.generateViewId());
        chip.setText(texto);
        chip.setCheckable(true);
        chip.setCheckedIconVisible(false);
        chip.setTextSize(12f);
        chip.setEnsureMinTouchTargetSize(false);
        int[][] estados = {{android.R.attr.state_checked}, {}};
        chip.setChipBackgroundColor(new ColorStateList(estados,
                new int[]{COR_FUNDO_SELECIONADO, COR_FUNDO_NORMAL}));
        chip.setChipStrokeColor(new ColorStateList(estados,
                new int[]{COR_BORDA_SELECIONADO, COR_BORDA_NORMAL}));
        chip.setChipStrokeWidth(getResources().getDisplayMetrics().density);
        chip.setTextColor(new ColorStateList(estados, new int[]{Color.WHITE, COR_TEXTO_NORMAL}));
        return chip;
    }

    private void definirFiltro(FiltroRendaFixa filtro) {
        selecionarPorTag(chipsTipo, filtro.tipo);
        selecionarPorTag(chipsIndexador, filtro.indexador);
        chipFgc.setChecked(filtro.somenteFgc);
        chipIsentoIR.setChecked(filtro.somenteIsentoIR);
        for (int i = 0; i < chipsOrdem.getChildCount(); i++) {
            Chip chip = (Chip) chipsOrdem.getChildAt(i);
            chip.setChecked(chip.getTag() == filtro.ordem);
        }
    }

    private void selecionarPorTag(ChipGroup grupo, @Nullable String valor) {
        for (int i = 0; i < grupo.getChildCount(); i++) {
            Chip chip = (Chip) grupo.getChildAt(i);
            Object tag = chip.getTag();
            chip.setChecked(valor == null ? tag == null : valor.equals(tag));
        }
    }

    @Nullable
    private String valorSelecionado(ChipGroup grupo) {
        int id = grupo.getCheckedChipId();
        if (id == View.NO_ID) return null;
        Chip chip = grupo.findViewById(id);
        return chip == null || chip.getTag() == null ? null : chip.getTag().toString();
    }

    private void aplicar() {
        FiltroRendaFixa filtro = new FiltroRendaFixa();
        filtro.tipo = valorSelecionado(chipsTipo);
        filtro.indexador = valorSelecionado(chipsIndexador);
        filtro.somenteFgc = chipFgc.isChecked();
        filtro.somenteIsentoIR = chipIsentoIR.isChecked();
        int idOrdem = chipsOrdem.getCheckedChipId();
        if (idOrdem != View.NO_ID) {
            Chip chip = chipsOrdem.findViewById(idOrdem);
            if (chip != null && chip.getTag() instanceof FiltroRendaFixa.Ordem) {
                filtro.ordem = (FiltroRendaFixa.Ordem) chip.getTag();
            }
        }
        if (getActivity() instanceof Listener) {
            ((Listener) getActivity()).aoAplicarFiltros(filtro);
        }
        dismissAllowingStateLoss();
    }
}
