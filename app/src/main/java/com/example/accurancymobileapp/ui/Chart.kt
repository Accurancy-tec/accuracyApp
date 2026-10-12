package com.example.accurancymobileapp.ui

import android.R
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLineComponent
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnModel
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
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
import java.util.Locale

private data class PontoGrafico(
    val data: LocalDate,
    val valor: Double
)

private val NomesMeses = listOf(
    "Jan", "Fev", "Mar", "Abr", "Mai", "Jun",
    "Jul", "Ago", "Set", "Out", "Nov", "Dez"
)

private val CorFundoGrafico = Color(0xFF06101E)
private val CorLinhaGrafico = Color(0xFF2563EB)
private val CorTextoGrafico = Color(0xFF6F829D)

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

private fun rotulosDoEixo(
    pontos: List<PontoGrafico>,
    periodo: String
): List<Pair<Int, String>> {

    if (periodo.equals("1M", ignoreCase = true)) {
        return emptyList()
    }

    return pontos.indices
        .filter { i ->
            i == 0 ||
                    YearMonth.from(pontos[i].data) !=
                    YearMonth.from(pontos[i - 1].data)
        }
        .map { i ->
            Pair(i, NomesMeses[pontos[i].data.monthValue - 1])
        }
}

@Composable
fun EvolucaoCarteiraChart(
    datas: List<String>,
    valores: List<Double>,
    periodo: String = "6M"
) {
    val pontos = remember(datas, valores) {
        prepararPontosGrafico(
            datas = datas,
            valores = valores
        )
    }

    val temValor = pontos.any { it.valor > 0.0 }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CorFundoGrafico)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {

        // Com pelo menos 2 dias o eixo é desenhado, mesmo sem nenhum valor.
        if (pontos.size >= 2) {
            GraficoLinha(
                pontos = pontos,
                periodo = periodo,
                temValor = temValor
            )
        }

        if (!temValor) {
            Text(
                text = "Sem dados para exibir ainda.\nFaça um aporte para ver a evolução da carteira.",
                color = CorTextoGrafico,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

@Composable
private fun GraficoLinha(
    pontos: List<PontoGrafico>,
    periodo: String,
    temValor: Boolean
) {
    val textMeasurer = rememberTextMeasurer()

    val estiloRotulo = remember {
        TextStyle(
            color = CorTextoGrafico,
            fontSize = 10.sp
        )
    }

    val rotulos = remember(pontos, periodo) {
        rotulosDoEixo(pontos, periodo)
    }

    Canvas(modifier = Modifier.fillMaxSize()) {

        val margemX = 6.dp.toPx()
        val margemTopo = 8.dp.toPx()
        val alturaRotulos = 24.dp.toPx()

        val baseY = size.height - alturaRotulos
        val alturaUtil = baseY - margemTopo

        // A escala sempre começa no zero: sem valor, a linha fica embaixo.
        val maximo = pontos.maxOf { it.valor }

        val passoX = (size.width - 2 * margemX) / (pontos.size - 1)

        val xs = pontos.indices.map { margemX + it * passoX }

        val ys = pontos.map { ponto ->
            val fracao = if (maximo > 0.0) {
                (ponto.valor / maximo).toFloat().coerceIn(0f, 1f)
            } else {
                0f
            }

            margemTopo + alturaUtil * (1f - fracao)
        }

        // Linha suave + área de baixo da linha.
        val linha = Path()
        val area = Path()

        linha.moveTo(xs[0], ys[0])
        area.moveTo(xs[0], baseY)
        area.lineTo(xs[0], ys[0])

        for (i in 1 until pontos.size) {
            val meioX = (xs[i - 1] + xs[i]) / 2f

            linha.cubicTo(meioX, ys[i - 1], meioX, ys[i], xs[i], ys[i])
            area.cubicTo(meioX, ys[i - 1], meioX, ys[i], xs[i], ys[i])
        }

        area.lineTo(xs.last(), baseY)
        area.close()

        if (temValor) {
            drawPath(
                path = area,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CorLinhaGrafico.copy(alpha = 0.30f),
                        Color.Transparent
                    ),
                    startY = margemTopo,
                    endY = baseY
                )
            )
        }

        drawLine(
            color = Color.White.copy(alpha = 0.08f),
            start = Offset(0f, baseY),
            end = Offset(size.width, baseY),
            strokeWidth = 1.dp.toPx()
        )

        drawPath(
            path = linha,
            color = CorLinhaGrafico,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Bolinha no valor mais recente.
        if (temValor) {
            drawCircle(
                color = CorLinhaGrafico,
                radius = 4.dp.toPx(),
                center = Offset(xs.last(), ys.last())
            )
        }

        rotulos.forEach { (indice, texto) ->
            val medida = textMeasurer.measure(texto, estiloRotulo)

            val x = xs[indice]
                .coerceIn(0f, size.width - medida.size.width)

            drawText(
                textLayoutResult = medida,
                topLeft = Offset(x, baseY + 8.dp.toPx())
            )
        }

        // 1M: legenda centralizada no lugar dos rótulos (evita o espaço vazio).
        if (periodo.equals("1M", ignoreCase = true)) {
            val legenda = textMeasurer.measure("Últimos 31 dias", estiloRotulo)

            drawText(
                textLayoutResult = legenda,
                topLeft = Offset(
                    (size.width - legenda.size.width) / 2f,
                    baseY + 8.dp.toPx()
                )
            )
        }
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

@Composable
fun HistoricoChart(
    meses: List<String>,
    compras: List<Double>,
    vendas: List<Double>
) {
    val azul = Color(0xFF2962FF)
    val vermelho = Color(0xFFFF646D)

    val modelProducer = remember {
        CartesianChartModelProducer()
    }

    LaunchedEffect(meses, compras, vendas) {
        modelProducer.runTransaction {
            columnModel {
                series(compras)
                series(vendas)
            }
        }
    }

    val corLinha = Color(0xFF2563EB)
    val corFundo = Color(0xFF06101E)
    val corTexto = Color(0xFF6F829D)
    val corGrade = Color.White.copy(alpha = 0.07f)

    val barraAzul = rememberLineComponent(
        fill = Fill(azul),
        thickness = 12.dp
    )

    val barraVermelha = rememberLineComponent(
        fill = Fill(vermelho),
        thickness = 12.dp
    )

    val formatadorMeses = remember(meses) {
        CartesianValueFormatter { _, valor, _ ->
            if (meses.isEmpty()) {
                valor.toInt().toString()
            } else {
                val indice = valor.toInt()
                    .coerceIn(meses.indices)

                meses[indice]
            }
        }
    }

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

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                columnProvider =
                    ColumnCartesianLayer.ColumnProvider.series(
                        barraAzul,
                        barraVermelha
                    ),
                mergeMode = {
                    ColumnCartesianLayer.MergeMode.Grouped()
                },
                columnCollectionSpacing = 8.dp
            ),

            startAxis = VerticalAxis.rememberStart(
                label = textoEixo,
                line = linhaGrade,
                guideline = linhaGrade,
                tick = null
            ),

            bottomAxis = HorizontalAxis.rememberBottom(
                label = textoEixo,
                line = linhaGrade,
                valueFormatter = formatadorMeses,
                guideline = null,
                tick = null
            )
        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    )
}