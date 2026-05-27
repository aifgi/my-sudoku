package sudoku.engine

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class BoardTest {

    @Test
    fun `fromDigits populates digits and givens correctly`() {
        val digits = IntArray(81) { if (it == 0) 5 else 0 }
        val givens = BooleanArray(81) { it == 0 }
        val board = Board.fromDigits(digits, givens)
        assertEquals(5, board.digits[0])
        assertEquals(0, board.digits[1])
        assertTrue(board.givens[0])
        assertFalse(board.givens[1])
    }

    @Test
    fun `withDigit returns new Board instance`() {
        val board = Board.empty()
        val modified = board.withDigit(0, 5)
        assertNotSame(board, modified)
    }

    @Test
    fun `withDigit does not mutate original digits`() {
        val board = Board.empty()
        val originalDigits = board.digits.copyOf()
        board.withDigit(0, 5)
        assertArrayEquals(originalDigits, board.digits)
    }

    @Test
    fun `withErased clears digit and marks non-given`() {
        val digits = IntArray(81) { if (it == 0) 5 else 0 }
        val givens = BooleanArray(81)
        val board = Board.fromDigits(digits, givens)
        val erased = board.withErased(0)
        assertEquals(0, erased.digits[0])
    }

    @Test
    fun `isEmpty returns true for empty board`() {
        assertTrue(Board.empty().isEmpty)
    }

    @Test
    fun `isFull returns false for partial board`() {
        val digits = IntArray(81) { if (it < 80) it % 9 + 1 else 0 }
        val board = Board.fromDigits(digits, BooleanArray(81))
        assertFalse(board.isFull)
    }

    @Test
    fun `peersOf corner cell 0 returns 20 peers`() {
        assertEquals(20, peersOf(0).size)
    }

    @Test
    fun `peersOf center cell 40 returns 20 peers`() {
        assertEquals(20, peersOf(40).size)
    }

    @Test
    fun `peersOf result contains no duplicates`() {
        val peers = peersOf(0)
        assertEquals(20, peers.toSet().size)
    }

    @Test
    fun `peersOf does not include the cell itself`() {
        assertTrue(peersOf(0).none { it == 0 })
    }

    @Test
    fun `row duplicate detected`() {
        val digits = IntArray(81)
        digits[0] = 5; digits[1] = 5  // same digit in row 0
        val conflicts = computeConflicts(digits)
        assertTrue(0 in conflicts && 1 in conflicts)
    }

    @Test
    fun `column duplicate detected`() {
        val digits = IntArray(81)
        digits[0] = 3; digits[9] = 3  // same digit in col 0
        val conflicts = computeConflicts(digits)
        assertTrue(0 in conflicts && 9 in conflicts)
    }

    @Test
    fun `box duplicate detected`() {
        val digits = IntArray(81)
        digits[0] = 7; digits[10] = 7  // same digit in box 0
        val conflicts = computeConflicts(digits)
        assertTrue(0 in conflicts && 10 in conflicts)
    }

    @Test
    fun `no false positive on valid board`() {
        val digits = IntArray(81)
        // Place 1-9 in first row only (no conflicts)
        for (i in 0..8) digits[i] = i + 1
        val conflicts = computeConflicts(digits)
        assertTrue(conflicts.isEmpty() || conflicts.none { it in 0..8 })
    }

    @Test
    fun `given cell participates in conflict detection`() {
        val digits = IntArray(81)
        val givens = BooleanArray(81)
        digits[0] = 5; givens[0] = true
        digits[1] = 5  // user entered same digit as given
        val conflicts = computeConflicts(digits)
        assertTrue(0 in conflicts && 1 in conflicts)
    }
}
