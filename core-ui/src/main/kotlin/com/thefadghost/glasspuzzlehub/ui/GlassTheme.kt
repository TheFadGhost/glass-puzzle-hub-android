package com.thefadghost.glasspuzzlehub.ui

import androidx.compose.ui.graphics.Color

data class GlassTheme(
    val id: String,
    val name: String,
    val background: Color,
    val backgroundAlt: Color,
    val panel: Color,
    val panelStrong: Color,
    val stroke: Color,
    val text: Color,
    val mutedText: Color,
    val accent: Color,
    val accentAlt: Color,
    val danger: Color,
    val success: Color,
)

object GlassThemes {
    val Noir = GlassTheme(
        id = "noir",
        name = "Noir Glass",
        background = Color(0xFF111317),
        backgroundAlt = Color(0xFF1B1E24),
        panel = Color(0xA620232B),
        panelStrong = Color(0xFF242832),
        stroke = Color(0x3DFFFFFF),
        text = Color(0xFFF8F4EA),
        mutedText = Color(0xFFB4B8C0),
        accent = Color(0xFFE7D767),
        accentAlt = Color(0xFFB9A7F2),
        danger = Color(0xFFFF8E7E),
        success = Color(0xFF7AD7B4),
    )

    val Frost = GlassTheme(
        id = "frost",
        name = "Frost Glass",
        background = Color(0xFFF4F8FA),
        backgroundAlt = Color(0xFFE2ECF0),
        panel = Color(0xCCFFFFFF),
        panelStrong = Color(0xFFFFFFFF),
        stroke = Color(0x6686A1AE),
        text = Color(0xFF14212B),
        mutedText = Color(0xFF5F7180),
        accent = Color(0xFF347D9E),
        accentAlt = Color(0xFF66A77D),
        danger = Color(0xFFC55B54),
        success = Color(0xFF3D936E),
    )

    val Aurora = GlassTheme(
        id = "aurora",
        name = "Aurora Glass",
        background = Color(0xFF0F1B19),
        backgroundAlt = Color(0xFF172A26),
        panel = Color(0xA61D3A35),
        panelStrong = Color(0xFF203E39),
        stroke = Color(0x4D8FD8C2),
        text = Color(0xFFF2FBF5),
        mutedText = Color(0xFFA9C2B8),
        accent = Color(0xFF69D0AE),
        accentAlt = Color(0xFFE5A0A4),
        danger = Color(0xFFFF927F),
        success = Color(0xFFA0D989),
    )

    val Ember = GlassTheme(
        id = "ember",
        name = "Ember Glass",
        background = Color(0xFF191513),
        backgroundAlt = Color(0xFF2B221D),
        panel = Color(0xA6312924),
        panelStrong = Color(0xFF3A302A),
        stroke = Color(0x4DFFD0A0),
        text = Color(0xFFF8EFE4),
        mutedText = Color(0xFFCBB6A4),
        accent = Color(0xFFF0B35C),
        accentAlt = Color(0xFFFF7D75),
        danger = Color(0xFFFF8E72),
        success = Color(0xFFB4D77A),
    )

    val Mono = GlassTheme(
        id = "mono",
        name = "Mono Ink",
        background = Color(0xFF111111),
        backgroundAlt = Color(0xFF202020),
        panel = Color(0xCC242424),
        panelStrong = Color(0xFF2E2E2E),
        stroke = Color(0x99FFFFFF),
        text = Color(0xFFFFFFFF),
        mutedText = Color(0xFFD2D2D2),
        accent = Color(0xFFFFFFFF),
        accentAlt = Color(0xFFBDBDBD),
        danger = Color(0xFFFFB4AA),
        success = Color(0xFFCFE8D2),
    )

    val Solar = GlassTheme(
        id = "solar",
        name = "Solar Clean",
        background = Color(0xFFF8F2E4),
        backgroundAlt = Color(0xFFEDE3CF),
        panel = Color(0xDDFDFBF5),
        panelStrong = Color(0xFFFFFCF4),
        stroke = Color(0x669B927F),
        text = Color(0xFF202119),
        mutedText = Color(0xFF676D5D),
        accent = Color(0xFF23856B),
        accentAlt = Color(0xFFE15C4F),
        danger = Color(0xFFD94A3D),
        success = Color(0xFF2E9465),
    )

    val all = listOf(Noir, Frost, Aurora, Ember, Mono, Solar)
}
