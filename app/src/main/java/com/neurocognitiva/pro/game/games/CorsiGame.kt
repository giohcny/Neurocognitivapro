package com.neurocognitiva.pro.game.games

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.neurocognitiva.pro.domain.CorsiParams
import com.neurocognitiva.pro.game.GameTracker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private enum class CorsiPhase { Preparing, Showing, Input, Feedback }

/** Actividad 1: Matriz de memoria visoespacial (Corsi). */
@Composable
fun CorsiGame(tracker: GameTracker, p: CorsiParams, onFinish: () -> Unit) {
    val total = p.gridSize * p.gridSize
    val scope = rememberCoroutineScope()
    var targets by remember { mutableStateOf(emptySet<Int>()) }
    var selected by remember { mutableStateOf(emptyList<Int>()) }
    var phase by remember { mutableStateOf(CorsiPhase.Preparing) }
    var lastTap by remember { mutableLongStateOf(0L) }

    // Cada ronda reinicia el ciclo: preparar -> mostrar -> responder
    LaunchedEffect(tracker.round) {
        val set = HashSet<Int>()
        while (set.size < p.targets) set.add(Random.nextInt(total))
        targets = set
        selected = emptyList()
        phase = CorsiPhase.Preparing
        delay(450)
        phase = CorsiPhase.Showing
        delay(p.showTimeMs.toLong())
        phase = CorsiPhase.Input
        lastTap = SystemClock.elapsedRealtime()
    }

    fun onTap(idx: Int) {
        if (phase != CorsiPhase.Input || idx in selected) return
        val now = SystemClock.elapsedRealtime()
        val rt = now - lastTap
        lastTap = now
        selected = selected + idx
        if (idx in targets) tracker.hit(25 * tracker.levelNumber, rt) else tracker.wrongAnswer()
        if (selected.size >= targets.size) {
            phase = CorsiPhase.Feedback
            scope.launch {
                delay(900)
                if (tracker.round < tracker.totalRounds) tracker.round += 1 else onFinish()
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        StatusPill(
            text = when (phase) {
                CorsiPhase.Preparing -> "Prepárate…"
                CorsiPhase.Showing -> "Observa y memoriza el patrón iluminado…"
                CorsiPhase.Input -> "Toca las ${p.targets} casillas que se iluminaron"
                CorsiPhase.Feedback -> "Ronda completada"
            },
            color = if (phase == CorsiPhase.Input) GameColors.EmeraldDark else GameColors.Indigo
        )
        Column(
            modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(p.gridSize) { r ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(p.gridSize) { c ->
                        val idx = r * p.gridSize + c
                        val isTarget = idx in targets
                        val isSelected = idx in selected
                        val (bg, edge) = when {
                            phase == CorsiPhase.Showing && isTarget -> GameColors.Indigo to GameColors.IndigoLight
                            isSelected && isTarget -> GameColors.Emerald to GameColors.EmeraldDark
                            isSelected -> GameColors.Rose to GameColors.RoseDark
                            phase == CorsiPhase.Feedback && isTarget -> GameColors.IndigoSoft to GameColors.Indigo
                            phase == CorsiPhase.Input -> Color.White to GameColors.SlateBorder
                            else -> GameColors.Slate200 to GameColors.SlateBorder
                        }
                        val shape = RoundedCornerShape(12.dp)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(shape)
                                .background(bg)
                                .border(2.dp, edge, shape)
                                .clickable(enabled = phase == CorsiPhase.Input && !isSelected) { onTap(idx) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = if (isTarget) Icons.Default.Check else Icons.Default.Close,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
