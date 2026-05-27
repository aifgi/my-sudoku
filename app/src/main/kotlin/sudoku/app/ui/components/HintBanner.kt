package sudoku.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import sudoku.engine.HintResult

@Composable
fun HintBanner(hintResult: HintResult?) {
    AnimatedVisibility(visible = hintResult != null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            when (val result = hintResult) {
                is HintResult.Found -> {
                    Text(text = result.technique, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = result.explanation)
                }
                is HintResult.NoHint -> Text(text = "No hint available")
                is HintResult.NoHintForDifficulty -> Text(text = "No hint available for this difficulty level")
                null -> {} // AnimatedVisibility handles this
            }
        }
    }
}
