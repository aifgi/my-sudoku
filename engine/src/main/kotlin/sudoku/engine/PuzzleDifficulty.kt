package sudoku.engine

sealed class PuzzleDifficulty {
    data class Technique(val grade: Difficulty) : PuzzleDifficulty()
    data class Given(val grade: GivenGrade) : PuzzleDifficulty()
}
