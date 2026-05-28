package sudoku.app.ui.i18n

fun resolveLocale(savedLocale: AppLocale?, systemLanguage: String): AppLocale =
    savedLocale ?: if (systemLanguage.startsWith("ru")) AppLocale.RUSSIAN else AppLocale.ENGLISH
