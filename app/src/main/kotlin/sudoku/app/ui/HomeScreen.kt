package sudoku.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import sudoku.app.ui.i18n.AppLocale
import sudoku.app.ui.i18n.LocalStrings
import sudoku.engine.Difficulty
import sudoku.engine.GivenGrade
import sudoku.engine.PuzzleDifficulty

@Composable
fun HomeScreen(
    onDifficultySelected: (PuzzleDifficulty) -> Unit,
    currentLocale: AppLocale,
    onLocaleChange: (AppLocale) -> Unit,
    currentMode: DifficultyMode,
    onModeChange: (DifficultyMode) -> Unit,
) {
    val strings = LocalStrings.current
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = strings.appTitle,
                style = MaterialTheme.typography.h4,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.Center) {
                TextButton(
                    onClick = { if (currentLocale != AppLocale.ENGLISH) onLocaleChange(AppLocale.ENGLISH) },
                    modifier = Modifier.alpha(if (currentLocale == AppLocale.ENGLISH) 1f else 0.4f),
                ) {
                    Text(
                        text = "\uD83C\uDDEC\uD83C\uDDE7",
                        fontWeight = if (currentLocale == AppLocale.ENGLISH) FontWeight.Bold else FontWeight.Normal,
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = { if (currentLocale != AppLocale.RUSSIAN) onLocaleChange(AppLocale.RUSSIAN) },
                    modifier = Modifier.alpha(if (currentLocale == AppLocale.RUSSIAN) 1f else 0.4f),
                ) {
                    Text(
                        text = "\uD83C\uDDF7\uD83C\uDDFA",
                        fontWeight = if (currentLocale == AppLocale.RUSSIAN) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.Center) {
                TextButton(
                    onClick = { if (currentMode != DifficultyMode.TECHNIQUE) onModeChange(DifficultyMode.TECHNIQUE) },
                    modifier = Modifier.alpha(if (currentMode == DifficultyMode.TECHNIQUE) 1f else 0.4f),
                ) {
                    Text(
                        text = strings.modeTechnique,
                        fontWeight = if (currentMode == DifficultyMode.TECHNIQUE) FontWeight.Bold else FontWeight.Normal,
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = { if (currentMode != DifficultyMode.GIVEN) onModeChange(DifficultyMode.GIVEN) },
                    modifier = Modifier.alpha(if (currentMode == DifficultyMode.GIVEN) 1f else 0.4f),
                ) {
                    Text(
                        text = strings.modeGiven,
                        fontWeight = if (currentMode == DifficultyMode.GIVEN) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    onDifficultySelected(
                        if (currentMode == DifficultyMode.TECHNIQUE) PuzzleDifficulty.Technique(Difficulty.EASY)
                        else PuzzleDifficulty.Given(GivenGrade.EASY)
                    )
                },
                modifier = Modifier.widthIn(min = 200.dp),
            ) {
                Text(strings.difficultyEasy)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    onDifficultySelected(
                        if (currentMode == DifficultyMode.TECHNIQUE) PuzzleDifficulty.Technique(Difficulty.MEDIUM)
                        else PuzzleDifficulty.Given(GivenGrade.MEDIUM)
                    )
                },
                modifier = Modifier.widthIn(min = 200.dp),
            ) {
                Text(strings.difficultyMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    onDifficultySelected(
                        if (currentMode == DifficultyMode.TECHNIQUE) PuzzleDifficulty.Technique(Difficulty.HARD)
                        else PuzzleDifficulty.Given(GivenGrade.HARD)
                    )
                },
                modifier = Modifier.widthIn(min = 200.dp),
            ) {
                Text(strings.difficultyHard)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    onDifficultySelected(
                        if (currentMode == DifficultyMode.TECHNIQUE) PuzzleDifficulty.Technique(Difficulty.EXPERT)
                        else PuzzleDifficulty.Given(GivenGrade.EXPERT)
                    )
                },
                modifier = Modifier.widthIn(min = 200.dp),
            ) {
                Text(strings.difficultyExpert)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
