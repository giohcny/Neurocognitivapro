package com.neurocognitiva.pro.game.games

import android.os.SystemClock
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neurocognitiva.pro.domain.TrailParams
import com.neurocognitiva.pro.game.GameTracker
import kotlin.math.hypot
import kotlin.random.Random

private class TrailNode(val label: String, val x: Float, val y: Float)

/** Los tableros densos usan nodos más pequeños para que nunca se superpongan. */
private fun nodeSizeFor(count: Int): Dp = when {
    count <= 12 -> 52.dp
    count <= 16 -> 46.dp
    count <= 20 -> 40.dp
    else -> 36.dp
}

private fun buildNodes(p: TrailParams, floorDist: Float): List<TrailNode> {
    val labels = ArrayList<String>()
    if (!p.alternating) {
        for (i in 1..p.nodes) labels.add(i.toString())
    } else {
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        var number = 1
        var letter = 0
        for (i in 0 until p.nodes) {
            if (i % 2 == 0) labels.add((number++).toString()) else labels.add(alphabet[letter++].toString())
        }
    }
    // Posiciones en un cuadrado unitario sin superposición (la distancia mínima se relaja si hace falta)
    val points = ArrayList<Pair<Float, Float>>()
    var minDist = maxOf(0.24f, floorDist)
    var tries = 0
    while (points.size < p.nodes && tries < 6000) {
        tries++
        val x = 0.09f + Random.nextFloat() * 0.82f
        val y = 0.09f + Random.nextFloat() * 0.82f
        if (points.all { hypot(it.first - x, it.second - y) >= minDist }) points.add(x to y)
        if (tries % 200 == 0) minDist = maxOf(floorDist, minDist * 0.9f)
    }
    while (points.size < p.nodes) points.add(Random.nextFloat() to Random.nextFloat())
    return labels.mapIndexed { i, label -> TrailNode(label, points[i].first, points[i].second) }
}

/** Actividad 4: Trail Making A/B (flexibilidad cognitiva). */
@Composable
fun TrailGame(tracker: GameTracker, p: TrailParams, onFinish: () -> Unit) {
    val nodeSize = nodeSizeFor(p.nodes)
    // Separación mínima (en fracción del tablero, ~320 dp de ancho) = tamaño del nodo + margen
    val nodes = remember { buildNodes(p, (nodeSize.value + 2f) / 320f) }
    var expected by remember { mutableIntStateOf(0) }
    var lastTap by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    var hint by remember { mutableStateOf("") }

    CountdownEffect(tracker, p.timeLimitSec) {
        tracker.timeUp(nodes.size - expected)
        onFinish()
    }

    fun tap(i: Int) {
        if (i < expected) return
        val now = SystemClock.elapsedRealtime()
        val rt = now - lastTap
        lastTap = now
        if (i == expected) {
            expected += 1
            hint = ""
            tracker.round = minOf(tracker.totalRounds, expected + 1)
            tracker.hit(30 * tracker.levelNumber, rt)
            if (expected >= nodes.size) onFinish()
        } else {
            tracker.wrongAnswer()
            hint = "Debes pulsar \"${nodes[expected].label}\""
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        StatusPill(
            text = (if (p.alternating) "TMT-B: 1 → A → 2 → B…" else "TMT-A: 1 → 2 → 3…") +
                "   Siguiente: " + (nodes.getOrNull(expected)?.label ?: "—"),
            color = GameColors.Amber
        )
        if (hint.isNotEmpty()) Text(hint, color = GameColors.RoseDark, fontWeight = FontWeight.SemiBold)

        BoxWithConstraints(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .aspectRatio(1f / 1.1f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(2.dp, GameColors.Slate200, RoundedCornerShape(20.dp))
        ) {
            val w = maxWidth
            val h = maxHeight
            Canvas(Modifier.fillMaxSize()) {
                for (i in 1 until expected) {
                    val a = nodes[i - 1]
                    val b = nodes[i]
                    drawLine(
                        color = GameColors.Emerald,
                        start = Offset(size.width * a.x, size.height * a.y),
                        end = Offset(size.width * b.x, size.height * b.y),
                        strokeWidth = 3.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 8f))
                    )
                }
            }
            nodes.forEachIndexed { i, node ->
                val done = i < expected
                Box(
                    modifier = Modifier
                        .offset(x = w * node.x - nodeSize / 2, y = h * node.y - nodeSize / 2)
                        .size(nodeSize)
                        .clip(CircleShape)
                        .background(if (done) GameColors.EmeraldDark else Color.White)
                        .border(2.dp, if (done) GameColors.EmeraldDark else GameColors.Slate800, CircleShape)
                        .clickable { tap(i) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = node.label,
                        color = if (done) Color.White else GameColors.Slate900,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = if (nodeSize < 40.dp) 13.sp else 15.sp
                    )
                }
            }
        }
    }
}
