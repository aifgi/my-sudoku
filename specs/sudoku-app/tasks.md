---
spec: sudoku-app
phase: tasks
created: 2026-05-26
---

# Tasks: Sudoku Desktop App

**Workflow**: POC-first (GREENFIELD)
**Granularity**: Fine (40-60+ atomic tasks)
**E2E verification**: Disabled (desktop GUI — no HTTP endpoint to automate)
**Execution priority**: Quality-first — engine proven correct before UI wired

---

## Phase 1: Make It Work (POC)

**Focus**: Build a playable game end-to-end. Engine must compile and pass basic manual play. Skip UI polish, accept hardcoded values, defer tests to Phase 3.

**Phase 1 ordering** (quality-first POC):
1. Gradle scaffolding
2. Engine data types (Cell, Board, Difficulty)
3. Solver
4. Generator
5. Grader
6. HintEngine + HintResult
7. MVI state layer (GameState, GameIntent, GameViewModel)
8. App entry point + HomeScreen
9. SudokuBoard Canvas composable
10. GameScreen layout + keyboard handler
11. NumberPad, TimerDisplay, HintBanner
12. CompletionOverlay, PauseOverlay, quit confirmation
13. Wire App.kt navigation
14. POC checkpoint

---

### 1.1 Create root Gradle settings and version catalog ✅

- **Do**:
  1. Create `settings.gradle.kts` at project root with `rootProject.name = "sudoku"` and `include(":engine", ":app")`
  2. Create `gradle/libs.versions.toml` with versions and library aliases from design.md
- **Files**: `settings.gradle.kts`, `gradle/libs.versions.toml`
- **Done when**: File exists, root project named `sudoku`, both submodules declared
- **Verify**: `grep -q 'include(":engine", ":app")' settings.gradle.kts && echo PASS`
- **Commit**: `chore(build): initialize Gradle multi-module root and version catalog`
- _Requirements: AC-8.1–AC-8.4_
- _Design: Gradle Build Files_

---

### 1.2 [P] Create engine/build.gradle.kts ✅

- **Do**:
  1. Create `engine/` directory
  2. Create `engine/build.gradle.kts` with `kotlin("jvm")` plugin, JVM toolchain 21, JUnit Jupiter test dependency and `useJUnitPlatform()` as per design.md
- **Files**: `engine/build.gradle.kts`
- **Done when**: File exists with correct plugin, toolchain, and test config
- **Verify**: `grep -q 'useJUnitPlatform' engine/build.gradle.kts && echo PASS`
- **Commit**: `chore(engine): add engine Gradle build file`
- _Design: Gradle Build Files_

---

### 1.3 [P] Create app/build.gradle.kts

- **Do**:
  1. Create `app/` directory
  2. Create `app/build.gradle.kts` with Compose Desktop plugin, Kotlin JVM plugin, `project(":engine")` dependency, coroutines, and `compose.desktop` block with `mainClass = "sudoku.app.MainKt"` and `nativeDistributions` for Dmg/Msi/Deb as per design.md
- **Files**: `app/build.gradle.kts`
- **Done when**: File exists with all plugins, dependencies, and distribution config
- **Verify**: `grep -q 'mainClass = "sudoku.app.MainKt"' app/build.gradle.kts && echo PASS`
- **Commit**: `chore(app): add app Gradle build file with Compose Desktop plugin`
- _Design: Gradle Build Files_

---

### V1 [VERIFY] Quality checkpoint: Gradle sync

- **Do**: Verify Gradle can resolve all modules and download dependencies without error
- **Files**: n/a
- **Verify**: `./gradlew tasks --all 2>&1 | grep -q 'engine\|app' && echo V1_PASS`
- **Done when**: Gradle tasks list shows tasks for both `:engine` and `:app` subprojects
- **Commit**: `chore(build): fix Gradle sync issues` (only if fixes needed)

---

### 1.4 Create engine source directory skeleton

- **Do**:
  1. Create directory `engine/src/main/kotlin/sudoku/engine/`
  2. Create directory `engine/src/test/kotlin/sudoku/engine/`
  3. Create placeholder `.gitkeep` in each if needed (directories must exist)
- **Files**: `engine/src/main/kotlin/sudoku/engine/`, `engine/src/test/kotlin/sudoku/engine/`
- **Done when**: Both directories exist on disk
- **Verify**: `ls engine/src/main/kotlin/sudoku/engine/ && ls engine/src/test/kotlin/sudoku/engine/ && echo PASS`
- **Commit**: `chore(engine): create source directory skeleton`

---

### 1.5 Implement Cell.kt with index extension properties and peersOf

- **Do**:
  1. Create `engine/src/main/kotlin/sudoku/engine/Cell.kt`
  2. Implement `data class Cell(val index: Int, val digit: Int, val isGiven: Boolean)`
  3. Add extension properties on `Int`: `val Int.row`, `val Int.col`, `val Int.box` (formulae from design.md)
  4. Implement `fun peersOf(index: Int): IntArray` returning the 20 peers for any cell index (union of row, col, box peers minus the cell itself)
  5. Add module-level `val PEERS: Array<IntArray>` pre-computed for all 81 indices at initialization
- **Files**: `engine/src/main/kotlin/sudoku/engine/Cell.kt`
- **Done when**: `PEERS[0].size == 20`, `PEERS[40].size == 20`, no duplicates, cell itself absent
- **Verify**: `./gradlew :engine:compileKotlin`
- **Commit**: `feat(engine): implement Cell data class, index extension props, PEERS lookup table`
- _Requirements: FR-001_
- _Design: Board Model_

---

### 1.6 Implement Difficulty.kt enum

- **Do**:
  1. Create `engine/src/main/kotlin/sudoku/engine/Difficulty.kt`
  2. Define `enum class Difficulty { EASY, MEDIUM, HARD, EXPERT }` exactly as in design.md
- **Files**: `engine/src/main/kotlin/sudoku/engine/Difficulty.kt`
- **Done when**: Enum compiles with 4 variants
- **Verify**: `./gradlew :engine:compileKotlin`
- **Commit**: `feat(engine): add Difficulty enum`
- _Requirements: FR-003_
- _Design: Difficulty Enum_

---

### 1.7 Implement Board.kt flat array model

- **Do**:
  1. Create `engine/src/main/kotlin/sudoku/engine/Board.kt`
  2. Implement `class Board private constructor(val digits: IntArray, val givens: BooleanArray, val candidates: IntArray)`
  3. Add companion object with `fun fromDigits(digits: IntArray, givens: BooleanArray): Board` — computes candidate bitmasks (bits 1–9) from peer digits
  4. Add `fun empty(): Board` returning all-zero board
  5. Add `fun withDigit(index: Int, digit: Int): Board` — returns new Board copy with digit set and candidates updated for the cell and its peers
  6. Add `fun withErased(index: Int): Board` — returns new Board copy with digit zeroed, candidates recomputed
  7. Add `val isEmpty: Boolean` and `val isFull: Boolean` computed properties
  8. Add module-level `val ROW_UNITS`, `COL_UNITS`, `BOX_UNITS`, `ALL_UNITS: Array<IntArray>` constants
- **Files**: `engine/src/main/kotlin/sudoku/engine/Board.kt`
- **Done when**: All methods compile; `Board.empty().isEmpty == true`; `withDigit` returns new instance (referential inequality)
- **Verify**: `./gradlew :engine:compileKotlin`
- **Commit**: `feat(engine): implement Board flat IntArray model with bitmask candidates`
- _Requirements: FR-001, FR-008_
- _Design: Board Model_

---

### V2 [VERIFY] Quality checkpoint: engine data types compile

- **Do**: Compile engine module; verify Cell, Board, Difficulty all resolve without errors
- **Files**: n/a
- **Verify**: `./gradlew :engine:compileKotlin 2>&1 | grep -c 'error:' | grep -q '^0$' && echo V2_PASS`
- **Done when**: Zero compile errors
- **Commit**: `chore(engine): fix data type compile issues` (only if fixes needed)

---

### 1.8 Implement Solver.kt — constraint propagation + MRV backtracking

- **Do**:
  1. Create `engine/src/main/kotlin/sudoku/engine/Solver.kt`
  2. Implement `object Solver` with `fun solve(board: Board): IntArray?` — constraint propagation seeded from givens, then DFS backtracking on MRV cell (fewest candidates); returns null if unsolvable
  3. Implement `fun countSolutions(digits: IntArray, limit: Int = 2): Int` — DFS with early-exit when count reaches limit; uses MRV heuristic on working copy; returns 0, 1, or limit (capped)
  4. Private helper: `fun pickMRV(digits: IntArray): Int` — returns index of non-empty cell with fewest valid placements, or -1 when board is complete
  5. Private helper: `fun isValidPlacement(digits: IntArray, index: Int, digit: Int): Boolean` — checks row, col, box peers via PEERS
- **Files**: `engine/src/main/kotlin/sudoku/engine/Solver.kt`
- **Done when**: `Solver.solve(Board.empty()) != null`; `countSolutions` returns ≤ limit
- **Verify**: `./gradlew :engine:compileKotlin`
- **Commit**: `feat(engine): implement Solver with constraint propagation and MRV backtracking`
- _Requirements: FR-001, FR-002_
- _Design: Solver_

