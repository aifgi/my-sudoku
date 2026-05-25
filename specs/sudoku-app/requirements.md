---
spec: sudoku-app
phase: requirements
created: 2026-05-25
---

# Requirements: sudoku-app

## Goal

A complete desktop Sudoku game for Windows, macOS, and Linux — distributed as platform-native installers — that lets players generate, solve, and get hinted through standard 9x9 Sudoku puzzles across four difficulty levels.

---

## User Stories

### US-1: New Game / Difficulty Selection

**As a** player
**I want to** start a new game at a chosen difficulty level
**So that** I can pick a challenge appropriate to my skill

**Acceptance Criteria:**
- [ ] AC-1.1: Home screen shows four difficulty options: Easy, Medium, Hard, Expert
- [ ] AC-1.2: Selecting a difficulty generates a new valid 9x9 puzzle with exactly one solution and transitions to the game screen
- [ ] AC-1.3: Starting a new game while one is in progress prompts confirmation before discarding the current game
- [ ] AC-1.4: "New Game" action is accessible from the game screen (not just home screen)
- [ ] AC-1.5: Each generated puzzle has exactly one solution (uniqueness guarantee)
- [ ] AC-1.6: Ctrl+N (Cmd+N on macOS) triggers "New Game" from the game screen (keyboard shortcut for NFR-009)

---

### US-2: Playing the Game — Cell Selection and Input

**As a** player
**I want to** select cells and enter digits using mouse/keyboard/number pad
**So that** I can fill in the puzzle with my answers

**Acceptance Criteria:**
- [ ] AC-2.1: Clicking or tapping any cell (given or user-filled) selects it with a visual highlight
- [ ] AC-2.2: When a cell containing a digit is selected, all other cells on the board containing the same digit are highlighted in a distinct secondary color (number highlight)
- [ ] AC-2.3: Given (pre-filled) cells can be selected (showing number highlight) but their digit cannot be modified
- [ ] AC-2.4: With a non-given cell selected, pressing digit 1–9 fills the cell with that digit
- [ ] AC-2.5: Pressing Backspace, Delete, or 0 clears a selected user-filled cell
- [ ] AC-2.6: Arrow keys move selection to adjacent cells (stops at grid border)
- [ ] AC-2.7: On-screen number pad (digits 1–9 + Erase button) provides the same input as keyboard
- [ ] AC-2.8: Pressing Escape deselects the current cell
- [ ] AC-2.9: Tab / Shift+Tab cycle selection through cells

---

### US-3: Real-Time Conflict Highlighting

**As a** player
**I want to** see conflicting cells highlighted immediately as I fill in numbers
**So that** I can catch errors early without waiting until the puzzle is complete

**Acceptance Criteria:**
- [ ] AC-3.1: When a digit is placed that duplicates another digit in the same row, the conflicting cells are visually distinguished (e.g., red background or red text) immediately — no button press required
- [ ] AC-3.2: When a digit is placed that duplicates another in the same column, same conflict highlight applies
- [ ] AC-3.3: When a digit is placed that duplicates another in the same 3×3 box, same conflict highlight applies
- [ ] AC-3.4: Conflict highlight is removed immediately when the offending digit is erased or corrected
- [ ] AC-3.5: Given (pre-filled) cells can participate in conflict detection — if a user entry conflicts with a given, both are highlighted
- [ ] AC-3.6: Correct entries with no conflicts receive no conflict highlight (normal appearance)
- [ ] AC-3.7: Conflict highlighting and number highlighting (AC-2.2) are visually distinct from each other and from the selection highlight — three different visual states must be distinguishable simultaneously

---

### US-4: Hint System

**As a** player
**I want to** request a hint that highlights relevant cells and names the solving technique
**So that** I can learn Sudoku strategies rather than just having answers revealed

