package com.thefadghost.glasspuzzlehub.variety

import com.thefadghost.glasspuzzlehub.model.Difficulty
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VarietyEnginesTest {
    @Test
    fun slitherlinkRecognizesSolutionEdgesOnly() {
        val puzzle = SlitherlinkGenerator.generate(seed = 10L, difficulty = Difficulty.Easy)

        assertTrue(SlitherlinkValidator.isSolved(puzzle, SlitherlinkState(puzzle.solutionEdges)))
        assertFalse(SlitherlinkValidator.isSolved(puzzle, SlitherlinkState(emptySet())))
    }

    @Test
    fun nurikabeRecognizesSolutionShadingOnly() {
        val puzzle = NurikabeGenerator.generate(seed = 20L, difficulty = Difficulty.Easy)

        assertTrue(NurikabeValidator.isSolved(puzzle, NurikabeState(puzzle.solutionShaded)))
        assertFalse(NurikabeValidator.isSolved(puzzle, NurikabeState(emptySet())))
    }

    @Test
    fun kakuroRecognizesExactDigitSolutionOnly() {
        val puzzle = KakuroGenerator.generate(seed = 30L, difficulty = Difficulty.Easy)

        assertTrue(KakuroValidator.isSolved(puzzle, KakuroState(puzzle.solutionValues)))
        assertFalse(KakuroValidator.isSolved(puzzle, KakuroState(emptyMap())))
    }
}
