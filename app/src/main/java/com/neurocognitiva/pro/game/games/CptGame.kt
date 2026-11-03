package com.neurocognitiva.pro.game.games

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import com.neurocognitiva.pro.domain.CptParams
import com.neurocognitiva.pro.game.GameTracker
import kotlinx.coroutines.delay

private class CptTrial(val isGo: Boolean, val display: String, val bg: Color, val fg: Color)

private fun ruleText(type: Int): String = when (type) {
    1 -> "Pulsa ¡GO! en CÍRCULO VERDE • No pulses nada con CUADRADO ROJO"
    2 -> "Pulsa ¡GO! si aparece una VOCAL (A, E, I, O, U) • No pulses con CONSONANTES"
    3 -> "Pulsa ¡GO! en NÚMEROS PARES • No pulses con NÚMEROS IMPARES"
    4 -> "Paradigma CPT-X: Pulsa ¡GO! con CUALQUIER LETRA, EXCEPTO cuando sea 'X'"
    else -> "Doble condición: Pulsa ¡GO! solo si es un NÚMERO PAR MAYOR A 50"
}

private fun makeTrial(type: Int, isGo: Boolean): CptTrial = when (type) {
    1 -> CptTrial(isGo, if (isGo) "●" else "■", if (isGo) Color(0xFF10B981) else Color(0xFFE11D48), Color.White)
    2 -> CptTrial(
        isGo,
        if (isGo) listOf("A", "E", "I", "O", "U").random() else listOf("B", "D", "F", "G", "K", "M", "P", "R", "T").random(),
        Color(0xFF0D9488), Color.White
    )
    3 -> CptTrial(
        isGo,
        if (isGo) listOf("2", "4", "6", "8", "12", "14", "18").random() else listOf("3", "5", "7", "9", "11", "15", "19").random(),
        Color(0xFF4F46E5), Color.White
    )
    4 -> CptTrial(
        isGo,
        if (isGo) listOf("A", "B", "C", "D", "H", "K", "L", "M", "R", "S").random() else "X",
        Color(0xFF0F172A), Color(0xFF22D3EE)
    )
    else -> CptTrial(
        isGo,
        if (isGo) listOf("52", "58", "64", "76", "82", "94").random() else listOf("24", "38", "46", "53", "67", "79", "85").random(),
        Color(0xFF7E22CE), Color.White
    )
}

/**
 * Actividad 7: CPT Go/No-Go (atención sostenida e inhibición).
 * Mejoras: proporción Go/No-Go exacta (65/35) en vez de aleatoria, estímulo breve con pantalla
 * en blanco, y un único botón ¡GO! (la inhibición real es NO pulsar; el botón "Inhibir" del
 * original convertía la inhibición en una respuesta activa).
 */
@Composable
fun CptGame(tracker: GameTracker, p: CptParams, onFinish: () -> Unit) {
    val trials = remember {
        val goCount = Math.round(p.totalTrials * 0.65f)
        (List(goCount) { true } + List(p.totalTrials - goCount) { false })
            .shuffled()
            .map { makeTrial(p.ruleType, it) }
    }
    var index by remember { mutableIntStateOf(-1) }
    var visible by remember { mutableStateOf(false) }
    var responded by remember { mutableStateOf(false) }
    var stimStart by remember { mutableLongStateOf(0L) }
    var feedback by remember { mutableStateOf("Atento al centro de la pantalla…") }

    LaunchedEffect(Unit) {
        delay(750)
        val visibleMs = (p.intervalMs - 300).coerceIn(300, 800)
        for (i in trials.indices) {
            index = i
            responded = false
            visible = true
            feedback = "Atento…"
            stimStart = SystemClock.elapsedRealtime()
            tracker.round = i + 1
            delay(visibleMs.toLong())
            visible = false
            delay((p.intervalMs - visibleMs).toLong())
            if (!responded) {
                val t = trials[i]
                tracker.signal(t.isGo, false)
                if (t.isGo) tracker.omit() else tracker.hit(30 * tracker.levelNumber)
            }
        }
        onFinish()
    }

    fun press() {
        if (index < 0 || responded) return
        responded = true
        val rt = SystemClock.elapsedRealtime() - stimStart
        val t = trials[index]
        tracker.signal(t.isGo, true)
        if (t.isGo) {
            tracker.hit(35 * tracker.levelNumber, rt)
            feedback = "✓ Correcto"
        } else {
            tracker.commit()
            feedback = "✗ Debías inhibir la respuesta"
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        StatusPill(ruleText(p.ruleType), GameColors.EmeraldDark)
        val current = trials.getOrNull(index)
        Box(
            modifier = Modifier
                .size(176.dp)
                .background(if (visible && current != null) current.bg else Color(0xFFE2E8F0), RoundedCornerShape(28.dp))
                .border(4.dp, GameColors.SlateBorder, RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (visible && current != null) current.display else "•",
                color = if (visible && current != null) current.fg else Color.Gray,
                fontSize = 60.sp,
                fontWeight = FontWeight.Black
            )
        }
        Button(
            onClick = { press() },
            modifier = Modifier.fillMaxWidth().height(76.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488))
        ) {
            Icon(Icons.Default.Bolt, contentDescription = null)
            Text("  ¡PULSAR (GO)!", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        }
        Text(feedback, color = Color.Gray, fontWeight = FontWeight.SemiBold)
    }
}
