# Tasks: UI Redesign

**Workflow**: TDD (MID_SIZED) | **Granularity**: coarse | **Target**: 10–20 tasks

**Engine note**: `HintResult.Found.targetCells: List<Int>` — use `targetCells.firstOrNull()` for `selectedIndex` in FR-7.

---

## Phase 1: Red-Green-Yellow Cycles

Focus: TDD-driven implementation. State changes first (testable via engine tests), then UI composables (verified via build).

---

- [x] 1.1 [RED] Failing test: `isGameOver` set when 3rd mistake entered

  - **Do**:
    1. Open `engine/src/test/kotlin/sudoku/engine/` — find or create `GameViewModelTest.kt` (check if exists first with `ls /workspace/engine/src/test/kotlin/sudoku/engine/`)
    2. Write test: construct a `GameState` with `mistakeCount = 2`, call `applyEnterDigit` with a wrong digit, assert `isGameOver == true`
    3. Write test: same setup, assert `checkCompletion` is NOT called (state has empty board — if `isGameOver` is true, `isComplete` stays false)
    4. Confirm `isGameOver` field does NOT yet exist in `GameState` — test must fail to compile or fail at runtime
  - **Files**: `engine/src/test/kotlin/sudoku/engine/GameViewModelTest.kt` (create if absent)
  - **Done when**: Test file exists with the two assertions; `./gradlew :engine:test` fails (compile error or assertion failure)
  - **Verify**: `./gradlew :engine:test 2>&1 | grep -E "FAILED|error:" | head -20`
  - **Commit**: `test(state): red - failing tests for isGameOver on 3rd mistake`
  - _Requirements: FR-3, FR-4_
  - _Design: GameState, GameViewModel — applyEnterDigit_

---

- [x] 1.2 [GREEN] Add `isGameOver: Boolean` to `GameState`; wire `applyEnterDigit`

  - **Do**:
    1. In `GameState.kt`: add `val isGameOver: Boolean` field (after `hintsRemaining`)
    2. In `GameState.Initial`: add `isGameOver = false`
    3. In `GameState.equals()`: add `&& isGameOver == other.isGameOver`
    4. In `GameViewModel.applyEnterDigit`: change the `newState` copy to include `isGameOver = newMistakeCount >= 3`; change the return to `return if (withMistake.isGameOver) withMistake else checkCompletion(withMistake)` (rename local `newState` → `withMistake` if needed, or add `isGameOver` inline)
  - **Files**:
    - `app/src/main/kotlin/sudoku/app/state/GameState.kt`
    - `app/src/main/kotlin/sudoku/app/state/GameViewModel.kt`
  - **Done when**: `./gradlew :engine:test` passes; `./gradlew :app:build` exits 0
  - **Verify**: `./gradlew :engine:test && ./gradlew :app:build`
  - **Commit**: `feat(state): green - add isGameOver field and wire applyEnterDigit`
  - _Requirements: FR-3, FR-4_
  - _Design: GameState, GameViewModel — applyEnterDigit_

---

- [x] 1.3 [RED] Failing test: `TimerTick` ignored when `isGameOver`; `PuzzleGenerated` resets `isGameOver`; `RequestHint` sets `selectedIndex`

  - **Do**:
    1. Add test: `TimerTick` dispatched on a state with `isGameOver = true` → `timerSeconds` unchanged
    2. Add test: `PuzzleGenerated` intent on game-over state → `isGameOver == false`
    3. Add test: `RequestHint` when engine returns `HintResult.Found(targetCells=[42, ...])` → `selectedIndex == 42`
    4. Confirm all three tests fail before any implementation changes
  - **Files**: `engine/src/test/kotlin/sudoku/engine/GameViewModelTest.kt`
  - **Done when**: Three new tests added; `./gradlew :engine:test` fails on at least the TimerTick and RequestHint assertions
  - **Verify**: `./gradlew :engine:test 2>&1 | grep -E "FAILED|AssertionError" | head -20`
  - **Commit**: `test(state): red - failing tests for TimerTick guard, PuzzleGenerated reset, RequestHint selectedIndex`
  - _Requirements: FR-6, FR-7_
  - _Design: GameViewModel — TimerTick, PuzzleGenerated, RequestHint_

