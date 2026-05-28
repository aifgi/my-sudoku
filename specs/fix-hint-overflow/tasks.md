---
generated: auto
---

# Tasks: Fix Hint Text Overflow

## Phase 1: POC — Confirm Before-State

Verify both target files exist and the exact lines to change are present before touching anything.

- [x] 1.1 Verify HintBanner.kt before-state
  - **Do**:
    1. Confirm file exists: `ls app/src/main/kotlin/sudoku/app/ui/components/HintBanner.kt`
    2. Grep for the un-modified explanation Text (no weight modifier): `grep -n "Text(text = explanationText)" app/src/main/kotlin/sudoku/app/ui/components/HintBanner.kt`
  - **Files**: none (read-only verification)
  - **Done when**: File exists AND grep returns at least one line containing `Text(text = explanationText)` without `weight`
  - **Verify**: `grep -c "Text(text = explanationText)" app/src/main/kotlin/sudoku/app/ui/components/HintBanner.kt` outputs `1`
  - **Commit**: None
  - _Requirements: FR-1 / Design: Change 1 — HintBanner.kt_

- [x] 1.2 Verify GameScreen.kt before-state
  - **Do**:
    1. Confirm file exists: `ls app/src/main/kotlin/sudoku/app/ui/GameScreen.kt`
    2. Grep for the fixed-height constraint: `grep -n "\.height(56\.dp)" app/src/main/kotlin/sudoku/app/ui/GameScreen.kt`
  - **Files**: none (read-only verification)
  - **Done when**: File exists AND grep returns the line containing `.height(56.dp)` (not yet `heightIn`)
  - **Verify**: `grep -c "\.height(56\.dp)" app/src/main/kotlin/sudoku/app/ui/GameScreen.kt` outputs `1`
  - **Commit**: None
  - _Requirements: FR-2 / Design: Change 2 — GameScreen.kt_

## Phase 2: Implementation

Apply the two surgical edits.

- [x] 2.1 Add `weight(1f)` modifier to explanation Text in HintBanner.kt
  - **Do**: At line 52, change `Text(text = explanationText)` to `Text(text = explanationText, modifier = Modifier.weight(1f))`
  - **Files**: `app/src/main/kotlin/sudoku/app/ui/components/HintBanner.kt`
  - **Done when**: Line 52 contains `modifier = Modifier.weight(1f)` on the explanation Text; `Modifier` is already imported (no new import needed)
  - **Verify**: `grep -c "weight(1f)" app/src/main/kotlin/sudoku/app/ui/components/HintBanner.kt` outputs `1`
  - **Commit**: `fix(hint): constrain explanation text width with weight(1f) to prevent overflow`
  - _Requirements: FR-1 / Design: Change 1 — HintBanner.kt_

- [x] 2.2 Change `height(56.dp)` to `heightIn(min = 56.dp)` in GameScreen.kt
  - **Do**: At line 173, replace `.height(56.dp)` with `.heightIn(min = 56.dp)` in the hint banner Box modifier
  - **Files**: `app/src/main/kotlin/sudoku/app/ui/GameScreen.kt`
  - **Done when**: Line 173 uses `heightIn(min = 56.dp)` instead of `height(56.dp)`; note — `heightIn` is in `androidx.compose.foundation.layout` (same package as `height`), so no new import line is required
  - **Verify**: `grep -c "heightIn" app/src/main/kotlin/sudoku/app/ui/GameScreen.kt` outputs `1`
  - **Commit**: `fix(hint): allow hint banner to grow beyond 56dp minimum so text is not clipped`
  - _Requirements: FR-2 / Design: Change 2 — GameScreen.kt_

## Phase 3: Testing

Grep-based verification that both fixes are present in source.

- [x] 3.1 Verify both fixes are present in source
  - **Do**:
    1. Check weight modifier: `grep -c "weight(1f)" app/src/main/kotlin/sudoku/app/ui/components/HintBanner.kt`
    2. Check heightIn modifier: `grep -c "heightIn" app/src/main/kotlin/sudoku/app/ui/GameScreen.kt`
    3. Confirm old fixed-height is gone: `grep -c "\.height(56\.dp)" app/src/main/kotlin/sudoku/app/ui/GameScreen.kt`
  - **Files**: none (verification only)
  - **Done when**: Commands 1 and 2 both output `1`; command 3 outputs `0`
  - **Verify**: `grep -c "weight(1f)" app/src/main/kotlin/sudoku/app/ui/components/HintBanner.kt && grep -c "heightIn" app/src/main/kotlin/sudoku/app/ui/GameScreen.kt` — both lines output `1`
  - **Commit**: None
  - _Requirements: FR-1, FR-2, FR-3 / Design: Verification Steps_

## Phase 4: Quality

Build gate + diff surface-area check.

- [x] 4.1 Build gate: confirm no compile errors
  - **Do**: Run `./gradlew assembleDebug` from the repo root
  - **Files**: none (build only)
  - **Done when**: Gradle exits 0 with no compile errors
  - **Verify**: `./gradlew assembleDebug` exits 0
  - **Commit**: `chore(hint): pass assembleDebug build gate`
  - _Requirements: FR-1, FR-2 / Design: Verification Steps_

- [x] 4.2 Confirm change surface area is exactly 2 files
  - **Do**: Run `git diff --name-only` and confirm the output lists exactly `HintBanner.kt` and `GameScreen.kt` and nothing else
  - **Files**: none (verification only)
  - **Done when**: `git diff --name-only` output matches exactly 2 paths: `app/src/main/kotlin/sudoku/app/ui/components/HintBanner.kt` and `app/src/main/kotlin/sudoku/app/ui/GameScreen.kt`
  - **Verify**: `git diff --name-only | wc -l` outputs `2`
  - **Commit**: None
  - _Requirements: FR-3 / Design: Verification Steps_
