package com.neurocognitiva.pro.game

import android.os.SystemClock
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.neurocognitiva.pro.data.SessionEntity
import com.neurocognitiva.pro.domain.ActivityDef
import com.neurocognitiva.pro.domain.ArithmeticParams
import com.neurocognitiva.pro.domain.CorsiParams
import com.neurocognitiva.pro.domain.CptParams
import com.neurocognitiva.pro.domain.HanoiParams
import com.neurocognitiva.pro.domain.LevelDef
import com.neurocognitiva.pro.domain.LevelParams
import com.neurocognitiva.pro.domain.NBackParams
import com.neurocognitiva.pro.domain.ReasoningParams
import com.neurocognitiva.pro.domain.RotationParams
import com.neurocognitiva.pro.domain.Scoring
import com.neurocognitiva.pro.domain.SdmtParams
import com.neurocognitiva.pro.domain.StroopParams
import com.neurocognitiva.pro.domain.TrailParams
import kotlin.math.roundToInt

/**
 * Acumula las métricas de una sesión en curso. Los contadores son estado de Compose,
 * así el HUD se actualiza solo.
 *
 * Criterios (más estrictos que la versión web):
 *  - El tiempo de reacción solo se registra en respuestas CORRECTAS.
 *  - Los ítems sin responder (tiempo agotado) cuentan como omisiones.
 *  - En N-Back y CPT se registran los conteos de teoría de detección de señal para calcular d′.
 */
@Stable
class GameTracker(
    val activity: ActivityDef,
    val level: LevelDef,
    val patientId: Long,
    val inBattery: Boolean
) {
    val levelNumber: Int get() = level.level

    var round by mutableIntStateOf(1)
    var totalRounds by mutableIntStateOf(totalFor(level.params))
    var score by mutableIntStateOf(0)
    var hits by mutableIntStateOf(0)
    var wrong by mutableIntStateOf(0)
    var omissions by mutableIntStateOf(0)
    var commissions by mutableIntStateOf(0)
    var timeRemaining by mutableIntStateOf(-1)
    var avgRtMs by mutableIntStateOf(0)
        private set

    /** Si no es null, reemplaza la precisión calculada (p. ej. eficiencia en la Torre). */
    var accuracyOverride: Int? = null

    var timedOut = false
        private set
    var finished = false
        private set

    private val rts = ArrayList<Long>()
    private val startMs = SystemClock.elapsedRealtime()

    private var usesSdt = false
    private var sdtHits = 0
    private var sdtMisses = 0
    private var sdtFalseAlarms = 0
    private var sdtCorrectRejections = 0

    val totalErrors: Int get() = wrong + omissions + commissions

    fun hit(points: Int, rtMs: Long? = null) {
        hits += 1
        score += points
        if (rtMs != null && rtMs > 0) {
            rts.add(rtMs)
            avgRtMs = rts.average().roundToInt()
        }
    }

    fun wrongAnswer() { wrong += 1 }
    fun omit(count: Int = 1) { omissions += count }
    fun commit() { commissions += 1 }

    /** Registra un ensayo de detección de señal (N-Back / CPT). */
    fun signal(isSignal: Boolean, responded: Boolean) {
        usesSdt = true
        when {
            isSignal && responded -> sdtHits++
            isSignal && !responded -> sdtMisses++
            !isSignal && responded -> sdtFalseAlarms++
            else -> sdtCorrectRejections++
        }
    }

    /** El tiempo se agotó: los ítems pendientes pasan a omisiones. */
    fun timeUp(pendingItems: Int) {
        timedOut = true
        if (pendingItems > 0) omissions += pendingItems
    }

    /** Devuelve true solo la primera vez (evita registrar la sesión dos veces). */
    fun finishOnce(): Boolean {
        if (finished) return false
        finished = true
        return true
    }

    fun buildSession(): SessionEntity {
        val attempts = hits + totalErrors
        val accuracy = accuracyOverride
            ?: if (attempts > 0) (100.0 * hits / attempts).roundToInt() else 0
        val average = if (rts.isEmpty()) 0 else rts.average().roundToInt()
        val median = if (rts.isEmpty()) 0 else rts.sorted()[rts.size / 2].toInt()
        val dPrime = if (usesSdt) Scoring.dPrime(sdtHits, sdtMisses, sdtFalseAlarms, sdtCorrectRejections) else null
        val validity = Scoring.evaluateValidity(level, hits, totalErrors, accuracy, median, timedOut, dPrime)
        return SessionEntity(
            patientId = patientId,
            activityId = activity.id,
            level = level.level,
            score = score,
            accuracy = accuracy,
            avgReactionMs = average,
            hits = hits,
            wrong = wrong,
            omissions = omissions,
            commissions = commissions,
            dPrime = dPrime,
            durationMs = SystemClock.elapsedRealtime() - startMs,
            validity = validity.code,
            inBattery = inBattery,
            simulated = false,
            timestamp = System.currentTimeMillis()
        )
    }

    private companion object {
        fun totalFor(p: LevelParams): Int = when (p) {
            is CorsiParams -> p.rounds
            is StroopParams -> p.trials
            is NBackParams -> p.totalStimuli
            is TrailParams -> p.nodes
            is SdmtParams -> p.targetCount
            is ReasoningParams -> p.items
            is CptParams -> p.totalTrials
            is RotationParams -> p.rounds
            is HanoiParams -> p.optimalMoves
            is ArithmeticParams -> p.items
        }
    }
}
