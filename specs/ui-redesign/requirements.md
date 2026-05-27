# Requirements: UI Redesign

## Goal

Redesign the Sudoku app's visual layout and interaction model to match the target screenshot: horizontal two-panel layout, 3×3 number pad, circular action buttons, blue color scheme, row/col/box cell highlighting, a 3-mistake game-over limit, and a fixed hint slot that selects the hinted cell.

---

## User Stories

### US-1: Two-Panel Layout

**As a** player
**I want to** see the board on the left and all controls on the right
**So that** the board is large and controls are easy to reach without scanning vertically

**Acceptance Criteria:**
- [ ] AC-1.1: App window displays a horizontal `Row`: board occupies ~60% of width, right panel ~40%
- [ ] AC-1.2: Right panel stacks top-to-bottom: stats row → hint slot → action buttons → number pad → spacer → New Game button
- [ ] AC-1.3: Board fills its allocated width/height without clipping

---

### US-2: Stats Row

**As a** player
**I want to** see elapsed time and mistake count in the right panel
**So that** I can track game progress at a glance

**Acceptance Criteria:**
- [ ] AC-2.1: Stats row shows elapsed time formatted as `MM:SS`
- [ ] AC-2.2: Stats row shows mistake count formatted as `X/3`
- [ ] AC-2.3: A pause/resume toggle button is present in the stats row
- [ ] AC-2.4: Timer display shows `--:--` when the game is paused

---

### US-3: 3-Mistake Game-Over

**As a** player
**I want to** see a game-over dialog when I make 3 mistakes
**So that** there is a meaningful consequence for incorrect guesses

**Acceptance Criteria:**
- [ ] AC-3.1: Entering a digit that differs from the solution increments `mistakeCount`
- [ ] AC-3.2: When `mistakeCount` reaches 3, a game-over dialog appears immediately
- [ ] AC-3.3: The game-over dialog displays a "Game Over" title and an explanation (e.g., "You made 3 mistakes")
- [ ] AC-3.4: The dialog provides a "New Game" button that starts a fresh game at the current difficulty
- [ ] AC-3.5: The dialog has no dismiss-without-action path (no close button, no click-outside dismiss)
- [ ] AC-3.6: Board input is disabled while the game-over dialog is visible
- [ ] AC-3.7: `GameState` gains a `isGameOver: Boolean` field; it is `false` on new game and `true` when mistake limit is reached

---

### US-4: Circular Action Buttons

**As a** player
**I want to** tap circular Undo, Erase, and Hints buttons in the right panel
**So that** the UI feels clean and touch-friendly

**Acceptance Criteria:**
- [ ] AC-4.1: Three circular buttons rendered: Undo (`↺`), Erase (`⌫`), Hints (`💡`)
- [ ] AC-4.2: Undo button is disabled (visually dimmed) when `undoStack` is empty
- [ ] AC-4.3: Erase button erases the selected cell's digit; disabled when no cell selected or cell is a given
- [ ] AC-4.4: Hints button shows a badge with remaining count (e.g., `3`, `2`, `1`); badge reads `0` when exhausted
- [ ] AC-4.5: Hints button is disabled when `hintsRemaining == 0`
- [ ] AC-4.6: Circular buttons use `Box + Modifier.clip(CircleShape).background(color)` pattern (clip before background)
- [ ] AC-4.7: No pencil/notes button exists anywhere in the UI

---

### US-5: 3×3 Number Pad

**As a** player
**I want to** see digits 1–9 arranged in a 3×3 grid
**So that** the layout resembles a phone keypad and is spatially intuitive

**Acceptance Criteria:**
- [ ] AC-5.1: Number pad renders digits 1–9 in a 3×3 grid (rows: 1-2-3, 4-5-6, 7-8-9)
- [ ] AC-5.2: No erase button inside the number pad
- [ ] AC-5.3: Grid uses nested `Column + Row` (not `LazyVerticalGrid`)
- [ ] AC-5.4: Each digit button is square (`Modifier.weight(1f).aspectRatio(1f)`)
- [ ] AC-5.5: Tapping a digit fires `EnterDigit` for the selected cell

---

### US-6: Hint Slot and Cell Selection

