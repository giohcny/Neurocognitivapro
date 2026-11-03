package com.neurocognitiva.pro.game.games

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

const val SYMBOL_COUNT = 9

/**
 * Símbolos geométricos dibujados con Canvas (en vez de glifos de texto) para que se vean
 * idénticos en cualquier dispositivo y fuente.
 */
@Composable
fun SymbolGlyph(index: Int, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = min(size.width, size.height) * 0.40f
        when (index % SYMBOL_COUNT) {
            0 -> drawCircle(color, r, Offset(cx, cy))
            1 -> drawRect(color, Offset(cx - r, cy - r), Size(2 * r, 2 * r))
            2 -> drawPath(Path().apply {
                moveTo(cx, cy - r); lineTo(cx + r, cy + r); lineTo(cx - r, cy + r); close()
            }, color)
            3 -> drawPath(Path().apply {
                moveTo(cx, cy + r); lineTo(cx + r, cy - r); lineTo(cx - r, cy - r); close()
            }, color)
            4 -> drawPath(Path().apply {
                moveTo(cx, cy - r); lineTo(cx + r, cy); lineTo(cx, cy + r); lineTo(cx - r, cy); close()
            }, color)
            5 -> {
                val t = r * 0.34f
                drawRect(color, Offset(cx - t, cy - r), Size(2 * t, 2 * r))
                drawRect(color, Offset(cx - r, cy - t), Size(2 * r, 2 * t))
            }
            6 -> {
                val w = r * 0.5f
                drawLine(color, Offset(cx - r, cy - r), Offset(cx + r, cy + r), w, StrokeCap.Round)
                drawLine(color, Offset(cx + r, cy - r), Offset(cx - r, cy + r), w, StrokeCap.Round)
            }
            7 -> drawPath(Path().apply {
                for (i in 0 until 10) {
                    val ang = -PI / 2 + i * PI / 5
                    val rad = if (i % 2 == 0) r * 1.1f else r * 0.48f
                    val x = cx + (rad * cos(ang)).toFloat()
                    val y = cy + (rad * sin(ang)).toFloat()
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }, color)
            else -> drawPath(Path().apply {
                for (i in 0 until 6) {
                    val ang = PI / 6 + i * PI / 3
                    val x = cx + (r * cos(ang)).toFloat()
                    val y = cy + (r * sin(ang)).toFloat()
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }, color)
        }
    }
}
