package com.neurocognitiva.pro.game.games

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neurocognitiva.pro.domain.StroopParams
import com.neurocognitiva.pro.game.GameTracker
import com.neurocognitiva.pro.ui.ChoiceGrid
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private class StroopColor(val name: String, val color: Color)

private val STROOP_COLORS = listOf(
    StroopColor("ROJO", Color(0xFFE11D48)),
    StroopColor("AZUL", Color(0xFF2563EB)),
    StroopColor("VERDE", Color(0xFF16A34A)),
    StroopColor("AMARILLO", Color(0xFFCA8A04)),
    StroopColor("MORADO", Color(0xFF9333EA)),
    StroopColor("NARANJA", Color(0xFFEA580C))
)

private class StroopTrial(val word: StroopColor, val ink: StroopColor, val askWord: Boolean) {
    val correct: String get() = if (askWord) word.name else ink.name
}

private fun makeTrial(palette: List<StroopColor>, mixedRule: Boolean): StroopTrial {
    val word = palette.random()
    var ink = palette.random()
    // ~78 % de ensayos incongruentes para exigir inhibición
    if (palette.size > 1 && Random.nextFloat() < 0.78f) {
        ink = palette.filter { it.name != word.name }.random()
    }
    return StroopTrial(word, ink, askWord = mixedRule && Random.nextFloat() < 0.4f)
}

/** Actividad 2: Stroop dinámico (inhibición y cambio de regla). */
@Composable
fun StroopGame(tracker: GameTracker, p: StroopParams, onFinish: () -> Unit) {
    val palette = remember { STROOP_COLORS.take(p.optionsCount) }
    val scope = rememberCoroutineScope()
    var trial by remember { mutableStateOf(makeTrial(palette, p.mixedRule)) }
    var answered by remember { mutableStateOf(false) }
    var startedAt by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }

    fun advance() {
        scope.launch {
            delay(260)
            if (tracker.round < tracker.totalRounds) tracker.round += 1 else onFinish()
        }
    }

    LaunchedEffect(tracker.round) {
        trial = makeTrial(palette, p.mixedRule)
        answered = false
        startedAt = SystemClock.elapsedRealtime()
        delay(p.timePerTrialMs.toLong())
        if (!answered) {          // tiempo agotado = omisión
            answered = true
            tracker.omit()
            advance()
        }
    }

    fun answer(name: String) {
        if (answered) return
        answered = true
        val rt = SystemClock.elapsedRealtime() - startedAt
        if (name == trial.correct) {
            tracker.hit(maxOf(15, 45 * tracker.levelNumber - (rt / 120).toInt()), rt)
        } else {
            tracker.wrongAnswer()
        }
        advance()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        val askWord = trial.askWord
        StatusPill(
            text = "Regla: selecciona el " + if (askWord) "SIGNIFICADO DEL TEXTO" else "COLOR DE LA TINTA",
            color = if (askWord) GameColors.Amber else GameColors.Indigo
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(20.dp))
                .border(2.dp, GameColors.Slate200, RoundedCornerShape(20.dp))
                .padding(vertical = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = trial.word.name,
                color = trial.ink.color,
                fontSize = 44.sp,
                fontWeight = FontWeight.Black
            )
        }
        ChoiceGrid(
            options = palette.map { it.name },
            columns = if (palette.size <= 4) 2 else 3,
            onPick = { idx -> answer(palette[idx].name) }
        )
    }
}
