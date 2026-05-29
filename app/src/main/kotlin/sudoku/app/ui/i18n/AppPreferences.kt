package sudoku.app.ui.i18n

import java.util.prefs.Preferences
import sudoku.app.ui.DifficultyMode

object AppPreferences {
    private const val KEY = "locale"
    private const val KEY_MODE = "difficulty_mode"
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

    fun loadMode(): DifficultyMode {
        return try {
            DifficultyMode.valueOf(prefs.get(KEY_MODE, null) ?: return DifficultyMode.TECHNIQUE)
        } catch (e: Exception) {
            DifficultyMode.TECHNIQUE
        }
    }

    fun saveMode(mode: DifficultyMode) {
        try {
            prefs.put(KEY_MODE, mode.name)
        } catch (e: Exception) {
            System.err.println("AppPreferences: failed to save mode: ${e.message}")
        }
    }
}
