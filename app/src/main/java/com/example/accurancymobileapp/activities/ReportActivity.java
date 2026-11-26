package com.example.accurancymobileapp.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.network.client.RetrofitClient;
import com.example.accurancymobileapp.network.service.ApiService;
import com.example.accurancymobileapp.response.ReportResponse;
import com.example.accurancymobileapp.utils.Aviso;
import com.example.accurancymobileapp.utils.CallbackDaTela;
import com.example.accurancymobileapp.utils.PuxarParaAtualizar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;

/** Account report: numbers of the period, notes, the assistant's analysis and a summary to share. */
public class ReportActivity extends AppCompatActivity {

    private static final int[] PERIODS = {7, 30, 90, 365};
    private static final String[] PERIOD_LABELS = {"7 dias", "30 dias", "90 dias", "12 meses"};
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private final NumberFormat currency = NumberFormat.getCurrencyInstance(PT_BR);

    private ApiService api;
    private Call<ReportResponse> call;
    private PuxarParaAtualizar pullToRefresh;
    private LayoutInflater inflater;
    private ReportResponse report;
    private int days = 30;

    private View content, state, progress, shareButton, studyTitle;
    private TextView tvState, tvPeriod, tvDisclaimer;
    private MaterialButton retryButton;
    private GridLayout numbersGrid;
    private LinearLayout notesList, diagnosisList, paragraphsList, studyList,
            distributionList, transactionsList, walletsList, highlightsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_report);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.reportRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        api = RetrofitClient.getApiService(this);
        inflater = LayoutInflater.from(this);
        content = findViewById(R.id.reportContent);
        state = findViewById(R.id.reportState);
        progress = findViewById(R.id.reportProgress);
        tvState = findViewById(R.id.tvReportState);
        retryButton = findViewById(R.id.btnReportRetry);
        tvPeriod = findViewById(R.id.tvReportPeriod);
        tvDisclaimer = findViewById(R.id.tvReportDisclaimer);
        shareButton = findViewById(R.id.btnReportShare);
        numbersGrid = findViewById(R.id.reportNumbersGrid);
        notesList = findViewById(R.id.reportNotesList);
        diagnosisList = findViewById(R.id.reportDiagnosisList);
        paragraphsList = findViewById(R.id.reportParagraphsList);
        studyTitle = findViewById(R.id.tvReportStudyTitle);
        studyList = findViewById(R.id.reportStudyList);
        distributionList = findViewById(R.id.reportDistributionList);
        transactionsList = findViewById(R.id.reportTransactionsList);
        walletsList = findViewById(R.id.reportWalletsList);
        highlightsList = findViewById(R.id.reportHighlightsList);

        SwipeRefreshLayout swipe = findViewById(R.id.reportSwipe);
        pullToRefresh = new PuxarParaAtualizar(swipe, this::load);

        findViewById(R.id.btnReportBack).setOnClickListener(v -> finish());
        shareButton.setOnClickListener(v -> share());
        retryButton.setOnClickListener(v -> load());
        setUpPeriods();

        load();
    }

    @Override
    protected void onDestroy() {
        if (call != null) call.cancel();
        super.onDestroy();
    }

    private void setUpPeriods() {
        ChipGroup group = findViewById(R.id.reportPeriodGroup);
        for (int i = 0; i < PERIODS.length; i++) {
            Chip chip = (Chip) inflater.inflate(R.layout.item_chip_filtro, group, false);
            chip.setId(View.generateViewId());
            chip.setText(PERIOD_LABELS[i]);
            chip.setCheckable(true);
            chip.setChecked(PERIODS[i] == days);
            final int period = PERIODS[i];
            chip.setOnClickListener(v -> {
                if (days == period) return;
                days = period;
                load();
            });
            group.addView(chip);
        }
    }

    private void load() {
        if (call != null) call.cancel();
        if (report == null) showState(true, "Montando o relatório...", false);
        pullToRefresh.iniciar();

        call = api.report(days);
        call.enqueue(new CallbackDaTela<ReportResponse>(this) {
            @Override
            public void aoSucesso(@NonNull ReportResponse response) {
                pullToRefresh.concluir();
                report = response;
                show(response);
            }

            @Override
            public void aoErro(@NonNull String message) {
                pullToRefresh.concluir();
                if (report == null) {
                    showState(false, message, true);
                } else {
                    Aviso.makeText(ReportActivity.this, message, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void showState(boolean loading, String text, boolean withRetry) {
        content.setVisibility(View.GONE);
        state.setVisibility(View.VISIBLE);
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        tvState.setText(text);
        retryButton.setVisibility(withRetry ? View.VISIBLE : View.GONE);
    }

    private void show(ReportResponse r) {
        state.setVisibility(View.GONE);
        content.setVisibility(View.VISIBLE);
        shareButton.setEnabled(true);

        if (r.period != null) {
            tvPeriod.setText("De " + date(r.period.start) + " a " + date(r.period.end) + " · valores atualizados agora.");
        }
        showNumbers(r);
        showNotes(r.getNotes());
        showAnalysis(r.analysis);
        showDistribution(r.getDistribution());
        showTransactions(r.transactions);
        showWallets(r.getWallets());
        showHighlights(r.highlights);
        tvDisclaimer.setText(r.disclaimer == null ? "" : r.disclaimer);
    }

    private void showNumbers(ReportResponse r) {
        numbersGrid.removeAllViews();
        ReportResponse.Real real = r.real != null ? r.real : new ReportResponse.Real();
        ReportResponse.Simulated simulated = r.simulated != null ? r.simulated : new ReportResponse.Simulated();

        addNumber("Patrimônio real", currency.format(real.netWorth),
                currency.format(r.availableBalance) + " de saldo disponível", 0);
        addNumber("Investido nas carteiras reais", currency.format(real.invested),
                "Valor atual: " + currency.format(real.marketValue), 0);
        addNumber("Resultado dos ativos reais", currency.format(real.result),
                real.invested > 0 ? percent(real.returnPercent) + " sobre o valor aplicado" : "Ainda sem ativos reais",
                signColor(real.result));
        addNumber("Carteiras simuladas", simulated.walletCount > 0 ? currency.format(simulated.netWorth) : "—",
                simulated.walletCount > 0 ? percent(simulated.returnPercent) + " · dinheiro fictício" : "Nenhuma carteira simulada",
                0);
    }

    private void addNumber(String label, String value, String detail, @ColorRes int color) {
        View card = inflater.inflate(R.layout.item_report_number, numbersGrid, false);
        ((TextView) card.findViewById(R.id.tvReportNumberLabel)).setText(label);
        TextView tvValue = card.findViewById(R.id.tvReportNumberValue);
        tvValue.setText(value);
        if (color != 0) tvValue.setTextColor(ContextCompat.getColor(this, color));
        ((TextView) card.findViewById(R.id.tvReportNumberDetail)).setText(detail);

        int margin = dp(4);
        // FILL keeps both cards of a row at the same height.
        GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL), GridLayout.spec(GridLayout.UNDEFINED, 1f));
        params.width = 0;
        params.setMargins(margin, margin, margin, margin);
        numbersGrid.addView(card, params);
    }

    private void showNotes(List<ReportResponse.Note> notes) {
        notesList.removeAllViews();
        for (ReportResponse.Note note : notes) {
            int color = R.color.blue_primary;
            int icon = R.drawable.ic_tendencia;
            if ("atencao".equals(note.type)) {
                color = R.color.atencao_destaque;
                icon = R.drawable.ic_bandeira;
            } else if ("positivo".equals(note.type)) {
                color = R.color.positivo_destaque;
            } else if ("inicio".equals(note.type)) {
                icon = R.drawable.ic_ia;
            } else if ("info".equals(note.type)) {
                icon = R.drawable.ic_ajuda;
            }
            notesList.addView(noteRow(notesList, icon, color, note.text));
        }
    }

    private View noteRow(ViewGroup parent, @DrawableRes int icon, @ColorRes int color, CharSequence text) {
        View row = inflater.inflate(R.layout.item_report_note, parent, false);
        ImageView image = row.findViewById(R.id.imgReportNote);
        image.setImageResource(icon);
        image.setColorFilter(ContextCompat.getColor(this, color));
        ((TextView) row.findViewById(R.id.tvReportNote)).setText(text);
        return row;
    }

    private void showAnalysis(@Nullable ReportResponse.Analysis analysis) {
        diagnosisList.removeAllViews();
        paragraphsList.removeAllViews();
        studyList.removeAllViews();
        if (analysis == null) {
            paragraphsList.addView(plainText("A análise não está disponível agora. Puxe a tela para atualizar."));
            studyTitle.setVisibility(View.GONE);
            studyList.setVisibility(View.GONE);
            return;
        }

        ReportResponse.Diagnosis diagnosis = analysis.diagnosis;
        if (diagnosis != null && diagnosis.diversification != null && diagnosis.risk != null) {
            ReportResponse.Diversification diversification = diagnosis.diversification;
            addDiagnosisRow("Diversificação", diversification.score == null ? diversification.label
                    : diversification.score + " de 100 · " + diversification.label);
            addDiagnosisRow("Perfil de risco", diagnosis.risk.label);
            addDiagnosisRow("Saldo sem aplicação", diagnosis.idleCashPercent == null ? "—"
                    : unsignedPercent(diagnosis.idleCashPercent) + " do patrimônio real");
        }

        for (String paragraph : analysis.getParagraphs()) {
            TextView tv = plainText(paragraph);
            tv.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            tv.setTextSize(14f);
            tv.setLineSpacing(0f, 1.2f);
            tv.setPadding(0, dp(6), 0, dp(6));
            paragraphsList.addView(tv);
        }

        List<ReportResponse.StudyTrail> study = analysis.getStudy();
        studyTitle.setVisibility(study.isEmpty() ? View.GONE : View.VISIBLE);
        studyList.setVisibility(study.isEmpty() ? View.GONE : View.VISIBLE);
        for (ReportResponse.StudyTrail trail : study) {
            SpannableStringBuilder text = new SpannableStringBuilder("Trilha " + trail.trail);
            text.setSpan(new StyleSpan(Typeface.BOLD), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.append(" · ").append(String.valueOf(trail.completed)).append(" de ")
                    .append(String.valueOf(trail.total)).append(" aulas\n").append(trail.reason);
            studyList.addView(noteRow(studyList, R.drawable.ic_study, R.color.blue_primary, text));
        }
    }

    private void addDiagnosisRow(String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(4), 0, dp(4));

        TextView tvLabel = plainText(label);
        TextView tvValue = plainText(value);
        tvValue.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        tvValue.setTypeface(Typeface.DEFAULT_BOLD);
        tvValue.setGravity(Gravity.END);

        row.addView(tvLabel, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.9f));
        row.addView(tvValue, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.1f));
        diagnosisList.addView(row);
    }

    private void showDistribution(List<ReportResponse.CategoryShare> shares) {
        distributionList.removeAllViews();
        if (shares.isEmpty()) {
            distributionList.addView(plainText("Sem ativos nas carteiras reais."));
            return;
        }
        for (ReportResponse.CategoryShare share : shares) {
            View row = inflater.inflate(R.layout.item_report_bar, distributionList, false);
            ((TextView) row.findViewById(R.id.tvReportBarCategory)).setText(share.category);
            ((TextView) row.findViewById(R.id.tvReportBarValue)).setText(
                    unsignedPercent(share.percent) + " · " + currency.format(share.value));
            LinearProgressIndicator bar = row.findViewById(R.id.reportCategoryBar);
            bar.setProgressCompat((int) Math.round(Math.max(2, Math.min(100, share.percent))), true);
            distributionList.addView(row);
        }
    }

    private void showTransactions(@Nullable ReportResponse.Transactions t) {
        transactionsList.removeAllViews();
        if (t == null || t.real == null || t.simulated == null) return;
        addTransactionRow("", "Reais", "Simuladas", true);
        addTransactionRow("Compras", t.real.purchases + " · " + currency.format(t.real.purchasedValue),
                t.simulated.purchases + " · " + currency.format(t.simulated.purchasedValue), false);
        addTransactionRow("Vendas", t.real.sales + " · " + currency.format(t.real.soldValue),
                t.simulated.sales + " · " + currency.format(t.simulated.soldValue), false);
        addTransactionRow("Dividendos", t.real.dividends + " · " + currency.format(t.real.dividendValue),
                t.simulated.dividends + " · " + currency.format(t.simulated.dividendValue), false);
        addTransactionRow("Depósitos", t.deposits + " · " + currency.format(t.depositValue), "", false);
    }

    private void addTransactionRow(String label, String real, String simulated, boolean header) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(5), 0, dp(5));

        String[] texts = {label, real, simulated};
        float[] weights = {0.8f, 1.1f, 1.1f};
        for (int i = 0; i < texts.length; i++) {
            TextView tv = new TextView(this);
            tv.setText(texts[i]);
            tv.setTextSize(header ? 11.5f : 13f);
            tv.setTextColor(ContextCompat.getColor(this, header ? R.color.texto_fraco
                    : i == 0 ? R.color.text_secondary : R.color.text_primary));
            tv.setGravity(i == 0 ? Gravity.START : Gravity.END);
            row.addView(tv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, weights[i]));
        }
        transactionsList.addView(row);
    }

    private void showWallets(List<ReportResponse.Wallet> wallets) {
        walletsList.removeAllViews();
        if (wallets.isEmpty()) {
            walletsList.addView(plainText("Você ainda não tem carteiras."));
            return;
        }
        for (ReportResponse.Wallet wallet : wallets) {
            View card = inflater.inflate(R.layout.item_report_wallet, walletsList, false);
            ((TextView) card.findViewById(R.id.tvReportWalletName)).setText(wallet.name);
            ((TextView) card.findViewById(R.id.tvReportWalletType)).setText(wallet.isSimulated() ? "Simulada" : "Real");
            ((TextView) card.findViewById(R.id.tvReportWalletNetWorth)).setText(currency.format(wallet.netWorth));

            TextView result = card.findViewById(R.id.tvReportWalletResult);
            result.setText(currency.format(wallet.result) + " (" + percent(wallet.returnPercent) + ")");
            int color = signColor(wallet.result);
            result.setTextColor(ContextCompat.getColor(this, color != 0 ? color : R.color.text_secondary));

            String goal = wallet.goal <= 0 ? "Sem meta"
                    : wallet.goalReached ? "Meta atingida (" + currency.format(wallet.goal) + ")"
                    : Math.round(wallet.goalProgress == null ? 0 : wallet.goalProgress) + "% da meta de " + currency.format(wallet.goal);
            ((TextView) card.findViewById(R.id.tvReportWalletDetail)).setText(
                    "Investido " + currency.format(wallet.invested) + " · " + goal);
            walletsList.addView(card);
        }
    }

    private void showHighlights(@Nullable ReportResponse.Highlights highlights) {
        highlightsList.removeAllViews();
        List<ReportResponse.Asset> best = highlights == null ? Collections.emptyList() : highlights.getBest();
        List<ReportResponse.Asset> worst = highlights == null ? Collections.emptyList() : highlights.getWorst();

        if (best.isEmpty() && worst.isEmpty()) {
            highlightsList.addView(plainText("Nenhum ativo subiu ou caiu desde a compra."));
            return;
        }
        for (ReportResponse.Asset asset : best) highlightsList.addView(assetRow(asset));
        for (ReportResponse.Asset asset : worst) highlightsList.addView(assetRow(asset));
    }

    private View assetRow(ReportResponse.Asset asset) {
        View row = inflater.inflate(R.layout.item_report_asset, highlightsList, false);
        ((TextView) row.findViewById(R.id.tvReportAssetSymbol)).setText(asset.symbol);
        ((TextView) row.findViewById(R.id.tvReportAssetWallet)).setText(
                asset.wallet + " · " + (asset.simulated ? "simulada" : "real"));
        TextView change = row.findViewById(R.id.tvReportAssetChange);
        boolean up = asset.returnPercent >= 0;
        change.setText(percent(asset.returnPercent));
        change.setBackgroundResource(up ? R.drawable.bg_tag_positivo : R.drawable.bg_tag_negativo);
        change.setTextColor(ContextCompat.getColor(this, up ? R.color.positivo_destaque : R.color.negativo_destaque));
        return row;
    }

    private void share() {
        if (report == null || report.real == null) return;
        ReportResponse r = report;
        StringBuilder text = new StringBuilder()
                .append("Meu relatório no Accuracy (").append(periodLabel()).append(")\n\n")
                .append("Patrimônio real: ").append(currency.format(r.real.netWorth)).append('\n')
                .append("Investido: ").append(currency.format(r.real.invested))
                .append(" · resultado ").append(currency.format(r.real.result))
                .append(" (").append(percent(r.real.returnPercent)).append(")\n");
        if (r.simulated != null && r.simulated.walletCount > 0) {
            text.append("Simulações: ").append(currency.format(r.simulated.netWorth))
                    .append(" (").append(percent(r.simulated.returnPercent)).append(")\n");
        }
        text.append('\n');
        for (ReportResponse.Note note : r.getNotes()) text.append("• ").append(note.text).append('\n');
        if (r.disclaimer != null) text.append('\n').append(r.disclaimer);

        Intent intent = new Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_SUBJECT, "Relatório Accuracy")
                .putExtra(Intent.EXTRA_TEXT, text.toString());
        startActivity(Intent.createChooser(intent, "Compartilhar resumo"));
    }

    private String periodLabel() {
        for (int i = 0; i < PERIODS.length; i++) {
            if (PERIODS[i] == days) return "últimos " + PERIOD_LABELS[i];
        }
        return "últimos " + days + " dias";
    }

    private TextView plainText(CharSequence text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(13f);
        tv.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        return tv;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    /** "2026-11-26" -> "26/11/2026". */
    private static String date(@Nullable String iso) {
        if (iso == null || !iso.matches("\\d{4}-\\d{2}-\\d{2}")) return "";
        String[] parts = iso.split("-");
        return parts[2] + "/" + parts[1] + "/" + parts[0];
    }

    @ColorRes
    private static int signColor(double value) {
        if (value > 0) return R.color.positivo_destaque;
        if (value < 0) return R.color.negativo_destaque;
        return 0;
    }

    /** +3,15% / -0,12% / 0% */
    private static String percent(double value) {
        String sign = value > 0.004 ? "+" : value < -0.004 ? "-" : "";
        return sign + unsignedPercent(Math.abs(value));
    }

    private static String unsignedPercent(double value) {
        String text = String.format(PT_BR, "%.2f", value);
        text = text.replaceAll("0+$", "").replaceAll(",$", "");
        return text + "%";
    }
}
