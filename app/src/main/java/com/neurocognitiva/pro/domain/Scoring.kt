package com.neurocognitiva.pro.domain

import com.neurocognitiva.pro.data.SessionEntity
import kotlin.math.ln
import kotlin.math.roundToInt
import kotlin.math.sqrt

/** Calidad de una sesión. Las sesiones "excluidas" no entran al índice si existen otras válidas. */
enum class Validity(val code: String, val label: String, val excludesFromIndex: Boolean) {
    OK("OK", "Válida", false),
    CHANCE("AZAR", "Rendimiento compatible con el azar (revisar comprensión de la tarea o fatiga)", true),
    ANTICIPATORY("ANTICIPATORIO", "Respuestas muy rápidas con baja precisión (posible impulsividad)", true),
    INCOMPLETE("INCOMPLETA", "Sesión incompleta: se agotó el tiempo", false);

    companion object {
        fun fromCode(code: String): Validity = entries.firstOrNull { it.code == code } ?: OK
    }
}

enum class Classification(val label: String) {
    SUPERIOR("Superior / Óptimo"),
    NORMAL_ALTO("Normal Alto"),
    NORMAL("Normal Promedio"),
    DESCENSO_LEVE("Descenso Leve"),
    REQUIERE("Requiere Estimulación Focalizada"),
    NO_EVALUADO("No Evaluado Aún")
}

data class DomainMetric(
    val activity: ActivityDef,
    val played: Boolean,
    val sessionsCount: Int = 0,
    /** Nivel más alto jugado. */
    val maxLevel: Int = 0,
    /** Nivel más alto con precisión >= 70%. */
    val masteredLevel: Int = 0,
    /** Precisión media ponderada por nivel (los niveles difíciles pesan más). */
    val accuracy: Int = 0,
    val avgRtMs: Int = 0,
    val indexScore: Int = 0,
    val classification: Classification = Classification.NO_EVALUADO,
    val omissions: Int = 0,
    val commissions: Int = 0,
    val dPrime: Double? = null,
    /** Diferencia de rendimiento entre la última y la primera sesión (puntos). */
    val trend: Int? = null,
    val ceilingNotReached: Boolean = false,
    val invalidSessions: Int = 0
)

data class GlobalMetrics(val icg: Int, val accuracy: Int, val rtMs: Int, val maxLevel: Int, val category: String)

data class Recommendation(val level: Int, val text: String)

/**
 * Motor psicométrico.
 *
 * IMPORTANTE: los umbrales y referencias de velocidad son HEURÍSTICOS. No provienen de una
 * baremación poblacional validada; el índice describe el desempeño relativo al ámbito de
 * aplicación y no debe usarse como diagnóstico.
 */
object Scoring {
    const val MASTERY_ACCURACY = 70
    private const val W_ACCURACY = 0.45
    private const val W_LEVEL = 0.40
    private const val W_SPEED = 0.15

    fun classify(index: Int): Classification = when {
        index >= 88 -> Classification.SUPERIOR
        index >= 75 -> Classification.NORMAL_ALTO
        index >= 60 -> Classification.NORMAL
        index >= 45 -> Classification.DESCENSO_LEVE
        else -> Classification.REQUIERE
    }

    /** 0-100: 100 = tiempo de respuesta "excelente" de la tarea, 0 = tiempo "lento". */
    fun speedScore(activity: ActivityDef, rtMs: Int): Double {
        val span = (activity.rtSlowMs - activity.rtFastMs).toDouble()
        return (100.0 * (activity.rtSlowMs - rtMs) / span).coerceIn(0.0, 100.0)
    }

    /** Rendimiento 0-100 de una sesión (mezcla precisión y nivel superado). Sirve para tendencias. */
    fun sessionPerformance(s: SessionEntity): Int {
        val effectiveLevel = if (s.accuracy >= MASTERY_ACCURACY) s.level else (s.level - 1).coerceAtLeast(0)
        return (0.55 * s.accuracy + 0.45 * (effectiveLevel / 8.0 * 100.0)).roundToInt().coerceIn(0, 100)
    }

