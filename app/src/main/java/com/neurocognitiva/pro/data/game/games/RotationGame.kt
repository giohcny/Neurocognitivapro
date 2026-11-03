package com.neurocognitiva.pro.game.games

import android.os.SystemClock
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.neurocognitiva.pro.domain.Rotation
import com.neurocognitiva.pro.domain.RotationParams
import com.neurocognitiva.pro.game.GameTracker
import com.neurocognitiva.pro.ui.MiniMatrix
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Actividad 8: Rotación mental de figuras. */
@Composable
fun RotationGame(tracker: GameTracker, p: RotationParams, onFinish: () -> Unit) {
    val scope = rememberCoroutineScope()
    val round = remember(tracker.round) { Rotation.buildRound(p.gridSize, p.cells, p.angles) }
    val startedAt = remember(tracker.round) { SystemClock.elapsedRealtime() }
    var locked by remember(tracker.round) { mutableStateOf(false) }
    var chosen by remember(tracker.round) { mutableIntStateOf(-1) }

    fun choose(i: Int) {
        if (locked) return
        locked = true
        chosen = i
        val rt = SystemClock.elapsedRealtime() - startedAt
        if (round.options[i].correct) tracker.hit(40 * tracker.levelNumber, rt) else tracker.wrongAnswer()
        scope.launch {
            delay(750)
            if (tracker.round < tracker.totalRounds) tracker.round += 1 else onFinish()
        }
    }

    val cell = when (p.gridSize) {
        3 -> 30.dp
        4 -> 26.dp
        5 -> 20.dp
        else -> 17.dp
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        StatusPill("Rota la figura ${round.angle}° en sentido horario", GameColors.Indigo)
        Text("FIGURA MODELO", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        MiniMatrix(round.base, GameColors.Indigo, cell + 4.dp)
        Text("¿Cuál es el resultado? (cuidado con los espejos)", style = MaterialTheme.typography.bodyMedium)
        round.options.chunked(2).forEachIndexed { rowIdx, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEachIndexed { colIdx, option ->
                    val i = rowIdx * 2 + colIdx
                    val edge = when {
                        !locked -> GameColors.SlateBorder
                        option.correct -> GameColors.Emerald
                        i == chosen -> GameColors.Rose
                        else -> GameColors.SlateBorder
                    }
                    Box(
                        modifier = Modifier
                            .border(3.dp, edge, RoundedCornerShape(16.dp))
                            .clickable(enabled = !locked) { choose(i) }
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        MiniMatrix(option.grid, GameColors.Slate800, cell)
                    }
                }
            }
        }
    }
}
