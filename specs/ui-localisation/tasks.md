# Tasks: UI Localisation (English + Russian)

## Phase 1: Make It Work (POC)

Focus: Wire the i18n skeleton end-to-end so the app compiles with `LocalStrings` in place.
Accept any shortcuts (stub Russian strings, simple layout for toggle).

---

- [x] 1.1 Create i18n package: `AppLocale` enum + `Strings` interface + `LocalStrings`
  - **Do**:
    1. Create `app/src/main/kotlin/sudoku/app/ui/i18n/AppLocale.kt` — `enum class AppLocale { ENGLISH, RUSSIAN }`
    2. Create `app/src/main/kotlin/sudoku/app/ui/i18n/Strings.kt` — declare `interface Strings` with all 24 members (5 app/home, 3 game labels, 2 hint, 2 pause, 3 completion, 3 game-over, 4 quit dialog, 4 new-game dialog; `gameOverMistakes: (Int) -> String`), then `val LocalStrings = compositionLocalOf<Strings> { EnglishStrings }`
    3. Create `app/src/main/kotlin/sudoku/app/ui/i18n/EnglishStrings.kt` — `object EnglishStrings : Strings` with all 24 English values exactly as listed in AC-5.1; `gameOverMistakes = { n -> "You made $n mistakes. Better luck next time!" }`
  - **Files**:
    - `app/src/main/kotlin/sudoku/app/ui/i18n/AppLocale.kt` (create)
    - `app/src/main/kotlin/sudoku/app/ui/i18n/Strings.kt` (create)
    - `app/src/main/kotlin/sudoku/app/ui/i18n/EnglishStrings.kt` (create)
  - **Done when**: All three files compile; `Strings` interface has exactly 24 members; `EnglishStrings` satisfies the compiler with no `override` missing
  - **Verify**: `./gradlew :app:compileKotlin 2>&1 | grep -E "error:|BUILD" | tail -5`
  - **Commit**: `feat(i18n): add AppLocale enum, Strings interface, EnglishStrings`
  - _Requirements: FR-1, FR-2, FR-10, AC-5.1, AC-5.2, AC-6.2_

---

- [x] 1.2 Create `RussianStrings` and `AppPreferences`
  - **Do**:
    1. Create `app/src/main/kotlin/sudoku/app/ui/i18n/RussianStrings.kt` — `object RussianStrings : Strings` with all 24 Cyrillic values. Use the following translations:
       - `appTitle = "Судоку"`, `difficultyEasy = "Лёгкий"`, `difficultyMedium = "Средний"`, `difficultyHard = "Сложный"`, `difficultyExpert = "Эксперт"`
       - `statMistakes = "Ошибки"`, `statTime = "Время"`, `actionNewGame = "Новая игра"`
       - `hintNoHint = "Подсказка недоступна"`, `hintNoHintForDifficulty = "Подсказок нет для этого уровня сложности"`
       - `pauseTitle = "Игра на паузе"`, `pauseResume = "Продолжить"`
       - `completionTitle = "Головоломка решена!"`, `completionNewGame = "Новая игра"`, `completionBackToHome = "На главную"`
       - `gameOverTitle = "Игра окончена"`, `gameOverMistakes = { n -> "Вы допустили $n ошибок. Удачи в следующий раз!" }`, `gameOverNewGame = "Новая игра"`
       - `quitTitle = "Выйти?"`, `quitMessage = "У вас есть несохранённый прогресс. Вы уверены, что хотите выйти?"`, `quitConfirm = "Выйти"`, `quitCancel = "Отмена"`
       - `newGameTitle = "Новая игра?"`, `newGameMessage = "Начать новую игру? Текущий прогресс будет утерян."`, `newGameConfirm = "Начать"`, `newGameCancel = "Отмена"`
    2. Create `app/src/main/kotlin/sudoku/app/ui/i18n/AppPreferences.kt` — `object AppPreferences` wrapping `java.util.prefs.Preferences.userRoot().node("sudoku/app")`; `fun loadLocale(): AppLocale?` uses `try/catch`, returns `AppLocale.valueOf(prefs.get(KEY, null) ?: return null)` catching all exceptions and returning `null`; `fun saveLocale(locale: AppLocale)` calls `prefs.put(KEY, locale.name)` in a `try/catch` that logs to stderr on failure
  - **Files**:
    - `app/src/main/kotlin/sudoku/app/ui/i18n/RussianStrings.kt` (create)
    - `app/src/main/kotlin/sudoku/app/ui/i18n/AppPreferences.kt` (create)
  - **Done when**: Both files compile; `RussianStrings` satisfies the `Strings` interface; `AppPreferences.loadLocale()` returns nullable `AppLocale`
  - **Verify**: `./gradlew :app:compileKotlin 2>&1 | grep -E "error:|BUILD" | tail -5`
  - **Commit**: `feat(i18n): add RussianStrings and AppPreferences`
  - _Requirements: FR-3, FR-7, AC-5.1, AC-4.1, AC-4.2, AC-4.3_

