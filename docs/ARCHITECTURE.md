# Architecture

Glass Puzzle Hub is structured as a game hub, not a single-game app. Game modules own puzzle logic and the app owns composition, navigation, and cross-game UX.

## Shared Contracts

`core-model` defines stable interfaces and serializable types:

- `GameId`
- `Difficulty`
- `PuzzleDescriptor`
- `GameSession`
- `MoveRecord`
- `StatsRecord`
- `GameDefinition`
- `DailySeed`

The app can add a new puzzle by adding another game module and registering its screens/definition without changing storage or global settings.

## UI System

`core-ui` contains the custom design system:

- glass theme tokens
- glass panels
- custom floating dock
- custom icon primitives drawn with Compose Canvas
- text helpers using sans and monospace stacks

The app does not use Material buttons, Material navigation, platform dialogs, or XML layouts for UI.

## Storage

`core-storage` provides Room tables for:

- game sessions
- daily records
- stats
- achievements
- generated puzzle cache

DataStore stores user settings:

- theme
- haptics
- sound
- reduced motion
- high contrast

## Game Modules

Shikaku and Sudoku are pure Kotlin modules. This keeps solver/generator logic testable without Android runtime dependencies.

Shikaku includes rectangle validation, exact-cover-style solution counting, and deterministic generation.

Sudoku includes grid validation, notes, backtracking solution counting, and deterministic generation from seeded solved grids.
