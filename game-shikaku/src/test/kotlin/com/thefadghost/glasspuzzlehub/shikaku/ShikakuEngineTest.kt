package com.thefadghost.glasspuzzlehub.shikaku

import com.thefadghost.glasspuzzlehub.model.Difficulty
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ShikakuEngineTest {
    @Test
    fun rectangleAreaBoundsAndOverlapAreComputed() {
        val rect = ShikakuRect(top = 1, left = 2, bottom = 3, right = 5)

        assertEquals(12, rect.area)
        assertTrue(rect.contains(ShikakuCell(2, 4)))
        assertFalse(rect.contains(ShikakuCell(4, 4)))
        assertTrue(rect.overlaps(ShikakuRect(3, 5, 4, 6)))
        assertFalse(rect.overlaps(ShikakuRect(4, 5, 5, 6)))
    }

    @Test
    fun validatorRejectsWrongAreaAndOverlaps() {
        val puzzle = ShikakuPuzzle(
            width = 2,
            height = 2,
            clues = listOf(
                ShikakuClue(ShikakuCell(0, 0), 2),
                ShikakuClue(ShikakuCell(1, 1), 2),
            ),
            solution = listOf(
                ShikakuRect(0, 0, 0, 1),
                ShikakuRect(1, 0, 1, 1),
            ),
            seed = 7L,
            difficulty = Difficulty.Beginner,
        )

        val bad = ShikakuValidator.validate(
            puzzle,
            listOf(
                ShikakuRect(0, 0, 1, 0),
                ShikakuRect(0, 0, 0, 1),
            ),
        )

        assertFalse(bad.isValid)
    }

    @Test
    fun generatorCreatesUniquelySolvedPuzzle() {
        val puzzle = ShikakuGenerator.generate(seed = 99L, difficulty = Difficulty.Beginner)

        val solutionCount = ShikakuSolver.countSolutions(puzzle, limit = 2)

        assertEquals(1, solutionCount)
        assertTrue(ShikakuValidator.validate(puzzle, puzzle.solution).isValid)
    }
}