---

- [ ] 1.3 [VERIFY] Quality checkpoint
  - **Do**: Full build to catch any type/import errors across new files
  - **Verify**: `./gradlew build 2>&1 | tail -5`
  - **Done when**: BUILD SUCCESSFUL
  - **Commit**: `chore(i18n): pass quality checkpoint after i18n package creation` (if fixes needed)

---

- [x] 1.4 Wire `App.kt`: locale state + `CompositionLocalProvider` + quit dialog strings
  - **Do**:
    1. Add imports: `sudoku.app.ui.i18n.*`, `androidx.compose.runtime.mutableStateOf`, `androidx.compose.runtime.remember`, `androidx.compose.runtime.setValue`, `androidx.compose.runtime.getValue`, `androidx.compose.runtime.CompositionLocalProvider`, `java.util.Locale`
    2. Add top-level private function `resolveInitialLocale(): AppLocale` (above `App` composable): calls `AppPreferences.loadLocale()?.let { return it }`, then `return if (Locale.getDefault().language.startsWith("ru")) AppLocale.RUSSIAN else AppLocale.ENGLISH`
    3. Add extension `fun AppLocale.toStrings(): Strings = when(this) { AppLocale.ENGLISH -> EnglishStrings; AppLocale.RUSSIAN -> RussianStrings }`
    4. Inside `App`, declare `var locale by remember { mutableStateOf(resolveInitialLocale()) }` as the FIRST line before `val state by viewModel.state.collectAsState()`
    5. Wrap the entire body (the `when` block + the quit `AlertDialog`) in `CompositionLocalProvider(LocalStrings provides locale.toStrings()) { ... }`
    6. Inside the quit `AlertDialog`, replace hardcoded strings with `val strings = LocalStrings.current` then `strings.quitTitle`, `strings.quitMessage`, `strings.quitConfirm`, `strings.quitCancel`
  - **Files**: `app/src/main/kotlin/sudoku/app/ui/App.kt`
  - **Done when**: `App.kt` compiles; `locale` state declared above `state`; `CompositionLocalProvider` wraps all content; quit dialog uses `LocalStrings.current`; `HomeScreen` call site is NOT yet updated (done in task 1.5)
  - **Verify**: `./gradlew :app:compileKotlin 2>&1 | grep -E "error:|BUILD" | tail -5`
  - **Commit**: `feat(i18n): wire locale state and CompositionLocalProvider in App`
  - _Requirements: FR-4, FR-5, FR-6, FR-9, FR-12, AC-1.3, AC-3.3_

---

