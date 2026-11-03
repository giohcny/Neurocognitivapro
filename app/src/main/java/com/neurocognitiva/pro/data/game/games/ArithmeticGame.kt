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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neurocognitiva.pro.domain.Arithmetic
import com.neurocognitiva.pro.domain.ArithmeticParams
import com.neurocognitiva.pro.game.GameTracker
import com.neurocognitiva.pro.ui.ChoiceGrid

/** Actividad 10: Fluidez numérica y cálculo mental. */
@Composable
fun ArithmeticGame(tracker: GameTracker, p: ArithmeticParams, onFinish: () -> Unit) {
    val items = remember { List(p.items) { Arithmetic.generate(p.tier) } }
    var index by remember { mutableIntStateOf(0) }
    var startedAt by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }

    CountdownEffect(tracker, p.timeLimitSec) {
        tracker.timeUp(items.size - index)
        onFinish()
    }

    fun answer(option: Int) {
        val rt = SystemClock.elapsedRealtime() - startedAt
        if (option == items[index].answer) tracker.hit(40 * tracker.levelNumber, rt) else tracker.wrongAnswer()
        if (index + 1 >= items.size) {
            onFinish()
            return
        }
        index += 1
        tracker.round = index + 1
        startedAt = SystemClock.elapsedRealtime()
    }

    val item = items[index]
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        StatusPill("Operación ${index + 1} de ${items.size}", GameColors.Indigo)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(20.dp))
                .border(2.dp, GameColors.Slate200, RoundedCornerShape(20.dp))
                .padding(vertical = 36.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.expr,
                color = GameColors.Slate900,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        }
        ChoiceGrid(
            options = item.options.map { it.toString() },
            columns = 2,
            onPick = { idx -> answer(item.options[idx]) }
        )
    }
}
