package com.example.accurancymobileapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorRes;
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
import com.example.accurancymobileapp.utils.Aviso;
import com.example.accurancymobileapp.utils.EscolhaCarteira;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class BuyAssetActivity extends AppCompatActivity {

    public static final String EXTRA_TIPO_OPERACAO = "tipo_operacao";
    /** Nome da carteira de onde sai a venda (escolhida antes, na tela do ativo). */
    public static final String EXTRA_NOME_CARTEIRA = "nome_carteira";

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(PT_BR);
    private static final NumberFormat QUANTITY_FORMAT = NumberFormat.getIntegerInstance(PT_BR);

    private static final BigDecimal ESTIMATED_FEE_RATE = new BigDecimal("0.0003");
    private static final BigDecimal MAX_QUANTITY = new BigDecimal("1000000");
    private static final int CRYPTO_QUANTITY_SCALE = 8;
    private static final BigDecimal MIN_OPERATION_VALUE = new BigDecimal("0.01");

    private static final String TYPE_BUY = "Compra";
    private static final String TYPE_SELL = "Venda";
    private static final String DEFAULT_SIMULATION_NOTICE = "Operação simulada para fins educacionais. Nenhuma ordem será enviada à B3.";
    private static final String CRYPTO_SIMULATION_NOTICE = "Operação simulada com a cotação atual da criptomoeda, em reais. Nenhuma ordem real é enviada.";
    private static final String CURRENCY_SIMULATION_NOTICE = "Operação simulada com a cotação atual da moeda, em reais. Nenhuma ordem real de câmbio é enviada.";
    private static final String FUTURES_SIMULATION_NOTICE = "Operação simulada com o preço de ajuste do contrato, sem margem, alavancagem ou ajuste diário. Nenhuma ordem será enviada à B3.";

    @ColorRes private static final int COLOR_POSITIVE = R.color.positivo_destaque;
    @ColorRes private static final int COLOR_NEGATIVE = R.color.negativo_destaque;
    @ColorRes private static final int COLOR_NEUTRAL = R.color.text_primary;

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
            Aviso.makeText(this, "Não foi informado qual ativo operar.", Toast.LENGTH_LONG).show();
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
        boolean barrasClaras = getResources().getBoolean(R.bool.barras_sistema_claras);
        controller.setAppearanceLightStatusBars(barrasClaras);
        controller.setAppearanceLightNavigationBars(barrasClaras);

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
            tvBuyDailyChange.setTextColor(getColor(
                    dailyChangePercent > 0.0 ? COLOR_POSITIVE
                            : dailyChangePercent < 0.0 ? COLOR_NEGATIVE : COLOR_NEUTRAL
            ));
        } else {
            tvBuyDailyChange.setText("—");
            tvBuyDailyChange.setTextColor(getColor(COLOR_NEUTRAL));
        }

        if (isSellOperation()) {
            String walletName = getIntent().getStringExtra(EXTRA_NOME_CARTEIRA);
            tvBuyAvailableBalance.setText(
                    (walletName == null || walletName.isEmpty()
                            ? "Disponível para venda: "
                            : "Disponível em " + walletName + ": ")
                            + formatQuantity(availableAssetQuantity) + unitSuffix()
            );
            tvBuyQuantityDescription.setText(isFractionalAsset()
                    ? "Informe quanto deseja vender (até 8 casas decimais)."
                    : "Informe quantas unidades deseja vender.");
            tvBuyAssetsSubtotalLabel.setText("Valor bruto da venda");
            tvBuyEstimatedTotalLabel.setText("Total estimado da venda");
            btnSubmitBuyOrder.setText("Confirmar venda simulada");
        } else {
            tvBuyAvailableBalance.setText(
                    "Saldo geral: " + formatCurrency(availableBalance)
            );
            tvBuyQuantityDescription.setText(isFractionalAsset()
                    ? "Informe quanto deseja comprar (até 8 casas decimais)."
                    : "Informe quantas unidades deseja comprar.");
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
        if (isFractionalAsset()) {
            // Crypto and currencies accept fractions (up to 8 decimals); other assets stay integer-only.
            edtBuyQuantity.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            edtBuyQuantity.setKeyListener(DigitsKeyListener.getInstance("0123456789.,"));
            edtBuyQuantity.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        }

        BigDecimal initialQuantity = quantityStep();
        if (isSellOperation() && initialQuantity.compareTo(getSellableQuantity()) > 0) {
            initialQuantity = getSellableQuantity();
        }
        edtBuyQuantity.setText(plainQuantity(initialQuantity));

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

    private void changeQuantity(int direction) {
        BigDecimal quantity = getRequestedQuantity();
        BigDecimal updatedQuantity = quantity.add(quantityStep().multiply(BigDecimal.valueOf(direction)))
                .setScale(quantityScale(), RoundingMode.DOWN);

        if (updatedQuantity.signum() < 0) {
            updatedQuantity = BigDecimal.ZERO;
        }
        if (updatedQuantity.compareTo(MAX_QUANTITY) > 0) {
            updatedQuantity = MAX_QUANTITY;
        }

        if (isSellOperation() && updatedQuantity.compareTo(getSellableQuantity()) > 0) {
            Aviso.makeText(
                    this,
                    "Você não possui essa quantidade disponível para venda.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        edtBuyQuantity.setText(plainQuantity(updatedQuantity));
        edtBuyQuantity.setSelection(edtBuyQuantity.getText().length());
    }

    private void refreshOrderPreview() {
        BigDecimal quantity = getRequestedQuantity();
        tvBuyOrderQuantity.setText(quantity.signum() > 0
                ? formatQuantity(quantity) + unitSuffix()
                : "0" + unitSuffix());
        tvBuyUnitPrice.setText(formatCurrency(currentPrice));

        if (quantity.signum() <= 0 || !isValidPrice()) {
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
            tvBuySimulationNotice.setText(isFuturesAsset()
                    ? FUTURES_SIMULATION_NOTICE
                    : isCryptoAsset() ? CRYPTO_SIMULATION_NOTICE
                    : isCurrencyAsset() ? CURRENCY_SIMULATION_NOTICE
                    : DEFAULT_SIMULATION_NOTICE);
            tvBuySimulationNotice.setTextColor(
                    getColor(R.color.text_secondary)
            );
        } else {
            tvBuySimulationNotice.setText(validationMessage);
            tvBuySimulationNotice.setTextColor(getColor(COLOR_NEGATIVE));
        }

        btnDecreaseBuyQuantity.setEnabled(quantity.signum() > 0 && !operationInProgress);
        boolean canIncrease = quantity.compareTo(MAX_QUANTITY) < 0 && !operationInProgress;
        if (isSellOperation()) {
            canIncrease = canIncrease && quantity.compareTo(getSellableQuantity()) < 0;
        }
        btnIncreaseBuyQuantity.setEnabled(canIncrease);
        btnSubmitBuyOrder.setEnabled(validationMessage == null && !operationInProgress);
    }

    @Nullable
    private String getValidationMessage(BigDecimal quantity) {
        if (!isValidPrice()) {
            return "Não há uma cotação válida para este ativo. Volte aos detalhes e atualize a cotação.";
        }
        if (quantity.signum() <= 0) {
            return "Informe uma quantidade maior que zero.";
        }
        if (quantity.stripTrailingZeros().scale() > quantityScale()) {
            return isFractionalAsset()
                    ? "Use no máximo 8 casas decimais."
                    : "Informe uma quantidade inteira.";
        }
        if (quantity.compareTo(MAX_QUANTITY) > 0) {
            return "A quantidade máxima permitida é 1.000.000 unidades.";
        }
        if (calculateSubtotal(quantity).compareTo(MIN_OPERATION_VALUE) < 0) {
            return "O valor da operação é menor que R$ 0,01. Aumente a quantidade.";
        }

        if (isSellOperation()) {
            BigDecimal sellableQuantity = getSellableQuantity();
            if (sellableQuantity.signum() <= 0) {
                return "Você não possui unidades deste ativo disponíveis para venda.";
            }
            if (quantity.compareTo(sellableQuantity) > 0) {
                return "Quantidade indisponível. Você possui "
                        + formatQuantity(sellableQuantity) + " unidade(s) para vender.";
            }
        }
        // O saldo é conferido depois que a carteira de destino é conhecida (ver chooseWalletAndRegister):
        // carteira real usa o saldo geral; carteira simulada, o saldo da própria simulação.

        return null;
    }

    private void submitOperation() {
        if (operationInProgress) {
            return;
        }

        BigDecimal quantity = getRequestedQuantity();
        String validationMessage = getValidationMessage(quantity);
        if (validationMessage != null) {
            Aviso.makeText(this, validationMessage, Toast.LENGTH_LONG).show();
            refreshOrderPreview();
            return;
        }

        chooseWalletAndRegister(quantity);
    }

    /**
     * Descobre a carteira de destino (a recebida da tela anterior ou a escolhida aqui, já marcando a
     * carteira em foco), confere o dinheiro disponível nela e só então registra a operação.
     */
    private void chooseWalletAndRegister(BigDecimal quantity) {
        setOperationInProgress(true);
        new WalletRepository(this).buscarWallets(new WalletRepository.WalletListCallback() {
            @Override
            public void onSuccess(List<Wallet> wallets) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                if (wallets == null || wallets.isEmpty()) {
                    setOperationInProgress(false);
                    Aviso.makeText(
                            BuyAssetActivity.this,
                            "Crie uma carteira antes de registrar uma operação.",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                if (walletId > 0) {
                    for (Wallet option : wallets) {
                        if (option.getId_carteira() == walletId) {
                            confirmBalanceAndRegister(option, quantity);
                            return;
                        }
                    }
                }

                EscolhaCarteira.mostrar(BuyAssetActivity.this,
                        isSellOperation() ? "Vender de qual carteira?" : "Registrar em qual carteira?",
                        wallets, knownGeneralBalance(),
                        wallet -> confirmBalanceAndRegister(wallet, quantity),
                        () -> setOperationInProgress(false));
            }

            @Override
            public void onError(String message) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                setOperationInProgress(false);
                Aviso.makeText(BuyAssetActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    /** Saldo geral recebido da tela anterior, ou null quando ele não veio. */
    private BigDecimal knownGeneralBalance() {
        return availableBalance >= 0.0 && Double.isFinite(availableBalance)
                ? BigDecimal.valueOf(availableBalance).setScale(2, RoundingMode.HALF_UP)
                : null;
    }

    private void confirmBalanceAndRegister(Wallet wallet, BigDecimal quantity) {
        if (!isSellOperation()) {
            BigDecimal total = calculateSubtotal(quantity).add(calculateEstimatedFee(calculateSubtotal(quantity)));
            String missing = EscolhaCarteira.faltaSaldo(wallet, knownGeneralBalance(), total);
            if (missing != null) {
                setOperationInProgress(false);
                new MaterialAlertDialogBuilder(this)
                        .setTitle(wallet.isSimulada() ? "Saldo da simulação insuficiente" : "Saldo insuficiente")
                        .setMessage("Total estimado: " + formatCurrency(total) + "\n" + missing
                                + (wallet.isSimulada()
                                ? "\n\nDeposite mais na simulação (na tela da carteira) ou diminua a quantidade."
                                : "\n\nAdicione saldo no Início ou diminua a quantidade."))
                        .setPositiveButton("Entendi", null)
                        .show();
                return;
            }
        }
        registerOperation(operationType, quantity, wallet.getId_carteira());
    }

    private void registerOperation(String type, BigDecimal quantity, int selectedWalletId) {
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
                quantity.doubleValue(),
                operationTotal.doubleValue(),
                type,
                "Único",
                selectedWalletId
        );

        // Quoted unit price (without the estimated fee); the API validates it against the total.
        if (isValidPrice()) {
            contribution.setPrecoUnitario(currentPrice);
        }

        new AporteRepository(this).registrarAporte(
                contribution,
                new AporteRepository.RegistroCallback() {
                    @Override
                    public void onSucesso(String message) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        Aviso.makeText(
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
                        Aviso.makeText(BuyAssetActivity.this, message, Toast.LENGTH_LONG).show();
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

    private boolean isFuturesAsset() {
        return assetCategory != null
                && assetCategory.trim().toLowerCase(java.util.Locale.ROOT).contains("futuro");
    }

    private BigDecimal getRequestedQuantity() {
        String value = edtBuyQuantity.getText() == null
                ? ""
                : edtBuyQuantity.getText().toString().trim().replace(',', '.');
        if (value.isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            BigDecimal parsed = new BigDecimal(value);
            return parsed.signum() < 0 ? BigDecimal.ZERO : parsed;
        } catch (NumberFormatException exception) {
            // Inputs such as "1.2.3" are invalid: treated as zero so the screen asks for a valid amount.
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal getSellableQuantity() {
        if (!Double.isFinite(availableAssetQuantity) || availableAssetQuantity <= 0.0) {
            return BigDecimal.ZERO;
        }
        BigDecimal sellable = BigDecimal.valueOf(availableAssetQuantity)
                .setScale(quantityScale(), RoundingMode.DOWN);
        return sellable.min(MAX_QUANTITY);
    }

    private boolean isCryptoAsset() {
        return assetCategory != null
                && assetCategory.trim().toLowerCase(java.util.Locale.ROOT).startsWith("cripto");
    }

    /** Foreign currencies are stored under the "Internacional" category. */
    private boolean isCurrencyAsset() {
        return assetCategory != null
                && assetCategory.trim().toLowerCase(java.util.Locale.ROOT).startsWith("internacional");
    }

    /** Crypto and currencies can be traded in fractions (up to 8 decimals). */
    private boolean isFractionalAsset() {
        return isCryptoAsset() || isCurrencyAsset();
    }

    private int quantityScale() {
        return isFractionalAsset() ? CRYPTO_QUANTITY_SCALE : 0;
    }

    /** Step of the +/- buttons: 1 unit, or for fractional assets roughly R$ 100 worth rounded to a power of ten. */
    private BigDecimal quantityStep() {
        if (!isFractionalAsset() || !isValidPrice()) {
            return BigDecimal.ONE;
        }
        int exponent = (int) Math.floor(Math.log10(100.0 / currentPrice));
        exponent = Math.max(-CRYPTO_QUANTITY_SCALE, Math.min(0, exponent));
        return BigDecimal.ONE.movePointLeft(-exponent);
    }

    private String plainQuantity(BigDecimal quantity) {
        return quantity.signum() == 0 ? "0" : quantity.stripTrailingZeros().toPlainString();
    }

    private String unitSuffix() {
        return isFractionalAsset() && ticker != null ? " " + ticker.trim() : " un.";
    }

    private boolean isSellOperation() {
        return TYPE_SELL.equals(operationType);
    }

    private boolean isValidPrice() {
        return Double.isFinite(currentPrice) && currentPrice > 0.0;
    }

    private BigDecimal calculateSubtotal(BigDecimal quantity) {
        return BigDecimal.valueOf(currentPrice)
                .multiply(quantity)
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
        return formatQuantity(BigDecimal.valueOf(quantity).setScale(quantityScale(), RoundingMode.DOWN));
    }

    private String formatQuantity(BigDecimal quantity) {
        if (isFractionalAsset()) {
            NumberFormat cryptoFormat = NumberFormat.getNumberInstance(PT_BR);
            cryptoFormat.setMinimumFractionDigits(0);
            cryptoFormat.setMaximumFractionDigits(CRYPTO_QUANTITY_SCALE);
            return cryptoFormat.format(quantity);
        }
        return QUANTITY_FORMAT.format(quantity.setScale(0, RoundingMode.DOWN));
    }
}