- [x] 1.5 Update `HomeScreen.kt`: add locale params, flag toggle, use `LocalStrings.current`; wire call site in `App.kt`
  - **Do**:
    1. Add imports: `sudoku.app.ui.i18n.AppLocale`, `sudoku.app.ui.i18n.LocalStrings`, `androidx.compose.ui.draw.alpha`, `androidx.compose.material.IconButton`
    2. Change signature to `fun HomeScreen(onDifficultySelected: (Difficulty) -> Unit, currentLocale: AppLocale, onLocaleChange: (AppLocale) -> Unit)`
    3. At the top of the composable body: `val strings = LocalStrings.current`
    4. Replace `"Sudoku"` with `strings.appTitle`, `"Easy"` with `strings.difficultyEasy`, `"Medium"` with `strings.difficultyMedium`, `"Hard"` with `strings.difficultyHard`, `"Expert"` with `strings.difficultyExpert`
    5. Add flag toggle row above (or below) difficulty buttons: two `TextButton` composables showing `"🇬🇧"` and `"🇷🇺"`. Active flag renders at `alpha = 1f`; inactive flag at `alpha = 0.4f`. Tapping inactive flag calls `onLocaleChange(AppLocale.ENGLISH)` or `onLocaleChange(AppLocale.RUSSIAN)` respectively
    6. In `App.kt`, update the `HomeScreen(...)` call to pass `currentLocale = locale, onLocaleChange = { newLocale -> locale = newLocale; AppPreferences.saveLocale(newLocale) }`
  - **Files**:
    - `app/src/main/kotlin/sudoku/app/ui/HomeScreen.kt`
    - `app/src/main/kotlin/sudoku/app/ui/App.kt` (call site only)
  - **Done when**: `HomeScreen` compiles with new signature; flag toggle renders; `App.kt` call site compiles
  - **Verify**: `./gradlew :app:compileKotlin 2>&1 | grep -E "error:|BUILD" | tail -5`
  - **Commit**: `feat(i18n): add locale toggle and LocalStrings to HomeScreen`
  - _Requirements: FR-8, FR-9, FR-11, AC-2.1, AC-2.2, AC-2.3, AC-2.4, AC-5.3_

---

- [ ] 1.6 [VERIFY] Quality checkpoint
  - **Do**: Full build after `App.kt` + `HomeScreen.kt` are wired together
  - **Verify**: `./gradlew build 2>&1 | tail -5`
  - **Done when**: BUILD SUCCESSFUL; locale state flows from `App` into `HomeScreen` with no compile errors
  - **Commit**: `chore(i18n): pass quality checkpoint after App/HomeScreen wiring` (if fixes needed)

---

- [ ] 1.7 [P] Replace hardcoded strings in `PauseOverlay.kt` and `CompletionOverlay.kt`
  - **Do** (PauseOverlay):
    1. Add `import sudoku.app.ui.i18n.LocalStrings`
    2. Inside `PauseOverlay`, add `val strings = LocalStrings.current`
    3. Replace `"Game Paused"` with `strings.pauseTitle`, `"Resume"` with `strings.pauseResume`
  - **Do** (CompletionOverlay):
    1. Add `import sudoku.app.ui.i18n.LocalStrings`
    2. Inside `CompletionOverlay`, add `val strings = LocalStrings.current`
    3. Replace `"Puzzle Solved!"` with `strings.completionTitle`, `"New Game"` with `strings.completionNewGame`, `"Back to Home"` with `strings.completionBackToHome`
  - **Files**:
    - `app/src/main/kotlin/sudoku/app/ui/components/PauseOverlay.kt`
    - `app/src/main/kotlin/sudoku/app/ui/components/CompletionOverlay.kt`
  - **Done when**: Both files compile with no hardcoded English strings for the covered keys
  - **Verify**: `./gradlew :app:compileKotlin 2>&1 | grep -E "error:|BUILD" | tail -5`
  - **Commit**: `feat(i18n): replace hardcoded strings in PauseOverlay and CompletionOverlay`
  - _Requirements: FR-9, FR-11, AC-5.3_

---

