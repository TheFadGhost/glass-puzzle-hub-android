package com.thefadghost.glasspuzzlehub.sudoku

import com.thefadghost.glasspuzzlehub.model.Difficulty
import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.random.Random

@Serializable
data class SudokuCell(val row: Int, val col: Int)

@Serializable
data class SudokuGrid(
    val values: List<Int>,
    val givens: List<Boolean> = List(81) { false },
    val notes: List<Set<Int>> = List(81) { emptySet() },
) {
    init {
        require(values.size == 81) { "Sudoku values must contain 81 cells." }
        require(givens.size == 81) { "Sudoku givens must contain 81 cells." }
        require(notes.size == 81) { "Sudoku notes must contain 81 cells." }
    }

    fun valueAt(row: Int, col: Int): Int = values[index(row, col)]
    fun notesAt(row: Int, col: Int): Set<Int> = notes[index(row, col)]

    fun withValue(row: Int, col: Int, value: Int): SudokuGrid {
        require(value in 0..9) { "Sudoku value must be 0..9." }
        val index = index(row, col)
        return copy(values = values.toMutableList().also { it[index] = value })
    }

    fun withNote(row: Int, col: Int, value: Int): SudokuGrid {
        require(value in 1..9) { "Sudoku note must be 1..9." }
        val index = index(row, col)
        return copy(notes = notes.toMutableList().also { it[index] = it[index] + value })
    }

    companion object {
        fun empty(): SudokuGrid = SudokuGrid(List(81) { 0 })

        fun fromRows(vararg rows: String, givens: Boolean = false): SudokuGrid {
            require(rows.size == 9)
            val values = rows.flatMap { row ->
                require(row.length == 9)
                row.map { char ->
                    when (char) {
                        '.', '0' -> 0
                        else -> char.digitToInt()
                    }
                }
            }
            return SudokuGrid(values, List(81) { givens && values[it] != 0 })
        }

        fun index(row: Int, col: Int): Int {
            require(row in 0..8 && col in 0..8)
            return row * 9 + col
        }
    }
}

@Serializable
data class SudokuPuzzle(
    val givens: SudokuGrid,
    val solution: SudokuGrid,
    val seed: Long,
    val difficulty: Difficulty,
) {
    val puzzleId: String = "sudoku-$seed-${difficulty.name.lowercase()}"
}

@Serializable
data class SudokuState(
    val puzzle: SudokuPuzzle,
    val grid: SudokuGrid,
    val selectedCell: SudokuCell? = null,
    val selectedDigit: Int? = null,
    val inputMode: String = "value",
)

object SudokuValidator {
    fun isSolved(grid: SudokuGrid): Boolean {
        val all = (1..9).toSet()
        for (row in 0..8) {
            if ((0..8).map { grid.valueAt(row, it) }.toSet() != all) return false
        }
        for (col in 0..8) {
            if ((0..8).map { grid.valueAt(it, col) }.toSet() != all) return false
        }
        for (boxRow in 0..2) {
            for (boxCol in 0..2) {
                val values = mutableSetOf<Int>()
                for (row in boxRow * 3 until boxRow * 3 + 3) {
                    for (col in boxCol * 3 until boxCol * 3 + 3) {
                        values += grid.valueAt(row, col)
                    }
                }
                if (values != all) return false
            }
        }
        return true
    }

    fun conflicts(grid: SudokuGrid, row: Int, col: Int, value: Int): Boolean {
        if (value == 0) return false
        for (i in 0..8) {
            if (i != col && grid.valueAt(row, i) == value) return true
            if (i != row && grid.valueAt(i, col) == value) return true
        }
        val boxRow = row / 3 * 3
        val boxCol = col / 3 * 3
        for (r in boxRow until boxRow + 3) {
            for (c in boxCol until boxCol + 3) {
                if ((r != row || c != col) && grid.valueAt(r, c) == value) return true
            }
        }
        return false
    }
}

object SudokuSolver {
    fun countSolutions(grid: SudokuGrid, limit: Int = 2): Int {
        val values = grid.values.toIntArray()
        return solve(values, limit)
    }

    private fun solve(values: IntArray, limit: Int): Int {
        val empty = chooseEmpty(values) ?: return 1
        var count = 0
        for (value in 1..9) {
            if (!canPlace(values, empty, value)) continue
            values[empty] = value
            count += solve(values, limit)
            values[empty] = 0
            if (count >= limit) break
        }
        return count
    }

    private fun chooseEmpty(values: IntArray): Int? {
        var bestIndex: Int? = null
        var bestCount = 10
        for (index in values.indices) {
            if (values[index] != 0) continue
            val count = (1..9).count { canPlace(values, index, it) }
            if (count < bestCount) {
                bestIndex = index
                bestCount = count
                if (count == 1) break
            }
        }
        return bestIndex
    }

    private fun canPlace(values: IntArray, index: Int, value: Int): Boolean {
        val row = index / 9
        val col = index % 9
        for (i in 0..8) {
            if (values[row * 9 + i] == value) return false
            if (values[i * 9 + col] == value) return false
        }
        val boxRow = row / 3 * 3
        val boxCol = col / 3 * 3
        for (r in boxRow until boxRow + 3) {
            for (c in boxCol until boxCol + 3) {
                if (values[r * 9 + c] == value) return false
            }
        }
        return true
    }
}

object SudokuGenerator {
    private val baseRows = arrayOf(
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

    fun generate(seed: Long, difficulty: Difficulty): SudokuPuzzle {
        val random = Random(seed)
        val solution = shuffledSolution(random)
        val targetGivens = when (difficulty) {
            Difficulty.Beginner -> 44
            Difficulty.Easy -> 38
            Difficulty.Medium -> 34
            Difficulty.Hard -> 30
            Difficulty.Expert -> 28
            Difficulty.Master -> 26
        }

        val puzzleValues = solution.values.toMutableList()
        val cells = (0 until 81).shuffled(random)
        for (cell in cells) {
            if (puzzleValues.count { it != 0 } <= targetGivens) break
            val mirror = 80 - cell
            val old = puzzleValues[cell]
            val oldMirror = puzzleValues[mirror]
            puzzleValues[cell] = 0
            puzzleValues[mirror] = 0

            val candidate = SudokuGrid(puzzleValues.toList(), List(81) { puzzleValues[it] != 0 })
            if (SudokuSolver.countSolutions(candidate, 2) != 1) {
                puzzleValues[cell] = old
                puzzleValues[mirror] = oldMirror
            }
        }

        val givens = SudokuGrid(puzzleValues.toList(), List(81) { puzzleValues[it] != 0 })
        return SudokuPuzzle(givens, solution, abs(seed), difficulty)
    }

    private fun shuffledSolution(random: Random): SudokuGrid {
        val base = SudokuGrid.fromRows(*baseRows, givens = true)
        val digitMap = (1..9).shuffled(random).withIndex().associate { (index, digit) -> index + 1 to digit }
        val mapped = base.values.map { digitMap.getValue(it) }
        return SudokuGrid(mapped, List(81) { true })
    }
}
