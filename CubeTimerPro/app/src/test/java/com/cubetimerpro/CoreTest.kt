package com.cubetimerpro

import com.cubetimerpro.core.*
import com.cubetimerpro.core.TimerEngine.State
import org.junit.Assert.*
import org.junit.Test

class StatsTest {
    private fun l(vararg v: Long?) = v.toList()

    @Test fun ao5DropsBestAndWorst() = assertEquals(11000L, Stats.average(l(9000, 10000, 11000, 12000, 13000)))
    @Test fun ao5OneDnfIsAllowed() = assertEquals(12000L, Stats.average(l(9000, 10000, 11000, 15000, null)))
    @Test fun ao5TwoDnfsIsDnf() = assertNull(Stats.average(l(9000, 10000, 11000, null, null)))
    @Test fun insufficientSolves() { assertNull(Stats.average(l(1, 2, 3, 4))); assertNull(Stats.currentAverage(l(1, 2, 3, 4), 5)) }
    @Test fun ties() = assertEquals(10000L, Stats.average(l(10000, 10000, 10000, 10000, 10000)))
    @Test fun ao12TrimsOneEachSide() {
        val v = (1L..12L).map { it * 1000 }
        assertEquals(6500L, Stats.average(v))
    }
    @Test fun ao100TrimsFive() { assertEquals(5, Stats.trimCount(100)); assertEquals(1, Stats.trimCount(12)) }
    @Test fun ao100WithFiveDnfsStillValid() {
        val v = (1..95).map { 10000L } + List(5) { null }
        assertEquals(10000L, Stats.average(v))
        assertNull(Stats.average((1..94).map { 10000L } + List(6) { null }))
    }
    @Test fun bestAverageAndRolling() {
        val v = l(20000, 20000, 20000, 20000, 20000, 10000, 10000, 10000, 10000)
        assertEquals(10000L, Stats.bestAverage(v, 5))
        assertEquals(20000L, Stats.currentAverage(v.take(5), 5))
        assertNull(Stats.rolling(v, 5)[3])
    }
    @Test fun sessionMeanIgnoresDnf() = assertEquals(15000L, Stats.sessionMean(l(10000, null, 20000)))
    @Test fun penalties() {
        assertEquals(12340L, finalResultMs(12340, Penalty.NONE))
        assertEquals(14340L, finalResultMs(12340, Penalty.PLUS2))
        assertNull(finalResultMs(12340, Penalty.DNF))
    }
    @Test fun formatting() {
        assertEquals("12.34", TimeFormat.format(12349))
        assertEquals("1:02.50", TimeFormat.format(62500))
        assertEquals("0.07", TimeFormat.format(70))
        assertEquals("DNF", TimeFormat.format(null))
        assertEquals("9.876", TimeFormat.format(9876, 3))
    }
}

class TimerEngineTest {
    private var t = 0L
    private fun ms(x: Long) = x * 1_000_000
    private fun engine(insp: Boolean) = TimerEngine { t }.also { it.inspectionEnabled = insp; it.holdMs = 300 }

    @Test fun basicSolveWithoutInspection() {
        t = 0; val e = engine(false)
        assertNull(e.press()); assertEquals(State.HOLDING, e.state)
        t = ms(400); e.release(); assertEquals(State.RUNNING, e.state)
        t = ms(10400); val r = e.press()!!
        assertEquals(10000L, r.rawMs); assertEquals(State.STOPPED, e.state)
        e.release(); assertEquals(State.STOPPED, e.state) // release after stop is ignored
    }

    @Test fun shortHoldDoesNotStart() {
        t = 0; val e = engine(false); e.press(); t = ms(100); e.release()
        assertEquals(State.IDLE, e.state)
    }

    @Test fun inspectionPenalties() {
        fun run(holdAt: Long): TimerResult {
            t = 0; val e = engine(true); e.press(); e.release()
            t = ms(holdAt); e.press(); t = ms(holdAt + 400); e.release()
            t = ms(holdAt + 10400); return e.press()!!
        }
        assertEquals(Penalty.NONE, run(14000).inspectionPenalty)
        assertEquals(Penalty.PLUS2, run(16000).inspectionPenalty)
        assertEquals(Penalty.DNF, run(18000).inspectionPenalty)
        assertEquals(10000L, run(16000).rawMs)
    }

    @Test fun stopIsReportedOnce() {
        t = 0; val e = engine(false); e.press(); t = ms(400); e.release(); t = ms(5000)
        assertNotNull(e.press()); assertNull(e.press()) // second press begins a new attempt, no second result
    }
}
