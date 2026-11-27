package com.example.accurancymobileapp.utils;

import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.adapter.SvgLogoLoader;

import java.util.Locale;
import java.util.regex.Pattern;

/** Asset logo from brapi for B3 tickers; the symbol initials stay when there is no logo. */
public final class AssetLogo {

    private static final Pattern B3_TICKER = Pattern.compile("^[A-Z]{4}[0-9]{1,2}[A-Z]?$");
    private static final String ICON_URL = "https://icons.brapi.dev/icons/%s.svg";

    private AssetLogo() { }

    @Nullable
    public static String url(@Nullable String symbol) {
        if (symbol == null) return null;
        String ticker = symbol.trim().toUpperCase(Locale.ROOT);
        return B3_TICKER.matcher(ticker).matches() ? String.format(Locale.ROOT, ICON_URL, ticker) : null;
    }

    public static void show(@NonNull ImageView imageView, @Nullable String symbol) {
        SiglaIcone.aplicarSeSimbolo(imageView, symbol, R.drawable.ic_ativo_placeholder);
        SvgLogoLoader.carregar(url(symbol), imageView);
    }
}
