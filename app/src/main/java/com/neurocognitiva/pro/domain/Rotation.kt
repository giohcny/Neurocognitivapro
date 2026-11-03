package com.neurocognitiva.pro.domain

import kotlin.random.Random

typealias Grid = List<List<Int>>

data class RotationOption(val grid: Grid, val correct: Boolean)
data class RotationRound(val base: Grid, val angle: Int, val options: List<RotationOption>)

/**
 * Generador de ítems de rotación mental.
 * Mejora respecto de la versión web: todas las opciones tienen EXACTAMENTE el mismo número de
 * celdas (en el original el distractor cambiaba 1 celda y delataba la respuesta por conteo),
 * y se incluyen distractores por ángulo equivocado y por reflejo en espejo.
 */
object Rotation {
    private val DIRS = listOf(intArrayOf(0, 1), intArrayOf(1, 0), intArrayOf(0, -1), intArrayOf(-1, 0))

    fun rotate90(g: Grid): Grid {
        val n = g.size
        return List(n) { r -> List(n) { c -> g[n - 1 - c][r] } }   // sentido horario
    }

    fun rotate(g: Grid, angle: Int): Grid {
        var out = g
        repeat((angle / 90) % 4) { out = rotate90(out) }
        return out
    }

    fun mirror(g: Grid): Grid = g.map { it.reversed() }

    fun key(g: Grid): String = g.joinToString("|") { row -> row.joinToString("") }

    /** Una figura es quiral si su espejo no coincide con ninguna de sus rotaciones. */
    fun isChiral(g: Grid): Boolean {
        val mirrored = key(mirror(g))
        return (0 until 4).none { key(rotate(g, it * 90)) == mirrored }
    }

    fun randomFigure(n: Int, cells: Int, rnd: Random = Random.Default): Grid {
        val target = cells.coerceIn(1, n * n - 1)
        var best: Grid? = null
        repeat(60) {
            val g = Array(n) { IntArray(n) }
            val filled = ArrayList<IntArray>()
            val mid = n / 2
            g[mid][mid] = 1
            filled.add(intArrayOf(mid, mid))
            var guard = 0
            while (filled.size < target && guard < 600) {
                guard++
                val from = filled[rnd.nextInt(filled.size)]
                val d = DIRS[rnd.nextInt(DIRS.size)]
                val nr = from[0] + d[0]
                val nc = from[1] + d[1]
                if (nr in 0 until n && nc in 0 until n && g[nr][nc] == 0) {
                    g[nr][nc] = 1
                    filled.add(intArrayOf(nr, nc))
                }
            }
            if (filled.size == target) {
                val grid: Grid = g.map { it.toList() }
                best = grid
                if (isChiral(grid)) return grid
            }
        }
        return best ?: List(n) { r -> List(n) { c -> if (r == n / 2 && c < target) 1 else 0 } }
    }

    /** Mueve una celda a otra posición vacía (preferentemente adyacente): mismo conteo de celdas. */
    fun moveOneCell(g: Grid, rnd: Random = Random.Default): Grid {
        val n = g.size
        val filled = ArrayList<Pair<Int, Int>>()
        val empty = ArrayList<Pair<Int, Int>>()
        for (r in 0 until n) for (c in 0 until n) {
            if (g[r][c] == 1) filled.add(r to c) else empty.add(r to c)
        }
        if (filled.isEmpty() || empty.isEmpty()) return g
        val adjacent = empty.filter { (r, c) ->
            DIRS.any { d -> g.getOrNull(r + d[0])?.getOrNull(c + d[1]) == 1 }
        }
        val src = filled[rnd.nextInt(filled.size)]
        val pool = adjacent.ifEmpty { empty }
        val dst = pool[rnd.nextInt(pool.size)]
        return List(n) { r ->
            List(n) { c ->
                when {
                    r == src.first && c == src.second -> 0
                    r == dst.first && c == dst.second -> 1
                    else -> g[r][c]
                }
            }
        }
    }

    fun buildRound(n: Int, cells: Int, angles: List<Int>, rnd: Random = Random.Default): RotationRound {
        var base = randomFigure(n, cells, rnd)
        var angle = angles[rnd.nextInt(angles.size)]
        var attempts = 0
        // Evita figuras que al rotar coinciden consigo mismas (ítem trivial)
        while (key(rotate(base, angle)) == key(base) && attempts < 30) {
            base = randomFigure(n, cells, rnd)
            angle = angles[rnd.nextInt(angles.size)]
            attempts++
        }
        val correct = rotate(base, angle)
        val used = hashSetOf(key(correct))
        val distractors = ArrayList<Grid>()
        fun tryAdd(g: Grid) {
            if (distractors.size < 3 && used.add(key(g))) distractors.add(g)
        }
        val wrongAngles = listOf(90, 180, 270).filter { it != angle }
        tryAdd(mirror(correct))
        tryAdd(rotate(base, wrongAngles[rnd.nextInt(wrongAngles.size)]))
        tryAdd(mirror(rotate(base, wrongAngles[rnd.nextInt(wrongAngles.size)])))
        var guard = 0
        while (distractors.size < 3 && guard < 100) {
            guard++
            tryAdd(moveOneCell(correct, rnd))
        }
        val options = (listOf(RotationOption(correct, true)) + distractors.map { RotationOption(it, false) })
            .shuffled(rnd)
        return RotationRound(base, angle, options)
    }
}
