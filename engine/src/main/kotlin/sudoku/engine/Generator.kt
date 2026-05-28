package sudoku.engine

import kotlin.coroutines.cancellation.CancellationException

object Generator {

    private const val MAX_ATTEMPTS = 1000

    // Difficulty is enforced by Kotlin's type system — no invalid enum values can be passed.

    /** Returns a puzzle Board (givens set) at the requested difficulty. */
    suspend fun generate(difficulty: Difficulty): Board {
        for (attempt in 1..MAX_ATTEMPTS) {
            try {
                val solution = fillGrid() ?: continue
                val puzzle = digHoles(solution, difficulty) ?: continue
                val givens = BooleanArray(81) { puzzle[it] != 0 }
                return Board.fromDigits(puzzle, givens, solution)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Unexpected error on this attempt — log and retry
            }
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

    /**
     * Digs holes in [solution] until the given count reaches a target sampled from [grade]'s range.
     * Returns the puzzle IntArray, or null if uniqueness prevents reaching the target.
     */
    private fun digHolesToTarget(solution: IntArray, grade: GivenGrade): IntArray? {
        val target = (grade.minGivens..grade.maxGivens).random()
        val puzzle = solution.copyOf()
        val indices = (0..80).toMutableList().also { it.shuffle() }
        for (idx in indices) {
            val currentGivens = puzzle.count { it != 0 }
            if (currentGivens == target) break
            val saved = puzzle[idx]
            puzzle[idx] = 0
            if (Solver.countSolutions(puzzle, 2) != 1) {
                puzzle[idx] = saved  // restore — not unique
            }
        }
        val givens = puzzle.count { it != 0 }
        if (givens !in grade.minGivens..grade.maxGivens) return null
        return puzzle
    }

    /**
     * Returns true if the puzzle exceeds the technique ceiling for [grade].
     * For SINGLES: puzzle must be solvable using only Naked/Hidden Singles.
     * For PAIRS: puzzle must be solvable at MEDIUM or below (not HARD/EXPERT).
     */
    private fun ceilingExceeded(puzzle: IntArray, grade: GivenGrade): Boolean {
        return when (grade.techniqueCeiling) {
            TechniqueCeiling.SINGLES -> {
                val digits = puzzle.copyOf()
                val candidates = Grader.computeCandidates(digits)
                var progressed = true
                while (progressed) {
                    progressed = Grader.applyNakedSingles(candidates, digits) ||
                            Grader.applyHiddenSingles(candidates, digits)
                }
                digits.any { it == 0 }
            }
            TechniqueCeiling.PAIRS -> {
                val gradeResult = Grader.grade(puzzle)
                gradeResult == Difficulty.HARD || gradeResult == Difficulty.EXPERT
            }
        }
    }

    /** Returns a puzzle Board (givens set) at the requested given-count grade. */
    suspend fun generateByGivenCount(grade: GivenGrade): Board {
        for (attempt in 1..MAX_ATTEMPTS) {
            try {
                val solution = fillGrid() ?: continue
                val puzzle = digHolesToTarget(solution, grade) ?: continue
                if (ceilingExceeded(puzzle, grade)) continue
                val givens = BooleanArray(81) { puzzle[it] != 0 }
                return Board.fromDigits(puzzle, givens, solution)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Unexpected error on this attempt — log and retry
            }
        }
        throw IllegalStateException("Failed to generate puzzle after $MAX_ATTEMPTS attempts")
    }
}