    /**
     * @param expectedTopLevel techo de niveles esperable para el ámbito de aplicación y el protocolo
     * (p. ej. Rehabilitación = N4). El componente de nivel se normaliza contra ese techo, de modo
     * que el "baremo por ámbito" sea coherente en lugar de una bonificación fija.
     */
    fun computeMetrics(sessions: List<SessionEntity>, expectedTopLevel: Int): List<DomainMetric> =
        Catalog.activities.map { act ->
            val records = sessions.filter { it.activityId == act.id }.sortedBy { it.timestamp }
            if (records.isEmpty()) return@map DomainMetric(activity = act, played = false)

            val valid = records.filter { !Validity.fromCode(it.validity).excludesFromIndex }
            val base = valid.ifEmpty { records }

            val maxLevel = base.maxOf { it.level }
            val mastered = base.filter { it.accuracy >= MASTERY_ACCURACY }.maxOfOrNull { it.level } ?: 0
            val levelWeight = base.sumOf { it.level }.toDouble()
            val accuracy = (base.sumOf { it.accuracy.toDouble() * it.level } / levelWeight).roundToInt()

            val rts = base.map { it.avgReactionMs }.filter { it > 0 }
            val rt = if (rts.isEmpty()) 0 else rts.average().roundToInt()
            val speed = if (rt == 0) 50.0 else speedScore(act, rt)

            val top = expectedTopLevel.coerceIn(1, Catalog.MAX_LEVEL)
            val levelComponent = (mastered.toDouble() / top).coerceAtMost(1.0) * 100.0
            val index = (W_ACCURACY * accuracy + W_LEVEL * levelComponent + W_SPEED * speed)
                .roundToInt().coerceIn(0, 100)

            val dps = base.mapNotNull { it.dPrime }
            DomainMetric(
                activity = act,
                played = true,
                sessionsCount = records.size,
                maxLevel = maxLevel,
                masteredLevel = mastered,
                accuracy = accuracy,
                avgRtMs = rt,
                indexScore = index,
                classification = classify(index),
                omissions = records.sumOf { it.omissions },
                commissions = records.sumOf { it.commissions },
                dPrime = if (dps.isEmpty()) null else dps.average(),
                trend = if (records.size >= 2) sessionPerformance(records.last()) - sessionPerformance(records.first()) else null,
                ceilingNotReached = mastered > 0 && mastered == maxLevel && maxLevel < top,
                invalidSessions = records.size - valid.size
            )
        }

    fun global(metrics: List<DomainMetric>): GlobalMetrics? {
        val played = metrics.filter { it.played }
        if (played.isEmpty()) return null
        val icg = played.map { it.indexScore }.average().roundToInt()
        val rts = played.map { it.avgRtMs }.filter { it > 0 }
        val category = when {
            icg >= 85 -> "Rendimiento neurocognitivo superior"
            icg >= 72 -> "Rendimiento normal alto"
            icg < 55 -> "Sugiere refuerzo cognitivo"
            else -> "Rango de desempeño esperado"
        }
        return GlobalMetrics(
            icg = icg,
            accuracy = played.map { it.accuracy }.average().roundToInt(),
            rtMs = if (rts.isEmpty()) 0 else rts.average().roundToInt(),
            maxLevel = played.maxOf { it.maxLevel },
            category = category
        )
    }

    /** Escalera adaptativa simple: sube con >= 85 %, baja con < 55 %. */
    fun recommendNext(level: Int, accuracy: Int, validity: Validity): Recommendation = when {
        validity.excludesFromIndex ->
            Recommendation(level, "La sesión no fue concluyente: repite este nivel con buena atención y descanso.")
        accuracy >= 85 && level < Catalog.MAX_LEVEL ->
            Recommendation(level + 1, "Excelente desempeño: se recomienda avanzar al Nivel ${level + 1}.")
        accuracy >= 85 ->
            Recommendation(level, "Nivel máximo dominado. Mantén una práctica periódica.")
        accuracy < 55 && level > 1 ->
            Recommendation(level - 1, "La demanda fue alta: se sugiere consolidar en el Nivel ${level - 1}.")
        else ->
            Recommendation(level, "Se sugiere repetir el nivel hasta superar el 85 % de precisión.")
    }

