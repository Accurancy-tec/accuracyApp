package com.example.accurancymobileapp.fragments;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.accurancymobileapp.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * Base dos cartões que sobem da parte de baixo da tela: abrem já expandidos e deixam
 * o fundo arredondado do layout aparecer (a janela do sistema fica transparente).
 */
public abstract class CartaoDeslizante extends BottomSheetDialogFragment {

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
}
