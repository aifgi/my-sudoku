---
spec: difficulty-modes
phase: requirements
created: 2026-05-28
---

# Requirements: Difficulty Modes

## Goal

Add two independent puzzle difficulty modes — technique-based (grade = solving techniques required) and given-based (grade = number of pre-filled cells) — and fix technique-based grading accuracy by adding missing intermediate and advanced techniques.

---

## User Stories

### US-1: Choose difficulty mode before starting a game

**As a** player
**I want to** choose between technique-based and given-based difficulty modes on the home screen
**So that** I can pick a puzzle difficulty style that matches how I like to be challenged

**Acceptance Criteria:**
- [ ] AC-1.1: Home screen displays a mode toggle (Technique / Given) above the grade buttons
- [ ] AC-1.2: Selecting a mode updates the grade buttons shown below to reflect that mode's labels and count
- [ ] AC-1.3: Mode selection persists across app launches (stored in AppPreferences)
- [ ] AC-1.4: Default mode on first launch is Technique-based
- [ ] AC-1.5: Mode toggle is labelled in both English and Russian

---

### US-2: Start a given-based puzzle at a chosen grade

**As a** player
**I want to** start a puzzle with a specific number of pre-filled cells (Easy, Medium, Hard, Expert)
**So that** I can control how much information I start with, regardless of solving technique complexity

**Acceptance Criteria:**
- [ ] AC-2.1: Given-based mode offers exactly four grades: Easy, Medium, Hard, Expert
- [ ] AC-2.2: Each grade maps to a target given-count range:

  | Grade  | Given Count |
  |--------|-------------|
  | Easy   | 36–45       |
  | Medium | 29–35       |
  | Hard   | 24–28       |
  | Expert | 17–23       |

- [ ] AC-2.3: Generated puzzle has a given count within the target range for the selected grade
- [ ] AC-2.4: Generated puzzle has a unique solution (verified by Solver)
- [ ] AC-2.5: After hole-digging reaches the target given count, `Grader` is called once to verify the puzzle does not exceed the grade's technique ceiling (not to match a specific grade). If the ceiling is exceeded, the generator digs one more hole or retries.
- [ ] AC-2.6: Generation completes without unbounded retry loops — hole-digging stops when given count reaches the target; ceiling check may cause at most one additional hole or a bounded retry, not an open-ended loop
- [ ] AC-2.8: Technique ceilings per given-based grade are enforced:

  | Grade  | Technique ceiling (puzzle must be solvable using at most…) |
  |--------|-------------------------------------------------------------|
  | Easy   | Naked Singles + Hidden Singles                              |
  | Medium | Naked Singles + Hidden Singles                              |
  | Hard   | Naked Pairs + Hidden Pairs + Pointing Pairs (and simpler)  |
  | Expert | Naked Pairs + Hidden Pairs + Pointing Pairs (and simpler)  |
- [ ] AC-2.7: Grade labels for given-based mode are distinct from technique-based labels in the codebase (separate type or clearly separated enumeration values) to prevent misuse

---

### US-3: Receive hints during a given-based puzzle

**As a** player
**I want to** request a hint during a given-based puzzle and have the game try all known techniques
**So that** I always get the best available hint regardless of what grade label the puzzle carries

**Acceptance Criteria:**
- [ ] AC-3.1: When playing a given-based puzzle, HintEngine gates techniques by the grade's technique ceiling — Easy/Medium puzzles only receive singles hints; Hard/Expert puzzles receive pairs-level hints and simpler
- [ ] AC-3.2: No given-based puzzle ever receives a hint for techniques above pairs (X-Wing, Swordfish, Naked/Hidden Triples are excluded from all given-based hints)
- [ ] AC-3.3: If no technique at or below the ceiling yields a result, hint banner shows "no hint available" (not "no hint for this difficulty")
- [ ] AC-3.4: The existing "no hint for difficulty" path (`NoHintForDifficulty`) is not triggered for given-based puzzles
- [ ] AC-3.5: Per-grade hint technique sets:

  | Grade  | Hint techniques available |
  |--------|---------------------------|
  | Easy   | Naked Singles, Hidden Singles |
  | Medium | Naked Singles, Hidden Singles |
  | Hard   | Naked Singles, Hidden Singles, Naked Pairs, Hidden Pairs, Pointing Pairs |
  | Expert | Naked Singles, Hidden Singles, Naked Pairs, Hidden Pairs, Pointing Pairs |

