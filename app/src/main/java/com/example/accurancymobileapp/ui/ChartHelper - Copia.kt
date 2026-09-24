package com.example.accurancymobileapp.ui

import androidx.compose.ui.platform.ComposeView
import androidx.compose.material3.MaterialTheme


object ChartHelper {
    @JvmStatic
    fun GraphicConfig(composeView: ComposeView, valores: List<Number>) {
        composeView.setContent {
            ChartLineDashboard(valores)
        }
    }

    @JvmStatic
    fun configurarGraficoLine(
        composeView: ComposeView,
        valores: List<Number>
    ) {
        composeView.setContent {
            MaterialTheme {
                LineChart(valores)
            }
        }
    }

    @JvmStatic
    fun configurarGraficoPizza(
        composeView: ComposeView,
        valores: List<Number>
    ) {
        composeView.setContent {
            MaterialTheme {
                pizzaChart(valores = valores)
            }
        }
    }
}