package sudoku.app.state

import sudoku.engine.Difficulty
import sudoku.engine.HintResult

data class GameState(
    // Board data
    val digits: IntArray,
    val givens: BooleanArray,
    val solution: IntArray,
    val conflictIndices: Set<Int>,
    val selectedIndex: Int?,
    val numberHighlightDigit: Int?,

    // Hint
    val hintResult: HintResult?,

    // Undo / Redo
    val undoStack: List<IntArray>,
    val redoStack: List<IntArray>,

    // Timer
    val timerSeconds: Long,
    val isPaused: Boolean,

    // Game lifecycle
    val isComplete: Boolean,
    val difficulty: Difficulty,

    // Loading
    val isLoading: Boolean,
    val pendingDifficulty: Difficulty?,

    // New game confirmation dialog
    val showNewGameConfirmation: Boolean,
    val newGameTargetDifficulty: Difficulty?,

    // Quit confirmation dialog
    val showQuitConfirmation: Boolean,

    // Stats
    val mistakeCount: Int,
    val hintsRemaining: Int,
    val isGameOver: Boolean,
) {
    companion object {
        val Initial = GameState(
            digits = IntArray(81),
            givens = BooleanArray(81),
            solution = IntArray(81),
            conflictIndices = emptySet(),
            selectedIndex = null,
            numberHighlightDigit = null,
            hintResult = null,
            undoStack = emptyList(),
            redoStack = emptyList(),
            timerSeconds = 0L,
            isPaused = false,
            isComplete = false,
            difficulty = Difficulty.EASY,
            isLoading = false,
            pendingDifficulty = null,
            showNewGameConfirmation = false,
            newGameTargetDifficulty = null,
            showQuitConfirmation = false,
            mistakeCount = 0,
            hintsRemaining = 3,
            isGameOver = false,
        )
    }

    // Custom equals/hashCode needed because IntArray/BooleanArray don't implement structural equality
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GameState) return false
        return digits.contentEquals(other.digits) &&
               givens.contentEquals(other.givens) &&
               solution.contentEquals(other.solution) &&
               conflictIndices == other.conflictIndices &&
               selectedIndex == other.selectedIndex &&
               numberHighlightDigit == other.numberHighlightDigit &&
               hintResult == other.hintResult &&
               undoStack.size == other.undoStack.size &&
               redoStack.size == other.redoStack.size &&
               timerSeconds == other.timerSeconds &&
               isPaused == other.isPaused &&
               isComplete == other.isComplete &&
               difficulty == other.difficulty &&
               isLoading == other.isLoading &&
               pendingDifficulty == other.pendingDifficulty &&
               showNewGameConfirmation == other.showNewGameConfirmation &&
               newGameTargetDifficulty == other.newGameTargetDifficulty &&
               showQuitConfirmation == other.showQuitConfirmation &&
               mistakeCount == other.mistakeCount &&
               hintsRemaining == other.hintsRemaining &&
               isGameOver == other.isGameOver
    }

    override fun hashCode(): Int = digits.contentHashCode() * 31 + givens.contentHashCode()
}
