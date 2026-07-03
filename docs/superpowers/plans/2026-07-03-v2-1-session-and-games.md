# v2.1 Session And Games Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build v2.1.0 with smart timers, persisted continue sessions, fixed touch targets/dock sizing, and playable Slitherlink, Nurikabe, and Kakuro.

**Architecture:** Add testable pure Kotlin timer and extra-game engines, expose active sessions through the existing Room module, then wire the Compose shell to store and restore game-specific JSON payloads. UI changes stay in the existing custom Canvas/Glass component system.

**Tech Stack:** Kotlin, Jetpack Compose Canvas, Room, DataStore, kotlinx.serialization, Gradle Kotlin DSL, kotlin.test.

---

### Task 1: Smart Timer Model

**Files:**
- Create: `core-model/src/main/kotlin/com/thefadghost/glasspuzzlehub/model/SessionTimer.kt`
- Test: `core-model/src/test/kotlin/com/thefadghost/glasspuzzlehub/model/SessionTimerTest.kt`

- [ ] Write failing tests for resume, pause, and formatting.
- [ ] Implement `SessionTimerState`.
- [ ] Run `.\gradlew.bat :core-model:test`.

### Task 2: Session Repository

**Files:**
- Modify: `core-storage/src/main/kotlin/com/thefadghost/glasspuzzlehub/storage/HubStorage.kt`

- [ ] Add DAO queries for active session by game and latest active sessions.
- [ ] Add `SessionRepository` using Room database builder.
- [ ] Keep schema unchanged and reuse `game_sessions`.

### Task 3: Variety Game Engines

**Files:**
- Create: `game-variety/build.gradle.kts`
- Create: `game-variety/src/main/kotlin/com/thefadghost/glasspuzzlehub/variety/VarietyEngines.kt`
- Test: `game-variety/src/test/kotlin/com/thefadghost/glasspuzzlehub/variety/VarietyEnginesTest.kt`
- Modify: `settings.gradle.kts`

- [ ] Write failing tests for solved/unsolved Slitherlink, Nurikabe, and Kakuro states.
- [ ] Implement deterministic compact generators and validators.
- [ ] Run `.\gradlew.bat :game-variety:test`.

### Task 4: Compose UI Wiring

**Files:**
- Modify: `core-model/src/main/kotlin/com/thefadghost/glasspuzzlehub/model/GameContracts.kt`
- Modify: `core-ui/src/main/kotlin/com/thefadghost/glasspuzzlehub/ui/GlassComponents.kt`
- Modify: `app/src/main/java/com/thefadghost/glasspuzzlehub/MainActivity.kt`

- [ ] Add new `GameId` values.
- [ ] Make the dock wrap content with four proportional icons.
- [ ] Make mode chips clickable across the entire card.
- [ ] Add JSON save payloads and load/continue logic.
- [ ] Add timer display and lifecycle pause/resume.
- [ ] Add Canvas boards for Slitherlink, Nurikabe, and Kakuro.

### Task 5: Release Metadata

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `README.md`
- Modify: `RELEASE_NOTES.md`

- [ ] Bump version to 2.1.0 / versionCode 4.
- [ ] Document new features and known limits.

### Task 6: Verification And Release

- [ ] Run `.\gradlew.bat test lint assembleDebug`.
- [ ] Install and smoke-test on emulator through the main flows.
- [ ] Commit, push, tag `v2.1.0`, and publish a GitHub release with the debug APK.
