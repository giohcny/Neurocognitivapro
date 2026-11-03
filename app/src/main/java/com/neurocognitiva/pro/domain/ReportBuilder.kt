package com.neurocognitiva.pro.domain

import com.neurocognitiva.pro.data.PatientEntity
import com.neurocognitiva.pro.data.SessionEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class QualitativeItem(val metric: DomainMetric, val text: String)

data class ReportData(
    val code: String,
    val dateLabel: String,
    val patient: PatientEntity,
    val sessionCount: Int,
    val playedCount: Int,
    val global: GlobalMetrics?,
    val metrics: List<DomainMetric>,
    val qualitative: List<QualitativeItem>,
    /** Párrafos con marcas **negrita**. */
    val synthesis: List<String>,
    val validityNotes: List<String>,
    val hasSimulated: Boolean
)

/**
 * Arma el informe a partir de la ficha y las sesiones. Los textos reproducen las plantillas del
 * informe original y agregan: validez de sesión, omisiones/comisiones, d′, tendencia y límites.
 */
object ReportBuilder {

    fun expectedTop(patient: PatientEntity): Int =
        minOf(Tiers.range(patient.appTier).last, Protocols.maxLevel(patient.evalProtocol))

    fun build(patient: PatientEntity, sessions: List<SessionEntity>): ReportData {
        val metrics = Scoring.computeMetrics(sessions, expectedTop(patient))
        val played = metrics.filter { it.played }
        val global = Scoring.global(metrics)

        val year = runCatching { LocalDate.parse(patient.evalDate).year }.getOrDefault(LocalDate.now().year)
        val code = "NC-$year-${patient.id.toString().padStart(4, '0')}"

        val notes = ArrayList<String>()
        val chance = sessions.count { Validity.fromCode(it.validity) == Validity.CHANCE }
        val anticipatory = sessions.count { Validity.fromCode(it.validity) == Validity.ANTICIPATORY }
        val incomplete = sessions.count { Validity.fromCode(it.validity) == Validity.INCOMPLETE }
        if (chance > 0) notes.add("$chance sesión(es) con rendimiento compatible con el azar (excluidas del índice cuando existen sesiones válidas).")
        if (anticipatory > 0) notes.add("$anticipatory sesión(es) con respuestas muy rápidas y baja precisión (excluidas del índice cuando existen sesiones válidas).")
        if (incomplete > 0) notes.add("$incomplete sesión(es) incompletas por tiempo agotado (los ítems sin responder cuentan como omisiones).")

        return ReportData(
            code = code,
            dateLabel = formatDate(patient.evalDate),
            patient = patient,
            sessionCount = sessions.size,
            playedCount = played.size,
            global = global,
            metrics = metrics,
            qualitative = metrics.map { QualitativeItem(it, qualitativeText(it, patient.evalProtocol)) },
            synthesis = synthesis(patient, sessions.size, played, global, notes),
            validityNotes = notes,
            hasSimulated = sessions.any { it.simulated }
        )
    }

    private fun formatDate(iso: String): String = try {
        LocalDate.parse(iso).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    } catch (e: Exception) {
        iso.ifBlank { "--/--/----" }
    }

    private fun fmt(value: Double): String = String.format(Locale.getDefault(), "%.2f", value)

    private fun qualitativeText(m: DomainMetric, protocol: String): String {
        val a = m.activity
        val domain = a.domain.lowercase()
        if (!m.played) {
            return "Dominio aún no administrado en la sesión actual. Evalúa $domain vinculada a ${a.brainAreas}. " +
                "Se sugiere aplicar los niveles iniciales según el protocolo $protocol."
        }
        val rtPart = if (m.avgRtMs > 0) " y una velocidad media de respuesta de **${m.avgRtMs} ms**" else ""
        val rtParen = if (m.avgRtMs > 0) " (${m.avgRtMs} ms)" else ""
        val mastered = if (m.masteredLevel in 1 until m.maxLevel) " (dominio consolidado hasta N${m.masteredLevel})" else ""

        val base = when {
            m.indexScore >= 80 ->
                "Se observa una **capacidad conservada y sobresaliente** en $domain. El evaluado alcanzó el " +
                    "**Nivel ${m.maxLevel} de 8**$mastered con una precisión ponderada del **${m.accuracy}%**$rtPart, " +
                    "evidenciando eficiente reclutamiento de ${a.brainAreas}."
            m.indexScore >= 60 ->
                "Desempeño dentro de **parámetros funcionales esperables** en $domain (Nivel ${m.maxLevel}/8$mastered, " +
                    "precisión de ${m.accuracy}%). Mantiene adecuada estabilidad atencional, aunque en niveles superiores " +
                    "se aprecia incremento fisiológico en la latencia de respuesta$rtParen."
            else ->
                "Se identifica **susceptibilidad a la sobrecarga o fatiga cognitiva** en tareas de $domain " +
                    "(Precisión: ${m.accuracy}%, Nivel ${m.maxLevel}/8). Se recomienda ejercitación graduada en niveles " +
                    "intermedios para consolidar la eficiencia de ${a.brainAreas}."
        }

        val extras = ArrayList<String>()
        m.dPrime?.let { extras.add("Sensibilidad de detección (d′) = ${fmt(it)}.") }
        if (m.omissions + m.commissions > 0) {
            extras.add("Omisiones: ${m.omissions} · Comisiones: ${m.commissions}.")
        }
        m.trend?.let { t ->
            extras.add(
                when {
                    t >= 3 -> "Evolución favorable intra-sujeto (+$t pts entre la primera y la última sesión)."
                    t <= -3 -> "Descenso intra-sujeto ($t pts); considerar fatiga o variabilidad atencional."
                    else -> "Rendimiento estable entre sesiones."
                }
            )
        }
        if (m.ceilingNotReached) extras.add("Techo no alcanzado: se recomienda explorar el Nivel ${m.maxLevel + 1}.")
        if (m.invalidSessions > 0) extras.add("${m.invalidSessions} sesión(es) de validez cuestionable excluida(s) del índice.")

        return if (extras.isEmpty()) base else base + " " + extras.joinToString(" ")
    }

