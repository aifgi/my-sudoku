package sudoku.app.ui.components

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import sudoku.app.ui.AppColors

@Composable
fun GameOverDialog(onNewGame: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Game Over") },
        text = { Text("You made 3 mistakes. Better luck next time!") },
        confirmButton = {
            Button(
                onClick = onNewGame,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = AppColors.NewGameBtn,
                    contentColor = Color.White,
                ),
            ) {
                Text("New Game")
            }
        },
        dismissButton = null,
    )
}
