package com.neurocognitiva.pro.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClinicalTest {
    @Test fun rutValido() {
        assertEquals(true, Rut.validate("12.345.678-5"))
        assertEquals(true, Rut.validate("18452910-6"))
        assertEquals(true, Rut.validate("7.612.490-6"))
    }

    @Test fun rutInvalidoOSinFormato() {
        assertEquals(false, Rut.validate("12.345.678-9"))
        assertNull(Rut.validate("pasaporte AB123"))
    }

    @Test fun protocoloYAmbito() {
        assertEquals(3, Protocols.maxLevel("Tamizaje Cognitivo Rápido (Niveles 1-3)"))
        assertEquals(6, Protocols.maxLevel(Protocols.DEFAULT))
        assertEquals(8, Protocols.maxLevel("Exploración Neuropsicológica Profunda (Niveles 1-8)"))
        assertEquals(4..8, Tiers.range("Alto Rendimiento Cognitivo / Académico-Laboral"))
    }

    @Test fun bateriaSubeHastaDosVecesYAvanza() {
        var s = Battery.start(Tiers.DEFAULT, Protocols.DEFAULT)
        assertEquals(2, s.level)
        s = Battery.advance(s, 90, Validity.OK)!!
        assertEquals(3, s.level)
        s = Battery.advance(s, 90, Validity.OK)!!
        assertEquals(4, s.level)
        s = Battery.advance(s, 90, Validity.OK)!!      // ya subió 2 veces: pasa a la siguiente prueba
        assertEquals(1, s.index)
        assertEquals(2, s.level)
    }

    @Test fun bateriaNoSuperaProtocoloYTerminaNull() {
        val s = Battery.start("Alto Rendimiento Cognitivo / Académico-Laboral", "Tamizaje Cognitivo Rápido (Niveles 1-3)")
        assertEquals(3, s.level)                          // 4 recortado al máximo del protocolo
        val last = s.copy(index = s.plan.size - 1)
        assertNull(Battery.advance(last, 50, Validity.OK))
    }

    @Test fun catalogoCompleto() {
        assertEquals(10, Catalog.activities.size)
        Catalog.activities.forEach { assertEquals(8, it.levels.size) }
        assertEquals(10, Battery.ORDER.toSet().intersect(Catalog.activities.map { it.id }.toSet()).size)
        (1..8).forEach { assertEquals(5, ReasoningBank.forTier(it).size) }
    }
}