    /** Precisión esperable respondiendo al azar (null = no aplica). */
    fun chancePct(level: LevelDef): Int? = when (val p = level.params) {
        is CorsiParams -> (100.0 * p.targets / (p.gridSize * p.gridSize)).roundToInt()
        is StroopParams -> 100 / p.optionsCount
        is NBackParams -> 50
        is SdmtParams -> 100 / p.keySize
        is ReasoningParams -> 25
        is CptParams -> 50
        is RotationParams -> 25
        is ArithmeticParams -> 25
        is TrailParams, is HanoiParams -> null
    }

    fun evaluateValidity(
        level: LevelDef,
        hits: Int,
        totalErrors: Int,
        accuracy: Int,
        medianRtMs: Int,
        timedOut: Boolean,
        dPrime: Double?
    ): Validity {
        val attempts = hits + totalErrors
        val chance = chancePct(level)
        if (attempts >= 6) {
            if (chance != null && accuracy <= chance + 8) return Validity.CHANCE
            if (dPrime != null && dPrime < 0.3) return Validity.CHANCE
            if (medianRtMs in 1..199 && accuracy < 70) return Validity.ANTICIPATORY
        }
        return if (timedOut) Validity.INCOMPLETE else Validity.OK
    }

    /** Eficiencia de planificación (Torre): óptimo/movimientos, penalizada por movimientos inválidos. */
    fun planningAccuracy(optimalMoves: Int, moves: Int, invalidMoves: Int): Int {
        if (moves <= 0) return 0
        val efficiency = optimalMoves.toDouble() / maxOf(moves, optimalMoves)
        val validShare = moves.toDouble() / (moves + invalidMoves)
        return (100.0 * efficiency * validShare).roundToInt().coerceIn(0, 100)
    }

    /** Índice de sensibilidad d′ con corrección log-lineal (evita tasas de 0 o 1). */
    fun dPrime(hits: Int, misses: Int, falseAlarms: Int, correctRejections: Int): Double {
        val hitRate = (hits + 0.5) / (hits + misses + 1.0)
        val faRate = (falseAlarms + 0.5) / (falseAlarms + correctRejections + 1.0)
        return probit(hitRate) - probit(faRate)
    }

    /** Inversa de la normal estándar (aproximación de Acklam, error relativo < 1.2e-9). */
    fun probit(p: Double): Double {
        val a = doubleArrayOf(-3.969683028665376e+01, 2.209460984245205e+02, -2.759285104469687e+02, 1.383577518672690e+02, -3.066479806614716e+01, 2.506628277459239e+00)
        val b = doubleArrayOf(-5.447609879822406e+01, 1.615858368580409e+02, -1.556989798598866e+02, 6.680131188771972e+01, -1.328068155288572e+01)
        val c = doubleArrayOf(-7.784894002430293e-03, -3.223964580411365e-01, -2.400758277161838e+00, -2.549732539343734e+00, 4.374664141464968e+00, 2.938163982698783e+00)
        val d = doubleArrayOf(7.784695709041462e-03, 3.224671290700398e-01, 2.445134137142996e+00, 3.754408661907416e+00)
        val pLow = 0.02425
        val pHigh = 1 - pLow
        return when {
            p < pLow -> {
                val q = sqrt(-2 * ln(p))
                (((((c[0] * q + c[1]) * q + c[2]) * q + c[3]) * q + c[4]) * q + c[5]) /
                    ((((d[0] * q + d[1]) * q + d[2]) * q + d[3]) * q + 1)
            }
            p <= pHigh -> {
                val q = p - 0.5
                val r = q * q
                (((((a[0] * r + a[1]) * r + a[2]) * r + a[3]) * r + a[4]) * r + a[5]) * q /
                    (((((b[0] * r + b[1]) * r + b[2]) * r + b[3]) * r + b[4]) * r + 1)
            }
            else -> {
                val q = sqrt(-2 * ln(1 - p))
                -(((((c[0] * q + c[1]) * q + c[2]) * q + c[3]) * q + c[4]) * q + c[5]) /
                    ((((d[0] * q + d[1]) * q + d[2]) * q + d[3]) * q + 1)
            }
        }
    }
}
