package sudoku.app.ui.i18n

enum class AppLocale { ENGLISH, RUSSIAN }

fun AppLocale.toStrings(): Strings = when (this) {
    AppLocale.ENGLISH -> EnglishStrings
    AppLocale.RUSSIAN -> RussianStrings
}
