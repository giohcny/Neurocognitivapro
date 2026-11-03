package com.neurocognitiva.pro.domain

import com.neurocognitiva.pro.data.PatientEntity
import java.time.LocalDate

data class TierInfo(val value: String, val label: String, val range: IntRange)

/** Nivel y ámbito de aplicación clínica (define el rango de niveles sugerido). */
object Tiers {
    const val DEFAULT = "Adulto Clínico Estándar (18-59 años)"

    val options = listOf(
        TierInfo("Infanto-Juvenil / Escolar (6-17 años)", "1. Infanto-Juvenil / Escolar (sugerido N1 a N5)", 1..5),
        TierInfo(DEFAULT, "2. Adulto Clínico Estándar (sugerido N2 a N7)", 2..7),
        TierInfo("Adulto Mayor / Envejecimiento Activo (60+ años)", "3. Adulto Mayor / Psicogerontología (sugerido N1 a N5)", 1..5),
        TierInfo("Rehabilitación y Estimulación Terapéutica", "4. Rehabilitación Neuropsicológica (sugerido N1 a N4)", 1..4),
        TierInfo("Alto Rendimiento Cognitivo / Académico-Laboral", "5. Alto Rendimiento Ejecutivo (sugerido N4 a N8)", 4..8)
    )

    fun info(value: String): TierInfo = options.firstOrNull { it.value == value } ?: options[1]
    fun range(value: String): IntRange = info(value).range

    fun suggestForAge(age: Int): String = when {
        age < 18 -> options[0].value
        age >= 60 -> options[2].value
        else -> DEFAULT
    }

    fun ageWarning(age: Int, tier: String): String? = when {
        age < 18 && tier.contains("Adulto") -> "La edad corresponde a un menor de edad: considera el ámbito Infanto-Juvenil."
        age >= 18 && tier.contains("Infanto-Juvenil") -> "La edad corresponde a un adulto: revisa el ámbito de aplicación."
        age >= 60 && tier == DEFAULT -> "Para 60+ años considera el ámbito Adulto Mayor."
        else -> null
    }
}

/** Protocolo de aplicación: define el nivel máximo que se administra. */
object Protocols {
    const val DEFAULT = "Batería Clínica Estándar (Niveles 1-6)"

    val options = listOf(
        "Tamizaje Cognitivo Rápido (Niveles 1-3)",
        DEFAULT,
        "Exploración Neuropsicológica Profunda (Niveles 1-8)"
    )

    fun maxLevel(protocol: String): Int = when {
        protocol.contains("1-3") -> 3
        protocol.contains("1-6") -> 6
        else -> 8
    }
}

object Education {
    val options = listOf(
        "Educación Básica / Primaria",
        "Educación Media / Secundaria",
        "Técnico Nivel Superior",
        "Universitaria Completa / En curso",
        "Posgrado (Magíster / Doctorado)"
    )
}

object Rut {
    /** null = el texto no tiene forma de RUT chileno; true/false = dígito verificador (módulo 11). */
    fun validate(raw: String): Boolean? {
        val clean = raw.replace(".", "").replace(" ", "").uppercase()
        val match = Regex("^(\\d{1,8})-([0-9K])$").matchEntire(clean) ?: return null
        return computeDv(match.groupValues[1]) == match.groupValues[2]
    }

    fun computeDv(body: String): String {
        var sum = 0
        var factor = 2
        for (ch in body.reversed()) {
            sum += (ch - '0') * factor
            factor = if (factor == 7) 2 else factor + 1
        }
        return when (val r = 11 - (sum % 11)) {
            11 -> "0"
            10 -> "K"
            else -> r.toString()
        }
    }
}

object DemoPatients {
    private val today: String get() = LocalDate.now().toString()

    fun random(): PatientEntity = listOf(
        PatientEntity(
            name = "Roberto Ignacio Fuentes Arriagada", doc = "14.892.301-9", age = 48, birthdate = "1978-08-22",
            gender = "Masculino", education = "Posgrado (Magíster / Doctorado)", handedness = "Diestro/a",
            occupation = "Ingeniero Civil Industrial", appTier = "Alto Rendimiento Cognitivo / Académico-Laboral",
            evalProtocol = "Exploración Neuropsicológica Profunda (Niveles 1-8)",
            evaluator = "Ps. Dra. Carolina Méndez Rojas (Neuropsicóloga Clínica)", evalDate = today,
            notes = "Evaluación de funciones ejecutivas, planificación, atención dividida y memoria operativa por alta exigencia laboral. Sin antecedentes neurológicos previos."
        ),
        PatientEntity(
            name = "Valentina Paz Contreras Silva", doc = "21.045.882-4", age = 23, birthdate = "2003-02-11",
            gender = "Femenino", education = "Universitaria Completa / En curso", handedness = "Zurdo/a",
            occupation = "Estudiante de Medicina", appTier = Tiers.DEFAULT,
            evalProtocol = Protocols.DEFAULT,
            evaluator = "Ps. Dr. Alejandro Valdivia (Especialista en Neuropsicología)", evalDate = today,
            notes = "Entrenamiento de concentración sostenida, velocidad de codificación visomotora, rotación espacial y control inhibitorio."
        ),
        PatientEntity(
            name = "Elena Margarita Valdés Donoso", doc = "7.612.490-6", age = 71, birthdate = "1955-04-09",
            gender = "Femenino", education = "Técnico Nivel Superior", handedness = "Diestro/a",
            occupation = "Docente Jubilada", appTier = "Adulto Mayor / Envejecimiento Activo (60+ años)",
            evalProtocol = Protocols.DEFAULT,
            evaluator = "Ps. Dr. Alejandro Valdivia (Especialista en Psicogerontología)", evalDate = today,
            notes = "Programa de envejecimiento activo y fortalecimiento preventivo de reserva cognitiva, memoria operativa y praxias visoconstructivas."
        )
    ).random()
}