---

### 1.9 Implement Generator.kt — fillGrid and digHoles

- **Do**:
  1. Create `engine/src/main/kotlin/sudoku/engine/Generator.kt`
  2. Implement `object Generator` with `suspend fun generate(difficulty: Difficulty): Board`
  3. Implement private `fun fillGrid(): IntArray?` — Las Vegas randomized backtracking: shuffle `(1..9)` at each cell, recurse with `backtrack(digits, index + 1)`
  4. Implement private `fun digHoles(solution: IntArray, difficulty: Difficulty): IntArray?` — shuffle 0..80 indices; remove each cell, run `countSolutions(puzzle, 2)`; restore if not unique; grade result; return null if grade mismatches
  5. Retry loop: up to 100 attempts; throw `IllegalStateException` if exhausted
- **Files**: `engine/src/main/kotlin/sudoku/engine/Generator.kt`
- **Done when**: `Generator.generate(Difficulty.EASY)` returns a board with `countSolutions == 1` (verified in test after Phase 3; for now, compile-only)
- **Verify**: `./gradlew :engine:compileKotlin`
- **Commit**: `feat(engine): implement Generator with Las Vegas fill and dig-holes`
- _Requirements: FR-001, FR-002, FR-003_
- _Design: Generator_

---

### V3 [VERIFY] Quality checkpoint: Solver + Generator compile

- **Do**: Compile engine module; confirm no unresolved references in Solver or Generator
- **Files**: n/a
- **Verify**: `./gradlew :engine:compileKotlin 2>&1 | grep -c 'error:' | grep -q '^0$' && echo V3_PASS`
- **Done when**: Zero compile errors in Solver.kt and Generator.kt
- **Commit**: `chore(engine): fix Solver/Generator compile issues` (only if fixes needed)

---

### 1.10 Implement Grader.kt — technique-threshold human solver

- **Do**:
  1. Create `engine/src/main/kotlin/sudoku/engine/Grader.kt`
  2. Implement `object Grader` with `fun grade(puzzle: IntArray): Difficulty`
  3. Implement a technique-constrained non-backtracking human solver loop with configurable technique set
  4. Technique functions: `applyNakedSingles(candidates)`, `applyHiddenSingles(candidates)`, `applyNakedPairs(candidates)`, `applyHiddenPairs(candidates)`, `applyPointingPairs(candidates)`, `applyXWing(candidates)` — each returns true if progress made
  5. Grade ladder: try Easy techniques → stalled? try Medium → stalled? try Hard → stalled? try Expert → grade EXPERT
- **Files**: `engine/src/main/kotlin/sudoku/engine/Grader.kt`
- **Done when**: File compiles; all four difficulty grades reachable via the technique ladder
- **Verify**: `./gradlew :engine:compileKotlin`
- **Commit**: `feat(engine): implement Grader with technique-threshold human solver`
- _Requirements: FR-003, FR-004, FR-005, FR-006, FR-007_
- _Design: Grader_

---

### 1.11 Implement HintResult.kt sealed class

- **Do**:
  1. Create `engine/src/main/kotlin/sudoku/engine/HintResult.kt`
  2. Implement `sealed class HintResult` with three variants:
     - `data class Found(val technique: String, val targetCells: List<Int>, val peerCells: List<Int>, val explanation: String)`
     - `object NoHint`
     - `object NoHintForDifficulty`
- **Files**: `engine/src/main/kotlin/sudoku/engine/HintResult.kt`
- **Done when**: Sealed class compiles; `when(result)` is exhaustive with the three branches
- **Verify**: `./gradlew :engine:compileKotlin`
- **Commit**: `feat(engine): add HintResult sealed class with Found/NoHint/NoHintForDifficulty variants`
- _Requirements: FR-010, AC-4.4, AC-4.6, AC-9.1_
- _Design: HintEngine_

---

### 1.12 Implement HintEngine.kt — five-technique cascade

- **Do**:
  1. Create `engine/src/main/kotlin/sudoku/engine/HintEngine.kt`
  2. Implement `object HintEngine` with `fun findHint(board: Board, difficulty: Difficulty): HintResult`
  3. Private `fun computeAllCandidates(board: Board): IntArray` — recompute from current board digits using PEERS
  4. Implement technique checks in priority order (each returns `HintResult.Found` or null):
     - `fun nakedSingle(board, candidates): HintResult.Found?`
     - `fun hiddenSingle(board, candidates): HintResult.Found?` — iterate ALL_UNITS, find digit with count==1
     - `fun nakedPair(board, candidates): HintResult.Found?` — pair of cells same 2-candidate bitmask in a unit
     - `fun hiddenPair(board, candidates): HintResult.Found?` — two digits confined to same 2 cells in a unit
     - `fun pointingPair(board, candidates): HintResult.Found?` — digit in box confined to one row or col
  5. Fall-through logic: if no technique found, return `NoHintForDifficulty` for HARD/EXPERT, else `NoHint`
  6. Private helpers: `fun cellName(index: Int): String` (e.g. "R3C5"), `fun unitName(unit: IntArray, index: Int): String`
- **Files**: `engine/src/main/kotlin/sudoku/engine/HintEngine.kt`
- **Done when**: Compiles; `when(HintEngine.findHint(...))` exhaustively covers all `HintResult` variants
- **Verify**: `./gradlew :engine:compileKotlin`
- **Commit**: `feat(engine): implement HintEngine five-technique cascade with NoHintForDifficulty`
- _Requirements: FR-010, AC-4.2, AC-4.4, AC-4.8, AC-9.1, AC-9.4_
- _Design: HintEngine_

---

### V4 [VERIFY] Quality checkpoint: full engine compiles

- **Do**: Compile full engine module; all 8 source files (Cell, Board, Difficulty, Solver, Generator, Grader, HintResult, HintEngine) must resolve without error
- **Files**: n/a
- **Verify**: `./gradlew :engine:compileKotlin 2>&1 | grep -c 'error:' | grep -q '^0$' && echo V4_PASS`
- **Done when**: Zero compile errors across all engine source files
- **Commit**: `chore(engine): fix compile errors across engine module` (only if fixes needed)

---

### 1.13 Create app source directory skeleton

- **Do**:
  1. Create `app/src/main/kotlin/sudoku/app/`
  2. Create `app/src/main/kotlin/sudoku/app/state/`
  3. Create `app/src/main/kotlin/sudoku/app/ui/`
  4. Create `app/src/main/kotlin/sudoku/app/ui/components/`
- **Files**: Directory structure under `app/src/main/kotlin/sudoku/app/`
- **Done when**: All four directories exist
- **Verify**: `ls app/src/main/kotlin/sudoku/app/state/ && ls app/src/main/kotlin/sudoku/app/ui/components/ && echo PASS`
- **Commit**: `chore(app): create app source directory skeleton`

---

### 1.14 Implement GameState.kt

- **Do**:
  1. Create `app/src/main/kotlin/sudoku/app/state/GameState.kt`
  2. Implement `data class GameState` with all 20 fields from design.md (digits, givens, conflictIndices, selectedIndex, numberHighlightDigit, hintResult, undoStack, redoStack, timerSeconds, isPaused, isComplete, difficulty, isLoading, pendingDifficulty, showNewGameConfirmation, newGameTargetDifficulty, showQuitConfirmation)
  3. Add `companion object { val Initial = GameState(...) }` with all-zero/null/empty defaults
- **Files**: `app/src/main/kotlin/sudoku/app/state/GameState.kt`
- **Done when**: Compiles; `GameState.Initial` references all fields with correct types
- **Verify**: `./gradlew :app:compileKotlin`
- **Commit**: `feat(state): implement GameState immutable data class with Initial companion`
- _Requirements: FR-008, FR-009, FR-011, FR-012, FR-013_
- _Design: GameState_

---

### 1.15 Implement GameIntent.kt

- **Do**:
  1. Create `app/src/main/kotlin/sudoku/app/state/GameIntent.kt`
  2. Implement `sealed class GameIntent` with all 16 variants from design.md: StartNewGame, ConfirmNewGame, CancelNewGame, ShowQuitConfirmation, ConfirmQuit, CancelQuit, PuzzleGenerated, SelectCell, DeselectCell, EnterDigit, EraseCell, Undo, Redo, RequestHint, TogglePause, TimerTick, GameCompleted
- **Files**: `app/src/main/kotlin/sudoku/app/state/GameIntent.kt`
- **Done when**: Sealed class compiles; all 16 variants are `data class` or `data object` as specified
- **Verify**: `./gradlew :app:compileKotlin`
- **Commit**: `feat(state): implement GameIntent sealed class with 16 intent variants`
- _Requirements: FR-008, FR-009, FR-011, FR-012, FR-013, AC-7.6_
- _Design: GameIntent_

---

### V5 [VERIFY] Quality checkpoint: state layer compiles

