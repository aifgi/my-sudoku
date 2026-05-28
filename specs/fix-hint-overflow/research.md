---
spec: fix-hint-overflow
phase: research
created: 2026-05-28
generated: auto
---

# Research: fix-hint-overflow

## Executive Summary

The hint banner clips on two independent axes: the explanation `Text` has no `weight(1f)` modifier so it fights the technique-name `Text` for available width instead of wrapping, and the containing `Box` in `GameScreen.kt` has a hard `height(56.dp)` that truncates any text that wraps to a second line. Fixing both issues eliminates the overflow entirely with minimal code change.

---

## Root Cause Analysis

### Cause 1 — No `weight(1f)` on explanation Text (HintBanner.kt:52)

```
Text(text = techniqueName, fontWeight = FontWeight.Bold)   // unbounded, takes natural width
Spacer(modifier = Modifier.width(8.dp))
Text(text = explanationText)                               // also unbounded — FIGHTS for space
```

In a `Row`, if neither child has a `weight` modifier both children measure at their intrinsic (unconstrained) width. The `Row` then tries to fit both into the available width. The explanation text gets pushed to whatever space remains and, if it overflows the row's constraint, Compose clips it. Giving the explanation `Modifier.weight(1f)` tells it to fill all remaining space after the technique name and spacer are placed, enabling normal text wrapping within that remaining width.

### Cause 2 — Hard `height(56.dp)` Box in GameScreen.kt:173

```kotlin
Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
    if (state.hintResult != null) {
        HintBanner(hintResult = state.hintResult)
    }
}
```

`height(56.dp)` sets an **exact** height constraint; content taller than 56 dp is clipped. On a narrow right panel (~40% screen width) the explanation text may need 2–3 lines. The fix is `heightIn(min = 56.dp)`, which reserves a stable baseline height but expands when content is taller.

---

## Longest Hint Strings Found

### English

| Template | Max rendered example | Approx chars |
|---|---|---|
| `hintExplainPointingPairRow` | "Pointing Pair: digit 9 in box 9 is confined to row 9, eliminating it from other row cells" | ~91 |
| `hintExplainPointingPairCol` | "Pointing Pair: digit 9 in box 9 is confined to column 9, eliminating it from other column cells" | ~95 |
| `hintExplainHiddenPair` | "Hidden Pair at R9C9 and R9C9: digits 9 and 9 are confined to these cells" | ~73 |
| `hintExplainNakedPair` | "Naked Pair at R9C9 and R9C9: digits 9 and 9 are confined here" | ~61 |
| `hintExplainHiddenSingle` | "Hidden Single at R9C9: digit 9 can only go here in this unit" | ~60 |

Longest template: **PointingPairCol** at ~95 chars. On a panel ~160 dp wide, default 14 sp text wraps at ~30 chars/line, meaning 3 lines ≈ ~56–84 dp — right at or over the current fixed height.

### Russian (longer due to Cyrillic word length)

| Template | Max rendered example | Approx chars |
|---|---|---|
| `hintExplainPointingPairRow` | "Указывающая пара: цифра 9 в блоке 9 ограничена строкой 9, исключая её из других клеток строки" | ~93 |
| `hintExplainPointingPairCol` | "Указывающая пара: цифра 9 в блоке 9 ограничена столбцом 9, исключая её из других клеток столбца" | ~96 |
| `hintExplainHiddenPair` | "Скрытая пара в R9C9 и R9C9: цифры 9 и 9 заперты в этих клетках" | ~64 |

Russian Cyrillic glyphs are wider per character than Latin, compounding the overflow risk.

---

## Screenshot Evidence

`/workspace/Screenshot 2026-05-27 at 22.55.01.png` shows the game at 5/3 mistakes with no hint banner rendered in the slot — consistent with the hint being invisible or clipped. The hint area between action buttons and number pad appears empty even though a hint result would normally be visible.

---

## No Existing Tests

No UI/screenshot tests or Compose instrumentation tests exist for `HintBanner`. The only tests are JVM unit tests: `GameViewModelTest`, `StringsCompletenessTest`, `LocaleResolverTest`, `AppPreferencesTest`.

---

## `heightIn` vs `height` Precedent

No `heightIn` usage exists anywhere in `/workspace/app/src/main/kotlin`. The codebase uses `height(56.dp)` on the hint box and `height(52.dp)` on the New Game button (both fixed). `Spacer` uses `height(N.dp)` for gaps. There is no existing `heightIn` precedent, but it is the standard Compose pattern for "minimum height, can grow".

---

## Fix Options

### Option A — Minimal: `weight(1f)` + `heightIn` (Recommended)

**HintBanner.kt**: add `Modifier.weight(1f)` to the explanation `Text`.  
**GameScreen.kt**: change `height(56.dp)` to `heightIn(min = 56.dp)`.

Trade-offs:
- Pro: Minimal diff (2 lines changed).
- Pro: Preserves stable layout slot when hint is absent (min height prevents jumpiness).
- Pro: Standard Compose pattern — no custom layout code.
- Con: Layout shifts height when hint wraps to 2 lines vs 1 line.

### Option B — `wrapContentHeight` only (incomplete fix)

Remove the fixed height and let the box wrap. Fixes clipping but causes layout shift when hint appears/disappears (controls below jump up/down).

Trade-offs:
- Simpler on the container side.
- Does NOT fix the width fighting between texts; explanation still gets squeezed without `weight(1f)`.
- Layout jump is user-visible and feels buggy.

### Option C — Scrollable / collapsible hint area

Wrap hint in a `LazyColumn` or `AnimatedContent`. Overkill for a single-line-to-multiline expansion. Not warranted here.

### Option D — Shorten strings

Truncate explanation text to fit 56 dp. Loses information, poor UX.

---

## Recommendation

**Option A**. Two-line change, no new patterns, no UX regression. Specifically:

1. In `HintBanner.kt` line 52: add `.weight(1f)` to the explanation `Text` modifier.
2. In `GameScreen.kt` line 173: change `.height(56.dp)` to `.heightIn(min = 56.dp)`.

The `Modifier.weight(1f)` fix is the more critical of the two; without it the explanation text still gets crushed even if the container can grow. Both changes together fully resolve the bug for all hint types in both EN and RU locales.

---

## Quality Commands

| Type | Command | Source |
|------|---------|--------|
| Unit Test | `./gradlew test` | build.gradle |
| Build | `./gradlew assembleDebug` | build.gradle |
| Lint | Not found | — |
| TypeCheck | N/A (Kotlin, checked at compile) | — |

Note: Java toolchain issue currently prevents `./gradlew test` from running locally (unrelated to this bug). Build system is Gradle with Kotlin/Compose.

---

## Verification Tooling

No automated E2E tooling detected. No Playwright, Cypress, or Espresso test infrastructure found.

**Project Type**: Android/Compose app  
**Verification Strategy**: Build with `./gradlew assembleDebug` and visually verify on device/emulator. No automated layout regression tests exist.

---

## Sources

- `/workspace/app/src/main/kotlin/sudoku/app/ui/components/HintBanner.kt`
- `/workspace/app/src/main/kotlin/sudoku/app/ui/GameScreen.kt`
- `/workspace/app/src/main/kotlin/sudoku/app/ui/i18n/EnglishStrings.kt`
- `/workspace/app/src/main/kotlin/sudoku/app/ui/i18n/RussianStrings.kt`
- `/workspace/Screenshot 2026-05-27 at 22.55.01.png`
- Compose docs: `Modifier.weight` in `RowScope` distributes remaining space after unweighted children are measured
- Compose docs: `heightIn(min)` sets a minimum constraint while allowing growth
