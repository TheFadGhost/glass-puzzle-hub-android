package com.thefadghost.glasspuzzlehub

import android.app.Activity
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thefadghost.glasspuzzlehub.model.Difficulty
import com.thefadghost.glasspuzzlehub.model.GameId
import com.thefadghost.glasspuzzlehub.shikaku.ShikakuCell
import com.thefadghost.glasspuzzlehub.shikaku.ShikakuCompletion
import com.thefadghost.glasspuzzlehub.shikaku.ShikakuGenerator
import com.thefadghost.glasspuzzlehub.shikaku.ShikakuPuzzle
import com.thefadghost.glasspuzzlehub.shikaku.ShikakuRect
import com.thefadghost.glasspuzzlehub.shikaku.ShikakuValidator
import com.thefadghost.glasspuzzlehub.sudoku.SudokuGenerator
import com.thefadghost.glasspuzzlehub.sudoku.SudokuGrid
import com.thefadghost.glasspuzzlehub.sudoku.SudokuPuzzle
import com.thefadghost.glasspuzzlehub.sudoku.SudokuValidator
import com.thefadghost.glasspuzzlehub.storage.HubSettings
import com.thefadghost.glasspuzzlehub.storage.SettingsStore
import com.thefadghost.glasspuzzlehub.ui.FloatingDock
import com.thefadghost.glasspuzzlehub.ui.GlassBackground
import com.thefadghost.glasspuzzlehub.ui.GlassIcon
import com.thefadghost.glasspuzzlehub.ui.GlassIconButton
import com.thefadghost.glasspuzzlehub.ui.GlassPanel
import com.thefadghost.glasspuzzlehub.ui.GlassText
import com.thefadghost.glasspuzzlehub.ui.GlassTheme
import com.thefadghost.glasspuzzlehub.ui.GlassThemes
import java.time.LocalDate
import kotlin.math.floor
import kotlin.math.min
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        setContent { GlassPuzzleHubApp() }
    }
}

private enum class Screen { Home, Games, Daily, Archive, Stats, Themes, Settings, Detail, Play }

@Composable
private fun GlassPuzzleHubApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settingsStore = remember { SettingsStore(context.applicationContext) }
    val settings by settingsStore.settings.collectAsStateWithLifecycle(initialValue = HubSettings())
    val theme = GlassThemes.all.firstOrNull { it.id == settings.themeId } ?: GlassThemes.Solar
    var screen by remember { mutableStateOf(Screen.Games) }
    var activeGame by remember { mutableStateOf(GameId.Shikaku) }
    var difficulty by remember { mutableStateOf(Difficulty.Easy) }
    var shikakuPuzzle by remember { mutableStateOf(sampleShikakuPuzzle()) }
    var sudokuPuzzle by remember { mutableStateOf(sampleSudokuPuzzle()) }
    val shikakuRects = remember { mutableStateListOf<ShikakuRect>() }
    var sudokuGrid by remember { mutableStateOf(sudokuPuzzle.givens) }
    var sudokuSelected by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var sudokuNotes by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("Pick a puzzle and start clean.") }

    fun startGame(game: GameId, nextDifficulty: Difficulty = difficulty, daily: Boolean = false) {
        activeGame = game
        difficulty = nextDifficulty
        val seedBase = if (daily) LocalDate.now().toEpochDay() else System.currentTimeMillis() / 1000L
        if (game == GameId.Shikaku) {
            shikakuPuzzle = ShikakuGenerator.generate(seedBase + nextDifficulty.ordinal * 41, nextDifficulty)
            shikakuRects.clear()
        } else {
            sudokuPuzzle = SudokuGenerator.generate(seedBase + nextDifficulty.ordinal * 71, nextDifficulty)
            sudokuGrid = sudokuPuzzle.givens
            sudokuSelected = null
        }
        message = if (daily) "Daily ${game.displayName} is ready." else "${game.displayName} ${nextDifficulty.label} started."
        screen = Screen.Play
    }

    GlassBackground(theme) {
        Box(
            Modifier
                .fillMaxSize()
                .systemBarsPadding(),
        ) {
            AnimatedContent(
                targetState = screen,
                transitionSpec = { (fadeIn() + scaleIn(initialScale = 0.98f)) togetherWith (fadeOut() + scaleOut(targetScale = 0.98f)) },
                label = "screen",
                modifier = Modifier.fillMaxSize(),
            ) { target ->
                when (target) {
                    Screen.Home -> HomeScreen(theme, message, onOpenGame = {
                        activeGame = it
                        screen = Screen.Detail
                    }, onStart = { startGame(it) })
                    Screen.Games -> GamesScreen(theme, onOpen = {
                        activeGame = it
                        screen = Screen.Detail
                    }, onStart = { startGame(it) }, onDaily = { screen = Screen.Daily }, onThemes = { screen = Screen.Themes }, onSettings = { screen = Screen.Settings })
                    Screen.Daily -> DailyScreen(theme, onStart = { game, level -> startGame(game, level, daily = true) })
                    Screen.Archive -> InfoScreen(theme, "Archive", "Unfinished sessions and completed daily records will collect here as you play.")
                    Screen.Stats -> StatsScreen(theme, shikakuSolved = shikakuRects.size, sudokuFilled = sudokuGrid.values.count { it != 0 })
                    Screen.Themes -> ThemesScreen(theme, onTheme = { next -> scope.launch { settingsStore.setTheme(next.id) } })
                    Screen.Settings -> SettingsScreen(theme, settings, onToggle = { key, value -> scope.launch { settingsStore.setBoolean(key, value) } })
                    Screen.Detail -> GameDetailScreen(theme, activeGame, difficulty, onDifficulty = { difficulty = it }, onBack = { screen = Screen.Games }, onStart = { startGame(activeGame, difficulty) }, onDaily = { startGame(activeGame, difficulty, daily = true) })
                    Screen.Play -> GamePlayScreen(
                        theme = theme,
                        game = activeGame,
                        difficulty = difficulty,
                        message = message,
                        shikakuPuzzle = shikakuPuzzle,
                        shikakuRects = shikakuRects,
                        sudokuPuzzle = sudokuPuzzle,
                        sudokuGrid = sudokuGrid,
                        sudokuSelected = sudokuSelected,
                        sudokuNotes = sudokuNotes,
                        onBack = { screen = Screen.Detail },
                        onMessage = { message = it },
                        onShikakuRects = {
                            shikakuRects.clear()
                            shikakuRects.addAll(it)
                        },
                        onSudokuGrid = { sudokuGrid = it },
                        onSudokuSelected = { sudokuSelected = it },
                        onSudokuNotes = { sudokuNotes = it },
                    )
                }
            }

            if (screen != Screen.Play) {
                FloatingDock(
                    theme = theme,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .fillMaxWidth(),
                ) {
                    DockButton("Games", GlassIcon.Grid, screen == Screen.Games, theme) { screen = Screen.Games }
                    DockButton("Daily", GlassIcon.Calendar, screen == Screen.Daily, theme) { screen = Screen.Daily }
                    DockButton("Themes", GlassIcon.Palette, screen == Screen.Themes, theme) { screen = Screen.Themes }
                    DockButton("Settings", GlassIcon.Settings, screen == Screen.Settings, theme) { screen = Screen.Settings }
                }
            }
        }
    }
}

