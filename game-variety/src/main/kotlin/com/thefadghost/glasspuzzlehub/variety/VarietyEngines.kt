package com.thefadghost.glasspuzzlehub.variety

import com.thefadghost.glasspuzzlehub.model.Difficulty
import kotlinx.serialization.Serializable
import kotlin.math.abs

@Serializable
data class PuzzleCell(val row: Int, val col: Int)

@Serializable
enum class EdgeOrientation { Horizontal, Vertical }

@Serializable
data class LoopEdge(
    val row: Int,
    val col: Int,
    val orientation: EdgeOrientation,
)

@Serializable
data class SlitherlinkClue(val cell: PuzzleCell, val value: Int)

@Serializable
data class SlitherlinkPuzzle(
    val width: Int,
    val height: Int,
    val clues: List<SlitherlinkClue>,
    val solutionEdges: Set<LoopEdge>,
    val seed: Long,
    val difficulty: Difficulty,
) {
    val puzzleId: String = "slitherlink-$seed-${difficulty.name.lowercase()}"
}

@Serializable
data class SlitherlinkState(val markedEdges: Set<LoopEdge> = emptySet())

object SlitherlinkGenerator {
    fun generate(seed: Long, difficulty: Difficulty): SlitherlinkPuzzle {
        val size = when (difficulty) {
            Difficulty.Beginner -> 3
            Difficulty.Easy -> 4
            Difficulty.Medium -> 5
            Difficulty.Hard -> 6
            Difficulty.Expert -> 7
            Difficulty.Master -> 8
        }
        val solution = perimeterLoop(size, size)
        val clues = buildList {
            for (row in 0 until size) {
                for (col in 0 until size) {
                    val value = edgesAround(PuzzleCell(row, col)).count { it in solution }
                    add(SlitherlinkClue(PuzzleCell(row, col), value))
                }
            }
        }
        return SlitherlinkPuzzle(size, size, clues, solution, abs(seed), difficulty)
    }

    private fun perimeterLoop(width: Int, height: Int): Set<LoopEdge> =
        buildSet {
            for (col in 0 until width) {
                add(LoopEdge(0, col, EdgeOrientation.Horizontal))
                add(LoopEdge(height, col, EdgeOrientation.Horizontal))
            }
            for (row in 0 until height) {
                add(LoopEdge(row, 0, EdgeOrientation.Vertical))
                add(LoopEdge(row, width, EdgeOrientation.Vertical))
            }
        }
}

object SlitherlinkInteractions {
    fun toggleEdge(state: SlitherlinkState, edge: LoopEdge): SlitherlinkState =
        if (edge in state.markedEdges) {
            state.copy(markedEdges = state.markedEdges - edge)
        } else {
            state.copy(markedEdges = state.markedEdges + edge)
        }
}

object SlitherlinkValidator {
    fun isSolved(puzzle: SlitherlinkPuzzle, state: SlitherlinkState): Boolean =
        state.markedEdges == puzzle.solutionEdges && isSingleLoop(state.markedEdges)

    private fun isSingleLoop(edges: Set<LoopEdge>): Boolean {
        if (edges.isEmpty()) return false
        val graph = mutableMapOf<PuzzleCell, MutableSet<PuzzleCell>>()
        for (edge in edges) {
            val (a, b) = endpoints(edge)
            graph.getOrPut(a) { mutableSetOf() } += b
            graph.getOrPut(b) { mutableSetOf() } += a
        }
        if (graph.values.any { it.size != 2 }) return false
        val start = graph.keys.first()
        val seen = mutableSetOf<PuzzleCell>()
        val stack = ArrayDeque<PuzzleCell>()
        stack += start
        while (stack.isNotEmpty()) {
            val cell = stack.removeLast()
            if (!seen.add(cell)) continue
            graph.getValue(cell).forEach { next -> if (next !in seen) stack += next }
        }
        return seen.size == graph.size
    }
}

fun edgesAround(cell: PuzzleCell): Set<LoopEdge> =
    setOf(
        LoopEdge(cell.row, cell.col, EdgeOrientation.Horizontal),
        LoopEdge(cell.row + 1, cell.col, EdgeOrientation.Horizontal),
        LoopEdge(cell.row, cell.col, EdgeOrientation.Vertical),
        LoopEdge(cell.row, cell.col + 1, EdgeOrientation.Vertical),
    )

private fun endpoints(edge: LoopEdge): Pair<PuzzleCell, PuzzleCell> =
    when (edge.orientation) {
        EdgeOrientation.Horizontal -> PuzzleCell(edge.row, edge.col) to PuzzleCell(edge.row, edge.col + 1)
        EdgeOrientation.Vertical -> PuzzleCell(edge.row, edge.col) to PuzzleCell(edge.row + 1, edge.col)
    }

@Serializable
data class NurikabeClue(val cell: PuzzleCell, val value: Int)

@Serializable
data class NurikabePuzzle(
    val width: Int,
    val height: Int,
    val clues: List<NurikabeClue>,
    val solutionShaded: Set<PuzzleCell>,
    val seed: Long,
    val difficulty: Difficulty,
) {
    val puzzleId: String = "nurikabe-$seed-${difficulty.name.lowercase()}"
}

