package com.thefadghost.glasspuzzlehub.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GlassBackground(theme: GlassTheme, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val pulse by rememberInfiniteTransition(label = "ambient").animateFloat(
        initialValue = 0.18f,
        targetValue = 0.34f,
        animationSpec = infiniteRepeatable(tween(3600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ambient-alpha",
    )
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.linearGradient(
                    listOf(theme.accent.copy(alpha = pulse * 0.62f), Color.Transparent),
                    start = Offset(0f, 0f),
                    end = Offset(size.width * 0.74f, size.height * 0.44f),
                ),
                topLeft = Offset(0f, 0f),
                size = Size(size.width, size.height * 0.42f),
            )
            drawRect(
                brush = Brush.linearGradient(
                    listOf(Color.Transparent, theme.accentAlt.copy(alpha = pulse * 0.45f)),
                    start = Offset(size.width * 0.2f, size.height * 0.48f),
                    end = Offset(size.width, size.height),
                ),
                topLeft = Offset(0f, size.height * 0.48f),
                size = Size(size.width, size.height * 0.52f),
            )
            repeat(7) { index ->
                val y = size.height * (0.12f + index * 0.11f)
                drawLine(
                    color = theme.stroke.copy(alpha = 0.08f + pulse * 0.08f),
                    start = Offset(-size.width * 0.15f, y),
                    end = Offset(size.width * 1.1f, y + size.height * 0.18f),
                    strokeWidth = 1.2f,
                )
            }
            drawRect(
                brush = Brush.verticalGradient(listOf(Color.Transparent, theme.backgroundAlt.copy(alpha = 0.72f))),
                size = size,
            )
        }
        Box(Modifier.fillMaxSize().blur(48.dp))
        content()
    }
}

@Composable
fun GlassPanel(
    theme: GlassTheme,
    modifier: Modifier = Modifier,
    radius: Dp = 28.dp,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(radius))
            .background(theme.panel)
            .border(1.dp, theme.stroke, RoundedCornerShape(radius))
            .padding(1.dp),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(radius))
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.10f), Color.Transparent),
                    ),
                ),
        )
        content()
    }
}

@Composable
fun GlassText(
    text: String,
    theme: GlassTheme,
    modifier: Modifier = Modifier,
    size: Int = 16,
    weight: FontWeight = FontWeight.Normal,
    muted: Boolean = false,
    mono: Boolean = false,
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = TextStyle(
            color = if (muted) theme.mutedText else theme.text,
            fontSize = size.sp,
            fontWeight = weight,
            fontFamily = if (mono) FontFamily.Monospace else FontFamily.SansSerif,
            letterSpacing = 0.sp,
            lineHeight = (size * 1.24f).sp,
        ),
    )
}

@Composable
fun GlassIconButton(
    label: String,
    theme: GlassTheme,
    icon: GlassIcon,
    selected: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val scale = if (selected) 1.04f else 1f
    GlassPanel(
        theme = theme,
        radius = 22.dp,
        modifier = modifier
            .size(58.dp)
            .scale(scale)
            .semantics { contentDescription = label }
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            IconCanvas(icon, if (selected) theme.accent else theme.text, Modifier.size(26.dp))
        }
    }
}