- [ ] 1.8 [P] Replace hardcoded strings in `HintBanner.kt` and `GameOverDialog.kt`
  - **Do** (HintBanner):
    1. Add `import sudoku.app.ui.i18n.LocalStrings`
    2. Inside `HintBanner`, add `val strings = LocalStrings.current`
    3. Replace `"No hint available"` with `strings.hintNoHint`, `"No hint available for this difficulty level"` with `strings.hintNoHintForDifficulty`
  - **Do** (GameOverDialog):
    1. Add `import sudoku.app.ui.i18n.LocalStrings`
    2. Change signature to `fun GameOverDialog(mistakeCount: Int, onNewGame: () -> Unit)`
    3. Inside composable, add `val strings = LocalStrings.current`
    4. Replace `"Game Over"` with `strings.gameOverTitle`, `"You made 3 mistakes. Better luck next time!"` with `strings.gameOverMistakes(mistakeCount)`, `"New Game"` with `strings.gameOverNewGame`
    5. Update the call site in `GameScreen.kt`: `GameOverDialog(mistakeCount = state.mistakeCount, onNewGame = { ... })`
  - **Files**:
    - `app/src/main/kotlin/sudoku/app/ui/components/HintBanner.kt`
    - `app/src/main/kotlin/sudoku/app/ui/components/GameOverDialog.kt`
    - `app/src/main/kotlin/sudoku/app/ui/GameScreen.kt` (call site only)
  - **Done when**: All three files compile; `GameOverDialog` takes `mistakeCount: Int`; no hardcoded English strings remain in covered keys
  - **Verify**: `./gradlew :app:compileKotlin 2>&1 | grep -E "error:|BUILD" | tail -5`
  - **Commit**: `feat(i18n): replace hardcoded strings in HintBanner and GameOverDialog`
  - _Requirements: FR-1, FR-9, FR-11, AC-5.2, AC-5.3_

---

- [ ] 1.9 Replace hardcoded stat labels and action button in `GameScreen.kt`
  - **Do**:
    1. Add `import sudoku.app.ui.i18n.LocalStrings`
    2. At top of `GameScreen` composable body, add `val strings = LocalStrings.current`
    3. Replace `label = "Mistakes"` with `label = strings.statMistakes`, `label = "Time"` with `label = strings.statTime`
    4. Replace `"New Game"` button text with `strings.actionNewGame`
  - **Files**: `app/src/main/kotlin/sudoku/app/ui/GameScreen.kt`
  - **Done when**: `GameScreen.kt` compiles; stat labels and action button use `LocalStrings.current`
  - **Verify**: `./gradlew :app:compileKotlin 2>&1 | grep -E "error:|BUILD" | tail -5`
  - **Commit**: `feat(i18n): replace hardcoded stat labels and action button in GameScreen`
  - _Requirements: FR-9, AC-5.3_

---

- [ ] 1.10 Replace hardcoded new-game dialog strings in `GameScreen.kt`
  - **Do**:
    1. Replace `AlertDialog` title `"New Game?"` with `strings.newGameTitle`, text `"Start a new game?..."` with `strings.newGameMessage`, confirm `"Start"` with `strings.newGameConfirm`, dismiss `"Cancel"` with `strings.newGameCancel`
  - **Files**: `app/src/main/kotlin/sudoku/app/ui/GameScreen.kt`
  - **Done when**: `GameScreen.kt` compiles; all 4 new-game dialog strings use `LocalStrings.current`; no remaining hardcoded UI strings in the file
  - **Verify**: `./gradlew :app:compileKotlin 2>&1 | grep -E "error:|BUILD" | tail -5`
  - **Commit**: `feat(i18n): replace hardcoded new-game dialog strings in GameScreen`
  - _Requirements: FR-9, AC-5.3_

---

- [ ] 1.11 Polish HomeScreen flag toggle layout
  - **Do**:
    1. Wrap the two flag `TextButton` composables in a `Row` with `horizontalArrangement = Arrangement.Center` and add a visible separator or spacing (e.g., `Spacer(Modifier.width(8.dp))`) between flags
    2. Ensure active flag uses `fontWeight = FontWeight.Bold` or a border/background highlight in addition to `alpha` to satisfy AC-2.3 more visibly
    3. Confirm no flag toggle element is present in `GameScreen`, `PauseOverlay`, `CompletionOverlay`, or `GameOverDialog` (grep check)
  - **Files**: `app/src/main/kotlin/sudoku/app/ui/HomeScreen.kt`
  - **Done when**: Flag toggle is visually distinct for active/inactive; build passes; grep finds no toggle UI in game-screen files
  - **Verify**: `./gradlew build 2>&1 | tail -5 && grep -r "onLocaleChange\|flag\|🇬🇧\|🇷🇺" app/src/main/kotlin/sudoku/app/ui/components/ app/src/main/kotlin/sudoku/app/ui/GameScreen.kt | grep -v ".class" || echo "CLEAN"`
  - **Commit**: `refactor(i18n): improve flag toggle visual design on HomeScreen`
  - _Requirements: FR-8, FR-11, AC-2.3, AC-2.4_

