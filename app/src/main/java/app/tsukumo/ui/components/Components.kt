package app.tsukumo.ui.components

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import app.tsukumo.domain.Thing
import app.tsukumo.ui.art.Sprites
import app.tsukumo.ui.theme.C
import app.tsukumo.ui.theme.T

/** A panel with a hard 2dp border, the way the whole app is boxed. */
fun Modifier.box(bg: Color = C.panel2, border: Color = C.line) = this.background(bg).border(2.dp, border)

@Composable
fun Btn(text: String, modifier: Modifier = Modifier, primary: Boolean = false, enabled: Boolean = true, onClick: () -> Unit) {
    val bg = when { !enabled -> C.panel2; primary -> C.accent; else -> C.panel }
    val bd = when { !enabled -> C.line; primary -> C.accentDeep; else -> C.line }
    val fg = when { !enabled -> C.faint; primary -> C.paperInk; else -> C.ink }
    Box(
        modifier.box(bg, bd).clickable(enabled = enabled, onClick = onClick).padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) { Text(text, style = T.Button.copy(color = fg)) }
}

@Composable
fun Chip(text: String, on: Boolean, modifier: Modifier = Modifier, sub: String? = null, onClick: () -> Unit) {
    Column(modifier.box(if (on) Color(0xFF3B2A2A) else C.field, if (on) C.accent else C.line).clickable(onClick = onClick).padding(horizontal = 10.dp, vertical = 7.dp)) {
        Text(text, style = T.Body.copy(color = if (on) C.accent else C.ink))
        if (sub != null) Text(sub, style = T.Small)
    }
}

@Composable
fun Bar(p: Float, modifier: Modifier = Modifier, color: Color = C.accent, height: Dp = 5.dp) {
    Box(modifier.fillMaxWidth().height(height).background(Color(0xFF45374C))) {
        Box(Modifier.fillMaxWidth(p.coerceIn(0f, 1f)).height(height).background(color))
    }
}

@Composable
fun Eyebrow(text: String, modifier: Modifier = Modifier) = Text(text.uppercase(), style = T.Eyebrow, modifier = modifier)

@Composable
fun Gap(h: Int = 10) = Spacer(Modifier.height(h.dp))

/** A sprite drawn with hard pixels at [px] dp per art pixel; the empty headroom above is trimmed. */
@Composable
fun SpriteImage(look: Sprites.Look, px: Float, modifier: Modifier = Modifier, crop: Int = 4) {
    val s = remember(look) { Sprites.get(look) }
    val bmp = remember(s) {
        Bitmap.createBitmap(Sprites.GW, Sprites.GH - crop, Bitmap.Config.ARGB_8888).also { it.setPixels(s.px, crop * Sprites.GW, Sprites.GW, 0, 0, Sprites.GW, Sprites.GH - crop) }.asImageBitmap()
    }
    Canvas(modifier.size((Sprites.GW * px).dp, ((Sprites.GH - crop) * px).dp)) {
        drawImage(bmp, IntOffset.Zero, IntSize(bmp.width, bmp.height), IntOffset.Zero, IntSize(size.width.toInt(), size.height.toInt()), filterQuality = FilterQuality.None)
    }
}

fun Thing.look(): Sprites.Look = Sprites.Look(arch, pal, stage, rank, sprout = kind == app.tsukumo.domain.Kind.KID, legs = if (stage >= 4) 1 else 0)

/** A full-screen sheet over the house, with a title and a way back. */
@Composable
fun Sheet(title: String, onClose: () -> Unit, closeLabel: String, content: @Composable ColumnScope.() -> Unit) {
    BackHandler(onBack = onClose)
    Box(Modifier.fillMaxSize().background(Color(0xCC0F0B11)).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(10.dp).box(C.panel, C.lineHi)) {
            Row(Modifier.fillMaxWidth().background(C.panel2).padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = T.Title, modifier = Modifier.weight(1f))
                Text(closeLabel, style = T.Button.copy(color = C.muted), modifier = Modifier.clickable(onClick = onClose).padding(6.dp))
            }
            Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()).padding(14.dp), content = content)
        }
    }
}

@Composable
fun Field(value: String, onChange: (String) -> Unit, hint: String, modifier: Modifier = Modifier, single: Boolean = true) {
    Box(modifier.fillMaxWidth().box(C.field, C.line).padding(horizontal = 10.dp, vertical = 9.dp)) {
        if (value.isEmpty()) Text(hint, style = T.Body.copy(color = C.faint))
        BasicTextField(
            value, onChange, singleLine = single, textStyle = T.Body, cursorBrush = SolidColor(C.accent), modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        )
    }
}

/** A row of wrapped chips. */
@Composable
fun Flow(modifier: Modifier = Modifier, gap: Dp = 6.dp, content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.FlowRow(modifier, horizontalArrangement = Arrangement.spacedBy(gap), verticalArrangement = Arrangement.spacedBy(gap)) { content() }
}
