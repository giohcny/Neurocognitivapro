package com.neurocognitiva.pro.game.games

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.neurocognitiva.pro.game.GameTracker
import kotlinx.coroutines.delay

/** Colores fijos de los estímulos (no cambian con el tema claro/oscuro). */
object GameColors {
    val Indigo = Color(0xFF4F46E5)
    val IndigoLight = Color(0xFF818CF8)
    val IndigoSoft = Color(0xFFC7D2FE)
    val Emerald = Color(0xFF10B981)
    val EmeraldDark = Color(0xFF059669)
    val Rose = Color(0xFFF43F5E)
    val RoseDark = Color(0xFFE11D48)
    val Amber = Color(0xFFD97706)
    val Slate200 = Color(0xFFE2E8F0)
    val SlateBorder = Color(0xFFCBD5E1)
    val Slate800 = Color(0xFF1E293B)
    val Slate900 = Color(0xFF0F172A)
    val Cyan = Color(0xFF22D3EE)
}

/** Cuenta regresiva de las tareas con límite de tiempo. Al llegar a 0 llama a [onTimeUp]. */
@Composable
fun CountdownEffect(tracker: GameTracker, seconds: Int, onTimeUp: () -> Unit) {
    val latest by rememberUpdatedState(onTimeUp)
    LaunchedEffect(Unit) {
        tracker.timeRemaining = seconds
        while (tracker.timeRemaining > 0) {
            delay(1000)
            tracker.timeRemaining -= 1
        }
        if (!tracker.finished) latest()
    }
}

@Composable
fun StatusPill(text: String, color: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = MaterialTheme.shapes.extraLarge, color = color.copy(alpha = 0.14f)) {
        Text(
            text = text,
            color = color,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}
