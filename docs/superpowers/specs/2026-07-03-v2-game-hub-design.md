# Glass Puzzle Hub v2 Game Hub Design

## Goal

Release Glass Puzzle Hub `v2.0.0` as a performance and playability upgrade: prefer high refresh displays, make Shikaku touch controls feel natural, add a Modes layer for puzzle variants, and document the next three single-player games for the hub.

## Research Basis

- Android high-refresh support should request the preferred frame/display rate but still work if the platform declines the request due to device policy, battery saver, or competing surfaces.
- Shikaku rules remain Nikoli classic: divide the grid into rectangles where each rectangle has one number and that number is the rectangle area.
- Shikaku can support legitimate grid variants through board shape and size. A blocked-cell variant is research-backed but must be clearly treated as an advanced variant because it changes the clue-area rule.
- Sudoku variants should be represented honestly: Classic is playable now; Mini, Diagonal, Irregular/Jigsaw, and Killer are staged as planned modes until their validators/generators exist.
- Recommended future games are Slitherlink, Nurikabe, and Kakuro because they are single-player, grid-based, and have established rules from Nikoli.

## Scope

### In v2.0.0

- Request the highest available display refresh mode at launch.
- Add a small pure selector for choosing the best refresh-rate candidate.
- Fix Shikaku interactions:
  - Tap a value-1 clue to place its single-cell rectangle.
  - Long-press a placed rectangle to remove it.
  - Drag a new rectangle over existing rectangles to replace overlapping placements instead of blocking the attempt.
- Add Shikaku modes:
  - Classic
  - Mini
  - Wide
  - Tall
  - Large
  - Shadow Blocks
- Add Sudoku modes as visible roadmap entries:
  - Classic playable
  - Mini planned
  - Diagonal planned
  - Irregular planned
  - Killer planned
- Add three next-game recommendation cards:
  - Slitherlink
  - Nurikabe
  - Kakuro
- Bump Android version to `2.0.0`.
- Add v2 release notes and research notes.

### Out Of Scope For v2.0.0

- Full playable engines for Slitherlink, Nurikabe, or Kakuro.
- Full playable Sudoku variant validators/generators beyond Classic.
- Play Store production signing.
- Cloud saves or accounts.

## Architecture

- `core-model` gets a pure high-refresh selector so the selection rule can be tested without Android framework dependencies.
- `game-shikaku` owns Shikaku modes, rectangular board generation, blocked-cell clue-area support, and input helper functions.
- `app` maps Android display modes to the pure selector, applies the preferred display mode to the window, and exposes mode selection in the existing Compose screens.
- `MainActivity.kt` remains the UI shell for this pass, with localized additions rather than a broad refactor.

## Testing

- Add `HighRefreshSelectorTest` in `core-model`.
- Add Shikaku tests for:
  - rectangular mode dimensions,
  - value-1 tap placement,
  - long-press removal helper,
  - overlap replacement helper,
  - blocked-cell effective area validation.
- Run full `test lint assembleDebug`.
- Install on emulator and verify:
  - app launches,
  - Games page still renders,
  - mode section appears,
  - Shikaku tap/drag/long-press paths work,
  - completion popup still works.