---

- [x] 1.4 [GREEN] Wire `TimerTick` guard, `PuzzleGenerated` reset, `RequestHint → selectedIndex`, timer cancel in `dispatch`

  - **Do**:
    1. In `GameViewModel.reduce`, `TimerTick` branch: change guard to `!state.isPaused && !state.isComplete && !state.isGameOver && !state.isLoading`
    2. In `PuzzleGenerated` branch: add `isGameOver = false` to the copy
    3. In `RequestHint` branch: compute `val hint = HintEngine.findHint(board, state.difficulty)`, then `val hintIndex = if (hint is HintResult.Found) hint.targetCells.firstOrNull() else null`, include `selectedIndex = hintIndex ?: state.selectedIndex` in the copy
    4. In `dispatch()`: after `handleSideEffects(intent)`, add `if (_state.value.isGameOver) timerJob?.cancel()`
  - **Files**: `app/src/main/kotlin/sudoku/app/state/GameViewModel.kt`
  - **Done when**: All engine tests pass; build succeeds
  - **Verify**: `./gradlew :engine:test && ./gradlew :app:build`
  - **Commit**: `feat(state): green - TimerTick guard, PuzzleGenerated reset, RequestHint selectedIndex, timer cancel`
  - _Requirements: FR-6, FR-7_
  - _Design: GameViewModel — TimerTick, PuzzleGenerated, RequestHint, dispatch_

---

- [ ] V1 [VERIFY] Quality checkpoint: engine tests + build green

  - **Do**: Run engine tests and full app build
  - **Verify**: `./gradlew :engine:test && ./gradlew :app:build`
  - **Done when**: Both commands exit 0, zero test failures
  - **Commit**: `chore(state): pass quality checkpoint after state layer` (only if fixes needed)

---

- [x] 1.5 [GREEN] Create `AppColors.kt` — brand color constants

  - **Do**:
    1. Create `app/src/main/kotlin/sudoku/app/ui/AppColors.kt`
    2. Declare `object AppColors` in package `sudoku.app.ui` with all color constants from design.md (Primary, NewGameBtn, Highlight, Background, StatLabel, StatValue, ActionBtnBg, ActionBtnDis, PauseBtnBg, NumBtnBg, NumBtnDis, GivenDigit, EnteredDigit, HintSlotBg)
  - **Files**: `app/src/main/kotlin/sudoku/app/ui/AppColors.kt` (create)
  - **Done when**: File compiles; `./gradlew :app:build` exits 0
  - **Verify**: `./gradlew :app:build && grep -c "val " /workspace/app/src/main/kotlin/sudoku/app/ui/AppColors.kt`
  - **Commit**: `feat(ui): add AppColors brand color constants`
  - _Requirements: FR-14_
  - _Design: AppColors_

  > Note: No [RED] step — this is a pure additive file with no behavior to test-drive. Build verification suffices.

---

- [ ] 1.6 [GREEN] Create `GameOverDialog.kt` — non-dismissible game-over dialog

  - **Do**:
    1. Create `app/src/main/kotlin/sudoku/app/ui/components/GameOverDialog.kt`
    2. Implement `@Composable fun GameOverDialog(onNewGame: () -> Unit)` using `AlertDialog` with `onDismissRequest = {}`, title "Game Over", text "You made 3 mistakes. Better luck next time!", single `Button` "New Game" using `AppColors.NewGameBtn`, `dismissButton = null`
  - **Files**: `app/src/main/kotlin/sudoku/app/ui/components/GameOverDialog.kt` (create)
  - **Done when**: File compiles; `./gradlew :app:build` exits 0
  - **Verify**: `./gradlew :app:build`
  - **Commit**: `feat(ui): add GameOverDialog composable`
  - _Requirements: FR-5_
  - _Design: GameOverDialog_

---

- [ ] V2 [VERIFY] Quality checkpoint: build after new files

  - **Do**: Full build to confirm AppColors and GameOverDialog compile cleanly
  - **Verify**: `./gradlew :app:build`
  - **Done when**: Exit 0, no warnings treated as errors
  - **Commit**: `chore(ui): pass quality checkpoint after new composables` (only if fixes needed)

---