@Composable
private fun DockButton(label: String, icon: GlassIcon, selected: Boolean, theme: GlassTheme, onClick: () -> Unit) {
    GlassIconButton(label, theme, icon, selected = selected, onClick = onClick)
}

@Composable
private fun HomeScreen(theme: GlassTheme, message: String, onOpenGame: (GameId) -> Unit, onStart: (GameId) -> Unit) {
    ScreenColumn {
        Header(theme, "Glass Puzzle Hub", "Two logic games, one custom glass playground.")
        GlassPanel(theme, Modifier.fillMaxWidth()) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                GlassText("Continue", theme, size = 15, weight = FontWeight.SemiBold, muted = true)
                GlassText(message, theme, size = 28, weight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionChip("Play Shikaku", theme) { onStart(GameId.Shikaku) }
                    ActionChip("Play Sudoku", theme) { onStart(GameId.Sudoku) }
                }
            }
        }
        GlassText("Game rail", theme, size = 14, weight = FontWeight.SemiBold, muted = true)
        val games: List<GameId> = listOf(GameId.Shikaku, GameId.Sudoku)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(end = 24.dp)) {
            items(games.size) { index ->
                val game = games[index]
                GameCard(theme, game, onOpen = { onOpenGame(game) }, onStart = { onStart(game) })
            }
        }
        StatsStrip(theme)
        Spacer(Modifier.height(92.dp))
    }
}

@Composable
private fun GamesScreen(
    theme: GlassTheme,
    onOpen: (GameId) -> Unit,
    onStart: (GameId) -> Unit,
    onDaily: () -> Unit,
    onThemes: () -> Unit,
    onSettings: () -> Unit,
) {
    ScreenColumn {
        Header(theme, "Games", "Pick one of the two logic puzzles, then tune difficulty and themes.")
        PrimaryGameOption(
            theme = theme,
            game = GameId.Shikaku,
            body = "Area-clue rectangles with a real solved popup.",
            onOpen = { onOpen(GameId.Shikaku) },
            onStart = { onStart(GameId.Shikaku) },
        )
        PrimaryGameOption(
            theme = theme,
            game = GameId.Sudoku,
            body = "9x9 logic with keypad, notes, hints, and checks.",
            onOpen = { onOpen(GameId.Sudoku) },
            onStart = { onStart(GameId.Sudoku) },
        )
        GlassText("Extras", theme, size = 14, weight = FontWeight.SemiBold, muted = true)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ExtraShortcut(theme, "Daily", "Date seeds", GlassIcon.Calendar, Modifier.weight(1f), onDaily)
            ExtraShortcut(theme, "Themes", "Color sets", GlassIcon.Palette, Modifier.weight(1f), onThemes)
        }
        ExtraShortcut(theme, "Settings", "Motion, haptics, contrast, sound", GlassIcon.Settings, Modifier.fillMaxWidth(), onSettings)
        Spacer(Modifier.height(92.dp))
    }
}

