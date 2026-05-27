package sudoku.app.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NumberPad(
    onDigit: (Int) -> Unit,
    onErase: () -> Unit,
    enabled: Boolean = true,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        for (digit in 1..9) {
            Button(
                onClick = { onDigit(digit) },
                enabled = enabled,
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
            ) {
                Text(digit.toString())
            }
        }
        Button(
            onClick = onErase,
            enabled = enabled,
            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
        ) {
            Text("⌫")
        }
    }
}
