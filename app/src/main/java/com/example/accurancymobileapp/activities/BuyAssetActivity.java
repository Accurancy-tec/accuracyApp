package com.example.accurancymobileapp.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.accurancymobileapp.R;
import com.example.accurancymobileapp.model.Wallet;
import com.example.accurancymobileapp.model.clsAportes;
import com.example.accurancymobileapp.network.repository.AporteRepository;
import com.example.accurancymobileapp.network.repository.WalletRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class BuyAssetActivity extends AppCompatActivity {

    public static final String EXTRA_TIPO_OPERACAO = "tipo_operacao";

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(PT_BR);
    private static final NumberFormat QUANTITY_FORMAT = NumberFormat.getIntegerInstance(PT_BR);

    private static final BigDecimal ESTIMATED_FEE_RATE = new BigDecimal("0.0003");
    private static final int MAX_QUANTITY = 1_000_000;

    private static final String TYPE_BUY = "Compra";
    private static final String TYPE_SELL = "Venda";
    private static final String DEFAULT_SIMULATION_NOTICE = "Operação simulada para fins educacionais. Nenhuma ordem será enviada à B3.";

    private static final int COLOR_POSITIVE = Color.rgb(43, 224, 163);
    private static final int COLOR_NEGATIVE = Color.rgb(255, 82, 105);
    private static final int COLOR_NEUTRAL = Color.WHITE;

    private View buyAssetRoot;

    private ImageButton btnBackBuyAsset;

    private EditText edtBuyQuantity;

    private Button btnSubmitBuyOrder;

    private TextView tvBuyAssetTitle, tvBuyAssetName, tvBuyQuoteUpdatedAt;
    private TextView tvBuyLastPrice, tvBuyDailyChange, tvBuyAvailableBalance;
    private TextView tvBuyQuantityDescription, btnDecreaseBuyQuantity, btnIncreaseBuyQuantity;
    private TextView tvBuyOrderQuantity, tvBuyUnitPrice;
    private TextView tvBuyAssetsSubtotalLabel, tvBuyAssetsSubtotal;
    private TextView tvBuyEstimatedFees, tvBuyEstimatedTotalLabel, tvBuyEstimatedTotal;
    private TextView tvBuySimulationNotice;

    private String ticker;
    private String assetName;
    private String assetCategory;
    private String operationType = TYPE_BUY;

    private int walletId = -1;
    private double availableBalance = -1.0;
    private double availableAssetQuantity;
    private double currentPrice = Double.NaN;
    private double dailyChangePercent = Double.NaN;

    private boolean operationInProgress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_asset);

        bindViews();
        configureSystemBars();
        readIntentData();

        if (ticker == null || ticker.trim().isEmpty()) {
            Toast.makeText(this, "Não foi informado qual ativo operar.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        displayAssetData();
        configureNavigation();
        configureQuantityControls();
        refreshOrderPreview();
    }

    private void bindViews() {
        buyAssetRoot = findViewById(R.id.buyAssetRoot);
        btnBackBuyAsset = findViewById(R.id.btnBackBuyAsset);
        tvBuyAssetTitle = findViewById(R.id.tvBuyAssetTitle);
        tvBuyAssetName = findViewById(R.id.tvBuyAssetName);
        tvBuyQuoteUpdatedAt = findViewById(R.id.tvBuyQuoteUpdatedAt);
        tvBuyLastPrice = findViewById(R.id.tvBuyLastPrice);
        tvBuyDailyChange = findViewById(R.id.tvBuyDailyChange);
        tvBuyAvailableBalance = findViewById(R.id.tvBuyAvailableBalance);
        tvBuyQuantityDescription = findViewById(R.id.tvBuyQuantityDescription);
        edtBuyQuantity = findViewById(R.id.edtBuyQuantity);
        btnDecreaseBuyQuantity = findViewById(R.id.btnDecreaseBuyQuantity);
        btnIncreaseBuyQuantity = findViewById(R.id.btnIncreaseBuyQuantity);
        tvBuyOrderQuantity = findViewById(R.id.tvBuyOrderQuantity);
        tvBuyUnitPrice = findViewById(R.id.tvBuyUnitPrice);
        tvBuyAssetsSubtotalLabel = findViewById(R.id.tvBuyAssetsSubtotalLabel);
        tvBuyAssetsSubtotal = findViewById(R.id.tvBuyAssetsSubtotal);
        tvBuyEstimatedFees = findViewById(R.id.tvBuyEstimatedFees);
        tvBuyEstimatedTotalLabel = findViewById(R.id.tvBuyEstimatedTotalLabel);
        tvBuyEstimatedTotal = findViewById(R.id.tvBuyEstimatedTotal);
        tvBuySimulationNotice = findViewById(R.id.tvBuySimulationNotice);
        btnSubmitBuyOrder = findViewById(R.id.btnSubmitBuyOrder);
    }

    private void configureSystemBars() {
        Window window = getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(window, window.getDecorView());
        controller.setAppearanceLightStatusBars(false);
        controller.setAppearanceLightNavigationBars(false);

        final int initialLeft = buyAssetRoot.getPaddingLeft();
        final int initialTop = buyAssetRoot.getPaddingTop();
        final int initialRight = buyAssetRoot.getPaddingRight();
        final int initialBottom = buyAssetRoot.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(buyAssetRoot, (view, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(
                    initialLeft + systemBars.left,
                    initialTop + systemBars.top,
                    initialRight + systemBars.right,
                    initialBottom + systemBars.bottom
            );
            return windowInsets;
        });
    }

    private void readIntentData() {
        ticker = getIntent().getStringExtra(DetalhesAtivoActivity.EXTRA_TICKER);
        assetName = getIntent().getStringExtra(DetalhesAtivoActivity.EXTRA_NOME);
        assetCategory = getIntent().getStringExtra(DetalhesAtivoActivity.EXTRA_CATEGORIA);
        walletId = getIntent().getIntExtra(DetalhesAtivoActivity.EXTRA_ID_CARTEIRA, -1);
        availableBalance = getIntent().getDoubleExtra(DetalhesAtivoActivity.EXTRA_SALDO, -1.0);
        availableAssetQuantity = getIntent().getDoubleExtra(
                DetalhesAtivoActivity.EXTRA_QUANTIDADE_DISPONIVEL_COMPRA,
                0.0
        );
        currentPrice = getIntent().getDoubleExtra(
                DetalhesAtivoActivity.EXTRA_PRECO_ATUAL_COMPRA,
                Double.NaN
        );
        dailyChangePercent = getIntent().getDoubleExtra(
                DetalhesAtivoActivity.EXTRA_VARIACAO_DIARIA_COMPRA,
                Double.NaN
        );

        String requestedType = getIntent().getStringExtra(EXTRA_TIPO_OPERACAO);
        operationType = TYPE_SELL.equalsIgnoreCase(requestedType) ? TYPE_SELL : TYPE_BUY;

        if (assetCategory == null || assetCategory.trim().isEmpty()) {
            assetCategory = "Ações";
        }
    }

    private void displayAssetData() {
        String safeTicker = ticker.trim();
        String safeAssetName = assetName == null || assetName.trim().isEmpty()
                ? safeTicker
                : assetName.trim();

        tvBuyAssetTitle.setText((isSellOperation() ? "Vender " : "Comprar ") + safeTicker);
        tvBuyAssetName.setText(safeAssetName);

        String updateText = getIntent().getStringExtra(
                DetalhesAtivoActivity.EXTRA_ULTIMA_ATUALIZACAO_COMPRA
        );
        tvBuyQuoteUpdatedAt.setText(
                updateText == null || updateText.trim().isEmpty()
                        ? "Última atualização: não informada"
                        : updateText
        );

        tvBuyLastPrice.setText(formatCurrency(currentPrice));
        tvBuyUnitPrice.setText(formatCurrency(currentPrice));

        if (Double.isFinite(dailyChangePercent)) {
            String prefix = dailyChangePercent > 0.0 ? "▲ "
                    : dailyChangePercent < 0.0 ? "▼ " : "• ";
            String formattedChange = String.format(
                    PT_BR,
                    "%.2f%%",
                    Math.abs(dailyChangePercent)
            );
            tvBuyDailyChange.setText(prefix + formattedChange);
            tvBuyDailyChange.setTextColor(
                    dailyChangePercent > 0.0 ? COLOR_POSITIVE
                            : dailyChangePercent < 0.0 ? COLOR_NEGATIVE : COLOR_NEUTRAL
            );
        } else {
            tvBuyDailyChange.setText("—");
            tvBuyDailyChange.setTextColor(COLOR_NEUTRAL);
        }

        if (isSellOperation()) {
            tvBuyAvailableBalance.setText(
                    "Disponível para venda: " + formatQuantity(availableAssetQuantity) + " un."
            );
            tvBuyQuantityDescription.setText("Informe quantas unidades deseja vender.");
            tvBuyAssetsSubtotalLabel.setText("Valor bruto da venda");
            tvBuyEstimatedTotalLabel.setText("Total estimado da venda");
            btnSubmitBuyOrder.setText("Confirmar venda simulada");
        } else {
            tvBuyAvailableBalance.setText(
                    "Saldo para investir: " + formatCurrency(availableBalance)
            );
            tvBuyQuantityDescription.setText("Informe quantas unidades deseja comprar.");
            tvBuyAssetsSubtotalLabel.setText("Valor dos ativos");
            tvBuyEstimatedTotalLabel.setText("Total estimado da compra");
            btnSubmitBuyOrder.setText("Confirmar compra simulada");
        }
    }

    private void configureNavigation() {
        btnBackBuyAsset.setOnClickListener(view -> finish());
        btnSubmitBuyOrder.setOnClickListener(view -> submitOperation());
    }

    private void configureQuantityControls() {
        int initialQuantity = 1;
        if (isSellOperation()) {
            initialQuantity = availableAssetQuantity >= 1.0 ? 1 : 0;
        }
        edtBuyQuantity.setText(String.valueOf(initialQuantity));

        edtBuyQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action required.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                refreshOrderPreview();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // No action required.
            }
        });

        btnIncreaseBuyQuantity.setOnClickListener(view -> changeQuantity(1));
        btnDecreaseBuyQuantity.setOnClickListener(view -> changeQuantity(-1));
    }

    private void changeQuantity(int difference) {
        int quantity = getRequestedQuantity();
        long updatedQuantity = (long) quantity + difference;

        if (updatedQuantity < 0) {
            updatedQuantity = 0;
        }
        if (updatedQuantity > MAX_QUANTITY) {
            updatedQuantity = MAX_QUANTITY;
        }

        if (isSellOperation() && updatedQuantity > getSellableQuantity()) {
            Toast.makeText(
                    this,
                    "Você não possui essa quantidade disponível para venda.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        edtBuyQuantity.setText(String.valueOf(updatedQuantity));
        edtBuyQuantity.setSelection(edtBuyQuantity.getText().length());
    }

    private void refreshOrderPreview() {
        int quantity = getRequestedQuantity();
        tvBuyOrderQuantity.setText(quantity > 0 ? formatQuantity(quantity) + " un." : "0 un.");
        tvBuyUnitPrice.setText(formatCurrency(currentPrice));

        if (quantity <= 0 || !isValidPrice()) {
            tvBuyAssetsSubtotal.setText("R$ 0");
            tvBuyEstimatedFees.setText("R$ 0");
            tvBuyEstimatedTotal.setText("R$ 0");
        } else {
            BigDecimal subtotal = calculateSubtotal(quantity);
            BigDecimal estimatedFees = calculateEstimatedFee(subtotal);
            BigDecimal estimatedTotal = isSellOperation()
                    ? subtotal.subtract(estimatedFees).max(BigDecimal.ZERO)
                    : subtotal.add(estimatedFees);

            tvBuyAssetsSubtotal.setText(formatCurrency(subtotal));
            tvBuyEstimatedFees.setText(formatCurrency(estimatedFees));
            tvBuyEstimatedTotal.setText(formatCurrency(estimatedTotal));
        }

        String validationMessage = getValidationMessage(quantity);
        if (validationMessage == null) {
            tvBuySimulationNotice.setText(DEFAULT_SIMULATION_NOTICE);
            tvBuySimulationNotice.setTextColor(
                    getColor(R.color.text_secondary)
            );
        } else {
            tvBuySimulationNotice.setText(validationMessage);
            tvBuySimulationNotice.setTextColor(COLOR_NEGATIVE);
        }

        btnDecreaseBuyQuantity.setEnabled(quantity > 0 && !operationInProgress);
        boolean canIncrease = quantity < MAX_QUANTITY && !operationInProgress;
        if (isSellOperation()) {
            canIncrease = canIncrease && quantity < getSellableQuantity();
        }
        btnIncreaseBuyQuantity.setEnabled(canIncrease);
        btnSubmitBuyOrder.setEnabled(validationMessage == null && !operationInProgress);
    }

    @Nullable
    private String getValidationMessage(int quantity) {
        if (!isValidPrice()) {
            return "Não há uma cotação válida para este ativo. Volte aos detalhes e atualize a cotação.";
        }
        if (quantity <= 0) {
            return "Informe uma quantidade maior que zero.";
        }
        if (quantity > MAX_QUANTITY) {
            return "A quantidade máxima permitida é 1.000.000 unidades.";
        }

        if (isSellOperation()) {
            int sellableQuantity = getSellableQuantity();
            if (sellableQuantity <= 0) {
                return "Você não possui unidades deste ativo disponíveis para venda.";
            }
            if (quantity > sellableQuantity) {
                return "Quantidade indisponível. Você possui "
                        + formatQuantity(sellableQuantity) + " unidade(s) para vender.";
            }
        } else if (availableBalance >= 0.0 && Double.isFinite(availableBalance)) {
            BigDecimal total = calculateSubtotal(quantity)
                    .add(calculateEstimatedFee(calculateSubtotal(quantity)));
            BigDecimal balance = BigDecimal.valueOf(availableBalance)
                    .setScale(2, RoundingMode.HALF_UP);
            if (total.compareTo(balance) > 0) {
                return "Saldo insuficiente. Total estimado: " + formatCurrency(total)
                        + " | Disponível: " + formatCurrency(balance) + ".";
            }
        }

        return null;
    }

    private void submitOperation() {
        if (operationInProgress) {
            return;
        }

        int quantity = getRequestedQuantity();
        String validationMessage = getValidationMessage(quantity);
        if (validationMessage != null) {
            Toast.makeText(this, validationMessage, Toast.LENGTH_LONG).show();
            refreshOrderPreview();
            return;
        }

        if (walletId > 0) {
            registerOperation(operationType, quantity, walletId);
            return;
        }

        chooseWalletAndRegister(quantity);
    }

    private void chooseWalletAndRegister(int quantity) {
        setOperationInProgress(true);
        new WalletRepository(this).buscarWallets(new WalletRepository.WalletListCallback() {
            @Override
            public void onSuccess(List<Wallet> wallets) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                if (wallets == null || wallets.isEmpty()) {
                    setOperationInProgress(false);
                    Toast.makeText(
                            BuyAssetActivity.this,
                            "Crie uma carteira antes de registrar uma operação.",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                Wallet[] options = wallets.toArray(new Wallet[0]);
                CharSequence[] walletNames = new CharSequence[options.length];
                for (int i = 0; i < options.length; i++) {
                    walletNames[i] = options[i].getNome_carteira()
                            + " · " + options[i].getTipo_carteira();
                }

                new AlertDialog.Builder(BuyAssetActivity.this)
                        .setTitle("Escolha a carteira")
                        .setItems(walletNames, (dialog, which) -> {
                            if (which >= 0 && which < options.length) {
                                registerOperation(
                                        operationType,
                                        quantity,
                                        options[which].getId_carteira()
                                );
                            } else {
                                setOperationInProgress(false);
                            }
                        })
                        .setNegativeButton("Cancelar", (dialog, which) ->
                                setOperationInProgress(false))
                        .setOnCancelListener(dialog -> setOperationInProgress(false))
                        .show();
            }

            @Override
            public void onError(String message) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                setOperationInProgress(false);
                Toast.makeText(BuyAssetActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void registerOperation(String type, int quantity, int selectedWalletId) {
        setOperationInProgress(true);

        String safeName = assetName == null || assetName.trim().isEmpty()
                ? ticker
                : assetName.trim();
        BigDecimal subtotal = calculateSubtotal(quantity);
        BigDecimal estimatedFees = calculateEstimatedFee(subtotal);

        BigDecimal operationTotal = TYPE_SELL.equals(type)
                ? subtotal.subtract(estimatedFees).max(BigDecimal.ZERO)
                : subtotal.add(estimatedFees);

        clsAportes contribution = new clsAportes(
                ticker,
                safeName,
                assetCategory,
                (double) quantity,
                operationTotal.doubleValue(),
                type,
                "Único",
                selectedWalletId
        );

        new AporteRepository(this).registrarAporte(
                contribution,
                new AporteRepository.RegistroCallback() {
                    @Override
                    public void onSucesso(String message) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        Toast.makeText(
                                BuyAssetActivity.this,
                                message + " (operação simulada)",
                                Toast.LENGTH_LONG
                        ).show();

                        Intent result = new Intent();
                        result.putExtra(DetalhesAtivoActivity.EXTRA_ID_CARTEIRA, selectedWalletId);
                        setResult(Activity.RESULT_OK, result);
                        finish();
                    }

                    @Override
                    public void onErro(String message) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        setOperationInProgress(false);
                        Toast.makeText(BuyAssetActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void setOperationInProgress(boolean inProgress) {
        operationInProgress = inProgress;
        btnSubmitBuyOrder.setText(inProgress
                ? "Registrando operação…"
                : isSellOperation() ? "Confirmar venda simulada" : "Confirmar compra simulada");
        refreshOrderPreview();
    }

    private int getRequestedQuantity() {
        String value = edtBuyQuantity.getText() == null
                ? ""
                : edtBuyQuantity.getText().toString().trim();
        if (value.isEmpty()) {
            return 0;
        }
        try {
            long parsed = Long.parseLong(value);
            if (parsed > Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
            return (int) parsed;
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private int getSellableQuantity() {
        if (!Double.isFinite(availableAssetQuantity) || availableAssetQuantity <= 0.0) {
            return 0;
        }
        return (int) Math.min(Math.floor(availableAssetQuantity), MAX_QUANTITY);
    }

    private boolean isSellOperation() {
        return TYPE_SELL.equals(operationType);
    }

    private boolean isValidPrice() {
        return Double.isFinite(currentPrice) && currentPrice > 0.0;
    }

    private BigDecimal calculateSubtotal(int quantity) {
        return BigDecimal.valueOf(currentPrice)
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateEstimatedFee(BigDecimal subtotal) {
        return subtotal.multiply(ESTIMATED_FEE_RATE)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private String formatCurrency(double value) {
        if (!Double.isFinite(value) || value < 0.0) {
            return "—";
        }
        return CURRENCY_FORMAT.format(value);
    }

    private String formatCurrency(BigDecimal value) {
        return CURRENCY_FORMAT.format(value);
    }

    private String formatQuantity(double quantity) {
        if (!Double.isFinite(quantity) || quantity < 0.0) {
            return "0";
        }
        return QUANTITY_FORMAT.format((long) Math.floor(quantity));
    }
}
