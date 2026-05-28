package sudoku.engine

object Grader {

    fun grade(puzzle: IntArray): Difficulty {
        val digits = puzzle.copyOf()
        val candidates = computeCandidates(digits)

        // Tier 1 (EASY): Naked Singles + Hidden Singles
        var progressed = true
        while (progressed) {
            progressed = applyNakedSingles(candidates, digits) || applyHiddenSingles(candidates, digits)
        }
        if (digits.none { it == 0 }) return Difficulty.EASY

        // Tier 2 (MEDIUM): + Naked Pairs + Hidden Pairs + Pointing Pairs
        progressed = true
        while (progressed) {
            progressed = applyNakedSingles(candidates, digits) || applyHiddenSingles(candidates, digits) ||
                    applyNakedPairs(candidates, digits) || applyHiddenPairs(candidates, digits) ||
                    applyPointingPairs(candidates, digits)
        }
        if (digits.none { it == 0 }) return Difficulty.MEDIUM

        // Tier 3 (HARD): + Naked Triples + Hidden Triples
        progressed = true
        while (progressed) {
            progressed = applyNakedSingles(candidates, digits) || applyHiddenSingles(candidates, digits) ||
                    applyNakedPairs(candidates, digits) || applyHiddenPairs(candidates, digits) ||
                    applyPointingPairs(candidates, digits) ||
                    applyNakedTriples(candidates, digits) || applyHiddenTriples(candidates, digits)
        }
        if (digits.none { it == 0 }) return Difficulty.HARD

        // Tier 4 (EXPERT): + X-Wing + Swordfish (fall-through)
        progressed = true
        while (progressed) {
            progressed = applyNakedSingles(candidates, digits) || applyHiddenSingles(candidates, digits) ||
                    applyNakedPairs(candidates, digits) || applyHiddenPairs(candidates, digits) ||
                    applyPointingPairs(candidates, digits) ||
                    applyNakedTriples(candidates, digits) || applyHiddenTriples(candidates, digits) ||
                    applyXWing(candidates, digits) || applySwordfish(candidates, digits)
        }
        return Difficulty.EXPERT
    }

    // ---------------------------------------------------------------------------
    // Candidate initialisation
    // ---------------------------------------------------------------------------

    internal fun computeCandidates(digits: IntArray): IntArray {
        val allMask = (1..9).fold(0) { acc, d -> acc or (1 shl d) }
        return IntArray(81) { i ->
            if (digits[i] != 0) 0
            else {
                var mask = allMask
                for (peer in PEERS[i]) {
                    val d = digits[peer]
                    if (d != 0) mask = mask and (1 shl d).inv()
                }
                mask
            }
        }
    }

    // ---------------------------------------------------------------------------
    // Place a digit and eliminate from peers
    // ---------------------------------------------------------------------------

    private fun placeDigit(index: Int, digit: Int, candidates: IntArray, digits: IntArray) {
        digits[index] = digit
        candidates[index] = 0
        val bit = (1 shl digit).inv()
        for (peer in PEERS[index]) {
            candidates[peer] = candidates[peer] and bit
        }
    }

    // ---------------------------------------------------------------------------
    // Technique 1: Naked Singles
    // ---------------------------------------------------------------------------

    internal fun applyNakedSingles(candidates: IntArray, digits: IntArray): Boolean {
        var progress = false
        for (i in 0 until 81) {
            val mask = candidates[i]
            if (mask != 0 && (mask and (mask - 1)) == 0) {
                // exactly one bit set
                val digit = Integer.numberOfTrailingZeros(mask)
                placeDigit(i, digit, candidates, digits)
                progress = true
            }
        }
        return progress
    }

    // ---------------------------------------------------------------------------
    // Technique 2: Hidden Singles
    // ---------------------------------------------------------------------------

    internal fun applyHiddenSingles(candidates: IntArray, digits: IntArray): Boolean {
        var progress = false
        for (unit in ALL_UNITS) {
            for (d in 1..9) {
                val bit = 1 shl d
                var count = 0
                var lastIdx = -1
                for (cell in unit) {
                    if (candidates[cell] and bit != 0) {
                        count++
                        lastIdx = cell
                    }
                }
                if (count == 1 && lastIdx != -1) {
                    placeDigit(lastIdx, d, candidates, digits)
                    progress = true
                }
            }
        }
        return progress
    }

    // ---------------------------------------------------------------------------
    // Technique 3: Naked Pairs
    // ---------------------------------------------------------------------------

    internal fun applyNakedPairs(candidates: IntArray, digits: IntArray): Boolean {
        var progress = false
        for (unit in ALL_UNITS) {
            // Find cells with exactly 2 candidates
            val pairs = mutableListOf<Int>()
            for (cell in unit) {
                val mask = candidates[cell]
                if (mask != 0 && Integer.bitCount(mask) == 2) pairs.add(cell)
            }
            // Check for two cells with the same bitmask
            for (a in pairs.indices) {
                for (b in a + 1 until pairs.size) {
                    val cellA = pairs[a]
                    val cellB = pairs[b]
                    if (candidates[cellA] == candidates[cellB]) {
                        val pairMask = candidates[cellA]
                        // Eliminate those two digits from all other cells in the unit
                        for (cell in unit) {
                            if (cell != cellA && cell != cellB && candidates[cell] and pairMask != 0) {
                                candidates[cell] = candidates[cell] and pairMask.inv()
                                progress = true
                            }
                        }
                    }
                }
            }
        }
        return progress
    }

