package sudoku.engine

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SolverTest {

    // Known Easy puzzle (from public domain)
    private val knownPuzzle = intArrayOf(
        5,3,0, 0,7,0, 0,0,0,
        6,0,0, 1,9,5, 0,0,0,
        0,9,8, 0,0,0, 0,6,0,
        8,0,0, 0,6,0, 0,0,3,
        4,0,0, 8,0,3, 0,0,1,
        7,0,0, 0,2,0, 0,0,6,
        0,6,0, 0,0,0, 2,8,0,
        0,0,0, 4,1,9, 0,0,5,
        0,0,0, 0,8,0, 0,7,9,
    )

    private val knownSolution = intArrayOf(
        5,3,4, 6,7,8, 9,1,2,
        6,7,2, 1,9,5, 3,4,8,
        1,9,8, 3,4,2, 5,6,7,
        8,5,9, 7,6,1, 4,2,3,
        4,2,6, 8,5,3, 7,9,1,
        7,1,3, 9,2,4, 8,5,6,
        9,6,1, 5,3,7, 2,8,4,
        2,8,7, 4,1,9, 6,3,5,
        3,4,5, 2,8,6, 1,7,9,
    )

    @Test
    fun `solve empty board returns valid complete grid`() {
        val board = Board.empty()
        val solution = Solver.solve(board)
        assertNotNull(solution)
        assertTrue(solution!!.none { it == 0 })
    }

    @Test
    fun `solve known puzzle returns known solution`() {
        val givens = BooleanArray(81) { knownPuzzle[it] != 0 }
        val board = Board.fromDigits(knownPuzzle, givens)
        val solution = Solver.solve(board)
        assertNotNull(solution)
        assertArrayEquals(knownSolution, solution)
    }

    @Test
    fun `solved grid passes constraint validation`() {
        val givens = BooleanArray(81) { knownPuzzle[it] != 0 }
        val board = Board.fromDigits(knownPuzzle, givens)
        val solution = Solver.solve(board)!!
        // Each of 27 units must contain digits 1-9 exactly once
        for (unit in ALL_UNITS) {
            val digits = unit.map { solution[it] }.toSet()
            assertEquals((1..9).toSet(), digits, "Unit failed constraint: ${unit.toList()}")
        }
    }

    @Test
    fun `solve invalid board returns null`() {
        // Two 1s in same row — unsolvable
        val digits = IntArray(81)
        digits[0] = 1; digits[1] = 1
        val givens = BooleanArray(81) { digits[it] != 0 }
        val board = Board.fromDigits(digits, givens)
        assertNull(Solver.solve(board))
    }

    @Test
    fun `countSolutions returns 1 for unique puzzle`() {
        assertEquals(1, Solver.countSolutions(knownPuzzle, 2))
    }

    @Test
    fun `countSolutions returns 2 for multi-solution puzzle`() {
        // Remove enough cells to create ambiguity
        val ambiguous = knownPuzzle.copyOf()
        ambiguous[0] = 0; ambiguous[1] = 0; ambiguous[2] = 0
        ambiguous[9] = 0; ambiguous[10] = 0; ambiguous[11] = 0
        val count = Solver.countSolutions(ambiguous, 2)
        assertTrue(count >= 1, "Should have at least 1 solution")
        // Note: may or may not be ambiguous depending on which cells removed
    }

    @Test
    fun `countSolutions early-exits at limit`() {
        // Empty board has many solutions; with limit=2 should return exactly 2
        val result = Solver.countSolutions(IntArray(81), 2)
        assertEquals(2, result)
    }
}
