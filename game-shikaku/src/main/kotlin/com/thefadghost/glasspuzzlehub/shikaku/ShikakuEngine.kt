package com.thefadghost.glasspuzzlehub.shikaku

import com.thefadghost.glasspuzzlehub.model.Difficulty
import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.random.Random

@Serializable
data class ShikakuCell(val row: Int, val col: Int)

@Serializable
data class ShikakuClue(val cell: ShikakuCell, val value: Int)

@Serializable
data class ShikakuRect(
    val top: Int,
    val left: Int,
    val bottom: Int,
    val right: Int,
) {
    init {
        require(top <= bottom) { "top must be <= bottom" }
        require(left <= right) { "left must be <= right" }
    }

    val width: Int get() = right - left + 1
    val height: Int get() = bottom - top + 1
    val area: Int get() = width * height

    fun contains(cell: ShikakuCell): Boolean =
        cell.row in top..bottom && cell.col in left..right

    fun overlaps(other: ShikakuRect): Boolean =
        left <= other.right &&
            right >= other.left &&
            top <= other.bottom &&
            bottom >= other.top

    fun cells(): Sequence<ShikakuCell> = sequence {
        for (row in top..bottom) {
            for (col in left..right) {
                yield(ShikakuCell(row, col))
            }
        }
    }
}

@Serializable
data class ShikakuPuzzle(
    val width: Int,
    val height: Int,
    val clues: List<ShikakuClue>,
    val solution: List<ShikakuRect>,
    val seed: Long,
    val difficulty: Difficulty,
) {
    val puzzleId: String = "shikaku-$seed-${difficulty.name.lowercase()}"
}

@Serializable
data class ShikakuState(
    val puzzle: ShikakuPuzzle,
    val placedRects: List<ShikakuRect> = emptyList(),
    val selectedRectIndex: Int? = null,
    val mode: String = "draw",
)

data class ShikakuValidation(val isValid: Boolean, val message: String? = null)

object ShikakuValidator {
    fun validate(puzzle: ShikakuPuzzle, rects: List<ShikakuRect>): ShikakuValidation {
        if (rects.size != puzzle.clues.size) {
            return ShikakuValidation(false, "Expected ${puzzle.clues.size} rectangles.")
        }

        val covered = Array(puzzle.height) { BooleanArray(puzzle.width) }
        for (rect in rects) {
            if (rect.top < 0 || rect.left < 0 || rect.bottom >= puzzle.height || rect.right >= puzzle.width) {
                return ShikakuValidation(false, "A rectangle leaves the board.")
            }

            val cluesInside = puzzle.clues.filter { rect.contains(it.cell) }
            if (cluesInside.size != 1) {
                return ShikakuValidation(false, "Each rectangle must contain exactly one clue.")
            }
            if (cluesInside.single().value != rect.area) {
                return ShikakuValidation(false, "Rectangle area does not match its clue.")
            }

            for (cell in rect.cells()) {
                if (covered[cell.row][cell.col]) {
                    return ShikakuValidation(false, "Rectangles overlap.")
                }
                covered[cell.row][cell.col] = true
            }
        }

        for (row in 0 until puzzle.height) {
            for (col in 0 until puzzle.width) {
                if (!covered[row][col]) {
                    return ShikakuValidation(false, "Every cell must be covered.")
                }
            }
        }

        return ShikakuValidation(true)
    }
}

object ShikakuSolver {
    fun countSolutions(puzzle: ShikakuPuzzle, limit: Int = 2): Int {
        val candidates = puzzle.clues.map { clue ->
            generateCandidates(puzzle, clue).filter { rect ->
                puzzle.clues.count { rect.contains(it.cell) } == 1
            }
        }

        if (candidates.any { it.isEmpty() }) return 0

        val occupied = Array(puzzle.height) { BooleanArray(puzzle.width) }
        val usedClues = BooleanArray(puzzle.clues.size)
        return search(puzzle, candidates, occupied, usedClues, 0, limit)
    }