**Acceptance Criteria:**
- [ ] AC-4.1: A "Hint" button is accessible on the game screen
- [ ] AC-4.2: Requesting a hint identifies the simplest applicable technique that can make progress on the current board state
- [ ] AC-4.3: The hint highlights the relevant cell(s) (e.g., the target cell and the cells that constrain it) in a visually distinct color
- [ ] AC-4.4: The hint displays a human-readable explanation naming the technique and the location, e.g. "Naked Single at R3C5: only digit 7 can go here"
- [ ] AC-4.5: The hint does NOT automatically fill in the answer — the player must enter the digit themselves
- [ ] AC-4.6: If no technique in the supported set applies, display "No hint available"
- [ ] AC-4.7: Hint highlights are dismissed when the player selects a different cell or enters a digit
- [ ] AC-4.8: Supported techniques (in order of preference): Naked Single, Hidden Single, Naked Pair, Hidden Pair, Pointing Pair
- [ ] AC-4.9: Pressing H on the keyboard while a game is active triggers the hint (same as clicking "Hint" button)

**Hint Scope Limitation (v1):**
The hint engine supports techniques up to and including Pointing Pair. Hard and Expert puzzles may require techniques beyond this set. When the current board state requires a technique not in the supported set, "No hint available for this difficulty level" is displayed. This is intentional product behavior in v1, not an error — Hard and Expert players are expected to have reduced hint support. Full hint coverage for Hard/Expert is deferred to v2.

---

### US-9: Hard / Expert Player Experience with Hints

**As a** Hard or Expert player
**I want to** understand why a hint is unavailable rather than receiving a silent failure
**So that** I know the limitation is intentional and am not left wondering if the app is broken

**Acceptance Criteria:**
- [ ] AC-9.1: When a Hard or Expert puzzle's board state requires a technique beyond Pointing Pair, the hint response is "No hint available for this difficulty level" (not a generic "No hint available")
- [ ] AC-9.2: The message is displayed in the same location/style as normal hint explanations
- [ ] AC-9.3: The message is dismissed when the player selects a cell or enters a digit (same as AC-4.7)
- [ ] AC-9.4: If a Hard/Expert board happens to be resolvable by a supported technique at the current state (e.g., early in the game), a valid hint IS shown — the restriction applies only when no supported technique can make progress

---

### US-5: Undo / Redo

**As a** player
**I want to** undo and redo my moves
**So that** I can experiment without fear of ruining my progress

**Acceptance Criteria:**
- [ ] AC-5.1: Ctrl+Z (Cmd+Z on macOS) undoes the last digit entry or erasure
- [ ] AC-5.2: Ctrl+Y or Ctrl+Shift+Z (Cmd+Shift+Z on macOS) redoes the last undone action
- [ ] AC-5.3: Undo/Redo buttons are visible on the game screen
- [ ] AC-5.4: Undo stack goes back to the start of the current game (unlimited within session)
- [ ] AC-5.5: Starting a new game clears the undo/redo stack
- [ ] AC-5.6: Undo/Redo is not available for given (pre-filled) cells — they are immutable

---

### US-6: Count-Up Timer with Pause/Resume

**As a** player
**I want to** see a running timer and be able to pause it
**So that** I can track how long a puzzle takes and take breaks without the timer advancing

**Acceptance Criteria:**
- [ ] AC-6.1: Timer starts at 00:00 when a new game begins and counts up in seconds (MM:SS format)
- [ ] AC-6.2: A Pause button stops the timer and obscures the board (blurs or hides cell values) to prevent cheating
- [ ] AC-6.3: A Resume button restarts the timer from where it paused and reveals the board
- [ ] AC-6.4: Timer display is visible at all times during an active game
- [ ] AC-6.5: Timer stops automatically when the game is completed (puzzle solved)
- [ ] AC-6.6: Timer displays HH:MM:SS format when the game exceeds 60 minutes
- [ ] AC-6.7: Pressing P or Space toggles Pause/Resume from the keyboard (keyboard shortcut for NFR-009)

---

### US-7: Game Completion

**As a** player
**I want to** see a success screen when I correctly solve the puzzle
**So that** I get clear confirmation and feel rewarded for completing the puzzle

