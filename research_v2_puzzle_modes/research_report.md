# V2 Puzzle Modes And High Refresh Research Report

## Sources

- Android frame rate API: https://developer.android.com/media/optimize/performance/frame-rate
- Android high-refresh rendering blog: https://android-developers.googleblog.com/2020/04/high-refresh-rate-rendering-on-android.html
- Nikoli Shikaku rules: https://www.nikoli.co.jp/en/puzzles/shikaku/
- Wired Shikaku blocked-cell variant: https://www.wired.com/2010/12/dr-sudoku-prescribes-a-shot-of-shikaku/
- Conceptis Sudoku variant rules: https://www.conceptispuzzles.com/?uri=puzzle%2Fsudoku%2Frules
- Conceptis Sudoku app variant list: https://play.google.com/store/apps/details?hl=en_US&id=com.conceptispuzzles.sudoku
- Nikoli Slitherlink rules: https://www.nikoli.co.jp/en/puzzles/slitherlink/
- Nikoli Nurikabe rules: https://www.nikoli.co.jp/en/puzzles/nurikabe/
- Nikoli Kakuro rules: https://www.nikoli.co.jp/en/puzzles/kakuro/
- Nikoli puzzle index: https://www.nikoli.co.jp/en/puzzles/

## Findings Applied

1. Android high-refresh support is request-based, not forced.
   - Android exposes frame-rate/display-mode APIs so apps can state their preferred rate.
   - The platform may decline the request due to battery saver, higher-priority surfaces, device policy, or unsupported modes.
   - v2 applies a highest-supported display mode preference and keeps the app correct if Android stays at 60Hz.

2. Shikaku modes can be genuinely playable through grid shape and clue-area rules.
   - Nikoli's Shikaku rule is simple: divide the grid into rectangles where each rectangle contains one number and the number is the cell count.
   - v2 adds square, mini, wide, tall, and large modes by changing generator dimensions.
   - The Wired/Thomas Snyder variant supports shaded cells that are covered by rectangles but do not count toward clue area. v2 implements this as Shadow Blocks.

3. Sudoku variants are real, but each needs its own validator/generator before being playable.
   - Established Sudoku variants include Mini, Mega, Irregular, Diagonal, Killer, OddEven, and Samurai.
   - v2 exposes Classic as playable and lists Mini, Diagonal, Irregular, and Killer as planned roadmap modes so the UI is honest.

4. Slitherlink, Nurikabe, and Kakuro are the best next games.
   - Slitherlink: draw one loop around numbered cells.
   - Nurikabe: shade cells into a continuous wall while preserving clue islands.
   - Kakuro: fill digit runs to match sum clues without repeated digits.
   - All three are established single-player logic puzzles and fit the existing grid/canvas architecture.
