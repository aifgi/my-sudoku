package sudoku.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import sudoku.engine.Difficulty

@Composable
fun HomeScreen(onDifficultySelected: (Difficulty) -> Unit) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Sudoku",
                style = MaterialTheme.typography.h4,
            )
            Spacer(modifier = Modifier.height(32.dp))
            Difficulty.entries.forEach { difficulty ->
                Button(
                    onClick = { onDifficultySelected(difficulty) },
                    modifier = Modifier.widthIn(min = 200.dp),
                ) {
                    Text(difficulty.name.lowercase().replaceFirstChar { it.uppercase() })
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