    private fun synthesis(
        p: PatientEntity,
        sessionCount: Int,
        played: List<DomainMetric>,
        global: GlobalMetrics?,
        validityNotes: List<String>
    ): List<String> {
        if (played.isEmpty() || global == null) {
            return listOf(
                "Para generar la síntesis personalizada de **${p.name}** bajo el ámbito **${p.appTier}**, realiza al menos " +
                    "una actividad en la pestaña **Actividades** o usa la **Evaluación guiada** para cubrir las 10 áreas."
            )
        }
        val sorted = played.sortedByDescending { it.indexScore }
        val strongest = sorted.first()
        val weakest = sorted.last()

        val paragraphs = ArrayList<String>()
        paragraphs.add(
            "**1. Impresión General y Nivel de Aplicación Clínica:** El/la evaluado/a **${p.name}** (${p.age} años, " +
                "escolaridad: ${p.education}, lateralidad: ${p.handedness}), evaluado/a bajo el ámbito de **${p.appTier}** " +
                "(${p.evalProtocol}), completó un total de **$sessionCount sesiones** distribuidas en " +
                "**${played.size} de los 10 dominios neuropsicológicos** de la batería, obteniendo un " +
                "**Índice Cognitivo Global (ICG) de ${global.icg}/100**."
        )
        paragraphs.add(
            "**2. Fortalezas Neuropsicológicas Observadas:** El mayor rendimiento relativo se registró en el área de " +
                "**${strongest.activity.domain}** (${strongest.activity.title}), alcanzando el **Nivel ${strongest.maxLevel} de 8** " +
                "con un índice de **${strongest.indexScore}/100** (${strongest.accuracy}% de precisión" +
                (if (strongest.avgRtMs > 0) " y ${strongest.avgRtMs} ms de tiempo de reacción" else "") +
                "). Esto refleja una buena eficiencia funcional en los circuitos asociados a ${strongest.activity.brainAreas}."
        )
        if (played.size >= 2 && weakest.activity.id != strongest.activity.id) {
            paragraphs.add(
                "**3. Dominios Susceptibles de Estimulación:** En comparación con su propio perfil intrasujeto, el área con " +
                    "mayor oportunidad de fortalecimiento corresponde a **${weakest.activity.domain}** (Índice: ${weakest.indexScore}/100, " +
                    "Nivel alcanzado: ${weakest.maxLevel}/8). Se sugiere entrenar progresivamente la transición entre los niveles " +
                    "${maxOf(1, weakest.maxLevel)} y ${minOf(8, weakest.maxLevel + 1)} priorizando la precisión y la autorregulación ejecutiva."
            )
        } else {
            paragraphs.add(
                "**3. Dominios Susceptibles de Estimulación:** Solo se evaluó un dominio, por lo que aún no es posible " +
                    "establecer un perfil comparativo intrasujeto. Se sugiere completar la batería de 10 áreas."
            )
        }
        paragraphs.add(
            "**4. Plan de Intervención Multinivel Sugerido:** Acorde al ámbito de aplicación *${p.appTier}*, se recomienda pautar " +
                "sesiones de 25 a 30 minutos, 3 veces por semana, integrando tareas de atención sostenida (*CPT Go/No-Go*), " +
                "memoria de trabajo (*N-Back* y *Matriz Visoespacial*), funciones ejecutivas (*Torre de Planificación* y " +
                "*Neuro-Trail B*), praxias de rotación mental y fluidez numérica."
        )
        val limits = StringBuilder(
            "**5. Alcances y Limitaciones:** Este informe resume el desempeño en tareas computarizadas de tamizaje y estimulación. " +
                "La clasificación es relativa al ámbito de aplicación y se calcula con referencias heurísticas, no con baremos " +
                "poblacionales validados; por lo tanto **no constituye un diagnóstico**. Debe integrarse con la entrevista clínica, " +
                "la observación directa y pruebas estandarizadas."
        )
        if (validityNotes.isNotEmpty()) limits.append(" Observaciones de validez: ").append(validityNotes.joinToString(" "))
        paragraphs.add(limits.toString())
        return paragraphs
    }

