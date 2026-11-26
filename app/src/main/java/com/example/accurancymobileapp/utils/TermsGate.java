package com.example.accurancymobileapp.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.activities.LoginActivity;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.response.RespostaSimples;
import com.example.accurancymobileapp.response.TermsStatusResponse;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Asks the signed-in user to accept the current Terms of Use and Privacy Policy. */
public final class TermsGate {

    public static final String TERMS_URL = "https://accuracy.site.je/terms.php";
    public static final String PRIVACY_URL = "https://accuracy.site.je/privacy.php";

    private TermsGate() { }

    /** Runs onReady once the terms are accepted, or right away when there is nothing to accept or no answer. */
    public static void check(@NonNull AppCompatActivity activity, @NonNull Runnable onReady) {
        RetrofitClient.getApiService(activity).termsStatus().enqueue(new Callback<TermsStatusResponse>() {
            @Override
            public void onResponse(@NonNull Call<TermsStatusResponse> call, @NonNull Response<TermsStatusResponse> response) {
                if (activity.isFinishing() || activity.isDestroyed()) return;
                TermsStatusResponse status = response.body();
                if (response.isSuccessful() && status != null && status.ok() && status.pending) {
                    ask(activity, onReady);
                } else {
                    onReady.run();
                }
            }

            @Override
            public void onFailure(@NonNull Call<TermsStatusResponse> call, @NonNull Throwable t) {
                if (!activity.isFinishing() && !activity.isDestroyed()) onReady.run();
            }
        });
    }

    public static void open(@NonNull Context context, @NonNull String url) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ActivityNotFoundException e) {
            Aviso.makeText(context, "Nenhum navegador encontrado para abrir o link.", Toast.LENGTH_LONG).show();
        }
    }

    private static void ask(@NonNull AppCompatActivity activity, @NonNull Runnable onReady) {
        View content = LayoutInflater.from(activity).inflate(R.layout.dialog_terms, null, false);
        CheckBox checkBox = content.findViewById(R.id.checkTermsAccept);
        TextView error = content.findViewById(R.id.tvTermsError);
        content.findViewById(R.id.btnTermsRead).setOnClickListener(v -> open(activity, TERMS_URL));
        content.findViewById(R.id.btnPrivacyRead).setOnClickListener(v -> open(activity, PRIVACY_URL));

        AlertDialog dialog = new MaterialAlertDialogBuilder(activity)
                .setTitle("Termos de Uso e Privacidade")
                .setView(content)
                .setCancelable(false)
                .setNegativeButton("Sair da conta", null)
                .setPositiveButton("Aceitar", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button accept = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            accept.setEnabled(false);
            checkBox.setOnCheckedChangeListener((button, checked) -> accept.setEnabled(checked));

            accept.setOnClickListener(v -> {
                accept.setEnabled(false);
                error.setVisibility(View.GONE);
                RetrofitClient.getApiService(activity).acceptTerms().enqueue(new Callback<RespostaSimples>() {
                    @Override
                    public void onResponse(@NonNull Call<RespostaSimples> call, @NonNull Response<RespostaSimples> response) {
                        if (activity.isFinishing() || activity.isDestroyed()) return;
                        RespostaSimples result = response.body();
                        if (response.isSuccessful() && result != null && result.ok()) {
                            dialog.dismiss();
                            onReady.run();
                            return;
                        }
                        showError(result != null ? result.texto(null) : null);
                    }

                    @Override
                    public void onFailure(@NonNull Call<RespostaSimples> call, @NonNull Throwable t) {
                        if (!activity.isFinishing() && !activity.isDestroyed()) showError(null);
                    }

                    private void showError(String message) {
                        error.setText(message != null ? message : "Não foi possível registrar o aceite. Tente de novo.");
                        error.setVisibility(View.VISIBLE);
                        accept.setEnabled(checkBox.isChecked());
                    }
                });
            });

            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> {
                dialog.dismiss();
                signOut(activity);
            });
        });
        dialog.show();
    }

    private static void signOut(@NonNull AppCompatActivity activity) {
        SessionManager session = new SessionManager(activity);
        String refreshToken = session.getRefreshToken();
        if (refreshToken != null && !refreshToken.isEmpty()) {
            Map<String, String> body = new HashMap<>();
            body.put("refresh_token", refreshToken);
            RetrofitClient.getApiService(activity.getApplicationContext()).sair(body).enqueue(new Callback<RespostaSimples>() {
                @Override
                public void onResponse(@NonNull Call<RespostaSimples> call, @NonNull Response<RespostaSimples> response) { }

                @Override
                public void onFailure(@NonNull Call<RespostaSimples> call, @NonNull Throwable t) { }
            });
        }
        session.logout();
        activity.startActivity(new Intent(activity, LoginActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }
}
