# Glass Puzzle Hub v2.1.0

Session, timer, and expanded-games release.

## Added

- Smart session timer with foreground-only elapsed time.
- Continue sessions backed by Room `game_sessions`, including restored puzzle state and elapsed time after app close/force stop.
- Playable Slitherlink compact engine and Canvas board.
- Playable Nurikabe compact engine and Canvas board.
- Playable Kakuro compact engine, Canvas board, and digit keypad.
- `game-variety` module with unit-tested Slitherlink, Nurikabe, and Kakuro validators.
- v2.1 design spec and implementation plan docs.

## Changed

- Mode chips now make the whole card area clickable, not just the text area.
- Floating bottom dock now wraps its four icons instead of stretching full width.
- Pause action now leaves the play screen, saving and pausing the current session.
- Daily screen can start all five games.
- App version is now `2.1.0` / versionCode `4`.

## Fixed

- Shikaku/Sudoku active state no longer disappears when the process is closed after a saved move.
- Timers no longer keep adding wall-clock time while the app is backgrounded.

## Known Limits

- Slitherlink, Nurikabe, and Kakuro use compact first-pass generators; deeper difficulty grading and advanced hints are still future work.
- Full archive/history browsing is still basic.
- Release APK is debug-signed unless a production signing config is added.

# Glass Puzzle Hub v2.0.0

Major performance and playability release.

## Added

- High-refresh display preference: the app now requests the highest available Android display mode, including 90Hz/120Hz devices when supported by the OS.
- Tested high-refresh mode selector in `core-model`.
- Shikaku modes:
  - Classic
  - Mini
  - Wide
  - Tall
  - Large
  - Shadow Blocks
- Shadow Blocks Shikaku variant where shaded cells are covered by rectangles but do not count toward clue area.
- Modes section on game detail screens.
- Sudoku roadmap modes: Mini, Diagonal, Irregular, and Killer.
- Next-game recommendation cards for Slitherlink, Nurikabe, and Kakuro.
- v2 research notes in `research_v2_puzzle_modes/`.
- v2 spec and implementation plan in `docs/superpowers/`.

## Changed

- Shikaku generator now supports rectangular boards and verifies unique solutions for generated mode boards.
- Shikaku board rendering and hit testing now support non-square board dimensions.
- Shikaku drag placement can replace overlapping rectangles, making direction changes practical.
- App version is now `2.0.0` / versionCode `3`.

## Fixed

- Tapping a value-1 Shikaku clue now places its single-cell rectangle.
- Long-pressing a placed Shikaku rectangle removes it.
- Drawing over an existing Shikaku rectangle no longer blocks valid replacement attempts.

## Known Limits

- Android can reject high-refresh requests due to battery saver, display policy, or device support.
- Sudoku variants beyond Classic are visible roadmap modes, not playable engines yet.
- Slitherlink, Nurikabe, and Kakuro are planned future games, not playable engines yet.
- Release APK is debug-signed unless a production signing config is added.

# Glass Puzzle Hub v0.1.1

UX polish and debugging release.

## Added

- Games-first launch flow with Shikaku and Sudoku as the two primary front-page options.
- Custom Shikaku completion popup when the full board validates cleanly.
- Android game UX research notes in `research_android_game_ux/`.
- Rounded vector icon pack for the dock and action controls.

## Changed

- Toned down the glass style: only the floating navbar keeps the glass treatment.
- Reworked cards, settings rows, popups, and game options into clean solid squircles.
- Made the floating navbar more elliptical and reduced it to Games, Daily, Themes, and Settings.
- Removed less useful bottom-bar destinations from the main dock.
- Refreshed all color schemes with brighter accents and better panel contrast.
- Changed the default first-run theme to Solar Clean.
- Bumped Android version to `0.1.1` / versionCode `2`.

## Fixed

- Shikaku completion now has an explicit solved-state API with tests.
- A valid Shikaku board no longer only shows a small status message; it opens a custom completion popup.

## Known Limits

- Session persistence schema exists, but active game state is still in-memory in this UI pass.
- Release APK is debug-signed unless a production signing config is added.
- No Play Store publishing or cloud sync.

# Glass Puzzle Hub v0.1.0

Initial Android release.

## Included

- Hub-first Android app shell.
- Shikaku quick play and daily generation.
- Sudoku quick play and daily generation.
- Custom glass UI with floating navigation and custom icon primitives.
- Canvas-rendered Shikaku and Sudoku boards.
- Six custom themes.
- DataStore-backed theme/settings persistence.
- Room schema for sessions, daily records, stats, achievements, and generated puzzle cache.
- Unit tests for shared contracts, Shikaku, and Sudoku engines.

## Known Limits

- Session persistence schema exists, but active game state is still in-memory in this V1 UI pass.
- Release APK is debug-signed unless a production signing config is added.
- No Play Store publishing or cloud sync.
