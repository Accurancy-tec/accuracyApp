package com.example.accurancymobileapp.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView

object ChartHelper {

    @JvmStatic
    fun configurarGraficoEvolucao(
        composeView: ComposeView,
        datas: List<String>,
        valores: List<Double>
    ) {
        composeView.setContent {
            MaterialTheme {
                EvolucaoCarteiraChart(
                    datas = datas,
                    valores = valores
                )
            }
        }
    }

    @JvmStatic
    fun configurarGraficoDistribuicao(
        composeView: ComposeView,
        categorias: List<String>,
        valores: List<Double>
    ) {
        composeView.setContent {
            MaterialTheme {
                DistribuicaoCarteiraChart(
                    categorias = categorias,
                    valores = valores
                )
            }
        }
    }
}
