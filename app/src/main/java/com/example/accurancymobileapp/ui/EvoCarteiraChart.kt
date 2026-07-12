package com.example.accurancymobileapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart

@Composable
fun EvoCarteiraChart(valores: List<Number>) {

    val meses = listOf(
        "Jan",
        "Fev",
        "Mar",
        "Abr",
        "Mai",
        "Jun"
    )

    val modelProducer = remember {
        CartesianChartModelProducer()
    }

    LaunchedEffect(valores) {
        modelProducer.runTransaction {
            lineSeries {
                series(valores)
            }
        }
    }

    CartesianChartHost(

        chart = rememberCartesianChart(

            rememberLineCartesianLayer(),

            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = { _, value, _ ->
                    if (value.toInt() < meses.size)
                        meses[value.toInt()]
                    else
                        ""
                }
            ),

            startAxis = null
        ),

        modelProducer = modelProducer,

        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(
                Color(0xFF0F172A)
            )
    )
}