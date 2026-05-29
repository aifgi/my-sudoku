package sudoku.app.ui.i18n

object EnglishStrings : Strings {
    override val appTitle = "Sudoku"
    override val difficultyEasy = "Easy"
    override val difficultyMedium = "Medium"
    override val difficultyHard = "Hard"
    override val difficultyExpert = "Expert"

    override val statMistakes = "Mistakes"
    override val statTime = "Time"
    override val actionNewGame = "New Game"

    override val hintNoHint = "No hint available"
    override val hintNoHintForDifficulty = "No hint available for this difficulty level"
    override val hintNakedSingle = "Naked Single"
    override val hintHiddenSingle = "Hidden Single"
    override val hintNakedPair = "Naked Pair"
    override val hintHiddenPair = "Hidden Pair"
    override val hintPointingPair = "Pointing Pair"
    override val hintExplainNakedSingle: (String, Int) -> String =
        { cell, digit -> "Naked Single at $cell: only $digit fits" }
    override val hintExplainHiddenSingle: (String, Int) -> String =
        { cell, digit -> "Hidden Single at $cell: digit $digit can only go here in this unit" }
    override val hintExplainNakedPair: (String, String, Int, Int) -> String =
        { cell1, cell2, d1, d2 -> "Naked Pair at $cell1 and $cell2: digits $d1 and $d2 are confined here" }
    override val hintExplainHiddenPair: (String, String, Int, Int) -> String =
        { cell1, cell2, d1, d2 -> "Hidden Pair at $cell1 and $cell2: digits $d1 and $d2 are confined to these cells" }
    override val hintExplainPointingPairRow: (Int, Int, Int) -> String =
        { digit, box, row -> "Pointing Pair: digit $digit in box $box is confined to row $row, eliminating it from other row cells" }
    override val hintExplainPointingPairCol: (Int, Int, Int) -> String =
        { digit, box, col -> "Pointing Pair: digit $digit in box $box is confined to column $col, eliminating it from other column cells" }
    override val hintXWing = "X-Wing"
    override val hintNakedTriple = "Naked Triple"
    override val hintHiddenTriple = "Hidden Triple"
    override val hintSwordfish = "Swordfish"
    override val hintExplainNakedTriple: (String, String, String, Int, Int, Int) -> String =
        { c1, c2, c3, d1, d2, d3 -> "Naked Triple at $c1, $c2, $c3: digits $d1, $d2, $d3 confined here" }
    override val hintExplainHiddenTriple: (String, String, String, Int, Int, Int) -> String =
        { c1, c2, c3, d1, d2, d3 -> "Hidden Triple at $c1, $c2, $c3: digits $d1, $d2, $d3 confined to these cells" }
    override val hintExplainSwordfish: (Int) -> String =
        { digit -> "Swordfish: digit $digit eliminated from rows" }
    override val modeTechnique = "Technique"
    override val modeGiven = "Given Count"

    override val pauseTitle = "Game Paused"
    override val pauseResume = "Resume"

    override val completionTitle = "Puzzle Solved!"
    override val completionNewGame = "New Game"
    override val completionBackToHome = "Back to Home"

    override val gameOverTitle = "Game Over"
    override val gameOverMistakes = { n: Int -> "You made $n mistakes. Better luck next time!" }
    override val gameOverNewGame = "New Game"

    override val quitTitle = "Quit?"
    override val quitMessage = "You have unsaved progress. Are you sure you want to quit?"
    override val quitConfirm = "Quit"
    override val quitCancel = "Cancel"

    override val newGameTitle = "New Game?"
    override val newGameMessage = "Start a new game? Your current progress will be lost."
    override val newGameConfirm = "Start"
    override val newGameCancel = "Cancel"
}
