package com.neurocognitiva.pro.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Ficha de identificación del evaluado. */
@Entity(tableName = "patients")
data class PatientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val doc: String,
    val age: Int,
    val birthdate: String = "",
    val gender: String,
    val education: String,
    val handedness: String,
    val occupation: String,
    val appTier: String,
    val evalProtocol: String,
    val evaluator: String,
    val evalDate: String,
    val notes: String,
    val createdAt: Long = System.currentTimeMillis()
)

/** Una sesión = un nivel de una actividad completado por un paciente. */
@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(
            entity = PatientEntity::class,
            parentColumns = ["id"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("patientId")]
)
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long,
    val activityId: String,
    val level: Int,
    val score: Int,
    /** Precisión 0-100 (aciertos / (aciertos + errores + omisiones + comisiones)). */
    val accuracy: Int,
    /** Tiempo de reacción medio de las respuestas correctas (ms). 0 = sin dato. */
    val avgReactionMs: Int,
    val hits: Int,
    /** Respuestas incorrectas (elección equivocada). */
    val wrong: Int,
    /** Estímulos o ítems sin respuesta / tiempo agotado. */
    val omissions: Int,
    /** Respuestas dadas cuando había que inhibirse (falsas alarmas). */
    val commissions: Int,
    /** Sensibilidad d′ (solo N-Back y CPT). */
    val dPrime: Double?,
    val durationMs: Long,
    /** Código de [com.neurocognitiva.pro.domain.Validity]. */
    val validity: String,
    val inBattery: Boolean,
    /** Dato de demostración: nunca debe mezclarse con registros clínicos reales. */
    val simulated: Boolean,
    val timestamp: Long
) {
    val totalErrors: Int get() = wrong + omissions + commissions
}
