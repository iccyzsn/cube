package com.cubetimerpro.core

import java.util.Locale

/** Events with fully implemented timing + scramble support. BLD, FMC, Clock and Square-1 are intentionally NOT exposed yet. */
enum class CubeEvent(val label: String) {
    THREE("3×3×3"), TWO("2×2×2"), FOUR("4×4×4"), FIVE("5×5×5"), SIX("6×6×6"), SEVEN("7×7×7"),
    THREE_OH("3×3 One-Handed"), PYRAMINX("Pyraminx"), SKEWB("Skewb"), MEGAMINX("Megaminx")
}

enum class Penalty(val code: Int) {
    NONE(0), PLUS2(1), DNF(2);
    companion object { fun fromCode(c: Int): Penalty = values().firstOrNull { it.code == c } ?: NONE }
}

/** Raw time is never overwritten; final result is derived. null == DNF. */
fun finalResultMs(rawMs: Long, penalty: Penalty): Long? = when (penalty) {
    Penalty.NONE -> rawMs
    Penalty.PLUS2 -> rawMs + 2000
    Penalty.DNF -> null
}

object TimeFormat {
    /** Truncates (never rounds up) to the requested decimals, as competition timers do. */
    fun format(ms: Long?, decimals: Int = 2): String {
        if (ms == null) return "DNF"
        val minutes = ms / 60000
        val rem = ms % 60000
        val secs = rem / 1000
        val frac = if (decimals == 3) String.format(Locale.US, "%03d", rem % 1000)
        else String.format(Locale.US, "%02d", (rem % 1000) / 10)
        return if (minutes > 0) String.format(Locale.US, "%d:%02d.%s", minutes, secs, frac)
        else String.format(Locale.US, "%d.%s", secs, frac)
    }
}