- **Do**: Compile app module; confirm GameState and GameIntent resolve with correct engine type references (Difficulty, Board, HintResult)
- **Files**: n/a
- **Verify**: `./gradlew :app:compileKotlin 2>&1 | grep -c 'error:' | grep -q '^0$' && echo V5_PASS`
- **Done when**: Zero compile errors in state layer
- **Commit**: `chore(state): fix state layer compile issues` (only if fixes needed)

---

### 1.16 Implement GameViewModel.kt — reduce() pure function

- **Do**:
  1. Create `app/src/main/kotlin/sudoku/app/state/GameViewModel.kt`
  2. Implement `class GameViewModel(private val coroutineScope: CoroutineScope)`
  3. Add `private val _state = MutableStateFlow(GameState.Initial)` and `val state: StateFlow<GameState> = _state.asStateFlow()`
  4. Implement `fun dispatch(intent: GameIntent)` calling `_state.update { reduce(it, intent) }` then `handleSideEffects(intent)`
  5. Implement `private fun reduce(state: GameState, intent: GameIntent): GameState` — exhaustive `when(intent)` covering all 16 variants:
     - `StartNewGame`: set `isLoading=true`, `pendingDifficulty`, clear undo/redo, hint
     - `PuzzleGenerated`: populate digits/givens from board, reset timer, `isLoading=false`, `isComplete=false`
     - `SelectCell`: set `selectedIndex`, compute `numberHighlightDigit` (digit at index or null if 0)
     - `DeselectCell`: clear `selectedIndex`, `numberHighlightDigit`
     - `EnterDigit`: guard `!givens[selectedIndex]`; push to undoStack; update digits; recompute `conflictIndices` via `computeConflicts`; check completion
     - `EraseCell`: guard `!givens[selectedIndex]`; push to undoStack; update digits; recompute conflicts
     - `Undo`: pop undoStack → digits; push current to redoStack; recompute conflicts
     - `Redo`: pop redoStack → digits; push current to undoStack; recompute conflicts
     - `RequestHint`: call `HintEngine.findHint(board, difficulty)`; set `hintResult`; clear `numberHighlightDigit`
     - `TogglePause`: toggle `isPaused`; clear `numberHighlightDigit`
     - `TimerTick`: if `!isPaused && !isComplete && !isLoading` increment `timerSeconds`
     - `GameCompleted`: set `isComplete=true`
     - Confirmation dialog intents: set/clear `showNewGameConfirmation`, `showQuitConfirmation` flags
  6. Add private `fun computeConflicts(digits: IntArray): Set<Int>` — O(27×9) scan via ALL_UNITS
  7. Add private `fun checkCompletion(state: GameState): GameState` — if `isFull && conflictIndices.isEmpty` auto-dispatch `GameCompleted`
- **Files**: `app/src/main/kotlin/sudoku/app/state/GameViewModel.kt`
- **Done when**: Compiles; `reduce()` is exhaustive (no non-exhaustive `when` warnings)
- **Verify**: `./gradlew :app:compileKotlin`
- **Commit**: `feat(state): implement GameViewModel with pure reduce() and MVI dispatch`
- _Requirements: FR-008, FR-009, FR-011, FR-012, FR-013, FR-016, FR-019, AC-3.1–3.7, AC-5.1–5.6, AC-7.6_
- _Design: GameViewModel_

---

### 1.17 Implement GameViewModel.kt — side effects (generation, timer)

- **Do**:
  1. In `GameViewModel.kt`, add `private var timerJob: Job? = null` and `private var generationJob: Job? = null`
  2. Implement `private fun handleSideEffects(intent: GameIntent)` dispatching to:
     - `StartNewGame` → `launchGeneration(intent.difficulty)`
     - `TogglePause` → `syncTimer()` (cancel or restart timer based on current isPaused state)
     - `PuzzleGenerated` → `startTimer()`
     - `GameCompleted` → `timerJob?.cancel()`
  3. Implement `private fun launchGeneration(difficulty: Difficulty)` — cancel existing job; launch coroutine; `withContext(Dispatchers.Default) { Generator.generate(difficulty) }`; dispatch `PuzzleGenerated`; rethrow `CancellationException`
  4. Implement `private fun startTimer()` — cancel existing; launch while-loop coroutine; `delay(1_000)`; check state; dispatch `TimerTick`
  5. Implement `private fun syncTimer()` — if `!isPaused && !isComplete`, start timer; else cancel
- **Files**: `app/src/main/kotlin/sudoku/app/state/GameViewModel.kt`
- **Done when**: Compiles; side-effect functions reference correct coroutine APIs
- **Verify**: `./gradlew :app:compileKotlin`
- **Commit**: `feat(state): add coroutine side-effects for generation and timer to GameViewModel`
- _Requirements: FR-001, FR-012, NFR-002, NFR-003_
- _Design: GameViewModel, Async & Concurrency_

---

### V6 [VERIFY] Quality checkpoint: app module compiles

- **Do**: Compile entire app module including ViewModel; confirm coroutine imports resolve
- **Files**: n/a
- **Verify**: `./gradlew :app:compileKotlin 2>&1 | grep -c 'error:' | grep -q '^0$' && echo V6_PASS`
- **Done when**: Zero compile errors in state directory
- **Commit**: `chore(state): fix ViewModel coroutine compile issues` (only if fixes needed)

---

### 1.18 Implement Main.kt — Compose Desktop entry point with window config

- **Do**:
  1. Create `app/src/main/kotlin/sudoku/app/Main.kt`
  2. Implement `fun main()` using `application { ... }` Compose Desktop entry
  3. Create `GameViewModel` with `rememberCoroutineScope()`
  4. Add `Window` with `title = "Sudoku"`, `state = rememberWindowState(width = 700.dp, height = 800.dp)`, `minimumSize = DpSize(600.dp, 700.dp)`
  5. Add `onCloseRequest` handler: if `undoStack.isNotEmpty() && !isComplete` dispatch `ShowQuitConfirmation`, else `exitApplication()`
  6. Call `App(viewModel)` as window content
- **Files**: `app/src/main/kotlin/sudoku/app/Main.kt`
- **Done when**: Compiles; `application` block and `Window` reference correct Compose Desktop APIs
- **Verify**: `./gradlew :app:compileKotlin`
- **Commit**: `feat(app): implement Main.kt with window config, min size, and quit handler`
- _Requirements: AC-7.6, AC-8.5, AC-8.6, AC-8.7, NFR-008_
- _Design: Error Handling, Quit Confirmation_

---

### 1.19 Implement App.kt — navigation between HomeScreen and GameScreen

- **Do**:
  1. Create `app/src/main/kotlin/sudoku/app/ui/App.kt`
  2. Implement `@Composable fun App(viewModel: GameViewModel)`
  3. Collect `viewModel.state.collectAsState()` and show:
     - `HomeScreen` when `!state.isLoading && state.digits.all { it == 0 } && !state.isComplete && state.undoStack.isEmpty()`
     - `CircularProgressIndicator` when `state.isLoading`
     - `GameScreen` otherwise
  4. Pass `viewModel::dispatch` lambda down (no ViewModel reference in composables)
  5. Show `AlertDialog` for quit confirmation when `state.showQuitConfirmation == true` (Confirm → `dispatch(ConfirmQuit)` + `exitApplication()`, Cancel → `dispatch(CancelQuit)`)
- **Files**: `app/src/main/kotlin/sudoku/app/ui/App.kt`
- **Done when**: Compiles; navigation logic covers all three screen states
- **Verify**: `./gradlew :app:compileKotlin`
- **Commit**: `feat(app): implement App.kt navigation composable with loading and quit dialog`
- _Requirements: AC-1.1, AC-7.6_
- _Design: Screen Flow_

---

### 1.20 Implement HomeScreen.kt — difficulty picker

- **Do**:
  1. Create `app/src/main/kotlin/sudoku/app/ui/HomeScreen.kt`
  2. Implement `@Composable fun HomeScreen(onDifficultySelected: (Difficulty) -> Unit)`
  3. Layout: `Column(horizontalAlignment = CenterHorizontally, verticalArrangement = Center)` with title `Text("Sudoku", style = MaterialTheme.typography.headlineLarge)` and four `Button` composables for Easy/Medium/Hard/Expert
  4. Each button calls `onDifficultySelected(difficulty)` which maps to `dispatch(GameIntent.StartNewGame(difficulty))`
- **Files**: `app/src/main/kotlin/sudoku/app/ui/HomeScreen.kt`
- **Done when**: Compiles; four buttons render; each fires the callback
- **Verify**: `./gradlew :app:compileKotlin`
- **Commit**: `feat(app): implement HomeScreen difficulty picker`
- _Requirements: AC-1.1, AC-1.2_
- _Design: HomeScreen_

---

### V7 [VERIFY] Quality checkpoint: entry point + nav compiles

- **Do**: Compile app module through Main.kt, App.kt, HomeScreen.kt
- **Files**: n/a
- **Verify**: `./gradlew :app:compileKotlin 2>&1 | grep -c 'error:' | grep -q '^0$' && echo V7_PASS`
- **Done when**: Zero compile errors in entry + nav files
- **Commit**: `chore(app): fix entry/nav compile issues` (only if fixes needed)

---

### 1.21 Implement SudokuBoard.kt — Canvas cell background layers

