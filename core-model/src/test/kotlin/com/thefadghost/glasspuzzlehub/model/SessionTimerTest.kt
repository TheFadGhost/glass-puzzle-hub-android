package com.thefadghost.glasspuzzlehub.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SessionTimerTest {
    @Test
    fun resumedTimerAddsOnlyForegroundTime() {
        val timer = SessionTimerState(accumulatedMs = 12_000L)
            .resume(nowMs = 100_000L)

        assertTrue(timer.isRunning)
        assertEquals(17_000L, timer.elapsedAt(nowMs = 105_000L))
    }

    @Test
    fun pausedTimerFreezesElapsedTimeAcrossBackgroundTime() {
        val paused = SessionTimerState(accumulatedMs = 12_000L)
            .resume(nowMs = 100_000L)
            .pause(nowMs = 105_000L)

        assertFalse(paused.isRunning)
        assertEquals(17_000L, paused.elapsedAt(nowMs = 205_000L))
    }

    @Test
    fun timerFormattingUsesMinutesAndSeconds() {
        assertEquals("00:00", SessionTimerFormatter.format(0L))
        assertEquals("01:05", SessionTimerFormatter.format(65_000L))
        assertEquals("61:01", SessionTimerFormatter.format(3_661_000L))
    }
}
