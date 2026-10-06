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
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
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
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private data class PontoGrafico(
    val data: LocalDate,
    val valor: Double
)

private data class SerieGrafico(
    val rotulos: List<String>,
    val valores: List<Double>,
    val passoEixo: Int
)

private val FormatoDataGrafico =
    DateTimeFormatter.ofPattern("dd/MM")

private val NomesMeses = listOf(
    "Jan",
    "Fev",
    "Mar",
    "Abr",
    "Mai",
    "Jun",
    "Jul",
    "Ago",
    "Set",
    "Out",
    "Nov",
    "Dez"
)

private fun formatarMes(
    data: LocalDate,
    mostrarAno: Boolean
): String {
    val mes = NomesMeses[data.monthValue - 1]

    return if (mostrarAno) {
        "$mes/${data.year.toString().takeLast(2)}"
    } else {
        mes
    }
}

private fun formatarMoedaGrafico(valor: Double): String {
    val formato = NumberFormat.getCurrencyInstance(
        Locale("pt", "BR")
    )

    formato.maximumFractionDigits = 0
    formato.minimumFractionDigits = 0

    return formato.format(valor)
}

private fun prepararPontosGrafico(
    datas: List<String>,
    valores: List<Double>
): List<PontoGrafico> {

    return datas
        .zip(valores)
        .mapNotNull { (data, valor) ->

            val dataConvertida = runCatching {
                LocalDate.parse(data)
            }.getOrNull()

            if (
                dataConvertida == null ||
                valor.isNaN() ||
                valor.isInfinite()
            ) {
                null
            } else {
                PontoGrafico(
                    data = dataConvertida,
                    valor = valor
                )
            }
        }
        .sortedBy { it.data }
}

private fun removerZerosIniciais(
    pontos: List<PontoGrafico>
): List<PontoGrafico> {

    val primeiroValorReal = pontos.indexOfFirst {
        it.valor > 0.0
    }

    if (primeiroValorReal == -1) {
        return emptyList()
    }

    return pontos.drop(primeiroValorReal)
}

private fun criarSerieGrafico(
    pontos: List<PontoGrafico>
): SerieGrafico {

    if (pontos.size < 2) {
        return SerieGrafico(
            rotulos = emptyList(),
            valores = emptyList(),
            passoEixo = 1
        )
    }

    val quantidadeDias = ChronoUnit.DAYS.between(
        pontos.first().data,
        pontos.last().data
    ).toInt()

    if (quantidadeDias <= 45) {

        val passo = (pontos.size / 6)
            .coerceAtLeast(1)

        return SerieGrafico(
            rotulos = pontos.map {
                it.data.format(FormatoDataGrafico)
            },
            valores = pontos.map {
                it.valor
            },
            passoEixo = passo
        )
    }

    val pontosPorMes = linkedMapOf<YearMonth, PontoGrafico>()

    pontos.forEach { ponto ->
        pontosPorMes[
            YearMonth.from(ponto.data)
        ] = ponto
    }

    val pontosMensais = pontosPorMes
        .values
        .toList()

    val possuiMaisDeUmAno = pontosMensais
        .map { it.data.year }
        .distinct()
        .size > 1

    return SerieGrafico(
        rotulos = pontosMensais.map {
            formatarMes(
                data = it.data,
                mostrarAno = possuiMaisDeUmAno
            )
        },
        valores = pontosMensais.map {
            it.valor
        },
        passoEixo = 1
    )
}

