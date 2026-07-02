# Glass Puzzle Hub

Glass Puzzle Hub is a native Android logic game hub built around custom glass UI and canvas-rendered puzzle boards. V1 includes Shikaku and Sudoku, with shared contracts for more games later.

## V1 Games

- Shikaku: partition the board into rectangles where each rectangle contains exactly one clue and its area matches that clue.
- Sudoku: fill a 9x9 grid with digits 1 through 9 so every row, column, and 3x3 box contains each digit once.

## Design Direction

- Kotlin and Jetpack Compose with custom Canvas rendering.
- No Material components, platform dialogs, XML layouts, stock Android buttons, or stock in-app navigation.
- Floating glass dock, custom icon primitives, custom theme tokens, custom boards, and custom settings toggles.
- Six themes: Noir Glass, Frost Glass, Aurora Glass, Ember Glass, Mono Ink, and Solar Clean.

## Architecture

- `app`: single-activity shell, navigation, game screens, board input.
- `core-model`: shared game IDs, difficulties, sessions, stats, validation contracts, daily seed generation.
- `core-ui`: reusable custom glass components and theme tokens.
- `core-storage`: Room entities/DAO and DataStore settings.
- `game-shikaku`: Shikaku model, validator, solver, generator.
- `game-sudoku`: Sudoku model, validator, solver, generator.

## Local Build

Use Android Studio's bundled JDK 17+ or set `JAVA_HOME` manually.

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat test
.\gradlew.bat assembleDebug
```

The debug APK is written to:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Current V1 Limits

- Saves and stats storage schema exists; only theme/settings persistence is wired in the first UI pass.
- No Play Store signing or cloud sync.
- Daily puzzles are generated locally from date-based seeds.
