package com.example.accurancymobileapp.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.accurancymobileapp.R
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.pie.PieChart
import com.patrykandpatrick.vico.compose.pie.PieChartHost
import com.patrykandpatrick.vico.compose.pie.PieSize
import com.patrykandpatrick.vico.compose.pie.data.PieChartModelProducer
import com.patrykandpatrick.vico.compose.pie.data.pieSeries
import com.patrykandpatrick.vico.compose.pie.rememberPieChart
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max

private data class PontoGrafico(
    val data: LocalDate,
    val valor: Double
)

private val LocalBR: Locale = Locale.forLanguageTag("pt-BR")

private val FormatoDataToque: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", LocalBR)

private val NomesMeses = listOf(
    "Jan", "Fev", "Mar", "Abr", "Mai", "Jun",
    "Jul", "Ago", "Set", "Out", "Nov", "Dez"
)

/** Quanto tempo o valor tocado continua visível depois que o dedo sai da tela. */
private const val TEMPO_DESTAQUE_APOS_SOLTAR_MS = 1200L

private fun formatarMoeda(valor: Double, casasDecimais: Int = 2): String {
    val formato = NumberFormat.getCurrencyInstance(LocalBR)
    formato.minimumFractionDigits = casasDecimais
    formato.maximumFractionDigits = casasDecimais
    return formato.format(valor)
}

