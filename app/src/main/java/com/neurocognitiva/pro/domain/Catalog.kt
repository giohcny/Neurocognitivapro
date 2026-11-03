package com.neurocognitiva.pro.domain

// ARCHIVO GENERADO a partir del catálogo original (10 actividades x 8 niveles).

sealed interface LevelParams
data class CorsiParams(val gridSize: Int, val targets: Int, val showTimeMs: Int, val rounds: Int) : LevelParams
data class StroopParams(val trials: Int, val timePerTrialMs: Int, val mixedRule: Boolean, val optionsCount: Int) : LevelParams
data class NBackParams(val n: Int, val totalStimuli: Int, val intervalMs: Int) : LevelParams
data class TrailParams(val nodes: Int, val alternating: Boolean, val timeLimitSec: Int) : LevelParams
data class SdmtParams(val keySize: Int, val targetCount: Int, val durationSec: Int, val shuffleKey: Boolean) : LevelParams
data class ReasoningParams(val items: Int, val timeLimitSec: Int, val tier: Int) : LevelParams
data class CptParams(val totalTrials: Int, val intervalMs: Int, val ruleType: Int) : LevelParams
data class RotationParams(val gridSize: Int, val cells: Int, val rounds: Int, val angles: List<Int>) : LevelParams
data class HanoiParams(val disks: Int, val targetPeg: Int, val optimalMoves: Int) : LevelParams
data class ArithmeticParams(val items: Int, val timeLimitSec: Int, val tier: Int) : LevelParams

data class LevelDef(val level: Int, val name: String, val desc: String, val params: LevelParams)

enum class Category(val label: String) {
    ATENCION("Atención y Velocidad"),
    MEMORIA("Memoria de Trabajo"),
    EJECUTIVAS("Funciones Ejecutivas"),
    VISOESPACIAL("Praxias y Visoespacial"),
    LENGUAJE_CALCULO("Razonamiento y Cálculo")
}

data class ActivityDef(
    val id: String,
    val category: Category,
    val title: String,
    val shortTitle: String,
    val domain: String,
    val brainAreas: String,
    val colorHex: Long,
    val description: String,
    val instructions: String,
    /** Tiempo de respuesta "excelente" y "lento" (ms) usados solo como referencia heurística del índice de velocidad. */
    val rtFastMs: Int,
    val rtSlowMs: Int,
    val levels: List<LevelDef>
) {
    fun level(n: Int): LevelDef = levels.firstOrNull { it.level == n } ?: levels.first()
}

object Catalog {
    const val MAX_LEVEL = 8

