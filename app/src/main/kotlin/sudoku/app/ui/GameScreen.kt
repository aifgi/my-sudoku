package sudoku.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Badge
import androidx.compose.material.BadgedBox
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sudoku.app.state.GameIntent
import sudoku.app.state.GameState
import sudoku.app.ui.AppColors
import sudoku.app.ui.i18n.LocalStrings
import sudoku.app.ui.components.CompletionOverlay
import sudoku.app.ui.components.GameOverDialog
import sudoku.app.ui.components.HintBanner
import sudoku.app.ui.components.NumberPad
import sudoku.app.ui.components.PauseOverlay
import sudoku.app.ui.components.SudokuBoard
import sudoku.app.ui.components.formatTime

@Composable
fun GameScreen(state: GameState, onIntent: (GameIntent) -> Unit) {
    val strings = LocalStrings.current
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    // Re-request focus whenever all dialogs close, because AlertDialog steals focus and
    // LaunchedEffect(Unit) does not re-fire after the initial composition.
    LaunchedEffect(state.hasAnyDialogVisible) {
        if (!state.hasAnyDialogVisible) focusRequester.requestFocus()
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent -> handleKeyEvent(keyEvent, state, onIntent) },
    ) {
        // ── Left: Sudoku board ──────────────────────────────────────────────
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
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
                    onNewGame = { onIntent(GameIntent.GoToHome) },
                    onBackToHome = { onIntent(GameIntent.GoToHome) },
                )
            }
        }

        // ── Right: Controls panel ───────────────────────────────────────────
        Column(
            modifier = Modifier
                .width(280.dp)
                .fillMaxHeight()
                .padding(start = 4.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
                .focusProperties { canFocus = false },
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Stats row: Mistakes | Time + Pause
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatItem(label = strings.statMistakes, value = "${state.mistakeCount}/3")
                StatItem(
                    label = strings.statTime,
                    value = formatTime(state.timerSeconds),
                    trailing = {
                        Box(
                            modifier = Modifier
                                .padding(start = 6.dp)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(AppColors.PauseBtnBg)
                                .clickable { onIntent(GameIntent.TogglePause) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = if (state.isPaused) "▶" else "⏸",
                                fontSize = 12.sp,
                                color = AppColors.Primary,
                            )
                        }
                    },
                )
            }

            // Action buttons: Undo | Erase | Hints(N)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                ActionButton(
                    label = "↺",
                    enabled = state.undoStack.isNotEmpty() && !state.isComplete && !state.isGameOver,
                    onClick = { onIntent(GameIntent.Undo) },
                )
                ActionButton(
                    label = "⌫",
                    enabled = !state.isLoading && !state.isComplete && !state.isPaused && !state.isGameOver,
                    onClick = { onIntent(GameIntent.EraseCell) },
                )
                BadgedActionButton(
                    label = "💡",
                    badge = state.hintsRemaining.toString(),
                    badgeColor = AppColors.Primary,
                    enabled = !state.isLoading && !state.isComplete && !state.isPaused && state.hintsRemaining > 0 && !state.isGameOver,
                    onClick = { onIntent(GameIntent.RequestHint) },
                )
            }

            // Hint banner — fixed-height slot so layout doesn't shift
            Box(modifier = Modifier.fillMaxWidth().height(80.dp)) {
                if (state.hintResult != null) {
                    HintBanner(hintResult = state.hintResult)
                }
            }

            // 3×3 number pad
            NumberPad(
                onDigit = { digit -> onIntent(GameIntent.EnterDigit(digit)) },
                enabled = !state.isLoading && !state.isComplete && !state.isPaused && !state.isGameOver,
            )

            Spacer(modifier = Modifier.weight(1f))

            // New Game button
            Button(
                onClick = { onIntent(GameIntent.GoToHome) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = AppColors.NewGameBtn,
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.elevation(defaultElevation = 0.dp),
            ) {
                Text(
                    text = strings.actionNewGame,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }

    // Game over dialog
    if (state.isGameOver) {
        GameOverDialog(mistakeCount = state.mistakeCount, onNewGame = { onIntent(GameIntent.GoToHome) })
    }

    // New game confirmation dialog
    if (state.showNewGameConfirmation) {
        AlertDialog(
            onDismissRequest = { onIntent(GameIntent.CancelNewGame) },
            title = { Text(strings.newGameTitle) },
            text = { Text(strings.newGameMessage) },
            confirmButton = {
                TextButton(onClick = { onIntent(GameIntent.ConfirmNewGame) }) { Text(strings.newGameConfirm) }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(GameIntent.CancelNewGame) }) { Text(strings.newGameCancel) }
            },
        )
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    trailing: (@Composable () -> Unit)? = null,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = AppColors.StatLabel,
            fontWeight = FontWeight.Medium,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                fontSize = 18.sp,
                color = AppColors.StatValue,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
            )
            trailing?.invoke()
        }
    }
}

@Composable
private fun ActionButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(if (enabled) AppColors.ActionBtnBg else AppColors.ActionBtnDis)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 20.sp,
            color = if (enabled) AppColors.Primary else Color(0xFFAAAAAA),
        )
    }
}

@Composable
private fun BadgedActionButton(
    label: String,
    badge: String,
    badgeColor: Color,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    BadgedBox(
        badge = {
            Badge(
                backgroundColor = badgeColor,
                contentColor = Color.White,
            ) {
                Text(badge, fontSize = 8.sp)
            }
        },
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(if (enabled) AppColors.ActionBtnBg else AppColors.ActionBtnDis)
                .clickable(enabled = enabled, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                fontSize = 20.sp,
                color = if (enabled) AppColors.Primary else Color(0xFFAAAAAA),
            )
        }
    }
}

private fun handleKeyEvent(
    keyEvent: androidx.compose.ui.input.key.KeyEvent,
    state: GameState,
    onIntent: (GameIntent) -> Unit,
): Boolean {
    if (keyEvent.type != KeyEventType.KeyDown) return false

    if (state.isGameOver) return false

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
        keyEvent.key == Key.DirectionDown && selected in 0..71 -> {
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
            onIntent(GameIntent.GoToHome); true
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
