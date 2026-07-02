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
        background = Color(0xFF10141C),
        backgroundAlt = Color(0xFF1A202A),
        panel = Color(0x66FFFFFF),
        panelStrong = Color(0x9928303B),
        stroke = Color(0x33FFFFFF),
        text = Color(0xFFF6F7F2),
        mutedText = Color(0xFFADB5C2),
        accent = Color(0xFFD9E487),
        accentAlt = Color(0xFFC9BDF0),
        danger = Color(0xFFE08B83),
        success = Color(0xFF8FD8C2),
    )

    val Frost = GlassTheme(
        id = "frost",
        name = "Frost Glass",
        background = Color(0xFFF2F6F8),
        backgroundAlt = Color(0xFFDDE8ED),
        panel = Color(0x99FFFFFF),
        panelStrong = Color(0xCCFFFFFF),
        stroke = Color(0x6688A3B2),
        text = Color(0xFF15212B),
        mutedText = Color(0xFF60717E),
        accent = Color(0xFF6FA8C9),
        accentAlt = Color(0xFF91BBAA),
        danger = Color(0xFFC36D65),
        success = Color(0xFF4A9B83),
    )

    val Aurora = GlassTheme(
        id = "aurora",
        name = "Aurora Glass",
        background = Color(0xFF0F1B19),
        backgroundAlt = Color(0xFF182A27),
        panel = Color(0x663E6A62),
        panelStrong = Color(0x99455D58),
        stroke = Color(0x338FD8C2),
        text = Color(0xFFF0F8F4),
        mutedText = Color(0xFFA5BDB5),
        accent = Color(0xFF8FD8C2),
        accentAlt = Color(0xFFD6A3A7),
        danger = Color(0xFFE09786),
        success = Color(0xFFA8DCA7),
    )

    val Ember = GlassTheme(
        id = "ember",
        name = "Ember Glass",
        background = Color(0xFF181512),
        backgroundAlt = Color(0xFF2A231D),
        panel = Color(0x665D5147),
        panelStrong = Color(0x99605245),
        stroke = Color(0x33FFD8A0),
        text = Color(0xFFF8F0E6),
        mutedText = Color(0xFFC6B5A4),
        accent = Color(0xFFE6B36A),
        accentAlt = Color(0xFFD78E7F),
        danger = Color(0xFFE38B7A),
        success = Color(0xFFAFCB8D),
    )

    val Mono = GlassTheme(
        id = "mono",
        name = "Mono Ink",
        background = Color(0xFF111111),
        backgroundAlt = Color(0xFF202020),
        panel = Color(0x88FFFFFF),
        panelStrong = Color(0xCC2B2B2B),
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
        background = Color(0xFFF8F6ED),
        backgroundAlt = Color(0xFFEAE4D4),
        panel = Color(0x99FFFFFF),
        panelStrong = Color(0xCCFFFFFF),
        stroke = Color(0x66B8B09D),
        text = Color(0xFF1B211A),
        mutedText = Color(0xFF687060),
        accent = Color(0xFF7FA46A),
        accentAlt = Color(0xFFC7A766),
        danger = Color(0xFFC67668),
        success = Color(0xFF6C9F77),
    )

    val all = listOf(Noir, Frost, Aurora, Ember, Mono, Solar)
}
