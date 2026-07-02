package com.thefadghost.glasspuzzlehub.sudoku

import com.thefadghost.glasspuzzlehub.model.Difficulty
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SudokuEngineTest {
    @Test
    fun validatorAcceptsSolvedGridAndRejectsDuplicateInRow() {
        val solved = SudokuGrid.fromRows(
            "534678912",
            "672195348",
            "198342567",
            "859761423",
            "426853791",
            "713924856",
            "961537284",
            "287419635",
            "345286179",
        )

        assertTrue(SudokuValidator.isSolved(solved))

        val broken = solved.withValue(0, 1, 5)
        assertFalse(SudokuValidator.isSolved(broken))
    }

    @Test
    fun notesDoNotCountAsValues() {
        val grid = SudokuGrid.empty().withNote(0, 0, 7)

        assertEquals(0, grid.valueAt(0, 0))
        assertTrue(grid.notesAt(0, 0).contains(7))
    }

    @Test
    fun generatorCreatesUniquePuzzle() {
        val puzzle = SudokuGenerator.generate(seed = 20260703L, difficulty = Difficulty.Easy)

        assertEquals(1, SudokuSolver.countSolutions(puzzle.givens, limit = 2))
        assertTrue(SudokuValidator.isSolved(puzzle.solution))
    }
}
