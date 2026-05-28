---
generated: auto
---

# Requirements: Fix Hint Text Overflow

## Goal

Hint text in the Sudoku game is clipped or invisible when it wraps to multiple lines. Fix the layout so hint text always displays fully, in both EN and RU locales, without changing visual style.

## User Stories

### US-1: Full Hint Visibility
**As a** player receiving a hint
**I want to** read the full hint explanation
**So that** I can understand and apply the suggested technique

**Acceptance Criteria:**
- [ ] AC-1.1: `./gradlew assembleDebug` exits 0 (build gate — confirms no compile errors introduced by the fix); manual inspection note: launch app, trigger PointingPairCol hint, confirm full text visible
- [ ] AC-1.2: `grep -c "heightIn" app/src/main/kotlin/sudoku/app/ui/GameScreen.kt` returns 1 (confirms the min-height constraint is present)
- [ ] AC-1.3: Manual: PointingPairCol hint explanation text (~95 chars) renders fully in both EN and RU locales on device/emulator

### US-2: Adaptive Hint Slot Height
**As a** player on any device
**I want** the hint area to grow when text wraps
**So that** longer hints don't overflow or get cut off

**Acceptance Criteria:**
- [ ] AC-2.1: Manual: hint slot expands vertically to fit multi-line hint text (trigger PointingPairCol hint and observe no clipping)
- [ ] AC-2.2: `grep -c "heightIn" app/src/main/kotlin/sudoku/app/ui/GameScreen.kt` returns 1 (confirms `heightIn(min = 56.dp)` replaced the fixed `height(56.dp)`)
- [ ] AC-2.3: Manual: NakedSingle hint (single-line) still renders at 56dp height with no layout shift

### US-3: Preserved Visual Style
**As a** designer
**I want** the fix to change only height constraint and text layout weight
**So that** visual appearance, spacing, and colours remain unchanged

**Acceptance Criteria:**
- [ ] AC-3.1: `grep -c "heightIn" app/src/main/kotlin/sudoku/app/ui/GameScreen.kt` returns 1 AND `grep -c "weight(1f)" app/src/main/kotlin/sudoku/app/ui/components/HintBanner.kt` returns 1 (confirms ONLY the two expected changes are present in the targeted files)
- [ ] AC-3.2: `git diff --name-only` of the feature branch shows exactly 2 files changed: `HintBanner.kt` and `GameScreen.kt` (no other files touched)
- [ ] AC-3.3: Manual: hint slot background, padding, corner shape, typography, and colour are visually unchanged from pre-fix baseline

## Functional Requirements

| ID | Requirement | Priority | Acceptance Criteria |
|----|-------------|----------|---------------------|
| FR-1 | `HintBanner.kt` explanation `Text` must receive `Modifier.weight(1f)` so it fills available row width | High | `grep -c "weight(1f)" app/src/main/kotlin/sudoku/app/ui/components/HintBanner.kt` = 1 |
| FR-2 | `GameScreen.kt` hint `Box` constraint must change from `height(56.dp)` to `heightIn(min = 56.dp)` | High | `grep -c "heightIn" app/src/main/kotlin/sudoku/app/ui/GameScreen.kt` = 1 |
| FR-3 | Both changes must not alter padding, colour, typography, or shape of the hint slot | Medium | `git diff --name-only` shows exactly 2 files; manual visual check confirms no style regression |

## Non-Functional Requirements

| ID | Requirement | Metric | Target |
|----|-------------|--------|--------|
| NFR-1 | Locale coverage | Locales tested | EN and RU |
| NFR-2 | Minimum height preserved | dp measurement at runtime | 56dp for single-line hints |
| NFR-3 | Change surface area | Files modified | Max 2 files (`HintBanner.kt`, `GameScreen.kt`) |

## Glossary

- **Hint slot**: The `Box` in `GameScreen.kt` that contains the `HintBanner` composable
- **Hint banner**: The `HintBanner` composable displaying technique name + explanation text in a `Row`
- **weight(1f)**: Compose `Modifier` that allocates remaining width in a `Row` to the modified child
- **heightIn(min)**: Compose `Modifier` that sets a minimum height while allowing expansion

## Out of Scope

- Adding automated UI or layout tests (no test infrastructure exists for visual layout)
- Changing typography, font size, or line spacing
- Modifying hint content or string resources
- Fixing any other layout issues outside the hint slot
- Supporting additional locales beyond EN and RU

## Dependencies

- `HintBanner.kt` and `GameScreen.kt` must be understood before change (research already done)
- No library upgrades required; fix uses existing Compose modifiers

## Success Criteria

- On a physical device or emulator, all hint types (including PointingPairCol) display their full explanation text in both EN and RU
- Single-line hints (e.g. NakedSingle) still render at exactly 56dp height
- No visual regression in hint slot appearance

## Unresolved Questions

None. Root causes are identified and the fix is unambiguous.

## Next Steps

1. Implement FR-1: add `Modifier.weight(1f)` to explanation `Text` in `HintBanner.kt:52`
2. Implement FR-2: replace `height(56.dp)` with `heightIn(min = 56.dp)` in `GameScreen.kt:173`
3. Manually verify on device/emulator with PointingPairCol hint in EN and RU
