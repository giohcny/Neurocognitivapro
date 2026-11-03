package com.neurocognitiva.pro.domain

import kotlin.random.Random

data class ArithItem(val expr: String, val answer: Int, val options: List<Int>)

object Arithmetic {
    fun generate(tier: Int, rnd: Random = Random.Default): ArithItem {
        val (expr, answer) = when (tier) {
            1 -> {
                val a = 3 + rnd.nextInt(9)
                val b = 2 + rnd.nextInt(8)
                if (a > b && rnd.nextBoolean()) "$a − $b = ?" to (a - b) else "$a + $b = ?" to (a + b)
            }
            2 -> {
                if (rnd.nextBoolean()) {
                    val a = 14 + rnd.nextInt(25)
                    val b = 6 + rnd.nextInt(14)
                    "$a + $b = ?" to (a + b)
                } else {
                    val a = 20 + rnd.nextInt(19)   // siempre > b: evita resultados negativos
                    val b = 6 + rnd.nextInt(14)
                    "$a − $b = ?" to (a - b)
                }
            }
            3 -> {
                val a = 4 + rnd.nextInt(8)
                val b = 3 + rnd.nextInt(7)
                val c = 2 + rnd.nextInt(9)
                "($a × $b) + $c = ?" to (a * b + c)
            }
            4 -> {
                val x = 5 + rnd.nextInt(15)
                val b = 8 + rnd.nextInt(12)
                "¿Qué valor tiene X en: X + $b = ${x + b}?" to x
            }
            5 -> {
                val a = 6 + rnd.nextInt(7)
                val b = 4 + rnd.nextInt(6)
                val c = 5 + rnd.nextInt(10)
                "($a × $b) − $c + 10 = ?" to (a * b - c + 10)
            }
            6 -> {
                val pcts = listOf(10, 20, 25, 50)
                val p = pcts[rnd.nextInt(pcts.size)]
                val base = (2 + rnd.nextInt(8)) * 20
                "¿Cuánto es el $p% de $base?" to (p * base / 100)
            }
            7 -> {
                val n = 4 + rnd.nextInt(8)
                val sub = 3 + rnd.nextInt(10)
                "(${n}²) − $sub = ?" to (n * n - sub)
            }
            else -> {
                val a = 12 + rnd.nextInt(10)
                val b = 3 + rnd.nextInt(4)
                val c = 8 + rnd.nextInt(12)
                "($a × $b) + ($c × 2) = ?" to (a * b + c * 2)
            }
        }

        val opts = linkedSetOf(answer)
        val deltas = listOf(-10, -6, -4, -2, -1, 1, 2, 4, 6, 10)
        var guard = 0
        while (opts.size < 4 && guard < 300) {
            guard++
            val candidate = answer + deltas[rnd.nextInt(deltas.size)]
            if (candidate >= 0) opts.add(candidate)
        }
        return ArithItem(expr, answer, opts.toList().sorted())
    }
}
