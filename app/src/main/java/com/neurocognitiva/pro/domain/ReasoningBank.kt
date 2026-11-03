package com.neurocognitiva.pro.domain

// ARCHIVO GENERADO a partir del banco original de razonamiento (8 niveles x 5 reactivos).

data class ReasoningItem(val question: String, val options: List<String>, val answerIndex: Int)

object ReasoningBank {
    val tiers: Map<Int, List<ReasoningItem>> = mapOf(
        1 to listOf(
            ReasoningItem("Analogía Semántica: 'LIBRO' es a 'LEER' como 'MELODÍA' es a...", listOf("Escuchar", "Pintar", "Caminar", "Escribir"), 0),
            ReasoningItem("Serie Numérica: ¿Qué número continúa la serie 4, 8, 12, 16, ...?", listOf("18", "20", "22", "24"), 1),
            ReasoningItem("Exclusión Conceptual: ¿Cuál de las siguientes palabras NO pertenece a la categoría?", listOf("Manzana", "Pera", "Zanahoria", "Durazno"), 2),
            ReasoningItem("Categorización: 'Martillo, Alicate, Destornillador' pertenecen al grupo de:", listOf("Electrodomésticos", "Herramientas", "Instrumentos", "Vehículos"), 1),
            ReasoningItem("Opuesto Semántico: El antónimo más preciso de 'EFÍMERO' es:", listOf("Breve", "Duradero", "Rápido", "Frágil"), 1)
        ),
        2 to listOf(
            ReasoningItem("Analogía Funcional: 'BRÚJULA' es a 'DIRECCIÓN' como 'TERMÓMETRO' es a...", listOf("Clima", "Fiebre", "Temperatura", "Mercurio"), 2),
            ReasoningItem("Progresión Lógica: 3, 6, 12, 24, ... ¿Cuál es el siguiente término?", listOf("36", "42", "48", "50"), 2),
            ReasoningItem("Intruso Semántico: Identifica el concepto que se aleja del grupo:", listOf("Alegría", "Euforia", "Nostalgia", "Prudencia"), 3),
            ReasoningItem("Relación Parte-Todo: 'PÁGINA' es a 'CUADERNO' como 'TECLA' es a...", listOf("Música", "Piano", "Nota", "Dedo"), 1),
            ReasoningItem("Serie Alfabética: A, C, E, G, ... ¿Qué letra continúa?", listOf("H", "I", "J", "K"), 1)
        ),
        3 to listOf(
            ReasoningItem("Serie Alternante: 2, 5, 10, 17, 26, ... ¿Cuál es el siguiente número?", listOf("35", "37", "39", "41"), 1),
            ReasoningItem("Analogía Científica: 'NEURONA' es a 'SISTEMA NERVIOSO' como 'ALVÉOLO' es a...", listOf("Corazón", "Sistema Respiratorio", "Sangre", "Hígado"), 1),
            ReasoningItem("Silogismo: Todos los metales conducen electricidad. El cobre es un metal. Por tanto:", listOf("El cobre es magnético", "El cobre conduce electricidad", "Todo conductor es cobre", "El cobre es líquido"), 1),
            ReasoningItem("Exclusión Abstracta: ¿Qué palabra NO comparte el mismo campo semántico?", listOf("Hipótesis", "Teoría", "Axioma", "Metáfora"), 3),
            ReasoningItem("Razonamiento Proporcional: Si 4 máquinas producen 40 piezas en 1 hora, ¿cuántas producen 6 máquinas en 1 hora?", listOf("50", "60", "70", "80"), 1)
        ),
        4 to listOf(
            ReasoningItem("Progresión de Segundo Orden: 1, 2, 6, 24, 120, ... ¿Qué número sigue?", listOf("240", "480", "600", "720"), 3),
            ReasoningItem("Analogía Epistemológica: 'PRÓLOGO' es a 'NOVELA' como 'OBERTURA' es a...", listOf("Escultura", "Ópera", "Pintura", "Ensayo"), 1),
            ReasoningItem("Deducción Lógica: Si A es mayor que B, y C es menor que B, ¿cuál relación es verdadera?", listOf("C es mayor que A", "A es mayor que C", "A y C son iguales", "No se puede saber"), 1),
            ReasoningItem("Exclusión Léxica: Identifica el término que no es sinónimo de 'PERSPICAZ':", listOf("Sagaz", "Agudo", "Ingenuo", "Penetrante"), 2),
            ReasoningItem("Matriz Numérica: (8 → 64), (5 → 25), (11 → ?)", listOf("110", "121", "132", "144"), 1)
        ),
        5 to listOf(
            ReasoningItem("Inferencia Condicional: 'Si llueve, la calle se moja. La calle NO está mojada.' Se deduce necesariamente que:", listOf("Llovió hace poco", "No ha llovido", "Va a llover pronto", "El sol secó la calle"), 1),
            ReasoningItem("Serie Compleja: 2, 3, 5, 9, 17, 33, ... ¿Cuál es el siguiente valor?", listOf("49", "64", "65", "67"), 2),
            ReasoningItem("Analogía Abstracta: 'ENTROPÍA' es a 'CAOS' como 'HOMEOSTASIS' es a...", listOf("Movimiento", "Equilibrio", "Velocidad", "Asimetría"), 1),
            ReasoningItem("Razonamiento Verbal: 'MITIGAR' es a 'EXACERBAR' una relación de:", listOf("Sinonimia", "Causa-Efecto", "Antonimia", "Inclusión"), 2),
            ReasoningItem("Problema Lógico: En una carrera, adelantas al corredor que va en segundo lugar. ¿En qué posición quedas?", listOf("Primero", "Segundo", "Tercero", "Cuarto"), 1)
        ),
        6 to listOf(
            ReasoningItem("Silogismo Transitivo: Ningún mamífero es reptil. Todas las ballenas son mamíferos. Por consiguiente:", listOf("Algunas ballenas son reptiles", "Ninguna ballena es reptil", "Todo mamífero es ballena", "Los reptiles viven en el mar"), 1),
            ReasoningItem("Serie de Fibonacci Modificada: 3, 5, 8, 13, 21, 34, ... ¿Qué número continúa?", listOf("47", "52", "55", "68"), 2),
            ReasoningItem("Analogía Cognitiva: 'AMNESIA' es a 'MEMORIA' como 'AFASIA' es a...", listOf("Movimiento", "Lenguaje", "Visión", "Audición"), 1),
            ReasoningItem("Exclusión Epistemológica: ¿Qué concepto NO pertenece a la misma categoría lógica?", listOf("Deducción", "Inducción", "Abducción", "Refutación"), 3),
            ReasoningItem("Proporción Inversa: Si 3 pintores tardan 12 días en pintar un edificio, ¿cuánto tardarán 6 pintores al mismo ritmo?", listOf("4 días", "6 días", "8 días", "24 días"), 1)
        ),
        7 to listOf(
            ReasoningItem("Progresión Alternada Doble: 10, 20, 18, 36, 34, 68, ... ¿Cuál es el siguiente número?", listOf("64", "66", "70", "136"), 1),
            ReasoningItem("Analogía Metodológica: 'AXIOMA' es a 'GEOMETRÍA' como 'DOGMA' es a...", listOf("Ciencia", "Doctrina", "Hipótesis", "Experimento"), 1),
            ReasoningItem("Inferencia Proposicional: 'Solo si estudia, aprueba el examen. Juan aprobó el examen.' ¿Qué se concluye con certeza?", listOf("Juan no estudió", "Juan estudió", "El examen fue fácil", "Todos aprobaron"), 1),
            ReasoningItem("Precisión Léxica Superior: El término 'UBICUO' significa:", listOf("Escaso o raro", "Que está presente en todas partes", "De forma ambigua", "Antiguo o arcaico"), 1),
            ReasoningItem("Matriz Alfanumérica: A1, C4, E9, G16, ... ¿Cuál es el siguiente elemento?", listOf("H20", "I25", "I24", "J25"), 1)
        ),
        8 to listOf(
            ReasoningItem("Lógica de Predicados: 'Si algunos A son B, y ningún B es C', entonces es necesariamente verdadero que:", listOf("Todos los A son C", "Algunos A no son C", "Ningún A es C", "Todos los C son A"), 1),
            ReasoningItem("Serie Exponencial Compuesta: 2, 9, 28, 65, 126, ... (Regla: n³ + 1). ¿Cuál sigue?", listOf("197", "215", "217", "256"), 2),
            ReasoningItem("Analogía Filosófica: 'ONTOLOGÍA' es a 'SER' como 'DEONTOLOGÍA' es a...", listOf("Conocimiento", "Deber", "Belleza", "Lenguaje"), 1),
            ReasoningItem("Paradoja Deductiva: Si la afirmación 'Todos los informes tienen errores' es falsa, entonces:", listOf("Ningún informe tiene errores", "Al menos un informe no tiene errores", "Todos los informes son perfectos", "La mitad tiene errores"), 1),
            ReasoningItem("Abstracción Relacional: 'EFÍMERO : PERENNE' guarda la misma relación semántica que:", listOf("Opaco : Oscuro", "Diáfano : Turbio", "Veloz : Rápido", "Árido : Seco"), 1)
        )
    )

    fun forTier(tier: Int): List<ReasoningItem> = tiers[tier] ?: tiers.getValue(1)
}
