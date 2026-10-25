package com.cubetimerpro

import com.cubetimerpro.core.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class CubeTest {
    private val s = CubieCube.SOLVED

    @Test fun turnAndInverse() { assertTrue(s.applyAlg("U U'").isSolved()); assertFalse(s.applyAlg("R").isSolved()) }

    @Test fun fourQuarterTurnsAreIdentity() {
        for (f in "URFDLB") { assertTrue(s.applyAlg("$f $f $f $f").isSolved()); assertTrue(s.applyAlg("${f}2 ${f}2").isSolved()) }
    }

    @Test fun halfTurnEqualsTwoQuarterTurns() { for (f in "URFDLB") assertEquals(s.applyAlg("$f $f"), s.applyAlg("${f}2")) }

    @Test fun triggerHasOrderSix() {
        assertTrue(s.applyAlg("R U R' U' ".repeat(6)).isSolved())
        assertFalse(s.applyAlg("R U R' U' ".repeat(3)).isSolved())
    }

    @Test fun solvedFaceletsAreUniform() {
        val f = s.toFacelets()
        for (face in 0..5) for (i in 0..8) assertEquals("URFDLB"[face], f[face * 9 + i])
    }

    @Test fun rTurnMovesFrontColumnOntoUp() {
        val f = s.applyAlg("R").toFacelets()
        assertEquals('F', f[2]); assertEquals('F', f[5]); assertEquals('F', f[8]) // U right column now green(F)
        assertEquals('D', f[20]); assertEquals('D', f[23]); assertEquals('D', f[26]) // D colour now on F right column
    }

    @Test fun inverseUndoesAlgorithm() {
        for (a in AlgDb.cases) assertTrue(a.id, s.applyAlg(a.alg).applyAlg(Alg.invert(a.alg)).isSolved())
    }

    @Test fun pllsPreserveF2LAndOrientation() {
        for (a in AlgDb.cases.filter { it.group == "PLL" }) {
            val c = s.applyAlg(a.alg)
            assertFalse(a.id, c.isSolved())
            assertTrue(a.id, c.co.all { it == 0 } && c.eo.all { it == 0 })
            for (i in 4..7) assertEquals(a.id, i, c.cp[i])
            for (i in 4..11) assertEquals(a.id, i, c.ep[i])
        }
    }

    @Test fun ollsPreserveF2L() {
        for (a in AlgDb.cases.filter { it.group == "OLL" }) {
            val c = s.applyAlg(a.alg)
            for (i in 4..7) { assertEquals(i, c.cp[i]); assertEquals(0, c.co[i]) }
            for (i in 4..11) { assertEquals(i, c.ep[i]); assertEquals(0, c.eo[i]) }
        }
    }

    @Test fun pllOrders() {
        assertTrue(s.applyAlg("${AlgDb.cases.first { it.id == "pll_t" }.alg} ${AlgDb.cases.first { it.id == "pll_t" }.alg}").isSolved())
        val ua = AlgDb.cases.first { it.id == "pll_ua" }.alg
        assertTrue(s.applyAlg("$ua $ua $ua").isSolved())
        assertEquals(Alg.invert(ua), AlgDb.cases.first { it.id == "pll_ub" }.alg)
    }

    @Test fun restartIsDeterministic() {
        val a = s.applyAlg("R U F'"); val b = s.applyAlg("R U F'")
        assertEquals(a, b)
    }

    @Test fun unsupportedMovesAreRejected() { assertThrows(IllegalArgumentException::class.java) { Alg.parse("M2") } }

    @Test fun threeByThreeScramblesParseAndFollowRules() {
        repeat(50) {
            val sc = ScrambleGenerator.generate(CubeEvent.THREE, Random(it))
            val t = sc.split(" ")
            assertEquals(20, t.size)
            Alg.parse(sc)
            for (i in 1 until t.size) assertNotEquals(t[i - 1][0], t[i][0])
        }
    }

    @Test fun allEventsProduceScrambles() { for (e in CubeEvent.values()) assertTrue(ScrambleGenerator.generate(e).isNotBlank()) }
}
