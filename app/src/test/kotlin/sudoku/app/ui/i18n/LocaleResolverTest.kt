package sudoku.app.ui.i18n

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LocaleResolverTest {

    @Test
    fun `null saved locale with system language ru resolves to RUSSIAN`() {
        assertEquals(AppLocale.RUSSIAN, resolveLocale(null, "ru"))
    }

    @Test
    fun `null saved locale with system language ru_RU resolves to RUSSIAN via prefix match`() {
        assertEquals(AppLocale.RUSSIAN, resolveLocale(null, "ru_RU"))
    }

    @Test
    fun `null saved locale with system language en resolves to ENGLISH`() {
        assertEquals(AppLocale.ENGLISH, resolveLocale(null, "en"))
    }

    @Test
    fun `null saved locale with system language fr resolves to ENGLISH`() {
        assertEquals(AppLocale.ENGLISH, resolveLocale(null, "fr"))
    }

    @Test
    fun `saved RUSSIAN overrides system language en`() {
        assertEquals(AppLocale.RUSSIAN, resolveLocale(AppLocale.RUSSIAN, "en"))
    }

    @Test
    fun `saved ENGLISH overrides system language ru`() {
        assertEquals(AppLocale.ENGLISH, resolveLocale(AppLocale.ENGLISH, "ru"))
    }

    @Test
    fun `null saved locale with unknown system language de falls back to ENGLISH`() {
        assertEquals(AppLocale.ENGLISH, resolveLocale(null, "de"))
    }
}
