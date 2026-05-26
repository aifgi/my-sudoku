package sudoku.engine

object Solver {

    /** Returns a solved IntArray(81), or null if the board is invalid/unsolvable. */
    fun solve(board: Board): IntArray? {
        val digits = board.digits.copyOf()
        return if (backtrack(digits, 0)) digits else null
    }

    private fun backtrack(digits: IntArray, startIndex: Int): Boolean {
        val idx = pickMRV(digits)
        if (idx == -1) return true  // board complete
        for (d in 1..9) {
            if (isValidPlacement(digits, idx, d)) {
                digits[idx] = d
                if (backtrack(digits, idx + 1)) return true
                digits[idx] = 0
            }
        }
        return false
    }

    /**
     * Counts solutions up to [limit]. Returns 0, 1, or [limit] (capped).
     * Used by Generator for uniqueness checking — exits immediately when count reaches [limit].
     */
    fun countSolutions(digits: IntArray, limit: Int = 2): Int {
        return countInternal(digits.copyOf(), limit)
    }

    private fun countInternal(digits: IntArray, limit: Int): Int {
        val idx = pickMRV(digits)
        if (idx == -1) return 1  // board complete, one solution found
        var count = 0
        for (d in 1..9) {
            if (isValidPlacement(digits, idx, d)) {
                digits[idx] = d
                count += countInternal(digits, limit)
                digits[idx] = 0
                if (count >= limit) return count
            }
        }
        return count
    }

    /** Returns index of empty cell with fewest valid placements (MRV), or -1 if board is complete. */
    private fun pickMRV(digits: IntArray): Int {
        var bestIdx = -1
        var bestCount = 10
        for (i in 0..80) {
            if (digits[i] != 0) continue
            var count = 0
            for (d in 1..9) {
                if (isValidPlacement(digits, i, d)) count++
            }
            if (count < bestCount) {
                bestCount = count
                bestIdx = i
                if (count == 0) break  // no valid placement, fail fast
            }
        }
        return bestIdx
    }

    /** Returns true if [digit] can be placed at [index] without conflicting with any peer. */
    private fun isValidPlacement(digits: IntArray, index: Int, digit: Int): Boolean {
        for (peer in PEERS[index]) {
            if (digits[peer] == digit) return false
        }
        return true
    }
}
