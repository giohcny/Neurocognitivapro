package com.neurocognitiva.pro.domain

import com.neurocognitiva.pro.data.SessionEntity
import kotlin.random.Random

/** Genera sesiones SIMULADAS (marcadas con simulated = true) para probar el informe. */
object DemoData {
    fun sessions(patientId: Long): List<SessionEntity> {
        val rnd = Random(patientId * 31 + System.currentTimeMillis() / 60_000)
        val now = System.currentTimeMillis()
        val day = 86_400_000L
        return Catalog.activities.flatMap { act ->
            val top = 4 + rnd.nextInt(5)
            (0 until 3).map { i ->
                val level = (top - 2 + i).coerceIn(1, 8)
                val accuracy = (94 - (level - 1) * 2 + rnd.nextInt(-6, 7)).coerceIn(45, 100)
                val items = 20
                val hits = accuracy * items / 100
                val usesDprime = act.id == "nback_memory" || act.id == "cpt_sustained"
                SessionEntity(
                    patientId = patientId,
                    activityId = act.id,
                    level = level,
                    score = hits * 30 * level,
                    accuracy = accuracy,
                    avgReactionMs = (act.rtFastMs * (1.1 + rnd.nextDouble() * 0.9)).toInt(),
                    hits = hits,
                    wrong = (items - hits) / 2,
                    omissions = (items - hits) - (items - hits) / 2,
                    commissions = 0,
                    dPrime = if (usesDprime) 1.2 + rnd.nextDouble() * 2.0 else null,
                    durationMs = 60_000L,
                    validity = Validity.OK.code,
                    inBattery = false,
                    simulated = true,
                    timestamp = now - (2 - i) * day - rnd.nextInt(3_600_000)
                )
            }
        }
    }
}