    // ---------------------------------------------------------------------------
    // Technique 4: Hidden Pairs
    // ---------------------------------------------------------------------------

    internal fun applyHiddenPairs(candidates: IntArray, digits: IntArray): Boolean {
        var progress = false
        for (unit in ALL_UNITS) {
            // For each digit, find which cells in the unit have it as a candidate
            val positions = Array(10) { mutableListOf<Int>() }
            for (cell in unit) {
                for (d in 1..9) {
                    if (candidates[cell] and (1 shl d) != 0) positions[d].add(cell)
                }
            }
            // Find pairs of digits that appear in exactly 2 cells, same cells
            for (d1 in 1..8) {
                if (positions[d1].size != 2) continue
                for (d2 in d1 + 1..9) {
                    if (positions[d2].size != 2) continue
                    if (positions[d1] == positions[d2]) {
                        val cellA = positions[d1][0]
                        val cellB = positions[d1][1]
                        val keepMask = (1 shl d1) or (1 shl d2)
                        // Eliminate all other candidates from those 2 cells
                        if (candidates[cellA] and keepMask.inv() != 0) {
                            candidates[cellA] = candidates[cellA] and keepMask
                            progress = true
                        }
                        if (candidates[cellB] and keepMask.inv() != 0) {
                            candidates[cellB] = candidates[cellB] and keepMask
                            progress = true
                        }
                    }
                }
            }
        }
        return progress
    }

    // ---------------------------------------------------------------------------
    // Technique 5: Pointing Pairs
    // ---------------------------------------------------------------------------

    internal fun applyPointingPairs(candidates: IntArray, digits: IntArray): Boolean {
        var progress = false
        for (box in BOX_UNITS) {
            for (d in 1..9) {
                val bit = 1 shl d
                // Find cells in this box that have this digit as a candidate
                val cells = box.filter { candidates[it] and bit != 0 }
                if (cells.size < 2) continue

                val rows = cells.map { it / 9 }.toSet()
                val cols = cells.map { it % 9 }.toSet()

                if (rows.size == 1) {
                    // All in the same row — eliminate from rest of that row outside box
                    val row = rows.first()
                    val boxSet = box.toSet()
                    for (cell in ROW_UNITS[row]) {
                        if (cell !in boxSet && candidates[cell] and bit != 0) {
                            candidates[cell] = candidates[cell] and bit.inv()
                            progress = true
                        }
                    }
                }
                if (cols.size == 1) {
                    // All in the same col — eliminate from rest of that col outside box
                    val col = cols.first()
                    val boxSet = box.toSet()
                    for (cell in COL_UNITS[col]) {
                        if (cell !in boxSet && candidates[cell] and bit != 0) {
                            candidates[cell] = candidates[cell] and bit.inv()
                            progress = true
                        }
                    }
                }
            }
        }
        return progress
    }

    // ---------------------------------------------------------------------------
    // Technique 6: X-Wing
    // ---------------------------------------------------------------------------

    internal fun applyXWing(candidates: IntArray, digits: IntArray): Boolean {
        var progress = false
        for (d in 1..9) {
            val bit = 1 shl d
            // Find rows where digit appears in exactly 2 columns
            val rowColPairs = mutableListOf<Pair<Int, Set<Int>>>()
            for (r in 0..8) {
                val cols = ROW_UNITS[r].filter { candidates[it] and bit != 0 }.map { it % 9 }.toSet()
                if (cols.size == 2) rowColPairs.add(r to cols)
            }
            // Check for 2 rows sharing the same 2 columns
            for (i in rowColPairs.indices) {
                for (j in i + 1 until rowColPairs.size) {
                    if (rowColPairs[i].second == rowColPairs[j].second) {
                        val r1 = rowColPairs[i].first
                        val r2 = rowColPairs[j].first
                        val cols = rowColPairs[i].second.toList()
                        // Eliminate from those columns except r1 and r2
                        for (col in cols) {
                            for (cell in COL_UNITS[col]) {
                                val row = cell / 9
                                if (row != r1 && row != r2 && candidates[cell] and bit != 0) {
                                    candidates[cell] = candidates[cell] and bit.inv()
                                    progress = true
                                }
                            }
                        }
                    }
                }
            }
        }
        return progress
    }

    // ---------------------------------------------------------------------------
    // Technique 7: Naked Triples
    // ---------------------------------------------------------------------------

