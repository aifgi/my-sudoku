package sudoku.engine

object HintEngine {

    fun findHint(board: Board, difficulty: PuzzleDifficulty): HintResult {
        val candidates = computeAllCandidates(board)
        return when (difficulty) {
            is PuzzleDifficulty.Technique -> findHintForTechnique(board, candidates, difficulty.grade)
            is PuzzleDifficulty.Given -> findHintForGiven(board, candidates, difficulty.grade)
        }
    }

    private fun findHintForTechnique(board: Board, candidates: IntArray, grade: Difficulty): HintResult {
        val singles = nakedSingle(board, candidates) ?: hiddenSingle(board, candidates)
        if (singles != null) return singles

        val pairsAndPointing = nakedPair(board, candidates)
            ?: hiddenPair(board, candidates)
            ?: pointingPair(board, candidates)
        if (pairsAndPointing != null) return pairsAndPointing

        if (grade == Difficulty.HARD || grade == Difficulty.EXPERT) {
            val triples = nakedTriple(board, candidates) ?: hiddenTriple(board, candidates)
            if (triples != null) return triples
        }

        if (grade == Difficulty.EXPERT) {
            val advanced = xWingHint(board, candidates) ?: swordfishHint(board, candidates)
            if (advanced != null) return advanced
        }

        return if (grade == Difficulty.HARD || grade == Difficulty.EXPERT)
            HintResult.NoHintForDifficulty
        else
            HintResult.NoHint
    }

    private fun findHintForGiven(board: Board, candidates: IntArray, grade: GivenGrade): HintResult {
        val singles = nakedSingle(board, candidates) ?: hiddenSingle(board, candidates)
        if (singles != null) return singles

        if (grade.techniqueCeiling == TechniqueCeiling.PAIRS) {
            val pairsAndPointing = nakedPair(board, candidates)
                ?: hiddenPair(board, candidates)
                ?: pointingPair(board, candidates)
            if (pairsAndPointing != null) return pairsAndPointing
        }

        return HintResult.NoHint
    }

    private fun computeAllCandidates(board: Board): IntArray {
        val candidates = IntArray(81)
        for (i in 0..80) {
            if (board.digits[i] != 0) {
                candidates[i] = 0
            } else {
                var mask = (1 shl 1) or (1 shl 2) or (1 shl 3) or (1 shl 4) or
                        (1 shl 5) or (1 shl 6) or (1 shl 7) or (1 shl 8) or (1 shl 9)
                for (peer in PEERS[i]) {
                    val d = board.digits[peer]
                    if (d != 0) mask = mask and (1 shl d).inv()
                }
                candidates[i] = mask
            }
        }
        return candidates
    }

    private fun nakedSingle(board: Board, candidates: IntArray): HintResult.Found? {
        for (i in 0..80) {
            if (board.digits[i] != 0) continue
            if (Integer.bitCount(candidates[i]) == 1) {
                val digit = Integer.numberOfTrailingZeros(candidates[i])
                return HintResult.Found(
                    technique = "Naked Single",
                    targetCells = listOf(i),
                    peerCells = PEERS[i].toList(),
                    explanation = "Naked Single at ${cellName(i)}: only $digit fits",
                    explanationData = HintExplanationData.Single(cellName(i), digit)
                )
            }
        }
        return null
    }

    private fun hiddenSingle(board: Board, candidates: IntArray): HintResult.Found? {
        for (unit in ALL_UNITS) {
            for (digit in 1..9) {
                val bit = 1 shl digit
                val cells = unit.filter { candidates[it] and bit != 0 }
                if (cells.size == 1) {
                    val cellIndex = cells[0]
                    return HintResult.Found(
                        technique = "Hidden Single",
                        targetCells = listOf(cellIndex),
                        peerCells = unit.filter { it != cellIndex }.toList(),
                        explanation = "Hidden Single at ${cellName(cellIndex)}: digit $digit can only go here in this unit",
                        explanationData = HintExplanationData.Single(cellName(cellIndex), digit)
                    )
                }
            }
        }
        return null
    }

    private fun nakedPair(board: Board, candidates: IntArray): HintResult.Found? {
        for (unit in ALL_UNITS) {
            val twoCandidateCells = unit.filter { Integer.bitCount(candidates[it]) == 2 }
            for (i in twoCandidateCells.indices) {
                for (j in i + 1 until twoCandidateCells.size) {
                    val a = twoCandidateCells[i]
                    val b = twoCandidateCells[j]
                    if (candidates[a] == candidates[b]) {
                        // Found a naked pair — check if any other cell in unit has those candidates
                        val pairMask = candidates[a]
                        val others = unit.filter { it != a && it != b }
                        val affected = others.filter { candidates[it] and pairMask != 0 }
                        val peerCells = if (affected.isNotEmpty()) affected else others
                        val d1 = Integer.numberOfTrailingZeros(pairMask)
                        val d2 = Integer.numberOfTrailingZeros(pairMask and (pairMask - 1))
                        return HintResult.Found(
                            technique = "Naked Pair",
                            targetCells = listOf(a, b),
                            peerCells = peerCells,
                            explanation = "Naked Pair at ${cellName(a)} and ${cellName(b)}: digits $d1 and $d2 are confined here",
                            explanationData = HintExplanationData.Pair(cellName(a), cellName(b), d1, d2)
                        )
                    }
                }
            }
        }
        return null
    }

