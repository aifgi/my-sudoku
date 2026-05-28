package sudoku.app.ui.components

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import sudoku.app.ui.AppColors
import sudoku.app.ui.i18n.LocalStrings

@Composable
fun GameOverDialog(mistakeCount: Int, onNewGame: () -> Unit) {
    val strings = LocalStrings.current
    AlertDialog(
        onDismissRequest = {},
        title = { Text(strings.gameOverTitle) },
        text = { Text(strings.gameOverMistakes(mistakeCount)) },
        confirmButton = {
            Button(
                onClick = onNewGame,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = AppColors.NewGameBtn,
                    contentColor = Color.White,
                ),
            ) {
                Text(strings.gameOverNewGame)
            }
        },
        dismissButton = null,
    )
}