- **Do**:
  1. Create `app/src/main/kotlin/sudoku/app/ui/components/SudokuBoard.kt`
  2. Implement `@Composable fun SudokuBoard(state: GameState, onCellClick: (index: Int) -> Unit)`
  3. Set up `Canvas(Modifier.fillMaxWidth().aspectRatio(1f).pointerInput(Unit) { detectTapGestures { ... } })`
  4. Implement `fun offsetToIndex(offset: Offset, size: Size): Int` — converts tap offset to 0..80 cell index
  5. Implement `fun DrawScope.drawCells(state: GameState, cellSize: Float)` — per-cell back-to-front layers:
     - Layer 1: normal background (white for user cells, light gray `Color(0xFFF5F5F5)` for givens)
     - Layer 2: number-match overlay amber `Color(0xFFFFF3CD)` when `state.numberHighlightDigit == digit`
     - Layer 3: conflict overlay red `Color(0xFFFFCCCC)` when index in `state.conflictIndices`
     - Layer 4: selected overlay blue `Color(0xFFC5D8FF)` when `index == state.selectedIndex`
  6. Return early (blank canvas) if `state.isPaused` (PauseOverlay covers the board)
- **Files**: `app/src/main/kotlin/sudoku/app/ui/components/SudokuBoard.kt`
- **Done when**: Compiles; `offsetToIndex` returns correct index for known positions
- **Verify**: `./gradlew :app:compileKotlin`
- **Commit**: `feat(ui): implement SudokuBoard Canvas composable — cell background layers`
- _Requirements: FR-008, FR-009, FR-017, FR-018, AC-2.1, AC-2.2, AC-3.1–3.7_
- _Design: SudokuBoard Composable_

---

### 1.22 Implement SudokuBoard.kt — digit rendering and grid lines

- **Do**:
  1. In `SudokuBoard.kt`, implement `fun DrawScope.drawDigits(state: GameState, cellSize: Float)`:
     - For each non-zero cell: draw text centered in cell
     - Given cells: bold font weight, dark color `Color(0xFF1A1A2E)`
     - User cells: regular weight, mid-gray `Color(0xFF444466)`
  2. Implement `fun DrawScope.drawGrid(cellSize: Float)`:
     - Thin lines 0.5dp between cells: iterate rows 0..9 and cols 0..9
     - Thick lines 2dp between 3×3 boxes: lines at multiples of 3
     - Outer border: 2dp thick line around entire grid
  3. Implement `fun DrawScope.drawBorders(state: GameState, cellSize: Float)`:
     - Selected: 3dp solid blue `Color(0xFF4A90D9)` border inside cell rect
     - Conflict: 2dp dashed red `Color(0xFFCC3333)` border using `PathEffect.dashPathEffect`
     - Number-match crosshatch: thin diagonal lines at 20% opacity using `drawLine` pattern overlay
- **Files**: `app/src/main/kotlin/sudoku/app/ui/components/SudokuBoard.kt`
- **Done when**: Compiles; all draw functions called from Canvas `onDraw` block
- **Verify**: `./gradlew :app:compileKotlin`
- **Commit**: `feat(ui): add digit text, grid lines, and border decorations to SudokuBoard`
- _Requirements: FR-017, FR-018, NFR-012, AC-3.7_
- _Design: SudokuBoard Composable, Cell rendering table_

---

### V8 [VERIFY] Quality checkpoint: SudokuBoard compiles

- **Do**: Compile app with full SudokuBoard; confirm Canvas, DrawScope, PathEffect APIs resolve
- **Files**: n/a
- **Verify**: `./gradlew :app:compileKotlin 2>&1 | grep -c 'error:' | grep -q '^0$' && echo V8_PASS`
- **Done when**: Zero compile errors in SudokuBoard.kt
- **Commit**: `chore(ui): fix SudokuBoard Canvas API compile issues` (only if fixes needed)

---

### 1.23 Implement NumberPad.kt

- **Do**:
  1. Create `app/src/main/kotlin/sudoku/app/ui/components/NumberPad.kt`
  2. Implement `@Composable fun NumberPad(onDigit: (Int) -> Unit, onErase: () -> Unit)`
  3. Layout: `Row(Modifier.fillMaxWidth())` with 10 items — `Button` for digits 1–9 each with `Modifier.weight(1f).heightIn(min = 48.dp)`, plus `Button("Erase", ...)`
  4. Disable all buttons and show reduced alpha when game is loading or complete (pass `enabled` param)
- **Files**: `app/src/main/kotlin/sudoku/app/ui/components/NumberPad.kt`
- **Done when**: Compiles; buttons fire correct callbacks
- **Verify**: `./gradlew :app:compileKotlin`
- **Commit**: `feat(ui): implement NumberPad with digits 1-9 and Erase button`
- _Requirements: FR-014, AC-2.7_
- _Design: NumberPad Composable_

---

### 1.24 Implement TimerDisplay.kt

- **Do**:
  1. Create `app/src/main/kotlin/sudoku/app/ui/components/TimerDisplay.kt`
  2. Implement `@Composable fun TimerDisplay(seconds: Long, isPaused: Boolean)`
  3. Format: `MM:SS` when `seconds < 3600`, `HH:MM:SS` when `seconds >= 3600`
  4. Append `" (Paused)"` suffix when `isPaused == true`
  5. Display as `Text` with monospace font for stable width
- **Files**: `app/src/main/kotlin/sudoku/app/ui/components/TimerDisplay.kt`
- **Done when**: Compiles; format logic handles 0s, 59s, 3599s, 3600s correctly
- **Verify**: `./gradlew :app:compileKotlin`
- **Commit**: `feat(ui): implement TimerDisplay with MM:SS/HH:MM:SS format and paused suffix`
- _Requirements: FR-012, AC-6.1, AC-6.6_
- _Design: TimerDisplay Composable_

---

### 1.25 Implement HintBanner.kt

- **Do**:
  1. Create `app/src/main/kotlin/sudoku/app/ui/components/HintBanner.kt`
  2. Implement `@Composable fun HintBanner(hintResult: HintResult?)`
  3. When `hintResult == null`: render nothing (`return`)
  4. When `Found`: render `Row` with bold technique name + explanation text
  5. When `NoHint`: render "No hint available"
  6. When `NoHintForDifficulty`: render "No hint available for this difficulty level"
  7. Use `AnimatedVisibility` for smooth show/hide
- **Files**: `app/src/main/kotlin/sudoku/app/ui/components/HintBanner.kt`
- **Done when**: Compiles; `when(hintResult)` is exhaustive with all sealed variants
- **Verify**: `./gradlew :app:compileKotlin`
- **Commit**: `feat(ui): implement HintBanner with all HintResult display variants`
- _Requirements: AC-4.4, AC-4.6, AC-9.1, AC-9.2_
- _Design: HintBanner Composable_

---

### V9 [VERIFY] Quality checkpoint: NumberPad, TimerDisplay, HintBanner compile

- **Do**: Compile app module; verify all three component files resolve
- **Files**: n/a
- **Verify**: `./gradlew :app:compileKotlin 2>&1 | grep -c 'error:' | grep -q '^0$' && echo V9_PASS`
- **Done when**: Zero compile errors in components directory
- **Commit**: `chore(ui): fix component compile issues` (only if fixes needed)

---

### 1.26 Implement PauseOverlay.kt

- **Do**:
  1. Create `app/src/main/kotlin/sudoku/app/ui/components/PauseOverlay.kt`
  2. Implement `@Composable fun PauseOverlay(onResume: () -> Unit)`
  3. Full-opacity `Box(Modifier.fillMaxSize())` with `background(MaterialTheme.colorScheme.surface)` covering the board area
  4. Center content: `Text("Game Paused")` + `Button("Resume", onClick = onResume)`
- **Files**: `app/src/main/kotlin/sudoku/app/ui/components/PauseOverlay.kt`
- **Done when**: Compiles; overlay covers board, Resume button fires callback
- **Verify**: `./gradlew :app:compileKotlin`
- **Commit**: `feat(ui): implement PauseOverlay with resume button`
- _Requirements: AC-6.2, AC-6.3_
- _Design: PauseOverlay Composable_

---

### 1.27 Implement CompletionOverlay.kt

- **Do**:
  1. Create `app/src/main/kotlin/sudoku/app/ui/components/CompletionOverlay.kt`
  2. Implement `@Composable fun CompletionOverlay(difficulty: Difficulty, timerSeconds: Long, onNewGame: () -> Unit, onBackToHome: () -> Unit)`
  3. Semi-transparent `Box` at 80% opacity with `background(Color.Black.copy(alpha = 0.8f))` covering board
  4. Content: "Puzzle Solved!" header, difficulty label, formatted elapsed time, two buttons (New Game, Back to Home)
  5. Reuse `TimerDisplay` format logic for elapsed time display
- **Files**: `app/src/main/kotlin/sudoku/app/ui/components/CompletionOverlay.kt`
- **Done when**: Compiles; both action buttons fire correct callbacks
- **Verify**: `./gradlew :app:compileKotlin`
- **Commit**: `feat(ui): implement CompletionOverlay with puzzle-solved screen`
- _Requirements: AC-7.1, AC-7.2, AC-7.3, AC-7.5_
- _Design: CompletionOverlay Composable_

