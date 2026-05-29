---
spec: difficulty-modes
phase: research
created: 2026-05-28
---

# Research: difficulty-modes

## Executive Summary

The current engine grades most real-world puzzles as EASY because it lacks intermediate and advanced techniques (naked/hidden triples, Swordfish, Y-Wing, etc.). A given-based mode is a practical alternative: it does not require grading accuracy but needs a modified Generator that stops digging holes at a target count. The two modes serve meaningfully different user needs — technique-based is "challenge by logic depth", given-based is "challenge by information scarcity" — and should be surfaced as a top-level mode toggle with independent grade sets.

---

## External Research

### Q1: Standard given-count ranges across the industry

| Level | Range (consensus across sources) | Notes |
|-------|----------------------------------|-------|
| Easy | 36–45 | Most sources center on 38–42 |
| Medium | 30–36 | |
| Hard | 25–30 | |
| Expert | 22–26 | |
| Extreme/Master | 17–23 | 17 is mathematical minimum (proven 2012) |

Critical nuance from multiple sources: **given count alone does not determine difficulty**. A 17-clue puzzle can be solvable by naked+hidden singles only; a 30-clue puzzle may require Swordfish. This is the central tension between the two modes.

The external test data in `ExternalPuzzleGraderTest.kt` matches industry ranges closely:
- Easy: 38 givens (consistent across 4 samples)
- Medium: 36–38 givens
- Hard: 30–36 givens
- Expert: 25–30 givens
- Master: 25 givens
- Extreme: 23–28 givens