**As a** player
**I want to** see a hint appear in a reserved slot and have the hinted cell selected on the board
**So that** I know exactly which cell the hint refers to without hunting for it

**Acceptance Criteria:**
- [ ] AC-6.1: A fixed-height slot (always present, same height whether empty or not) sits above the number pad in the right panel
- [ ] AC-6.2: When no hint is active, the slot is visually blank (no layout shift)
- [ ] AC-6.3: When `hintResult` is non-null, the slot shows the hint text
- [ ] AC-6.4: When a hint is revealed, `GameState.selectedIndex` is set to the hinted cell's index
- [ ] AC-6.5: The hinted cell becomes the selected cell on the board (standard selected-cell highlight applies)

---

### US-7: Row/Col/Box Cell Highlighting

**As a** player
**I want to** see all cells in the same row, column, and 3×3 box highlighted when I select a cell
**So that** I can quickly scan related cells for conflicts

**Acceptance Criteria:**
- [ ] AC-7.1: Selecting a cell highlights all cells in the same row with a light blue tint (`#D4E8FA`)
- [ ] AC-7.2: Selecting a cell highlights all cells in the same column with the same tint
- [ ] AC-7.3: Selecting a cell highlights all cells in the same 3×3 box with the same tint
- [ ] AC-7.4: The selected cell itself renders at a darker blue (distinct from the row/col/box tint)
- [ ] AC-7.5: Canvas layer order: base bg → row/col/box highlight → number-match overlay → selected cell → conflict overlay → grid lines
- [ ] AC-7.6: Deselecting a cell removes all highlights

---

### US-8: Blue Color Scheme

**As a** player
**I want to** see a blue-tinted UI (buttons, board, accents)
**So that** the app has a consistent and polished visual identity

**Acceptance Criteria:**
- [ ] AC-8.1: Primary accent color is `#3D5A9A`
- [ ] AC-8.2: New Game button background is `#5B71B9`
- [ ] AC-8.3: Row/col/box highlight tint is `#D4E8FA`
- [ ] AC-8.4: Action buttons and number pad buttons use the blue color family (not gray default)

---

### US-9: New Game Button Position

**As a** player
**I want to** find the New Game button at the bottom of the right panel
**So that** starting over is clearly separated from in-game controls

**Acceptance Criteria:**
- [ ] AC-9.1: New Game button is the bottommost element in the right panel
- [ ] AC-9.2: A `Spacer(Modifier.weight(1f))` between the number pad and New Game button pushes it to the bottom
- [ ] AC-9.3: New Game button triggers the existing new-game confirmation dialog flow

---

## Functional Requirements

| ID | Requirement | Priority | Acceptance Criteria |
|----|-------------|----------|---------------------|
| FR-1 | `GameScreen` uses `Row` layout: board `weight(0.6f)`, right panel `weight(0.4f)` | High | AC-1.1 |
| FR-2 | Right panel order: stats → hint slot → action buttons → numpad → spacer → New Game | High | AC-1.2 |
| FR-3 | `GameState` gains `isGameOver: Boolean`, defaulting to `false` | High | AC-3.7 |
| FR-4 | `applyEnterDigit` sets `isGameOver = true` when `mistakeCount` reaches 3 | High | AC-3.2 |
| FR-5 | Game-over dialog shown when `isGameOver == true`; no dismiss path except New Game | High | AC-3.3–3.6 |
| FR-6 | `mistakeCount` and `hintsRemaining` reset to 0 / 3 on `PuzzleGenerated` | High | AC-3.1, AC-4.4 |
| FR-7 | `RequestHint` reducer sets `selectedIndex` to hinted cell index | High | AC-6.4 |
| FR-8 | Pencil/notes button removed from all UI components | High | AC-4.7 |
| FR-9 | `NumberPad` rewritten as nested `Column + Row` 3×3 grid, digits 1–9 only | High | AC-5.1–5.4 |
| FR-10 | Erase action moved to circular button in right panel | High | AC-4.3 |
| FR-11 | Row/col/box highlight inserted as Canvas layer 2 in `SudokuBoard` | High | AC-7.1–7.5 |
| FR-12 | Stats row displays `MM:SS` timer and `X/3` mistake count | Medium | AC-2.1–2.4 |
| FR-13 | Hints circular button shows `BadgedBox` with remaining count | Medium | AC-4.4–4.5 |
| FR-14 | All blue color constants defined as named `Color` vals in a single location | Medium | AC-8.1–8.4 |
| FR-15 | Fixed-height hint slot always occupies space; content visible only when `hintResult != null` | Medium | AC-6.1–6.3 |

