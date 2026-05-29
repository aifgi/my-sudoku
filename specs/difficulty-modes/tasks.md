# Tasks: difficulty-modes

## Phase 1 — Foundation (new types + engine accuracy)

Steps 1–3: no inter-phase dependencies; all three tasks can start in parallel.

---

- [x] 1.1 [P] Create `PuzzleDifficulty` sealed class and `GivenGrade` enum
  - **Do**:
    1. Create `PuzzleDifficulty.kt` with `sealed class PuzzleDifficulty` containing `data class Technique(val grade: Difficulty)` and `data class Given(val grade: GivenGrade)` subclasses
    2. Create `GivenGrade.kt` with `enum class GivenGrade(val minGivens: Int, val maxGivens: Int, val techniqueCeiling: TechniqueCeiling)` — values EASY(36,45,SINGLES), MEDIUM(29,35,SINGLES), HARD(24,28,PAIRS), EXPERT(17,23,PAIRS)
    3. Add `enum class TechniqueCeiling { SINGLES, PAIRS }` in the same `GivenGrade.kt` file
  - **Files**:
    - `engine/src/main/kotlin/sudoku/engine/PuzzleDifficulty.kt` (create)
    - `engine/src/main/kotlin/sudoku/engine/GivenGrade.kt` (create)
  - **Done when**: Both files compile as part of `:engine:compileKotlin`; `PuzzleDifficulty.Technique(Difficulty.EASY)` and `PuzzleDifficulty.Given(GivenGrade.HARD)` can be instantiated; exhaustive `when` on `PuzzleDifficulty` requires no else branch
  - **Commit**: `feat(engine): add PuzzleDifficulty sealed class and GivenGrade enum`
  - _Requirements: FR-8, AC-2.7_
  - _Design: PuzzleDifficulty, GivenGrade components_

---

- [x] 1.2 [P] Add `applyNakedTriples`, `applyHiddenTriples`, `applySwordfish` to `Grader` and restructure grade ladder
  - **Do**:
    1. Change `computeCandidates` visibility from `private` to `internal` in `Grader.kt`
    2. Implement `internal fun applyNakedTriples(candidates: IntArray, digits: IntArray): Boolean` — for each unit, find all 3-cell subsets whose union of candidates contains exactly 3 digits; eliminate those 3 digits from all other cells in the unit; return true if any elimination occurred
    3. Implement `internal fun applyHiddenTriples(candidates: IntArray, digits: IntArray): Boolean` — for each unit, find all 3-digit subsets that appear in exactly 3 cells; remove all other candidates from those 3 cells; return true if any removal occurred
    4. Implement `internal fun applySwordfish(candidates: IntArray, digits: IntArray): Boolean` — for each digit, find 3 rows each having that digit in ≤3 columns; if the union of those column indices is exactly 3, eliminate the digit from all other cells in those 3 columns; also apply the symmetric column-based variant; return true if any elimination occurred
    5. Restructure the grade ladder in `Grader.grade()` to the new 4-tier boundary:
       - Tier 1 (EASY): Naked Singles + Hidden Singles
       - Tier 2 (MEDIUM): + Naked Pairs + Hidden Pairs + Pointing Pairs
       - Tier 3 (HARD): + Naked Triples + Hidden Triples
       - Tier 4 (EXPERT): + X-Wing + Swordfish (fall-through)
    6. Update `GraderTest.kt`: replace the `hardPuzzle` reference puzzle (which previously required Pointing Pairs — now MEDIUM) with a puzzle that requires Naked/Hidden Triples to grade HARD; add test cases `applyNakedTriples eliminates candidates`, `applyHiddenTriples eliminates candidates`, `applySwordfish eliminates candidates`, `known Triples puzzle grades as HARD`, `known Swordfish puzzle grades as EXPERT`
  - **Files**:
    - `engine/src/main/kotlin/sudoku/engine/Grader.kt` (modify)
    - `engine/src/test/kotlin/sudoku/engine/GraderTest.kt` (modify)
  - **Done when**: `./gradlew :engine:test --tests "sudoku.engine.GraderTest"` passes; new technique tests green; existing EASY/MEDIUM/EXPERT reference puzzles still grade correctly; `ExternalPuzzleGraderTest` runs without assertion errors (it only prints, no assertions)
  - **Commit**: `feat(engine): add Naked/Hidden Triples and Swordfish to Grader; restructure grade ladder`
  - _Requirements: FR-4, FR-5, FR-6, AC-4.1–AC-4.5, AC-4.7, NFR-3_
  - _Design: Grader component, Test Strategy_

