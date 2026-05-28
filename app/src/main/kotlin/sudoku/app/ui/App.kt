package sudoku.app.ui

import androidx.compose.material.AlertDialog
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import sudoku.app.state.GameIntent
import sudoku.app.state.GameViewModel
import sudoku.app.ui.i18n.AppLocale
import sudoku.app.ui.i18n.AppPreferences
import sudoku.app.ui.i18n.LocalStrings
import sudoku.app.ui.i18n.resolveLocale
import sudoku.app.ui.i18n.toStrings
import java.util.Locale

private fun resolveInitialLocale(): AppLocale =
    resolveLocale(AppPreferences.loadLocale(), Locale.getDefault().language)

@Composable
fun App(viewModel: GameViewModel, onExitConfirmed: () -> Unit = {}) {
    var locale by remember { mutableStateOf(resolveInitialLocale()) }
    val state by viewModel.state.collectAsState()
    val onIntent = viewModel::dispatch

    CompositionLocalProvider(LocalStrings provides locale.toStrings()) {
        when {
            state.isLoading -> CircularProgressIndicator()
            state.digits.all { it == 0 } && !state.isComplete && state.undoStack.isEmpty() ->
                HomeScreen(
                    onDifficultySelected = { difficulty ->
                        onIntent(GameIntent.StartNewGame(difficulty))
                    },
                    currentLocale = locale,
                    onLocaleChange = { newLocale ->
                        locale = newLocale
                        AppPreferences.saveLocale(newLocale)
                    },
                )
            else -> GameScreen(state = state, onIntent = onIntent)
        }

        if (state.showQuitConfirmation) {
            val strings = LocalStrings.current
            AlertDialog(
                onDismissRequest = { onIntent(GameIntent.CancelQuit) },
                title = { Text(strings.quitTitle) },
                text = { Text(strings.quitMessage) },
                confirmButton = {
                    TextButton(onClick = { onIntent(GameIntent.ConfirmQuit); onExitConfirmed() }) {
                        Text(strings.quitConfirm)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onIntent(GameIntent.CancelQuit) }) {
                        Text(strings.quitCancel)
                    }
                }
            )
        }
    }
}
