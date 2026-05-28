package sudoku.app.ui.i18n

import androidx.compose.runtime.compositionLocalOf

interface Strings {
    // App / home
    val appTitle: String
    val difficultyEasy: String
    val difficultyMedium: String
    val difficultyHard: String
    val difficultyExpert: String

    // Game screen labels
    val statMistakes: String
    val statTime: String
    val actionNewGame: String

    // Hint banner
    val hintNoHint: String
    val hintNoHintForDifficulty: String
    val hintNakedSingle: String
    val hintHiddenSingle: String
    val hintNakedPair: String
    val hintHiddenPair: String
    val hintPointingPair: String
    val hintExplainNakedSingle: (cell: String, digit: Int) -> String
    val hintExplainHiddenSingle: (cell: String, digit: Int) -> String
    val hintExplainNakedPair: (cell1: String, cell2: String, d1: Int, d2: Int) -> String
    val hintExplainHiddenPair: (cell1: String, cell2: String, d1: Int, d2: Int) -> String
    val hintExplainPointingPairRow: (digit: Int, box: Int, row: Int) -> String
    val hintExplainPointingPairCol: (digit: Int, box: Int, col: Int) -> String

    // Pause overlay
    val pauseTitle: String
    val pauseResume: String

    // Completion overlay
    val completionTitle: String
    val completionNewGame: String
    val completionBackToHome: String

    // Game over dialog
    val gameOverTitle: String
    val gameOverMistakes: (Int) -> String
    val gameOverNewGame: String

    // Quit confirmation dialog
    val quitTitle: String
    val quitMessage: String
    val quitConfirm: String
    val quitCancel: String

    // New game confirmation dialog
    val newGameTitle: String
    val newGameMessage: String
    val newGameConfirm: String
    val newGameCancel: String
}

val LocalStrings = compositionLocalOf<Strings> { EnglishStrings }
