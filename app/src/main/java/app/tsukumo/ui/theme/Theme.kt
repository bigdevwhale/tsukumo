package app.tsukumo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.tsukumo.R

/** The colours of the house at dusk: dark lacquer, paper, persimmon, the gold of candlelight. */
@Immutable
object C {
    val bg = Color(0xFF1C161F)
    val panel = Color(0xFF2A2130)
    val panel2 = Color(0xFF231B28)
    val line = Color(0xFF4A3A50)
    val lineHi = Color(0xFF6C5776)
    val ink = Color(0xFFF4E7CF)
    val muted = Color(0xFFB7A58C)
    val faint = Color(0xFF7D6C80)
    val accent = Color(0xFFEE8A55)
    val accentDeep = Color(0xFFB85F3A)
    val gold = Color(0xFFFFD9A0)
    val goldDeep = Color(0xFFE7B847)
    val paper = Color(0xFFFFF4DF)
    val paperInk = Color(0xFF2B1C1E)
    val paperMuted = Color(0xFF7A6650)
    val blue = Color(0xFF5A7FB0)
    val field = Color(0xFF1F1824)
    val dark = Color(0xFF0F0B11)
}

/**
 * Every text in the app: m6x11 by Daniel Linssen with Cyrillic by Natto Games (free with attribution).
 * One family; sizes set the hierarchy.
 */
val Pixel = FontFamily(Font(R.font.m6x11, FontWeight.Normal))

object T {
    val Display = TextStyle(fontFamily = Pixel, fontSize = 34.sp, lineHeight = 34.sp, color = C.gold)
    val Title = TextStyle(fontFamily = Pixel, fontSize = 27.sp, lineHeight = 28.sp, color = C.gold)
    val Heading = TextStyle(fontFamily = Pixel, fontSize = 23.sp, lineHeight = 24.sp, color = C.ink)
    val Eyebrow = TextStyle(fontFamily = Pixel, fontSize = 16.sp, lineHeight = 17.sp, letterSpacing = 0.04.em, color = C.accent)
    val Body = TextStyle(fontFamily = Pixel, fontSize = 20.sp, lineHeight = 22.sp, color = C.ink)
    val Sub = TextStyle(fontFamily = Pixel, fontSize = 18.sp, lineHeight = 19.sp, color = C.muted)
    val Small = TextStyle(fontFamily = Pixel, fontSize = 16.sp, lineHeight = 17.sp, color = C.muted)
    val Number = TextStyle(fontFamily = Pixel, fontSize = 30.sp, lineHeight = 28.sp, color = C.gold)
    val Button = TextStyle(fontFamily = Pixel, fontSize = 21.sp, lineHeight = 21.sp)
    val Bubble = TextStyle(fontFamily = Pixel, fontSize = 16.sp, lineHeight = 16.sp, color = C.paperInk)
}

@Composable
fun TsukumoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = C.accent, onPrimary = C.paperInk, background = C.bg, surface = C.panel, onSurface = C.ink,
            onBackground = C.ink, surfaceContainer = C.panel, surfaceContainerHigh = C.panel, surfaceContainerHighest = C.panel2,
            outline = C.line, error = C.accent,
        ),
        typography = MaterialTheme.typography.copy(bodyLarge = T.Body, bodyMedium = T.Body, bodySmall = T.Small, labelLarge = T.Button, titleLarge = T.Title),
        content = content,
    )
}