---

### V9b [VERIFY] Quality checkpoint: PauseOverlay and CompletionOverlay compile

- **Do**: Compile app module through PauseOverlay.kt and CompletionOverlay.kt
- **Files**: n/a
- **Verify**: `./gradlew :app:compileKotlin 2>&1 | grep -c 'error:' | grep -q '^0$' && echo V9b_PASS`
- **Done when**: Zero compile errors in overlay composables
- **Commit**: `chore(ui): fix overlay compile issues` (only if fixes needed)

---

### 1.28 Implement GameScreen.kt — layout skeleton

- **Do**:
  1. Create `app/src/main/kotlin/sudoku/app/ui/GameScreen.kt`
  2. Implement `@Composable fun GameScreen(state: GameState, onIntent: (GameIntent) -> Unit)`
  3. Root: `Column(Modifier.fillMaxSize())` with:
     - Top toolbar `Row`: New Game button, title "Sudoku", `TimerDisplay`, Pause button
     - Second toolbar `Row`: Undo button, Redo button (spacer), Hint button
     - Board area `Box(Modifier.weight(1f))`: `SudokuBoard` (or `CircularProgressIndicator` when loading), `PauseOverlay` if paused, `CompletionOverlay` if complete
     - Bottom: `NumberPad`
     - `HintBanner` below number pad
  4. Wire all button clicks to correct `GameIntent` via `onIntent`
  5. Undo/Redo buttons disabled when respective stacks empty (`state.undoStack.isEmpty()`)
  6. Show `AlertDialog` for new-game confirmation when `state.showNewGameConfirmation == true`
- **Files**: `app/src/main/kotlin/sudoku/app/ui/GameScreen.kt`
- **Done when**: Compiles; all composable slots reference existing component functions
- **Verify**: `./gradlew :app:compileKotlin`
- **Commit**: `feat(ui): implement GameScreen layout skeleton with all component slots`
- _Requirements: AC-1.3, AC-1.4, AC-4.1, AC-5.3, AC-6.4_
- _Design: GameScreen_

---

### 1.29 Implement GameScreen.kt — keyboard handler

- **Do**:
  1. In `GameScreen.kt`, set up focus infrastructure: `val focusRequester = remember { FocusRequester() }` + `LaunchedEffect(Unit) { focusRequester.requestFocus() }`
  2. Apply `Modifier.focusRequester(focusRequester).focusable().onKeyEvent { keyEvent -> ... }` to root Column
  3. Implement key handler as exhaustive `when` on `keyEvent.key` and modifier flags:
     - Arrow keys → `SelectCell` with bounds checks (row 0..8, col 0..8)
     - `1`–`9` → `EnterDigit(digit)` on KeyDown
     - `0`, `Backspace`, `Delete` → `EraseCell`
     - `Escape` → `DeselectCell`
     - `Tab` (no shift) → `SelectCell((selected + 1) % 81)`; `Tab` + shift → `SelectCell((selected + 80) % 81)`
     - `Ctrl+Z` / `Meta+Z` → `Undo`
     - `Ctrl+Y` / `Ctrl+Shift+Z` / `Meta+Shift+Z` → `Redo`
     - `H` → `RequestHint`
     - `P`, `Space` → `TogglePause`
     - `Ctrl+N` / `Meta+N` → `StartNewGame(state.difficulty)`
  4. When `state.isPaused`, consume all events except `TogglePause` (P/Space)
  5. Return `true` for all handled events, `false` otherwise
- **Files**: `app/src/main/kotlin/sudoku/app/ui/GameScreen.kt`
- **Done when**: Compiles; key handler references correct `Key.*` constants from Compose
- **Verify**: `./gradlew :app:compileKotlin`
- **Commit**: `feat(ui): add full keyboard handler to GameScreen with all mapped shortcuts`
- _Requirements: FR-015, AC-1.6, AC-2.4, AC-2.5, AC-2.6, AC-2.8, AC-2.9, AC-4.9, AC-5.1, AC-5.2, AC-6.7, NFR-009_
- _Design: Keyboard Handling_

---

### V10 [VERIFY] Quality checkpoint: GameScreen compiles

- **Do**: Compile full app module through GameScreen.kt; confirm focus, keyboard, and overlay APIs resolve
- **Files**: n/a
- **Verify**: `./gradlew :app:compileKotlin 2>&1 | grep -c 'error:' | grep -q '^0$' && echo V10_PASS`
- **Done when**: Zero compile errors in GameScreen.kt and its component dependencies
- **Commit**: `chore(ui): fix GameScreen compile issues` (only if fixes needed)

---

### 1.30 Full app build — first runnable POC

- **Do**:
  1. Run `./gradlew :app:run` to launch the app for the first time
  2. If build fails: fix compilation errors one at a time until app launches
  3. Manually verify the home screen appears with four difficulty buttons (Easy/Medium/Hard/Expert)
  4. Click "Easy" — loading spinner should appear briefly, then game screen with board
  5. Click a cell — blue highlight should appear
  6. Enter a digit — digit appears in cell
  7. App does not crash during basic play
- **Files**: No new files; fix any files needed to achieve successful run
- **Done when**: `./gradlew :app:jar` exits 0 with BUILD SUCCESSFUL; manual launch with `./gradlew :app:run` shows home screen (verify manually)
- **Verify**: `./gradlew :app:jar 2>&1 | tail -3 | grep -q 'BUILD SUCCESSFUL' && echo POC_BUILD_PASS`
- **Commit**: `feat(app): POC complete — full app builds and game screen renders`
- _Requirements: AC-1.1, AC-1.2, AC-2.1, AC-2.4_

---

### 1.31 POC Checkpoint — end-to-end game loop verified

- **Do**:
  1. Run full build skipping tests: `./gradlew build -x test`
  2. Verify engine jar built: `ls engine/build/libs/ && echo ENGINE_JAR_PRESENT`
  3. Verify app jar or classes built: `ls app/build/classes/kotlin/main/sudoku/app/state/GameViewModel.class 2>/dev/null || ls app/build/libs/ && echo APP_BUILT`
  4. Build passes with zero errors
- **Done when**: App jar contains all three module layers (engine, state, UI); build exits 0
- **Verify**: `./gradlew build -x test 2>&1 | tail -5 | grep -q 'BUILD SUCCESSFUL' && echo POC_CHECKPOINT_PASS`
- **Commit**: `feat(sudoku): complete POC — engine, state, and UI all compiled and linked`
- _Requirements: All FRs provisionally satisfied at POC level_

---

## Phase 2: Refactoring

**Focus**: Clean up code structure, add error handling, enforce immutability contract. No new features.

---

### 2.1 Enforce Board immutability — add copy-on-write documentation and validation

- **Do**:
  1. In `Board.kt`, add KDoc to `withDigit` and `withErased` documenting the copy-on-write contract
  2. Add `init` block asserting `digits.size == 81 && givens.size == 81 && candidates.size == 81`
  3. Ensure `fromDigits` makes defensive copies of input arrays (`digits.copyOf()`, `givens.copyOf()`)
  4. Ensure `withDigit` and `withErased` never mutate the receiver's arrays
- **Files**: `engine/src/main/kotlin/sudoku/engine/Board.kt`
- **Done when**: Compiles; `fromDigits` and mutation methods use `.copyOf()`
- **Verify**: `./gradlew :engine:compileKotlin`
- **Commit**: `refactor(engine): enforce Board copy-on-write immutability with defensive copies`
- _Requirements: FR-001_
- _Design: Board Model, Existing Patterns_

---

### 2.2 Extract ROW/COL/BOX unit constants into Board.kt module level

- **Do**:
  1. In `Board.kt`, ensure `ROW_UNITS`, `COL_UNITS`, `BOX_UNITS`, and `ALL_UNITS` are module-level `val` constants (not recomputed per call)
  2. Verify `Grader.kt` and `HintEngine.kt` reference these constants rather than computing their own unit lists
  3. Update any duplicate unit computation in those files to import from `Board.kt`
- **Files**: `engine/src/main/kotlin/sudoku/engine/Board.kt`, `engine/src/main/kotlin/sudoku/engine/Grader.kt`, `engine/src/main/kotlin/sudoku/engine/HintEngine.kt`
- **Done when**: Compiles; no duplicate unit array construction outside `Board.kt`
- **Verify**: `./gradlew :engine:compileKotlin`
- **Commit**: `refactor(engine): centralize ROW/COL/BOX_UNITS as module-level constants in Board.kt`
- _Design: Existing Patterns_

---

### 2.3 Add Generator retry error handling and MAX_ATTEMPTS constant

- **Do**:
  1. In `Generator.kt`, extract `private const val MAX_ATTEMPTS = 100` at top of object
  2. Wrap the retry loop with try-catch; log retries (use `println` for POC, keep it simple)
  3. Ensure `CancellationException` is re-thrown (not swallowed by catch)
  4. Add `require` check that `difficulty` is a valid enum value (already enforced by Kotlin type system — add comment)
