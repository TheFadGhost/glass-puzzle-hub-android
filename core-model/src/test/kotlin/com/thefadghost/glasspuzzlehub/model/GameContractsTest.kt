package com.thefadghost.glasspuzzlehub.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class GameContractsTest {
    @Test
    fun gameIdsExposeStableStringValues() {
        assertEquals("shikaku", GameId.Shikaku.value)
        assertEquals("sudoku", GameId.Sudoku.value)
    }

    @Test
    fun dailySeedIsStableAcrossCalls() {
        val first = DailySeed.forPuzzle(GameId.Shikaku, "2026-07-03", Difficulty.Hard, 1)
        val second = DailySeed.forPuzzle(GameId.Shikaku, "2026-07-03", Difficulty.Hard, 1)
        assertEquals(first, second)
    }

    @Test
    fun freshSessionStartsIncompleteWithZeroCounters() {
        val descriptor = PuzzleDescriptor(
            gameId = GameId.Sudoku,
            puzzleId = "sudoku-2026-07-03-easy",
            seed = 42L,
            difficulty = Difficulty.Easy,
            createdForDate = "2026-07-03",
            generatorVersion = 1,
        )

        val session = GameSession.fresh("session-1", descriptor, "{}")

        assertEquals(0L, session.elapsedMs)
        assertEquals(0, session.moveCount)
        assertEquals(0, session.hintsUsed)
        assertEquals(0, session.mistakes)
        assertFalse(session.isComplete)
    }
}