**Acceptance Criteria:**
- [ ] AC-7.1: When all 81 cells are filled and no conflicts exist, a completion screen/dialog appears automatically
- [ ] AC-7.2: The completion screen shows the difficulty level and the elapsed time
- [ ] AC-7.3: The completion screen offers "New Game" (returns to difficulty picker) and "Back to Home" actions
- [ ] AC-7.4: The completion screen does NOT appear if conflicts remain, even if all cells are filled
- [ ] AC-7.5: The board remains visible (read-only) beneath/behind the completion overlay
- [ ] AC-7.6: When the player attempts to close the app mid-game (window close button or OS-level quit), a confirmation dialog is shown ("Quit and lose current progress?") before the app exits — no game state is saved on exit

---

### US-8: App Launch and Distribution

**As a** player
**I want to** install and launch the app on Windows, macOS, or Linux with no manual setup
**So that** I can start playing without configuring a Java runtime or build tools

**Acceptance Criteria:**
- [ ] AC-8.1: A Windows installer (`.exe` or `.msi`) installs the app and creates a Start Menu entry
- [ ] AC-8.2: A macOS installer (`.dmg`) delivers a standard `.app` bundle draggable to Applications
- [ ] AC-8.3: A Linux package (`.deb`) installs the app via standard package manager
- [ ] AC-8.4: None of the above require a pre-installed JDK or JRE on the end-user machine (JRE is bundled)
- [ ] AC-8.5: App launches to the home/difficulty-picker screen within 5 seconds on a modern machine (SSD, 8 GB RAM)
- [ ] AC-8.6: App window is resizable and the board maintains correct aspect ratio when resized
- [ ] AC-8.7: App has a distinct window title (e.g., "Sudoku")

---

## Functional Requirements

| ID | Requirement | Priority | Acceptance Criteria |
|----|-------------|----------|---------------------|
| FR-001 | Generate a valid 9x9 puzzle using Las Vegas randomized backtracking fill + dig-holes algorithm | High | Generated board passes constraint validation; all given cells belong to the unique solution |
| FR-002 | Uniqueness check: every generated puzzle has exactly one valid solution | High | Solver returns exactly 1 solution for any generated puzzle; verified in unit tests |
| FR-003 | Support four difficulty levels: Easy, Medium, Hard, Expert | High | Each level uses a defined technique-threshold to grade difficulty before serving the puzzle |
| FR-004 | Easy puzzles solvable using Naked Singles and Hidden Singles only | High | Automated solver using only those two techniques reaches a complete solution |
| FR-005 | Medium puzzles require at most Naked/Hidden Pairs beyond Easy techniques | Medium | Easy solver stalls; medium solver (adds pairs) completes it |
| FR-006 | Hard puzzles require at most Pointing Pairs (capped to techniques the hint engine supports — Naked/Hidden Triples are explicitly excluded from v1 difficulty grading) | Medium | Medium solver stalls; hard solver (adds Pointing Pairs) completes it; Naked/Hidden Triple classifier not required |
| FR-007 | Expert puzzles require X-Wing or more advanced techniques | Medium | Hard solver stalls; expert solver (adds X-Wing) completes it |
| FR-008 | Conflict detection: identify duplicate digits in shared row, column, or 3×3 box | High | All three conflict classes highlighted in real time |
| FR-009 | Number highlighting: selecting any cell with a digit highlights all cells containing the same digit | High | All matching cells receive secondary highlight; selecting an empty cell shows no number highlight |
| FR-010 | Hint engine runs constraint propagation and returns the simplest applicable technique | High | Returns structured result: technique name, target cell(s), peer cells, explanation string |
| FR-011 | Undo/redo stack maintained in-memory for the full session of the current game | High | Arbitrary undo depth from current state back to initial board |
| FR-012 | Count-up timer with pause that obscures the board | High | Timer value frozen during pause; board obscured |
| FR-013 | Auto-detect puzzle completion (all cells filled, zero conflicts) | High | Completion event fires without player action |
| FR-014 | Number pad widget renders digits 1–9 and an Erase button | High | Each button triggers the equivalent keyboard input |
| FR-015 | Keyboard navigation: arrow keys move cell selection; digit keys input values | High | All four arrow directions work; digits 1–9 and 0/Backspace/Delete work |
| FR-016 | New game confirmation dialog when an in-progress game exists | Medium | Dialog appears if any user cell has been filled; skipped if board is untouched |
| FR-017 | Given cells are visually distinct from user-entered cells | High | Different font weight, color, or background distinguishes givens from player input |
| FR-018 | Three simultaneous visual states on cells: selection, number-match highlight, conflict highlight | High | All three states are visually distinguishable; they can coexist on different cells simultaneously |
| FR-019 | Quit-on-close confirmation: closing the app window while a game is in progress shows a confirmation dialog | High | Dialog appears on OS close event mid-game; no dialog if no game is in progress |

