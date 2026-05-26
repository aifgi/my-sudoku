package sudoku.app.ui

import androidx.compose.foundation.focusable
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
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
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                handleKeyEvent(keyEvent, state, onIntent)
            }
    ) {
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

private fun handleKeyEvent(
    keyEvent: androidx.compose.ui.input.key.KeyEvent,
    state: GameState,
    onIntent: (GameIntent) -> Unit,
): Boolean {
    if (keyEvent.type != KeyEventType.KeyDown) return false

    // When paused, only allow toggle pause
    if (state.isPaused) {
        return when (keyEvent.key) {
            Key.P, Key.Spacebar -> { onIntent(GameIntent.TogglePause); true }
            else -> false
        }
    }

    val selected = state.selectedIndex ?: -1

    return when {
        keyEvent.key == Key.DirectionUp && selected >= 9 -> {
            onIntent(GameIntent.SelectCell(selected - 9)); true
        }
        keyEvent.key == Key.DirectionDown && selected <= 71 -> {
            onIntent(GameIntent.SelectCell(selected + 9)); true
        }
        keyEvent.key == Key.DirectionLeft && selected % 9 > 0 -> {
            onIntent(GameIntent.SelectCell(selected - 1)); true
        }
        keyEvent.key == Key.DirectionRight && selected % 9 < 8 -> {
            onIntent(GameIntent.SelectCell(selected + 1)); true
        }
        keyEvent.key == Key.Tab && !keyEvent.isShiftPressed -> {
            onIntent(GameIntent.SelectCell((if (selected < 0) 0 else (selected + 1) % 81))); true
        }
        keyEvent.key == Key.Tab && keyEvent.isShiftPressed -> {
            onIntent(GameIntent.SelectCell((if (selected < 0) 80 else (selected + 80) % 81))); true
        }
        keyEvent.key in listOf(Key.Zero, Key.Backspace, Key.Delete) -> {
            onIntent(GameIntent.EraseCell); true
        }
        keyEvent.key == Key.Escape -> { onIntent(GameIntent.DeselectCell); true }
        keyEvent.key == Key.P || keyEvent.key == Key.Spacebar -> {
            onIntent(GameIntent.TogglePause); true
        }
        keyEvent.key == Key.H -> { onIntent(GameIntent.RequestHint); true }
        (keyEvent.isCtrlPressed || keyEvent.isMetaPressed) && keyEvent.key == Key.Z && !keyEvent.isShiftPressed -> {
            onIntent(GameIntent.Undo); true
        }
        (keyEvent.isCtrlPressed || keyEvent.isMetaPressed) && (keyEvent.key == Key.Y ||
            (keyEvent.key == Key.Z && keyEvent.isShiftPressed)) -> {
            onIntent(GameIntent.Redo); true
        }
        (keyEvent.isCtrlPressed || keyEvent.isMetaPressed) && keyEvent.key == Key.N -> {
            onIntent(GameIntent.StartNewGame(state.difficulty)); true
        }
        else -> {
            val digit = when (keyEvent.key) {
                Key.One -> 1; Key.Two -> 2; Key.Three -> 3
                Key.Four -> 4; Key.Five -> 5; Key.Six -> 6
                Key.Seven -> 7; Key.Eight -> 8; Key.Nine -> 9
                else -> 0
            }
            if (digit in 1..9 && selected >= 0) {
                onIntent(GameIntent.EnterDigit(digit)); true
            } else false
        }
    }
}
