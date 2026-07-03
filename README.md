# Glass Puzzle Hub

Glass Puzzle Hub is a native Android logic game hub with a clean custom UI, a glass floating dock, smart persisted sessions, and canvas-rendered puzzle boards. V2.1 includes Shikaku, Sudoku, Slitherlink, Nurikabe, and Kakuro.

## Games

- Shikaku: partition the board into rectangles where each rectangle contains exactly one clue and its area matches that clue.
- Sudoku: fill a 9x9 grid with digits 1 through 9 so every row, column, and 3x3 box contains each digit once.
- Slitherlink: toggle edges to draw one continuous loop around numbered cells.
- Nurikabe: shade wall cells while keeping numbered islands unshaded.
- Kakuro: fill white cells so across and down sum clues match.

## Design Direction

- Kotlin and Jetpack Compose with custom Canvas rendering.
- No Material components, platform dialogs, XML layouts, stock Android buttons, or stock in-app navigation.
- Games-first home screen with two primary puzzle options.
- Floating glass dock, rounded vector icons, custom theme tokens, custom boards, and custom settings toggles.
- Solid squircle panels for cards, settings, popups, and game surfaces so the glass effect stays restrained.
- High-refresh display preference for devices that expose 90Hz/120Hz modes.
- Smart timer pauses when the app leaves foreground and resumes from saved elapsed time.
- Continue sessions persist unfinished maps through app close and force stop.
- Shikaku modes: Classic, Mini, Wide, Tall, Large, and Shadow Blocks.
- Sudoku modes: Classic playable, with Mini, Diagonal, Irregular, and Killer staged as planned engines.
- Playable compact engines for Slitherlink, Nurikabe, and Kakuro.
- Six themes: Noir Glass, Frost Glass, Aurora Glass, Ember Glass, Mono Ink, and Solar Clean.

## Architecture

- `app`: single-activity shell, navigation, game screens, board input.
- `core-model`: shared game IDs, difficulties, sessions, stats, validation contracts, daily seed generation.
- `core-ui`: reusable custom glass components and theme tokens.
- `core-storage`: Room entities/DAO and DataStore settings.
- `game-shikaku`: Shikaku model, validator, solver, generator.
- `game-sudoku`: Sudoku model, validator, solver, generator.
- `game-variety`: Slitherlink, Nurikabe, and Kakuro compact engines.

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

## Current Limits

- Continue sessions are wired for active puzzle state and elapsed time; full archive/history browsing is still basic.
- Shikaku has a completion popup when the board validates cleanly; Sudoku completion currently reports through the play message.
- Slitherlink, Nurikabe, and Kakuro are compact first playable engines, not full production-grade generators yet.
- No Play Store signing or cloud sync.
- Daily puzzles are generated locally from date-based seeds.
