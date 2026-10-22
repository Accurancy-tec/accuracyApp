package com.example.accurancymobileapp.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max

private val FundoGrafico = Color(0xFF0B1A2D)
private val VerdeGrafico = Color(0xFF34D399)
private val TextoGrafico = Color(0xFF91A4BD)
private val GradeGrafico = Color(0x1FFFFFFF)
private val MoedaBR = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))

@Composable
fun StockPriceChart(
    labels: List<String>,
    values: List<Double>,
    selectedRange: String,
    loading: Boolean = false,
    error: String? = null,
    modifier: Modifier = Modifier
) {
    val pontos = remember(labels, values) {
        labels.zip(values).filter { (_, value) -> value.isFinite() && value > 0.0 }
    }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(pontos, selectedRange) {
        progress.snapTo(0f)
        if (pontos.size >= 2) progress.animateTo(1f, animationSpec = tween(durationMillis = 950))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(FundoGrafico)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "HISTÓRICO DE PREÇOS",
                color = TextoGrafico,
                fontSize = 10.sp,
                letterSpacing = 1.2.sp
            )
            Text(
                text = selectedRange,
                color = VerdeGrafico,
                fontSize = 11.sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(142.dp)
                .padding(top = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                loading -> CircularProgressIndicator(color = VerdeGrafico, strokeWidth = 2.dp)
                !error.isNullOrBlank() -> Text(
                    text = error,
                    color = TextoGrafico,
                    fontSize = 12.sp
                )
                pontos.isEmpty() -> Text(
                    text = "Histórico indisponível para este período",
                    color = TextoGrafico,
                    fontSize = 12.sp
                )
                pontos.size == 1 -> Text(
                    text = "É necessário mais de um ponto para desenhar o gráfico.",
                    color = TextoGrafico,
                    fontSize = 12.sp
                )
                else -> Canvas(Modifier.fillMaxSize()) {
                    val margemEsquerda = 4.dp.toPx()
                    val margemDireita = 4.dp.toPx()
                    val margemTopo = 8.dp.toPx()
                    val margemBase = 8.dp.toPx()
                    val larguraUtil = max(1f, size.width - margemEsquerda - margemDireita)
                    val alturaUtil = max(1f, size.height - margemTopo - margemBase)

                    val precos = pontos.map { it.second }
                    val menor = precos.minOrNull() ?: 0.0
                    val maior = precos.maxOrNull() ?: 0.0
                    val intervalo = maior - menor
                    val folga = if (intervalo == 0.0) max(maior * 0.02, 0.01) else intervalo * 0.12
                    val escalaMin = menor - folga
                    val escalaMax = maior + folga
                    val escalaIntervalo = max(0.000001, escalaMax - escalaMin)
                    val passoX = if (pontos.size > 1) larguraUtil / (pontos.size - 1) else 0f

                    val coordenadas = pontos.mapIndexed { index, ponto ->
                        val frac = ((ponto.second - escalaMin) / escalaIntervalo).toFloat().coerceIn(0f, 1f)
                        Offset(
                            x = margemEsquerda + index * passoX,
                            y = margemTopo + alturaUtil * (1f - frac)
                        )
                    }

                    // Grade discreta para leitura do valor sem poluir o visual.
                    for (i in 0..3) {
                        val y = margemTopo + alturaUtil * i / 3f
                        drawLine(
                            color = GradeGrafico,
                            start = Offset(margemEsquerda, y),
                            end = Offset(size.width - margemDireita, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    val linha = Path().apply {
                        moveTo(coordenadas.first().x, coordenadas.first().y)
                        for (i in 1 until coordenadas.size) {
                            val anterior = coordenadas[i - 1]
                            val atual = coordenadas[i]
                            val meioX = (anterior.x + atual.x) / 2f
                            cubicTo(meioX, anterior.y, meioX, atual.y, atual.x, atual.y)
                        }
                    }
                    val area = Path().apply {
                        moveTo(coordenadas.first().x, size.height - margemBase)
                        lineTo(coordenadas.first().x, coordenadas.first().y)
                        for (i in 1 until coordenadas.size) {
                            val anterior = coordenadas[i - 1]
                            val atual = coordenadas[i]
                            val meioX = (anterior.x + atual.x) / 2f
                            cubicTo(meioX, anterior.y, meioX, atual.y, atual.x, atual.y)
                        }
                        lineTo(coordenadas.last().x, size.height - margemBase)
                        close()
                    }

                    val limiteDireito = margemEsquerda + larguraUtil * progress.value
                    clipRect(
                        left = 0f,
                        top = 0f,
                        right = limiteDireito.coerceAtLeast(0f),
                        bottom = size.height
                    ) {
                        drawPath(
                            path = area,
                            brush = Brush.verticalGradient(
                                colors = listOf(VerdeGrafico.copy(alpha = 0.25f), VerdeGrafico.copy(alpha = 0.015f), Color.Transparent),
                                startY = margemTopo,
                                endY = size.height - margemBase
                            )
                        )
                        drawPath(
                            path = linha,
                            color = VerdeGrafico,
                            style = Stroke(width = 2.7.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawCircle(
                            color = VerdeGrafico,
                            radius = 4.dp.toPx(),
                            center = coordenadas.last()
                        )
                        drawCircle(
                            color = VerdeGrafico.copy(alpha = 0.18f),
                            radius = 8.dp.toPx(),
                            center = coordenadas.last()
                        )
                    }
                }
            }
        }

        if (!loading && error.isNullOrBlank() && pontos.isNotEmpty()) {
            val primeiro = pontos.first()
            val ultimo = pontos.last()
            val variacao = if (primeiro.second != 0.0) ((ultimo.second - primeiro.second) / primeiro.second) * 100.0 else 0.0
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = formatarDataCurta(primeiro.first), color = TextoGrafico, fontSize = 10.sp)
                    Text(text = MoedaBR.format(primeiro.second), color = Color.White, fontSize = 11.sp)
                }
                Text(
                    text = String.format(Locale.forLanguageTag("pt-BR"), "%+.2f%%", variacao),
                    color = if (variacao >= 0.0) VerdeGrafico else Color(0xFFFF7278),
                    fontSize = 11.sp
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = formatarDataCurta(ultimo.first), color = TextoGrafico, fontSize = 10.sp)
                    Text(text = MoedaBR.format(ultimo.second), color = Color.White, fontSize = 11.sp)
                }
            }
        }
    }
}

private fun formatarDataCurta(data: String): String {
    return runCatching {
        LocalDate.parse(data).format(DateTimeFormatter.ofPattern("dd MMM", Locale.forLanguageTag("pt-BR")))
    }.getOrDefault(data)
}