- [ ] 1.7 [GREEN] Modify `GameScreen.kt` — remove pencil button, fix weights, fixed hint slot, game-over guards, show dialog, use AppColors

  - **Do**:
    1. Change board panel weight `0.58f` → `0.6f`; right panel weight `0.42f` → `0.4f`
    2. Remove the `BadgedActionButton` for pencil (`✏` / "OFF") from the action row entirely — keep only Undo, Erase, Hints; confirm Erase circular button is present in the action row (not in NumberPad)
    3. Add `&& !state.isGameOver` to: Undo `enabled`, Erase `enabled`, Hints `enabled`, `NumberPad` `enabled`
    4. Replace conditional `if (state.hintResult != null) { HintBanner(...) }` with fixed-height Box: `Box(modifier = Modifier.fillMaxWidth().height(56.dp)) { if (state.hintResult != null) HintBanner(hintResult = state.hintResult) }`
    5. Add `if (state.isGameOver) { GameOverDialog(onNewGame = { onIntent(GameIntent.StartNewGame(state.difficulty)) }) }` after the closing `}` of the outer `Row`, alongside the existing `showNewGameConfirmation` dialog
    6. In `handleKeyEvent`: add `if (state.isGameOver) return false` guard at the top (before the `isPaused` check)
    7. Replace inline hex color literals in `GameScreen.kt` with `AppColors.*` equivalents (background, pause button, action button colors, stat colors, new game button)
    8. Add import for `GameOverDialog` and `AppColors`
  - **Files**: `app/src/main/kotlin/sudoku/app/ui/GameScreen.kt`
  - **Done when**: `./gradlew :app:build` exits 0; grep confirms `BadgedActionButton.*✏` is gone and `isGameOver` guard present
  - **Verify**: `./gradlew :app:build && grep -c "isGameOver" /workspace/app/src/main/kotlin/sudoku/app/ui/GameScreen.kt && ! grep -q "✏" /workspace/app/src/main/kotlin/sudoku/app/ui/GameScreen.kt && echo PENCIL_REMOVED`
  - **Commit**: `feat(ui): update GameScreen - remove pencil, fix weights, fixed hint slot, game-over guards`
  - _Requirements: FR-1, FR-2, FR-5, FR-8, FR-9, FR-10, FR-12, FR-13, FR-15_
  - _Design: GameScreen_

---

- [ ] 1.8 [GREEN] Modify `NumberPad.kt` — use `AppColors` for button colors

  - **Do**:
    1. Add import `import sudoku.app.ui.AppColors`
    2. Replace `Color(0xFFDDE8F5)` → `AppColors.NumBtnBg`
    3. Replace `Color(0xFFEEEEEE)` → `AppColors.NumBtnDis`
    4. Replace `Color(0xFF1A3060)` → `AppColors.GivenDigit`
    5. Replace `Color(0xFF999999)` → `Color(0xFF999999)` (keep as-is — no exact AppColors match; acceptable)
  - **Files**: `app/src/main/kotlin/sudoku/app/ui/components/NumberPad.kt`
  - **Done when**: `./gradlew :app:build` exits 0; no inline hex literals for btn colors remain
  - **Verify**: `./gradlew :app:build`
  - **Commit**: `refactor(ui): use AppColors in NumberPad`
  - _Requirements: FR-14_
  - _Design: NumberPad_

---

- [ ] 1.9 [VERIFY] FR-9 structural check: NumberPad is 3×3 grid, digits 1–9 only, no erase button inside

  - **Do**:
    1. Confirm nested `Column + Row` pattern: `grep -n "for (row in 0..2)" /workspace/app/src/main/kotlin/sudoku/app/ui/components/NumberPad.kt`
    2. Confirm digit range 1–9: `grep -n "row \* 3 + col + 1" /workspace/app/src/main/kotlin/sudoku/app/ui/components/NumberPad.kt`
    3. Confirm no erase/delete button inside NumberPad: `! grep -qi "erase\|delete\|clear\|backspace" /workspace/app/src/main/kotlin/sudoku/app/ui/components/NumberPad.kt && echo NO_ERASE_IN_NUMPAD`
    4. Confirm `weight(1f)` and `aspectRatio(1f)` modifiers present (3×3 uniform grid): `grep -n "weight(1f)" /workspace/app/src/main/kotlin/sudoku/app/ui/components/NumberPad.kt`
  - **Files**: `app/src/main/kotlin/sudoku/app/ui/components/NumberPad.kt` (read-only verify)
  - **Done when**: All four greps succeed; structure confirmed correct with no changes needed
  - **Verify**: `grep -q "for (row in 0..2)" /workspace/app/src/main/kotlin/sudoku/app/ui/components/NumberPad.kt && grep -q "row \* 3 + col + 1" /workspace/app/src/main/kotlin/sudoku/app/ui/components/NumberPad.kt && ! grep -qi "erase\|delete\|clear\|backspace" /workspace/app/src/main/kotlin/sudoku/app/ui/components/NumberPad.kt && echo FR9_PASS`
  - **Commit**: None (verification only; no code change expected)
  - _Requirements: FR-9_
  - _Design: NumberPad_

