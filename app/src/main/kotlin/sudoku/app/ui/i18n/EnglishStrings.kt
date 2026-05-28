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