- **Files**: `engine/src/main/kotlin/sudoku/engine/Generator.kt`
- **Done when**: Compiles; `MAX_ATTEMPTS` used in retry loop; `CancellationException` re-thrown
- **Verify**: `./gradlew :engine:compileKotlin`
- **Commit**: `refactor(engine): add Generator MAX_ATTEMPTS constant and proper cancellation handling`
- _Requirements: NFR-002_
- _Design: Async & Concurrency_

---

### V11 [VERIFY] Quality checkpoint: engine refactoring compiles

- **Do**: Compile engine module post-refactoring
- **Files**: n/a
- **Verify**: `./gradlew :engine:compileKotlin 2>&1 | grep -c 'error:' | grep -q '^0$' && echo V11_PASS`
- **Done when**: Zero compile errors after refactoring
- **Commit**: `chore(engine): fix post-refactor compile issues` (only if fixes needed)

---

### 2.4 Extract keyboard handler into standalone function in GameScreen.kt

- **Do**:
  1. In `GameScreen.kt`, extract the `onKeyEvent` lambda body into `private fun handleKeyEvent(keyEvent: KeyEvent, state: GameState, onIntent: (GameIntent) -> Unit): Boolean`
  2. The root `Box` `onKeyEvent` simply calls `handleKeyEvent(it, state, onIntent)`
  3. Ensure pause guard is preserved: when `isPaused`, only `TogglePause` events pass through
- **Files**: `app/src/main/kotlin/sudoku/app/ui/GameScreen.kt`
- **Done when**: Compiles; keyboard handler is a named function with clear signature
- **Verify**: `./gradlew :app:compileKotlin`
- **Commit**: `refactor(ui): extract handleKeyEvent into standalone function in GameScreen`
- _Design: Keyboard Handling_

---

### 2.5 Add window minimum size enforcement in Main.kt

- **Do**:
  1. In `Main.kt`, confirm `minimumSize = DpSize(600.dp, 700.dp)` is set on `Window`
  2. Add `onCloseRequest` edge case: if `state.showQuitConfirmation == true` (dialog already showing), do not dispatch again — guard with `if (!state.showQuitConfirmation)` check
  3. Add error state handling in ViewModel: if `generationJob` throws non-cancellation exception, reset `isLoading = false` and restore to home screen state
- **Files**: `app/src/main/kotlin/sudoku/app/Main.kt`, `app/src/main/kotlin/sudoku/app/state/GameViewModel.kt`
- **Done when**: Compiles; double-dispatch guard present; error handling in coroutine
- **Verify**: `./gradlew :app:compileKotlin`
- **Commit**: `refactor(app): add min window size enforcement and generation error recovery`
- _Requirements: NFR-008, AC-7.6_

---

### 2.6 Clean up GameViewModel.reduce() — extract helper functions

- **Do**:
  1. In `GameViewModel.kt`, extract `private fun applyEnterDigit(state: GameState, digit: Int): GameState` covering the guard check, undo stack push, digit array update, conflict recomputation, and completion check
  2. Extract `private fun applyEraseCell(state: GameState): GameState` similarly
  3. Extract `private fun applyUndo(state: GameState): GameState` and `private fun applyRedo(state: GameState): GameState`
  4. Main `reduce()` becomes a clean dispatch `when` calling these helpers
- **Files**: `app/src/main/kotlin/sudoku/app/state/GameViewModel.kt`
- **Done when**: Compiles; `reduce()` body is under 60 lines; each helper is under 25 lines
- **Verify**: `./gradlew :app:compileKotlin`
- **Commit**: `refactor(state): extract EnterDigit/Erase/Undo/Redo helpers from GameViewModel.reduce()`
- _Design: GameViewModel_

---

### V12 [VERIFY] Quality checkpoint: full app refactoring compiles

- **Do**: Compile full app module post-refactoring; verify no regressions in module resolution
- **Files**: n/a
- **Verify**: `./gradlew build -x test 2>&1 | tail -3 | grep -q 'BUILD SUCCESSFUL' && echo V12_PASS`
- **Done when**: Full build succeeds (compile only, tests deferred to Phase 3)
- **Commit**: `chore(app): fix post-refactor compile issues` (only if fixes needed)

---

## Phase 3: Testing

**Focus**: Write engine unit tests to reach ≥80% line coverage. All tests must pass via `./gradlew :engine:test`.

---

### 3.1 [P] Implement BoardTest.kt — construction and immutability

- **Do**:
  1. Create `engine/src/test/kotlin/sudoku/engine/BoardTest.kt`
  2. Write test class with `@Test` methods:
     - `fromDigits populates digits and givens correctly` — construct with known array, assert each cell
     - `withDigit returns new Board instance` — `assertNotSame(original, modified)` (referential inequality)
     - `withDigit does not mutate original digits` — copy original digits, call `withDigit`, assert original unchanged
     - `withErased clears digit and marks non-given` — set digit, erase, assert 0
     - `isEmpty returns true for empty board` — `Board.empty().isEmpty`
     - `isFull returns false for partial board` — fill 80 cells, assert `!isFull`
- **Files**: `engine/src/test/kotlin/sudoku/engine/BoardTest.kt`
- **Done when**: File exists with 6+ test methods
- **Verify**: `./gradlew :engine:test --tests "sudoku.engine.BoardTest" 2>&1 | tail -5 | grep -q 'BUILD SUCCESSFUL' && echo BOARD_TEST_PASS`
- **Commit**: `test(engine): add BoardTest for construction and immutability`
- _Requirements: FR-001, NFR-011_
- _Design: Test Strategy — BoardTest.kt_

---

### 3.2 [P] Implement BoardTest.kt — peersOf correctness and conflict detection

- **Do**:
  1. In `BoardTest.kt`, add peer and conflict test methods:
     - `peersOf corner cell 0 returns 20 peers` — assert `peersOf(0).size == 20`
     - `peersOf center cell 40 returns 20 peers` — assert `peersOf(40).size == 20`
     - `peersOf result contains no duplicates` — `peersOf(0).toSet().size == 20`
     - `peersOf does not include the cell itself` — `peersOf(0).none { it == 0 }`
  2. Add conflict detection tests using `Board.computeConflicts(digits)` — a module-level function in `Board.kt` (not in GameViewModel) so it is testable from the engine test module. GameViewModel calls `computeConflicts(state.digits)` importing from the engine module:
     - `row duplicate detected` — place same digit in two cells of row 0
     - `column duplicate detected` — place same digit in two cells of col 0
     - `box duplicate detected` — place same digit in two cells of box 0
     - `no false positive on valid board` — valid board returns empty conflict set
     - `given cell participates in conflict detection` (AC-3.5)
  - Note: Extract `computeConflicts(digits: IntArray): Set<Int>` as a module-level function in `Board.kt` (not in GameViewModel) so it is testable from the engine test module. GameViewModel calls `computeConflicts(state.digits)` importing from the engine module.
- **Files**: `engine/src/test/kotlin/sudoku/engine/BoardTest.kt`
- **Done when**: 9+ additional test methods; all pass
- **Verify**: `./gradlew :engine:test --tests "sudoku.engine.BoardTest" 2>&1 | tail -5 | grep -q 'BUILD SUCCESSFUL' && echo BOARD_CONFLICT_TEST_PASS`
- **Commit**: `test(engine): add peersOf correctness and conflict detection tests to BoardTest`
- _Requirements: FR-008, AC-3.5, NFR-011_
- _Design: Test Strategy — Conflict Detection_

---

### V13 [VERIFY] Quality checkpoint: BoardTest passes

- **Do**: Run BoardTest suite; all tests green
- **Files**: n/a
- **Verify**: `./gradlew :engine:test --tests "sudoku.engine.BoardTest" 2>&1 | grep -q 'BUILD SUCCESSFUL' && echo V13_PASS`
- **Done when**: All BoardTest methods pass; zero failures
- **Commit**: `chore(engine): fix Board implementation to pass BoardTest` (only if fixes needed)

---

### 3.3 Implement SolverTest.kt

- **Do**:
  1. Create `engine/src/test/kotlin/sudoku/engine/SolverTest.kt`
  2. Define a known-valid puzzle string constant (e.g., a real Easy puzzle as `IntArray(81)`)
  3. Write test methods:
     - `solve empty board returns valid complete grid` — `assertNotNull(Solver.solve(Board.empty()))`
     - `solve known puzzle returns known solution` — solve puzzle, compare digit-by-digit to expected solution
     - `solved grid passes constraint validation` — verify each of 27 units contains digits 1–9 exactly once
     - `solve invalid board returns null` — construct board with two 1s in same row, assert null
     - `countSolutions returns 1 for unique puzzle` — `assertEquals(1, Solver.countSolutions(puzzle, 2))`
     - `countSolutions returns 2 for multi-solution puzzle` — remove enough cells to create non-unique board, assert `== 2`
     - `countSolutions early-exits at limit` — verify performance: count == limit even if more solutions exist