---

- [ ] 1.10 [VERIFY] FR-11 canvas layer check: row/col/box highlight is Layer 2 in SudokuBoard

  - **Do**:
    1. Confirm Layer 2 highlight block exists: `grep -n "Layer 2" /workspace/app/src/main/kotlin/sudoku/app/ui/components/SudokuBoard.kt`
    2. Confirm highlight draws before selected (Layer 4) and conflict (Layer 5) overlays: `grep -n "Layer [1-5]" /workspace/app/src/main/kotlin/sudoku/app/ui/components/SudokuBoard.kt`
    3. Confirm highlight color draws row/col/box: `grep -n "selRow\|selCol\|selBox\|iBox" /workspace/app/src/main/kotlin/sudoku/app/ui/components/SudokuBoard.kt`
    4. FR-11 is already satisfied — no code change required. If any check fails, add the missing highlight block (draw `Color(0xFFD4E8FA)` rect when `iRow == selRow || iCol == selCol || iBox == selBox`) and add `import sudoku.app.ui.AppColors` only if adopting AppColors for the highlight color.
  - **Files**: `app/src/main/kotlin/sudoku/app/ui/components/SudokuBoard.kt` (read-only verify; modify only if layer check fails)
  - **Done when**: All three greps confirm Layer 2 highlight is present and correctly ordered; build exits 0
  - **Verify**: `grep -q "Layer 2" /workspace/app/src/main/kotlin/sudoku/app/ui/components/SudokuBoard.kt && grep -q "selRow\|selCol\|selBox" /workspace/app/src/main/kotlin/sudoku/app/ui/components/SudokuBoard.kt && ./gradlew :app:build && echo FR11_PASS`
  - **Commit**: `fix(ui): add row/col/box highlight layer to SudokuBoard` (only if code change was needed)
  - _Requirements: FR-11_
  - _Design: SudokuBoard_

---

- [ ] V3 [VERIFY] Full quality gate: engine tests + build

  - **Do**: Run engine tests and full build; confirm no regressions
  - **Verify**: `./gradlew :engine:test && ./gradlew :app:build`
  - **Done when**: All tests pass, build exits 0
  - **Commit**: `chore(ui): pass quality gate after UI layer` (only if fixes needed)

---

## Phase 2: Additional Testing

Focus: Confirm edge cases compile and key behavioral paths are covered.

---

- [ ] 2.1 [GREEN] Add engine test: Undo disabled after game-over; keyboard guard

  - **Do**:
    1. Add test: `Undo` intent on `isGameOver = true` state → still applies (undo is state-only; game-over only disables UI, not the reducer). Verify by confirming `undoStack` shrinks — OR document that undo IS allowed reducer-side and UI disables it.
    2. Add test: `StartNewGame` after game-over → `isGameOver == false` in the resulting `PuzzleGenerated` state
    3. Verify tests pass
  - **Files**: `engine/src/test/kotlin/sudoku/engine/GameViewModelTest.kt`
  - **Done when**: New tests pass; `./gradlew :engine:test` exits 0
  - **Verify**: `./gradlew :engine:test`
  - **Commit**: `test(state): add edge case tests for game-over recovery`
  - _Requirements: FR-4, FR-6_
  - _Design: Edge Cases_

---

