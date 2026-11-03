package com.neurocognitiva.pro.game.games

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neurocognitiva.pro.domain.SdmtParams
import com.neurocognitiva.pro.game.GameTracker
import com.neurocognitiva.pro.ui.ChoiceGrid
import kotlin.random.Random

/** Actividad 5: Símbolo-Dígito (velocidad de procesamiento). */
@Composable
fun SdmtGame(tracker: GameTracker, p: SdmtParams, onFinish: () -> Unit) {
    // digits[i] = dígito asociado al símbolo i
    var digits by remember { mutableStateOf((1..p.keySize).toList()) }
    var target by remember { mutableIntStateOf(Random.nextInt(p.keySize)) }
    var answered by remember { mutableIntStateOf(0) }
    var trialStart by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    var keyChanged by remember { mutableStateOf(false) }

    CountdownEffect(tracker, p.durationSec) {
        tracker.timeUp(p.targetCount - answered)
        onFinish()
    }

    fun answer(digit: Int) {
        val rt = SystemClock.elapsedRealtime() - trialStart
        if (digit == digits[target]) tracker.hit(25 * tracker.levelNumber, rt) else tracker.wrongAnswer()
        answered += 1
        if (answered >= p.targetCount) {
            onFinish()
            return
        }
        tracker.round = answered + 1
        keyChanged = false
        // En niveles altos la clave se reordena cada 4 ítems
        if (p.shuffleKey && answered % 4 == 0) {
            digits = digits.shuffled()
            keyChanged = true
        }
        target = Random.nextInt(p.keySize)
        trialStart = SystemClock.elapsedRealtime()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("CLAVE: SÍMBOLO = DÍGITO", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(GameColors.Slate900, RoundedCornerShape(16.dp))
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(p.keySize) { i ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(GameColors.Slate800, RoundedCornerShape(10.dp))
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SymbolGlyph(i, GameColors.Cyan, Modifier.size(26.dp))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF475569)))
                    Text(digits[i].toString(), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }
        }
        if (keyChanged) {
            StatusPill("¡La clave cambió! Revísala", GameColors.Amber)
        }
        Text("¿Qué dígito corresponde a este símbolo?", style = MaterialTheme.typography.bodyMedium)
        Box(
            modifier = Modifier
                .size(112.dp)
                .background(Color(0xFFECFEFF), RoundedCornerShape(20.dp))
                .border(2.dp, Color(0xFF06B6D4), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            SymbolGlyph(target, Color(0xFF0E7490), Modifier.size(70.dp))
        }
        ChoiceGrid(
            options = (1..p.keySize).map { it.toString() },
            columns = minOf(5, p.keySize),
            onPick = { idx -> answer(idx + 1) }
        )
    }
}
