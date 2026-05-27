package sudoku.app.state

import sudoku.engine.Board
import sudoku.engine.Difficulty

sealed class GameIntent {
    // Navigation / lifecycle
    data class StartNewGame(val difficulty: Difficulty) : GameIntent()
    data object ConfirmNewGame : GameIntent()
    data object CancelNewGame : GameIntent()

    // Quit confirmation
    data object ShowQuitConfirmation : GameIntent()
    data object ConfirmQuit : GameIntent()
    data object CancelQuit : GameIntent()

    data class PuzzleGenerated(val board: Board) : GameIntent()

    // Cell interaction
    data class SelectCell(val index: Int) : GameIntent()
    data object DeselectCell : GameIntent()
    data class EnterDigit(val digit: Int) : GameIntent()
    data object EraseCell : GameIntent()

    // Undo / Redo
    data object Undo : GameIntent()
    data object Redo : GameIntent()

    // Hint
    data object RequestHint : GameIntent()

    // Timer
    data object TogglePause : GameIntent()
    data object TimerTick : GameIntent()

    // Completion
    data object GameCompleted : GameIntent()
}
