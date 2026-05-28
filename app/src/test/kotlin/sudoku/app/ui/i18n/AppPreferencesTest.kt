package sudoku.app.ui.i18n

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.prefs.Preferences

class AppPreferencesTest {

    private val testNode: Preferences = Preferences.userRoot().node("sudoku/app")
    private val key = "locale"

    @BeforeEach
    fun setUp() {
        testNode.remove(key)
        testNode.flush()
    }

    @AfterEach
    fun tearDown() {
        testNode.remove(key)
        testNode.flush()
    }

    @Test
    fun `saveLocale RUSSIAN then loadLocale returns RUSSIAN`() {
        AppPreferences.saveLocale(AppLocale.RUSSIAN)
        assertEquals(AppLocale.RUSSIAN, AppPreferences.loadLocale())
    }

    @Test
    fun `saveLocale ENGLISH then loadLocale returns ENGLISH`() {
        AppPreferences.saveLocale(AppLocale.ENGLISH)
        assertEquals(AppLocale.ENGLISH, AppPreferences.loadLocale())
    }

    @Test
    fun `no key set returns null`() {
        assertNull(AppPreferences.loadLocale())
    }

    @Test
    fun `invalid value stored returns null without throwing`() {
        testNode.put(key, "INVALID")
        testNode.flush()
        assertNull(AppPreferences.loadLocale())
    }
}
