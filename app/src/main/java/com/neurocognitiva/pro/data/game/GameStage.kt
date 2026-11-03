package com.neurocognitiva.pro.game

import androidx.compose.runtime.Composable
import com.neurocognitiva.pro.domain.ArithmeticParams
import com.neurocognitiva.pro.domain.CorsiParams
import com.neurocognitiva.pro.domain.CptParams
import com.neurocognitiva.pro.domain.HanoiParams
import com.neurocognitiva.pro.domain.NBackParams
import com.neurocognitiva.pro.domain.ReasoningParams
import com.neurocognitiva.pro.domain.RotationParams
import com.neurocognitiva.pro.domain.SdmtParams
import com.neurocognitiva.pro.domain.StroopParams
import com.neurocognitiva.pro.domain.TrailParams
import com.neurocognitiva.pro.game.games.ArithmeticGame
import com.neurocognitiva.pro.game.games.CorsiGame
import com.neurocognitiva.pro.game.games.CptGame
import com.neurocognitiva.pro.game.games.HanoiGame
import com.neurocognitiva.pro.game.games.NBackGame
import com.neurocognitiva.pro.game.games.ReasoningGame
import com.neurocognitiva.pro.game.games.RotationGame
import com.neurocognitiva.pro.game.games.SdmtGame
import com.neurocognitiva.pro.game.games.StroopGame
import com.neurocognitiva.pro.game.games.TrailGame

/** Elige el juego según los parámetros del nivel. */
@Composable
fun GameStage(tracker: GameTracker, onFinish: () -> Unit) {
    when (val p = tracker.level.params) {
        is CorsiParams -> CorsiGame(tracker, p, onFinish)
        is StroopParams -> StroopGame(tracker, p, onFinish)
        is NBackParams -> NBackGame(tracker, p, onFinish)
        is TrailParams -> TrailGame(tracker, p, onFinish)
        is SdmtParams -> SdmtGame(tracker, p, onFinish)
        is ReasoningParams -> ReasoningGame(tracker, p, onFinish)
        is CptParams -> CptGame(tracker, p, onFinish)
        is RotationParams -> RotationGame(tracker, p, onFinish)
        is HanoiParams -> HanoiGame(tracker, p, onFinish)
        is ArithmeticParams -> ArithmeticGame(tracker, p, onFinish)
    }
}
