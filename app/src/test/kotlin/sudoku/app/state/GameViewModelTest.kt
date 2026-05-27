package sudoku.app.state

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

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
}