@Composable
fun EvolucaoCarteiraChart(
    datas: List<String>,
    valores: List<Double>
) {
    val pontos = remember(datas, valores) {
        prepararPontosGrafico(
            datas = datas,
            valores = valores
        )
    }

    val pontosValidos = remember(pontos) {
        removerZerosIniciais(pontos)
    }

    if (pontosValidos.size < 2) {
        return
    }

    if (pontosValidos.none { it.valor > 0.0 }) {
        return
    }

    val serie = remember(pontosValidos) {
        criarSerieGrafico(pontosValidos)
    }

    if (serie.valores.size < 2) {
        return
    }

    val modelProducer = remember {
        CartesianChartModelProducer()
    }

    LaunchedEffect(serie.valores) {
        modelProducer.runTransaction {
            lineModel {
                series(serie.valores)
            }
        }
    }

    val corLinha = Color(0xFF2563EB)
    val corFundo = Color(0xFF06101E)
    val corTexto = Color(0xFF6F829D)
    val corGrade = Color.White.copy(alpha = 0.07f)

    val linha = LineCartesianLayer.rememberLine(

        fill = LineCartesianLayer.LineFill.single(
            Fill(corLinha)
        ),

        stroke = LineCartesianLayer.LineStroke.Continuous(
            3.dp
        ),

        areaFill = LineCartesianLayer.AreaFill.single(
            Fill(
                Brush.verticalGradient(
                    listOf(
                        corLinha.copy(alpha = 0.30f),
                        Color.Transparent
                    )
                )
            )
        ),

        interpolator =
            LineCartesianLayer.Interpolator.catmullRom()
    )

    val linhaGrade = rememberLineComponent(
        fill = Fill(corGrade)
    )

    val textoEixo = rememberTextComponent(
        TextStyle(
            color = corTexto
        )
    )

    val formatadorY = remember {
        CartesianValueFormatter { _, value, _ ->
            formatarMoedaGrafico(value)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(corFundo)
            .padding(12.dp)
    ) {

        CartesianChartHost(

            chart = rememberCartesianChart(

                rememberLineCartesianLayer(
                    lineProvider =
                        LineCartesianLayer.LineProvider.series(
                            linha
                        )
                ),

                startAxis = VerticalAxis.rememberStart(
                    label = textoEixo,
                    guideline = linhaGrade,
                    valueFormatter = formatadorY
                ),

                bottomAxis = HorizontalAxis.rememberBottom(

                    label = textoEixo,
                    guideline = null,
                    itemPlacer =
                        HorizontalAxis.ItemPlacer.aligned(
                            spacing = {
                                serie.passoEixo
                            }
                        ),

                    valueFormatter =
                        CartesianValueFormatter {
                                _,
                                value,
                                _ ->

                            serie.rotulos.getOrNull(
                                value.toInt()
                            ) ?: ""
                        }
                )
            ),

            modelProducer = modelProducer,

            modifier = Modifier.fillMaxSize(),

            animateIn = true
        )
    }
}

private val CoresDistribuicao = listOf(
    Color(0xFF2563EB),
    Color(0xFF22C55E),
    Color(0xFFA855F7),
    Color(0xFFF59E0B),
    Color(0xFFEF4444),
    Color(0xFF06B6D4),
    Color(0xFFEC4899),
    Color(0xFF84CC16)
)

private fun formatarMoedaCarteira(valor: Double): String {
    val formato = NumberFormat.getCurrencyInstance(
        Locale("pt", "BR")
    )

    formato.maximumFractionDigits = 0
    formato.minimumFractionDigits = 0

    return formato.format(valor)
}

@Composable
fun DistribuicaoCarteiraChart(
    categorias: List<String>,
    valores: List<Double>
) {

    val itens = categorias
        .zip(valores)
        .filter { (_, valor) ->
            valor > 0.0 &&
                    !valor.isNaN() &&
                    !valor.isInfinite()
        }

    if (itens.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF06101E)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Sem investimentos para exibir",
                color = Color(0xFF6F829D),
                fontSize = 13.sp
            )
        }

        return
    }

    val nomes = itens.map { it.first }
    val dados = itens.map { it.second }

    val total = dados.sum()

    if (total <= 0.0) {
        return
    }

    val modelProducer = remember {
        PieChartModelProducer()
    }

    LaunchedEffect(dados) {
        modelProducer.runTransaction {
            pieSeries {
                series(dados)
            }
        }
    }

    val slices = remember(nomes.size) {
        nomes.mapIndexed { index, _ ->
            PieChart.Slice(
                fill = Fill(
                    CoresDistribuicao[
                        index % CoresDistribuicao.size
                    ]
                )
            )
        }
    }

    val sliceProvider = remember(slices) {
        PieChart.SliceProvider.series(slices)
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF06101E))
            .padding(
                horizontal = 8.dp,
                vertical = 4.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .weight(1.1f)
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
                    .height(230.dp),
                animateIn = true
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            }
        }

        Spacer(
            modifier = Modifier.size(8.dp)
        )

        Column(
            modifier = Modifier
                .weight(0.9f),
            verticalArrangement =
                Arrangement.spacedBy(9.dp)
        ) {

            nomes.forEachIndexed { index, categoria ->

                val valor = dados[index]

                val percentual =
                    (valor / total) * 100.0

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                CoresDistribuicao[
                                    index %
                                            CoresDistribuicao.size
                                ],
                                CircleShape
                            )
                    )

                    Spacer(
                        modifier = Modifier.size(7.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = categoria,
                            color = Color.White,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow =
                                TextOverflow.Ellipsis
                        )

                        Text(
                            text =
                                formatarMoedaCarteira(valor),
                            color =
                                Color(0xFF6F829D),
                            fontSize = 10.sp
                        )
                    }

                    Text(
                        text = String.format(
                            Locale("pt", "BR"),
                            "%.1f%%",
                            percentual
                        ),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight =
                            FontWeight.SemiBold
                    )
                }
            }
        }
    }
}