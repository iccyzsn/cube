package com.cubetimerpro.core

data class TimerResult(val rawMs: Long, val inspectionMs: Long?, val inspectionPenalty: Penalty)

/**
 * Pure state machine; no Android or UI dependencies. Time source is injected
 * (use SystemClock.elapsedRealtimeNanos() in the app) so it is monotonic and testable.
 *
 * With inspection: press = start inspection -> press = hold -> release (after holdMs) = run -> press = stop.
 * Without:         press = hold -> release (after holdMs) = run -> press = stop.
 * A release that follows a stop-press is ignored (prevents accidental restarts / duplicate stops).
 */
class TimerEngine(private val nanoClock: () -> Long) {
    enum class State { IDLE, INSPECTING, HOLDING, RUNNING, STOPPED }

    var holdMs: Long = 300
    var inspectionEnabled: Boolean = true
    var inspectionSeconds: Int = 15

    var state: State = State.IDLE
        private set
    var lastResult: TimerResult? = null
        private set

    private var startNs = 0L
    private var inspectStartNs = -1L
    private var holdStartNs = 0L

    fun isReady(): Boolean = state == State.HOLDING && (nanoClock() - holdStartNs) / 1_000_000 >= holdMs

    fun elapsedMs(): Long = when (state) {
        State.RUNNING -> (nanoClock() - startNs) / 1_000_000
        State.STOPPED -> lastResult?.rawMs ?: 0
        else -> 0
    }

    /** Milliseconds spent in inspection so far, or null if inspection is not in progress. */
    fun inspectionElapsedMs(): Long? =
        if (inspectStartNs >= 0 && (state == State.INSPECTING || state == State.HOLDING))
            (nanoClock() - inspectStartNs) / 1_000_000 else null

    /** Returns a result only when this press stops a running solve. */
    fun press(): TimerResult? {
        val now = nanoClock()
        when (state) {
            State.IDLE, State.STOPPED -> {
                if (inspectionEnabled) { inspectStartNs = now; state = State.INSPECTING }
                else { inspectStartNs = -1; holdStartNs = now; state = State.HOLDING }
            }
            State.INSPECTING -> { holdStartNs = now; state = State.HOLDING }
            State.HOLDING -> Unit
            State.RUNNING -> {
                val r = lastResultFor(now)
                lastResult = r
                state = State.STOPPED
                return r
            }
        }
        return null
    }

    private var pendingInspectionMs: Long? = null
    private var pendingPenalty = Penalty.NONE

    private fun lastResultFor(now: Long) =
        TimerResult((now - startNs) / 1_000_000, pendingInspectionMs, pendingPenalty)

    fun release() {
        val now = nanoClock()
        if (state != State.HOLDING) return
        if ((now - holdStartNs) / 1_000_000 >= holdMs) {
            pendingInspectionMs = if (inspectStartNs >= 0) (now - inspectStartNs) / 1_000_000 else null
            pendingPenalty = pendingInspectionMs?.let { inspectionPenalty(it) } ?: Penalty.NONE
            inspectStartNs = -1
            startNs = now
            state = State.RUNNING
        } else {
            state = if (inspectStartNs >= 0) State.INSPECTING else State.IDLE
        }
    }

    /** 15–17 s => +2, over 17 s => DNF (with the default 15 s inspection). */
    fun inspectionPenalty(ms: Long): Penalty {
        val limit = inspectionSeconds * 1000L
        return when {
            ms > limit + 2000 -> Penalty.DNF
            ms > limit -> Penalty.PLUS2
            else -> Penalty.NONE
        }
    }

    fun reset() { state = State.IDLE; inspectStartNs = -1 }
}