    fun stripMarks(text: String): String = text.replace("**", "").replace("*", "")

    fun toPlainText(r: ReportData, aiText: String?): String = buildString {
        val p = r.patient
        appendLine("INFORME PSICOLÓGICO DE FUNCIONES COGNITIVAS")
        appendLine("Código: ${r.code}   Fecha: ${r.dateLabel}")
        if (r.hasSimulated) appendLine("*** CONTIENE DATOS SIMULADOS (DEMOSTRACIÓN) — NO USAR COMO REGISTRO CLÍNICO ***")
        appendLine()
        appendLine("I. IDENTIFICACIÓN")
        appendLine("Nombre: ${p.name}")
        appendLine("Documento: ${p.doc}")
        appendLine("Edad: ${p.age} años ${p.birthdate}".trim())
        appendLine("Género / Lateralidad: ${p.gender} / ${p.handedness}")
        appendLine("Escolaridad: ${p.education}")
        appendLine("Ocupación: ${p.occupation}")
        appendLine("Evaluador: ${p.evaluator}")
        appendLine("Ámbito de aplicación: ${p.appTier}")
        appendLine("Protocolo: ${p.evalProtocol}")
        appendLine("Motivo / observaciones: ${p.notes}")
        appendLine()
        appendLine("II. PERFIL GLOBAL")
        if (r.global != null) {
            appendLine("ICG: ${r.global.icg}/100 (${r.global.category})")
            appendLine("Precisión media: ${r.global.accuracy}%   Latencia media: ${r.global.rtMs} ms   Nivel máximo: ${r.global.maxLevel}/8")
        } else {
            appendLine("Sin datos suficientes.")
        }
        appendLine()
        appendLine("III. DETALLE POR FUNCIÓN")
        r.metrics.forEach { m ->
            if (m.played) {
                appendLine("- ${m.activity.shortTitle}: N${m.maxLevel}/8 · Precisión ${m.accuracy}% · ${m.avgRtMs} ms · Índice ${m.indexScore} · ${m.classification.label}")
            } else {
                appendLine("- ${m.activity.shortTitle}: no evaluado")
            }
        }
        appendLine()
        appendLine("IV. SÍNTESIS POR FUNCIÓN")
        r.qualitative.forEach { appendLine("${it.metric.activity.shortTitle}: ${stripMarks(it.text)}"); appendLine() }
        appendLine("V. CONCLUSIÓN INTEGRADA")
        r.synthesis.forEach { appendLine(stripMarks(it)); appendLine() }
        if (!aiText.isNullOrBlank()) {
            appendLine("INTERPRETACIÓN AVANZADA (IA — requiere revisión profesional)")
            appendLine(aiText)
        }
    }

    /** Prompt para la IA. No incluye nombre, documento ni notas clínicas libres. */
    fun aiPrompt(r: ReportData): Pair<String, String> {
        val system = "Actúa como un Neuropsicólogo Clínico Senior. Redacta una síntesis clínica formal en español " +
            "(3 párrafos claros y estructurados) interpretando el perfil cognitivo del paciente en las 10 áreas evaluadas, " +
            "considerando su nivel de aplicación clínica y recomendando un plan práctico de estimulación cognitiva. " +
            "Aclara que los resultados provienen de una herramienta digital de tamizaje y estimulación y no constituyen diagnóstico. " +
            "No uses formato markdown de títulos (#), usa párrafos limpios."
        val p = r.patient
        val lines = r.metrics.joinToString("\n") { m ->
            val body = if (!m.played) "no evaluado" else
                "Nivel máx. ${m.maxLevel}/8 (dominado hasta N${m.masteredLevel}), Precisión ${m.accuracy}%, " +
                    "T. Reacción ${m.avgRtMs} ms, Índice ${m.indexScore}/100 (${m.classification.label})" +
                    (m.dPrime?.let { ", d′ ${fmt(it)}" } ?: "") +
                    ", omisiones ${m.omissions}, comisiones ${m.commissions}"
            "- ${m.activity.title} (${m.activity.domain}): $body"
        }
        val user = "Datos del evaluado (sin nombre ni documento): Edad: ${p.age} años, Escolaridad: ${p.education}, " +
            "Lateralidad: ${p.handedness}, Ocupación: ${p.occupation}, Nivel de Aplicación: ${p.appTier}, " +
            "Protocolo: ${p.evalProtocol}.\n\nResultados psicométricos:\n$lines"
        return system to user
    }
}
