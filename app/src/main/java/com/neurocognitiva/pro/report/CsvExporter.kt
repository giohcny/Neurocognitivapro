package com.neurocognitiva.pro.report

import android.content.Context
import com.neurocognitiva.pro.data.PatientEntity
import com.neurocognitiva.pro.data.SessionEntity
import com.neurocognitiva.pro.domain.Catalog
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Exporta las sesiones en CSV. Por privacidad NO incluye nombre ni documento (solo un id interno). */
object CsvExporter {
    fun export(context: Context, patient: PatientEntity, sessions: List<SessionEntity>): File {
        val iso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val sb = StringBuilder()
        sb.appendLine(
            "paciente_id,edad,ambito,fecha,actividad,nivel,precision_pct,tr_medio_ms,aciertos,errores," +
                "omisiones,comisiones,d_prime,puntaje,duracion_s,validez,en_bateria,simulada"
        )
        sessions.forEach { s ->
            val title = Catalog.activities.firstOrNull { it.id == s.activityId }?.shortTitle ?: s.activityId
            sb.appendLine(
                listOf(
                    patient.id, patient.age, "\"${patient.appTier}\"", iso.format(Date(s.timestamp)), "\"$title\"",
                    s.level, s.accuracy, s.avgReactionMs, s.hits, s.wrong, s.omissions, s.commissions,
                    s.dPrime?.let { String.format(Locale.US, "%.3f", it) } ?: "",
                    s.score, s.durationMs / 1000, s.validity, s.inBattery, s.simulated
                ).joinToString(",")
            )
        }
        val file = File(ShareUtils.exportDir(context), "sesiones_paciente_${patient.id}.csv")
        file.writeText(sb.toString(), Charsets.UTF_8)
        return file
    }
}
