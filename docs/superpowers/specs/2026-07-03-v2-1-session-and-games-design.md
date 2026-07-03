# v2.1 Session And Games Design

## Goal
Ship Glass Puzzle Hub v2.1.0 with tighter game navigation, larger mode-chip hit targets, lifecycle-aware timers, persisted continue sessions, and first playable versions of Slitherlink, Nurikabe, and Kakuro.

## Scope
- Mode chips must treat the whole visual card as clickable, not just the text.
- The bottom dock must fit the four visible icons instead of stretching across the screen.
- Active play sessions must persist across app close, process death, and force stop. Timers count only while the play screen is active and the app is resumed.
- Game detail screens must show Continue when an unfinished session exists for that game.
- Slitherlink, Nurikabe, and Kakuro must be selectable from the hub and playable from start to solved state in compact V1 engine form.
- The release must be versioned as 2.1.0 and include release notes.

## Architecture
- Add pure model code for the smart timer so lifecycle math can be unit-tested without Android.
- Extend the existing Room-backed storage module with a small session repository around `game_sessions`.
- Add a JVM `game-variety` module for Slitherlink, Nurikabe, and Kakuro models, generators, validators, and interaction helpers.
- Keep the Compose UI custom: Canvas boards, custom chips, custom dock, and no platform widgets.

## Timer Rules
- A session stores accumulated elapsed time.
- When the app enters play while foregrounded, the timer resumes from the stored elapsed time.
- When the app is paused/backgrounded, the timer pauses and writes the accumulated value.
- Timer display updates while resumed; no background wall-clock time is added.

## First Playable Extra Games
- Slitherlink: toggle loop edges around a small clue grid; solved when the marked edges match the generated single-loop solution.
- Nurikabe: toggle shaded cells; solved when the shaded set matches the generated valid wall solution.
- Kakuro: fill white cells with digits using a keypad; solved when all entries match the generated solution.

## Out Of Scope For This Release
- Full procedural difficulty grading for the three new games.
- Advanced Slitherlink/Nurikabe/Kakuro hints.
- Cloud sync, accounts, monetization, or Play Store signing.