- [ ] V4 [VERIFY] Full local CI: engine tests + app build

  - **Do**: Final local verification before VE
  - **Files**: none
  - **Verify**: `./gradlew :engine:test && ./gradlew :app:build`
  - **Done when**: Both exit 0
  - **Commit**: None

---

## Phase 3: Quality Gates

---

- [ ] VE1 [VERIFY] E2E startup: build app for run

  - **Do**:
    1. Run build to confirm runnable artifact: `./gradlew :app:build`
    2. Verify build output jar exists: `ls /workspace/app/build/compose/jars/*.jar 2>/dev/null || ls /workspace/app/build/libs/*.jar 2>/dev/null || echo "check build output"`
  - **Files**: none
  - **Verify**: `./gradlew :app:build && echo VE1_PASS`
  - **Done when**: Build exits 0, app artifact produced
  - **Commit**: None

- [ ] VE2 [VERIFY] E2E check: verify key code paths present in built artifact

  - **Do**:
    1. Confirm `isGameOver` field present in compiled state: `grep -r "isGameOver" /workspace/app/src/main/kotlin/ | wc -l`
    2. Confirm `GameOverDialog` composable exists: `grep -l "GameOverDialog" /workspace/app/src/main/kotlin/`
    3. Confirm pencil button removed: `! grep -r "✏" /workspace/app/src/main/kotlin/sudoku/app/ui/GameScreen.kt && echo PENCIL_GONE`
    4. Confirm fixed hint slot at 56.dp: `grep "56.dp" /workspace/app/src/main/kotlin/sudoku/app/ui/GameScreen.kt`
    5. Confirm AppColors object exists: `grep "object AppColors" /workspace/app/src/main/kotlin/sudoku/app/ui/AppColors.kt`
    6. Confirm targetCells.firstOrNull used in RequestHint: `grep "targetCells.firstOrNull" /workspace/app/src/main/kotlin/sudoku/app/state/GameViewModel.kt`
    7. Confirm NumberPad 3×3 structure (FR-9): `grep -q "for (row in 0..2)" /workspace/app/src/main/kotlin/sudoku/app/ui/components/NumberPad.kt && echo FR9_CONFIRMED`
    8. Confirm SudokuBoard Layer 2 highlight (FR-11): `grep -q "Layer 2" /workspace/app/src/main/kotlin/sudoku/app/ui/components/SudokuBoard.kt && echo FR11_CONFIRMED`
  - **Verify**: All eight grep commands succeed (exit 0)
  - **Done when**: All structural checks pass
  - **Commit**: None

- [ ] VE3 [VERIFY] E2E cleanup: no stale build artifacts blocking

  - **Do**: No persistent process to kill (build-only verification). Confirm working directory is clean for PR.
  - **Verify**: `echo VE3_PASS`
  - **Done when**: Always passes
  - **Commit**: None

---

- [ ] V5 [VERIFY] AC checklist

  - **Do**: Programmatically verify each FR is satisfied:
    - FR-3 (`isGameOver: Boolean`): `grep "isGameOver: Boolean" /workspace/app/src/main/kotlin/sudoku/app/state/GameState.kt`
    - FR-4 (applyEnterDigit sets isGameOver): `grep "isGameOver = newMistakeCount >= 3" /workspace/app/src/main/kotlin/sudoku/app/state/GameViewModel.kt`
    - FR-5 (GameOverDialog, no dismiss): `grep "onDismissRequest = {}" /workspace/app/src/main/kotlin/sudoku/app/ui/components/GameOverDialog.kt`
    - FR-6 (PuzzleGenerated resets isGameOver): `grep "isGameOver = false" /workspace/app/src/main/kotlin/sudoku/app/state/GameViewModel.kt`
    - FR-7 (RequestHint sets selectedIndex): `grep "targetCells.firstOrNull" /workspace/app/src/main/kotlin/sudoku/app/state/GameViewModel.kt`
    - FR-8 (pencil removed): `! grep -q "✏" /workspace/app/src/main/kotlin/sudoku/app/ui/GameScreen.kt && echo FR8_PASS`
    - FR-9 (NumberPad 3×3, digits 1–9, no erase): `grep -q "for (row in 0..2)" /workspace/app/src/main/kotlin/sudoku/app/ui/components/NumberPad.kt && ! grep -qi "erase\|delete\|clear\|backspace" /workspace/app/src/main/kotlin/sudoku/app/ui/components/NumberPad.kt && echo FR9_PASS`
    - FR-10 (Erase in GameScreen action row, not in NumberPad): `grep -q "Erase" /workspace/app/src/main/kotlin/sudoku/app/ui/GameScreen.kt && ! grep -qi "erase" /workspace/app/src/main/kotlin/sudoku/app/ui/components/NumberPad.kt && echo FR10_PASS`
    - FR-11 (row/col/box highlight Layer 2): `grep -q "Layer 2" /workspace/app/src/main/kotlin/sudoku/app/ui/components/SudokuBoard.kt && echo FR11_PASS`
    - FR-14 (AppColors object): `grep "object AppColors" /workspace/app/src/main/kotlin/sudoku/app/ui/AppColors.kt`
    - FR-15 (fixed hint slot): `grep "56.dp" /workspace/app/src/main/kotlin/sudoku/app/ui/GameScreen.kt`
  - **Verify**: All grep commands exit 0
  - **Done when**: Every FR has a passing grep
  - **Commit**: None