---

- [ ] 1.12 POC Checkpoint — full build
  - **Do**: Run full build; verify the entire app compiles with i18n wired end-to-end
  - **Verify**: `./gradlew build 2>&1 | tail -10`
  - **Done when**: BUILD SUCCESSFUL; no compile errors; all 7 composable files use `LocalStrings.current`
  - **Commit**: `feat(i18n): complete POC - all screens use LocalStrings`
  - _Requirements: FR-4, FR-9, AC-5.3_

---

## Phase 2: Refactor

Focus: Co-locate locale logic, extract pure `resolveLocale` function for testability, and improve error logging.

---

- [ ] 2.1 Refactor `AppPreferences` error handling + extract `resolveLocale` pure function + co-locate locale logic
  - **Do**:
    1. In `app/src/main/kotlin/sudoku/app/ui/i18n/LocaleResolver.kt` (create new file), add a pure top-level function `fun resolveLocale(savedLocale: AppLocale?, systemLanguage: String): AppLocale` — returns `savedLocale` if non-null, else `if (systemLanguage.startsWith("ru")) AppLocale.RUSSIAN else AppLocale.ENGLISH`
    2. Update `resolveInitialLocale()` in `App.kt` to delegate: `return resolveLocale(AppPreferences.loadLocale(), Locale.getDefault().language)`
    3. In `AppPreferences.saveLocale`, ensure the `catch` block prints a meaningful stderr message: `System.err.println("AppPreferences: failed to save locale: ${e.message}")`
    4. Move `AppLocale.toStrings()` extension into `AppLocale.kt` alongside the enum to keep locale logic co-located; remove it from `App.kt`
    5. Update `App.kt` imports accordingly
  - **Files**:
    - `app/src/main/kotlin/sudoku/app/ui/i18n/LocaleResolver.kt` (create)
    - `app/src/main/kotlin/sudoku/app/ui/i18n/AppLocale.kt` (add `toStrings()` extension)
    - `app/src/main/kotlin/sudoku/app/ui/i18n/AppPreferences.kt` (improve error logging)
    - `app/src/main/kotlin/sudoku/app/ui/App.kt` (update imports, remove inline extension, delegate to `resolveLocale`)
  - **Done when**: `resolveLocale` is a pure function in `LocaleResolver.kt`; `resolveInitialLocale` delegates to it; `toStrings` lives in `AppLocale.kt`; `App.kt` has no logic that belongs in i18n package; build passes
  - **Verify**: `./gradlew build 2>&1 | tail -5`
  - **Commit**: `refactor(i18n): extract resolveLocale pure function and co-locate locale logic in i18n package`
  - _Requirements: FR-6, FR-7, AC-4.3_

---

- [ ] 2.2 [VERIFY] Quality checkpoint
  - **Do**: Full build after all refactoring
  - **Verify**: `./gradlew build 2>&1 | tail -5`
  - **Done when**: BUILD SUCCESSFUL
  - **Commit**: `chore(i18n): pass quality checkpoint after Phase 2 refactor` (if fixes needed)

---

## Phase 3: Testing

Focus: Unit tests for `LocaleResolver`, `AppPreferences`, and `StringsCompleteness`.

---