@Serializable
data class NurikabeState(val shadedCells: Set<PuzzleCell> = emptySet())

object NurikabeGenerator {
    fun generate(seed: Long, difficulty: Difficulty): NurikabePuzzle {
        val size = when (difficulty) {
            Difficulty.Beginner, Difficulty.Easy -> 5
            Difficulty.Medium, Difficulty.Hard -> 7
            Difficulty.Expert, Difficulty.Master -> 9
        }
        val clues = buildList {
            for (row in 0 until size step 2) {
                for (col in 0 until size step 2) {
                    add(NurikabeClue(PuzzleCell(row, col), 1))
                }
            }
        }
        val shaded = buildSet {
            for (row in 0 until size) {
                for (col in 0 until size) {
                    if (row % 2 == 1 || col % 2 == 1) add(PuzzleCell(row, col))
                }
            }
        }
        return NurikabePuzzle(size, size, clues, shaded, abs(seed), difficulty)
    }
}

object NurikabeInteractions {
    fun toggleShade(state: NurikabeState, cell: PuzzleCell): NurikabeState =
        if (cell in state.shadedCells) {
            state.copy(shadedCells = state.shadedCells - cell)
        } else {
            state.copy(shadedCells = state.shadedCells + cell)
        }
}

object NurikabeValidator {
    fun isSolved(puzzle: NurikabePuzzle, state: NurikabeState): Boolean =
        state.shadedCells == puzzle.solutionShaded &&
            hasConnectedWall(state.shadedCells) &&
            hasNoTwoByTwoWall(puzzle, state.shadedCells)

    private fun hasConnectedWall(shaded: Set<PuzzleCell>): Boolean {
        if (shaded.isEmpty()) return false
        val seen = mutableSetOf<PuzzleCell>()
        val stack = ArrayDeque<PuzzleCell>()
        stack += shaded.first()
        while (stack.isNotEmpty()) {
            val cell = stack.removeLast()
            if (!seen.add(cell)) continue
            neighbors(cell).forEach { if (it in shaded && it !in seen) stack += it }
        }
        return seen.size == shaded.size
    }

    private fun hasNoTwoByTwoWall(puzzle: NurikabePuzzle, shaded: Set<PuzzleCell>): Boolean {
        for (row in 0 until puzzle.height - 1) {
            for (col in 0 until puzzle.width - 1) {
                val block = setOf(
                    PuzzleCell(row, col),
                    PuzzleCell(row + 1, col),
                    PuzzleCell(row, col + 1),
                    PuzzleCell(row + 1, col + 1),
                )
                if (block.all { it in shaded }) return false
            }
        }
        return true
    }
}

private fun neighbors(cell: PuzzleCell): List<PuzzleCell> =
    listOf(
        PuzzleCell(cell.row - 1, cell.col),
        PuzzleCell(cell.row + 1, cell.col),
        PuzzleCell(cell.row, cell.col - 1),
        PuzzleCell(cell.row, cell.col + 1),
    )

@Serializable
data class KakuroClue(
    val cell: PuzzleCell,
    val rightSum: Int? = null,
    val downSum: Int? = null,
)

@Serializable
data class KakuroPuzzle(
    val width: Int,
    val height: Int,
    val clues: List<KakuroClue>,
    val whiteCells: Set<PuzzleCell>,
    val solutionValues: Map<PuzzleCell, Int>,
    val seed: Long,
    val difficulty: Difficulty,
) {
    val puzzleId: String = "kakuro-$seed-${difficulty.name.lowercase()}"
}

@Serializable
data class KakuroState(val values: Map<PuzzleCell, Int> = emptyMap())

object KakuroGenerator {
    fun generate(seed: Long, difficulty: Difficulty): KakuroPuzzle {
        val solution = mapOf(
            PuzzleCell(1, 1) to 1,
            PuzzleCell(1, 2) to 2,
            PuzzleCell(2, 1) to 3,
            PuzzleCell(2, 2) to 4,
        )
        val clues = listOf(
            KakuroClue(PuzzleCell(0, 1), downSum = 4),
            KakuroClue(PuzzleCell(0, 2), downSum = 6),
            KakuroClue(PuzzleCell(1, 0), rightSum = 3),
            KakuroClue(PuzzleCell(2, 0), rightSum = 7),
        )
        return KakuroPuzzle(
            width = 5,
            height = 5,
            clues = clues,
            whiteCells = solution.keys,
            solutionValues = solution,
            seed = abs(seed),
            difficulty = difficulty,
        )
    }
}

object KakuroInteractions {
    fun setValue(state: KakuroState, cell: PuzzleCell, value: Int): KakuroState {
        require(value in 0..9) { "Kakuro value must be 0..9." }
        return if (value == 0) {
            state.copy(values = state.values - cell)
        } else {
            state.copy(values = state.values + (cell to value))
        }
    }
}

object KakuroValidator {
    fun isSolved(puzzle: KakuroPuzzle, state: KakuroState): Boolean =
        state.values.filterKeys { it in puzzle.whiteCells } == puzzle.solutionValues
}
