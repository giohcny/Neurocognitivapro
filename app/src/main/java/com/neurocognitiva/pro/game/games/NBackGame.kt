package com.neurocognitiva.pro.game.games

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neurocognitiva.pro.domain.NBackParams
import com.neurocognitiva.pro.game.GameTracker
import kotlinx.coroutines.delay
import kotlin.random.Random

private val NBACK_LETTERS = listOf("A", "B", "D", "E", "G", "H", "K", "M", "P", "R", "T")

/** Actividad 3: N-Back (actualización de memoria de trabajo). */
@Composable
fun NBackGame(tracker: GameTracker, p: NBackParams, onFinish: () -> Unit) {
    val sequence = remember {
        val seq = ArrayList<String>()
        for (i in 0 until p.totalStimuli) {
            if (i >= p.n && Random.nextFloat() < 0.38f) seq.add(seq[i - p.n]) else seq.add(NBACK_LETTERS.random())
        }
        seq.toList()
    }
    var index by remember { mutableIntStateOf(-1) }
    var responded by remember { mutableStateOf(false) }
    var stimStart by remember { mutableLongStateOf(0L) }
    var feedback by remember { mutableStateOf("Preparando secuencia…") }
    var feedbackOk by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        delay(700)
        for (i in sequence.indices) {
            index = i
            responded = false
            feedbackOk = null
            stimStart = SystemClock.elapsedRealtime()
            tracker.round = i + 1
            feedback = if (i < p.n) "Memoriza este estímulo (${i + 1} de ${p.n})…"
            else "¿Es igual a la de hace ${p.n} ${if (p.n == 1) "turno" else "turnos"}?"
            delay(p.intervalMs.toLong())
            // Sin respuesta: equivale a "no coincide"
            if (i >= p.n && !responded) {
                val isMatch = sequence[i] == sequence[i - p.n]
                tracker.signal(isMatch, false)
                if (isMatch) tracker.omit() else tracker.hit(15 * tracker.levelNumber)
            }
        }
        onFinish()
    }

    fun respond(saysMatch: Boolean) {
        if (index < p.n || responded) return
        responded = true
        val rt = SystemClock.elapsedRealtime() - stimStart
        val isMatch = sequence[index] == sequence[index - p.n]
        tracker.signal(isMatch, saysMatch)
        if (saysMatch == isMatch) {
            tracker.hit(35 * tracker.levelNumber, rt)
            feedback = "✓ Respuesta correcta"
            feedbackOk = true
        } else {
            if (saysMatch) tracker.commit() else tracker.omit()
            feedback = "✗ Error de actualización"
            feedbackOk = false
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        StatusPill(
            text = "${p.n}-Back: ¿la letra actual es igual a la de hace ${p.n} ${if (p.n == 1) "posición" else "posiciones"}?",
            color = GameColors.EmeraldDark
        )
        Box(
            modifier = Modifier
                .size(170.dp)
                .background(if (index >= 0) Color(0xFFEEF2FF) else Color.White, RoundedCornerShape(28.dp))
                .border(3.dp, if (index >= 0) GameColors.Indigo else GameColors.SlateBorder, RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (index >= 0) sequence[index] else "•",
                fontSize = 64.sp,
                fontWeight = FontWeight.Black,
                color = GameColors.Slate900
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { respond(true) },
                modifier = Modifier.weight(1f).height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GameColors.EmeraldDark)
            ) { Text("¡COINCIDE!", fontWeight = FontWeight.Bold) }
            OutlinedButton(
                onClick = { respond(false) },
                modifier = Modifier.weight(1f).height(60.dp)
            ) { Text("No coincide", fontWeight = FontWeight.Bold) }
        }
        Text(
            text = feedback,
            color = when (feedbackOk) {
                true -> GameColors.EmeraldDark
                false -> GameColors.RoseDark
                null -> Color.Gray
            },
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
    }
}
