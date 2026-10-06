package com.example.accurancymobileapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.pie.PieChart
import com.patrykandpatrick.vico.compose.pie.PieChartHost
import com.patrykandpatrick.vico.compose.pie.PieSize
import com.patrykandpatrick.vico.compose.pie.rememberPieChart
import com.patrykandpatrick.vico.compose.pie.data.PieChartModelProducer
import com.patrykandpatrick.vico.compose.pie.data.pieSeries
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val FundoGrafico = Color(0xFF06101E)
private val TextoGrafico = Color(0xFF6F829D)
private val AzulGrafico = Color(0xFF2563EB)

private val CoresDistribuicao = listOf(
    Color(0xFF2563EB),
    Color(0xFF22C55E),
    Color(0xFFA855F7),
    Color(0xFFF59E0B),
    Color(0xFFEF4444)
)

private val FormatoData = DateTimeFormatter.ofPattern("dd/MM")

private fun formatarData(data: String): String {
    return runCatching {
        LocalDate.parse(data).format(FormatoData)
    }.getOrDefault(data)
}

private fun formatarMoeda(valor: Double): String {
    val formato = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    formato.maximumFractionDigits = 0
    formato.minimumFractionDigits = 0
    return formato.format(valor)
}

@Composable
fun EvolucaoCarteiraChart(
    datas: List<String>,
    valores: List<Double>
) {
    if (datas.isEmpty() || valores.isEmpty() || datas.size != valores.size) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FundoGrafico),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Sem dados para exibir",
                color = TextoGrafico,
                fontSize = 13.sp
            )
        }
        return
    }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(valores) {
        modelProducer.runTransaction {
            lineSeries {
                series(valores)
            }
        }
    }

    val passo = remember(datas) {
        (datas.size / 5).coerceAtLeast(1)
    }

    val linha = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(AzulGrafico)),
        stroke = LineCartesianLayer.LineStroke.Continuous(3.dp),
        areaFill = LineCartesianLayer.AreaFill.single(
            Fill(
                Brush.verticalGradient(
                    listOf(
                        AzulGrafico.copy(alpha = 0.30f),
                        Color.Transparent
                    )
                )
            )
        ),
        interpolator = LineCartesianLayer.Interpolator.catmullRom()
    )

    val linhaGrade = rememberLineComponent(
        fill = Fill(Color.White.copy(alpha = 0.07f))
    )

    val estiloTexto = rememberTextComponent(
        TextStyle(color = TextoGrafico)
    )

    val formatadorEixoY = remember {
        CartesianValueFormatter { _, value, _ ->
            formatarMoeda(value)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoGrafico)
            .padding(12.dp)
    ) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider = LineCartesianLayer.LineProvider.series(linha)
                ),
                startAxis = VerticalAxis.rememberStart(
                    label = estiloTexto,
                    guideline = linhaGrade,
                    valueFormatter = formatadorEixoY
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    label = estiloTexto,
                    guideline = null,
                    itemPlacer = HorizontalAxis.ItemPlacer.aligned(
                        spacing = { passo }
                    ),
                    valueFormatter = CartesianValueFormatter { _, value, _ ->
                        datas.getOrNull(value.toInt())?.let(::formatarData) ?: "-"
                    }
                )
            ),
            modelProducer = modelProducer,
            modifier = Modifier.fillMaxSize(),
            animateIn = true
        )
    }
}

@Composable
fun DistribuicaoCarteiraChart(
    categorias: List<String>,
    valores: List<Double>
) {
    val itens = categorias.zip(valores).filter { it.second > 0.0 }

    if (itens.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FundoGrafico),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Sem investimentos para distribuir",
                color = TextoGrafico,
                fontSize = 13.sp
            )
        }
        return
    }

    val nomes = itens.map { it.first }
    val dados = itens.map { it.second }
    val total = dados.sum()

    val modelProducer = remember { PieChartModelProducer() }

    LaunchedEffect(dados) {
        modelProducer.runTransaction {
            pieSeries {
                series(dados)
            }
        }
    }

    val sliceProvider = remember(nomes.size) {
        PieChart.SliceProvider.series(
            CoresDistribuicao.map { cor ->
                PieChart.Slice(fill = Fill(cor))
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoGrafico)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1.15f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            PieChartHost(
                chart = rememberPieChart(
                    sliceProvider = sliceProvider,
                    innerSize = PieSize.Inner.fixed(58.dp)
                ),
                modelProducer = modelProducer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                animateIn = true
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "100%",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "carteira",
                    color = TextoGrafico,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.size(8.dp))

        Column(
            modifier = Modifier.weight(0.85f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            nomes.forEachIndexed { index, categoria ->
                val percentual = (dados[index] / total) * 100.0
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                CoresDistribuicao[index % CoresDistribuicao.size],
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = categoria,
                            color = Color.White,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = formatarMoeda(dados[index]),
                            color = TextoGrafico,
                            fontSize = 10.sp
                        )
                    }
                    Text(
                        text = String.format(Locale("pt", "BR"), "%.1f%%", percentual),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
