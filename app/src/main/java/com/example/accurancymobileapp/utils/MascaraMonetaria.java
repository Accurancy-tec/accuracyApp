
package com.example.accurancymobileapp.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class MascaraMonetaria implements TextWatcher {

    private final EditText editText;
    private final NumberFormat formato;
    private boolean editando = false;

    public MascaraMonetaria(EditText editText) {
        this.editText = editText;

        formato = NumberFormat.getCurrencyInstance(
                new Locale("pt", "BR")
        );
    }

    @Override
    public void beforeTextChanged(
            CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(
            CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {

        if (editando) return;

        editando = true;

        String valorDigitado = s.toString();

        // Mantém somente os números
        String numeros = valorDigitado.replaceAll("[^0-9]", "");

        if (numeros.isEmpty()) {
            numeros = "0";
        }

        // Converte os centavos para reais
        BigDecimal valor = new BigDecimal(numeros)
                .movePointLeft(2);

        // Formata como moeda brasileira
        String valorFormatado = formato.format(valor);

        editText.setText(valorFormatado);

        // Mantém o cursor no final
        editText.setSelection(valorFormatado.length());

        editando = false;
    }

    // Recupera o valor numérico sem a formatação
    public static BigDecimal obterValor(EditText editText) {

        String numeros = editText.getText()
                .toString()
                .replaceAll("[^0-9]", "");

        if (numeros.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return new BigDecimal(numeros).movePointLeft(2);
    }
}