---

## Phase 2 — Generation + Hints

Steps 4–6. Task 2.1 depends on Phase 1 (Grader changes in 1.2 must be complete first). Task 2.2 depends on 1.1 and the new `HintResult` data classes (added within 2.2 itself, see below).

---

- [x] 2.1 Add `generateByGivenCount` to `Generator` and update `GeneratorTest`
  - **Do**:
    1. Add private `fun digHolesToTarget(solution: IntArray, grade: GivenGrade): IntArray?` — shuffle cell indices, dig holes until `81 - holes == target` (target sampled uniformly from `grade.minGivens..grade.maxGivens`), checking `Solver.countSolutions(puzzle, 2) == 1` each step; return `null` if uniqueness blocks target
    2. Add private `fun ceilingExceeded(puzzle: IntArray, grade: GivenGrade): Boolean` — for SINGLES: inline apply Naked/Hidden Singles loop using `Grader.computeCandidates` (now `internal`), return `digits.any { it == 0 }`; for PAIRS: call `Grader.grade(puzzle)`, return true if grade is HARD or EXPERT
    3. Add `suspend fun generateByGivenCount(grade: GivenGrade): Board` — outer loop up to `MAX_ATTEMPTS`: `fillGrid()`, `digHolesToTarget`, `ceilingExceeded` check, then construct and return `Board.fromDigits`; throw `IllegalStateException` if exhausted
    4. Update `GeneratorTest.kt`: add tests `generateByGivenCount EASY produces givens in 36–45`, `MEDIUM in 29–35`, `HARD in 24–28`, `board has exactly one solution`, `EASY puzzle does not exceed singles ceiling`, `HARD puzzle does not exceed pairs ceiling` (use `@Disabled` annotation on any slow EXPERT test consistent with existing pattern)
  - **Files**:
    - `engine/src/main/kotlin/sudoku/engine/Generator.kt` (modify)
    - `engine/src/test/kotlin/sudoku/engine/GeneratorTest.kt` (modify)
  - **Done when**: `./gradlew :engine:test --tests "sudoku.engine.GeneratorTest"` passes; given-count range assertions hold; uniqueness test passes; ceiling tests pass
  - **Commit**: `feat(engine): add generateByGivenCount with hole-digging and ceiling check`
  - _Requirements: FR-1, AC-2.3–AC-2.6, AC-2.8, NFR-1_
  - _Design: Generator component_

---

- [x] FIX-2.1 Fix `digHolesToTarget` null-return to use exact-target check
  - **Do**:
    1. In `Generator.kt`, find the `digHolesToTarget` function
    2. Change the null-return condition from `if (givens !in grade.minGivens..grade.maxGivens) return null` to `if (givens != target) return null` (where `target` is the local variable already in scope at the top of `digHolesToTarget`)
    3. This ensures the exact sampled target is required, matching the design's uniform sampling guarantee
  - **Files**:
    - `engine/src/main/kotlin/sudoku/engine/Generator.kt` (modify)
  - **Done when**: `./gradlew :engine:test --tests "sudoku.engine.GeneratorTest"` passes; `digHolesToTarget` returns null when uniqueness pressure prevents reaching exact target
  - **Commit**: `fix(engine): require exact given-count target in digHolesToTarget`
  - _Fix for: review finding on task 2.1_

---

