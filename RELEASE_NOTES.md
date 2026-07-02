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