- [ ] AC-3.6: Hint behaviour for technique-based puzzles is unchanged

---

### US-4: Get accurately graded technique-based puzzles

**As a** player selecting technique-based difficulty
**I want to** receive a puzzle that genuinely requires the techniques associated with my chosen grade
**So that** a "Hard" puzzle actually challenges me with harder logic, not just naked/hidden singles

**Acceptance Criteria:**
- [ ] AC-4.1: `Grader` uses the following technique ladder in order: Naked Singles → Hidden Singles → Naked Pairs → Hidden Pairs → Pointing Pairs → Naked Triples → Hidden Triples → X-Wing → Swordfish
- [ ] AC-4.2: A puzzle solvable by Naked/Hidden Singles only grades as EASY
- [ ] AC-4.3: A puzzle requiring Naked/Hidden Pairs or Pointing Pairs (but not triples/fish) grades as MEDIUM
- [ ] AC-4.4: A puzzle requiring Naked/Hidden Triples grades as HARD
- [ ] AC-4.5: A puzzle requiring X-Wing or Swordfish grades as EXPERT
- [ ] AC-4.6: `HintEngine` provides hints for Naked Triples, Hidden Triples, and Swordfish in addition to existing techniques
- [ ] AC-4.7: Existing `ExternalPuzzleGraderTest` puzzles grade at MEDIUM or higher after adding the new techniques (no longer all EASY)

---

### US-5: App strings for new modes and grades are localised

**As a** Russian-speaking player
**I want to** see all new difficulty mode labels and grade names in Russian
**So that** the app is fully usable in my language

**Acceptance Criteria:**
- [ ] AC-5.1: All new string keys added to the `Strings` interface
- [ ] AC-5.2: All new string keys have values in `EnglishStrings`
- [ ] AC-5.3: All new string keys have values in `RussianStrings`
- [ ] AC-5.4: No hardcoded English strings in UI composables for new features

---

## Functional Requirements

| ID   | Requirement | Priority | Acceptance Criteria |
|------|-------------|----------|---------------------|
| FR-1 | Add `Generator.generateByGivenCount(targetGivens: Int, ceiling: TechniqueCeiling): Board` that stops hole-digging when given count reaches target, then calls `Grader` once to verify the puzzle does not exceed the ceiling; retries with one additional hole if ceiling is breached | High | AC-2.3, AC-2.5, AC-2.6, AC-2.8 |
| FR-2 | Add mode toggle to HomeScreen (Technique / Given); toggle updates grade button set | High | AC-1.1, AC-1.2 |
| FR-3 | Persist selected mode to `AppPreferences`; restore on launch | Medium | AC-1.3, AC-1.4 |
| FR-4 | Add `Naked Triples` technique to `Grader` and `HintEngine` | High | AC-4.1, AC-4.4, AC-4.6 |
| FR-5 | Add `Hidden Triples` technique to `Grader` and `HintEngine` | High | AC-4.1, AC-4.4, AC-4.6 |
| FR-6 | Add `Swordfish` technique to `Grader` and `HintEngine` | High | AC-4.1, AC-4.5, AC-4.6 |
| FR-7 | Gate `HintEngine` technique set for given-based puzzles by the grade's technique ceiling (singles only for Easy/Medium; pairs and simpler for Hard/Expert); advanced techniques (Triples, X-Wing, Swordfish) never suggested for given-based puzzles | High | AC-3.1, AC-3.2, AC-3.3, AC-3.5 |
| FR-8 | Represent given-based grades as a type distinct from technique-based `Difficulty` (separate enum or sealed class) | High | AC-2.7 |
| FR-9 | Add all new UI strings to `Strings` interface and both locale implementations | Medium | AC-5.1–AC-5.4 |
| FR-10 | `GameState` carries sufficient mode information to determine hint gating behaviour at game time | High | AC-3.1, AC-3.3 |

---

## Non-Functional Requirements

| ID    | Requirement | Metric | Target |
|-------|-------------|--------|--------|
| NFR-1 | Given-based generation speed | Time from button tap to puzzle display | Under 3 seconds on a 2-year-old Mac (no retry loops) |
| NFR-2 | Technique-based generation speed | Unchanged from current | No regression |
| NFR-3 | Grader correctness | ExternalPuzzleGraderTest pass rate | All test puzzles grade at MEDIUM or higher after technique additions |
| NFR-4 | Hint correctness | No wrong candidate eliminations suggested | All new technique hints must be logically sound; covered by unit tests |
| NFR-5 | Localisation completeness | Missing string keys at runtime | Zero — compile-time interface ensures completeness |