- [x] 2.2 Add `HintExplanationData.Triple` and `HintExplanationData.Swordfish`; update `HintEngine` signature and add triple/swordfish hint methods; update `HintEngineTest`
  - **Do**:
    1. In `HintResult.kt`, add to `HintExplanationData` sealed class: `data class Triple(val cell1: Int, val cell2: Int, val cell3: Int, val d1: Int, val d2: Int, val d3: Int) : HintExplanationData()` and `data class Swordfish(val digit: Int) : HintExplanationData()`
    2. Change `HintEngine.findHint` signature from `(board: Board, difficulty: Difficulty)` to `(board: Board, difficulty: PuzzleDifficulty)`
    3. Implement private `findHintForTechnique(board, candidates, grade: Difficulty): HintResult` containing the existing single/pair/pointing-pair chain, plus: Naked/Hidden Triples for HARD and EXPERT grades, X-Wing and Swordfish for EXPERT grade; return `NoHintForDifficulty` only for HARD/EXPERT when all techniques exhausted (preserve existing EASY/MEDIUM behaviour returning `NoHint`)
    4. Implement private `findHintForGiven(board, candidates, grade: GivenGrade): HintResult` — always try singles; for HARD/EXPERT also try pairs and pointing pairs; never suggest Triples, X-Wing, Swordfish; return `HintResult.NoHint` (not `NoHintForDifficulty`) when ceiling reached
    5. Implement private `nakedTriple(board, candidates): HintResult.Found?` — find a naked triple in any unit, return `Found` with `HintExplanationData.Triple` and elimination targets
    6. Implement private `hiddenTriple(board, candidates): HintResult.Found?` — find a hidden triple in any unit, return `Found` with `HintExplanationData.Triple`
    7. Implement private `swordfishHint(board, candidates): HintResult.Found?` — find swordfish pattern for any digit, return `Found` with `HintExplanationData.Swordfish` (note: `xWingHint` already exists; verify it does and wire it in; add if missing)
    8. Update `HintEngineTest.kt`: migrate all `findHint(board, Difficulty.*)` call sites to `findHint(board, PuzzleDifficulty.Technique(Difficulty.*))` ; add new tests: `Given EASY returns NoHint (not NoHintForDifficulty) when singles exhausted`, `Given HARD returns pair hint when singles exhausted`, `Given EXPERT never returns triple hint`, `Technique HARD returns triple hint when pairs exhausted`, `Technique EXPERT returns swordfish hint`
  - **Files**:
    - `engine/src/main/kotlin/sudoku/engine/HintResult.kt` (modify)
    - `engine/src/main/kotlin/sudoku/engine/HintEngine.kt` (modify)
    - `engine/src/test/kotlin/sudoku/engine/HintEngineTest.kt` (modify)
  - **Done when**: `./gradlew :engine:test --tests "sudoku.engine.HintEngineTest"` passes; all new hint gating tests green; `when(difficulty)` on `PuzzleDifficulty` in `findHint` is exhaustive; no `NoHintForDifficulty` returned for Given-mode paths
  - **Commit**: `feat(engine): add PuzzleDifficulty dispatch and triple/swordfish hints to HintEngine`
  - _Requirements: FR-4, FR-5, FR-6, FR-7, FR-10, AC-3.1–AC-3.6, AC-4.6, NFR-4_
  - _Design: HintEngine component, HintExplanationData subclasses_

---

## Phase 3 — App Layer

Steps 7–13. Tasks in this phase depend on Phase 1 (PuzzleDifficulty sealed class) and Phase 2 (HintEngine signature). Tasks 3.1 and 3.2 can run in parallel; 3.3 depends on both; 3.4 depends on 3.1 and 3.2.

---

