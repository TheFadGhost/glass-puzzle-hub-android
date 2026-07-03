package com.thefadghost.glasspuzzlehub.model

import kotlinx.serialization.Serializable
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

@Serializable
enum class GameId(val value: String, val displayName: String) {
    Shikaku("shikaku", "Shikaku"),
    Sudoku("sudoku", "Sudoku"),
    Slitherlink("slitherlink", "Slitherlink"),
    Nurikabe("nurikabe", "Nurikabe"),
    Kakuro("kakuro", "Kakuro"),
}

@Serializable
enum class Difficulty(val label: String) {
    Beginner("Beginner"),
    Easy("Easy"),
    Medium("Medium"),
    Hard("Hard"),
    Expert("Expert"),
    Master("Master"),
}

@Serializable
data class PuzzleDescriptor(
    val gameId: GameId,
    val puzzleId: String,
    val seed: Long,
    val difficulty: Difficulty,
    val createdForDate: String? = null,
    val generatorVersion: Int,
)

@Serializable
data class GameSession(
    val sessionId: String,
    val descriptor: PuzzleDescriptor,
    val statePayload: String,
    val elapsedMs: Long,
    val moveCount: Int,
    val hintsUsed: Int,
    val mistakes: Int,
    val startedAt: Long,
    val completedAt: Long? = null,
) {
    val isComplete: Boolean
        get() = completedAt != null

    companion object {
        fun fresh(
            sessionId: String,
            descriptor: PuzzleDescriptor,
            statePayload: String,
            startedAt: Long = System.currentTimeMillis(),
        ): GameSession = GameSession(
            sessionId = sessionId,
            descriptor = descriptor,
            statePayload = statePayload,
            elapsedMs = 0L,
            moveCount = 0,
            hintsUsed = 0,
            mistakes = 0,
            startedAt = startedAt,
            completedAt = null,
        )
    }
}

@Serializable
data class MoveRecord(
    val beforePayload: String,
    val afterPayload: String,
    val moveType: String,
    val timestampMs: Long,
)

@Serializable
data class StatsRecord(
    val gameId: GameId? = null,
    val solves: Int = 0,
    val bestTimeMs: Long? = null,
    val averageTimeMs: Long? = null,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalHints: Int = 0,
    val totalMistakes: Int = 0,
)

@Serializable
data class PuzzleBundle(
    val descriptor: PuzzleDescriptor,
    val puzzlePayload: String,
    val statePayload: String,
)

@Serializable
data class ValidationResult(
    val isValid: Boolean,
    val message: String? = null,
) {
    companion object {
        val Valid = ValidationResult(true)
        fun invalid(message: String) = ValidationResult(false, message)
    }
}

@Serializable
data class Hint(
    val title: String,
    val body: String,
    val payload: String? = null,
)

interface GameDefinition {
    val id: GameId
    val displayName: String
    val shortName: String
    val rulesSummary: String
    val difficultySet: Set<Difficulty>
    val themeAccent: Long

    fun createRandomPuzzle(seed: Long, difficulty: Difficulty): PuzzleBundle
    fun createDailyPuzzle(date: String, difficulty: Difficulty): PuzzleBundle
    fun validateMove(statePayload: String, movePayload: String): ValidationResult
    fun checkCompletion(statePayload: String): ValidationResult
    fun serializeState(state: Any): String
    fun deserializeState(payload: String): Any
    fun createHint(statePayload: String, hintLevel: Int): Hint
}

object DailySeed {
    fun forPuzzle(
        gameId: GameId,
        date: String,
        difficulty: Difficulty,
        generatorVersion: Int,
    ): Long {
        val input = "${gameId.value}|$date|${difficulty.name}|$generatorVersion"
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray(StandardCharsets.UTF_8))
        var value = 0L
        for (index in 0 until 8) {
            value = (value shl 8) or (digest[index].toLong() and 0xffL)
        }
        return value and Long.MAX_VALUE
    }
}
