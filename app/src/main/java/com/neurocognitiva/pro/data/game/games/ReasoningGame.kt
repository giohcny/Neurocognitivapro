package com.neurocognitiva.pro.game.games

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neurocognitiva.pro.domain.ReasoningBank
import com.neurocognitiva.pro.domain.ReasoningParams
import com.neurocognitiva.pro.game.GameTracker
import com.neurocognitiva.pro.ui.ChoiceGrid

private class ReasoningQuestion(val text: String, val options: List<String>, val answer: Int)

/** Actividad 6: Razonamiento semántico, analógico y lógico. */
@Composable
fun ReasoningGame(tracker: GameTracker, p: ReasoningParams, onFinish: () -> Unit) {
    // Mejora: se baraja el orden de las preguntas y de las opciones (en el original la
    // respuesta correcta siempre caía en la misma posición).
    val questions = remember {
        ReasoningBank.forTier(p.tier).shuffled().take(p.items).map { q ->
            val order = q.options.indices.shuffled()
            ReasoningQuestion(q.question, order.map { q.options[it] }, order.indexOf(q.answerIndex))
        }
    }
    var index by remember { mutableIntStateOf(0) }
    var startedAt by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }

    CountdownEffect(tracker, p.timeLimitSec) {
        tracker.timeUp(questions.size - index)
        onFinish()
    }

    fun answer(choice: Int) {
        val rt = SystemClock.elapsedRealtime() - startedAt
        if (choice == questions[index].answer) tracker.hit(40 * tracker.levelNumber, rt) else tracker.wrongAnswer()
        if (index + 1 >= questions.size) {
            onFinish()
            return
        }
        index += 1
        tracker.round = index + 1
        startedAt = SystemClock.elapsedRealtime()
    }

    val q = questions[index]
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        StatusPill("Pregunta ${index + 1} de ${questions.size}", GameColors.Indigo)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(20.dp))
                .border(2.dp, GameColors.Slate200, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Text(
                text = q.text,
                color = GameColors.Slate900,
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        ChoiceGrid(
            options = q.options.mapIndexed { i, text -> "${'A' + i}) $text" },
            columns = 1,
            onPick = { answer(it) }
        )
    }
}
