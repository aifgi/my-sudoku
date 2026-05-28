package sudoku.app.state

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import sudoku.engine.Difficulty

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    private fun buildStateWithMistakes(mistakeCount: Int): GameState {
        // Set up a board where index 0 is not a given, solution[0] = 1
        val solution = IntArray(81) { if (it == 0) 1 else 2 }
        val digits = IntArray(81) { if (it == 0) 0 else 2 }
        val givens = BooleanArray(81) { it != 0 }
        return GameState.Initial.copy(
            digits = digits,
            givens = givens,
            solution = solution,
            mistakeCount = mistakeCount,
            selectedIndex = 0,
        )
    }

    @Test
    fun `isGameOver is true after 3rd mistake`() {
        val scope = TestScope(StandardTestDispatcher())
        val vm = GameViewModel(scope)

        // Inject state with 2 mistakes and cell 0 selected (not a given, solution=1)
        val stateWith2Mistakes = buildStateWithMistakes(2)
        // Use reflection to set the internal state since _state is private
        val stateField = GameViewModel::class.java.getDeclaredField("_state")
        stateField.isAccessible = true
        val stateFlow = stateField.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<GameState>
        stateFlow.value = stateWith2Mistakes

        // Enter wrong digit (9 != solution[0] which is 1) — should be 3rd mistake
        vm.dispatch(GameIntent.EnterDigit(9))

        val result = vm.state.value
        assertEquals(3, result.mistakeCount)
        assertTrue(result.isGameOver, "isGameOver should be true after 3rd mistake")
    }

    // Helper: build a state with isGameOver=true and timerSeconds=10
    private fun buildGameOverState(): GameState {
        return GameState.Initial.copy(
            isGameOver = true,
            timerSeconds = 10L,
        )
    }

    // Helper: valid complete Sudoku grid (index 42 = digit 7, row 4 col 6)
    // Solution: rows 0-8 are filled; index 42 is the only empty cell
    private fun buildNearCompleteState(): GameState {
        val solution = intArrayOf(
            5, 3, 4, 6, 7, 8, 9, 1, 2,
            6, 7, 2, 1, 9, 5, 3, 4, 8,
            1, 9, 8, 3, 4, 2, 5, 6, 7,
            8, 5, 9, 7, 6, 1, 4, 2, 3,
            4, 2, 6, 8, 5, 3, 7, 9, 1,
            7, 1, 3, 9, 2, 4, 8, 5, 6,
            9, 6, 1, 5, 3, 7, 2, 8, 4,
            2, 8, 7, 4, 1, 9, 6, 3, 5,
            3, 4, 5, 2, 8, 6, 1, 7, 9,
        )
        // All filled except index 42 (digit 7 at row 4, col 6)
        val digits = solution.copyOf()
        digits[42] = 0
        val givens = BooleanArray(81) { it != 42 }
        return GameState.Initial.copy(
            digits = digits,
            givens = givens,
            solution = solution,
            difficulty = Difficulty.EASY,
        )
    }

    @Test
    fun `TimerTick does not increment timerSeconds when isGameOver is true`() {
        val scope = TestScope(StandardTestDispatcher())
        val vm = GameViewModel(scope)

        val stateField = GameViewModel::class.java.getDeclaredField("_state")
        stateField.isAccessible = true
        val stateFlow = stateField.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<GameState>
        stateFlow.value = buildGameOverState()

        vm.dispatch(GameIntent.TimerTick)

        assertEquals(10L, vm.state.value.timerSeconds, "timerSeconds should not change when isGameOver is true")
    }

    @Test
    fun `PuzzleGenerated resets isGameOver to false`() {
        val scope = TestScope(StandardTestDispatcher())
        val vm = GameViewModel(scope)

        val stateField = GameViewModel::class.java.getDeclaredField("_state")
        stateField.isAccessible = true
        val stateFlow = stateField.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<GameState>
        stateFlow.value = buildGameOverState()

        // Build a minimal board to pass to PuzzleGenerated
        val board = sudoku.engine.Board.fromDigits(
            IntArray(81),
            BooleanArray(81),
        )
        vm.dispatch(GameIntent.PuzzleGenerated(board))

        assertFalse(vm.state.value.isGameOver, "isGameOver should be reset to false after PuzzleGenerated")
    }

    @Test
    fun `RequestHint sets selectedIndex to first targetCell from HintResult`() {
        val scope = TestScope(StandardTestDispatcher())
        val vm = GameViewModel(scope)

        val stateField = GameViewModel::class.java.getDeclaredField("_state")
        stateField.isAccessible = true
        val stateFlow = stateField.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<GameState>
        stateFlow.value = buildNearCompleteState()

        vm.dispatch(GameIntent.RequestHint)

        val result = vm.state.value
        // HintEngine should find a naked single at index 42 (only empty cell)
        // RequestHint reducer currently does NOT set selectedIndex — so this will fail until task 1.4
        assertEquals(42, result.selectedIndex, "selectedIndex should be set to first targetCell (42) from HintResult.Found")
    }

    // Finding: Undo IS allowed reducer-side even when isGameOver=true.
    // Game-over only disables the Undo button in the UI via the `!state.isGameOver`
    // guard on the button `enabled` property; the reducer itself has no such guard.
    @Test
    fun `Undo still applies reducer-side when isGameOver is true`() {
        val scope = TestScope(StandardTestDispatcher())
        val vm = GameViewModel(scope)

        // Build a game-over state with a non-empty undoStack
        val solution = IntArray(81) { 1 }
        val digits = IntArray(81) { 1 }
        val previousDigits = IntArray(81) { 0 }
        val givens = BooleanArray(81) { false }
        val gameOverState = GameState.Initial.copy(
            digits = digits,
            givens = givens,
            solution = solution,
            isGameOver = true,
            undoStack = listOf(previousDigits),
        )
        val stateField = GameViewModel::class.java.getDeclaredField("_state")
        stateField.isAccessible = true
        val stateFlow = stateField.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<GameState>
        stateFlow.value = gameOverState

        vm.dispatch(GameIntent.Undo)

        val result = vm.state.value
        // Undo was applied: undoStack is now empty, digits reverted to previousDigits
        assertTrue(result.undoStack.isEmpty(), "undoStack should be empty after Undo")
        assertTrue(result.digits.contentEquals(previousDigits), "digits should revert to previousDigits after Undo")
        // isGameOver is unchanged — the reducer does not touch it on Undo
        assertTrue(result.isGameOver, "isGameOver should remain true (only UI disables the button)")
    }

    @Test
    fun `StartNewGame after game-over resets isGameOver to false`() {
        val scope = TestScope(StandardTestDispatcher())
        val vm = GameViewModel(scope)

        val stateField = GameViewModel::class.java.getDeclaredField("_state")
        stateField.isAccessible = true
        val stateFlow = stateField.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<GameState>
        stateFlow.value = buildGameOverState()

        // StartNewGame sets isLoading = true and triggers async puzzle generation.
        // We verify the reducer side immediately: isGameOver is NOT reset by StartNewGame itself.
        vm.dispatch(GameIntent.StartNewGame(sudoku.engine.Difficulty.EASY))
        assertTrue(vm.state.value.isLoading, "isLoading should be true after StartNewGame")

        // Simulate the PuzzleGenerated callback that the async job would dispatch.
        val board = sudoku.engine.Board.fromDigits(IntArray(81), BooleanArray(81))
        vm.dispatch(GameIntent.PuzzleGenerated(board))

        assertFalse(vm.state.value.isGameOver, "isGameOver should be false after PuzzleGenerated (new game started)")
    }

    @Test
    fun `isGameOver true does not trigger isComplete on empty board`() {
        val scope = TestScope(StandardTestDispatcher())
        val vm = GameViewModel(scope)

        val stateWith2Mistakes = buildStateWithMistakes(2)
        val stateField = GameViewModel::class.java.getDeclaredField("_state")
        stateField.isAccessible = true
        val stateFlow = stateField.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<GameState>
        stateFlow.value = stateWith2Mistakes

        // Enter wrong digit to trigger game over
        vm.dispatch(GameIntent.EnterDigit(9))

        val result = vm.state.value
        assertTrue(result.isGameOver, "isGameOver should be true")
        assertFalse(result.isComplete, "isComplete should remain false when game is over by mistake limit")
    }

    // ── Bug #9 fix: checkCompletion must not call dispatch() inside _state.update ──

    @Test
    fun `entering last correct digit sets isComplete to true`() {
        val scope = TestScope(StandardTestDispatcher())
        val vm = GameViewModel(scope)

        val stateField = GameViewModel::class.java.getDeclaredField("_state")
        stateField.isAccessible = true
        val stateFlow = stateField.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<GameState>
        val nearComplete = buildNearCompleteState().copy(selectedIndex = 42)
        stateFlow.value = nearComplete

        vm.dispatch(GameIntent.EnterDigit(7)) // correct digit for index 42

        val result = vm.state.value
        assertTrue(result.isComplete, "isComplete should be true after filling last cell correctly")
        assertFalse(result.isGameOver, "isGameOver should remain false on completion")
        assertEquals(7, result.digits[42], "digit at index 42 should be 7")
    }

    @Test
    fun `puzzle completion state is consistent with no repeated completion`() {
        // Guards against the previous CAS-retry bug where checkCompletion dispatching
        // inside _state.update could cause applyEnterDigit to run twice, producing
        // redundant or inconsistent intermediate states.
        val scope = TestScope(StandardTestDispatcher())
        val vm = GameViewModel(scope)

        val stateField = GameViewModel::class.java.getDeclaredField("_state")
        stateField.isAccessible = true
        val stateFlow = stateField.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<GameState>
        val nearComplete = buildNearCompleteState().copy(selectedIndex = 42)
        stateFlow.value = nearComplete

        vm.dispatch(GameIntent.EnterDigit(7))

        val result = vm.state.value
        assertTrue(result.isComplete, "isComplete should be true")
        // If CAS-retry caused a double-run, mistakeCount or conflictIndices could diverge.
        assertEquals(0, result.mistakeCount, "mistakeCount should be unchanged")
        assertTrue(result.conflictIndices.isEmpty(), "no conflicts on a correct completion")
        assertTrue(result.digits.none { it == 0 }, "no empty cells remain")
    }

    // ── Bug #9 fix: hasAnyDialogVisible — used to re-request keyboard focus ──

    @Test
    fun `hasAnyDialogVisible is false when no dialogs are showing`() {
        assertFalse(GameState.Initial.hasAnyDialogVisible)
    }

    @Test
    fun `hasAnyDialogVisible is true when isGameOver`() {
        assertTrue(GameState.Initial.copy(isGameOver = true).hasAnyDialogVisible)
    }

    @Test
    fun `hasAnyDialogVisible is true when showNewGameConfirmation`() {
        assertTrue(GameState.Initial.copy(showNewGameConfirmation = true).hasAnyDialogVisible)
    }

    @Test
    fun `hasAnyDialogVisible is true when showQuitConfirmation`() {
        assertTrue(GameState.Initial.copy(showQuitConfirmation = true).hasAnyDialogVisible)
    }

    @Test
    fun `hasAnyDialogVisible becomes false after all dialogs are dismissed`() {
        val state = GameState.Initial.copy(
            isGameOver = true,
            showNewGameConfirmation = false,
            showQuitConfirmation = false,
        )
        assertTrue(state.hasAnyDialogVisible)
        assertFalse(state.copy(isGameOver = false).hasAnyDialogVisible)
    }
}
