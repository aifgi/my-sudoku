package sudoku.app.ui.i18n

import kotlin.test.Test
import kotlin.test.assertTrue

class StringsCompletenessTest {

    private fun assertNonBlank(s: String, name: String) {
        assertTrue(s.isNotBlank(), "Expected '$name' to be non-blank")
    }

    @Test
    fun `EnglishStrings has non-blank values for all 28 String properties`() {
        val e = EnglishStrings
        assertNonBlank(e.appTitle, "appTitle")
        assertNonBlank(e.difficultyEasy, "difficultyEasy")
        assertNonBlank(e.difficultyMedium, "difficultyMedium")
        assertNonBlank(e.difficultyHard, "difficultyHard")
        assertNonBlank(e.difficultyExpert, "difficultyExpert")
        assertNonBlank(e.statMistakes, "statMistakes")
        assertNonBlank(e.statTime, "statTime")
        assertNonBlank(e.actionNewGame, "actionNewGame")
        assertNonBlank(e.hintNoHint, "hintNoHint")
        assertNonBlank(e.hintNoHintForDifficulty, "hintNoHintForDifficulty")
        assertNonBlank(e.hintNakedSingle, "hintNakedSingle")
        assertNonBlank(e.hintHiddenSingle, "hintHiddenSingle")
        assertNonBlank(e.hintNakedPair, "hintNakedPair")
        assertNonBlank(e.hintHiddenPair, "hintHiddenPair")
        assertNonBlank(e.hintPointingPair, "hintPointingPair")
        assertNonBlank(e.pauseTitle, "pauseTitle")
        assertNonBlank(e.pauseResume, "pauseResume")
        assertNonBlank(e.completionTitle, "completionTitle")
        assertNonBlank(e.completionNewGame, "completionNewGame")
        assertNonBlank(e.completionBackToHome, "completionBackToHome")
        assertNonBlank(e.gameOverTitle, "gameOverTitle")
        assertNonBlank(e.gameOverNewGame, "gameOverNewGame")
        assertNonBlank(e.quitTitle, "quitTitle")
        assertNonBlank(e.quitMessage, "quitMessage")
        assertNonBlank(e.quitConfirm, "quitConfirm")
        assertNonBlank(e.quitCancel, "quitCancel")
        assertNonBlank(e.newGameTitle, "newGameTitle")
        assertNonBlank(e.newGameMessage, "newGameMessage")
        assertNonBlank(e.newGameConfirm, "newGameConfirm")
        assertNonBlank(e.newGameCancel, "newGameCancel")
    }

    @Test
    fun `RussianStrings has non-blank values for all 28 String properties`() {
        val r = RussianStrings
        assertNonBlank(r.appTitle, "appTitle")
        assertNonBlank(r.difficultyEasy, "difficultyEasy")
        assertNonBlank(r.difficultyMedium, "difficultyMedium")
        assertNonBlank(r.difficultyHard, "difficultyHard")
        assertNonBlank(r.difficultyExpert, "difficultyExpert")
        assertNonBlank(r.statMistakes, "statMistakes")
        assertNonBlank(r.statTime, "statTime")
        assertNonBlank(r.actionNewGame, "actionNewGame")
        assertNonBlank(r.hintNoHint, "hintNoHint")
        assertNonBlank(r.hintNoHintForDifficulty, "hintNoHintForDifficulty")
        assertNonBlank(r.hintNakedSingle, "hintNakedSingle")
        assertNonBlank(r.hintHiddenSingle, "hintHiddenSingle")
        assertNonBlank(r.hintNakedPair, "hintNakedPair")
        assertNonBlank(r.hintHiddenPair, "hintHiddenPair")
        assertNonBlank(r.hintPointingPair, "hintPointingPair")
        assertNonBlank(r.pauseTitle, "pauseTitle")
        assertNonBlank(r.pauseResume, "pauseResume")
        assertNonBlank(r.completionTitle, "completionTitle")
        assertNonBlank(r.completionNewGame, "completionNewGame")
        assertNonBlank(r.completionBackToHome, "completionBackToHome")
        assertNonBlank(r.gameOverTitle, "gameOverTitle")
        assertNonBlank(r.gameOverNewGame, "gameOverNewGame")
        assertNonBlank(r.quitTitle, "quitTitle")
        assertNonBlank(r.quitMessage, "quitMessage")
        assertNonBlank(r.quitConfirm, "quitConfirm")
        assertNonBlank(r.quitCancel, "quitCancel")
        assertNonBlank(r.newGameTitle, "newGameTitle")
        assertNonBlank(r.newGameMessage, "newGameMessage")
        assertNonBlank(r.newGameConfirm, "newGameConfirm")
        assertNonBlank(r.newGameCancel, "newGameCancel")
    }

    @Test
    fun `EnglishStrings gameOverMistakes interpolates the count`() {
        val result = EnglishStrings.gameOverMistakes(3)
        assertTrue(result.contains("3"), "Expected gameOverMistakes(3) to contain '3', got: '$result'")
    }

    @Test
    fun `RussianStrings gameOverMistakes interpolates the count`() {
        val result = RussianStrings.gameOverMistakes(3)
        assertTrue(result.contains("3"), "Expected gameOverMistakes(3) to contain '3', got: '$result'")
    }

    @Test
    fun `RussianStrings gameOverMistakes interpolates zero`() {
        val result = RussianStrings.gameOverMistakes(0)
        assertTrue(result.contains("0"), "Expected gameOverMistakes(0) to contain '0', got: '$result'")
    }
}
