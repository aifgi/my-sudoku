package sudoku.engine

object HintEngine {

    fun findHint(board: Board, difficulty: Difficulty): HintResult {
        val candidates = computeAllCandidates(board)
        return nakedSingle(board, candidates)
            ?: hiddenSingle(board, candidates)
            ?: nakedPair(board, candidates)
            ?: hiddenPair(board, candidates)
            ?: pointingPair(board, candidates)
            ?: if (difficulty == Difficulty.HARD || difficulty == Difficulty.EXPERT)
                HintResult.NoHintForDifficulty
            else
                HintResult.NoHint
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
                    explanation = "Naked Single at ${cellName(i)}: only $digit fits"
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
                        explanation = "Hidden Single at ${cellName(cellIndex)}: digit $digit can only go here in this unit"
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
                            explanation = "Naked Pair at ${cellName(a)} and ${cellName(b)}: digits $d1 and $d2 are confined here"
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
                            explanation = "Hidden Pair at ${cellName(a)} and ${cellName(b)}: digits $d1 and $d2 are confined to these cells"
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
                            explanation = "Pointing Pair: digit $digit in box ${boxIndex + 1} is confined to row ${row + 1}, eliminating it from other row cells"
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
                            explanation = "Pointing Pair: digit $digit in box ${boxIndex + 1} is confined to column ${col + 1}, eliminating it from other column cells"
                        )
                    }
                }
            }
        }
        return null
    }

    private fun cellName(index: Int) = "R${index / 9 + 1}C${index % 9 + 1}"
}
