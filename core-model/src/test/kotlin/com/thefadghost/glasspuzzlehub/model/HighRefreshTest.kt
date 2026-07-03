package com.thefadghost.glasspuzzlehub.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HighRefreshTest {
    @Test
    fun selectorPrefersHighestRefreshRate() {
        val selected = HighRefreshSelector.bestMode(
            listOf(
                DisplayModeCandidate(id = 1, refreshRate = 60f, width = 1080, height = 2400),
                DisplayModeCandidate(id = 2, refreshRate = 120f, width = 1080, height = 2400),
                DisplayModeCandidate(id = 3, refreshRate = 90f, width = 1440, height = 3200),
            ),
        )

        assertEquals(2, selected?.id)
    }

    @Test
    fun selectorBreaksRefreshTiesByResolution() {
        val selected = HighRefreshSelector.bestMode(
            listOf(
                DisplayModeCandidate(id = 1, refreshRate = 120f, width = 1080, height = 2400),
                DisplayModeCandidate(id = 2, refreshRate = 120f, width = 1440, height = 3200),
            ),
        )

        assertEquals(2, selected?.id)
    }

    @Test
    fun selectorReturnsNullForNoModes() {
        assertNull(HighRefreshSelector.bestMode(emptyList()))
    }
}
