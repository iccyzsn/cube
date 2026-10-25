package com.cubetimerpro.core

import kotlin.math.ceil

/** All inputs are final results in ms, oldest first; null == DNF. */
object Stats {
    fun best(results: List<Long?>): Long? = results.filterNotNull().minOrNull()

    /** Arithmetic mean of valid (non-DNF) solves; for session overview, not a competition mean. */
    fun sessionMean(results: List<Long?>): Long? = roundedMean(results.filterNotNull())

    fun trimCount(n: Int): Int = ceil(n * 0.05).toInt().coerceAtLeast(1)

    /** Competition-style trimmed average over exactly this window (needs >= 5). DNFs rank as worst. */
    fun average(window: List<Long?>): Long? {
        val n = window.size
        if (n < 5) return null
        val trim = trimCount(n)
        val sorted = window.sortedWith(compareBy<Long?> { it == null }.thenBy { it ?: 0L })
        val kept = sorted.subList(trim, n - trim)
        if (kept.any { it == null }) return null
        return roundedMean(kept.filterNotNull())
    }

    fun currentAverage(results: List<Long?>, n: Int): Long? =
        if (results.size < n) null else average(results.takeLast(n))

    fun rolling(results: List<Long?>, n: Int): List<Long?> =
        results.indices.map { i -> if (i + 1 < n) null else average(results.subList(i + 1 - n, i + 1)) }

    fun bestAverage(results: List<Long?>, n: Int): Long? = rolling(results, n).filterNotNull().minOrNull()

    private fun roundedMean(v: List<Long>): Long? =
        if (v.isEmpty()) null else (v.sum() + v.size / 2) / v.size
}