---

## Non-Functional Requirements

| ID | Requirement | Metric | Target |
|----|-------------|--------|--------|
| NFR-001 | Startup time | Time from app launch to interactive home screen | < 5 seconds on SSD, 8 GB RAM |
| NFR-002 | Puzzle generation time | Time from difficulty selected to game screen ready | < 2 seconds for any difficulty — **Note:** Expert puzzle generation with uniqueness checking may require Dancing Links or an optimized solver; naive backtracking is unlikely to meet this target for Expert (see Unresolved Questions) |
| NFR-003 | Hint response time | Time from "Hint" button press to hint displayed | < 500 ms |
| NFR-004 | UI frame rate | Frames per second during normal interaction | ≥ 30 fps sustained |
| NFR-005 | Installer size | Compressed installer size per platform | ≤ 200 MB per platform |
| NFR-006 | Platform support | Operating systems | Windows 10+, macOS 12+, Ubuntu 22.04+ |
| NFR-007 | No external runtime required | User machine prerequisites | Zero — JRE bundled via jpackage |
| NFR-008 | Minimum window size | Smallest usable window without scrolling | 600 × 700 px |
| NFR-009 | Keyboard-only play | Full game playable without mouse | All actions reachable via keyboard shortcuts |
| NFR-010 | Build reproducibility | Cross-platform packaging | Each platform build triggered independently (CI matrix); no shared native dependencies |
| NFR-011 | Engine unit test coverage | Line coverage on `engine/` package | ≥ 80% |
| NFR-012 | Color-blind accessibility | The three cell visual states (selection, number-match, conflict) must be distinguishable by means beyond color alone | Each state uses a distinct visual cue in addition to color: e.g., border thickness variation, outline style, or pattern — so players with red-green or blue-yellow color vision deficiency can distinguish all three states; if not achievable in v1, this is a documented known limitation deferred to v2 |

---

## Glossary

| Term | Definition |
|------|------------|
| **Given** | A pre-filled cell provided by the puzzle; its digit cannot be modified by the player |
| **Naked Single** | A cell where only one digit is possible after eliminating all peers' values |
| **Hidden Single** | A digit that can appear in only one cell within a row, column, or box, even though that cell has multiple candidates |
| **Naked Pair** | Two cells in the same unit sharing exactly the same two candidates; those candidates can be eliminated from all other cells in the unit |
| **Hidden Pair** | Two digits that appear as candidates in exactly two cells of a unit; all other candidates can be removed from those two cells |
| **Pointing Pair** | When a candidate digit in a box is confined to one row or column of that box, it can be eliminated from the rest of that row/column outside the box |
| **X-Wing** | A pattern where a candidate digit appears in exactly two cells in each of two rows, all in the same two columns — allows elimination from the rest of those columns |
| **Uniqueness Guarantee** | Property of a generated puzzle: exactly one valid complete solution exists |
| **Dig Holes** | Puzzle generation phase that removes givens one at a time while preserving uniqueness |
| **Las Vegas Algorithm** | Randomized backtracking used to generate a fully-valid complete 9x9 grid |
| **MVI** | Model-View-Intent — unidirectional state management pattern used in the UI architecture |
| **jpackage** | JDK tool that bundles a JVM application + JRE into a platform-native installer |
| **Unit** | A row, column, or 3×3 box — the three groupings in which no digit may repeat |
| **Conflict** | Two identical digits appearing in the same unit |
| **Session** | A single continuous app run from launch to close; no state persists across sessions |
| **Number Highlight** | Secondary visual highlight applied to all cells containing the same digit as the currently selected cell |

