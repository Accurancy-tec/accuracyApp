package com.example.accurancymobileapp.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import kotlinx.coroutines.delay


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

//Gráfico de linhas
@Composable
fun LineChart(valores: List<Number>) {

    val meses = listOf(
        "Jan",
        "Fev",
        "Mar",
        "Abr",
        "Mai"
    )

    val modelProducer = remember {
        CartesianChartModelProducer()
    }

    var visible by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        visible = true
    }

    LaunchedEffect(valores) {
        if(valores.isEmpty()) return@LaunchedEffect

        delay(250)
        modelProducer.runTransaction {
            lineSeries {
                series(valores)
            }
        }
    }

    val alphaGrafico by animateFloatAsState(
        targetValue = if(visible) 1f else 0f,
        animationSpec = tween(700),
        label = "alphaGráfico"
    )

    val azulGrafico = Color(0xFF246BFD)
    val fundoGrafico = Color(0xFF06101E)
    val corTextoEixo = Color(0xFF6F829D)

    // Marcador ao clicar na linha
    val axisLabel = rememberTextComponent(TextStyle(corTextoEixo))

    val marker = rememberDefaultCartesianMarker(
        label = rememberTextComponent()
    )

    val pointComponent = rememberShapeComponent(
        fill = Fill(azulGrafico),
        shape = CircleShape
    )

    val point = LineCartesianLayer.Point(
        component = pointComponent,
        size = 8.dp
    )

    val guideline = rememberLineComponent(
        fill = Fill(Color.White.copy(alpha = 0.08f))
    )

    val horizontalGuideLine = rememberLineComponent(
        fill = Fill(Color.White.copy(alpha = 0.06f))
    )

    val customLine = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(
            Fill(azulGrafico)
        ),

        stroke = LineCartesianLayer.LineStroke.Continuous(
            3.dp
        ),

        areaFill = LineCartesianLayer.AreaFill.single(
            Fill(
                azulGrafico.copy(alpha = 0.20f)
            )
        ),

        interpolator = LineCartesianLayer.Interpolator.catmullRom(0.4f)

    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alphaGrafico)
            .background(fundoGrafico)
            .padding(12.dp)
    ) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    // Personalização da linha do gráfico
                    lineProvider = LineCartesianLayer.LineProvider.series(
                        customLine
                    )
                ),
                startAxis = VerticalAxis.rememberStart(
                    guideline = horizontalGuideLine,
                    label = axisLabel,
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    guideline = null,
                    label = axisLabel,
                    valueFormatter = { _, value, _ ->
                        meses.getOrElse(value.toInt()) {""}
                    }
                ),

                marker = marker
            ),
            modelProducer = modelProducer,
            modifier = Modifier.fillMaxWidth()
        )
    }
}