package sudoku.engine

// Flat IntArray(81) is the primary representation.
// 0 = empty, 1–9 = filled.
// Candidates stored as IntArray(81) where each element is a bitmask:
//   bit i set (1 shl i, i in 1..9) means digit i is a candidate.
// Givens tracked as BooleanArray(81).

val ROW_UNITS: Array<IntArray> = Array(9) { r -> IntArray(9) { r * 9 + it } }
val COL_UNITS: Array<IntArray> = Array(9) { c -> IntArray(9) { it * 9 + c } }
val BOX_UNITS: Array<IntArray> = Array(9) { b ->
    val topLeft = (b / 3) * 27 + (b % 3) * 3
    IntArray(9) { i -> topLeft + (i / 3) * 9 + (i % 3) }
}
val ALL_UNITS: Array<IntArray> = ROW_UNITS + COL_UNITS + BOX_UNITS

fun computeConflicts(digits: IntArray): Set<Int> {
    val conflicts = mutableSetOf<Int>()
    for (unit in ALL_UNITS) {
        val seen = mutableMapOf<Int, Int>()
        for (idx in unit) {
            val d = digits[idx]
            if (d != 0) {
                val prev = seen[d]
                if (prev != null) { conflicts += idx; conflicts += prev }
                else seen[d] = idx
            }
        }
    }
    return conflicts
}

private const val ALL_CANDIDATES = (1 shl 1) or (1 shl 2) or (1 shl 3) or
        (1 shl 4) or (1 shl 5) or (1 shl 6) or
        (1 shl 7) or (1 shl 8) or (1 shl 9)

private fun computeCandidates(index: Int, digits: IntArray): Int {
    var mask = ALL_CANDIDATES
    for (peer in PEERS[index]) {
        val d = digits[peer]
        if (d != 0) mask = mask and (1 shl d).inv()
    }
    return mask
}

class Board private constructor(
    val digits: IntArray,       // size 81, copied on mutation
    val givens: BooleanArray,   // size 81, immutable after construction
    val candidates: IntArray,   // size 81, bitmask per cell
) {
    init {
        require(digits.size == 81 && givens.size == 81 && candidates.size == 81) {
            "Board arrays must each have exactly 81 elements"
        }
    }
    companion object {
        fun fromDigits(digits: IntArray, givens: BooleanArray): Board {
            val d = digits.copyOf()
            val g = givens.copyOf()
            val c = IntArray(81) { i ->
                if (d[i] != 0) 0 else computeCandidates(i, d)
            }
            return Board(d, g, c)
        }

        fun empty(): Board {
            val d = IntArray(81)
            val g = BooleanArray(81)
            val c = IntArray(81) { ALL_CANDIDATES }
            return Board(d, g, c)
        }
    }

    /**
     * Returns a new [Board] with [digit] placed at [index].
     * Copy-on-write: this receiver is never mutated; a fresh copy of all arrays is made.
     */
    fun withDigit(index: Int, digit: Int): Board {
        val d = digits.copyOf()
        d[index] = digit
        val c = candidates.copyOf()
        // Filled cell has no candidates
        c[index] = 0
        // Recompute candidates for all peers
        for (peer in PEERS[index]) {
            if (d[peer] == 0) {
                c[peer] = computeCandidates(peer, d)
            }
        }
        return Board(d, givens.copyOf(), c)
    }

    /**
     * Returns a new [Board] with the digit at [index] cleared (set to 0).
     * Copy-on-write: this receiver is never mutated; a fresh copy of all arrays is made.
     */
    fun withErased(index: Int): Board {
        val d = digits.copyOf()
        d[index] = 0
        val c = candidates.copyOf()
        // Recompute candidates for this cell and all peers
        c[index] = computeCandidates(index, d)
        for (peer in PEERS[index]) {
            if (d[peer] == 0) {
                c[peer] = computeCandidates(peer, d)
            }
        }
        return Board(d, givens.copyOf(), c)
    }

    val isEmpty: Boolean get() = digits.all { it == 0 }
    val isFull: Boolean get() = digits.none { it == 0 }
}
