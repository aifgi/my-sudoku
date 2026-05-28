package sudoku.app.ui.i18n

import java.util.prefs.Preferences

object AppPreferences {
    private const val KEY = "locale"
    private val prefs: Preferences = Preferences.userRoot().node("sudoku/app")

    fun loadLocale(): AppLocale? {
        return try {
            AppLocale.valueOf(prefs.get(KEY, null) ?: return null)
        } catch (e: Exception) {
            null
        }
    }

    fun saveLocale(locale: AppLocale) {
        try {
            prefs.put(KEY, locale.name)
        } catch (e: Exception) {
            System.err.println("AppPreferences: failed to save locale: ${e.message}")
        }
    }
}
