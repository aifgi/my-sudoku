package sudoku.engine

sealed class HintResult {
    data class Found(
        val technique: String,
        val targetCells: List<Int>,
        val peerCells: List<Int>,
        val explanation: String,
    ) : HintResult()

    object NoHint : HintResult()
    object NoHintForDifficulty : HintResult()
}
