# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Run all engine tests (primary quality gate)
./gradlew :engine:test

# Run a single test class
./gradlew :engine:test --tests "sudoku.engine.GraderTest"

# Run a single test by name (use wildcards for spaces)
./gradlew :engine:test --tests "sudoku.engine.GraderTest.known*"

# Run app tests
./gradlew :app:test

# Compile-check the app module (fast, no tests)
./gradlew :app:compileKotlin

# Run the app locally (launches with -Ddebug=true, which gives 99 hints)
./gradlew :app:run

# Package for current OS
./gradlew :app:packageDmg   # macOS
./gradlew :app:packageMsi   # Windows
./gradlew :app:packageDeb   # Linux
```

CI runs `./gradlew :engine:test :app:packageDmg` (or equivalent for each OS) with Java 25.

## Architecture

Two Gradle modules: `:engine` (pure Kotlin, no dependencies) and `:app` (Compose Multiplatform desktop).

### Engine module (`engine/`)

Stateless `object` singletons — no instances, no DI:

- **`Board`** — immutable puzzle snapshot: `digits: IntArray(81)`, `givens: BooleanArray(81)`, `solution: IntArray(81)`. Cell index = `row * 9 + col`.
- **`Generator`** — produces a `Board` at a target `Difficulty`. Fills a full grid via Las Vegas backtracking, then `digHoles()` removes cells while `Solver.countSolutions()` enforces uniqueness. Retries up to `MAX_ATTEMPTS = 1000` until `Grader.grade()` matches the requested difficulty.
- **`Grader`** — grades a puzzle by running techniques progressively until the board is solved; the tier where it solves determines the grade. Techniques are applied as `internal fun apply*(candidates, digits): Boolean` functions that return `true` if they made progress. Candidate sets are bitmasks (bit `d` = digit `d` is a candidate).
- **`Solver`** — backtracking solver; only `countSolutions(puzzle, max)` is used publicly (returns early at `max` for uniqueness checks).
- **`HintEngine`** — takes a `Board` + `Difficulty`, tries each hint technique in order, returns the first `HintResult.Found` or a `NoHint`/`NoHintForDifficulty` sentinel.
- **`HintResult`** — sealed class: `Found(technique, targetCells, peerCells, explanation, explanationData?)`, `NoHint`, `NoHintForDifficulty`.
- **`HintExplanationData`** — sealed class with typed variants (`Single`, `Pair`, `PointingPairRow`, `PointingPairCol`) used by the UI to render structured hint text.

Shared geometry (`PEERS`, `ROW_UNITS`, `COL_UNITS`, `BOX_UNITS`, `ALL_UNITS`) lives in a file alongside the engine objects and is accessed by all of them.

### App module (`app/`)

Unidirectional data flow: `GameIntent` → `GameViewModel` → `GameState` → Compose UI.

- **`GameViewModel`** — single entry point `dispatch(intent)`. `reduce()` is a pure function (no side effects). Side effects (puzzle generation, timer) are launched in `handleSideEffects()` as coroutines and dispatch further intents (`PuzzleGenerated`, `TimerTick`). Generation runs on `Dispatchers.Default`.
- **`GameState`** — immutable data class with manual `equals`/`hashCode` (required because `IntArray`/`BooleanArray` don't implement structural equality).
- **`App.kt`** — root composable. Switches between `HomeScreen` (no puzzle loaded), `CircularProgressIndicator` (loading), and `GameScreen` (game in progress). Owns locale state and wires `AppPreferences`.
- **`AppPreferences`** — thin wrapper around `java.util.prefs.Preferences` (`userRoot().node("sudoku/app")`). Currently persists locale. Follows a load/save pattern with silent exception handling.

### i18n

All UI strings go through `LocalStrings.current` (a `CompositionLocal`). The `Strings` interface is implemented by `EnglishStrings` and `RussianStrings`. **Every new string key must be added to all three files** — the interface enforces completeness at compile time. String keys for hint explanations use lambda types, e.g. `val hintExplainPair: (String, String, Int, Int) -> String`.

### Technique implementation pattern

Both `Grader` and `HintEngine` implement each solving technique separately:
- `Grader` techniques mutate `candidates`/`digits` arrays and return `Boolean` (progress made).
- `HintEngine` techniques take `Board` + `candidates`, return `HintResult.Found?` (null if technique doesn't apply).

When adding a new technique, implement it in both places. The `Grader` version eliminates candidates; the `HintEngine` version explains which cells are affected and why.

### Slow generation tests

`GeneratorTest` uses `@Disabled` for EXPERT-level generation tests that may exceed CI time limits. Follow this pattern for any new slow generation tests.