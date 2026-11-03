package com.neurocognitiva.pro.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GeneratorsTest {
    private fun cells(g: Grid) = g.sumOf { row -> row.sum() }

    @Test fun rotacionOpcionesEquilibradas() {
        val configs = listOf(Triple(3, 3, listOf(90)), Triple(4, 5, listOf(90, 180)), Triple(5, 7, listOf(90, 180, 270)), Triple(6, 10, listOf(90, 180, 270)))
        for ((n, c, angles) in configs) {
            for (seed in 0 until 60) {
                val round = Rotation.buildRound(n, c, angles, Random(seed))
                assertEquals(4, round.options.size)
                assertEquals(1, round.options.count { it.correct })
                assertEquals(4, round.options.map { Rotation.key(it.grid) }.toSet().size)
                round.options.forEach { assertEquals("mismo nº de celdas", c, cells(it.grid)) }
                val correct = round.options.first { it.correct }.grid
                assertEquals(Rotation.key(Rotation.rotate(round.base, round.angle)), Rotation.key(correct))
            }
        }
    }

    @Test fun rotacionHorariaDe90() {
        val g = listOf(listOf(1, 0, 0), listOf(0, 0, 0), listOf(0, 0, 0))
        assertEquals(listOf(listOf(0, 0, 1), listOf(0, 0, 0), listOf(0, 0, 0)), Rotation.rotate90(g))
        assertEquals(g, Rotation.rotate(g, 360))
    }

    @Test fun aritmeticaSinNegativosYConRespuestaPresente() {
        for (tier in 1..8) {
            repeat(300) {
                val item = Arithmetic.generate(tier, Random(it))
                assertTrue("tier $tier: ${item.expr}", item.answer >= 0)
                assertEquals(4, item.options.size)
                assertEquals(4, item.options.toSet().size)
                assertTrue(item.answer in item.options)
                assertTrue(item.options.all { o -> o >= 0 })
            }
        }
    }
}
