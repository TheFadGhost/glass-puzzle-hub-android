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