- [ ] 3.1 Create `LocaleResolverTest` — unit tests for startup locale resolution
  - **Do**:
    1. Create directory `app/src/test/kotlin/sudoku/app/ui/i18n/` if it doesn't exist
    2. Create `app/src/test/kotlin/sudoku/app/ui/i18n/LocaleResolverTest.kt` with a `LocaleResolverTest` class using JUnit 4 (check existing test deps) or JUnit 5
    3. Test the pure `resolveLocale(savedLocale: AppLocale?, systemLanguage: String): AppLocale` function extracted in task 2.1 — no mocking needed as it has no side effects
    4. Test cases:
       - `resolveLocale(null, "ru")` → `AppLocale.RUSSIAN`
       - `resolveLocale(null, "ru_RU")` → `AppLocale.RUSSIAN` (prefix match via `startsWith`)
       - `resolveLocale(null, "en")` → `AppLocale.ENGLISH`
       - `resolveLocale(null, "fr")` → `AppLocale.ENGLISH`
       - `resolveLocale(AppLocale.RUSSIAN, "en")` → `AppLocale.RUSSIAN` (saved pref overrides)
       - `resolveLocale(AppLocale.ENGLISH, "ru")` → `AppLocale.ENGLISH` (saved pref overrides)
       - `resolveLocale(null, "de")` → `AppLocale.ENGLISH` (unknown falls back to English)
  - **Files**:
    - `app/src/test/kotlin/sudoku/app/ui/i18n/LocaleResolverTest.kt` (create)
  - **Done when**: 7 test cases defined; all pass; tests call `resolveLocale` directly with no mocking required
  - **Verify**: `./gradlew test 2>&1 | tail -15`
  - **Commit**: `test(i18n): add LocaleResolverTest with 7 cases`
  - _Requirements: FR-6, AC-1.1, AC-1.2, AC-4.2, AC-4.3_

---

- [ ] 3.2 Create `AppPreferencesTest` — unit tests for prefs load/save/fallback
  - **Do**:
    1. Create `app/src/test/kotlin/sudoku/app/ui/i18n/AppPreferencesTest.kt`
    2. Use a unique Preferences node per test (e.g., `Preferences.userRoot().node("sudoku/test-${UUID.randomUUID()}")`) to avoid cross-test pollution; or call `prefs.removeNode()` in `@After`. Since `AppPreferences` is an `object` with a fixed node, consider using a `@Before` that clears the node's `locale` key via reflection or by adding a `clearForTest()` internal method
    3. Test cases:
       - `saveLocale(RUSSIAN)` then `loadLocale()` returns `RUSSIAN`
       - `saveLocale(ENGLISH)` then `loadLocale()` returns `ENGLISH`
       - No key set (fresh node) → `loadLocale()` returns `null`
       - Invalid/corrupt value stored manually → `loadLocale()` returns `null` (not throws); test by calling `prefs.put(KEY, "INVALID")` then `loadLocale()`
    4. Clean up test node in `@After`
  - **Files**: `app/src/test/kotlin/sudoku/app/ui/i18n/AppPreferencesTest.kt` (create)
  - **Done when**: 4 test cases pass; `loadLocale()` never throws; cleanup runs in `@After`
  - **Verify**: `./gradlew test 2>&1 | tail -15`
  - **Commit**: `test(i18n): add AppPreferencesTest with save/load/fallback cases`
  - _Requirements: FR-7, AC-4.1, AC-4.2, AC-4.3_

---

- [ ] 3.3 Create `StringsCompletenessTest` — verify both impls have non-blank values
  - **Do**:
    1. Create `app/src/test/kotlin/sudoku/app/ui/i18n/StringsCompletenessTest.kt`
    2. Test cases using direct property access (no reflection needed since interface is known):
       - For `EnglishStrings`: assert each of the 23 `String`-typed `val` properties is non-blank (the 24th member `gameOverMistakes` is the `(Int)->String` lambda, tested separately below) (use a helper `fun assertNonBlank(s: String, name: String)`)
       - For `RussianStrings`: same check on all 23 `String`-typed `val` properties (the 24th member `gameOverMistakes` is the `(Int)->String` lambda, tested separately below)
       - `EnglishStrings.gameOverMistakes(3)` contains `"3"`
       - `RussianStrings.gameOverMistakes(3)` contains `"3"`
       - `RussianStrings.gameOverMistakes(0)` contains `"0"` (interpolation works for any N)
    3. List all 23 property names explicitly (do NOT use reflection) — this makes test intent clear and means adding a 24th string requires updating this test
  - **Files**: `app/src/test/kotlin/sudoku/app/ui/i18n/StringsCompletenessTest.kt` (create)
  - **Done when**: All assertions pass; both English and Russian strings verified non-blank; parameterised function tested
  - **Verify**: `./gradlew test 2>&1 | tail -15`
  - **Commit**: `test(i18n): add StringsCompletenessTest for all 24 string members`
  - _Requirements: FR-1, FR-2, FR-3, AC-5.1, AC-5.2, AC-6.2_

