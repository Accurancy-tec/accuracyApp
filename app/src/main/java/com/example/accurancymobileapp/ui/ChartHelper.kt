package com.example.accurancymobileapp.ui

import androidx.compose.ui.platform.ComposeView
import androidx.compose.material3.MaterialTheme

object ChartHelper {
    @JvmStatic
    fun configurarGrafico(composeView: ComposeView, valores: List<Number>) {
        composeView.setContent {
            EvoCarteiraChart(valores)
        }
    }

    @JvmStatic
    fun configurarGraficoWallet(
        composeView: ComposeView,
        valores: List<Number>
    ) {
        composeView.setContent {
            MaterialTheme {
                EvoCarteiraChart(valores)
            }
        }
    }
}