private fun prepararPontosGrafico(
    datas: List<String>,
    valores: List<Double>
): List<PontoGrafico> {
    return datas
        .zip(valores)
        .mapNotNull { (data, valor) ->
            val dataConvertida = runCatching { LocalDate.parse(data) }.getOrNull()
            if (dataConvertida == null || valor.isNaN() || valor.isInfinite()) {
                null
            } else {
                PontoGrafico(data = dataConvertida, valor = valor)
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
        .filter { i -> i == 0 || YearMonth.from(pontos[i].data) != YearMonth.from(pontos[i - 1].data) }
        .map { i -> Pair(i, NomesMeses[pontos[i].data.monthValue - 1]) }
}

@Composable
fun EvolucaoCarteiraChart(
    datas: List<String>,
    valores: List<Double>,
    periodo: String = "6M"
) {
    val pontos = remember(datas, valores) {
        prepararPontosGrafico(datas = datas, valores = valores)
    }
    val temValor = pontos.any { it.valor > 0.0 }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Com pelo menos 2 dias o eixo é desenhado, mesmo sem nenhum valor.
        if (pontos.size >= 2) {
            GraficoLinha(pontos = pontos, periodo = periodo, temValor = temValor)
        }

        if (!temValor) {
            Text(
                text = "Sem dados para exibir ainda.\nFaça um aporte para ver a evolução da carteira.",
                color = colorResource(R.color.text_secondary),
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
    val corLinha = colorResource(R.color.primary_blue)
    val corTexto = colorResource(R.color.text_secondary)
    val corTextoForte = colorResource(R.color.text_primary)
    val corGrade = colorResource(R.color.grafico_grade)
    val corBalao = colorResource(R.color.card_background)
    val corBorda = colorResource(R.color.borda)

    val textMeasurer = rememberTextMeasurer()
    val estiloRotulo = TextStyle(color = corTexto, fontSize = 10.sp)
    val estiloData = TextStyle(color = corTexto, fontSize = 11.sp)
    val estiloValor = TextStyle(color = corTextoForte, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)

    val rotulos = remember(pontos, periodo) { rotulosDoEixo(pontos, periodo) }

    // A linha é "desenhada" da esquerda para a direita sempre que os dados mudam.
    val progresso = remember { Animatable(0f) }
    LaunchedEffect(pontos) {
        progresso.snapTo(0f)
        progresso.animateTo(1f, tween(durationMillis = 900, easing = FastOutSlowInEasing))
    }

    var selecionado by remember(pontos) { mutableStateOf<Int?>(null) }
    var tocando by remember { mutableStateOf(false) }
    LaunchedEffect(tocando) {
        if (!tocando) {
            delay(TEMPO_DESTAQUE_APOS_SOLTAR_MS)
            selecionado = null
        }
    }

    val view = LocalView.current
    val vibracao = LocalHapticFeedback.current
    val margemX = with(LocalDensity.current) { 6.dp.toPx() }
    val ultimo = pontos.last()
    val descricao = "Evolução do patrimônio: ${formatarMoeda(ultimo.valor)} em ${ultimo.data.format(FormatoDataToque)}"

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = descricao }
            .pointerInput(pontos, temValor) {
                if (!temValor) return@pointerInput
                acompanharToque(view, pontos.size, margemX) { indice ->
                    if (indice == null) {
                        tocando = false
                    } else {
                        if (indice != selecionado) {
                            vibracao.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        tocando = true
                        selecionado = indice
                    }
                }
            }
    ) {
        val margemTopo = 8.dp.toPx()
        val alturaRotulos = 24.dp.toPx()
        val baseY = size.height - alturaRotulos
        val alturaUtil = baseY - margemTopo

        // A escala sempre começa no zero: sem valor, a linha fica embaixo.
        val maximo = pontos.maxOf { it.valor }
        val passoX = (size.width - 2 * margemX) / (pontos.size - 1)
        val xs = pontos.indices.map { margemX + it * passoX }
        val ys = pontos.map { ponto ->
            val fracao = if (maximo > 0.0) (ponto.valor / maximo).toFloat().coerceIn(0f, 1f) else 0f
            margemTopo + alturaUtil * (1f - fracao)
        }

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

        drawLine(
            color = corGrade,
            start = Offset(0f, baseY),
            end = Offset(size.width, baseY),
            strokeWidth = 1.dp.toPx()
        )

        val limiteDesenho = margemX + (size.width - 2 * margemX) * progresso.value + 4.dp.toPx()
        clipRect(right = limiteDesenho) {
            if (temValor) {
                drawPath(
                    path = area,
                    brush = Brush.verticalGradient(
                        colors = listOf(corLinha.copy(alpha = 0.26f), Color.Transparent),
                        startY = margemTopo,
                        endY = baseY
                    )
                )
            }
            drawPath(
                path = linha,
                color = corLinha,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        val indiceSelecionado = selecionado
        if (temValor && progresso.value >= 1f && indiceSelecionado == null) {
            drawCircle(color = corLinha, radius = 4.dp.toPx(), center = Offset(xs.last(), ys.last()))
        }

        rotulos.forEach { (indice, texto) ->
            val medida = textMeasurer.measure(texto, estiloRotulo)
            val x = xs[indice].coerceIn(0f, size.width - medida.size.width)
            drawText(textLayoutResult = medida, topLeft = Offset(x, baseY + 8.dp.toPx()))
        }

        // 1M: legenda centralizada no lugar dos rótulos dos meses.
        if (periodo.equals("1M", ignoreCase = true)) {
            val legenda = textMeasurer.measure("Últimos 31 dias", estiloRotulo)
            drawText(
                textLayoutResult = legenda,
                topLeft = Offset((size.width - legenda.size.width) / 2f, baseY + 8.dp.toPx())
            )
        }

        if (indiceSelecionado != null && indiceSelecionado in pontos.indices) {
            val ponto = pontos[indiceSelecionado]
            desenharValorTocado(
                centro = Offset(xs[indiceSelecionado], ys[indiceSelecionado]),
                topoGuia = margemTopo,
                baseGuia = baseY,
                data = textMeasurer.measure(ponto.data.format(FormatoDataToque), estiloData),
                valor = textMeasurer.measure(formatarMoeda(ponto.valor), estiloValor),
                corLinha = corLinha,
                corBalao = corBalao,
                corBorda = corBorda,
                corGuia = corTexto.copy(alpha = 0.55f)
            )
        }
    }
}

/** Linha-guia, marcador e balão com a data e o valor do ponto tocado. */
internal fun DrawScope.desenharValorTocado(
    centro: Offset,
    topoGuia: Float,
    baseGuia: Float,
    data: TextLayoutResult,
    valor: TextLayoutResult,
    corLinha: Color,
    corBalao: Color,
    corBorda: Color,
    corGuia: Color
) {
    drawLine(
        color = corGuia,
        start = Offset(centro.x, topoGuia),
        end = Offset(centro.x, baseGuia),
        strokeWidth = 1.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()))
    )
    drawCircle(color = corBalao, radius = 7.dp.toPx(), center = centro)
    drawCircle(color = corLinha, radius = 4.5.dp.toPx(), center = centro)

    val folga = 8.dp.toPx()
    val distancia = 12.dp.toPx()
    val largura = max(data.size.width, valor.size.width) + folga * 2
    val altura = (data.size.height + valor.size.height).toFloat() + folga * 2
    val esquerda = (centro.x - largura / 2f).coerceIn(0f, max(0f, size.width - largura))
    val acima = centro.y - altura - distancia
    val topo = if (acima >= 0f) acima else (centro.y + distancia).coerceAtMost(max(0f, size.height - altura))
    val raio = CornerRadius(8.dp.toPx())

    drawRoundRect(color = corBalao, topLeft = Offset(esquerda, topo), size = Size(largura, altura), cornerRadius = raio)
    drawRoundRect(
        color = corBorda,
        topLeft = Offset(esquerda, topo),
        size = Size(largura, altura),
        cornerRadius = raio,
        style = Stroke(width = 1.dp.toPx())
    )
    drawText(textLayoutResult = data, topLeft = Offset(esquerda + folga, topo + folga))
    drawText(textLayoutResult = valor, topLeft = Offset(esquerda + folga, topo + folga + data.size.height))
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

@Composable
fun DistribuicaoCarteiraChart(
    categorias: List<String>,
    valores: List<Double>
) {
    val corTexto = colorResource(R.color.text_primary)
    val corTextoSuave = colorResource(R.color.text_secondary)

    val itens = categorias
        .zip(valores)
        .filter { (_, valor) -> valor > 0.0 && !valor.isNaN() && !valor.isInfinite() }

    if (itens.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Sem investimentos para exibir", color = corTextoSuave, fontSize = 13.sp)
        }
        return
    }

    val nomes = itens.map { it.first }
    val dados = itens.map { it.second }
    val total = dados.sum()
    if (total <= 0.0) {
        return
    }

    val modelProducer = remember { PieChartModelProducer() }
    LaunchedEffect(dados) {
        modelProducer.runTransaction {
            pieSeries { series(dados) }
        }
    }

    val slices = remember(nomes.size) {
        nomes.mapIndexed { index, _ ->
            PieChart.Slice(fill = Fill(CoresDistribuicao[index % CoresDistribuicao.size]))
        }
    }
    val sliceProvider = remember(slices) { PieChart.SliceProvider.series(slices) }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 4.dp),
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
        }

        Spacer(modifier = Modifier.size(8.dp))

        Column(
            modifier = Modifier.weight(0.9f),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            nomes.forEachIndexed { index, categoria ->
                val valor = dados[index]
                val percentual = (valor / total) * 100.0

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(CoresDistribuicao[index % CoresDistribuicao.size], CircleShape)
                    )

                    Spacer(modifier = Modifier.size(7.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = categoria,
                            color = corTexto,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = formatarMoeda(valor, casasDecimais = 0),
                            color = corTextoSuave,
                            fontSize = 10.sp
                        )
                    }

                    Text(
                        text = String.format(LocalBR, "%.1f%%", percentual),
                        color = corTexto,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