---

- [ ] 3.4 [VERIFY] Quality checkpoint — full build + tests green
  - **Do**: Run full build and all tests
  - **Verify**: `./gradlew build 2>&1 | tail -10`
  - **Done when**: BUILD SUCCESSFUL; all tests pass; zero test failures
  - **Commit**: `chore(i18n): pass quality checkpoint after Phase 3 tests` (if fixes needed)

---

## Phase 4: Quality Gates + PR

---

- [ ] V4 [VERIFY] Full local CI: build + test
  - **Do**: Run complete build and test suite; confirm zero errors and zero test failures
  - **Verify**: `./gradlew build 2>&1 | grep -E "BUILD|tests|failures|errors" | tail -10`
  - **Done when**: `BUILD SUCCESSFUL`; test summary shows 0 failures; no compiler errors
  - **Commit**: `chore(i18n): pass local CI` (if fixes needed)
  - _Requirements: FR-1, FR-2, FR-3, FR-4, FR-5, FR-6, FR-7, FR-8, FR-9, FR-10, FR-11, FR-12_

- [ ] V5 [VERIFY] CI pipeline passes
  - **Do**: Push branch and verify GitHub Actions CI passes
  - **Verify**: `gh pr checks 2>/dev/null || echo "Push branch first, then check gh pr checks"`
  - **Done when**: All CI checks show green; or confirm no CI configured and build passes locally
  - **Commit**: None
  - _Requirements: FR-1, FR-2, FR-3, FR-4, FR-5, FR-6, FR-7, FR-8, FR-9, FR-10, FR-11, FR-12_

- [ ] V6 [VERIFY] AC checklist
  - **Do**:
    1. AC-1.1/1.2: `grep -r "startsWith.*ru\|language.*ru" app/src/main/kotlin/sudoku/app/ui/i18n/ && echo AC-1-PASS`
    2. AC-2.1/2.3: `grep -r "🇬🇧\|🇷🇺\|alpha" app/src/main/kotlin/sudoku/app/ui/HomeScreen.kt && echo AC-2-PASS`
    3. AC-2.4: `grep -r "🇬🇧\|🇷🇺\|onLocaleChange" app/src/main/kotlin/sudoku/app/ui/components/ app/src/main/kotlin/sudoku/app/ui/GameScreen.kt | grep -v ".class" && echo "FLAG IN GAME FILES - FAIL" || echo AC-2.4-PASS`
    4. AC-4.1/4.2: `grep -r "saveLocale\|loadLocale" app/src/main/kotlin/sudoku/app/ui/i18n/AppPreferences.kt && echo AC-4-PASS`
    5. AC-5.3: `grep -rn '"Quit\|"Cancel\|"Easy\|"Medium\|"Hard\|"Expert\|"Sudoku\|"New Game\|"Mistakes\|"Time\|"Game Paused\|"Resume\|"Puzzle Solved\|"Back to Home\|"Game Over\|"No hint" app/src/main/kotlin/sudoku/app/ui/*.kt app/src/main/kotlin/sudoku/app/ui/components/*.kt | grep -v "//\|EnglishStrings\|RussianStrings\|Strings.kt" | grep -v ".class" && echo "HARDCODED STRINGS REMAIN - FAIL" || echo AC-5.3-PASS`
    6. AC-6.1/6.2: `grep "interface Strings" app/src/main/kotlin/sudoku/app/ui/i18n/Strings.kt && echo AC-6-PASS`
  - **Verify**: All commands produce expected PASS output
  - **Done when**: All ACs confirmed via automated grep/build checks
  - **Commit**: None
  - _Requirements: All FRs_

