package sudoku.app.ui.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

@Composable
fun TimerDisplay(seconds: Long, isPaused: Boolean) {
    val formatted = formatTime(seconds)
    val suffix = if (isPaused) " (Paused)" else ""
    Text(
        text = "$formatted$suffix",
        fontFamily = FontFamily.Monospace,
    )
}

fun formatTime(seconds: Long): String {
    return if (seconds < 3600) {
        val m = seconds / 60
        val s = seconds % 60
        "%02d:%02d".format(m, s)
    } else {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        "%02d:%02d:%02d".format(h, m, s)
    }
}
