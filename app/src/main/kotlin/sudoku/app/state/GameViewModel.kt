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
    private val debugMode: Boolean = false,
) {
    private val _state = MutableStateFlow(GameState.Initial)
    val state: StateFlow<GameState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private var generationJob: Job? = null

    fun dispatch(intent: GameIntent) {
        _state.update { current -> reduce(current, intent) }
        handleSideEffects(intent)
        val s = _state.value
        if (s.isGameOver || s.isComplete) timerJob?.cancel()
    }

    private fun reduce(state: GameState, intent: GameIntent): GameState = when (intent) {
        is GameIntent.StartNewGame -> if (state.hasProgress) {
            state.copy(
                showNewGameConfirmation = true,
                newGameTargetDifficulty = intent.difficulty,
            )
        } else {
            state.copy(
                isLoading = true,
                pendingDifficulty = intent.difficulty,
                difficulty = intent.difficulty,
                undoStack = emptyList(),
                redoStack = emptyList(),
                hintResult = null,
                showNewGameConfirmation = false,
                newGameTargetDifficulty = null,
            )
        }
        is GameIntent.PuzzleGenerated -> {
            val digits = intent.board.digits.copyOf()
            val givens = intent.board.givens.copyOf()
            state.copy(
                digits = digits,
                givens = givens,
                solution = intent.board.solution.copyOf(),
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
                mistakeCount = 0,
                hintsRemaining = if (debugMode) 99 else 3,
                isGameOver = false,
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
            val hint = HintEngine.findHint(board, state.difficulty)
            val hintIndex = if (hint is sudoku.engine.HintResult.Found) hint.targetCells.firstOrNull() else null
            state.copy(
                hintResult = hint,
                numberHighlightDigit = null,
                hintsRemaining = maxOf(0, state.hintsRemaining - 1),
                selectedIndex = hintIndex ?: state.selectedIndex,
            )
        }
        is GameIntent.TogglePause -> state.copy(
            isPaused = !state.isPaused,
            numberHighlightDigit = null,
        )
        is GameIntent.TimerTick -> if (!state.isPaused && !state.isComplete && !state.isGameOver && !state.isLoading)
            state.copy(timerSeconds = state.timerSeconds + 1)
        else state
        is GameIntent.GameCompleted -> state.copy(isComplete = true)
        is GameIntent.ShowQuitConfirmation -> state.copy(showQuitConfirmation = true)
        is GameIntent.ConfirmQuit -> state.copy(showQuitConfirmation = false)
        is GameIntent.CancelQuit -> state.copy(showQuitConfirmation = false)
        is GameIntent.ConfirmNewGame -> state.copy(
            isLoading = true,
            pendingDifficulty = state.newGameTargetDifficulty ?: state.difficulty,
            difficulty = state.newGameTargetDifficulty ?: state.difficulty,
            undoStack = emptyList(),
            redoStack = emptyList(),
            hintResult = null,
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
        val conflicts = computeConflicts(newDigits, state.solution)
        val isWrong = state.solution[idx] != 0 && digit != state.solution[idx]
        val newMistakeCount = if (isWrong) state.mistakeCount + 1 else state.mistakeCount
        val withMistake = state.copy(
            digits = newDigits,
            conflictIndices = conflicts,
            undoStack = newUndo,
            redoStack = emptyList(),
            numberHighlightDigit = null,
            hintResult = null,
            mistakeCount = newMistakeCount,
            isGameOver = newMistakeCount >= 3,
        )
        return if (withMistake.isGameOver) withMistake else checkCompletion(withMistake)
    }

    private fun applyEraseCell(state: GameState): GameState {
        val idx = state.selectedIndex ?: return state
        if (state.givens[idx]) return state
        val newDigits = state.digits.copyOf()
        newDigits[idx] = 0
        val newUndo = state.undoStack + listOf(state.digits.copyOf())
        return state.copy(
            digits = newDigits,
            conflictIndices = computeConflicts(newDigits, state.solution),
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
            conflictIndices = computeConflicts(prevDigits, state.solution),
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
            conflictIndices = computeConflicts(nextDigits, state.solution),
            undoStack = newUndo,
            redoStack = newRedo,
        )
    }

    private fun checkCompletion(state: GameState): GameState {
        return if (state.digits.none { it == 0 } && state.conflictIndices.isEmpty()) {
            state.copy(isComplete = true)
        } else {
            state
        }
    }

    internal fun computeConflicts(digits: IntArray, solution: IntArray = IntArray(81)): Set<Int> {
        val structural = sudoku.engine.computeConflicts(digits)
        val wrong = digits.indices.filter { i ->
            digits[i] != 0 && solution[i] != 0 && digits[i] != solution[i]
        }.toSet()
        return structural + wrong
    }

    private fun handleSideEffects(intent: GameIntent) {
        when (intent) {
            is GameIntent.StartNewGame -> if (!_state.value.showNewGameConfirmation) launchGeneration(intent.difficulty)
            is GameIntent.ConfirmNewGame -> launchGeneration(_state.value.pendingDifficulty ?: _state.value.difficulty)
            is GameIntent.TogglePause -> syncTimer()
            is GameIntent.PuzzleGenerated -> startTimer()
            else -> {}
        }
    }

    private fun launchGeneration(difficulty: sudoku.engine.PuzzleDifficulty) {
        generationJob?.cancel()
        generationJob = coroutineScope.launch {
            try {
                val board = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                    when (difficulty) {
                        is sudoku.engine.PuzzleDifficulty.Technique -> sudoku.engine.Generator.generate(difficulty.grade)
                        is sudoku.engine.PuzzleDifficulty.Given -> sudoku.engine.Generator.generateByGivenCount(difficulty.grade)
                    }
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
