package sudoku.app.ui.i18n

object RussianStrings : Strings {
    override val appTitle = "Судоку"
    override val difficultyEasy = "Лёгкий"
    override val difficultyMedium = "Средний"
    override val difficultyHard = "Сложный"
    override val difficultyExpert = "Эксперт"

    override val statMistakes = "Ошибки"
    override val statTime = "Время"
    override val actionNewGame = "Новая игра"

    override val hintNoHint = "Подсказка недоступна"
    override val hintNoHintForDifficulty = "Подсказок нет для этого уровня сложности"
    override val hintNakedSingle = "Открытый одиночка"
    override val hintHiddenSingle = "Скрытый одиночка"
    override val hintNakedPair = "Открытая пара"
    override val hintHiddenPair = "Скрытая пара"
    override val hintPointingPair = "Указывающая пара"

    override val pauseTitle = "Игра на паузе"
    override val pauseResume = "Продолжить"

    override val completionTitle = "Головоломка решена!"
    override val completionNewGame = "Новая игра"
    override val completionBackToHome = "На главную"

    override val gameOverTitle = "Игра окончена"
    override val gameOverMistakes = { n: Int -> "Вы допустили $n ошибок. Удачи в следующий раз!" }
    override val gameOverNewGame = "Новая игра"

    override val quitTitle = "Выйти?"
    override val quitMessage = "У вас есть несохранённый прогресс. Вы уверены, что хотите выйти?"
    override val quitConfirm = "Выйти"
    override val quitCancel = "Отмена"

    override val newGameTitle = "Новая игра?"
    override val newGameMessage = "Начать новую игру? Текущий прогресс будет утерян."
    override val newGameConfirm = "Начать"
    override val newGameCancel = "Отмена"
}
