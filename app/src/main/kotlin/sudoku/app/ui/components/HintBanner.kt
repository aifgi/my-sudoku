package sudoku.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import sudoku.app.ui.i18n.LocalStrings
import sudoku.engine.HintExplanationData
import sudoku.engine.HintResult

@Composable
fun HintBanner(hintResult: HintResult?) {
    val strings = LocalStrings.current
    AnimatedVisibility(visible = hintResult != null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            when (val result = hintResult) {
                is HintResult.Found -> {
                    val techniqueName = when (result.technique) {
                        "Naked Single"   -> strings.hintNakedSingle
                        "Hidden Single"  -> strings.hintHiddenSingle
                        "Naked Pair"     -> strings.hintNakedPair
                        "Hidden Pair"    -> strings.hintHiddenPair
                        "Pointing Pair"  -> strings.hintPointingPair
                        else             -> result.technique
                    }
                    val explanationText = when (val data = result.explanationData) {
                        is HintExplanationData.Single ->
                            if (result.technique == "Naked Single") strings.hintExplainNakedSingle(data.cell, data.digit)
                            else strings.hintExplainHiddenSingle(data.cell, data.digit)
                        is HintExplanationData.Pair ->
                            if (result.technique == "Naked Pair") strings.hintExplainNakedPair(data.cell1, data.cell2, data.d1, data.d2)
                            else strings.hintExplainHiddenPair(data.cell1, data.cell2, data.d1, data.d2)
                        is HintExplanationData.PointingPairRow ->
                            strings.hintExplainPointingPairRow(data.digit, data.box, data.row)
                        is HintExplanationData.PointingPairCol ->
                            strings.hintExplainPointingPairCol(data.digit, data.box, data.col)
                        null -> result.explanation
                    }
                    Column {
                        Text(text = techniqueName, fontWeight = FontWeight.Bold)
                        Text(text = explanationText)
                    }
                }
                is HintResult.NoHint -> Text(text = strings.hintNoHint)
                is HintResult.NoHintForDifficulty -> Text(text = strings.hintNoHintForDifficulty)
                null -> {} // AnimatedVisibility handles this
            }
        }
    }
}