---

## Glossary

- **Technique-based mode**: Difficulty grade determined by which solving techniques are required to solve the puzzle. Uses `Grader.grade()`.
- **Given-based mode**: Difficulty grade determined solely by the number of pre-filled cells (givens). `Grader` is called only to verify the technique ceiling is not exceeded — not to match a specific grade.
- **Given**: A cell whose value is pre-filled at puzzle start and cannot be changed by the player.
- **Given count**: Number of given cells in a puzzle (out of 81 total cells).
- **Hole-digging**: The process of removing givens from a fully solved grid one at a time, checking uniqueness after each removal.
- **Uniqueness**: A puzzle has a unique solution if exactly one completed grid satisfies all constraints. Verified via `Solver.countSolutions()`.
- **Naked Triple**: A solving technique where three cells in a unit collectively contain only three candidate values, allowing those values to be eliminated from all other cells in the unit.
- **Hidden Triple**: A solving technique where three candidate values appear only in three cells of a unit, allowing other candidates to be removed from those three cells.
- **Swordfish**: A fish technique operating on three rows/columns; an extension of X-Wing to three base sets.
- **Technique ceiling**: The maximum technique complexity allowed for a given-based grade. Easy and Medium cap at singles; Hard and Expert cap at pairs. No given-based puzzle may require or hint techniques above its ceiling.
- **Technique gating**: The current HintEngine behaviour of refusing to suggest advanced techniques for lower-grade puzzles.

---

## Out of Scope

- Y-Wing, XYZ-Wing, W-Wing, forcing chains, or ALS techniques (may follow in a later spec)
- Showing given count to the player in the game UI (e.g., "Medium — 30 givens")
- More than 4 grade levels per mode (6-tier given scale is not required)
- Expert/Extreme given-based grades below 17 (17 is the proven mathematical minimum)
- Changing how existing technique-based puzzles are stored or migrated (no persistence of puzzles exists)
- Any change to the `Solver` algorithm
- Box-Line Reduction technique (not required to fix the grading gap at the targeted accuracy level)

---

## Dependencies

- `ui-redesign` spec: if HomeScreen layout changes under that spec, the mode toggle placement may need adjustment. Coordinate before finalising HomeScreen composable changes.
- `ui-localisation` spec (completed): i18n pattern (Strings interface + per-locale objects) is established; must be followed for all new string keys.
- Kotlin/Compose Multiplatform: Naked Triples, Hidden Triples, and Swordfish implementations must not use any JVM-only APIs not already available in the engine module.

---

## Success Criteria

- A player selecting given-based Hard receives a puzzle with 24–28 givens that requires at most pairs techniques, every time
- A player selecting given-based Easy receives a puzzle with 36–45 givens that is solvable by singles only, every time
- A player selecting technique-based Hard receives a puzzle that is not solvable by naked/hidden singles alone
- `ExternalPuzzleGraderTest` no longer grades all puzzles as EASY after technique additions
- Hints on a given-based Easy/Medium puzzle never suggest pairs or higher techniques
- Hints on a given-based Hard/Expert puzzle never suggest triples, X-Wing, or Swordfish
- The app builds and all engine unit tests pass (`./gradlew :engine:test`)
- All new UI text appears correctly in both English and Russian

---

## Unresolved Questions

None — all design decisions resolved via user interview:
- Given-based labels: same 4 as technique-based (Easy / Medium / Hard / Expert), independent type
- Given-based hints: gated by grade's technique ceiling (singles only for Easy/Medium; pairs and simpler for Hard/Expert)
- Grader accuracy fixes: included in this spec (Naked Triples, Hidden Triples, Swordfish)
- Mode persistence: yes, stored in AppPreferences
- Generation approach: stop-at-count, then ceiling-check via Grader; bounded retry if ceiling breached

---

## Next Steps

1. Approve requirements (or request changes)
2. Design phase: define sealed class/enum approach for `GivenDifficulty`, `GameState` changes, `HintEngine` interface changes, and technique implementation contracts
3. Tasks phase: break design into atomic implementation tasks ordered by dependency