---

## Phase 4: PR Lifecycle

---

- [ ] 4.1 [GREEN] Create PR and verify CI

  - **Do**:
    1. Confirm on feature branch: `git branch --show-current`
    2. Push: `git push -u origin feat/ui-redesign`
    3. Create PR: `gh pr create --title "feat(ui): redesign - game-over dialog, hint slot, AppColors, isGameOver state" --body "$(cat <<'EOF'
## Summary
- Add \`isGameOver: Boolean\` to GameState; wire 3-mistake game-over in applyEnterDigit
- Add GameOverDialog (non-dismissible); add AppColors brand constants
- Update GameScreen: remove pencil button, fix weights, fixed hint slot, game-over guards, Erase in action row
- RequestHint now sets selectedIndex to first targetCell; TimerTick guarded on isGameOver
- Verified NumberPad 3×3 structure (FR-9) and SudokuBoard highlight layer order (FR-11)

## Test plan
- [ ] \`./gradlew :engine:test\` passes (isGameOver, TimerTick, PuzzleGenerated, RequestHint tests)
- [ ] \`./gradlew :app:build\` exits 0
- [ ] Verify GameOverDialog appears after 3 wrong entries (manual run)
- [ ] Verify hint button selects hinted cell (manual run)
- [ ] Verify pencil button absent from action row (manual run)
- [ ] Verify Erase circular button in action row, not inside NumberPad (manual run)

🤖 Generated with [Claude Code](https://claude.com/claude-code)
EOF
)"`
    4. Watch CI: `gh pr checks --watch`
  - **Files**: none
  - **Verify**: `gh pr checks` shows all green
  - **Done when**: PR created and all CI checks green
  - **Commit**: None (commit happened per task)

---

## Notes

- **TDD adaptation**: UI composable tasks (1.5, 1.6, 1.7, 1.8) skip [RED] — no UI unit test framework exists. [GREEN] = implement + build passes.
- **HintResult.Found field**: Uses `targetCells: List<Int>` (not `index`). Reducer uses `targetCells.firstOrNull()`.
- **GameViewModelTest.kt**: May need to be created in engine test directory. Verify path exists before writing.
- **Pencil button**: Already UI-only (always disabled). Safe to remove — no reducer impact.
- **Timer cancel**: Inline in `dispatch()` post-update — consistent with `GameCompleted` pattern.
- **NumberPad (FR-9)**: Already uses nested Column+Row (`for (row in 0..2)` / `for (col in 0..2)`), digit = `row*3+col+1` (1–9 only), `weight(1f).aspectRatio(1f)`. No erase button present. Task 1.9 verifies this; no structural change needed.
- **SudokuBoard (FR-11)**: Layer 2 row/col/box highlight already implemented in `drawCells` (lines 64–74 of SudokuBoard.kt). Canvas layer order: 1=base bg, 2=row/col/box highlight, 3=number-match, 4=selected, 5=conflict. Already correct. Task 1.10 verifies this; no code change expected.
- **FR-10 (Erase placement)**: Erase circular button lives in GameScreen action row. Task 1.7 step 2 explicitly confirms it remains there (not moved into NumberPad).
