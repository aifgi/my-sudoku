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
import sudoku.app.ui.i18n.LocalStrings
import sudoku.engine.Difficulty
import sudoku.engine.GivenGrade
import sudoku.engine.PuzzleDifficulty

@Composable
fun CompletionOverlay(
    difficulty: PuzzleDifficulty,
    timerSeconds: Long,
    onNewGame: () -> Unit,
    onBackToHome: () -> Unit,
) {
    val strings = LocalStrings.current
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
                text = strings.completionTitle,
                style = MaterialTheme.typography.h4,
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when (difficulty) {
                    is PuzzleDifficulty.Technique -> when (difficulty.grade) {
                        Difficulty.EASY -> strings.difficultyEasy
                        Difficulty.MEDIUM -> strings.difficultyMedium
                        Difficulty.HARD -> strings.difficultyHard
                        Difficulty.EXPERT -> strings.difficultyExpert
                    }
                    is PuzzleDifficulty.Given -> when (difficulty.grade) {
                        GivenGrade.EASY -> strings.difficultyEasy
                        GivenGrade.MEDIUM -> strings.difficultyMedium
                        GivenGrade.HARD -> strings.difficultyHard
                        GivenGrade.EXPERT -> strings.difficultyExpert
                    }
                },
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
                    Text(strings.completionNewGame)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = onBackToHome) {
                    Text(strings.completionBackToHome)
                }
            }
        }
    }
}
