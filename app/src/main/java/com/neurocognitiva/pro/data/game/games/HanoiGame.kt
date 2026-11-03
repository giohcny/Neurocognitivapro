package com.neurocognitiva.pro.game.games

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neurocognitiva.pro.domain.HanoiParams
import com.neurocognitiva.pro.domain.Scoring
import com.neurocognitiva.pro.game.GameTracker
import kotlin.math.roundToInt

private val DISK_COLORS = listOf(
    Color(0xFFF43F5E), Color(0xFFF59E0B), Color(0xFF10B981), Color(0xFF3B82F6), Color(0xFF8B5CF6), Color(0xFFEC4899)
)

/**
 * Actividad 9: Torre de Hanoi (planificación).
 * Mejora: la precisión mide la EFICIENCIA (óptimo / movimientos) y no solo la validez de las
 * jugadas; en el original, resolver en 40 movimientos una torre de 7 daba 100 %.
 */
@Composable
fun HanoiGame(tracker: GameTracker, p: HanoiParams, onFinish: () -> Unit) {
    var pegs by remember { mutableStateOf(listOf((p.disks downTo 1).toList(), emptyList<Int>(), emptyList<Int>())) }
    var selected by remember { mutableStateOf<Int?>(null) }
    var moves by remember { mutableIntStateOf(0) }
    var invalid by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf("Toca una torre para tomar su disco superior.") }
    var lastMove by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    val names = listOf("Torre A", "Torre B", "Torre C")

    fun onPeg(i: Int) {
        val from = selected
        if (from == null) {
            if (pegs[i].isEmpty()) {
                message = "Esa torre está vacía."
            } else {
                selected = i
                message = "Disco D${pegs[i].last()} seleccionado: elige la torre destino."
            }
            return
        }
        if (from == i) {
            selected = null
            message = "Selección cancelada."
            return
        }
        val disk = pegs[from].last()
        val target = pegs[i]
        if (target.isEmpty() || disk < target.last()) {
            val next = pegs.toMutableList()
            next[from] = pegs[from].dropLast(1)
            next[i] = target + disk
            pegs = next
            moves += 1
            selected = null
            val now = SystemClock.elapsedRealtime()
            tracker.hit(0, now - lastMove)
            lastMove = now
            tracker.round = moves
            message = "Movimiento válido."
            if (pegs[p.targetPeg].size == p.disks) {
                val efficiency = p.optimalMoves.toDouble() / maxOf(moves, p.optimalMoves)
                tracker.accuracyOverride = Scoring.planningAccuracy(p.optimalMoves, moves, invalid)
                tracker.score = maxOf(0, (300 * tracker.levelNumber * efficiency).roundToInt() - invalid * 10)
                onFinish()
            }
        } else {
            invalid += 1
            tracker.wrongAnswer()
            selected = null
            message = "Movimiento inválido: no puedes apoyar un disco grande sobre uno más pequeño."
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        StatusPill(
            "Meta: mover los ${p.disks} discos a la ${names[p.targetPeg]} (mínimo posible: ${p.optimalMoves} movimientos)",
            GameColors.Indigo
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            repeat(3) { i ->
                val isTarget = i == p.targetPeg
                val isSelected = selected == i
                val shape = RoundedCornerShape(16.dp)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(240.dp)
                        .clip(shape)
                        .background(if (isSelected) Color(0xFFEEF2FF) else Color.White)
                        .border(
                            width = if (isSelected || isTarget) 3.dp else 2.dp,
                            color = when {
                                isSelected -> GameColors.Indigo
                                isTarget -> GameColors.Emerald
                                else -> GameColors.Slate200
                            },
                            shape = shape
                        )
                        .clickable { onPeg(i) },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        Modifier
                            .width(8.dp)
                            .fillMaxHeight(0.82f)
                            .background(GameColors.SlateBorder, RoundedCornerShape(4.dp))
                    )
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        pegs[i].reversed().forEach { d ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.26f + d * 0.14f)
                                    .height(24.dp)
                                    .background(DISK_COLORS[(d - 1) % DISK_COLORS.size], RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("D$d", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Text(
                        text = names[i] + if (isTarget) " ★" else "",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isTarget) GameColors.EmeraldDark else Color.Gray,
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp)
                    )
                }
            }
        }
        Text(
            text = "Movimientos: $moves · Inválidos: $invalid",
            fontWeight = FontWeight.Bold
        )
        Text(message, color = if (message.startsWith("Movimiento inválido")) GameColors.RoseDark else Color.Gray)
    }
}
