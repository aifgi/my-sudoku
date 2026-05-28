package sudoku.engine

enum class TechniqueCeiling { SINGLES, PAIRS }

enum class GivenGrade(val minGivens: Int, val maxGivens: Int, val techniqueCeiling: TechniqueCeiling) {
    EASY(36, 45, TechniqueCeiling.SINGLES),
    MEDIUM(29, 35, TechniqueCeiling.SINGLES),
    HARD(24, 28, TechniqueCeiling.PAIRS),
    EXPERT(17, 23, TechniqueCeiling.PAIRS)
}