    private fun hiddenPair(board: Board, candidates: IntArray): HintResult.Found? {
        for (unit in ALL_UNITS) {
            for (d1 in 1..8) {
                val bit1 = 1 shl d1
                val cells1 = unit.filter { candidates[it] and bit1 != 0 }
                if (cells1.size != 2) continue
                for (d2 in d1 + 1..9) {
                    val bit2 = 1 shl d2
                    val cells2 = unit.filter { candidates[it] and bit2 != 0 }
                    if (cells2.size == 2 && cells1 == cells2) {
                        val a = cells1[0]
                        val b = cells1[1]
                        return HintResult.Found(
                            technique = "Hidden Pair",
                            targetCells = listOf(a, b),
                            peerCells = unit.filter { it != a && it != b }.toList(),
                            explanation = "Hidden Pair at ${cellName(a)} and ${cellName(b)}: digits $d1 and $d2 are confined to these cells",
                            explanationData = HintExplanationData.Pair(cellName(a), cellName(b), d1, d2)
                        )
                    }
                }
            }
        }
        return null
    }

    private fun pointingPair(board: Board, candidates: IntArray): HintResult.Found? {
        for (boxIndex in 0..8) {
            val box = BOX_UNITS[boxIndex]
            for (digit in 1..9) {
                val bit = 1 shl digit
                val cells = box.filter { candidates[it] and bit != 0 }
                if (cells.size < 2) continue
                // Check if all in same row
                val rows = cells.map { it / 9 }.toSet()
                if (rows.size == 1) {
                    val row = rows.first()
                    val rowCells = ROW_UNITS[row].filter { it !in box }.filter { candidates[it] and bit != 0 }
                    if (rowCells.isNotEmpty()) {
                        return HintResult.Found(
                            technique = "Pointing Pair",
                            targetCells = cells,
                            peerCells = rowCells,
                            explanation = "Pointing Pair: digit $digit in box ${boxIndex + 1} is confined to row ${row + 1}, eliminating it from other row cells",
                            explanationData = HintExplanationData.PointingPairRow(digit, boxIndex + 1, row + 1)
                        )
                    }
                }
                // Check if all in same column
                val cols = cells.map { it % 9 }.toSet()
                if (cols.size == 1) {
                    val col = cols.first()
                    val colCells = COL_UNITS[col].filter { it !in box }.filter { candidates[it] and bit != 0 }
                    if (colCells.isNotEmpty()) {
                        return HintResult.Found(
                            technique = "Pointing Pair",
                            targetCells = cells,
                            peerCells = colCells,
                            explanation = "Pointing Pair: digit $digit in box ${boxIndex + 1} is confined to column ${col + 1}, eliminating it from other column cells",
                            explanationData = HintExplanationData.PointingPairCol(digit, boxIndex + 1, col + 1)
                        )
                    }
                }
            }
        }
        return null
    }

    private fun nakedTriple(board: Board, candidates: IntArray): HintResult.Found? {
        for (unit in ALL_UNITS) {
            val emptyCells = unit.filter { board.digits[it] == 0 }
            val smallCells = emptyCells.filter { Integer.bitCount(candidates[it]) in 2..3 }
            for (i in smallCells.indices) {
                for (j in i + 1 until smallCells.size) {
                    for (k in j + 1 until smallCells.size) {
                        val a = smallCells[i]
                        val b = smallCells[j]
                        val c = smallCells[k]
                        val combinedMask = candidates[a] or candidates[b] or candidates[c]
                        if (Integer.bitCount(combinedMask) == 3) {
                            // Naked triple: exactly 3 digits among the 3 cells
                            val others = unit.filter { it != a && it != b && it != c }
                            val affected = others.filter { candidates[it] and combinedMask != 0 }
                            if (affected.isNotEmpty()) {
                                val digits = (1..9).filter { combinedMask and (1 shl it) != 0 }
                                return HintResult.Found(
                                    technique = "Naked Triple",
                                    targetCells = listOf(a, b, c),
                                    peerCells = affected,
                                    explanation = "Naked Triple at ${cellName(a)}, ${cellName(b)}, ${cellName(c)}: digits ${digits[0]}, ${digits[1]}, ${digits[2]} are confined here",
                                    explanationData = HintExplanationData.Triple(a, b, c, digits[0], digits[1], digits[2])
                                )
                            }
                        }
                    }
                }
            }
        }
        return null
    }