- [x] 3.1 [P] Update `GameState` and `GameIntent` to use `PuzzleDifficulty`
  - **Do**:
    1. In `GameState.kt`, change field types: `difficulty: PuzzleDifficulty`, `pendingDifficulty: PuzzleDifficulty?`, `newGameTargetDifficulty: PuzzleDifficulty?`
    2. Update `GameState.Initial` companion: `difficulty = PuzzleDifficulty.Technique(Difficulty.EASY)`, `pendingDifficulty = null`, `newGameTargetDifficulty = null`
    3. In `GameIntent.kt`, change `StartNewGame.difficulty` from `Difficulty` to `PuzzleDifficulty`
    4. Fix all compile errors in `GameViewModel.kt` arising from the type change (cast/unwrap as needed; full ViewModel changes are task 3.3)
  - **Files**:
    - `app/src/main/kotlin/sudoku/app/state/GameState.kt` (modify)
    - `app/src/main/kotlin/sudoku/app/state/GameIntent.kt` (modify)
  - **Done when**: `./gradlew :app:compileKotlin` passes (all usages of `difficulty` in app module type-check with `PuzzleDifficulty`)
  - **Commit**: `feat(app): migrate GameState and GameIntent difficulty fields to PuzzleDifficulty`
  - _Requirements: FR-10, AC-3.1_
  - _Design: GameState, GameIntent components_

---

- [x] 3.2 [P] Create `DifficultyMode.kt` enum and add `loadMode`/`saveMode` to `AppPreferences`
  - **Do**:
    1. Create `DifficultyMode.kt` with `enum class DifficultyMode { TECHNIQUE, GIVEN }` in `app/src/main/kotlin/sudoku/app/ui/`
    2. In `AppPreferences.kt`, add `private const val KEY_MODE = "difficulty_mode"`, `fun loadMode(): DifficultyMode` (reads key, defaults to `TECHNIQUE`, catches parse exceptions silently), `fun saveMode(mode: DifficultyMode)` (writes key, catches exceptions, prints to stderr)
  - **Files**:
    - `app/src/main/kotlin/sudoku/app/ui/DifficultyMode.kt` (create)
    - `app/src/main/kotlin/sudoku/app/ui/i18n/AppPreferences.kt` (modify)
  - **Done when**: `./gradlew :app:compileKotlin` passes; `AppPreferences().loadMode()` returns `DifficultyMode.TECHNIQUE` when no key is stored
  - **Commit**: `feat(app): add DifficultyMode enum and AppPreferences mode persistence`
  - _Requirements: FR-3, AC-1.3, AC-1.4_
  - _Design: DifficultyMode, AppPreferences components_

---

- [x] 3.3 Update `GameViewModel` to dispatch `PuzzleDifficulty` through `launchGeneration` and `RequestHint`
  - **Do**:
    1. Update `launchGeneration(difficulty: PuzzleDifficulty)` — `when (difficulty)` dispatch: `is PuzzleDifficulty.Technique` calls `Generator.generate(difficulty.grade)`; `is PuzzleDifficulty.Given` calls `Generator.generateByGivenCount(difficulty.grade)`
    2. In the `RequestHint` handler, pass `state.difficulty` (already `PuzzleDifficulty`) directly to `HintEngine.findHint(board, state.difficulty)` — no cast needed
    3. Fix `GameViewModelTest.kt`: update all references to `GameState.difficulty` or `StartNewGame(difficulty = Difficulty.*)` to wrap with `PuzzleDifficulty.Technique(...)`; check `app/src/test/kotlin/sudoku/app/` for any other test files referencing `Difficulty` directly and update them
  - **Files**:
    - `app/src/main/kotlin/sudoku/app/state/GameViewModel.kt` (modify)
    - `app/src/test/kotlin/sudoku/app/state/GameViewModelTest.kt` (modify)
  - **Done when**: `./gradlew :app:compileKotlin` passes and any existing ViewModel tests still pass (run with `./gradlew :engine:test` — note: app tests may use different task; verify ViewModel test runner)
  - **Commit**: `feat(app): update GameViewModel to dispatch PuzzleDifficulty for generation and hints`
  - _Requirements: FR-1, FR-10, AC-3.6_
  - _Design: GameViewModel component_

---

