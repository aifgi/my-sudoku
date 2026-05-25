---
spec: sudoku-app
phase: research
created: 2026-05-25
---

# Research: sudoku-app

## Executive Summary

The project is a blank slate (no source files). The target is a desktop Sudoku app (Windows/Linux/macOS) packaged as a single binary. **Compose Multiplatform Desktop** is the strongest GUI option with Kotlin, but it cannot produce a true JVM-free binary — it bundles a JRE, producing a ~100–150 MB installer. If a true single self-contained binary with no JVM is required, the options narrow to non-Kotlin stacks (Go + Ebiten, Rust + egui) or a terminal/TUI approach. This is the primary question that must be resolved before requirements.

---

## Codebase Analysis

| Item | Detail |
|------|--------|
| Language indices in IDE | TypeScript AND Kotlin both present in `workspace.xml` |
| Build system | None — only `sudoku.iml` (IntelliJ generic module descriptor) |
| Source files | Zero — project is completely empty |
| IDE | IntelliJ IDEA Ultimate (IU-253.31033.145) |
| Git | Initialized, single commit with IDE files only |

**Conclusion**: No technology is locked in. Full stack freedom.

---

## Technology Landscape

### "Single Binary" — What It Means in Practice

| Approach | Binary size | JVM required | True single file | Notes |
|----------|------------|-------------|-----------------|-------|
| Compose Desktop + jpackage (installer) | ~100–150 MB | Bundled | No (installer package) | `.exe`/`.msi`/`.dmg`/`.deb` — JRE is embedded |
| Compose Desktop + fat JAR | ~70–90 MB | Must be installed | Yes (single `.jar`) | Requires `java` on PATH; not zero-dependency |
| Kotlin/Native | Small (~5–10 MB) | No | Yes | Compose UI NOT available on K/N desktop |
| Go + Fyne/Ebiten | ~10–20 MB | No | Yes | True single binary; cross-compiles |
| Rust + egui/iced | ~5–15 MB | No | Yes | True single binary; excellent cross-compile |
| Python + PyInstaller | ~50–80 MB | No | Yes (bundled Python) | Single executable but larger |

