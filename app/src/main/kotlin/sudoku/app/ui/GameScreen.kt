package sudoku.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import sudoku.app.state.GameIntent
import sudoku.app.state.GameState
import sudoku.app.ui.components.CompletionOverlay
import sudoku.app.ui.components.HintBanner
import sudoku.app.ui.components.NumberPad
import sudoku.app.ui.components.PauseOverlay
import sudoku.app.ui.components.SudokuBoard
import sudoku.app.ui.components.TimerDisplay
import sudoku.engine.Difficulty

@Composable
fun GameScreen(state: GameState, onIntent: (GameIntent) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Top toolbar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(onClick = {
                if (state.undoStack.isNotEmpty() && !state.isComplete) {
                    onIntent(GameIntent.StartNewGame(state.difficulty))
                } else {
                    onIntent(GameIntent.StartNewGame(state.difficulty))
                }
            }) { Text("New Game") }
            Spacer(modifier = Modifier.weight(1f))
            Text("Sudoku", style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.weight(1f))
            TimerDisplay(seconds = state.timerSeconds, isPaused = state.isPaused)
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { onIntent(GameIntent.TogglePause) }) {
                Text(if (state.isPaused) "▶" else "⏸")
            }
        }

        // Second toolbar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = { onIntent(GameIntent.Undo) },
                enabled = state.undoStack.isNotEmpty(),
            ) {
                Text("↩")
            }
            Spacer(modifier = Modifier.width(4.dp))
            Button(
                onClick = { onIntent(GameIntent.Redo) },
                enabled = state.redoStack.isNotEmpty(),
            ) {
                Text("↪")
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = { onIntent(GameIntent.RequestHint) }) {
                Text("Hint")
            }
        }

        // Board area
        Box(modifier = Modifier.weight(1f)) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                SudokuBoard(
                    state = state,
                    onCellClick = { index -> onIntent(GameIntent.SelectCell(index)) },
                )
            }
            if (state.isPaused) {
                PauseOverlay(onResume = { onIntent(GameIntent.TogglePause) })
            }
            if (state.isComplete) {
                CompletionOverlay(
                    difficulty = state.difficulty,
                    timerSeconds = state.timerSeconds,
                    onNewGame = { onIntent(GameIntent.StartNewGame(state.difficulty)) },
                    onBackToHome = { onIntent(GameIntent.StartNewGame(Difficulty.EASY)) },
                )
            }
        }

        // NumberPad
        NumberPad(
            onDigit = { digit -> onIntent(GameIntent.EnterDigit(digit)) },
            onErase = { onIntent(GameIntent.EraseCell) },
            enabled = !state.isLoading && !state.isComplete && !state.isPaused,
        )

        // HintBanner
        HintBanner(hintResult = state.hintResult)
    }

    // New game confirmation dialog
    if (state.showNewGameConfirmation) {
        AlertDialog(
            onDismissRequest = { onIntent(GameIntent.CancelNewGame) },
            title = { Text("New Game?") },
            text = { Text("Start a new game? Your current progress will be lost.") },
            confirmButton = {
                TextButton(onClick = { onIntent(GameIntent.ConfirmNewGame) }) { Text("Start") }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(GameIntent.CancelNewGame) }) { Text("Cancel") }
            }
        )
    }
}