    private fun search(
        puzzle: ShikakuPuzzle,
        candidates: List<List<ShikakuRect>>,
        occupied: Array<BooleanArray>,
        usedClues: BooleanArray,
        placed: Int,
        limit: Int,
    ): Int {
        if (placed == puzzle.clues.size) {
            return if (allCovered(occupied)) 1 else 0
        }

        var bestIndex = -1
        var bestOptions = emptyList<ShikakuRect>()
        for (index in candidates.indices) {
            if (usedClues[index]) continue
            val options = candidates[index].filter { canPlace(it, occupied) }
            if (options.isEmpty()) return 0
            if (bestIndex == -1 || options.size < bestOptions.size) {
                bestIndex = index
                bestOptions = options
            }
        }

        var count = 0
        usedClues[bestIndex] = true
        for (rect in bestOptions) {
            mark(rect, occupied, true)
            count += search(puzzle, candidates, occupied, usedClues, placed + 1, limit)
            mark(rect, occupied, false)
            if (count >= limit) break
        }
        usedClues[bestIndex] = false
        return count
    }

    private fun generateCandidates(puzzle: ShikakuPuzzle, clue: ShikakuClue): List<ShikakuRect> {
        val result = mutableListOf<ShikakuRect>()
        for (height in 1..clue.value) {
            if (clue.value % height != 0) continue
            val width = clue.value / height
            for (top in (clue.cell.row - height + 1)..clue.cell.row) {
                val bottom = top + height - 1
                if (top < 0 || bottom >= puzzle.height) continue
                for (left in (clue.cell.col - width + 1)..clue.cell.col) {
                    val right = left + width - 1
                    if (left < 0 || right >= puzzle.width) continue
                    result += ShikakuRect(top, left, bottom, right)
                }
            }
        }
        return result.distinct()
    }

    private fun canPlace(rect: ShikakuRect, occupied: Array<BooleanArray>): Boolean =
        rect.cells().all { !occupied[it.row][it.col] }

    private fun mark(rect: ShikakuRect, occupied: Array<BooleanArray>, value: Boolean) {
        rect.cells().forEach { occupied[it.row][it.col] = value }
    }

    private fun allCovered(occupied: Array<BooleanArray>): Boolean =
        occupied.all { row -> row.all { it } }
}

object ShikakuGenerator {
    fun generate(seed: Long, difficulty: Difficulty): ShikakuPuzzle {
        val size = when (difficulty) {
            Difficulty.Beginner -> 5
            Difficulty.Easy -> 6
            Difficulty.Medium -> 7
            Difficulty.Hard -> 8
            Difficulty.Expert -> 9
            Difficulty.Master -> 10
        }
        val random = Random(seed)
        val solution = mutableListOf<ShikakuRect>()
        val consumed = Array(size) { BooleanArray(size) }

        for (row in 0 until size) {
            var col = 0
            while (col < size) {
                if (consumed[row][col]) {
                    col++
                    continue
                }
                val maxWidth = (1..3).takeWhile { col + it <= size && (0 until it).all { offset -> !consumed[row][col + offset] } }
                    .lastOrNull() ?: 1
                val width = if (maxWidth == 1 || random.nextInt(100) < 45) 1 else random.nextInt(2, maxWidth + 1)
                val rect = ShikakuRect(row, col, row, col + width - 1)
                solution += rect
                rect.cells().forEach { consumed[it.row][it.col] = true }
                col += width
            }
        }

        val adjusted = mergeSomeVerticalStrips(solution, size, random)
        val clues = adjusted.map { rect ->
            val clueRow = if (random.nextBoolean()) rect.top else rect.bottom
            val clueCol = if (random.nextBoolean()) rect.left else rect.right
            ShikakuClue(ShikakuCell(clueRow, clueCol), rect.area)
        }

        return ShikakuPuzzle(
            width = size,
            height = size,
            clues = clues,
            solution = adjusted,
            seed = abs(seed),
            difficulty = difficulty,
        )
    }

    private fun mergeSomeVerticalStrips(
        initial: List<ShikakuRect>,
        size: Int,
        random: Random,
    ): List<ShikakuRect> {
        val remaining = initial.toMutableList()
        val result = mutableListOf<ShikakuRect>()
        val byCell = remaining.associateBy { it.top to it.left }.toMutableMap()

        for (row in 0 until size) {
            for (col in 0 until size) {
                val rect = byCell.remove(row to col) ?: continue
                if (rect.width == 1 && row + 1 < size && random.nextInt(100) < 18) {
                    val below = byCell[row + 1 to col]
                    if (below != null && below.width == 1) {
                        byCell.remove(row + 1 to col)
                        result += ShikakuRect(rect.top, rect.left, below.bottom, below.right)
                    } else {
                        result += rect
                    }
                } else {
                    result += rect
                }
            }
        }
        return result
    }
}
