package com.example.accurancymobileapp.ui

import android.view.View
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.PointerInputScope
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Acompanha o dedo sobre um gráfico de linha e informa o índice do ponto mais próximo.
 *
 * Arrastando na horizontal, o gráfico segura o toque para a tela não rolar junto;
 * arrastando na vertical, a rolagem da tela continua normal. Quando o dedo sai
 * da tela (ou a rolagem assume o gesto), [aoMudar] recebe null.
 */
internal suspend fun PointerInputScope.acompanharToque(
    view: View,
    quantidadePontos: Int,
    margemHorizontal: Float,
    aoMudar: (Int?) -> Unit
) {
    fun indiceEm(x: Float): Int {
        if (quantidadePontos < 2) return 0
        val passo = (size.width - 2 * margemHorizontal) / (quantidadePontos - 1)
        return ((x - margemHorizontal) / passo).roundToInt().coerceIn(0, quantidadePontos - 1)
    }

    awaitEachGesture {
        val inicio = awaitFirstDown(requireUnconsumed = false)
        aoMudar(indiceEm(inicio.position.x))

        var segurando = false
        try {
            while (true) {
                val evento = awaitPointerEvent()
                val toque = evento.changes.firstOrNull { it.id == inicio.id } ?: break
                if (!toque.pressed) break

                if (!segurando) {
                    val deslocamento = toque.position - inicio.position
                    if (abs(deslocamento.x) > viewConfiguration.touchSlop &&
                        abs(deslocamento.x) > abs(deslocamento.y)
                    ) {
                        segurando = true
                        view.parent?.requestDisallowInterceptTouchEvent(true)
                    }
                }
                if (segurando) toque.consume()
                aoMudar(indiceEm(toque.position.x))
            }
        } finally {
            if (segurando) view.parent?.requestDisallowInterceptTouchEvent(false)
            aoMudar(null)
        }
    }
}
