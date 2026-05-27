package sudoku.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import sudoku.engine.Difficulty

@Composable
fun CompletionOverlay(
    difficulty: Difficulty,
    timerSeconds: Long,
    onNewGame: () -> Unit,
    onBackToHome: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
        ) {
            Text(
                text = "Puzzle Solved!",
                style = MaterialTheme.typography.h4,
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = difficulty.name.lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.h6,
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatTime(timerSeconds),
                style = MaterialTheme.typography.body1,
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row {
                Button(onClick = onNewGame) {
                    Text("New Game")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = onBackToHome) {
                    Text("Back to Home")
                }
            }
        }
    }
}
