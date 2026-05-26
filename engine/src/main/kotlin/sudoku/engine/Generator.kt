package sudoku.engine

object Generator {

    private const val MAX_ATTEMPTS = 100

    /** Returns a puzzle Board (givens set) at the requested difficulty. */
    suspend fun generate(difficulty: Difficulty): Board {
        repeat(MAX_ATTEMPTS) {
            val solution = fillGrid() ?: return@repeat
            val puzzle = digHoles(solution, difficulty) ?: return@repeat
            val givens = BooleanArray(81) { puzzle[it] != 0 }
            return Board.fromDigits(puzzle, givens)
        }
        throw IllegalStateException("Failed to generate puzzle after $MAX_ATTEMPTS attempts")
    }

    /**
     * Las Vegas randomized backtracking on an empty 9x9 grid.
     * Returns a complete solution as IntArray(81), or null on failure (extremely rare).
     */
    private fun fillGrid(): IntArray? {
        val digits = IntArray(81)
        return if (backtrack(digits, 0)) digits else null
    }

    private fun backtrack(digits: IntArray, index: Int): Boolean {
        if (index == 81) return true
        val shuffled = (1..9).toMutableList().also { it.shuffle() }
        for (digit in shuffled) {
            if (isValidPlacement(digits, index, digit)) {
                digits[index] = digit
                if (backtrack(digits, index + 1)) return true
                digits[index] = 0
            }
        }
        return false
    }

    /** Returns true if [digit] can be placed at [index] without conflicting with any peer. */
    private fun isValidPlacement(digits: IntArray, index: Int, digit: Int): Boolean {
        for (peer in PEERS[index]) {
            if (digits[peer] == digit) return false
        }
        return true
    }

    /**
     * Digs holes in [solution] aiming for a puzzle of [difficulty].
     * Returns the puzzle IntArray, or null if the resulting grade doesn't match.
     */
    private fun digHoles(solution: IntArray, difficulty: Difficulty): IntArray? {
        val puzzle = solution.copyOf()
        val indices = (0..80).toMutableList().also { it.shuffle() }
        for (idx in indices) {
            val saved = puzzle[idx]
            puzzle[idx] = 0
            if (Solver.countSolutions(puzzle, 2) != 1) {
                puzzle[idx] = saved  // restore — not unique
            }
        }
        if (Grader.grade(puzzle) != difficulty) return null
        return puzzle
    }
}
