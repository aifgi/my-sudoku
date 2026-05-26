package sudoku.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import sudoku.app.state.GameState

@Composable
fun SudokuBoard(state: GameState, onCellClick: (index: Int) -> Unit) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val cellSize = size.width / 9f
                    val idx = offsetToIndex(offset, cellSize)
                    if (idx in 0..80) onCellClick(idx)
                }
            }
    ) {
        if (state.isPaused) return@Canvas
        val cellSize = size.width / 9f
        drawCells(state, cellSize)
        drawDigits(state, cellSize)
        drawGrid(cellSize)
        drawBorders(state, cellSize)
    }
}

fun offsetToIndex(offset: Offset, cellSize: Float): Int {
    val row = (offset.y / cellSize).toInt()
    val col = (offset.x / cellSize).toInt()
    return if (row in 0..8 && col in 0..8) row * 9 + col else -1
}

private fun DrawScope.drawCells(state: GameState, cellSize: Float) {
    for (i in 0..80) {
        val row = i / 9
        val col = i % 9
        val x = col * cellSize
        val y = row * cellSize
        val rect = Rect(x, y, x + cellSize, y + cellSize)

        // Layer 1: base background
        val baseColor = if (state.givens[i]) Color(0xFFF5F5F5) else Color.White
        drawRect(baseColor, topLeft = Offset(rect.left, rect.top), size = Size(cellSize, cellSize))

        // Layer 2: number-match overlay
        val digit = state.digits[i]
        if (digit != 0 && digit == state.numberHighlightDigit) {
            drawRect(Color(0xFFFFF3CD), topLeft = Offset(rect.left, rect.top), size = Size(cellSize, cellSize))
        }

        // Layer 3: conflict overlay
        if (i in state.conflictIndices) {
            drawRect(Color(0xFFFFCCCC), topLeft = Offset(rect.left, rect.top), size = Size(cellSize, cellSize))
        }

        // Layer 4: selected overlay
        if (i == state.selectedIndex) {
            drawRect(Color(0xFFC5D8FF), topLeft = Offset(rect.left, rect.top), size = Size(cellSize, cellSize))
        }
    }
}

private fun DrawScope.drawDigits(state: GameState, cellSize: Float) {
    drawIntoCanvas { canvas ->
        val skiaCanvas = canvas.nativeCanvas
        val font = org.jetbrains.skia.Font(null, cellSize * 0.55f)
        val paint = org.jetbrains.skia.Paint()
        for (i in 0..80) {
            val digit = state.digits[i]
            if (digit == 0) continue
            val row = i / 9
            val col = i % 9
            val cx = col * cellSize + cellSize / 2f
            val cy = row * cellSize + cellSize * 0.65f
            paint.color = if (state.givens[i]) 0xFF1A1A2E.toInt() else 0xFF444466.toInt()
            val text = digit.toString()
            val line = org.jetbrains.skia.TextLine.make(text, font)
            skiaCanvas.drawTextLine(line, cx - line.width / 2f, cy, paint)
        }
    }
}

private fun DrawScope.drawGrid(cellSize: Float) {
    val gridColor = Color(0xFFCCCCCC)
    val thickColor = Color(0xFF666666)
    val totalSize = cellSize * 9

    for (i in 0..9) {
        val pos = i * cellSize
        val isBox = i % 3 == 0
        val strokeWidth = if (isBox) 2f else 0.5f
        val color = if (isBox) thickColor else gridColor

        // Horizontal lines
        drawLine(color, Offset(0f, pos), Offset(totalSize, pos), strokeWidth = strokeWidth)
        // Vertical lines
        drawLine(color, Offset(pos, 0f), Offset(pos, totalSize), strokeWidth = strokeWidth)
    }
}

private fun DrawScope.drawBorders(state: GameState, cellSize: Float) {
    // Selected cell: solid blue border
    state.selectedIndex?.let { idx ->
        val row = idx / 9
        val col = idx % 9
        val x = col * cellSize
        val y = row * cellSize
        val inset = 1.5f
        drawRect(
            color = Color(0xFF4A90D9),
            topLeft = Offset(x + inset, y + inset),
            size = Size(cellSize - inset * 2, cellSize - inset * 2),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
        )
    }

    // Conflict cells: dashed red border
    for (idx in state.conflictIndices) {
        val row = idx / 9
        val col = idx % 9
        val x = col * cellSize
        val y = row * cellSize
        val inset = 1f
        drawRect(
            color = Color(0xFFCC3333),
            topLeft = Offset(x + inset, y + inset),
            size = Size(cellSize - inset * 2, cellSize - inset * 2),
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 2f,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 4f))
            )
        )
    }
}
