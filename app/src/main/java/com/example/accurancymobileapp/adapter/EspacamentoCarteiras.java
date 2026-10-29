package com.example.accurancymobileapp.adapter;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class EspacamentoCarteiras extends RecyclerView.ItemDecoration {
    private final int espacamentoHorizontal;
    private final int espacamentoVertical;

    public EspacamentoCarteiras(
            int espacamentoHorizontal,
            int espacamentoVertical
    ) {
        this.espacamentoHorizontal = espacamentoHorizontal;
        this.espacamentoVertical = espacamentoVertical;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);

        if (position == RecyclerView.NO_POSITION) {
            return;
        }

        int coluna = position % 2;

        // Espaçamento horizontal entre as duas colunas
        if (coluna == 0) {
            outRect.left = 0;
            outRect.right = espacamentoHorizontal / 2;
        } else {
            outRect.left = espacamentoHorizontal / 2;
            outRect.right = 0;
        }

        // Espaçamento vertical entre as linhas
        outRect.top = espacamentoVertical / 2;
        outRect.bottom = espacamentoVertical / 2;
    }
}
