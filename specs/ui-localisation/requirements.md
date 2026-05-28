# Requirements: UI Localisation

## Overview

Add localisation support to the Compose Desktop sudoku app so all UI strings are translatable. English and Russian are the two supported languages in v1, with a home-screen toggle and automatic system-locale detection.

---

## User Stories

### US-1: System Locale Auto-Detection
**As a** Russian-speaking user  
**I want to** open the app and see the UI in Russian automatically  
**So that** I don't have to configure anything manually on first launch

**Acceptance Criteria:**
- [ ] AC-1.1: On first launch, if the system locale is `ru` (or `ru_*`), the app displays all strings in Russian
- [ ] AC-1.2: On first launch, if the system locale is anything other than `ru`, the app defaults to English
- [ ] AC-1.3: Auto-detection runs before the first frame is rendered — no flash of wrong language

### US-2: Manual Language Toggle
**As a** user  
**I want to** switch the app language from the home screen  
**So that** I can override the auto-detected language at any time

**Acceptance Criteria:**
- [ ] AC-2.1: The home screen displays two flag icons: 🇬🇧 (English) and 🇷🇺 (Russian)
- [ ] AC-2.2: Tapping the inactive flag switches the app language instantly (no restart, no navigation)
- [ ] AC-2.3: The active language flag is visually distinct from the inactive one (e.g. highlighted, full opacity vs. dimmed)
- [ ] AC-2.4: The toggle is only reachable from the home screen — it is not visible during an active game, on pause, or on completion overlays

### US-3: Live Language Switching
**As a** user  
**I want** the entire UI to update immediately when I change language  
**So that** every screen reflects the new language without navigating away or restarting

**Acceptance Criteria:**
- [ ] AC-3.1: All 24 strings update on the next recomposition after the toggle — no stale strings remain visible
- [ ] AC-3.2: Switching language while on the home screen shows the home screen strings in the new language
- [ ] AC-3.3: Switching language does not reset game state, navigation state, or any other app state

### US-4: Persisted Language Preference
**As a** returning user  
**I want** my language choice remembered between app launches  
**So that** I don't have to re-select my language every time I open the app

**Acceptance Criteria:**
- [ ] AC-4.1: After selecting a language and restarting the app, the previously selected language is restored
- [ ] AC-4.2: Persisted preference overrides system-locale auto-detection on subsequent launches
- [ ] AC-4.3: If stored preference is absent or unreadable, fall back to system-locale detection

### US-5: Full String Coverage
**As a** Russian-speaking user  
**I want** every visible text string in the app to be in Russian  
**So that** there are no English remnants when Russian is selected

**Acceptance Criteria:**
- [ ] AC-5.1: All 24 strings listed below are translated and display correctly in Russian when Russian is selected:
  - `"Quit Game?"`, `"You have unsaved progress. Are you sure you want to quit?"`, `"Quit"`, `"Cancel"` (App.kt)
  - `"Sudoku"`, `"Easy"`, `"Medium"`, `"Hard"`, `"Expert"` (HomeScreen.kt)
  - `"Mistakes"`, `"Time"`, `"New Game"`, `"New Game?"`, `"Start a new game? Your current progress will be lost."`, `"Start"`, `"Cancel"` (GameScreen.kt)
  - `"Game Paused"`, `"Resume"` (PauseOverlay.kt)
  - `"Puzzle Solved!"`, `"New Game"`, `"Back to Home"` (CompletionOverlay.kt)
  - `"Game Over"`, `"You made {N} mistakes. Better luck next time!"` (GameOverDialog.kt)
  - `"No hint available"`, `"No hint available for this difficulty level"` (HintBanner.kt)
- [ ] AC-5.2: The parameterised mistakes string (`"You made {N} mistakes..."`) correctly interpolates the integer N in both English and Russian
- [ ] AC-5.3: No hardcoded English string literals remain in any UI composable file after the change

### US-6: Adding a Third Language (Maintainability)
**As a** developer  
**I want** the localisation structure to make adding a new language straightforward  
**So that** future contributors can add a 3rd language without modifying existing translation files

**Acceptance Criteria:**
- [ ] AC-6.1: Adding a 3rd language requires only: implementing the `Strings` interface + registering the new locale in the locale-selection logic — no changes to any composable
- [ ] AC-6.2: The `Strings` interface is the single authoritative list of all translatable string keys

---

## Functional Requirements

