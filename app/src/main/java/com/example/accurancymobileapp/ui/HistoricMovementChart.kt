package com.example.accurancymobileapp.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material3.Text
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.ceil
import kotlin.math.max

private val FundoHistorico = Color(0xFF0D1D30)
private val AzulCompra = Color(0xFF3B82F6)
private val VermelhoVenda = Color(0xFFFF7278)
private val TextoHistorico = Color(0xFF91A4BD)
private val GradeHistorico = Color(0x1FFFFFFF)

/** Gráfico animado de volume de compras e vendas por data/período. */
@Composable
fun HistoricMovementChart(
    labels: List<String>,
    compras: List<Double>,
    vendas: List<Double>,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }
    val maior = remember(labels, compras, vendas) {
        (compras + vendas).filter { it.isFinite() && it > 0.0 }.maxOrNull() ?: 0.0
    }
    LaunchedEffect(labels, compras, vendas) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(durationMillis = 700))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(14.dp))
            .background(FundoHistorico)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.size(7.dp).clip(CircleShape).background(AzulCompra))
                Text("Compras", color = TextoHistorico, fontSize = 10.sp, modifier = Modifier.padding(start = 5.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.size(7.dp).clip(CircleShape).background(VermelhoVenda))
                Text("Vendas", color = TextoHistorico, fontSize = 10.sp, modifier = Modifier.padding(start = 5.dp))
            }
        }

        if (labels.isEmpty() || maior <= 0.0) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("Sem compras ou vendas neste período", color = TextoHistorico, fontSize = 11.sp)
            }
        } else {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 6.dp)
            ) {
                val top = 3.dp.toPx()
                val bottom = 3.dp.toPx()
                val usefulHeight = max(1f, size.height - top - bottom)
                val usefulWidth = max(1f, size.width)
                val grupo = usefulWidth / labels.size
                val barWidth = minOf(10.dp.toPx(), grupo * 0.27f).coerceAtLeast(2.dp.toPx())
                val gap = 2.dp.toPx()

                for (i in 0..3) {
                    val y = top + usefulHeight * i / 3f
                    drawLine(GradeHistorico, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
                }

                labels.indices.forEach { index ->
                    val centerX = grupo * index + grupo / 2f
                    val buy = compras.getOrNull(index)?.takeIf { it.isFinite() && it > 0.0 } ?: 0.0
                    val sell = vendas.getOrNull(index)?.takeIf { it.isFinite() && it > 0.0 } ?: 0.0
                    val buyHeight = (buy / maior).toFloat() * usefulHeight * progress.value
                    val sellHeight = (sell / maior).toFloat() * usefulHeight * progress.value
                    val baseline = size.height - bottom

                    if (buyHeight > 0f) {
                        drawRoundRect(
                            color = AzulCompra,
                            topLeft = Offset(centerX - gap / 2f - barWidth, baseline - buyHeight),
                            size = Size(barWidth, buyHeight),
                            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                        )
                    }
                    if (sellHeight > 0f) {
                        drawRoundRect(
                            color = VermelhoVenda,
                            topLeft = Offset(centerX + gap / 2f, baseline - sellHeight),
                            size = Size(barWidth, sellHeight),
                            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                        )
                    }
                }
            }

            val intervalo = max(1, ceil(labels.size / 6.0).toInt())
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val indices = labels.indices.filter { it % intervalo == 0 || it == labels.lastIndex }
                indices.forEach { index ->
                    Text(labels[index], color = TextoHistorico, fontSize = 9.sp, maxLines = 1)
                }
            }
        }
    }
}
