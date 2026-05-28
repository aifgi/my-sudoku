package sudoku.engine

sealed class HintExplanationData {
    data class Single(val cell: String, val digit: Int) : HintExplanationData()
    data class Pair(val cell1: String, val cell2: String, val d1: Int, val d2: Int) : HintExplanationData()
    data class PointingPairRow(val digit: Int, val box: Int, val row: Int) : HintExplanationData()
    data class PointingPairCol(val digit: Int, val box: Int, val col: Int) : HintExplanationData()
}

sealed class HintResult {
    data class Found(
        val technique: String,
        val targetCells: List<Int>,
        val peerCells: List<Int>,
        val explanation: String,
        val explanationData: HintExplanationData? = null,
    ) : HintResult()

    object NoHint : HintResult()
    object NoHintForDifficulty : HintResult()
}