- [x] 3.4 Add all new i18n string keys to `Strings`, `EnglishStrings`, and `RussianStrings`
  - **Do**:
    1. In `Strings.kt`, add to the interface: `val modeTechnique: String`, `val modeGiven: String`, `val hintNakedTriple: String`, `val hintHiddenTriple: String`, `val hintSwordfish: String`, `val hintExplainNakedTriple: (String, String, String, Int, Int, Int) -> String`, `val hintExplainHiddenTriple: (String, String, String, Int, Int, Int) -> String`, `val hintExplainSwordfish: (Int) -> String`
    2. In `EnglishStrings.kt`, add values: `modeTechnique = "Technique"`, `modeGiven = "Given Count"`, `hintNakedTriple = "Naked Triple"`, `hintHiddenTriple = "Hidden Triple"`, `hintSwordfish = "Swordfish"`, and the three lambda explanations per the design
    3. In `RussianStrings.kt`, add corresponding Russian values for all eight keys (use: `modeTechnique = "Техника"`, `modeGiven = "Количество подсказок"`, `hintNakedTriple = "Открытая тройка"`, `hintHiddenTriple = "Скрытая тройка"`, `hintSwordfish = "Рыба-меч"`, and Russian explanation lambdas)
  - **Files**:
    - `app/src/main/kotlin/sudoku/app/ui/i18n/Strings.kt` (modify)
    - `app/src/main/kotlin/sudoku/app/ui/i18n/EnglishStrings.kt` (modify)
    - `app/src/main/kotlin/sudoku/app/ui/i18n/RussianStrings.kt` (modify)
  - **Done when**: `./gradlew :app:compileKotlin` passes; both locale objects satisfy the updated `Strings` interface at compile time (no missing implementations); no hardcoded English strings in new UI code
  - **Commit**: `feat(app): add i18n keys for difficulty modes and triple/swordfish hints`
  - _Requirements: FR-9, AC-1.5, AC-5.1–AC-5.4, NFR-5_
  - _Design: i18n Changes section_

---

## Phase 4 — Polish (UI wiring + HintBanner + final verification)

Steps 11–13. Depends on Phase 3 being complete.

---

- [ ] 4.1 Update `HomeScreen` with mode toggle and `PuzzleDifficulty` emission; wire mode in `App.kt`
  - **Do**:
    1. Add `currentMode: DifficultyMode` and `onModeChange: (DifficultyMode) -> Unit` parameters to `HomeScreen` composable signature alongside the existing `onDifficultySelected`, `currentLocale`, and `onLocaleChange` params
    2. Add mode toggle UI above the difficulty buttons — two `TextButton`s (Technique / Given) styled with `alpha()` modifier matching the existing locale toggle pattern; labels from `strings.modeTechnique` / `strings.modeGiven`
    3. Change the four difficulty buttons to emit `PuzzleDifficulty` via `onDifficultySelected`: when `currentMode == TECHNIQUE`, emit `PuzzleDifficulty.Technique(Difficulty.*)` for each button; when `currentMode == GIVEN`, emit `PuzzleDifficulty.Given(GivenGrade.*)` (same label order: Easy→EASY, Medium→MEDIUM, Hard→HARD, Expert→EXPERT)
    4. In `App.kt` (or the main composition entry point), load mode with `AppPreferences.loadMode()` into a `remember` state on startup; pass it as `currentMode`; on `onModeChange`, call `AppPreferences.saveMode(mode)` and update state
    5. Update any `HomeScreen` call sites in `App.kt` to supply the new parameters
  - **Files**:
    - `app/src/main/kotlin/sudoku/app/ui/HomeScreen.kt` (modify)
    - `app/src/main/kotlin/sudoku/app/state/App.kt` (modify)
  - **Done when**: `./gradlew :app:compileKotlin` passes; `HomeScreen` signature matches design; `onDifficultySelected` callback receives `PuzzleDifficulty` (verified by reading call site in App.kt)
  - **Commit**: `feat(app): add difficulty mode toggle to HomeScreen and wire AppPreferences persistence`
  - _Requirements: FR-2, FR-3, AC-1.1–AC-1.5, AC-2.1_
  - _Design: HomeScreen, AppPreferences components_

---

