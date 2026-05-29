package sudoku.app

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import sudoku.app.state.GameIntent
import sudoku.app.state.GameViewModel
import sudoku.app.ui.App

fun main() = application {
    val coroutineScope = rememberCoroutineScope()
    val viewModel = remember { GameViewModel(coroutineScope, debugMode = System.getProperty("debug") == "true") }
    val state by viewModel.state.collectAsState()

    Window(
        onCloseRequest = {
            if (state.hasProgress) {
                if (!state.showQuitConfirmation) {
                    viewModel.dispatch(GameIntent.ShowQuitConfirmation)
                }
            } else {
                exitApplication()
            }
        },
        title = "Sudoku",
        state = rememberWindowState(width = 700.dp, height = 800.dp),
    ) {
        window.minimumSize = java.awt.Dimension(600, 700)
        App(viewModel, onExitConfirmed = ::exitApplication)
    }
}
