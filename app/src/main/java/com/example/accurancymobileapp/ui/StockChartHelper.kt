package com.example.accurancymobileapp.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView

/** Ponte Java/Kotlin no mesmo padrão do ChartHelper já existente no projeto. */
object StockChartHelper {
    @JvmStatic
    fun configurar(
        composeView: ComposeView,
        labels: List<String>,
        prices: List<Double>,
        selectedRange: String,
        loading: Boolean,
        error: String?
    ) {
        composeView.setContent {
            MaterialTheme {
                StockPriceChart(
                    labels = labels,
                    values = prices,
                    selectedRange = selectedRange,
                    loading = loading,
                    error = error
                )
            }
        }
    }
}
