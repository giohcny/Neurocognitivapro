package com.neurocognitiva.pro.ui

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Radar de perfil cognitivo. [values] en 0..100 (una por etiqueta). */
@Composable
fun RadarChart(
    labels: List<String>,
    values: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary
) {
    val density = LocalDensity.current
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val gridColor = MaterialTheme.colorScheme.outline
    val paint = remember(textColor, density) {
        Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            textSize = with(density) { 10.sp.toPx() }
            color = textColor
            typeface = Typeface.DEFAULT_BOLD
        }
    }
    Canvas(modifier.aspectRatio(1f)) {
        val n = labels.size
        if (n < 3) return@Canvas
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = size.minDimension / 2f * 0.64f

        fun point(i: Int, fraction: Float): Offset {
            val angle = -PI / 2 + 2 * PI * i / n
            return Offset(cx + (radius * fraction * cos(angle)).toFloat(), cy + (radius * fraction * sin(angle)).toFloat())
        }

        listOf(0.25f, 0.5f, 0.75f, 1f).forEach { f ->
            val ring = Path()
            for (i in 0 until n) {
                val o = point(i, f)
                if (i == 0) ring.moveTo(o.x, o.y) else ring.lineTo(o.x, o.y)
            }
            ring.close()
            drawPath(ring, gridColor, style = Stroke(1.dp.toPx()))
        }
        for (i in 0 until n) drawLine(gridColor, Offset(cx, cy), point(i, 1f), 1.dp.toPx())

        val area = Path()
        for (i in 0 until n) {
            val o = point(i, (values.getOrElse(i) { 0f } / 100f).coerceIn(0f, 1f))
            if (i == 0) area.moveTo(o.x, o.y) else area.lineTo(o.x, o.y)
        }
        area.close()
        drawPath(area, lineColor.copy(alpha = 0.22f))
        drawPath(area, lineColor, style = Stroke(2.dp.toPx()))
        for (i in 0 until n) {
            drawCircle(lineColor, 3.5.dp.toPx(), point(i, (values.getOrElse(i) { 0f } / 100f).coerceIn(0f, 1f)))
        }

        drawIntoCanvas { canvas ->
            labels.forEachIndexed { i, label ->
                val o = point(i, 1.22f)
                canvas.nativeCanvas.drawText(label, o.x, o.y + paint.textSize / 3f, paint)
            }
        }
    }
}

/** Línea de evolución 0..100. [labels] se escriben bajo cada punto (p. ej. "N4"). */
@Composable
fun LineChart(
    values: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary
) {
    val density = LocalDensity.current
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
    val paint = remember(textColor, density) {
        Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            textSize = with(density) { 10.sp.toPx() }
            color = textColor
        }
    }
    Canvas(modifier) {
        val left = 30.dp.toPx()
        val right = 12.dp.toPx()
        val top = 12.dp.toPx()
        val bottom = 24.dp.toPx()
        val w = size.width - left - right
        val h = size.height - top - bottom

        drawIntoCanvas { canvas ->
            for (g in 0..4) {
                val y = top + h * (1f - g / 4f)
                drawLine(gridColor, Offset(left, y), Offset(left + w, y), 1.dp.toPx())
                paint.textAlign = Paint.Align.RIGHT
                canvas.nativeCanvas.drawText("${g * 25}", left - 6.dp.toPx(), y + paint.textSize / 3f, paint)
            }
            paint.textAlign = Paint.Align.CENTER
        }
        if (values.isEmpty()) return@Canvas

        val pts = values.mapIndexed { i, v ->
            val x = if (values.size == 1) left + w / 2f else left + w * i / (values.size - 1)
            Offset(x, top + h * (1f - v.coerceIn(0f, 100f) / 100f))
        }
        val path = Path()
        pts.forEachIndexed { i, o -> if (i == 0) path.moveTo(o.x, o.y) else path.lineTo(o.x, o.y) }
        drawPath(path, lineColor, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round))
        pts.forEach { drawCircle(lineColor, 4.dp.toPx(), it) }

        drawIntoCanvas { canvas ->
            pts.forEachIndexed { i, o ->
                if (i < labels.size) canvas.nativeCanvas.drawText(labels[i], o.x, size.height - 6.dp.toPx(), paint)
            }
        }
    }
}