- **Files**: `engine/src/test/kotlin/sudoku/engine/SolverTest.kt`
- **Done when**: 7 test methods; all pass
- **Verify**: `./gradlew :engine:test --tests "sudoku.engine.SolverTest" 2>&1 | grep -q 'BUILD SUCCESSFUL' && echo SOLVER_TEST_PASS`
- **Commit**: `test(engine): add SolverTest for solve, countSolutions, and constraint validation`
- _Requirements: FR-001, FR-002, NFR-011_
- _Design: Test Strategy — SolverTest.kt_

---

### V14 [VERIFY] Quality checkpoint: SolverTest passes

- **Do**: Run SolverTest suite; all tests green
- **Files**: n/a
- **Verify**: `./gradlew :engine:test --tests "sudoku.engine.SolverTest" 2>&1 | grep -q 'BUILD SUCCESSFUL' && echo V14_PASS`
- **Done when**: All SolverTest methods pass
- **Commit**: `chore(engine): fix Solver implementation to pass SolverTest` (only if fixes needed)

---

### 3.4 Implement GeneratorTest.kt

- **Do**:
  1. Create `engine/src/test/kotlin/sudoku/engine/GeneratorTest.kt`
  2. Use `@Timeout` annotation (JUnit 5) or `runBlocking` with timeout assertion for 2s limit
  3. Write test methods:
     - `generated board has exactly 81 cells` — `assertEquals(81, board.digits.size)`
     - `generated board has at least one empty cell` — `assertTrue(board.digits.any { it == 0 })`
     - `generated board passes constraint validation` — same 27-unit check as SolverTest
     - `generated board has exactly one solution` — `assertEquals(1, Solver.countSolutions(board.digits, 2))`
     - `generated Easy board grades as EASY` — generate Easy board, assert `Grader.grade(puzzle) == EASY`
     - `generated Medium board grades as MEDIUM`
     - `generated Hard board grades as HARD`
     - `generated Expert board grades as EXPERT`
     - `generation completes within 2 seconds for Easy` — use `assertTimeout(Duration.ofSeconds(2)) { ... }`
- **Files**: `engine/src/test/kotlin/sudoku/engine/GeneratorTest.kt`
- **Done when**: 9 test methods; all pass (Expert grade test may be `@Disabled` if generation is slow — log a warning)
- **Verify**: `./gradlew :engine:test --tests "sudoku.engine.GeneratorTest" 2>&1 | grep -q 'BUILD SUCCESSFUL' && echo GEN_TEST_PASS`
- **Commit**: `test(engine): add GeneratorTest for uniqueness, constraint validation, and difficulty grading`
- _Requirements: FR-001, FR-002, FR-003, NFR-002, NFR-011_
- _Design: Test Strategy — GeneratorTest.kt_

---

### V15 [VERIFY] Quality checkpoint: GeneratorTest passes

- **Do**: Run GeneratorTest suite; all tests green (note: Expert timeout test may be annotated `@Timeout(5)` if 2s proves too strict)
- **Files**: n/a
- **Verify**: `./gradlew :engine:test --tests "sudoku.engine.GeneratorTest" 2>&1 | grep -q 'BUILD SUCCESSFUL' && echo V15_PASS`
- **Done when**: All GeneratorTest methods pass
- **Commit**: `chore(engine): fix Generator to pass GeneratorTest` (only if fixes needed)

---

### 3.5 Implement GraderTest.kt — known puzzles

- **Do**:
  1. Create `engine/src/test/kotlin/sudoku/engine/GraderTest.kt`
  2. Include hardcoded known-graded puzzle arrays (one per difficulty, sourced from SudokuWiki or crafted by technique-stall property)
  3. Write test methods:
     - `known Easy puzzle grades as EASY`
     - `known Medium puzzle grades as MEDIUM`
     - `known Hard puzzle grades as HARD`
     - `known Expert puzzle grades as EXPERT`
     - `empty board grades as EASY` (or whatever the degenerate case produces — document expected behavior)
- **Files**: `engine/src/test/kotlin/sudoku/engine/GraderTest.kt`
- **Done when**: 5 test methods; all pass; known puzzles match expected grades
- **Verify**: `./gradlew :engine:test --tests "sudoku.engine.GraderTest" 2>&1 | grep -q 'BUILD SUCCESSFUL' && echo GRADER_TEST_PASS`
- **Commit**: `test(engine): add GraderTest with known Easy/Medium/Hard/Expert reference puzzles`
- _Requirements: FR-003, FR-004, FR-005, FR-006, FR-007, NFR-011_
- _Design: Test Strategy — GraderTest.kt_

---

### V16 [VERIFY] Quality checkpoint: GraderTest passes

- **Do**: Run GraderTest suite; all tests green
- **Files**: n/a
- **Verify**: `./gradlew :engine:test --tests "sudoku.engine.GraderTest" 2>&1 | grep -q 'BUILD SUCCESSFUL' && echo V16_PASS`
- **Done when**: All GraderTest methods pass
- **Commit**: `chore(engine): fix Grader technique-threshold classification to pass GraderTest` (only if fixes needed)

---

### 3.6 Implement HintEngineTest.kt — technique detection

- **Do**:
  1. Create `engine/src/test/kotlin/sudoku/engine/HintEngineTest.kt`
  2. Construct minimal board states that trigger each technique (hand-craft boards where only one technique applies)
  3. Write test methods:
     - `Naked Single found when one candidate remains in cell` — construct board where cell 0 has only digit 5 as candidate; assert `Found` with technique `"Naked Single"`
     - `Hidden Single found when digit appears in exactly one cell of a unit`
     - `Naked Pair found in unit with two cells sharing two candidates`
     - `Hidden Pair found correctly`
     - `Pointing Pair found when box candidates confined to one row`
     - `NoHint returned for Easy board where no technique applies` — use a board with only one cell empty, filled correctly (nothing to hint)
     - `NoHintForDifficulty returned for Hard board where no supported technique applies`
     - `Found returned for Hard board if supported technique IS available` (AC-9.4)
     - `targetCells and peerCells are non-overlapping` — verify for any `Found` result
- **Files**: `engine/src/test/kotlin/sudoku/engine/HintEngineTest.kt`
- **Done when**: 9 test methods; all pass
- **Verify**: `./gradlew :engine:test --tests "sudoku.engine.HintEngineTest" 2>&1 | grep -q 'BUILD SUCCESSFUL' && echo HINT_TEST_PASS`
- **Commit**: `test(engine): add HintEngineTest for all five techniques and NoHint/NoHintForDifficulty variants`
- _Requirements: FR-010, AC-4.2, AC-4.4, AC-4.8, AC-9.1, AC-9.4, NFR-011_
- _Design: Test Strategy — HintEngineTest.kt_

---

### V17 [VERIFY] Quality checkpoint: HintEngineTest passes

- **Do**: Run HintEngineTest suite; all tests green
- **Files**: n/a
- **Verify**: `./gradlew :engine:test --tests "sudoku.engine.HintEngineTest" 2>&1 | grep -q 'BUILD SUCCESSFUL' && echo V17_PASS`
- **Done when**: All HintEngineTest methods pass
- **Commit**: `chore(engine): fix HintEngine technique detection to pass HintEngineTest` (only if fixes needed)

---

### 3.7 Run full engine test suite and verify ≥80% coverage

- **Do**:
  1. Run all engine tests: `./gradlew :engine:test`
  2. Enable JaCoCo coverage: add to `engine/build.gradle.kts`:
     ```kotlin
     plugins { jacoco }
     tasks.test { finalizedBy(tasks.jacocoTestReport) }
     tasks.jacocoTestReport {
         reports { xml.required = true; html.required = true }
     }
     ```
  3. Run: `./gradlew :engine:test :engine:jacocoTestReport`
  4. Check HTML report at `engine/build/reports/jacoco/test/html/index.html`
  5. If coverage < 80%: identify uncovered lines and add tests until ≥80%
- **Files**: `engine/build.gradle.kts`, test files as needed
- **Done when**: `./gradlew :engine:test` exits 0 with all tests passing; JaCoCo reports ≥80% line coverage
- **Verify**: `./gradlew :engine:test :engine:jacocoTestReport 2>&1 | grep -q 'BUILD SUCCESSFUL' && echo COVERAGE_PASS`
- **Commit**: `test(engine): achieve ≥80% line coverage across engine module`
- _Requirements: NFR-011_
- _Design: Test Strategy_

---

## Phase 4: Quality Gates

**Focus**: Full local CI, create PR, verify CI pipeline green on all three platforms.

---

### 4.1 Add GitHub Actions CI workflow file

- **Do**:
  1. Create `.github/workflows/build.yml`
  2. Implement 3-runner matrix as per design.md: `windows-latest` (packageMsi), `macos-14` (packageDmg), `ubuntu-22.04` (packageDeb)
  3. Steps per runner: `actions/checkout@v4`, `actions/setup-java@v4` (java 21, temurin), WiX install on Windows (`choco install wixtoolset --version=4.0.5`), `./gradlew :engine:test :app:${{ matrix.task }}`, `actions/upload-artifact@v4`
  4. Trigger on: `push` to `main`, `pull_request`