---

## Out of Scope (v2)

- **Pencil marks / candidate notes**: toggle mode to write small candidate digits in cells
- **Statistics and history**: tracking games played, win rate, average solve times across sessions
- **Color themes / dark mode**: multiple visual themes beyond the default
- **Custom puzzle import**: entering or loading an externally-sourced puzzle
- **Sound effects and music**: audio feedback for any action
- **Online features**: leaderboards, cloud sync, multiplayer
- **Puzzle variants**: 6×6, 12×12, irregular boxes, Killer Sudoku, etc.
- **Solution reveal**: "give up" button that fills in the complete answer
- **Save/resume across sessions**: persisting an in-progress game between app launches
- **Hint depth beyond Pointing Pairs**: Swordfish, Jellyfish, coloring, ALS, etc.
- **Distribution via package managers**: winget, Homebrew, apt repository — GitHub Releases binary download is sufficient for v1

---

## Dependencies

| Dependency | Version Constraint | Purpose |
|------------|-------------------|---------|
| Kotlin | ≥ 2.0 | Primary language |
| Compose Multiplatform Desktop | ≥ 1.7 | UI framework |
| Gradle | ≥ 8.5 | Build system |
| JDK (build machine only) | ≥ 21 (LTS) | Compilation + jpackage toolchain |
| jpackage | Bundled with JDK 21+ | Platform-native installer creation |
| WiX Toolset (Windows build only) | ≥ 4.0 | Required by jpackage for `.msi` generation |

**Architecture Constraint — Persistence Layer:** `GameRepository` (a persistence module visible in reference architecture diagrams) is explicitly omitted in v1. There is no database, file-based save, or repository abstraction layer. All game state lives in memory for the duration of a session only. `GameRepository` is a v2 concern if save/resume across sessions is added.

---

## Success Criteria

- Player can install and launch on Windows, macOS, and Linux without pre-installing Java
- Full game loop (select difficulty → generate puzzle → solve → see completion screen) works on all three platforms
- All four difficulty levels produce puzzles solvable using only the techniques specified for that level (verified by automated solver tests)
- Every generated puzzle has exactly one solution (unit-tested)
- Hint system correctly identifies and explains at least the five supported techniques
- Real-time conflict highlighting correctly flags all three conflict classes with no false positives
- Number highlighting correctly shows all matching digits when any cell is selected

---

## Unresolved Questions

- **Minimum OS targets**: macOS 12 and Ubuntu 22.04 assumed — confirm if older versions must be supported (affects JDK and Compose version floor)
- **Distribution channel**: GitHub Releases assumed for v1 — confirm if any other channel is needed
- **WiX on CI**: `.msi` packaging requires WiX on the Windows build agent — confirm CI environment
- **Difficulty threshold tuning**: Medium/Hard/Expert boundaries are subjective and require empirical calibration — agree on a "good enough" acceptance bar before launch
- **Hint engine scope**: Hard/Expert puzzles that require techniques beyond Pointing Pair return "No hint available for this difficulty level" — this is documented intentional behavior; confirm the distinct message wording is acceptable vs. the generic "No hint available"
- **Expert generation performance**: Meeting the < 2s NFR-002 target for Expert puzzles likely requires Dancing Links (DLX) or an equivalently optimized uniqueness checker. Naive backtracking may exceed this target. Confirm whether DLX is in scope for v1 or whether the Expert generation time target should be relaxed (e.g., < 5s)
- **Color-blind accessibility (NFR-012)**: Confirm whether non-color visual differentiation for the three cell states is a v1 hard requirement or a known v1 limitation deferred to v2

---

## Next Steps

1. Get requirements approved (user sign-off on scope, out-of-scope list, and unresolved questions above)
2. Proceed to Design phase: define module structure, MVI state model, engine interfaces, and UI component hierarchy
3. Proceed to Tasks phase: break design into ordered implementation tasks with clear done criteria