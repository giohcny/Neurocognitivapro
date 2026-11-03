package com.neurocognitiva.pro.domain

import com.neurocognitiva.pro.data.SessionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScoringTest {
    private fun session(activity: String, level: Int, accuracy: Int, rt: Int, validity: String = "OK", ts: Long = 0L) =
        SessionEntity(
            patientId = 1, activityId = activity, level = level, score = 0, accuracy = accuracy,
            avgReactionMs = rt, hits = 10, wrong = 0, omissions = 0, commissions = 0, dPrime = null,
            durationMs = 1000, validity = validity, inBattery = false, simulated = false, timestamp = ts
        )

    @Test fun umbralesDeClasificacion() {
        assertEquals(Classification.SUPERIOR, Scoring.classify(88))
        assertEquals(Classification.NORMAL_ALTO, Scoring.classify(87))
        assertEquals(Classification.NORMAL_ALTO, Scoring.classify(75))
        assertEquals(Classification.NORMAL, Scoring.classify(60))
        assertEquals(Classification.DESCENSO_LEVE, Scoring.classify(45))
        assertEquals(Classification.REQUIERE, Scoring.classify(44))
    }

    @Test fun probitYdPrime() {
        assertEquals(1.95996, Scoring.probit(0.975), 1e-4)
        assertEquals(0.0, Scoring.probit(0.5), 1e-9)
        assertEquals(0.0, Scoring.dPrime(5, 5, 5, 5), 1e-9)
        assertEquals(2.194, Scoring.dPrime(9, 1, 1, 9), 0.01)
        assertTrue(Scoring.dPrime(10, 0, 0, 10) > Scoring.dPrime(9, 1, 1, 9))
    }

    @Test fun eficienciaDePlanificacion() {
        assertEquals(100, Scoring.planningAccuracy(7, 7, 0))
        assertEquals(50, Scoring.planningAccuracy(7, 14, 0))
        assertEquals(50, Scoring.planningAccuracy(7, 7, 7))
        assertEquals(0, Scoring.planningAccuracy(7, 0, 3))
    }

    @Test fun recomendacionAdaptativa() {
        assertEquals(5, Scoring.recommendNext(4, 90, Validity.OK).level)
        assertEquals(8, Scoring.recommendNext(8, 95, Validity.OK).level)
        assertEquals(3, Scoring.recommendNext(4, 40, Validity.OK).level)
        assertEquals(1, Scoring.recommendNext(1, 40, Validity.OK).level)
        assertEquals(4, Scoring.recommendNext(4, 70, Validity.OK).level)
        assertEquals(4, Scoring.recommendNext(4, 99, Validity.CHANCE).level)
    }

    @Test fun indiceNormalizadoPorAmbito() {
        // Precisión ponderada = (90*4 + 80*5)/9 = 84; dominado = N5; techo esperado = 6; TR 800 ms => velocidad 85
        val sessions = listOf(
            session("corsi_matrix", 4, 90, 800, ts = 1),
            session("corsi_matrix", 5, 80, 800, ts = 2)
        )
        val m = Scoring.computeMetrics(sessions, expectedTopLevel = 6).first { it.activity.id == "corsi_matrix" }
        assertEquals(5, m.masteredLevel)
        assertEquals(84, m.accuracy)
        assertEquals(84, m.indexScore)
        assertEquals(Classification.NORMAL_ALTO, m.classification)
        assertTrue(m.ceilingNotReached)                    // dominó el 5 de un techo de 6
        assertEquals(false, Scoring.computeMetrics(emptyList(), 6).first().played)
    }

    @Test fun sesionesDeAzarSeExcluyenDelIndice() {
        val sessions = listOf(
            session("corsi_matrix", 3, 90, 800, ts = 1),
            session("corsi_matrix", 8, 100, 100, validity = Validity.ANTICIPATORY.code, ts = 2)
        )
        val m = Scoring.computeMetrics(sessions, 8).first { it.activity.id == "corsi_matrix" }
        assertEquals(3, m.maxLevel)
        assertEquals(1, m.invalidSessions)
    }

    @Test fun validezDeLaSesion() {
        val level = Catalog.byId("semantic_reasoning").level(3)   // 4 opciones => azar 25 %
        assertEquals(Validity.CHANCE, Scoring.evaluateValidity(level, 1, 5, 17, 3000, false, null))
        assertEquals(Validity.OK, Scoring.evaluateValidity(level, 5, 0, 100, 3000, false, null))
        assertEquals(Validity.INCOMPLETE, Scoring.evaluateValidity(level, 3, 0, 100, 3000, true, null))
        assertEquals(Validity.CHANCE, Scoring.evaluateValidity(level, 6, 4, 60, 3000, false, 0.1))
    }
}