@Composable
fun FloatingDock(
    theme: GlassTheme,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    GlassPanel(
        theme = theme,
        radius = 30.dp,
        modifier = modifier.height(76.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

enum class GlassIcon {
    Home,
    Grid,
    Calendar,
    Archive,
    Chart,
    Palette,
    Settings,
    Back,
    Play,
    Hint,
    Check,
    Undo,
    Erase,
    Notes,
    Pause,
}

@Composable
fun IconCanvas(icon: GlassIcon, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val stroke = Stroke(width = size.minDimension * 0.08f, cap = StrokeCap.Round)
        val w = size.width
        val h = size.height
        when (icon) {
            GlassIcon.Home -> {
                val path = Path().apply {
                    moveTo(w * 0.15f, h * 0.48f)
                    lineTo(w * 0.5f, h * 0.18f)
                    lineTo(w * 0.85f, h * 0.48f)
                    lineTo(w * 0.85f, h * 0.85f)
                    lineTo(w * 0.62f, h * 0.85f)
                    lineTo(w * 0.62f, h * 0.62f)
                    lineTo(w * 0.38f, h * 0.62f)
                    lineTo(w * 0.38f, h * 0.85f)
                    lineTo(w * 0.15f, h * 0.85f)
                    close()
                }
                drawPath(path, color, style = stroke)
            }
            GlassIcon.Grid -> repeat(3) { r ->
                repeat(3) { c ->
                    drawRoundRect(
                        color,
                        topLeft = Offset(w * (0.15f + c * 0.25f), h * (0.15f + r * 0.25f)),
                        size = Size(w * 0.16f, h * 0.16f),
                        style = stroke,
                    )
                }
            }
            GlassIcon.Calendar -> {
                drawRoundRect(color, Offset(w * 0.18f, h * 0.22f), Size(w * 0.64f, h * 0.58f), style = stroke)
                drawLine(color, Offset(w * 0.18f, h * 0.38f), Offset(w * 0.82f, h * 0.38f), stroke.width, StrokeCap.Round)
                drawCircle(color, w * 0.045f, Offset(w * 0.38f, h * 0.58f))
            }
            GlassIcon.Archive -> {
                drawRoundRect(color, Offset(w * 0.18f, h * 0.28f), Size(w * 0.64f, h * 0.48f), style = stroke)
                drawLine(color, Offset(w * 0.26f, h * 0.44f), Offset(w * 0.74f, h * 0.44f), stroke.width, StrokeCap.Round)
            }
            GlassIcon.Chart -> {
                drawLine(color, Offset(w * 0.2f, h * 0.8f), Offset(w * 0.2f, h * 0.25f), stroke.width, StrokeCap.Round)
                drawLine(color, Offset(w * 0.2f, h * 0.8f), Offset(w * 0.84f, h * 0.8f), stroke.width, StrokeCap.Round)
                drawLine(color, Offset(w * 0.32f, h * 0.62f), Offset(w * 0.5f, h * 0.46f), stroke.width, StrokeCap.Round)
                drawLine(color, Offset(w * 0.5f, h * 0.46f), Offset(w * 0.72f, h * 0.3f), stroke.width, StrokeCap.Round)
            }
            GlassIcon.Palette -> {
                drawCircle(color, w * 0.31f, Offset(w * 0.5f, h * 0.5f), style = stroke)
                drawCircle(color, w * 0.035f, Offset(w * 0.38f, h * 0.38f))
                drawCircle(color, w * 0.035f, Offset(w * 0.58f, h * 0.36f))
                drawCircle(color, w * 0.035f, Offset(w * 0.64f, h * 0.56f))
            }
            GlassIcon.Settings -> {
                drawCircle(color, w * 0.22f, Offset(w * 0.5f, h * 0.5f), style = stroke)
                repeat(6) {
                    val angle = Math.PI * 2 * it / 6
                    val start = Offset((w * 0.5f + kotlin.math.cos(angle) * w * 0.28f).toFloat(), (h * 0.5f + kotlin.math.sin(angle) * h * 0.28f).toFloat())
                    val end = Offset((w * 0.5f + kotlin.math.cos(angle) * w * 0.38f).toFloat(), (h * 0.5f + kotlin.math.sin(angle) * h * 0.38f).toFloat())
                    drawLine(color, start, end, stroke.width, StrokeCap.Round)
                }
            }
            GlassIcon.Back -> {
                drawLine(color, Offset(w * 0.72f, h * 0.2f), Offset(w * 0.28f, h * 0.5f), stroke.width, StrokeCap.Round)
                drawLine(color, Offset(w * 0.28f, h * 0.5f), Offset(w * 0.72f, h * 0.8f), stroke.width, StrokeCap.Round)
            }
            GlassIcon.Play -> {
                val path = Path().apply {
                    moveTo(w * 0.32f, h * 0.22f)
                    lineTo(w * 0.78f, h * 0.5f)
                    lineTo(w * 0.32f, h * 0.78f)
                    close()
                }
                drawPath(path, color)
            }
            GlassIcon.Hint -> {
                drawCircle(color, w * 0.22f, Offset(w * 0.5f, h * 0.4f), style = stroke)
                drawLine(color, Offset(w * 0.42f, h * 0.68f), Offset(w * 0.58f, h * 0.68f), stroke.width, StrokeCap.Round)
                drawLine(color, Offset(w * 0.46f, h * 0.8f), Offset(w * 0.54f, h * 0.8f), stroke.width, StrokeCap.Round)
            }
            GlassIcon.Check -> {
                drawLine(color, Offset(w * 0.2f, h * 0.54f), Offset(w * 0.42f, h * 0.74f), stroke.width, StrokeCap.Round)
                drawLine(color, Offset(w * 0.42f, h * 0.74f), Offset(w * 0.82f, h * 0.28f), stroke.width, StrokeCap.Round)
            }
            GlassIcon.Undo -> {
                drawLine(color, Offset(w * 0.28f, h * 0.35f), Offset(w * 0.48f, h * 0.18f), stroke.width, StrokeCap.Round)
                drawLine(color, Offset(w * 0.28f, h * 0.35f), Offset(w * 0.48f, h * 0.52f), stroke.width, StrokeCap.Round)
                drawArc(color, 190f, 250f, false, Offset(w * 0.28f, h * 0.22f), Size(w * 0.5f, h * 0.5f), style = stroke)
            }
            GlassIcon.Erase -> drawRoundRect(color, Offset(w * 0.22f, h * 0.36f), Size(w * 0.56f, h * 0.28f), style = stroke)
            GlassIcon.Notes -> {
                drawRoundRect(color, Offset(w * 0.24f, h * 0.18f), Size(w * 0.48f, h * 0.64f), style = stroke)
                drawLine(color, Offset(w * 0.34f, h * 0.36f), Offset(w * 0.62f, h * 0.36f), stroke.width, StrokeCap.Round)
                drawLine(color, Offset(w * 0.34f, h * 0.52f), Offset(w * 0.62f, h * 0.52f), stroke.width, StrokeCap.Round)
            }
            GlassIcon.Pause -> {
                drawLine(color, Offset(w * 0.4f, h * 0.25f), Offset(w * 0.4f, h * 0.75f), stroke.width * 1.4f, StrokeCap.Round)
                drawLine(color, Offset(w * 0.62f, h * 0.25f), Offset(w * 0.62f, h * 0.75f), stroke.width * 1.4f, StrokeCap.Round)
            }
        }
    }
}