@Composable
private fun DailyScreen(theme: GlassTheme, onStart: (GameId, Difficulty) -> Unit) {
    ScreenColumn {
        Header(theme, "Daily", LocalDate.now().toString())
        val games: List<GameId> = listOf(GameId.Shikaku, GameId.Sudoku)
        games.forEach { game: GameId ->
            GlassPanel(theme, Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    GlassText(game.displayName, theme, size = 24, weight = FontWeight.Bold)
                    GlassText("One deterministic puzzle per difficulty for today.", theme, muted = true)
                    val levels: List<Difficulty> = Difficulty.values().toList()
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(levels.size) { index ->
                            val difficulty = levels[index]
                            ActionChip(difficulty.label, theme) { onStart(game, difficulty) }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(92.dp))
    }
}

@Composable
private fun GameDetailScreen(
    theme: GlassTheme,
    game: GameId,
    difficulty: Difficulty,
    onDifficulty: (Difficulty) -> Unit,
    onBack: () -> Unit,
    onStart: () -> Unit,
    onDaily: () -> Unit,
) {
    ScreenColumn {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GlassIconButton("Back", theme, GlassIcon.Back, onClick = onBack)
            Header(theme, game.displayName, if (game == GameId.Shikaku) "Divide the board into exact clue rectangles." else "Fill every row, column, and box with 1 through 9.")
        }
        PreviewBoard(theme, game, Modifier.fillMaxWidth().height(170.dp))
        GlassPanel(theme, Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                GlassText("Difficulty", theme, size = 16, weight = FontWeight.SemiBold, muted = true)
                val levels: List<Difficulty> = Difficulty.values().toList()
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(levels.size) { index ->
                        val item = levels[index]
                        ActionChip(item.label, theme, selected = item == difficulty) { onDifficulty(item) }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionChip("Quick play", theme, icon = GlassIcon.Play, onClick = onStart)
                    ActionChip("Daily", theme, icon = GlassIcon.Calendar, onClick = onDaily)
                }
            }
        }
    }
}

@Composable
private fun GamePlayScreen(
    theme: GlassTheme,
    game: GameId,
    difficulty: Difficulty,
    message: String,
    shikakuPuzzle: ShikakuPuzzle,
    shikakuRects: List<ShikakuRect>,
    sudokuPuzzle: SudokuPuzzle,
    sudokuGrid: SudokuGrid,
    sudokuSelected: Pair<Int, Int>?,
    sudokuNotes: Boolean,
    onBack: () -> Unit,
    onMessage: (String) -> Unit,
    onShikakuRects: (List<ShikakuRect>) -> Unit,
    onSudokuGrid: (SudokuGrid) -> Unit,
    onSudokuSelected: (Pair<Int, Int>?) -> Unit,
    onSudokuNotes: (Boolean) -> Unit,
) {
    var showShikakuComplete by remember(game, shikakuPuzzle.puzzleId) { mutableStateOf(false) }

    fun updateShikakuRects(next: List<ShikakuRect>, messageWhenOpen: String) {
        onShikakuRects(next)
        if (ShikakuCompletion.isComplete(shikakuPuzzle, next)) {
            showShikakuComplete = true
            onMessage("Completed: every Shikaku rectangle is correct.")
        } else {
            showShikakuComplete = false
            onMessage(messageWhenOpen)
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            TopGameBar(theme, game, difficulty, onBack)
            GlassText(message, theme, muted = true, mono = true)
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                if (game == GameId.Shikaku) {
                    ShikakuBoard(theme, shikakuPuzzle, shikakuRects, onRects = { updateShikakuRects(it, "Rectangle placed.") }, onMessage = onMessage)
                } else {
                    SudokuBoard(theme, sudokuPuzzle, sudokuGrid, sudokuSelected, onSelect = onSudokuSelected)
                }
            }
            Spacer(Modifier.height(84.dp))
        }

        FloatingDock(
            theme = theme,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .fillMaxWidth(),
        ) {
            if (game == GameId.Shikaku) {
                DockButton("Undo", GlassIcon.Undo, false, theme) {
                    updateShikakuRects(shikakuRects.dropLast(1), "Last rectangle removed.")
                }
                DockButton("Erase", GlassIcon.Erase, false, theme) {
                    updateShikakuRects(emptyList(), "Board cleared.")
                }
                DockButton("Hint", GlassIcon.Hint, false, theme) {
                    val next = shikakuPuzzle.solution.firstOrNull { solution -> shikakuRects.none { it == solution } }
                    if (next != null) {
                        updateShikakuRects(shikakuRects + next, "Hint placed one exact rectangle.")
                    } else {
                        onMessage("No hint needed; every solution rectangle is already placed.")
                    }
                }
                DockButton("Check", GlassIcon.Check, false, theme) {
                    val result = ShikakuValidator.validate(shikakuPuzzle, shikakuRects)
                    if (result.isValid) {
                        showShikakuComplete = true
                        onMessage("Completed: every Shikaku rectangle is correct.")
                    } else {
                        onMessage(result.message ?: "Not solved yet.")
                    }
                }
            } else {
                DockButton("Notes", GlassIcon.Notes, sudokuNotes, theme) {
                    onSudokuNotes(!sudokuNotes)
                    onMessage(if (!sudokuNotes) "Notes mode enabled." else "Value mode enabled.")
                }
                DockButton("Erase", GlassIcon.Erase, false, theme) {
                    val cell = sudokuSelected
                    if (cell != null) onSudokuGrid(sudokuGrid.withValue(cell.first, cell.second, 0))
                }
                DockButton("Hint", GlassIcon.Hint, false, theme) {
                    val index = sudokuGrid.values.indexOfFirst { it == 0 }
                    if (index >= 0) {
                        onSudokuGrid(sudokuGrid.withValue(index / 9, index % 9, sudokuPuzzle.solution.values[index]))
                        onMessage("Hint filled one cell.")
                    }
                }
                DockButton("Check", GlassIcon.Check, false, theme) {
                    onMessage(if (SudokuValidator.isSolved(sudokuGrid)) "Sudoku solved cleanly." else "Keep going; some cells are unfinished or conflicting.")
                }
            }
            DockButton("Pause", GlassIcon.Pause, false, theme) { onMessage("Paused without leaving the board.") }
        }

        if (game == GameId.Sudoku) {
            SudokuKeypad(
                theme = theme,
                selected = sudokuSelected,
                notes = sudokuNotes,
                grid = sudokuGrid,
                puzzle = sudokuPuzzle,
                onGrid = onSudokuGrid,
                onMessage = onMessage,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp, start = 18.dp, end = 18.dp),
            )
        }

        CompletionPopup(
            visible = game == GameId.Shikaku && showShikakuComplete,
            theme = theme,
            title = "Shikaku completed",
            body = "${shikakuPuzzle.width}x${shikakuPuzzle.height} board solved with ${shikakuRects.size} exact rectangles.",
            onKeep = { showShikakuComplete = false },
            onClear = { updateShikakuRects(emptyList(), "Board cleared.") },
        )
    }
}

