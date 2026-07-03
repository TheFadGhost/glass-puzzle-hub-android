package com.thefadghost.glasspuzzlehub.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Backspace
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.InsertChart
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Undo
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
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
        initialValue = 0.08f,
        targetValue = 0.16f,
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
                    listOf(theme.accent.copy(alpha = pulse * 0.65f), Color.Transparent),
                    start = Offset(0f, 0f),
                    end = Offset(size.width * 0.74f, size.height * 0.44f),
                ),
                topLeft = Offset(0f, 0f),
                size = Size(size.width, size.height * 0.42f),
            )
            drawRect(
                brush = Brush.linearGradient(
                    listOf(Color.Transparent, theme.accentAlt.copy(alpha = pulse * 0.55f)),
                    start = Offset(size.width * 0.2f, size.height * 0.48f),
                    end = Offset(size.width, size.height),
                ),
                topLeft = Offset(0f, size.height * 0.48f),
                size = Size(size.width, size.height * 0.52f),
            )
            repeat(7) { index ->
                val y = size.height * (0.12f + index * 0.11f)
                drawLine(
                    color = theme.stroke.copy(alpha = 0.04f + pulse * 0.04f),
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
            .background(theme.panelStrong)
            .border(1.dp, theme.stroke.copy(alpha = 0.72f), RoundedCornerShape(radius))
            .padding(1.dp),
    ) {
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
    Box(
        modifier = modifier
            .size(58.dp)
            .scale(scale)
            .clip(RoundedCornerShape(29.dp))
            .background(if (selected) theme.accent.copy(alpha = 0.20f) else Color.Transparent)
            .border(1.dp, if (selected) theme.accent.copy(alpha = 0.45f) else theme.stroke.copy(alpha = 0.08f), RoundedCornerShape(29.dp))
            .semantics { contentDescription = label }
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        IconCanvas(icon, if (selected) theme.accent else theme.text, Modifier.size(27.dp))
    }
}

@Composable
fun FloatingDock(
    theme: GlassTheme,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .height(74.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(theme.panel.copy(alpha = 0.84f))
            .border(1.dp, theme.stroke.copy(alpha = 0.62f), RoundedCornerShape(percent = 50)),
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
    val imageVector: ImageVector = when (icon) {
        GlassIcon.Home -> Icons.Rounded.Home
        GlassIcon.Grid -> Icons.Rounded.Apps
        GlassIcon.Calendar -> Icons.Rounded.CalendarToday
        GlassIcon.Archive -> Icons.Rounded.Archive
        GlassIcon.Chart -> Icons.Rounded.InsertChart
        GlassIcon.Palette -> Icons.Rounded.Palette
        GlassIcon.Settings -> Icons.Rounded.Settings
        GlassIcon.Back -> Icons.Rounded.ArrowBack
        GlassIcon.Play -> Icons.Rounded.PlayArrow
        GlassIcon.Hint -> Icons.Rounded.Lightbulb
        GlassIcon.Check -> Icons.Rounded.CheckCircle
        GlassIcon.Undo -> Icons.Rounded.Undo
        GlassIcon.Erase -> Icons.Rounded.Backspace
        GlassIcon.Notes -> Icons.Rounded.EditNote
        GlassIcon.Pause -> Icons.Rounded.Pause
    }
    Image(
        painter = rememberVectorPainter(imageVector),
        contentDescription = null,
        colorFilter = ColorFilter.tint(color),
        modifier = modifier,
    )
}
