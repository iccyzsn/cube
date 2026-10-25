package com.cubetimerpro.core

/** Face order everywhere: U R F D L B (indices 0..5). Supports face turns U R F D L B with ', 2. */
data class Move(val face: Int, val power: Int) {
    override fun toString(): String = "${"URFDLB"[face]}${when (power) { 1 -> ""; 2 -> "2"; else -> "'" }}"
}

object Alg {
    fun parse(s: String): List<Move> =
        s.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.map { parseMove(it) }

    fun parseMove(t: String): Move {
        val face = "URFDLB".indexOf(t[0])
        require(face >= 0) { "Unsupported move '$t' (only U R F D L B turns are implemented)" }
        val power = when (t.substring(1)) {
            "" -> 1; "2", "2'" -> 2; "'" -> 3
            else -> throw IllegalArgumentException("Bad move '$t'")
        }
        return Move(face, power)
    }

    fun invert(s: String): String = parse(s).reversed().joinToString(" ") { Move(it.face, 4 - it.power).toString() }
}

/**
 * Cubie-level 3x3 state (corner/edge permutation + orientation), independent of any renderer.
 * Piece indices: corners URF UFL ULB UBR DFR DLF DBL DRB; edges UR UF UL UB DR DF DL DB FR FL BL BR.
 */
class CubieCube(
    val cp: IntArray = IntArray(8) { it },
    val co: IntArray = IntArray(8),
    val ep: IntArray = IntArray(12) { it },
    val eo: IntArray = IntArray(12)
) {
    fun multiply(b: CubieCube): CubieCube {
        val ncp = IntArray(8); val nco = IntArray(8); val nep = IntArray(12); val neo = IntArray(12)
        for (c in 0..7) { ncp[c] = cp[b.cp[c]]; nco[c] = (co[b.cp[c]] + b.co[c]) % 3 }
        for (e in 0..11) { nep[e] = ep[b.ep[e]]; neo[e] = (eo[b.ep[e]] + b.eo[e]) % 2 }
        return CubieCube(ncp, nco, nep, neo)
    }

    fun applyMove(m: Move): CubieCube { var r = this; repeat(m.power) { r = r.multiply(BASE[m.face]) }; return r }
    fun applyMoves(ms: List<Move>): CubieCube = ms.fold(this) { c, m -> c.applyMove(m) }
    fun applyAlg(alg: String): CubieCube = applyMoves(Alg.parse(alg))

    fun isSolved(): Boolean = this == SOLVED

    /** 54 chars in U R F D L B order, 9 per face, each char the colour's home face. */
    fun toFacelets(): CharArray {
        val fc = CharArray(54)
        for (i in 0..7) {
            val j = cp[i]; val ori = co[i]
            for (n in 0..2) fc[idx(CORNER_FACELET[i], (n + ori) % 3)] = CORNER_COLOR[j][n]
        }
        for (i in 0..11) {
            val j = ep[i]; val ori = eo[i]
            for (n in 0..1) fc[idx(EDGE_FACELET[i], (n + ori) % 2)] = EDGE_COLOR[j][n]
        }
        for (f in 0..5) fc[f * 9 + 4] = "URFDLB"[f]
        return fc
    }

    override fun equals(other: Any?) = other is CubieCube && cp.contentEquals(other.cp) &&
        co.contentEquals(other.co) && ep.contentEquals(other.ep) && eo.contentEquals(other.eo)
    override fun hashCode() = cp.contentHashCode() * 31 + ep.contentHashCode()

    companion object {
        val SOLVED = CubieCube()
        private fun idx(s: String, k: Int) = "URFDLB".indexOf(s[k * 2]) * 9 + (s[k * 2 + 1] - '1')
        private val CORNER_FACELET = listOf("U9R1F3", "U7F1L3", "U1L1B3", "U3B1R3", "D3F9R7", "D1L9F7", "D7B9L7", "D9R9B7")
        private val EDGE_FACELET = listOf("U6R2", "U8F2", "U4L2", "U2B2", "D6R8", "D2F8", "D4L8", "D8B8", "F6R4", "F4L6", "B6L4", "B4R6")
        private val CORNER_COLOR = listOf("URF", "UFL", "ULB", "UBR", "DFR", "DLF", "DBL", "DRB")
        private val EDGE_COLOR = listOf("UR", "UF", "UL", "UB", "DR", "DF", "DL", "DB", "FR", "FL", "BL", "BR")

        private fun c(cp: IntArray, co: IntArray, ep: IntArray, eo: IntArray) = CubieCube(cp, co, ep, eo)
        private val z8 = IntArray(8); private val z12 = IntArray(12)

        /** Order: U R F D L B. Tables follow the standard Kociemba cubie definitions. */
        private val BASE = listOf(
            c(intArrayOf(3, 0, 1, 2, 4, 5, 6, 7), z8, intArrayOf(3, 0, 1, 2, 4, 5, 6, 7, 8, 9, 10, 11), z12),
            c(intArrayOf(4, 1, 2, 0, 7, 5, 6, 3), intArrayOf(2, 0, 0, 1, 1, 0, 0, 2),
                intArrayOf(8, 1, 2, 3, 11, 5, 6, 7, 4, 9, 10, 0), z12),
            c(intArrayOf(1, 5, 2, 3, 0, 4, 6, 7), intArrayOf(1, 2, 0, 0, 2, 1, 0, 0),
                intArrayOf(0, 9, 2, 3, 4, 8, 6, 7, 1, 5, 10, 11), intArrayOf(0, 1, 0, 0, 0, 1, 0, 0, 1, 1, 0, 0)),
            c(intArrayOf(0, 1, 2, 3, 5, 6, 7, 4), z8, intArrayOf(0, 1, 2, 3, 5, 6, 7, 4, 8, 9, 10, 11), z12),
            c(intArrayOf(0, 2, 6, 3, 4, 1, 5, 7), intArrayOf(0, 1, 2, 0, 0, 2, 1, 0),
                intArrayOf(0, 1, 10, 3, 4, 5, 9, 7, 8, 2, 6, 11), z12),
            c(intArrayOf(0, 1, 3, 7, 4, 5, 2, 6), intArrayOf(0, 0, 1, 2, 0, 0, 2, 1),
                intArrayOf(0, 1, 2, 11, 4, 5, 6, 10, 8, 9, 3, 7), intArrayOf(0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 1))
        )
    }
}