---

- [ ] 4.1 Create PR
  - **Do**:
    1. Confirm on feature branch: `git branch --show-current`
    2. Push: `git push -u origin HEAD`
    3. Create PR: `gh pr create --title "feat(i18n): add English + Russian localisation" --body "$(cat <<'EOF'
## Summary
- Adds typed Strings interface with EnglishStrings and RussianStrings object implementations (24 string keys)
- Wires CompositionLocal<Strings> at App root; AppLocale mutableStateOf drives which impl is provided
- Flag toggle (🇬🇧/🇷🇺) on HomeScreen persists choice via java.util.prefs.Preferences with system-locale fallback
- All 7 composable files (App, HomeScreen, GameScreen, PauseOverlay, CompletionOverlay, GameOverDialog, HintBanner) use LocalStrings.current

## Test plan
- ./gradlew build passes with zero errors
- ./gradlew test passes — LocaleResolverTest (7 cases), AppPreferencesTest (4 cases), StringsCompletenessTest pass
- AC-5.3: no hardcoded English strings remain in composable files
- AC-2.4: flag toggle absent from game-screen files (verified by grep in V6)
EOF
)"`
  - **Verify**: `gh pr view --json url -q .url`
  - **Done when**: PR created and URL returned
  - **Commit**: None
  - _Requirements: FR-1, FR-2, FR-3, FR-4, FR-5, FR-6, FR-7, FR-8, FR-9, FR-10, FR-11, FR-12_

---

## Phase 5: PR Lifecycle

Autonomous loop: monitor CI, fix failures, address review comments.

- [ ] 5.1 Monitor CI and fix failures
  - **Do**:
    1. Check CI status: `gh pr checks --watch`
    2. If failures: read logs `gh run view --log-failed`, fix locally, push
    3. Repeat until all checks green
  - **Verify**: `gh pr checks 2>&1 | grep -v "pass\|✓" | grep -E "fail|×" && echo "FAILURES REMAIN" || echo "ALL GREEN"`
  - **Done when**: All CI checks pass
  - **Commit**: `fix(i18n): address CI failure - <description>` (if fixes needed)
  - _Requirements: FR-1, FR-2, FR-3, FR-4, FR-5, FR-6, FR-7, FR-8, FR-9, FR-10, FR-11, FR-12_

- [ ] 5.2 Address review comments
  - **Do**:
    1. Read review comments: `gh pr view --json reviews,comments`
    2. For each blocking comment: implement fix, commit, push
    3. Reply to each addressed comment: `gh api repos/{owner}/{repo}/pulls/{pr}/reviews/{review_id}/comments/{comment_id}/replies -f body="Fixed in <commit>"`
  - **Verify**: `gh pr view --json reviewDecision -q .reviewDecision`
  - **Done when**: All blocking comments resolved; `reviewDecision` is `APPROVED` or no blocking comments remain
  - **Commit**: `fix(i18n): address review - <description>` per comment batch
  - _Requirements: FR-1, FR-2, FR-3, FR-4, FR-5, FR-6, FR-7, FR-8, FR-9, FR-10, FR-11, FR-12_

---

## Notes

- **POC shortcuts taken**: Flag toggle uses emoji text (🇬🇧/🇷🇺) — may render as letter pairs on Windows JVM; documented in design.md as known caveat
- **Production TODO**: If Windows flag rendering is unacceptable, swap `TextButton` emoji for `Image` composables with bundled PNG assets (one-composable change in `HomeScreen`)
- **Dependency order**: Tasks 1.1 → 1.2 (EnglishStrings must exist before `Strings.kt` can compile its default `{ EnglishStrings }`) → 1.3 → 1.4 → 1.5 → 1.6 [VERIFY] → 1.7+1.8 (parallel) → 1.9 → 1.10 → 1.11 → 1.12
- **`mistakeCount` threading**: `GameOverDialog` hardcodes `3`; task 1.8 introduces `mistakeCount: Int` param and updates `GameScreen.kt` call site in the same task to avoid a transient compile error