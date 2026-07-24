package com.example.accurancymobileapp.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart


@Composable
fun EvoCarteiraChart(valores: List<Number>) {
    /*
        val meses =listOf(
            "Janeiro",
            "Fevereiro",
            "Março",
            "Abril",
            "Maio",
            "Junho",
            "Julho",
            "Agosto",
            "Setembro",
            "Outubro",
            "Novembro",
            "Dezembro"
        )
        val modelProducer = remember { CartesianChartModelProducer() }

        LaunchedEffect(Unit) {
            modelProducer.runTransaction {
                lineSeries {
                    series(
                        400,
                        600,
                        800,
                        1000
                    )
                }
            }
        }

        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(),

                startAxis =
                    VerticalAxis.rememberStart(
                        valueFormatter = { value, _ ->
                            "R$ ${value.toInt()}"
                        }
                    ),

                bottomAxis =
                    HorizontalAxis.rememberBottom(
                        valueFormatter = { value, _ ->
                            meses.getOrElse(
                                value.toInt()
                            ) { "" }
                        }
                    )
            ),

            modelProducer = modelProducer
        )*/
            val modelProducer = remember { CartesianChartModelProducer() }

        LaunchedEffect(valores) {
            modelProducer.runTransaction {
                lineSeries { series(valores) }
            }
        }

        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(),
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom(),
            ),
            modelProducer = modelProducer,
            modifier = Modifier.fillMaxWidth()
        )
}