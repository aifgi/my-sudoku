package sudoku.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import sudoku.app.ui.AppColors
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val buttonShape = RoundedCornerShape(8.dp)
private val digitLabels = Array(10) { it.toString() }
private val disabledTextColor = Color(0xFF999999)

@Composable
fun NumberPad(
    onDigit: (Int) -> Unit,
    enabled: Boolean = true,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        for (row in 0..2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                for (col in 0..2) {
                    val digit = row * 3 + col + 1
                    NumberButton(
                        digit = digit,
                        onClick = { onDigit(digit) },
                        enabled = enabled,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun NumberButton(
    digit: Int,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .clip(buttonShape)
            .clickable(enabled = enabled, onClick = onClick),
        color = if (enabled) AppColors.NumBtnBg else AppColors.NumBtnDis,
        shape = buttonShape,
        elevation = 0.dp,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(4.dp),
        ) {
            Text(
                text = digitLabels[digit],
                color = if (enabled) AppColors.GivenDigit else disabledTextColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