Sources: [sudokupuzzles.net](https://www.sudokupuzzles.net/blog/sudoku-difficulty-levels-explained-easy-to-extreme), [sudokugames.org](https://www.sudokugames.org/blog/sudoku-puzzle-difficulty-levels), [sudoku2.com](https://sudoku2.com/sudoku-tips/how-sudoku-difficulty-is-measured/)

### Q2: UI pattern for two modes

No mainstream sudoku app exposes technique-vs-given as an explicit user-facing mode toggle — they all use one grading system internally. The closest analogues are apps that offer 5+ difficulty tiers (Breezy → Easy → Medium → Hard → Expert → Evil), which implicitly blend both dimensions.

Two viable UI approaches:

**Option A — Mode first, then grade (two-step)**
```
[Technique-based] [Given-based]   <-- toggle/tabs at top
       Easy / Medium / Hard / Expert
```

**Option B — Integrated: relabel, single list**
Add a "Mode" setting in a settings drawer; the same 4–6 buttons appear but their labels change subtly (e.g., "Easy (38+ givens)" vs "Easy (singles only)").

Option A is simpler to reason about and implement. Option B reduces cognitive overhead for users who don't care about the distinction. Given this app is a desktop app with a simple HomeScreen (no settings screen exists yet), **Option A is recommended** — it fits the existing HomeScreen layout with minimal new components.

### Q3: UX trade-offs — technique-based vs given-based

| Dimension | Technique-based | Given-based |
|-----------|----------------|-------------|
| Correctness guarantee | Grade = actual solving complexity | Grade = information density only; may not reflect solve complexity |
| Generation speed | Slow: must generate + grade match (currently up to 1000 retries) | Fast: dig to target count and stop |
| User expectation match | Higher — "Hard" means you actually need hard techniques | Lower — a "Hard" puzzle may be solvable by easy techniques |
| Engine accuracy dependency | High — current engine under-grades | None — just counts givens |
| Consistency across puzzles | High within a grade (all use same technique set) | Variable — two 28-given puzzles may differ dramatically in technique difficulty |
| Accessibility | Richer educational value (matches hint system) | More intuitive for casual users |

**Key insight**: given-based difficulty aligns well with the hint system only incidentally — a puzzle graded "Hard" by given count may not actually require any technique the HintEngine knows about. Technique-based difficulty keeps hint availability meaningful (hints reflect the techniques the puzzle actually required).

### Q4: Generator approach for given-based mode

Two approaches:

**Approach A — Stop digging at target count**
Modify `digHoles()` to stop when `puzzle.count { it == 0 } == (81 - targetGivens)` rather than digging until no more can be removed. This is simpler and very fast — no retries needed.

**Approach B — Dig fully, accept if within range**
Dig until no more holes possible (maximum hole-digging), then accept only if final give count falls in target range. Reject otherwise and retry.

Approach B has a problem: the maximum-hole puzzle's give count depends on the specific filled grid, and for most grids it will land around 22–26 (near minimum). This means Easy (38+ givens) would almost never be accepted — extremely high retry rate.

**Approach A is correct for given-based mode.** The digging loop stops when the desired hole count is reached, regardless of whether more holes could be dug. Since uniqueness is still enforced (each removal checked with `Solver.countSolutions`), the result is always a valid, unique puzzle. The `Grader` is not called at all.

Implementation sketch for `Generator`:
```kotlin
suspend fun generateByGivenCount(targetGivens: Int): Board {
    // digHoles stops when puzzle.count { it != 0 } == targetGivens
    // No Grader.grade() call — just count check
}
```

### Q5: Missing grading techniques (why engine grades most puzzles as EASY)

The current grader progression: Naked Singles → Hidden Singles → Naked Pairs → Hidden Pairs → Pointing Pairs → X-Wing.

The engine fails to advance past EASY for most real puzzles because the technique ladder has large gaps. The technique hierarchy (from sudokuwiki.org and forum.enjoysudoku.com):

| Technique | Typical difficulty tier |
|-----------|------------------------|
| Naked Singles | Easy |
| Hidden Singles | Easy |
| Naked Pairs | Medium |
| Hidden Pairs | Medium |
| Pointing Pairs/Triples | Medium/Hard |
| Box-Line Reduction | Medium/Hard |
| **Naked Triples** | Medium/Hard — **missing from engine** |
| **Hidden Triples** | Hard — **missing from engine** |
| **Naked Quads** | Hard — **missing from engine** |
| X-Wing | Hard/Expert |
| **Swordfish** | Expert — **missing from engine** |
| **Y-Wing (XY-Wing)** | Expert — **missing from engine** |
| **Simple Colouring** | Expert — **missing from engine** |
| XYZ-Wing, W-Wing | Master |
| Forcing chains, ALS | Extreme |

**Root cause of EASY over-grading**: The external test shows Easy/Medium/Hard/Expert puzzles from the reference site all grade as EASY in the engine. This means even Medium-difficulty puzzles (which need pairs/pointing pairs) are being solved by naked+hidden singles alone in many cases — but more critically, Hard/Expert puzzles that do require advanced techniques fall through to EXPERT (not EASY), yet the test shows they grade EASY too. This suggests the real issue: the reference site's "Hard" puzzles (30 givens) are still solvable by the current technique set, meaning the current technique-based grade labels don't match the reference site's labels — the reference site uses a different (likely scoring-based) grading system, not a pure technique-gate system.

Adding Naked/Hidden Triples and Swordfish would improve grading accuracy significantly for real-world puzzles. Y-Wing is the next most important addition after Swordfish.

### Q6: Label naming conventions

| Mode | Recommended labels | Notes |
|------|--------------------|-------|
| Technique-based | Easy / Medium / Hard / Expert | Current labels; well-understood; keep as-is |
| Given-based | Beginner / Easy / Medium / Hard / Expert / Master | 6 tiers map better to the wider given-count range (38→23) |

Alternative for given-based: use same 4 labels (Easy/Medium/Hard/Expert) but with wider ranges to avoid confusion. Downside: users switching between modes see the same label mean different things.

**Recommendation**: Use distinct label sets per mode to make the distinction visceral. If only 4 levels are desired for given-based, use: Beginner / Intermediate / Advanced / Expert.

---

## Codebase Analysis

### Existing Patterns

- `Generator.generate(difficulty)` — engine entry point. All difficulty logic flows through `Grader.grade()`. A parallel `generateByGivenCount(targetGivens: Int)` function is the cleanest addition.
- `Difficulty` enum is used in `GameState`, `GameIntent.StartNewGame`, `HintEngine.findHint()`, and `HomeScreen`. A new `GivenDifficulty` enum (or a sealed class approach) would be needed to avoid polluting the existing `Difficulty` enum with given-based concepts.
- `digHoles()` is private in `Generator`. Its internal logic is a natural extension point.
- `HintEngine.findHint(board, difficulty)` uses `difficulty` only to gate the "NoHintForDifficulty" result. This will need updating if given-mode difficulty levels are added as a separate type.
- `GameState.difficulty: Difficulty` — if a second mode is added, either this field becomes a sealed class or a second field `givenDifficulty` is added.

### Dependencies

- `Solver.countSolutions()` is already used for uniqueness; no change needed for given-based generation.
- No persistence layer exists (no saved games, no preferences beyond locale). Mode selection can be in-memory or saved to `AppPreferences`.

### Constraints

- `Generator.generate()` currently enforces `Grader.grade(puzzle) == difficulty` — retry rate for MEDIUM/HARD/EXPERT is already high (up to 1000 attempts). This confirms Approach A for given-based mode (stop early, not reject-and-retry).
- `HintEngine` only supports techniques up through Pointing Pairs. If a given-based "Hard" puzzle happens to require X-Wing, the hint system has no X-Wing hint. This is an acceptable inconsistency for given-based mode — the hint system should signal "no hint available at this difficulty" rather than expose a wrong hint.
- The app has two locales (English, Russian). Any new labels need additions to `EnglishStrings`, `RussianStrings`, and the `Strings` interface.

### Related Specs

| Spec | Relationship | May Need Update |
|------|-------------|----------------|
| `sudoku-app` | Parent spec — original app build; established architecture patterns | No |
| `ui-redesign` | Modifies HomeScreen layout | Yes — if ui-redesign is implemented first, the HomeScreen layout this spec targets may change |
| `ui-localisation` | Completed: established i18n pattern (Strings interface + per-locale objects) | Yes — new difficulty labels must follow the same i18n pattern |
| `fix-hint-overflow` | Modifies HintBanner layout | No |
| `quit-button` | Bug fix only | No |

---

## Feasibility Assessment

| Aspect | Assessment | Notes |
|--------|------------|-------|
| Given-based generation | High | Straightforward: stop digging at count, no grader call |
| Given-based UI | High | Two-step HomeScreen with mode toggle; additive change |
| Technique-based accuracy fix | Medium | Adding Naked/Hidden Triples + Swordfish is well-understood but requires careful implementation and test coverage |
| State model changes | Medium | `GameState` and `GameIntent` need to carry mode info; sealed class or two-field approach |
| i18n completeness | Low risk | Established pattern; just add new string keys |
| Generation speed (given-based) | High | Eliminates retry problem — always succeeds on first attempt (uniqueness permitting) |

---

## Recommendations for Requirements

1. **Model difficulty as a sealed class.** Replace or wrap `enum class Difficulty` with a sealed hierarchy: `TechniqueDifficulty(EASY/MEDIUM/HARD/EXPERT)` and `GivenDifficulty(BEGINNER/EASY/MEDIUM/HARD/EXPERT/MASTER)`. This keeps the type system honest about which mode produced the puzzle. Alternatively, add a `DifficultyMode` enum and keep one `Difficulty` enum — simpler but less type-safe.

2. **Add `Generator.generateByGivenCount(targetGivens: Int): Board`.** Stop hole-digging when `count { it != 0 } == targetGivens`. No `Grader` call. Target ranges based on external data and industry norms:
   - Beginner: 38–42 givens
   - Easy: 33–37 givens
   - Medium: 28–32 givens
   - Hard: 24–27 givens
   - Expert: 20–23 givens
   - Master: 17–19 givens

3. **HomeScreen: two-step mode selection.** Add a `DifficultyMode` toggle (Technique / Given) above the grade buttons. When mode changes, show the appropriate grade list. This is a minimal additive change to HomeScreen; no new screen required.

4. **Decouple HintEngine from difficulty for given-mode.** When playing a given-based puzzle, hints should always try all known techniques (not gate on difficulty level). The current gating `if (difficulty == HARD || difficulty == EXPERT) NoHintForDifficulty` was designed for technique-based mode — given-based puzzles should use `NoHint` (technique not applicable) rather than `NoHintForDifficulty`.

5. **Fix technique-based grading accuracy (independent sub-task).** Add Naked Triples, Hidden Triples, and Swordfish to `Grader.grade()` and `HintEngine`. This directly fixes the "everything grades as EASY" problem reported by the user. Y-Wing should follow in a subsequent iteration. This is separable from the two-mode feature and can be done first.

6. **Use distinct label sets per mode.** Technique-based: Easy/Medium/Hard/Expert (keep current). Given-based: Beginner/Easy/Medium/Hard/Expert/Master. Add all new keys to the `Strings` interface and both locale objects.

---

## Open Questions

1. Should given-based mode show the given count to the user (e.g., "Medium — 30 givens")? This would make the mode more transparent and educational.
2. Should mode preference be persisted (alongside locale in `AppPreferences`)? Or reset to technique-based on each app launch?
3. For given-based generation, should the target be a fixed count (e.g., exactly 30) or a range (e.g., 28–32)? A fixed count is simpler but may occasionally fail if uniqueness prevents reaching it. A range is more robust.
4. Should the `Difficulty` enum gain new values (BEGINNER, MASTER) or should `GivenDifficulty` be a fully separate type? The answer affects how much of the existing codebase needs to change.
5. Is 6 given-based tiers the right number, or would 4 (matching technique-based) reduce confusion at the cost of coarser granularity?

---

## Quality Commands

| Type | Command | Source |
|------|---------|--------|
| Unit Test | `./gradlew :engine:test` | CI workflow (_build.yml) |
| Build | `./gradlew :app:packageDmg` (macOS) | CI workflow |
| Test + Build (local) | `./gradlew :engine:test :app:compileKotlin` | Derived from CI |
| Lint | Not found | No lint task configured |
| TypeCheck | Not found (compiled via Gradle) | Kotlin compilation is the type check |

**Local verification**: `./gradlew :engine:test`

---

## Verification Tooling

No automated E2E tooling detected. This is a Compose Desktop app — no browser automation applicable.

**Project Type**: Desktop GUI Application (Compose Multiplatform / JVM)
**Verification Strategy**: Build and run unit tests (`./gradlew :engine:test`); manual smoke-test of HomeScreen mode toggle and puzzle generation by running the app.

---

## Sources

- [sudokupuzzles.net — Difficulty Levels Explained](https://www.sudokupuzzles.net/blog/sudoku-difficulty-levels-explained-easy-to-extreme)
- [sudokugames.org — Complete Difficulty Guide](https://www.sudokugames.org/blog/sudoku-puzzle-difficulty-levels)
- [sudoku2.com — How Difficulty Is Measured](https://sudoku2.com/sudoku-tips/how-sudoku-difficulty-is-measured/)
- [sudokuwiki.org — Swordfish Strategy](https://www.sudokuwiki.org/sword_fish_strategy)
- [forum.enjoysudoku.com — Method Hierarchy](http://forum.enjoysudoku.com/method-hierachy-t3188.html)
- [premiumsudoku.com — Advanced Techniques](https://premiumsudoku.com/en/articles/advanced-sudoku-techniques)
- [finalsudoku.com — How Puzzles Are Generated](https://finalsudoku.com/sudoku-generator)
- [kevinhooke.com — Grading Algorithm](https://www.kevinhooke.com/2021/07/23/grading-the-difficulty-of-a-sudoku-puzzle/)
- `/Users/aifgi/src/sudoku/engine/src/main/kotlin/sudoku/engine/Grader.kt`
- `/Users/aifgi/src/sudoku/engine/src/main/kotlin/sudoku/engine/Generator.kt`
- `/Users/aifgi/src/sudoku/engine/src/main/kotlin/sudoku/engine/HintEngine.kt`
- `/Users/aifgi/src/sudoku/app/src/main/kotlin/sudoku/app/ui/HomeScreen.kt`
- `/Users/aifgi/src/sudoku/app/src/main/kotlin/sudoku/app/state/GameState.kt`
- `/Users/aifgi/src/sudoku/engine/src/test/kotlin/sudoku/engine/ExternalPuzzleGraderTest.kt`
- `/Users/aifgi/src/sudoku/.github/workflows/_build.yml`