package com.neurocognitiva.pro.domain

/** Estado de una "Evaluación guiada": recorre las 10 áreas con nivel adaptativo. */
data class BatteryState(
    val plan: List<String>,
    val index: Int,
    val level: Int,
    val attempts: Int,
    val startLevel: Int,
    val maxLevel: Int
) {
    val activityId: String get() = plan[index]
    val total: Int get() = plan.size
}

object Battery {
    val ORDER = listOf(
        "corsi_matrix", "stroop_inhibition", "nback_memory", "trail_flexibility", "sdmt_speed",
        "semantic_reasoning", "cpt_sustained", "mental_rotation", "planning_hanoi", "arithmetic_fluency"
    )

    /** Parte en el primer nivel sugerido del ámbito y no supera el máximo del protocolo. */
    fun start(appTier: String, protocol: String): BatteryState {
        val maxLevel = Protocols.maxLevel(protocol)
        val start = Tiers.range(appTier).first.coerceAtMost(maxLevel)
        return BatteryState(ORDER, 0, start, 0, start, maxLevel)
    }

    /** Devuelve el siguiente paso o null si la batería terminó. Sube de nivel (máx. 2 veces) con >= 80 %. */
    fun advance(state: BatteryState, accuracy: Int, validity: Validity): BatteryState? {
        val canClimb = accuracy >= 80 && !validity.excludesFromIndex &&
            state.level < state.maxLevel && state.attempts < 2
        return when {
            canClimb -> state.copy(level = state.level + 1, attempts = state.attempts + 1)
            state.index + 1 < state.plan.size ->
                state.copy(index = state.index + 1, level = state.startLevel, attempts = 0)
            else -> null
        }
    }
}
