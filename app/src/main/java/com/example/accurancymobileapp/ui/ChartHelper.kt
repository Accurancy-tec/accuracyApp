package com.example.accurancymobileapp.ui

import androidx.compose.ui.platform.ComposeView

object ChartHelper {
    @JvmStatic
    fun configurarGrafico(composeView: ComposeView, valores: List<Number>) {
        composeView.setContent {
            EvoCarteiraChart(valores)
        }
    }
}