package com.thefadghost.glasspuzzlehub.model

data class DisplayModeCandidate(
    val id: Int,
    val refreshRate: Float,
    val width: Int,
    val height: Int,
) {
    val pixels: Int get() = width * height
}

object HighRefreshSelector {
    fun bestMode(candidates: List<DisplayModeCandidate>): DisplayModeCandidate? =
        candidates.maxWithOrNull(compareBy<DisplayModeCandidate> { it.refreshRate }.thenBy { it.pixels })
}
