package com.example.accurancymobileapp.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.pie.PieChartHost
import com.patrykandpatrick.vico.compose.pie.data.PieChartModelProducer
import com.patrykandpatrick.vico.compose.pie.data.pieSeries
import com.patrykandpatrick.vico.compose.pie.rememberPieChart
import kotlinx.coroutines.delay


//Gráfico de linhas na Dashboard
@Composable
fun ChartLineDashboard(valores: List<Number>) {

    val meses = listOf(
        "Jan",
        "Fev",
        "Mar",
        "Abr",
        "Mai",
        "Jun")


    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(valores) {
        if (valores.isEmpty()) return@LaunchedEffect
        modelProducer.runTransaction {
            lineSeries {
                series(valores)
            }
        }
    }


    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val alphaGrafico by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(700),
        label = "alphaGrafico"
    )

    val corLinha = Color(0xFF246BFD)
    val corFundo = Color(0xFF06101E)
    val corTexto = Color(0xFF6F829D)
    val corGrade = Color.White.copy(alpha = 0.08f)

    val linhaCustomizada = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(corLinha)),
        stroke = LineCartesianLayer.LineStroke.Continuous(3.dp),
        areaFill = LineCartesianLayer.AreaFill.single(
            Fill(
                Brush.verticalGradient(
                    listOf(corLinha.copy(alpha = 0.35f), Color.Transparent)
                )
            )
        ),
        interpolator = LineCartesianLayer.Interpolator.catmullRom(),
    )


    val linhaDeGrade = rememberLineComponent(
        fill = Fill(corGrade)
    )
    val estiloTextoEixo = rememberTextComponent(TextStyle(color = corTexto))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .alpha(alphaGrafico)
            .background(corFundo)
            .padding(12.dp)
    ) {

        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider = LineCartesianLayer.LineProvider.series(linhaCustomizada)
                ),
                startAxis = VerticalAxis.rememberStart(
                    label = estiloTextoEixo,
                    guideline = linhaDeGrade,
                    valueFormatter = CartesianValueFormatter.decimal(suffix = "R$"),
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    label = estiloTextoEixo,
                    guideline = null,
                    valueFormatter = { _, value, _ ->
                        meses.getOrElse(value.toInt()) { "" }
                    },
                ),
            ),
            modelProducer = modelProducer,
            modifier = Modifier.fillMaxWidth(),
        )
    }
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

    //Gráfico em Pizza
    @Composable
    fun pizzaChart(
        valores: List<Number>,
        modifier: Modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    ) {
        val modelProducer = remember {
            PieChartModelProducer()
        }

        LaunchedEffect(Unit) {
            modelProducer.runTransaction {
                pieSeries {
                    series(40, 30, 20, 10)
                }
            }
        }

        PieChartHost(
            chart = rememberPieChart(),
            modelProducer = modelProducer,
            modifier = modifier
        )
}