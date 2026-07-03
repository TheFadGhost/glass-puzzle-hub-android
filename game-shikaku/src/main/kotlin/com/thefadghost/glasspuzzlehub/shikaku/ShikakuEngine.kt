package com.thefadghost.glasspuzzlehub.shikaku

import com.thefadghost.glasspuzzlehub.model.Difficulty
import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.random.Random

@Serializable
enum class ShikakuMode(val label: String, val summary: String) {
    Classic("Classic", "Balanced square boards."),
    Mini("Mini", "Smaller fast boards for quick solves."),
    Wide("Wide", "Horizontal boards that change rectangle scanning."),
    Tall("Tall", "Vertical boards for different drag rhythm."),
    Large("Large", "Bigger boards with more regions."),
    ShadowBlocks("Shadow Blocks", "Shaded cells are covered but do not count toward clue area."),
}

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
    val blockedCells: List<ShikakuCell> = emptyList(),
    val mode: ShikakuMode = ShikakuMode.Classic,
) {
    val puzzleId: String = "shikaku-$seed-${difficulty.name.lowercase()}-${mode.name.lowercase()}"

    private val blockedSet: Set<ShikakuCell> = blockedCells.toSet()

    fun isBlocked(cell: ShikakuCell): Boolean = cell in blockedSet

    fun effectiveArea(rect: ShikakuRect): Int =
        rect.cells().count { it !in blockedSet }
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
            if (cluesInside.single().value != puzzle.effectiveArea(rect)) {
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

object ShikakuCompletion {
    fun isComplete(puzzle: ShikakuPuzzle, rects: List<ShikakuRect>): Boolean =
        ShikakuValidator.validate(puzzle, rects).isValid
}

object ShikakuInteractions {
    fun singleCellRectForTap(puzzle: ShikakuPuzzle, cell: ShikakuCell): ShikakuRect? {
        val clue = puzzle.clues.firstOrNull { it.cell == cell } ?: return null
        if (puzzle.isBlocked(cell)) return null
        val rect = ShikakuRect(cell.row, cell.col, cell.row, cell.col)
        return if (clue.value == puzzle.effectiveArea(rect)) rect else null
    }

    fun removeRectAt(rects: List<ShikakuRect>, cell: ShikakuCell): List<ShikakuRect> =
        rects.filterNot { it.contains(cell) }

    fun replaceOverlapping(rects: List<ShikakuRect>, candidate: ShikakuRect): List<ShikakuRect> =
        rects.filterNot { it.overlaps(candidate) } + candidate
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
        for (top in 0..clue.cell.row) {
            for (bottom in clue.cell.row until puzzle.height) {
                for (left in 0..clue.cell.col) {
                    for (right in clue.cell.col until puzzle.width) {
                        val rect = ShikakuRect(top, left, bottom, right)
                        if (puzzle.effectiveArea(rect) == clue.value) result += rect
                    }
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
    fun generate(seed: Long, difficulty: Difficulty, mode: ShikakuMode = ShikakuMode.Classic): ShikakuPuzzle {
        val (width, height) = dimensionsFor(difficulty, mode)
        return generate(seed, difficulty, width, height, mode)
    }

    fun generate(
        seed: Long,
        difficulty: Difficulty,
        width: Int,
        height: Int,
        mode: ShikakuMode = ShikakuMode.Classic,
    ): ShikakuPuzzle {
        repeat(80) { attempt ->
            val puzzle = generateOnce(seed + attempt * 9973L, difficulty, width, height, mode)
            if (ShikakuSolver.countSolutions(puzzle, limit = 2) == 1) return puzzle
        }
        return generateOnce(seed, difficulty, width, height, mode)
    }

    private fun generateOnce(
        seed: Long,
        difficulty: Difficulty,
        width: Int,
        height: Int,
        mode: ShikakuMode,
    ): ShikakuPuzzle {
        val random = Random(seed)
        val solution = mutableListOf<ShikakuRect>()
        val consumed = Array(height) { BooleanArray(width) }

        for (row in 0 until height) {
            var col = 0
            while (col < width) {
                if (consumed[row][col]) {
                    col++
                    continue
                }
                val maxWidth = (1..3).takeWhile { col + it <= width && (0 until it).all { offset -> !consumed[row][col + offset] } }
                    .lastOrNull() ?: 1
                val runWidth = if (maxWidth == 1 || random.nextInt(100) < 45) 1 else random.nextInt(2, maxWidth + 1)
                val rect = ShikakuRect(row, col, row, col + runWidth - 1)
                solution += rect
                rect.cells().forEach { consumed[it.row][it.col] = true }
                col += runWidth
            }
        }

        val adjusted = mergeSomeVerticalStrips(solution, width, height, random)
        val blockedCells = if (mode == ShikakuMode.ShadowBlocks) createBlockedCells(adjusted, random) else emptyList()
        val blockedSet = blockedCells.toSet()
        val clues = adjusted.map { rect ->
            val availableCells = rect.cells().filter { it !in blockedSet }.toList()
            val clueCell = availableCells[random.nextInt(availableCells.size)]
            ShikakuClue(clueCell, availableCells.size)
        }

        return ShikakuPuzzle(
            width = width,
            height = height,
            clues = clues,
            solution = adjusted,
            seed = abs(seed),
            difficulty = difficulty,
            blockedCells = blockedCells,
            mode = mode,
        )
    }

    private fun dimensionsFor(difficulty: Difficulty, mode: ShikakuMode): Pair<Int, Int> {
        val base = when (difficulty) {
            Difficulty.Beginner -> 5
            Difficulty.Easy -> 6
            Difficulty.Medium -> 7
            Difficulty.Hard -> 8
            Difficulty.Expert -> 9
            Difficulty.Master -> 10
        }
        return when (mode) {
            ShikakuMode.Classic -> base to base
            ShikakuMode.Mini -> (base - 1).coerceAtLeast(4) to (base - 1).coerceAtLeast(4)
            ShikakuMode.Wide -> base + 3 to base
            ShikakuMode.Tall -> base to base + 3
            ShikakuMode.Large -> base + 2 to base + 2
            ShikakuMode.ShadowBlocks -> base to base
        }
    }

    private fun createBlockedCells(rects: List<ShikakuRect>, random: Random): List<ShikakuCell> =
        rects.mapNotNull { rect ->
            val cells = rect.cells().toList()
            if (cells.size <= 1 || random.nextInt(100) >= 24) {
                null
            } else {
                cells[random.nextInt(cells.size)]
            }
        }

    private fun mergeSomeVerticalStrips(
        initial: List<ShikakuRect>,
        width: Int,
        height: Int,
        random: Random,
    ): List<ShikakuRect> {
        val remaining = initial.toMutableList()
        val result = mutableListOf<ShikakuRect>()
        val byCell = remaining.associateBy { it.top to it.left }.toMutableMap()

        for (row in 0 until height) {
            for (col in 0 until width) {
                val rect = byCell.remove(row to col) ?: continue
                if (rect.width == 1 && row + 1 < height && random.nextInt(100) < 18) {
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
