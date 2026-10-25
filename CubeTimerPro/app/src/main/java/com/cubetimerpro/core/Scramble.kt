package com.cubetimerpro.core

import kotlin.random.Random

/**
 * Random-move scrambles in standard notation for practice use.
 * These are NOT random-state/official scrambles and must never be labelled as such.
 */
object ScrambleGenerator {
    private val SUFFIX = listOf("", "'", "2")
    private val FACES = listOf("U", "D", "R", "L", "F", "B")

    fun generate(event: CubeEvent, rng: Random = Random.Default): String = when (event) {
        CubeEvent.TWO -> nxn(rng, 2, 9)
        CubeEvent.THREE, CubeEvent.THREE_OH -> nxn(rng, 3, 20)
        CubeEvent.FOUR -> nxn(rng, 4, 40)
        CubeEvent.FIVE -> nxn(rng, 5, 60)
        CubeEvent.SIX -> nxn(rng, 6, 80)
        CubeEvent.SEVEN -> nxn(rng, 7, 100)
        CubeEvent.PYRAMINX -> pyraminx(rng)
        CubeEvent.SKEWB -> skewb(rng)
        CubeEvent.MEGAMINX -> megaminx(rng)
    }

    private fun axisOf(f: String) = when (f) { "U", "D" -> 0; "R", "L" -> 1; else -> 2 }

    private fun nxn(rng: Random, n: Int, len: Int): String {
        val faces = if (n == 2) listOf("R", "U", "F") else FACES
        val out = ArrayList<String>()
        var lastAxis = -1
        var used = mutableSetOf<String>()
        while (out.size < len) {
            val f = faces[rng.nextInt(faces.size)]
            val ax = axisOf(f)
            if (ax == lastAxis && f in used) continue   // no repeated face within an axis run
            if (ax != lastAxis) { used = mutableSetOf(); lastAxis = ax }
            used.add(f)
            val forms = mutableListOf(f)
            if (n >= 4) forms.add(f + "w")
            if (n >= 6) forms.add("3" + f + "w")
            out += forms[rng.nextInt(forms.size)] + SUFFIX[rng.nextInt(3)]
        }
        return out.joinToString(" ")
    }

    private fun pyraminx(rng: Random): String {
        val moves = ArrayList<String>()
        var last = ""
        while (moves.size < 10) {
            val f = listOf("U", "L", "R", "B")[rng.nextInt(4)]
            if (f == last) continue
            last = f
            moves += f + if (rng.nextBoolean()) "'" else ""
        }
        val tips = listOf("u", "l", "r", "b").mapNotNull {
            when (rng.nextInt(3)) { 1 -> it; 2 -> "$it'"; else -> null }
        }
        return (moves + tips).joinToString(" ")
    }

    private fun skewb(rng: Random): String {
        val moves = ArrayList<String>()
        var last = ""
        while (moves.size < 11) {
            val f = listOf("R", "U", "L", "B")[rng.nextInt(4)]
            if (f == last) continue
            last = f
            moves += f + if (rng.nextBoolean()) "'" else ""
        }
        return moves.joinToString(" ")
    }

    /** Pochmann-style megaminx scramble: 7 lines, returned newline-separated. */
    private fun megaminx(rng: Random): String = (0 until 7).joinToString("\n") {
        val row = (0 until 10).joinToString(" ") { i ->
            (if (i % 2 == 0) "R" else "D") + if (rng.nextBoolean()) "++" else "--"
        }
        row + if (rng.nextBoolean()) " U" else " U'"
    }
}
