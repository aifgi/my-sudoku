package sudoku.app.ui

import androidx.compose.material.AlertDialog
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import sudoku.app.state.GameIntent
import sudoku.app.state.GameViewModel

@Composable
fun App(viewModel: GameViewModel, onExitConfirmed: () -> Unit = {}) {
    val state by viewModel.state.collectAsState()
    val onIntent = viewModel::dispatch

    when {
        state.isLoading -> CircularProgressIndicator()
        state.digits.all { it == 0 } && !state.isComplete && state.undoStack.isEmpty() ->
            HomeScreen(onDifficultySelected = { difficulty ->
                onIntent(GameIntent.StartNewGame(difficulty))
            })
        else -> GameScreen(state = state, onIntent = onIntent)
    }

    if (state.showQuitConfirmation) {
        AlertDialog(
            onDismissRequest = { onIntent(GameIntent.CancelQuit) },
            title = { Text("Quit Game?") },
            text = { Text("You have unsaved progress. Are you sure you want to quit?") },
            confirmButton = {
                TextButton(onClick = { onIntent(GameIntent.ConfirmQuit); onExitConfirmed() }) {
                    Text("Quit")
                }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(GameIntent.CancelQuit) }) {
                    Text("Cancel")
                }
            }
        )
    }
}