---

## Non-Functional Requirements

| ID | Requirement | Metric | Target |
|----|-------------|--------|--------|
| NFR-1 | Build succeeds with no new dependencies | `./gradlew :app:build` exits 0 | No additions to `build.gradle` |
| NFR-2 | Engine tests continue to pass | `./gradlew :engine:test` | 0 failures |
| NFR-3 | App launches and renders without crash | `./gradlew :app:run` | No exception on startup |
| NFR-4 | No Compose recomposition warnings from `LazyVerticalGrid` in non-lazy parent | Manual inspection | 0 warnings |
| NFR-5 | Circular button clip order correct | Code review | `clip(CircleShape)` before `.background()` in all circular buttons |

---

## Glossary

- **Given**: A pre-filled digit supplied by the puzzle generator; cannot be erased or overwritten by the player.
- **Conflict**: A cell whose digit violates Sudoku rules (duplicate in row/col/box) or differs from the solution.
- **Hint slot**: A reserved fixed-height UI area above the number pad that displays hint text when available.
- **Row/col/box highlight**: Light blue tint applied to all cells sharing a row, column, or 3×3 box with the selected cell.
- **Circular action button**: A button rendered as a filled circle using `Box + clip(CircleShape) + background`.
- **Badge**: A small pill overlay on a button showing a numeric count (Material2 `BadgedBox + Badge`).
- **Game-over**: State reached when `mistakeCount == 3`; blocks further input and shows a dialog.
- **Stats row**: Horizontal row in the right panel showing elapsed time and mistake count.
- **Primary accent**: `#3D5A9A` — main blue used for selected-cell overlay and action button backgrounds.

---

## Out of Scope

- Pencil / notes mode (explicitly removed; no UI surface for it)
- Redo button (not in target screenshot; `redoStack` state remains but no UI button)
- Difficulty selector UI changes (difficulty stays on existing New Game dialog)
- Sound / haptic feedback
- Keyboard input handling changes
- Persistent high-score or statistics storage
- Accessibility / screen reader support
- Dark mode / theme switching
- Any change to the puzzle-generation engine (`sudoku.engine` package)

---

## Dependencies

- `GameState.isGameOver` field must exist before game-over dialog UI can be wired up (FR-3 before FR-5)
- `RequestHint` must set `selectedIndex` (FR-7) before hint-slot cell-selection behavior can be tested (AC-6.4–6.5)
- Row/col/box highlight layer (FR-11) depends on correct canvas layer ordering; must not regress conflict/selection overlays
- Material2 `BadgedBox` / `Badge` already available via existing `androidx.compose.material` dependency — no new dep needed

---

## Success Criteria

- `./gradlew :app:build` exits 0 with no new dependencies added
- `./gradlew :engine:test` reports 0 failures
- Visual inspection via `./gradlew :app:run` confirms: two-panel layout, 3×3 numpad, circular buttons, blue scheme, row/col/box highlighting
- Entering 3 wrong digits triggers the game-over dialog; clicking New Game resets `mistakeCount` to 0
- Requesting a hint selects the hinted cell on the board
- No pencil button visible anywhere in the UI

---

## Unresolved Questions

- Should `isGameOver` live in `GameState` as a boolean flag, or should it be modelled as a new `GameStatus` sealed class (`Playing | GameOver | Complete`)? A sealed class would be cleaner long-term but adds refactor surface — flag is simpler given current scope.
- Should the game-over dialog offer difficulty selection, or always restart at the current difficulty? (User said "current difficulty" — recorded as decision above.)
- Exact height of the fixed hint slot — should it match one line of text (~20dp) or be taller to show multi-line hints?

## Next Steps

1. Approve or amend these requirements
2. Run `/design` to generate the technical design (component breakdown, state changes, file-by-file plan)
3. Run `/tasks` to break design into implementation tasks
4. Implement via `/implement`