    val activities: List<ActivityDef> = listOf(
        ActivityDef(
            id = "corsi_matrix",
            category = Category.MEMORIA,
            title = "Matriz de Memoria Visoespacial",
            shortTitle = "Memoria Visoespacial",
            domain = "Memoria de Trabajo Visoespacial y Retención Inmediata",
            brainAreas = "Corteza Parietal Posterior Derecha y Corteza Prefrontal Dorsolateral",
            colorHex = 0xFF4F46E5,
            description = "Memoriza la posición de las celdas iluminadas en la cuadrícula y reprodúcelas con exactitud tras desaparecer el estímulo.",
            instructions = "Se iluminarán algunas casillas de la cuadrícula. Memoriza su posición y, cuando desaparezcan, toca exactamente esas casillas.",
            rtFastMs = 500,
            rtSlowMs = 2500,
            levels = listOf(
                LevelDef(1, "Nivel 1: Inicial", "Cuadrícula 3x3 • 2 estímulos • Exposición amplia", CorsiParams(gridSize = 3, targets = 2, showTimeMs = 1600, rounds = 3)),
                LevelDef(2, "Nivel 2: Básico", "Cuadrícula 3x3 • 3 estímulos • Exposición guiada", CorsiParams(gridSize = 3, targets = 3, showTimeMs = 1450, rounds = 4)),
                LevelDef(3, "Nivel 3: Elemental", "Cuadrícula 4x4 • 4 estímulos • Exposición moderada", CorsiParams(gridSize = 4, targets = 4, showTimeMs = 1300, rounds = 4)),
                LevelDef(4, "Nivel 4: Intermedio", "Cuadrícula 4x4 • 5 estímulos • Demanda espacial media", CorsiParams(gridSize = 4, targets = 5, showTimeMs = 1100, rounds = 4)),
                LevelDef(5, "Nivel 5: Intermedio Alto", "Cuadrícula 5x5 • 6 estímulos • Exposición breve", CorsiParams(gridSize = 5, targets = 6, showTimeMs = 950, rounds = 5)),
                LevelDef(6, "Nivel 6: Avanzado", "Cuadrícula 5x5 • 7 estímulos • Alta densidad espacial", CorsiParams(gridSize = 5, targets = 7, showTimeMs = 820, rounds = 5)),
                LevelDef(7, "Nivel 7: Superior", "Cuadrícula 6x6 • 8 estímulos • Exposición ultrarrápida", CorsiParams(gridSize = 6, targets = 8, showTimeMs = 740, rounds = 5)),
                LevelDef(8, "Nivel 8: Maestro Clínico", "Cuadrícula 6x6 • 10 estímulos • Capacidad visoespacial máxima", CorsiParams(gridSize = 6, targets = 10, showTimeMs = 650, rounds = 5))
            )
        ),
        ActivityDef(
            id = "stroop_inhibition",
            category = Category.ATENCION,
            title = "Inhibición Neuro-Stroop Dinámico",
            shortTitle = "Control Inhibitorio",
            domain = "Control Inhibitorio, Atención Selectiva y Resistencia a la Interferencia",
            brainAreas = "Corteza Cingulada Anterior y Corteza Prefrontal Ventrolateral",
            colorHex = 0xFFE11D48,
            description = "Inhibe la lectura automática de la palabra e identifica el color de la tinta o cumple la regla cambiante según el nivel.",
            instructions = "Aparecerá el nombre de un color escrito con otra tinta. Sigue la regla indicada arriba (color de la tinta o significado de la palabra) e ignora la lectura automática. Responde lo más rápido que puedas sin cometer errores.",
            rtFastMs = 700,
            rtSlowMs = 3500,
            levels = listOf(
                LevelDef(1, "Nivel 1: Inicial", "Identificar color de tinta • 3 opciones • Ritmo pausado", StroopParams(trials = 6, timePerTrialMs = 5500, mixedRule = false, optionsCount = 3)),
                LevelDef(2, "Nivel 2: Básico", "Color de tinta • 3 opciones • Transición fluida", StroopParams(trials = 8, timePerTrialMs = 4600, mixedRule = false, optionsCount = 3)),
                LevelDef(3, "Nivel 3: Elemental", "Incongruencia cromática • 4 opciones • Ritmo normal", StroopParams(trials = 10, timePerTrialMs = 4000, mixedRule = false, optionsCount = 4)),
                LevelDef(4, "Nivel 4: Intermedio", "Alta interferencia léxica • Presión temporal media", StroopParams(trials = 12, timePerTrialMs = 3300, mixedRule = false, optionsCount = 4)),
                LevelDef(5, "Nivel 5: Intermedio Alto", "Cambio de criterio (Color de Tinta vs Significado)", StroopParams(trials = 12, timePerTrialMs = 2800, mixedRule = true, optionsCount = 4)),
                LevelDef(6, "Nivel 6: Avanzado", "Alternancia ejecutiva • 5 colores en competencia", StroopParams(trials = 14, timePerTrialMs = 2400, mixedRule = true, optionsCount = 5)),
                LevelDef(7, "Nivel 7: Superior", "Baja latencia de inhibición • Cambio continuo de regla", StroopParams(trials = 16, timePerTrialMs = 2050, mixedRule = true, optionsCount = 5)),
                LevelDef(8, "Nivel 8: Maestro Clínico", "6 canales cromáticos • Exigencia inhibitoria extrema", StroopParams(trials = 18, timePerTrialMs = 1750, mixedRule = true, optionsCount = 6))
            )
        ),
        ActivityDef(
            id = "nback_memory",
            category = Category.MEMORIA,
            title = "Secuencia Operativa N-Back",
            shortTitle = "Memoria Operativa",
            domain = "Actualización de Memoria de Trabajo y Monitoreo Continuo",
            brainAreas = "Corteza Prefrontal Dorsolateral Bilateral y Lóbulo Parietal Inferior",
            colorHex = 0xFF059669,
            description = "Observa la secuencia de estímulos y detecta si el símbolo actual coincide con el presentado 'N' posiciones atrás.",
            instructions = "Se mostrará una letra a la vez. Indica si la letra actual es IGUAL a la que apareció N posiciones atrás. Las primeras N letras solo se memorizan.",
            rtFastMs = 450,
            rtSlowMs = 2200,
            levels = listOf(
                LevelDef(1, "Nivel 1: Inicial (1-Back Pausado)", "1-Back básico • Intervalo amplio de consolidación", NBackParams(n = 1, totalStimuli = 8, intervalMs = 2600)),
                LevelDef(2, "Nivel 2: Básico (1-Back)", "1-Back estándar (¿Igual al inmediatamente anterior?)", NBackParams(n = 1, totalStimuli = 10, intervalMs = 2200)),
                LevelDef(3, "Nivel 3: Elemental (1-Back Veloz)", "1-Back con velocidad incrementada y mayor flujo", NBackParams(n = 1, totalStimuli = 12, intervalMs = 1800)),
                LevelDef(4, "Nivel 4: Intermedio (2-Back)", "2-Back (¿Igual al de hace 2 posiciones?) • Carga dual", NBackParams(n = 2, totalStimuli = 12, intervalMs = 2350)),
                LevelDef(5, "Nivel 5: Intermedio Alto (2-Back Veloz)", "2-Back de alta velocidad con distractores fonológicos", NBackParams(n = 2, totalStimuli = 14, intervalMs = 1850)),
                LevelDef(6, "Nivel 6: Avanzado (3-Back)", "3-Back (¿Igual al de hace 3 posiciones?) • Nivel clínico alto", NBackParams(n = 3, totalStimuli = 15, intervalMs = 2200)),
                LevelDef(7, "Nivel 7: Superior (3-Back Veloz)", "3-Back rápido • Actualización ejecutiva intensiva", NBackParams(n = 3, totalStimuli = 16, intervalMs = 1750)),
                LevelDef(8, "Nivel 8: Maestro Clínico (4-Back)", "4-Back (¿Igual al de hace 4 posiciones?) • Techo psicométrico", NBackParams(n = 4, totalStimuli = 18, intervalMs = 2100))
            )
        ),
        ActivityDef(
            id = "trail_flexibility",
            category = Category.EJECUTIVAS,
            title = "Rastreo Secuencial Neuro-Trail (TMT A/B)",
            shortTitle = "Flexibilidad Cognitiva",
            domain = "Atención Alternante, Rastreo Visomotor y Flexibilidad Cognitiva",
            brainAreas = "Lóbulo Frontal Superior, Campos Oculares Frontales y Red Fronto-Parietal",
            colorHex = 0xFFD97706,
            description = "Conecta los nodos en orden ascendente (1-2-3...) o alternando números y letras (1-A-2-B-3-C...) en el menor tiempo posible.",
            instructions = "Toca los nodos en el orden correcto lo más rápido que puedas. En modo A: 1-2-3... En modo B alterna número y letra: 1-A-2-B-3-C...",
            rtFastMs = 600,
            rtSlowMs = 3000,
            levels = listOf(
                LevelDef(1, "Nivel 1: Inicial (TMT-A Corto)", "Secuencia numérica directa (1 al 6) • Rastreo inicial", TrailParams(nodes = 6, alternating = false, timeLimitSec = 45)),
                LevelDef(2, "Nivel 2: Básico (TMT-A)", "Secuencia numérica (1 al 9) • Coordinación visomotora", TrailParams(nodes = 9, alternating = false, timeLimitSec = 45)),
                LevelDef(3, "Nivel 3: Elemental (TMT-A+)", "Secuencia numérica (1 al 12) • Mayor densidad visual", TrailParams(nodes = 12, alternating = false, timeLimitSec = 50)),
                LevelDef(4, "Nivel 4: Intermedio (TMT-B Inicial)", "Alternancia Número-Letra (1-A-2-B hasta 4-D)", TrailParams(nodes = 8, alternating = true, timeLimitSec = 50)),
                LevelDef(5, "Nivel 5: Intermedio Alto (TMT-B)", "Alternancia Número-Letra (1-A hasta 6-F)", TrailParams(nodes = 12, alternating = true, timeLimitSec = 55)),
                LevelDef(6, "Nivel 6: Avanzado (TMT-B+)", "Alternancia Número-Letra (1-A hasta 8-H) • Alta flexibilidad", TrailParams(nodes = 16, alternating = true, timeLimitSec = 65)),
                LevelDef(7, "Nivel 7: Superior (TMT-B Pro)", "Alternancia compleja de 20 nodos (1-A hasta 10-J)", TrailParams(nodes = 20, alternating = true, timeLimitSec = 72)),
                LevelDef(8, "Nivel 8: Maestro Clínico (TMT-B Experto)", "24 nodos en alternancia (1-A hasta 12-L) • Máximo control", TrailParams(nodes = 24, alternating = true, timeLimitSec = 80))
            )
        ),
        ActivityDef(
            id = "sdmt_speed",
            category = Category.ATENCION,
            title = "Codificación Símbolo-Dígito (SDMT)",
            shortTitle = "Velocidad Procesamiento",
            domain = "Velocidad de Procesamiento Psicomotor y Atención Sostenida",
            brainAreas = "Fascículo Longitudinal Superior, Ganglios Basales y Corteza Visual Asociativa",
            colorHex = 0xFF0891B2,
            description = "Decodifica a máxima velocidad cada símbolo geométrico asociándolo con su dígito correspondiente según la clave superior.",
            instructions = "Observa la clave superior (símbolo = dígito). Para cada símbolo central toca el dígito que le corresponde, lo más rápido posible. Algunas versiones cambian la clave durante la tarea.",
            rtFastMs = 1000,
            rtSlowMs = 4000,
            levels = listOf(
                LevelDef(1, "Nivel 1: Inicial", "Clave de 3 símbolos fijos • 8 decodificaciones", SdmtParams(keySize = 3, targetCount = 8, durationSec = 40, shuffleKey = false)),
                LevelDef(2, "Nivel 2: Básico", "Clave de 4 símbolos fijos • 10 decodificaciones", SdmtParams(keySize = 4, targetCount = 10, durationSec = 40, shuffleKey = false)),
                LevelDef(3, "Nivel 3: Elemental", "Clave de 5 símbolos • 12 decodificaciones", SdmtParams(keySize = 5, targetCount = 12, durationSec = 40, shuffleKey = false)),
                LevelDef(4, "Nivel 4: Intermedio", "Clave de 6 símbolos • Mayor exigencia oculomotora", SdmtParams(keySize = 6, targetCount = 15, durationSec = 42, shuffleKey = false)),
                LevelDef(5, "Nivel 5: Intermedio Alto", "Clave de 7 símbolos con rotación periódica de matriz", SdmtParams(keySize = 7, targetCount = 16, durationSec = 42, shuffleKey = true)),
                LevelDef(6, "Nivel 6: Avanzado", "Clave de 8 símbolos con reordenamiento dinámico", SdmtParams(keySize = 8, targetCount = 18, durationSec = 44, shuffleKey = true)),
                LevelDef(7, "Nivel 7: Superior", "Clave completa de 9 símbolos • Alta tasa de respuesta", SdmtParams(keySize = 9, targetCount = 22, durationSec = 45, shuffleKey = true)),
                LevelDef(8, "Nivel 8: Maestro Clínico", "9 símbolos con rotación frecuente • Velocidad máxima", SdmtParams(keySize = 9, targetCount = 26, durationSec = 45, shuffleKey = true))
            )
        ),
        ActivityDef(
            id = "semantic_reasoning",
            category = Category.LENGUAJE_CALCULO,
            title = "Razonamiento Abstracto y Semántico",
            shortTitle = "Razonamiento y Lenguaje",
            domain = "Abstracción Lógica, Fluidez Semántica y Categorización Conceptual",
            brainAreas = "Corteza Prefrontal Rostrolateral (Polo Frontal) y Giro Temporal Medio Izquierdo",
            colorHex = 0xFF7C3AED,
            description = "Resuelve analogías verbales, series lógicas, matrices numéricas y exclusión semántica de complejidad creciente.",
            instructions = "Resuelve analogías, series y problemas lógicos eligiendo la mejor alternativa. Hay un tiempo máximo por nivel.",
            rtFastMs = 4000,
            rtSlowMs = 20000,
            levels = listOf(
                LevelDef(1, "Nivel 1: Inicial", "Asociación categorial directa y vocabulario nuclear", ReasoningParams(items = 5, timeLimitSec = 50, tier = 1)),
                LevelDef(2, "Nivel 2: Básico", "Analogías funcionales e identificación de intruso semántico", ReasoningParams(items = 5, timeLimitSec = 50, tier = 2)),
                LevelDef(3, "Nivel 3: Elemental", "Progresiones lógico-matemáticas y relaciones abstractas", ReasoningParams(items = 5, timeLimitSec = 50, tier = 3)),
                LevelDef(4, "Nivel 4: Intermedio", "Silogismos deductivos y matrices verbales de segundo orden", ReasoningParams(items = 5, timeLimitSec = 50, tier = 4)),
                LevelDef(5, "Nivel 5: Intermedio Alto", "Inferencia proposicional compleja y abstracción conceptual", ReasoningParams(items = 5, timeLimitSec = 55, tier = 5)),
                LevelDef(6, "Nivel 6: Avanzado", "Lógica relacional transitiva y analogías epistemológicas", ReasoningParams(items = 5, timeLimitSec = 55, tier = 6)),
                LevelDef(7, "Nivel 7: Superior", "Matrices alfanuméricas de doble regla y deducción formal", ReasoningParams(items = 5, timeLimitSec = 60, tier = 7)),
                LevelDef(8, "Nivel 8: Maestro Clínico", "Razonamiento hipotético-deductivo de nivel superior", ReasoningParams(items = 5, timeLimitSec = 60, tier = 8))
            )
        ),
        ActivityDef(
            id = "cpt_sustained",
            category = Category.ATENCION,
            title = "Vigilancia y Atención Sostenida (CPT Go/No-Go)",
            shortTitle = "Atención Sostenida CPT",
            domain = "Vigilancia Atencional, Control de Impulsividad y Atención Sostenida",
            brainAreas = "Red Fronto-Parietal Ventral Derecha, Tálamo Reticular y Corteza Prefrontal Medial",
            colorHex = 0xFF0D9488,
            description = "Responde de inmediato ante los estímulos objetivo (GO) e inhibe tu respuesta frente a los estímulos trampa (NO-GO).",
            instructions = "Presiona el botón ¡PULSAR! SOLO cuando aparezca el estímulo objetivo indicado por la regla. Si aparece un estímulo trampa, NO pulses nada.",
            rtFastMs = 350,
            rtSlowMs = 1200,
            levels = listOf(
                LevelDef(1, "Nivel 1: Inicial", "Pulsa en Círculo Verde • Inhibe en Cuadrado Rojo", CptParams(totalTrials = 8, intervalMs = 2400, ruleType = 1)),
                LevelDef(2, "Nivel 2: Básico", "Flujo continuo Go/No-Go • Ritmo moderado", CptParams(totalTrials = 10, intervalMs = 2100, ruleType = 1)),
                LevelDef(3, "Nivel 3: Elemental", "Pulsa cualquier Vocal • Inhibe Consonantes", CptParams(totalTrials = 12, intervalMs = 1850, ruleType = 2)),
                LevelDef(4, "Nivel 4: Intermedio", "Discriminación rápida Vocal vs Consonante", CptParams(totalTrials = 12, intervalMs = 1650, ruleType = 2)),
                LevelDef(5, "Nivel 5: Intermedio Alto", "Pulsa Números Pares • Inhibe Números Impares", CptParams(totalTrials = 14, intervalMs = 1600, ruleType = 3)),
                LevelDef(6, "Nivel 6: Avanzado", "Regla CPT-X: Pulsa en toda letra EXCEPTO en la letra 'X'", CptParams(totalTrials = 15, intervalMs = 1450, ruleType = 4)),
                LevelDef(7, "Nivel 7: Superior", "CPT-X de alta velocidad • Control de comisión estricto", CptParams(totalTrials = 16, intervalMs = 1300, ruleType = 4)),
                LevelDef(8, "Nivel 8: Maestro Clínico", "Doble condición: Pulsa solo Números Mayores a 50 Pares", CptParams(totalTrials = 18, intervalMs = 1150, ruleType = 5))
            )
        ),
        ActivityDef(
            id = "mental_rotation",
            category = Category.VISOESPACIAL,
            title = "Praxias Visoconstructivas y Rotación Mental",
            shortTitle = "Praxias y Rotación",
            domain = "Transformación Visoespacial, Praxias Constructivas y Rotación Mental",
            brainAreas = "Surco Intraparietal Superior, Lóbulo Parietal Derecho y Corteza Occipito-Temporal",
            colorHex = 0xFF0284C7,
            description = "Rota mentalmente la figura matriz de referencia e identifica cuál de las 4 opciones corresponde exactamente a su giro en el plano.",
            instructions = "Imagina que la figura modelo gira en el plano según el ángulo indicado (sentido horario) y elige la opción que corresponde exactamente a ese giro. Cuidado con las imágenes en espejo.",
            rtFastMs = 3000,
            rtSlowMs = 15000,
            levels = listOf(
                LevelDef(1, "Nivel 1: Inicial", "Matriz 3x3 • Rotación simple de 90° en sentido horario", RotationParams(gridSize = 3, cells = 3, rounds = 4, angles = listOf(90))),
                LevelDef(2, "Nivel 2: Básico", "Matriz 3x3 • Giros de 90° y 180° • Figuras en L", RotationParams(gridSize = 3, cells = 4, rounds = 4, angles = listOf(90, 180))),
                LevelDef(3, "Nivel 3: Elemental", "Matriz 3x3 • Cualquier ángulo (90°, 180°, 270°)", RotationParams(gridSize = 3, cells = 5, rounds = 5, angles = listOf(90, 180, 270))),
                LevelDef(4, "Nivel 4: Intermedio", "Matriz 4x4 • Estructuras asimétricas intermedias", RotationParams(gridSize = 4, cells = 5, rounds = 5, angles = listOf(90, 180))),
                LevelDef(5, "Nivel 5: Intermedio Alto", "Matriz 4x4 • Distractores especulares (efecto espejo)", RotationParams(gridSize = 4, cells = 6, rounds = 5, angles = listOf(90, 180, 270))),
                LevelDef(6, "Nivel 6: Avanzado", "Matriz 4x4 • Alta complejidad visoconstructiva", RotationParams(gridSize = 4, cells = 7, rounds = 5, angles = listOf(90, 180, 270))),
                LevelDef(7, "Nivel 7: Superior", "Matriz 5x5 • Rotación mental de patrones densos", RotationParams(gridSize = 5, cells = 8, rounds = 5, angles = listOf(90, 180, 270))),
                LevelDef(8, "Nivel 8: Maestro Clínico", "Matriz 5x5 • Discriminación espacial experta", RotationParams(gridSize = 5, cells = 10, rounds = 6, angles = listOf(90, 180, 270)))
            )
        ),
        ActivityDef(
            id = "planning_hanoi",
            category = Category.EJECUTIVAS,
            title = "Planificación Ejecutiva (Torre Secuencial)",
            shortTitle = "Planificación Ejecutiva",
            domain = "Planificación Anticipatoria, Secuenciación Ejecutiva y Resolución de Problemas",
            brainAreas = "Corteza Prefrontal Dorsolateral Anterior, Polo Frontal y Núcleo Caudado",
            colorHex = 0xFFEA580C,
            description = "Traslada los discos desde la Torre A hasta la Torre C utilizando el mínimo número de movimientos y sin colocar un disco mayor sobre uno menor.",
            instructions = "Mueve los discos a la torre meta con el menor número de movimientos. Toca una torre para tomar su disco superior y luego la torre destino. Nunca coloques un disco grande sobre uno más pequeño.",
            rtFastMs = 1500,
            rtSlowMs = 8000,
            levels = listOf(
                LevelDef(1, "Nivel 1: Inicial", "2 discos • Meta en Torre C (Mínimo: 3 movimientos)", HanoiParams(disks = 2, targetPeg = 2, optimalMoves = 3)),
                LevelDef(2, "Nivel 2: Básico", "2 discos • Meta en Torre Central B • Planificación directa", HanoiParams(disks = 2, targetPeg = 1, optimalMoves = 3)),
                LevelDef(3, "Nivel 3: Elemental", "3 discos • Clásico Torre de Hanoi (Mínimo: 7 movimientos)", HanoiParams(disks = 3, targetPeg = 2, optimalMoves = 7)),
                LevelDef(4, "Nivel 4: Intermedio", "3 discos • Meta en Torre Central B • Inversión espacial", HanoiParams(disks = 3, targetPeg = 1, optimalMoves = 7)),
                LevelDef(5, "Nivel 5: Intermedio Alto", "4 discos • Secuenciación recursiva (Mínimo: 15 movimientos)", HanoiParams(disks = 4, targetPeg = 2, optimalMoves = 15)),
                LevelDef(6, "Nivel 6: Avanzado", "4 discos hacia Torre B • Control de sub-metas ejecutivas", HanoiParams(disks = 4, targetPeg = 1, optimalMoves = 15)),
                LevelDef(7, "Nivel 7: Superior", "5 discos • Alta demanda de anticipación (Mínimo: 31 mov.)", HanoiParams(disks = 5, targetPeg = 2, optimalMoves = 31)),
                LevelDef(8, "Nivel 8: Maestro Clínico", "5 discos en configuración experta • Planificación máxima", HanoiParams(disks = 5, targetPeg = 1, optimalMoves = 31))
            )
        ),
        ActivityDef(
            id = "arithmetic_fluency",
            category = Category.LENGUAJE_CALCULO,
            title = "Cálculo Mental Operativo y Fluidez Numérica",
            shortTitle = "Cálculo y Numeracidad",
            domain = "Procesamiento Numérico, Cálculo Mental Operativo y Magnitud Cuantitativa",
            brainAreas = "Surco Intraparietal Horizontal Bilateral y Giro Angular Izquierdo",
            colorHex = 0xFFC026D3,
            description = "Resuelve operaciones encadenadas en memoria de trabajo e identifica la relación cuantitativa correcta bajo presión de tiempo.",
            instructions = "Resuelve mentalmente cada operación y elige el resultado correcto antes de que se agote el tiempo.",
            rtFastMs = 3000,
            rtSlowMs = 15000,
            levels = listOf(
                LevelDef(1, "Nivel 1: Inicial", "Sumas y restas directas de un dígito", ArithmeticParams(items = 5, timeLimitSec = 45, tier = 1)),
                LevelDef(2, "Nivel 2: Básico", "Operaciones aditivas de dos dígitos sin acarreo complejo", ArithmeticParams(items = 6, timeLimitSec = 45, tier = 2)),
                LevelDef(3, "Nivel 3: Elemental", "Multiplicación básica y operaciones combinadas simples", ArithmeticParams(items = 6, timeLimitSec = 50, tier = 3)),
                LevelDef(4, "Nivel 4: Intermedio", "Ecuaciones con incógnita operativa y jerarquía de signos", ArithmeticParams(items = 6, timeLimitSec = 50, tier = 4)),
                LevelDef(5, "Nivel 5: Intermedio Alto", "Cálculo encadenado de 3 pasos en memoria operativa", ArithmeticParams(items = 7, timeLimitSec = 55, tier = 5)),
                LevelDef(6, "Nivel 6: Avanzado", "Porcentajes mentales, fracciones y razones numéricas", ArithmeticParams(items = 7, timeLimitSec = 55, tier = 6)),
                LevelDef(7, "Nivel 7: Superior", "Potencias, raíces exactas y equilibrio algebraico", ArithmeticParams(items = 8, timeLimitSec = 60, tier = 7)),
                LevelDef(8, "Nivel 8: Maestro Clínico", "Operaciones anidadas de alta demanda aritmética", ArithmeticParams(items = 8, timeLimitSec = 60, tier = 8))
            )
        )
    )

    fun byId(id: String): ActivityDef = activities.first { it.id == id }
}