- **Files**: `.github/workflows/build.yml`
- **Done when**: YAML file exists with correct matrix, steps, and artifact upload
- **Verify**: `grep -q 'macos-14' .github/workflows/build.yml && grep -q 'ubuntu-22.04' .github/workflows/build.yml && echo CI_YAML_PASS`
- **Commit**: `chore(ci): add GitHub Actions 3-runner build matrix for Windows/macOS/Linux`
- _Requirements: AC-8.1, AC-8.2, AC-8.3, NFR-006, NFR-010_
- _Design: GitHub Actions CI Matrix_

---

### VP1 [VERIFY] Full local CI

- **Do**:
  1. Run full local build including engine tests: `./gradlew build`
  2. Verify engine tests pass: `./gradlew :engine:test`
  3. Verify app compiles: `./gradlew :app:compileKotlin`
  4. Verify coverage report generated: `./gradlew :engine:jacocoTestReport`
- **Files**: n/a
- **Verify**: `./gradlew build 2>&1 | tail -3 | grep -q 'BUILD SUCCESSFUL' && echo FULL_CI_PASS`
- **Done when**: `./gradlew build` exits 0; all engine tests pass; no compile errors in either module
- **Commit**: `chore(sudoku): pass full local build` (only if fixes needed)

---

### 4.2 Create feature branch and PR

- **Do**:
  1. Verify current branch is not `main`: `git branch --show-current`
  2. If on `main`, create feature branch: `git checkout -b feat/sudoku-app`
  3. Stage all spec-driven source files: `git add engine/ app/ settings.gradle.kts gradle/ .github/`
  4. Push branch: `git push -u origin feat/sudoku-app`
  5. Create PR: `gh pr create --title "feat: implement Sudoku desktop app (engine + UI + CI)" --body "$(cat <<'EOF' ... EOF)"`
  6. PR body should summarize: engine module (Cell/Board/Solver/Generator/Grader/HintEngine), MVI state (GameState/GameIntent/GameViewModel), UI (SudokuBoard Canvas, GameScreen, overlays), CI matrix, ≥80% engine coverage
- **Files**: None (git operations)
- **Done when**: PR created; URL returned by `gh pr create`
- **Verify**: `gh pr view --json state | grep -q '"state":"OPEN"' && echo PR_OPEN`
- **Commit**: None (commit already done per task)

---

### VP2 [VERIFY] CI pipeline passes

- **Do**:
  1. Wait for CI to complete: `gh pr checks --watch`
  2. Verify all 3 matrix runners pass (windows, macos, ubuntu)
  3. If any fail: `gh pr checks` to identify which step failed; fix and push
- **Files**: n/a
- **Verify**: `gh pr checks 2>&1 | grep -v 'pass\|success\|✓' | grep -q 'fail\|error\|✗' && echo CI_FAIL || echo VP2_PASS`
- **Done when**: All CI checks show passing state; artifact uploads successful on each runner
- **Commit**: `fix(ci): resolve CI failures` (only if needed after inspecting failures)

---

### VP3 [VERIFY] AC checklist — programmatic verification

- **Do**:
  1. Verify AC-1.1 (four difficulty buttons): `grep -r 'Difficulty.EASY\|Difficulty.MEDIUM\|Difficulty.HARD\|Difficulty.EXPERT' app/src/main/kotlin/sudoku/app/ui/HomeScreen.kt | wc -l | grep -q '[4-9]' && echo AC1_PASS`
  2. Verify AC-1.5 (uniqueness): `./gradlew :engine:test --tests "sudoku.engine.GeneratorTest.generated board has exactly one solution" 2>&1 | grep -q 'BUILD SUCCESSFUL' && echo AC1_5_PASS`
  3. Verify AC-2.6 (arrow key navigation): `grep -q 'Key.DirectionUp\|Key.DirectionDown' app/src/main/kotlin/sudoku/app/ui/GameScreen.kt && echo AC2_6_PASS`
  4. Verify AC-3.5 (given in conflict): `./gradlew :engine:test --tests "sudoku.engine.BoardTest" 2>&1 | grep -q 'BUILD SUCCESSFUL' && echo AC3_5_PASS`
  5. Verify AC-4.8 (five techniques): `grep -rn 'nakedSingle\|hiddenSingle\|nakedPair\|hiddenPair\|pointingPair' engine/src/main/kotlin/sudoku/engine/HintEngine.kt | wc -l | grep -q '[5-9]\|[1-9][0-9]' && echo AC4_8_PASS`
  6. Verify AC-5.1 (Ctrl+Z Undo): `grep -q 'Key.Z.*Undo\|Undo.*Key.Z' app/src/main/kotlin/sudoku/app/ui/GameScreen.kt && echo AC5_1_PASS`
  7. Verify AC-6.6 (HH:MM:SS format): `grep -q '3600\|HH:MM:SS\|toHours\|hour' app/src/main/kotlin/sudoku/app/ui/components/TimerDisplay.kt && echo AC6_6_PASS`
  8. Verify AC-7.6 (quit confirmation): `grep -q 'showQuitConfirmation\|ShowQuitConfirmation' app/src/main/kotlin/sudoku/app/Main.kt && echo AC7_6_PASS`
  9. Verify AC-9.1 (NoHintForDifficulty): `./gradlew :engine:test --tests "sudoku.engine.HintEngineTest" 2>&1 | grep -q 'BUILD SUCCESSFUL' && echo AC9_1_PASS`
  10. Verify NFR-011 (≥80% coverage): `./gradlew :engine:test :engine:jacocoTestReport && grep -o 'Total.*[0-9]%' engine/build/reports/jacoco/test/html/index.html | head -1`
- **Files**: n/a
- **Verify**: Run all grep/test commands above; all must output `*_PASS`
- **Done when**: All AC checks confirmed implemented; coverage ≥80% documented
- **Commit**: None

---

## Phase 5: PR Lifecycle

**Goal**: Autonomous PR management loop until all criteria met and PR merged.

---

### 5.1 Monitor CI and fix failures

- **Do**:
  1. Check CI status: `gh pr checks`
  2. For each failing check: read logs (`gh run view <run-id> --log-failed`), identify root cause, fix locally, push
  3. Re-verify: `gh pr checks --watch`
  4. Repeat until all checks green
- **Verify**: `gh pr checks 2>&1 | grep -v '#' | grep -q 'fail\|error' && echo FAILURES_REMAIN || echo ALL_GREEN`
- **Done when**: `gh pr checks` shows no failures; all 3 matrix runners green
- **Commit**: `fix(ci): resolve <specific failure>` per fix cycle

---

### 5.2 Address code review comments

- **Do**:
  1. List PR review comments: `gh pr view --comments`
  2. For each comment: understand the concern, make code change, commit, push
  3. Reply to comment via `gh api` if response needed
  4. Re-request review after addressing all comments
- **Verify**: `gh pr view --json reviewDecision | grep -q '"APPROVED"\|"CHANGES_REQUESTED"'`
- **Done when**: No unresolved review comments; review status is APPROVED or no review required
- **Commit**: `fix(<scope>): address review comment — <description>` per comment

---

### 5.3 Final validation before merge

- **Do**:
  1. Run full local build one final time: `./gradlew build`
  2. Verify engine test count: `./gradlew :engine:test 2>&1 | grep 'tests were run\|tests run' | head -1`
  3. Verify zero test failures: `./gradlew :engine:test 2>&1 | grep -q '0 failures' && echo ZERO_FAILURES`
  4. Confirm PR is mergeable: `gh pr view --json mergeable | grep -q '"MERGEABLE"'`
  5. Confirm CI all green: `gh pr checks 2>&1 | grep -q 'pass\|success' && echo CI_GREEN`
- **Verify**: `./gradlew build 2>&1 | grep -q 'BUILD SUCCESSFUL' && echo FINAL_PASS`
- **Done when**: Full build passes; zero test failures; PR mergeable; CI green; PR approved or no review required
- **Commit**: None (merge commit is the final commit)

---

## Notes

### POC Shortcuts Taken (Phase 1)
- Timer accuracy: `delay(1_000)` is approximate; no wall-clock correction
- Conflict detection: computed in reducer on every intent (not debounced); acceptable for 81 cells
- Number-match crosshatch: drawn as fixed diagonal lines at 20% opacity; not adaptive to cell size
- Loading state: simple `CircularProgressIndicator`; no progress percentage
- No UI tests: composables verified by compile + manual run only

### Production TODOs (Address in Phase 2 if needed)
- Expert generation benchmark: run after Phase 1 POC; if consistently >2s, add forward-checking to `countSolutions` before DLX consideration
- WiX 4 availability on CI: confirm `choco install wixtoolset` finds version 4.0.5; fallback is the `crazy-max/ghaction-setup-wix` GitHub Action
- NFR-012 crosshatch: confirm crosshatch pattern is visible at ≥600px board width on target OSes
- Difficulty threshold tuning: Expert puzzles may grade as HARD depending on X-Wing implementation; calibrate with 10+ generated puzzles per difficulty

### Key Verification Commands
- Engine compile: `./gradlew :engine:compileKotlin`
- App compile: `./gradlew :app:compileKotlin`
- Engine tests: `./gradlew :engine:test`
- Coverage report: `./gradlew :engine:jacocoTestReport`
- Run app: `./gradlew :app:run`
- Full build: `./gradlew build`
- CI status: `gh pr checks`