    internal fun applyNakedTriples(candidates: IntArray, digits: IntArray): Boolean {
        var progress = false
        for (unit in ALL_UNITS) {
            // Candidates cells with 2 or 3 candidates
            val eligible = unit.filter { candidates[it] != 0 && Integer.bitCount(candidates[it]) in 2..3 }
            // Check all 3-cell subsets
            for (ai in eligible.indices) {
                for (bi in ai + 1 until eligible.size) {
                    for (ci in bi + 1 until eligible.size) {
                        val cellA = eligible[ai]
                        val cellB = eligible[bi]
                        val cellC = eligible[ci]
                        val union = candidates[cellA] or candidates[cellB] or candidates[cellC]
                        if (Integer.bitCount(union) == 3) {
                            // Eliminate these 3 digits from all other cells in the unit
                            for (cell in unit) {
                                if (cell != cellA && cell != cellB && cell != cellC &&
                                    candidates[cell] and union != 0) {
                                    candidates[cell] = candidates[cell] and union.inv()
                                    progress = true
                                }
                            }
                        }
                    }
                }
            }
        }
        return progress
    }

    // ---------------------------------------------------------------------------
    // Technique 8: Hidden Triples
    // ---------------------------------------------------------------------------

    internal fun applyHiddenTriples(candidates: IntArray, digits: IntArray): Boolean {
        var progress = false
        for (unit in ALL_UNITS) {
            // For each digit, find which cells in the unit have it as a candidate
            val positions = Array(10) { mutableListOf<Int>() }
            for (cell in unit) {
                for (d in 1..9) {
                    if (candidates[cell] and (1 shl d) != 0) positions[d].add(cell)
                }
            }
            // Find 3 digits that each appear in 2 or 3 cells, and together span exactly 3 cells
            for (d1 in 1..7) {
                if (positions[d1].size !in 2..3) continue
                for (d2 in d1 + 1..8) {
                    if (positions[d2].size !in 2..3) continue
                    for (d3 in d2 + 1..9) {
                        if (positions[d3].size !in 2..3) continue
                        val cells = (positions[d1] + positions[d2] + positions[d3]).toSet()
                        if (cells.size == 3) {
                            val keepMask = (1 shl d1) or (1 shl d2) or (1 shl d3)
                            for (cell in cells) {
                                if (candidates[cell] and keepMask.inv() != 0) {
                                    candidates[cell] = candidates[cell] and keepMask
                                    progress = true
                                }
                            }
                        }
                    }
                }
            }
        }
        return progress
    }

    // ---------------------------------------------------------------------------
    // Technique 9: Swordfish
    // ---------------------------------------------------------------------------

    internal fun applySwordfish(candidates: IntArray, digits: IntArray): Boolean {
        var progress = false
        for (d in 1..9) {
            val bit = 1 shl d

            // Row-based Swordfish: find rows where digit appears in 2 or 3 columns
            val rowCols = Array(9) { r ->
                ROW_UNITS[r].filter { candidates[it] and bit != 0 }.map { it % 9 }.toSet()
            }
            val eligibleRows = (0..8).filter { rowCols[it].size in 2..3 }
            // Check all 3-row subsets
            for (ri in eligibleRows.indices) {
                for (rj in ri + 1 until eligibleRows.size) {
                    for (rk in rj + 1 until eligibleRows.size) {
                        val r1 = eligibleRows[ri]
                        val r2 = eligibleRows[rj]
                        val r3 = eligibleRows[rk]
                        val colUnion = rowCols[r1] + rowCols[r2] + rowCols[r3]
                        if (colUnion.size == 3) {
                            // Eliminate digit from those 3 columns, except rows r1, r2, r3
                            for (col in colUnion) {
                                for (cell in COL_UNITS[col]) {
                                    val row = cell / 9
                                    if (row != r1 && row != r2 && row != r3 &&
                                        candidates[cell] and bit != 0) {
                                        candidates[cell] = candidates[cell] and bit.inv()
                                        progress = true
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Column-based Swordfish: symmetric variant
            val colRows = Array(9) { c ->
                COL_UNITS[c].filter { candidates[it] and bit != 0 }.map { it / 9 }.toSet()
            }
            val eligibleCols = (0..8).filter { colRows[it].size in 2..3 }
            for (ci in eligibleCols.indices) {
                for (cj in ci + 1 until eligibleCols.size) {
                    for (ck in cj + 1 until eligibleCols.size) {
                        val c1 = eligibleCols[ci]
                        val c2 = eligibleCols[cj]
                        val c3 = eligibleCols[ck]
                        val rowUnion = colRows[c1] + colRows[c2] + colRows[c3]
                        if (rowUnion.size == 3) {
                            // Eliminate digit from those 3 rows, except cols c1, c2, c3
                            for (row in rowUnion) {
                                for (cell in ROW_UNITS[row]) {
                                    val col = cell % 9
                                    if (col != c1 && col != c2 && col != c3 &&
                                        candidates[cell] and bit != 0) {
                                        candidates[cell] = candidates[cell] and bit.inv()
                                        progress = true
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return progress
    }
}