@Composable
private fun CompletionPopup(
    visible: Boolean,
    theme: GlassTheme,
    title: String,
    body: String,
    onKeep: () -> Unit,
    onClear: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(initialScale = 0.96f),
        exit = fadeOut() + scaleOut(targetScale = 0.96f),
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.34f))
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            GlassPanel(theme, Modifier.fillMaxWidth(), radius = 30.dp) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(
                        Modifier
                            .size(58.dp)
                            .clip(RoundedCornerShape(29.dp))
                            .background(theme.success.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        com.thefadghost.glasspuzzlehub.ui.IconCanvas(GlassIcon.Check, theme.success, Modifier.size(30.dp))
                    }
                    GlassText(title, theme, size = 30, weight = FontWeight.Bold)
                    GlassText(body, theme, muted = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ActionChip("Continue", theme, selected = true, icon = GlassIcon.Check, onClick = onKeep)
                        ActionChip("Clear board", theme, icon = GlassIcon.Erase, onClick = onClear)
                    }
                }
            }
        }
    }
}

@Composable
private fun TopGameBar(theme: GlassTheme, game: GameId, difficulty: Difficulty, onBack: () -> Unit) {
    GlassPanel(theme, Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            GlassIconButton("Back", theme, GlassIcon.Back, onClick = onBack)
            Column(Modifier.weight(1f)) {
                GlassText(game.displayName, theme, size = 22, weight = FontWeight.Bold)
                GlassText("${difficulty.label} / 00:00", theme, size = 13, muted = true, mono = true)
            }
            GlassIconButton("Settings", theme, GlassIcon.Settings, onClick = {})
        }
    }
}

