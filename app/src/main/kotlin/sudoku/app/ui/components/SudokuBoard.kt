package sudoku.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import sudoku.app.state.GameState

// Cached draw styles — allocated once, shared across all recompositions
private val selectedStroke = Stroke(width = 3f)
private val conflictStroke = Stroke(
    width = 2f,
    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f))
)

/** Caches Skia Font and pre-rendered TextLines for digits 1–9, invalidated on cellSize change. */
private class DigitCache {
    private var cachedCellSize = -1f
    private var font: org.jetbrains.skia.Font? = null
    private val lines = arrayOfNulls<org.jetbrains.skia.TextLine>(10)

    fun getLine(digit: Int, cellSize: Float): org.jetbrains.skia.TextLine {
        if (cellSize != cachedCellSize) rebuild(cellSize)
        return lines[digit]!!
    }

    fun getFont(cellSize: Float): org.jetbrains.skia.Font {
        if (cellSize != cachedCellSize) rebuild(cellSize)
        return font!!
    }

    private fun rebuild(cellSize: Float) {
        cachedCellSize = cellSize
        font = org.jetbrains.skia.Font(null, cellSize * 0.55f)
        for (d in 1..9) lines[d] = org.jetbrains.skia.TextLine.make(d.toString(), font!!)
    }
}

@Composable
fun SudokuBoard(state: GameState, onCellClick: (index: Int) -> Unit) {
    val digitCache = remember { DigitCache() }

    val modifier = remember {
        Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    }

    Canvas(
        modifier = modifier.pointerInput(Unit) {
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
        drawDigits(state, cellSize, digitCache)
        drawGrid(cellSize)
        drawBorders(state, cellSize)
    }
}

fun offsetToIndex(offset: Offset, cellSize: Float): Int {
    val row = (offset.y / cellSize).toInt()
    val col = (offset.x / cellSize).toInt()
    return if (row in 0..8 && col in 0..8) row * 9 + col else -1
}

private fun isRowComplete(digits: IntArray, conflictIndices: Set<Int>, row: Int): Boolean {
    for (col in 0..8) {
        val i = row * 9 + col
        if (digits[i] == 0 || i in conflictIndices) return false
    }
    return true
}

private fun isColComplete(digits: IntArray, conflictIndices: Set<Int>, col: Int): Boolean {
    for (row in 0..8) {
        val i = row * 9 + col
        if (digits[i] == 0 || i in conflictIndices) return false
    }
    return true
}

private fun DrawScope.drawCells(state: GameState, cellSize: Float) {
    val selRow = state.selectedIndex?.div(9)
    val selCol = state.selectedIndex?.rem(9)

    val completedSelRow = selRow != null && isRowComplete(state.digits, state.conflictIndices, selRow)
    val completedSelCol = selCol != null && isColComplete(state.digits, state.conflictIndices, selCol)

    for (i in 0..80) {
        val row = i / 9
        val col = i % 9
        val x = col * cellSize
        val y = row * cellSize
        val sz = Size(cellSize, cellSize)
        val tl = Offset(x, y)

        // Layer 1: base background
        drawRect(if (state.givens[i]) Color(0xFFF5F5F5) else Color.White, topLeft = tl, size = sz)

        // Layer 2: row/col highlight
        if (state.selectedIndex != null && i != state.selectedIndex) {
            val inSelRow = row == selRow
            val inSelCol = col == selCol
            if (inSelRow || inSelCol) {
                val complete = (inSelRow && completedSelRow) || (inSelCol && completedSelCol)
                drawRect(if (complete) Color(0xFFDFF0DA) else Color(0xFFE8EFFF), topLeft = tl, size = sz)
            }
        }

        // Layer 3: number-match overlay
        val digit = state.digits[i]
        if (digit != 0 && digit == state.numberHighlightDigit) {
            drawRect(Color(0xFFFFF3CD), topLeft = tl, size = sz)
        }

        // Layer 4: selected overlay
        if (i == state.selectedIndex) {
            drawRect(Color(0xFFC5D8FF), topLeft = tl, size = sz)
        }

        // Layer 5: conflict overlay (drawn on top so it remains visible when selected)
        if (i in state.conflictIndices) {
            drawRect(Color(0xFFFFCCCC), topLeft = tl, size = sz)
        }
    }
}

private fun DrawScope.drawDigits(state: GameState, cellSize: Float, cache: DigitCache) {
    drawIntoCanvas { canvas ->
        val skiaCanvas = canvas.nativeCanvas
        val paint = org.jetbrains.skia.Paint()
        for (i in 0..80) {
            val digit = state.digits[i]
            if (digit == 0) continue
            val row = i / 9
            val col = i % 9
            val cx = col * cellSize + cellSize / 2f
            val cy = row * cellSize + cellSize * 0.65f
            paint.color = if (state.givens[i]) 0xFF1A1A2E.toInt() else 0xFF444466.toInt()
            val line = cache.getLine(digit, cellSize)
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

        drawLine(color, Offset(0f, pos), Offset(totalSize, pos), strokeWidth = strokeWidth)
        drawLine(color, Offset(pos, 0f), Offset(pos, totalSize), strokeWidth = strokeWidth)
    }
}

private fun DrawScope.drawBorders(state: GameState, cellSize: Float) {
    // Selected cell: solid blue border
    state.selectedIndex?.let { idx ->
        val row = idx / 9
        val col = idx % 9
        val inset = 1.5f
        drawRect(
            color = Color(0xFF4A90D9),
            topLeft = Offset(col * cellSize + inset, row * cellSize + inset),
            size = Size(cellSize - inset * 2, cellSize - inset * 2),
            style = selectedStroke
        )
    }

    // Conflict cells: dashed red border
    for (idx in state.conflictIndices) {
        val row = idx / 9
        val col = idx % 9
        val inset = 1f
        drawRect(
            color = Color(0xFFCC3333),
            topLeft = Offset(col * cellSize + inset, row * cellSize + inset),
            size = Size(cellSize - inset * 2, cellSize - inset * 2),
            style = conflictStroke
        )
    }
}