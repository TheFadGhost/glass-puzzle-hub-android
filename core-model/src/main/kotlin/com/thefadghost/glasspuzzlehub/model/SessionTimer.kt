package com.thefadghost.glasspuzzlehub.model

import kotlinx.serialization.Serializable

@Serializable
data class SessionTimerState(
    val accumulatedMs: Long = 0L,
    val runningSinceMs: Long? = null,
) {
    val isRunning: Boolean
        get() = runningSinceMs != null

    fun elapsedAt(nowMs: Long): Long {
        val startedAt = runningSinceMs ?: return accumulatedMs.coerceAtLeast(0L)
        return (accumulatedMs + (nowMs - startedAt).coerceAtLeast(0L)).coerceAtLeast(0L)
    }

    fun resume(nowMs: Long): SessionTimerState =
        if (isRunning) this else copy(runningSinceMs = nowMs)

    fun pause(nowMs: Long): SessionTimerState =
        if (!isRunning) this else SessionTimerState(accumulatedMs = elapsedAt(nowMs), runningSinceMs = null)
}

object SessionTimerFormatter {
    fun format(elapsedMs: Long): String {
        val totalSeconds = (elapsedMs.coerceAtLeast(0L) / 1000L)
        val minutes = totalSeconds / 60L
        val seconds = totalSeconds % 60L
        return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    }
}