@Composable
private fun ShikakuBoard(
    theme: GlassTheme,
    puzzle: ShikakuPuzzle,
    rects: List<ShikakuRect>,
    onRects: (List<ShikakuRect>) -> Unit,
    onMessage: (String) -> Unit,
) {
    var boardSize by remember { mutableStateOf(IntSize.Zero) }
    var start by remember { mutableStateOf<ShikakuCell?>(null) }
    var preview by remember { mutableStateOf<ShikakuRect?>(null) }

    Canvas(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .semantics { contentDescription = "Shikaku board" }
            .onSizeChanged { boardSize = it }
            .pointerInput(puzzle, rects) {
                detectDragGestures(
                    onDragStart = { offset ->
                        start = cellAt(offset, boardSize, puzzle.width, puzzle.height)
                        preview = null
                    },
                    onDrag = { change, _ ->
                        val from = start
                        val to = cellAt(change.position, boardSize, puzzle.width, puzzle.height)
                        if (from != null && to != null) preview = rectBetween(from, to)
                    },
                    onDragEnd = {
                        val candidate = preview
                        if (candidate != null) {
                            val overlaps = rects.any { it.overlaps(candidate) }
                            val cluesInside = puzzle.clues.count { candidate.contains(it.cell) }
                            if (!overlaps && cluesInside <= 1) {
                                onRects(rects + candidate)
                            } else {
                                onMessage("That rectangle overlaps or captures too many clues.")
                            }
                        }
                        start = null
                        preview = null
                    },
                )
            },
    ) {
        val cell = size.minDimension / puzzle.width
        drawRoundRect(theme.panelStrong, size = Size(cell * puzzle.width, cell * puzzle.height), cornerRadius = CornerRadius(24f, 24f))
        for (row in 0 until puzzle.height) {
            for (col in 0 until puzzle.width) {
                drawRoundRect(
                    color = theme.background.copy(alpha = 0.42f),
                    topLeft = Offset(col * cell + 3f, row * cell + 3f),
                    size = Size(cell - 6f, cell - 6f),
                    cornerRadius = CornerRadius(12f, 12f),
                )
            }
        }
        rects.forEachIndexed { index, rect ->
            val fill = if (index % 2 == 0) theme.accent.copy(alpha = 0.42f) else theme.accentAlt.copy(alpha = 0.38f)
            drawRoundRect(fill, Offset(rect.left * cell + 4f, rect.top * cell + 4f), Size(rect.width * cell - 8f, rect.height * cell - 8f), CornerRadius(16f, 16f))
            drawRoundRect(theme.text.copy(alpha = 0.55f), Offset(rect.left * cell + 4f, rect.top * cell + 4f), Size(rect.width * cell - 8f, rect.height * cell - 8f), CornerRadius(16f, 16f), style = Stroke(2.2f))
        }
        preview?.let { rect ->
            val valid = !rects.any { it.overlaps(rect) } && puzzle.clues.count { rect.contains(it.cell) } <= 1
            drawRoundRect(
                if (valid) theme.success.copy(alpha = 0.30f) else theme.danger.copy(alpha = 0.35f),
                Offset(rect.left * cell + 4f, rect.top * cell + 4f),
                Size(rect.width * cell - 8f, rect.height * cell - 8f),
                CornerRadius(16f, 16f),
            )
            drawRoundRect(
                if (valid) theme.success else theme.danger,
                Offset(rect.left * cell + 4f, rect.top * cell + 4f),
                Size(rect.width * cell - 8f, rect.height * cell - 8f),
                CornerRadius(16f, 16f),
                style = Stroke(3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))),
            )
        }
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = cell * 0.34f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
        puzzle.clues.forEach {
            paint.color = theme.text.toArgbInt()
            drawContext.canvas.nativeCanvas.drawText(it.value.toString(), (it.cell.col + 0.5f) * cell, (it.cell.row + 0.62f) * cell, paint)
        }
    }
}