**Key finding**: Compose Multiplatform Desktop targets JVM only. There is no Kotlin path to a true JVM-free single binary with a full GUI. Source: [Kotlin Compose native distributions docs](https://kotlinlang.org/docs/multiplatform/compose-native-distribution.html).

### Viable Stacks by Priority

**Option A — Compose Multiplatform Desktop (Kotlin, JVM bundled)**
- Pros: Kotlin-native, declarative UI, Material3, hot-reload, proven Sudoku prior art
- Cons: Installer is ~100–150 MB; not a single file — it's a platform-native installer
- Distribution: `.exe`/`.msi` (Windows), `.dmg`/`.pkg` (macOS), `.deb`/`.rpm` (Linux)
- True single binary: No. jpackage bundles JRE into an installer, not one file.

**Option B — Go + Ebiten (game library)**
- Pros: True single binary (~15 MB), cross-compiles to all 3 OS from one machine, fast, zero runtime
- Cons: Different language from Kotlin; developer must know Go
- Sudoku prior art: Abundant (Go is popular for game examples)
- Distribution: Single `.exe`/binary with no dependencies

**Option C — Rust + egui or iced**
- Pros: True single binary (~10 MB), excellent performance, zero runtime
- Cons: Steeper learning curve, different from Kotlin
- Distribution: Single `.exe`/binary

**Option D — Kotlin + TUI (Lanterna / Mordant)**
- Pros: Single JAR runnable with `java -jar`; can be wrapped in GraalVM native-image for true binary; Kotlin throughout
- Cons: Terminal UI — less visual polish; GraalVM native-image requires additional tooling
- GraalVM native-image: Compiles JVM app to single native binary; Kotlin is supported

**Option E — Kotlin + GraalVM Native Image + Compose (experimental)**
- Compose Desktop + GraalVM native-image: Not officially supported as of 2025. High risk.

### Prior Art (Sudoku-specific)

| Project | Stack | Packaging |
|---------|-------|-----------|
| [ComposeArcade](https://github.com/aaronoe/ComposeArcade) | Kotlin Multiplatform + Compose | Android + Desktop (JVM) |
| [LibreSudoku](https://github.com/kaajjo/LibreSudoku) | Kotlin + Jetpack Compose + Material3 | Android APK |
| [LPRegen/sudoku](https://github.com/LPRegen/sudoku) | React + TypeScript | Web |

No identified prior art using true single-binary Kotlin for a desktop Sudoku app.

---

## Sudoku Domain Knowledge

### Puzzle Generation

Standard two-phase "dig holes" approach:

1. **Fill a valid complete grid** — Las Vegas algorithm (randomized backtracking) to produce a random fully-valid 9x9 solution
2. **Dig holes** — repeatedly remove cells in random order; after each removal, run uniqueness check; if two solutions exist, restore the cell and skip it
3. **Uniqueness check** — solver with early-exit on second solution found; ~24× faster than naive full solve

Sources: [Sudoku Puzzles Generating: from Easy to Evil (PDF)](https://zhangroup.aporc.org/images/files/Paper_3485.pdf), [101 Computing](https://www.101computing.net/sudoku-generator-algorithm/)

### Solving Algorithms

| Algorithm | Speed | Complexity | Best For |
|-----------|-------|------------|---------|
| Backtracking (DFS) | Moderate | Low | General solver, always correct |
| Constraint Propagation + Backtracking | Fast (ms) | Medium | Human-like solving + hint engine |
| Dancing Links (Algorithm X) | Microseconds | High | Uniqueness validation during generation |

**Recommended**: Constraint propagation + backtracking hybrid for hint engine; Dancing Links for generation uniqueness checks.

Source: [Wikipedia — Sudoku solving algorithms](https://en.wikipedia.org/wiki/Sudoku_solving_algorithms)

### Difficulty Grading

Technique-based grading (mirrors human solving progression):

| Level | Required Techniques |
|-------|---------------------|
| Easy | Naked singles, hidden singles only |
| Medium | Naked/hidden pairs |
| Hard | Naked/hidden triples, pointing pairs |
| Expert | X-Wing, Swordfish, advanced eliminations |

**Caveat**: No mathematical standard exists. Grading is technique-hierarchy-based and inherently subjective. Thresholds require tuning.

Sources: [Kevin Hooke — Grading Sudoku difficulty](https://www.kevinhooke.com/2021/07/23/grading-the-difficulty-of-a-sudoku-puzzle/), [arXiv — Difficulty Rating overview](https://arxiv.org/pdf/1403.7373)

### Hint System

Constraint propagation logic doubles as the hint engine:
1. Run human-technique solver step-by-step
2. Identify the simplest applicable technique that makes progress
3. Expose as hint: highlight relevant cells, explain the technique

Technique order: naked singles → hidden singles → naked/hidden pairs → pointing pairs → triples → X-Wing

Source: [SudokuWiki — Hidden Candidates](https://www.sudokuwiki.org/hidden_candidates)

---

## Recommended Approach

### Recommendation (pending "single binary" clarification)

**If "single binary" means a platform-native installer (common understanding)**:
- **Kotlin + Compose Multiplatform Desktop + jpackage**
- Produces `.exe`/`.msi`/`.dmg`/`.deb` installers; JRE embedded; no JDK needed on end-user machine
- Architecture: single Gradle module (no KMP needed for desktop-only); MVI state management
- Size: ~100–150 MB installer

**If "single binary" means a true zero-dependency single executable file**:
- **Go + Ebiten** — simplest path; single `.exe`/binary; ~15 MB; excellent for game-style rendering
- Or: Kotlin + GraalVM native-image + TUI (no Compose) — stays in Kotlin, produces real binary, terminal UI

### Architecture (Compose Desktop path)

```
src/
  main/kotlin/
    sudoku/
      engine/
        Board.kt          — 9x9 grid model, candidate tracking
        Generator.kt      — Las Vegas fill + dig holes
        Solver.kt         — constraint propagation + backtracking
        Grader.kt         — technique-based difficulty scoring
        HintEngine.kt     — step-by-step technique application
      ui/
        App.kt            — root Compose entry point
        GameScreen.kt     — board + numpad + controls
        HomeScreen.kt     — difficulty picker + stats
        components/
          SudokuBoard.kt
          NumberPad.kt
          TimerDisplay.kt
      state/
        GameState.kt      — immutable state model
        GameIntent.kt     — all user actions
        GameViewModel.kt  — MVI reducer
      persistence/
        GameRepository.kt — save/load in-progress games
```

### Key UI Components

- Board: fixed-aspect-ratio grid, tap/click cell selection, conflict highlighting
- Number pad: digits 1–9 + erase; large tap targets
- Pencil marks (candidate notes): toggle mode
- Timer: count-up display
- Undo/redo stack (in-memory)

---

## Feasibility

### Complexity Assessment

| Component | Effort | Risk |
|-----------|--------|------|
| Project setup (Compose Desktop + Gradle) | S | Low |
| Puzzle generation (Las Vegas + dig holes) | M | Low |
| Backtracking solver | S | Low |
| Constraint propagation solver | M | Medium |
| Dancing Links uniqueness check | L | Medium — complex data structure |
| Board UI (Compose) | M | Low |
| Difficulty grading | M | Medium — threshold tuning needed |
| Hint engine | L | Medium — full technique library is large |
| Pencil marks / notes | S | Low |
| Statistics / history | M | Low |
| jpackage distribution | S | Low — well-documented |
| GraalVM native-image (if required) | L | High — Compose not supported |

### MVP Scope (recommended)

1. 9x9 Sudoku only
2. 4 difficulty levels (Easy/Medium/Hard/Expert)
3. Puzzle generation with uniqueness guarantee
4. Backtracking solver (for solution reveal)
5. Basic hint (reveal one naked single + highlight)
6. Timer (count-up), undo/redo
7. Windows + macOS + Linux via jpackage

**Exclude from MVP**: Full hint technique library, custom puzzle import, statistics persistence, 6x6/12x12 variants

### Key Risks

| Risk | Severity | Mitigation |
|------|----------|-----------|
| "Single binary" definition mismatch | High | Clarify before implementation starts |
| Compose Desktop jpackage produces installer, not single file | High | Decide if installer is acceptable |
| Difficulty grading subjectivity | Medium | Start with simple thresholds, iterate |
| Dancing Links complexity | Medium | Can substitute naive uniqueness check (~100ms, acceptable for generation) |
| Cross-platform jpackage (must build on each OS) | Medium | Use CI matrix (GitHub Actions) |

---

## Quality Commands

No build system exists yet. After Gradle setup:

| Type | Command | Source |
|------|---------|--------|
| Build | `./gradlew build` | Gradle |
| Run | `./gradlew run` | Compose Desktop plugin |
| Package | `./gradlew packageDistributionForCurrentOS` | Compose Desktop plugin |
| Test | `./gradlew test` | Gradle |

---

## Verification Tooling

No tooling exists yet.

**Project Type**: Desktop GUI App (Compose Multiplatform Desktop)
**Verification Strategy**: `./gradlew run` to launch app interactively; `./gradlew test` for unit tests on engine logic; `./gradlew packageDistributionForCurrentOS` to verify packaging

---

## Related Specs

None — this is the first spec in this project.

---

## Open Questions

1. **"Single binary" definition** — does this mean a true single zero-dependency executable, or a self-contained installer that bundles the JRE? These are fundamentally different and determine the tech stack.
2. **Language preference** — is Kotlin required, or is Go/Rust acceptable if it produces a true single binary?
3. **UI vs TUI** — graphical window, or terminal/text UI acceptable?
4. **Minimum OS versions** — Windows 10+? macOS 12+? Which Linux distros?
5. **Persistence** — save in-progress games between sessions? High score / statistics?
6. **Timer behavior** — count-up or count-down? Penalty for incorrect inputs?
7. **Input validation mode** — show conflicts immediately (highlight) or only on puzzle completion?
8. **Custom puzzles** — import/create feature in scope?
9. **Hint depth** — just "reveal one cell" or full technique explanations?
10. **Distribution channel** — direct download, GitHub Releases, package manager (winget/homebrew/apt)?

---

## Sources

- [Wikipedia — Sudoku solving algorithms](https://en.wikipedia.org/wiki/Sudoku_solving_algorithms)
- [Kevin Hooke — Grading the difficulty of a Sudoku puzzle](https://www.kevinhooke.com/2021/07/23/grading-the-difficulty-of-a-sudoku-puzzle/)
- [arXiv — Difficulty Rating of Sudoku Puzzles: An Overview and Evaluation](https://arxiv.org/pdf/1403.7373)
- [Sudoku Puzzles Generating: from Easy to Evil (PDF)](https://zhangroup.aporc.org/images/files/Paper_3485.pdf)
- [101 Computing — Sudoku Generator Algorithm](https://www.101computing.net/sudoku-generator-algorithm/)
- [SudokuWiki — Hidden Candidates](https://www.sudokuwiki.org/hidden_candidates)
- [GitHub — ComposeArcade (KMP Sudoku, Android + Desktop)](https://github.com/aaronoe/ComposeArcade)
- [GitHub — LibreSudoku (Android, Kotlin + Compose + Material3)](https://github.com/kaajjo/LibreSudoku)
- [Kotlin Multiplatform — native distributions docs](https://kotlinlang.org/docs/multiplatform/compose-native-distribution.html)
- [Compose Multiplatform — official](https://kotlinlang.org/compose-multiplatform/)
- [ResearchGate — Rating and Generating Sudoku Puzzles](https://www.researchgate.net/publication/232628110_Rating_and_Generating_Sudoku_Puzzles)