- [ ] 4.2 Update `HintBanner` to render `HintExplanationData.Triple` and `HintExplanationData.Swordfish`
  - **Do**:
    1. In the `when(explanationData)` block of `HintBanner.kt`, add a branch for `is HintExplanationData.Triple` — display `strings.hintNakedTriple` or `strings.hintHiddenTriple` label (determine which by checking if the triple is a naked or hidden variant; if `HintResult.Found` carries the technique type, use it; otherwise add a `isHidden: Boolean` field to `Triple` or use a separate `HiddenTriple` subclass if design calls for it — follow whichever pattern `Pair` uses) plus the explanation via `strings.hintExplainNakedTriple` / `strings.hintExplainHiddenTriple`
    2. Add a branch for `is HintExplanationData.Swordfish` — display `strings.hintSwordfish` label plus `strings.hintExplainSwordfish(explanationData.digit)`
    3. Ensure no `when` branch is left without a handler (add `else -> {}` only if the sealed class is not exhaustively sealed — it is sealed, so the compiler will enforce exhaustiveness)
  - **Files**:
    - `app/src/main/kotlin/sudoku/app/ui/components/HintBanner.kt` (modify)
  - **Done when**: `./gradlew :app:compileKotlin` passes; `when(explanationData)` in HintBanner has branches for Triple and Swordfish with no compile warnings about missing cases
  - **Commit**: `feat(app): render Triple and Swordfish hint explanations in HintBanner`
  - _Requirements: AC-4.6_
  - _Design: HintBanner, HintExplanationData subclasses_

---

- [ ] 4.3 Full build verification and engine test pass
  - **Do**:
    1. Run `./gradlew :engine:test` — all engine tests must pass (GraderTest, GeneratorTest, HintEngineTest, and any others)
    2. Run `./gradlew :app:compileKotlin` — app module must compile cleanly with zero errors
    3. Fix any remaining compile errors or test failures found in steps 1–2
    4. Verify `ExternalPuzzleGraderTest` output reflects updated grades (at least some puzzles now grade MEDIUM or higher per AC-4.7) by checking test output — it only prints, no assertions, so this is a manual log review step
  - **Files**: Any files still failing compilation or tests
  - **Done when**: `./gradlew :engine:test` exits 0 with all tests passing; `./gradlew :app:compileKotlin` exits 0; no test regressions vs pre-spec baseline
  - **Commit**: `fix(engine,app): resolve remaining compile errors and test failures`
  - _Requirements: NFR-2, NFR-3, NFR-5_
  - _Design: Test Strategy_

---

## Notes

- **Dependency order**: Phase 1 tasks (1.1, 1.2) are independent of each other and can run in parallel. Phase 2 task 2.1 requires Grader changes from 1.2 to be complete (ceilingExceeded PAIRS branch depends on the new 4-tier ladder). Phase 2 task 2.2 requires PuzzleDifficulty from 1.1. Phase 3 tasks 3.1 and 3.2 can run in parallel after Phase 1; 3.3 and 3.4 can run in parallel after 3.1/3.2. Phase 4 tasks require all of Phase 3.
- **Key visibility change**: `Grader.computeCandidates` must be changed from `private` to `internal` in task 1.2 before Generator's `ceilingExceeded` can compile (task 2.1).
- **hardPuzzle replacement**: The existing `GraderTest` reference puzzle for HARD will grade as MEDIUM after moving Pointing Pairs to Tier 2. Task 1.2 must supply a replacement puzzle that genuinely requires Naked/Hidden Triples. Finding such a puzzle may require checking known resources (sudokuwiki.org publishes triples examples) or running the new Grader against test data.
- **`NoHintForDifficulty` contract**: For Given-mode, the hint ceiling hit must return `HintResult.NoHint`, never `NoHintForDifficulty`. This is enforced in `findHintForGiven` (task 2.2) and verified in HintEngineTest.
- **Slow tests**: `generateByGivenCount` EXPERT tests may be slow. Mark with `@Disabled` consistent with the existing Generator test precedent if they exceed reasonable CI time.
- **Local verification**: `./gradlew :engine:test` — the primary quality gate for this spec.