| ID | Requirement | Priority | Acceptance Criteria |
|----|-------------|----------|---------------------|
| FR-1 | Typed `Strings` interface listing all 24 string keys, including a function type for the parameterised mistakes string | High | AC-5.1, AC-5.2, AC-6.2 |
| FR-2 | `EnglishStrings` object implementing `Strings` with all 24 English values | High | AC-5.1, AC-5.3 |
| FR-3 | `RussianStrings` object implementing `Strings` with all 24 Russian values | High | AC-5.1 |
| FR-4 | `CompositionLocal<Strings>` (`LocalStrings`) provided at `App` root via `CompositionLocalProvider` | High | AC-3.1, AC-3.2 |
| FR-5 | `mutableStateOf<AppLocale>` at `App` root drives which `Strings` implementation is provided | High | AC-3.1, AC-3.3 |
| FR-6 | System locale auto-detection on startup: read `java.util.Locale.getDefault()`, map `ru` prefix to Russian, all others to English | High | AC-1.1, AC-1.2, AC-1.3 |
| FR-7 | Language preference persisted and loaded via `java.util.prefs.Preferences` | High | AC-4.1, AC-4.2, AC-4.3 |
| FR-8 | Home screen displays 🇬🇧 / 🇷🇺 flag toggle; tapping inactive flag updates `AppLocale` state and persists choice | High | AC-2.1, AC-2.2, AC-2.3, AC-2.4 |
| FR-9 | All 7 composable files (`App.kt`, `HomeScreen.kt`, `GameScreen.kt`, `PauseOverlay.kt`, `CompletionOverlay.kt`, `GameOverDialog.kt`, `HintBanner.kt`) replaced to use `LocalStrings.current` — no hardcoded string literals | High | AC-5.3 |
| FR-10 | `AppLocale` enum or sealed class with `ENGLISH` and `RUSSIAN` values | Medium | AC-6.1 |
| FR-11 | `GameScreen`, `PauseOverlay`, `CompletionOverlay`, and `GameOverDialog` must NOT render the flag toggle UI — the toggle is exclusively a `HomeScreen` concern | High | AC-2.4 |
| FR-12 | `AppLocale` `mutableStateOf` must be declared at the `App` root above and independently of all game-state holders so that updating it does not trigger recomposition of game-state nodes in the composition tree | High | AC-3.3 |

---

## Non-Functional Requirements

| ID | Requirement | Metric | Target |
|----|-------------|--------|--------|
| NFR-1 | Performance — language switch | Recomposition latency | No perceptible lag; recomposition completes within a single frame (~16 ms) on all target platforms |
| NFR-2 | Startup overhead | Time added to cold start by locale detection + prefs read | < 50 ms |
| NFR-3 | Maintainability — adding a 3rd language | Files to create/modify | 1 new file (new `Strings` impl) + 1 small change (locale mapping) — 0 composable changes |
| NFR-4 | Compatibility | Platforms | macOS, Windows, Linux — `java.util.prefs.Preferences` and `java.util.Locale` are JVM-standard and available on all three |
| NFR-5 | Correctness | Missing translation keys | Kotlin compiler enforces full interface implementation — no runtime missing-string errors possible |
| NFR-6 | Russian text rendering | Cyrillic character support | All target platforms ship with fonts covering the Cyrillic block; no custom font bundling required |

---

## Out of Scope

- RTL (right-to-left) language support
- In-game language switching (toggle is home screen only; switching mid-game is not supported)
- Languages beyond English and Russian (v1 ships exactly two locales)
- Locale-aware formatting of numbers, dates, or times beyond string substitution
- Pluralisation rules (the mistakes string uses a single template for all N values in both languages)
- Translation tooling, translation memory, or `.properties`/`.arb` file-based string resources

---

## Glossary

| Term | Definition |
|------|-----------|
| **AppLocale** | Enum with values `ENGLISH` and `RUSSIAN` representing the two supported UI languages |
| **Strings interface** | Kotlin interface declaring one property or function per translatable string; the single source of truth for all string keys |
| **EnglishStrings** | Object implementing `Strings` with hardcoded English values |
| **RussianStrings** | Object implementing `Strings` with hardcoded Russian (Cyrillic) values |
| **CompositionLocal** | Compose mechanism for implicitly passing a value down the composition tree without explicit prop-drilling |
| **LocalStrings** | The `ProvidableCompositionLocal<Strings>` instance; composables call `LocalStrings.current` to access strings |
| **CompositionLocalProvider** | Compose composable that sets a `CompositionLocal` value for its subtree |
| **System locale** | The locale returned by `java.util.Locale.getDefault()` at app startup, reflecting the OS language setting |
| **Preferences** | `java.util.prefs.Preferences` — cross-platform JVM API for persisting small key/value user preferences |
| **Parameterised string** | The mistakes string `"You made {N} mistakes. Better luck next time!"` where N is an `Int`; represented as a function `(Int) -> String` in the `Strings` interface |

---

## Dependencies

| Dependency | Type | Notes |
|-----------|------|-------|
| `App.kt` | Existing file | Root composable; must host `AppLocale` state and `CompositionLocalProvider` |
| `HomeScreen.kt` | Existing file | Must receive or access a callback to change `AppLocale`; hosts the flag toggle UI |
| `GameScreen.kt`, `PauseOverlay.kt`, `CompletionOverlay.kt`, `GameOverDialog.kt`, `HintBanner.kt` | Existing files | Must be updated to read strings from `LocalStrings.current` |
| `java.util.prefs.Preferences` | JVM stdlib | Already available; no new dependency |
| `java.util.Locale` | JVM stdlib | Already available; no new dependency |
| Jetpack Compose `CompositionLocalProvider` | Existing framework dep | Already in use in the project |

---

## Success Criteria

- All 24 strings display in Russian when Russian is selected and in English when English is selected — verified on all three platforms (macOS, Windows, Linux)
- The compiler rejects any `Strings` implementation that omits a key — no runtime missing-translation errors
- Language toggle on home screen switches the entire visible UI within a single frame
- Selected language survives an app restart
- A developer can add a 3rd language by creating one new file and editing one mapping expression — no composable changes required