    private fun hiddenTriple(board: Board, candidates: IntArray): HintResult.Found? {
        for (unit in ALL_UNITS) {
            for (d1 in 1..7) {
                val bit1 = 1 shl d1
                val cells1 = unit.filter { candidates[it] and bit1 != 0 }
                if (cells1.size !in 2..3) continue
                for (d2 in d1 + 1..8) {
                    val bit2 = 1 shl d2
                    val cells2 = unit.filter { candidates[it] and bit2 != 0 }
                    if (cells2.size !in 2..3) continue
                    val combined12 = (cells1 + cells2).distinct()
                    if (combined12.size > 3) continue
                    for (d3 in d2 + 1..9) {
                        val bit3 = 1 shl d3
                        val cells3 = unit.filter { candidates[it] and bit3 != 0 }
                        if (cells3.size !in 2..3) continue
                        val tripleSet = (combined12 + cells3).distinct()
                        if (tripleSet.size == 3) {
                            val a = tripleSet[0]
                            val b = tripleSet[1]
                            val c = tripleSet[2]
                            // Verify each digit appears in at least 2 of the 3 cells
                            val mask = bit1 or bit2 or bit3
                            val hasElim = listOf(a, b, c).any { cell ->
                                candidates[cell] and mask.inv() != 0
                            }
                            if (hasElim) {
                                return HintResult.Found(
                                    technique = "Hidden Triple",
                                    targetCells = listOf(a, b, c),
                                    peerCells = unit.filter { it != a && it != b && it != c }.toList(),
                                    explanation = "Hidden Triple at ${cellName(a)}, ${cellName(b)}, ${cellName(c)}: digits $d1, $d2, $d3 are confined to these cells",
                                    explanationData = HintExplanationData.Triple(a, b, c, d1, d2, d3)
                                )
                            }
                        }
                    }
                }
            }
        }
        return null
    }

    private fun xWingHint(board: Board, candidates: IntArray): HintResult.Found? {
        for (digit in 1..9) {
            val bit = 1 shl digit
            // Check rows: find two rows where the digit appears in exactly 2 cells each, same columns
            for (r1 in 0..7) {
                val row1Cells = ROW_UNITS[r1].filter { candidates[it] and bit != 0 }
                if (row1Cells.size != 2) continue
                val cols = row1Cells.map { it % 9 }
                for (r2 in r1 + 1..8) {
                    val row2Cells = ROW_UNITS[r2].filter { candidates[it] and bit != 0 }
                    if (row2Cells.size != 2) continue
                    if (row2Cells.map { it % 9 } != cols) continue
                    // X-Wing found in rows r1, r2; columns cols[0], cols[1]
                    val c1 = cols[0]; val c2 = cols[1]
                    val xWingCells = row1Cells + row2Cells
                    val elimCells = (COL_UNITS[c1] + COL_UNITS[c2])
                        .filter { it !in xWingCells && candidates[it] and bit != 0 }
                    if (elimCells.isNotEmpty()) {
                        return HintResult.Found(
                            technique = "X-Wing",
                            targetCells = xWingCells,
                            peerCells = elimCells,
                            explanation = "X-Wing: digit $digit locked in rows ${r1 + 1} and ${r2 + 1} at columns ${c1 + 1} and ${c2 + 1}",
                            explanationData = null
                        )
                    }
                }
            }
        }
        return null
    }

    private fun swordfishHint(board: Board, candidates: IntArray): HintResult.Found? {
        for (digit in 1..9) {
            val bit = 1 shl digit
            // Check rows: find 3 rows where digit appears in 2 or 3 cells, covering exactly 3 columns
            for (r1 in 0..6) {
                val row1Cells = ROW_UNITS[r1].filter { candidates[it] and bit != 0 }
                if (row1Cells.size !in 2..3) continue
                for (r2 in r1 + 1..7) {
                    val row2Cells = ROW_UNITS[r2].filter { candidates[it] and bit != 0 }
                    if (row2Cells.size !in 2..3) continue
                    val combined12Cols = (row1Cells + row2Cells).map { it % 9 }.distinct()
                    if (combined12Cols.size > 3) continue
                    for (r3 in r2 + 1..8) {
                        val row3Cells = ROW_UNITS[r3].filter { candidates[it] and bit != 0 }
                        if (row3Cells.size !in 2..3) continue
                        val allCols = (row1Cells + row2Cells + row3Cells).map { it % 9 }.distinct()
                        if (allCols.size != 3) continue
                        val swordfishCells = row1Cells + row2Cells + row3Cells
                        val elimCells = allCols.flatMap { c -> COL_UNITS[c].toList() }
                            .filter { it !in swordfishCells && candidates[it] and bit != 0 }
                        if (elimCells.isNotEmpty()) {
                            return HintResult.Found(
                                technique = "Swordfish",
                                targetCells = swordfishCells,
                                peerCells = elimCells,
                                explanation = "Swordfish: digit $digit locked across rows ${r1 + 1}, ${r2 + 1}, ${r3 + 1}",
                                explanationData = HintExplanationData.Swordfish(digit)
                            )
                        }
                    }
                }
            }
        }
        return null
    }

    private fun cellName(index: Int) = "R${index / 9 + 1}C${index % 9 + 1}"
}