@Composable
private fun SudokuBoard(
    theme: GlassTheme,
    puzzle: SudokuPuzzle,
    grid: SudokuGrid,
    selected: Pair<Int, Int>?,
    onSelect: (Pair<Int, Int>) -> Unit,
) {
    var boardSize by remember { mutableStateOf(IntSize.Zero) }
    Canvas(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .semantics { contentDescription = "Sudoku board" }
            .onSizeChanged { boardSize = it }
            .pointerInput(grid) {
                detectTapGestures { offset ->
                    cellAt(offset, boardSize, 9, 9)?.let { onSelect(it.row to it.col) }
                }
            },
    ) {
        val cell = size.minDimension / 9f
        drawRoundRect(theme.panelStrong, size = Size(cell * 9, cell * 9), cornerRadius = CornerRadius(24f, 24f))
        selected?.let { (row, col) ->
            drawRoundRect(theme.accent.copy(alpha = 0.16f), Offset(0f, row * cell), Size(cell * 9, cell), CornerRadius(8f, 8f))
            drawRoundRect(theme.accent.copy(alpha = 0.16f), Offset(col * cell, 0f), Size(cell, cell * 9), CornerRadius(8f, 8f))
            drawRoundRect(theme.accentAlt.copy(alpha = 0.22f), Offset(col / 3 * 3 * cell, row / 3 * 3 * cell), Size(cell * 3, cell * 3), CornerRadius(12f, 12f))
        }
        for (row in 0..8) {
            for (col in 0..8) {
                drawRoundRect(
                    theme.background.copy(alpha = 0.28f),
                    Offset(col * cell + 2f, row * cell + 2f),
                    Size(cell - 4f, cell - 4f),
                    CornerRadius(8f, 8f),
                )
            }
        }
        for (line in 0..9) {
            val thick = if (line % 3 == 0) 4f else 1.4f
            drawLine(theme.stroke.copy(alpha = 0.8f), Offset(line * cell, 0f), Offset(line * cell, 9 * cell), thick, StrokeCap.Round)
            drawLine(theme.stroke.copy(alpha = 0.8f), Offset(0f, line * cell), Offset(9 * cell, line * cell), thick, StrokeCap.Round)
        }
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }
        for (row in 0..8) {
            for (col in 0..8) {
                val value = grid.valueAt(row, col)
                if (value != 0) {
                    val index = row * 9 + col
                    val conflict = SudokuValidator.conflicts(grid, row, col, value)
                    paint.color = when {
                        conflict -> theme.danger.toArgbInt()
                        puzzle.givens.givens[index] -> theme.text.toArgbInt()
                        else -> theme.accent.toArgbInt()
                    }
                    paint.textSize = cell * 0.46f
                    drawContext.canvas.nativeCanvas.drawText(value.toString(), (col + 0.5f) * cell, (row + 0.66f) * cell, paint)
                } else {
                    val notes = grid.notesAt(row, col)
                    if (notes.isNotEmpty()) {
                        paint.color = theme.mutedText.toArgbInt()
                        paint.textSize = cell * 0.16f
                        notes.forEach { note ->
                            val nr = (note - 1) / 3
                            val nc = (note - 1) % 3
                            drawContext.canvas.nativeCanvas.drawText(note.toString(), col * cell + (nc + 0.5f) * cell / 3f, row * cell + (nr + 0.68f) * cell / 3f, paint)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SudokuKeypad(
    theme: GlassTheme,
    selected: Pair<Int, Int>?,
    notes: Boolean,
    grid: SudokuGrid,
    puzzle: SudokuPuzzle,
    onGrid: (SudokuGrid) -> Unit,
    onMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(visible = selected != null, enter = fadeIn() + scaleIn(initialScale = 0.96f), exit = fadeOut() + scaleOut(targetScale = 0.96f), modifier = modifier) {
        GlassPanel(theme, Modifier.fillMaxWidth(), radius = 26.dp) {
            Row(Modifier.padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                (1..9).forEach { digit ->
                    ActionChip(digit.toString(), theme, compact = true) {
                        val cell = selected ?: return@ActionChip
                        val index = cell.first * 9 + cell.second
                        if (puzzle.givens.givens[index]) {
                            onMessage("Given cells are locked.")
                        } else if (notes) {
                            onGrid(grid.withNote(cell.first, cell.second, digit))
                            onMessage("Note $digit added.")
                        } else {
                            onGrid(grid.withValue(cell.first, cell.second, digit))
                            onMessage("Digit $digit placed.")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemesScreen(theme: GlassTheme, onTheme: (GlassTheme) -> Unit) {
    ScreenColumn {
        Header(theme, "Themes", "Every game inherits the same polished glass tokens.")
        GlassThemes.all.forEach { item: GlassTheme ->
            val selected = item.id == theme.id
            GlassPanel(item, Modifier.fillMaxWidth()) {
                Row(
                    Modifier
                        .clickable(remember { MutableInteractionSource() }, indication = null) { onTheme(item) }
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Box(Modifier.size(54.dp).clip(CircleShape).background(item.accent))
                    Column(Modifier.weight(1f)) {
                        GlassText(item.name, item, size = 22, weight = FontWeight.Bold)
                        GlassText(if (selected) "Active theme" else "Tap to apply", item, muted = true)
                    }
                    PreviewBoard(item, GameId.Shikaku, Modifier.size(90.dp))
                }
            }
        }
        Spacer(Modifier.height(92.dp))
    }
}

@Composable
private fun SettingsScreen(theme: GlassTheme, settings: HubSettings, onToggle: (String, Boolean) -> Unit) {
    ScreenColumn {
        Header(theme, "Settings", "Custom controls without platform switches.")
        val rows: List<Pair<String, Boolean>> = listOf(
            "haptics" to settings.haptics,
            "sound" to settings.sound,
            "reduced_motion" to settings.reducedMotion,
            "high_contrast" to settings.highContrast,
        )
        rows.forEach { (key, enabled) ->
            val label = key.split("_").joinToString(" ") { part -> part.replaceFirstChar { it.uppercase() } }
            GlassPanel(theme, Modifier.fillMaxWidth()) {
                Row(
                    Modifier
                        .clickable(remember { MutableInteractionSource() }, indication = null) { onToggle(key, !enabled) }
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        GlassText(label, theme, size = 20, weight = FontWeight.SemiBold)
                        GlassText(if (enabled) "Enabled" else "Disabled", theme, muted = true, mono = true)
                    }
                    CustomToggle(theme, enabled)
                }
            }
        }
        Spacer(Modifier.height(92.dp))
    }
}

@Composable
private fun StatsScreen(theme: GlassTheme, shikakuSolved: Int, sudokuFilled: Int) {
    ScreenColumn {
        Header(theme, "Stats", "Per-game counters, streaks, and best times are ready for persistence.")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatPill(theme, "Shikaku rects", shikakuSolved.toString(), Modifier.weight(1f))
            StatPill(theme, "Sudoku cells", sudokuFilled.toString(), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatPill(theme, "Daily streak", "0", Modifier.weight(1f))
            StatPill(theme, "No-hint solves", "0", Modifier.weight(1f))
        }
        Spacer(Modifier.height(92.dp))
    }
}

@Composable
private fun InfoScreen(theme: GlassTheme, title: String, body: String) {
    ScreenColumn {
        Header(theme, title, body)
        GlassPanel(theme, Modifier.fillMaxWidth()) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GlassText("V1 storage hooks are in place", theme, size = 22, weight = FontWeight.Bold)
                GlassText("Room tables exist for sessions, daily records, achievements, stats, and generated puzzle cache.", theme, muted = true)
            }
        }
    }
}

@Composable
private fun ScreenColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        content = content,
    )
}

@Composable
private fun Header(theme: GlassTheme, title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        GlassText(title, theme, size = 34, weight = FontWeight.Bold)
        GlassText(subtitle, theme, size = 15, muted = true)
    }
}

@Composable
private fun GameCard(theme: GlassTheme, game: GameId, onOpen: () -> Unit, onStart: () -> Unit) {
    GlassPanel(theme, Modifier.width(280.dp).height(230.dp)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            PreviewBoard(theme, game, Modifier.fillMaxWidth().weight(1f))
            GlassText(game.displayName, theme, size = 22, weight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionChip("Open", theme, onClick = onOpen)
                ActionChip("Play", theme, icon = GlassIcon.Play, onClick = onStart)
            }
        }
    }
}

@Composable
private fun PrimaryGameOption(
    theme: GlassTheme,
    game: GameId,
    body: String,
    onOpen: () -> Unit,
    onStart: () -> Unit,
) {
    GlassPanel(theme, Modifier.fillMaxWidth(), radius = 28.dp) {
        Row(
            Modifier
                .clickable(remember { MutableInteractionSource() }, indication = null, onClick = onOpen)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            PreviewBoard(theme, game, Modifier.size(112.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                GlassText(game.displayName, theme, size = 25, weight = FontWeight.Bold)
                GlassText(body, theme, size = 14, muted = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionChip("Options", theme, compact = true, onClick = onOpen)
                    ActionChip("Play", theme, compact = true, icon = GlassIcon.Play, onClick = onStart)
                }
            }
        }
    }
}

@Composable
private fun ExtraShortcut(
    theme: GlassTheme,
    title: String,
    subtitle: String,
    icon: GlassIcon,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    GlassPanel(theme, modifier, radius = 24.dp) {
        Row(
            Modifier
                .clickable(remember { MutableInteractionSource() }, indication = null, onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(21.dp))
                    .background(theme.accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                com.thefadghost.glasspuzzlehub.ui.IconCanvas(icon, theme.accent, Modifier.size(22.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                GlassText(title, theme, size = 18, weight = FontWeight.SemiBold)
                GlassText(subtitle, theme, size = 12, muted = true)
            }
        }
    }
}

@Composable
private fun GameWideRow(theme: GlassTheme, game: GameId, body: String, onOpen: (GameId) -> Unit) {
    GlassPanel(theme, Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .clickable(remember { MutableInteractionSource() }, indication = null) { onOpen(game) }
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PreviewBoard(theme, game, Modifier.size(104.dp))
            Column(Modifier.weight(1f)) {
                GlassText(game.displayName, theme, size = 24, weight = FontWeight.Bold)
                GlassText(body, theme, muted = true)
            }
        }
    }
}

@Composable
private fun StatsStrip(theme: GlassTheme) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatPill(theme, "Streak", "0", Modifier.weight(1f))
        StatPill(theme, "Solves", "0", Modifier.weight(1f))
        StatPill(theme, "Best", "--:--", Modifier.weight(1f))
    }
}

@Composable
private fun StatPill(theme: GlassTheme, label: String, value: String, modifier: Modifier = Modifier) {
    GlassPanel(theme, modifier) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            GlassText(label, theme, size = 12, muted = true, weight = FontWeight.SemiBold)
            GlassText(value, theme, size = 26, weight = FontWeight.Bold, mono = true)
        }
    }
}

@Composable
private fun ActionChip(
    label: String,
    theme: GlassTheme,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    compact: Boolean = false,
    icon: GlassIcon? = null,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(if (selected) 1.04f else 1f, spring(stiffness = 380f, dampingRatio = 0.78f), label = "chip-scale")
    GlassPanel(
        theme = theme,
        radius = if (compact) 18.dp else 22.dp,
        modifier = modifier
            .scale(scale)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
    ) {
        Row(
            Modifier.padding(horizontal = if (compact) 12.dp else 16.dp, vertical = if (compact) 10.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (icon != null) com.thefadghost.glasspuzzlehub.ui.IconCanvas(icon, if (selected) theme.accent else theme.text, Modifier.size(18.dp))
            BasicText(
                label,
                style = TextStyle(
                    color = if (selected) theme.accent else theme.text,
                    fontSize = if (compact) 16.sp else 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.SansSerif,
                ),
            )
        }
    }
}

@Composable
private fun PreviewBoard(theme: GlassTheme, game: GameId, modifier: Modifier = Modifier) {
    Canvas(modifier.clip(RoundedCornerShape(24.dp))) {
        val n = if (game == GameId.Shikaku) 5 else 9
        val cell = min(size.width, size.height) / n
        val left = (size.width - cell * n) / 2f
        val top = (size.height - cell * n) / 2f
        drawRoundRect(theme.panelStrong, Offset(left, top), Size(cell * n, cell * n), CornerRadius(20f, 20f))
        for (r in 0 until n) {
            for (c in 0 until n) {
                drawRoundRect(theme.background.copy(alpha = 0.32f), Offset(left + c * cell + 2f, top + r * cell + 2f), Size(cell - 4f, cell - 4f), CornerRadius(8f, 8f))
            }
        }
        if (game == GameId.Shikaku) {
            drawRoundRect(theme.accent.copy(alpha = 0.48f), Offset(left + cell * 0, top + cell * 0), Size(cell, cell * 5), CornerRadius(10f, 10f))
            drawRoundRect(theme.accentAlt.copy(alpha = 0.42f), Offset(left + cell * 2, top + cell * 0), Size(cell * 3, cell * 2), CornerRadius(10f, 10f))
        } else {
            for (line in 0..9) {
                val thick = if (line % 3 == 0) 3f else 1f
                drawLine(theme.stroke, Offset(left + line * cell, top), Offset(left + line * cell, top + cell * 9), thick)
                drawLine(theme.stroke, Offset(left, top + line * cell), Offset(left + cell * 9, top + line * cell), thick)
            }
        }
    }
}

@Composable
private fun CustomToggle(theme: GlassTheme, enabled: Boolean) {
    val x by animateFloatAsState(if (enabled) 1f else 0f, spring(stiffness = 420f, dampingRatio = 0.8f), label = "toggle")
    Canvas(Modifier.size(width = 64.dp, height = 36.dp)) {
        drawRoundRect(if (enabled) theme.accent.copy(alpha = 0.72f) else theme.panelStrong, size = size, cornerRadius = CornerRadius(size.height / 2, size.height / 2))
        drawCircle(theme.text, radius = size.height * 0.34f, center = Offset(size.height * 0.5f + x * (size.width - size.height), size.height * 0.5f))
    }
}

private fun cellAt(offset: Offset, size: IntSize, width: Int, height: Int): ShikakuCell? {
    if (size.width == 0 || size.height == 0) return null
    val board = min(size.width, size.height).toFloat()
    val cellW = board / width
    val cellH = board / height
    val col = floor(offset.x / cellW).toInt()
    val row = floor(offset.y / cellH).toInt()
    return if (row in 0 until height && col in 0 until width) ShikakuCell(row, col) else null
}

private fun rectBetween(a: ShikakuCell, b: ShikakuCell): ShikakuRect =
    ShikakuRect(
        top = minOf(a.row, b.row),
        left = minOf(a.col, b.col),
        bottom = maxOf(a.row, b.row),
        right = maxOf(a.col, b.col),
    )

private fun Color.toArgbInt(): Int {
    val a = (alpha * 255).toInt().coerceIn(0, 255)
    val r = (red * 255).toInt().coerceIn(0, 255)
    val g = (green * 255).toInt().coerceIn(0, 255)
    val b = (blue * 255).toInt().coerceIn(0, 255)
    return android.graphics.Color.argb(a, r, g, b)
}

private fun sampleShikakuPuzzle(): ShikakuPuzzle {
    val solution = listOf(
        ShikakuRect(0, 0, 4, 0),
        ShikakuRect(0, 1, 0, 4),
        ShikakuRect(1, 1, 4, 1),
        ShikakuRect(1, 2, 2, 4),
        ShikakuRect(3, 2, 4, 4),
    )
    return ShikakuPuzzle(
        width = 5,
        height = 5,
        clues = solution.map { rect -> com.thefadghost.glasspuzzlehub.shikaku.ShikakuClue(ShikakuCell(rect.top, rect.left), rect.area) },
        solution = solution,
        seed = 0L,
        difficulty = Difficulty.Easy,
    )
}

private fun sampleSudokuPuzzle(): SudokuPuzzle {
    val solution = SudokuGrid.fromRows(
        "534678912",
        "672195348",
        "198342567",
        "859761423",
        "426853791",
        "713924856",
        "961537284",
        "287419635",
        "345286179",
        givens = true,
    )
    val givens = SudokuGrid.fromRows(
        "530070000",
        "600195000",
        "098000060",
        "800060003",
        "400803001",
        "700020006",
        "060000280",
        "000419005",
        "000080079",
        givens = true,
    )
    return SudokuPuzzle(givens = givens, solution = solution, seed = 0L, difficulty = Difficulty.Easy)
}
