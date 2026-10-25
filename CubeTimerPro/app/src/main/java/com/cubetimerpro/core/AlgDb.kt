package com.cubetimerpro.core

data class AlgCase(val id: String, val name: String, val group: String, val alg: String, val note: String)

/**
 * Versioned algorithm database. Only a small starter set is included; each entry is checked
 * structurally by unit tests (parses, correct order, preserves the pieces it should), but is NOT yet
 * cross-checked against a published database. Do not advertise as a complete OLL/PLL set.
 */
object AlgDb {
    const val VERSION = 1
    val cases = listOf(
        AlgCase("trigger", "Sexy move", "Basics", "R U R' U'", "Order 6: repeat six times to return to solved."),
        AlgCase("oll27", "Sune (OLL 27)", "OLL", "R U R' U R U2 R'", "Orients the last-layer corners; F2L stays intact."),
        AlgCase("oll26", "Anti-Sune (OLL 26)", "OLL", "R U2 R' U' R U' R'", "Inverse-mirror of Sune; F2L stays intact."),
        AlgCase("pll_t", "T-Perm", "PLL", "R U R' U' R' F R2 U' R' U' R U R' F'", "Swaps two corners and two edges; applying twice restores."),
        AlgCase("pll_y", "Y-Perm", "PLL", "F R U' R' U' R U R' F' R U R' U' R' F R F'", "Diagonal corner swap plus adjacent edge swap."),
        AlgCase("pll_ua", "Ua-Perm", "PLL", "R U' R U R U R U' R' U' R2", "3-cycle of edges."),
        AlgCase("pll_ub", "Ub-Perm", "PLL", "R2 U R U R' U' R' U' R' U R'", "Inverse of Ua.")
    )
    /** Honest coverage: (included, standard total). */
    val ollCoverage = cases.count { it.group == "OLL" } to 57
    val pllCoverage = cases.count { it.group == "PLL" } to 21
}
