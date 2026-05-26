package sudoku.app.state

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import sudoku.engine.Board
import sudoku.engine.HintEngine
import sudoku.engine.computeConflicts

class GameViewModel(
    private val coroutineScope: CoroutineScope,
) {
    private val _state = MutableStateFlow(GameState.Initial)
    val state: StateFlow<GameState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private var generationJob: Job? = null

    fun dispatch(intent: GameIntent) {
        _state.update { current -> reduce(current, intent) }
        handleSideEffects(intent)
    }

    private fun reduce(state: GameState, intent: GameIntent): GameState = when (intent) {
        is GameIntent.StartNewGame -> state.copy(
            isLoading = true,
            pendingDifficulty = intent.difficulty,
            difficulty = intent.difficulty,
            undoStack = emptyList(),
            redoStack = emptyList(),
            hintResult = null,
            showNewGameConfirmation = false,
            newGameTargetDifficulty = null,
        )
        is GameIntent.PuzzleGenerated -> {
            val digits = intent.board.digits.copyOf()
            val givens = intent.board.givens.copyOf()
            state.copy(
                digits = digits,
                givens = givens,
                conflictIndices = emptySet(),
                selectedIndex = null,
                numberHighlightDigit = null,
                hintResult = null,
                undoStack = emptyList(),
                redoStack = emptyList(),
                timerSeconds = 0L,
                isPaused = false,
                isComplete = false,
                isLoading = false,
                pendingDifficulty = null,
            )
        }
        is GameIntent.SelectCell -> {
            val digit = state.digits[intent.index]
            state.copy(
                selectedIndex = intent.index,
                numberHighlightDigit = if (digit != 0) digit else null,
            )
        }
        is GameIntent.DeselectCell -> state.copy(
            selectedIndex = null,
            numberHighlightDigit = null,
        )
        is GameIntent.EnterDigit -> applyEnterDigit(state, intent.digit)
        is GameIntent.EraseCell -> applyEraseCell(state)
        is GameIntent.Undo -> applyUndo(state)
        is GameIntent.Redo -> applyRedo(state)
        is GameIntent.RequestHint -> {
            val board = Board.fromDigits(state.digits, state.givens)
            state.copy(
                hintResult = HintEngine.findHint(board, state.difficulty),
                numberHighlightDigit = null,
            )
        }
        is GameIntent.TogglePause -> state.copy(
            isPaused = !state.isPaused,
            numberHighlightDigit = null,
        )
        is GameIntent.TimerTick -> if (!state.isPaused && !state.isComplete && !state.isLoading)
            state.copy(timerSeconds = state.timerSeconds + 1)
        else state
        is GameIntent.GameCompleted -> state.copy(isComplete = true)
        is GameIntent.ShowQuitConfirmation -> state.copy(showQuitConfirmation = true)
        is GameIntent.ConfirmQuit -> state.copy(showQuitConfirmation = false)
        is GameIntent.CancelQuit -> state.copy(showQuitConfirmation = false)
        is GameIntent.ConfirmNewGame -> state.copy(
            showNewGameConfirmation = false,
            newGameTargetDifficulty = null,
        )
        is GameIntent.CancelNewGame -> state.copy(
            showNewGameConfirmation = false,
            newGameTargetDifficulty = null,
        )
    }

    private fun applyEnterDigit(state: GameState, digit: Int): GameState {
        val idx = state.selectedIndex ?: return state
        if (state.givens[idx]) return state
        val newDigits = state.digits.copyOf()
        newDigits[idx] = digit
        val newUndo = state.undoStack + listOf(state.digits.copyOf())
        val conflicts = computeConflicts(newDigits)
        val newState = state.copy(
            digits = newDigits,
            conflictIndices = conflicts,
            undoStack = newUndo,
            redoStack = emptyList(),
            numberHighlightDigit = null,
            hintResult = null,
        )
        return checkCompletion(newState)
    }

    private fun applyEraseCell(state: GameState): GameState {
        val idx = state.selectedIndex ?: return state
        if (state.givens[idx]) return state
        val newDigits = state.digits.copyOf()
        newDigits[idx] = 0
        val newUndo = state.undoStack + listOf(state.digits.copyOf())
        return state.copy(
            digits = newDigits,
            conflictIndices = computeConflicts(newDigits),
            undoStack = newUndo,
            redoStack = emptyList(),
            numberHighlightDigit = null,
            hintResult = null,
        )
    }

    private fun applyUndo(state: GameState): GameState {
        if (state.undoStack.isEmpty()) return state
        val prevDigits = state.undoStack.last()
        val newUndo = state.undoStack.dropLast(1)
        val newRedo = state.redoStack + listOf(state.digits.copyOf())
        return state.copy(
            digits = prevDigits,
            conflictIndices = computeConflicts(prevDigits),
            undoStack = newUndo,
            redoStack = newRedo,
        )
    }

    private fun applyRedo(state: GameState): GameState {
        if (state.redoStack.isEmpty()) return state
        val nextDigits = state.redoStack.last()
        val newRedo = state.redoStack.dropLast(1)
        val newUndo = state.undoStack + listOf(state.digits.copyOf())
        return state.copy(
            digits = nextDigits,
            conflictIndices = computeConflicts(nextDigits),
            undoStack = newUndo,
            redoStack = newRedo,
        )
    }

    private fun checkCompletion(state: GameState): GameState {
        if (state.digits.none { it == 0 } && state.conflictIndices.isEmpty()) {
            dispatch(GameIntent.GameCompleted)
        }
        return state
    }

    internal fun computeConflicts(digits: IntArray): Set<Int> =
        sudoku.engine.computeConflicts(digits)

    private fun handleSideEffects(intent: GameIntent) {
        when (intent) {
            is GameIntent.StartNewGame -> launchGeneration(intent.difficulty)
            is GameIntent.TogglePause -> syncTimer()
            is GameIntent.PuzzleGenerated -> startTimer()
            is GameIntent.GameCompleted -> timerJob?.cancel()
            else -> {}
        }
    }

    private fun launchGeneration(difficulty: sudoku.engine.Difficulty) {
        generationJob?.cancel()
        generationJob = coroutineScope.launch {
            try {
                val board = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                    sudoku.engine.Generator.generate(difficulty)
                }
                dispatch(GameIntent.PuzzleGenerated(board))
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, pendingDifficulty = null) }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = coroutineScope.launch {
            while (isActive) {
                kotlinx.coroutines.delay(1_000)
                if (!_state.value.isPaused && !_state.value.isComplete) {
                    dispatch(GameIntent.TimerTick)
                }
            }
        }
    }

    private fun syncTimer() {
        val s = _state.value
        if (!s.isPaused && !s.isComplete) {
            startTimer()
        } else {
            timerJob?.cancel()
        }
    }
}
