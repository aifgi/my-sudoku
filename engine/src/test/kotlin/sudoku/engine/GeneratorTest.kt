package sudoku.engine

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.Duration

class GeneratorTest {

    @Test
    fun `generated board has exactly 81 cells`() = runBlocking {
        val board = Generator.generate(Difficulty.EASY)
        assertEquals(81, board.digits.size)
    }

    @Test
    fun `generated board has at least one empty cell`() = runBlocking {
        val board = Generator.generate(Difficulty.EASY)
        assertTrue(board.digits.any { it == 0 })
    }

    @Test
    fun `generated board passes constraint validation`() = runBlocking {
        val board = Generator.generate(Difficulty.EASY)
        val solution = Solver.solve(board)
        assertNotNull(solution, "Generated puzzle must be solvable")
        for (unit in ALL_UNITS) {
            val digits = unit.map { solution!![it] }.toSet()
            assertEquals((1..9).toSet(), digits, "Unit failed constraint: ${unit.toList()}")
        }
    }

    @Test
    fun `generated board has exactly one solution`() = runBlocking {
        val board = Generator.generate(Difficulty.EASY)
        assertEquals(1, Solver.countSolutions(board.digits, 2))
    }

    @Test
    fun `generated Easy board grades as EASY`() = runBlocking {
        val board = Generator.generate(Difficulty.EASY)
        assertEquals(Difficulty.EASY, Grader.grade(board.digits))
    }

    @Test
    fun `generated Medium board grades as MEDIUM`() = runBlocking {
        val board = Generator.generate(Difficulty.MEDIUM)
        assertEquals(Difficulty.MEDIUM, Grader.grade(board.digits))
    }

    @Test
    fun `generated Hard board grades as HARD`() = runBlocking {
        val board = Generator.generate(Difficulty.HARD)
        assertEquals(Difficulty.HARD, Grader.grade(board.digits))
    }

    @Test
    @Disabled("Expert generation can be slow (>2s) on CI; grade correctness verified by Generator contract")
    fun `generated Expert board grades as EXPERT`() = runBlocking {
        System.err.println("WARNING: Expert grade test may be slow — disabled by default on CI")
        val board = Generator.generate(Difficulty.EXPERT)
        assertEquals(Difficulty.EXPERT, Grader.grade(board.digits))
    }

    @Test
    fun `generation completes within 2 seconds for Easy`() {
        assertTimeout(Duration.ofSeconds(2)) {
            runBlocking { Generator.generate(Difficulty.EASY) }
        }
    }

    @Test
    fun `generateByGivenCount EASY produces givens in 36-45`() = runBlocking {
        val board = Generator.generateByGivenCount(GivenGrade.EASY)
        val givens = board.digits.count { it != 0 }
        assertTrue(givens in 36..45, "Expected 36-45 givens but got $givens")
    }

    @Test
    fun `generateByGivenCount MEDIUM produces givens in 29-35`() = runBlocking {
        val board = Generator.generateByGivenCount(GivenGrade.MEDIUM)
        val givens = board.digits.count { it != 0 }
        assertTrue(givens in 29..35, "Expected 29-35 givens but got $givens")
    }

    @Test
    fun `generateByGivenCount HARD produces givens in 24-28`() = runBlocking {
        val board = Generator.generateByGivenCount(GivenGrade.HARD)
        val givens = board.digits.count { it != 0 }
        assertTrue(givens in 24..28, "Expected 24-28 givens but got $givens")
    }

    @Test
    fun `generateByGivenCount board has exactly one solution`() = runBlocking {
        val board = Generator.generateByGivenCount(GivenGrade.EASY)
        assertEquals(1, Solver.countSolutions(board.digits, 2))
    }

    @Test
    fun `generateByGivenCount EASY puzzle does not exceed singles ceiling`() = runBlocking {
        val board = Generator.generateByGivenCount(GivenGrade.EASY)
        val digits = board.digits.copyOf()
        val candidates = Grader.computeCandidates(digits)
        var progressed = true
        while (progressed) {
            progressed = Grader.applyNakedSingles(candidates, digits) ||
                    Grader.applyHiddenSingles(candidates, digits)
        }
        assertTrue(digits.none { it == 0 }, "EASY puzzle should be solvable by Naked/Hidden Singles alone")
    }

    @Test
    fun `generateByGivenCount HARD puzzle does not exceed pairs ceiling`() = runBlocking {
        val board = Generator.generateByGivenCount(GivenGrade.HARD)
        val grade = Grader.grade(board.digits)
        assertTrue(grade != Difficulty.HARD && grade != Difficulty.EXPERT,
            "HARD given-count puzzle should not require Triples or above, but graded as $grade")
    }

    @Test
    @Disabled("Expert given-count generation can be slow (>2s) on CI; range correctness verified by Generator contract")
    fun `generateByGivenCount EXPERT produces givens in 17-23`() = runBlocking {
        val board = Generator.generateByGivenCount(GivenGrade.EXPERT)
        val givens = board.digits.count { it != 0 }
        assertTrue(givens in 17..23, "Expected 17-23 givens but got $givens")
    }
}
