package app.tsukumo.ui.art

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import app.tsukumo.domain.State
import app.tsukumo.ui.theme.C
import app.tsukumo.ui.theme.T
import java.time.LocalDateTime
import kotlin.math.roundToInt

/** Global switches for the art. */
object Art {
    /** Screenshots and renders want a still frame: no loop, a fixed moment. */
    @Volatile var still = false
}

fun seasonOf(month: Int) = when (month) { 12, 1, 2 -> Season.WINTER; 3, 4, 5 -> Season.SPRING; 6, 7, 8 -> Season.SUMMER; else -> Season.AUTUMN }

/** How the house looks for a state at a moment. */
fun mood(s: State, now: LocalDateTime, nowMs: Long, season: Season? = null, night: Boolean? = null): Mood {
    val last = s.sessions.lastOrNull()?.at
    val f = .32 + .68 * (s.candles / 1000.0).coerceAtMost(1.0)
    return Mood(
        season = season ?: seasonOf(now.monthValue),
        night = night ?: (now.hour >= 19 || now.hour < 6),
        motif = s.year(nowMs),
        tree = (f * 25).roundToInt(),
        lit = s.litCandles,
        wonders = app.tsukumo.domain.Rules.WONDERS.filter { s.candles >= it.at }.map { it.key }.toSet(),
        asleep = last != null && nowMs - last > 7L * 24 * 3600 * 1000 && s.things.any { it.awake },
    )
}

fun baseKey(m: Mood) = House.Key(m.season, m.night, m.motif, m.tree, "shimenawa" in m.wonders, "gold" in m.wonders)

/**
 * The house, alive: it steps the [world] every frame, paints it over the cached still picture, shows the
 * spirits' words in bubbles and answers taps on a spirit, the tansu and the candles.
 */
@Composable
fun HouseView(
    world: World,
    mood: Mood,
    modifier: Modifier = Modifier,
    onSpirit: (Long) -> Unit = {},
    onChest: () -> Unit = {},
    onCandles: () -> Unit = {},
) {
    val key = baseKey(mood)
    val base = remember(key) { House.base(key) }
    val frame = remember { Buf(House.W, House.H) }
    val bitmap = remember { Bitmap.createBitmap(House.W, House.H, Bitmap.Config.ARGB_8888) }
    val image = remember(bitmap) { bitmap.asImageBitmap() }
    var redraw by remember { mutableIntStateOf(0) }

    fun paint(ms: Long, tick: Int) {
        frame.copyFrom(base)
        world.render(frame, ms, tick)
        bitmap.setPixels(frame.d, 0, frame.w, 0, 0, frame.w, frame.h)
    }

    if (Art.still) {
        remember(key, world.actors.size) { repeat(40) { world.update(.1) }; paint(12_000, 7); 0 }
    } else {
        LaunchedEffect(key) {
            var last = 0L
            var tickAt = 0L
            var tick = 0
            var ms0 = 0L
            while (true) {
                withFrameNanos { ns ->
                    val ms = ns / 1_000_000
                    if (ms0 == 0L) ms0 = ms
                    val dt = if (last == 0L) 0.0 else ((ms - last) / 1000.0).coerceAtMost(.1)
                    // About 30 frames a second is plenty for pixel art and kind to the battery.
                    if (ms - last >= 30 || last == 0L) {
                        last = ms
                        world.update(dt)
                        world.weather(dt, false)
                        if (ms - tickAt >= 110) { tickAt = ms; tick++ }
                        paint(ms - ms0, tick)
                        redraw++
                    }
                }
            }
        }
    }

    BoxWithConstraints(modifier.fillMaxWidth().aspectRatio(House.W.toFloat() / House.H).background(C.dark)) {
        val density = LocalDensity.current
        val scale = with(density) { maxWidth.toPx() } / House.W
        Canvas(
            Modifier.fillMaxSize().pointerInput(world) {
                detectTapGestures { o ->
                    val x = o.x / scale; val y = o.y / scale
                    val a = world.at(x.toDouble(), y.toDouble())
                    when {
                        a != null -> onSpirit(a.thing.id)
                        x in 4f..58f && y in 142f..192f -> onChest()
                        x in 7f..75f && y in 80f..126f -> onCandles()
                    }
                }
            },
        ) {
            @Suppress("UNUSED_EXPRESSION") redraw
            drawImage(image, IntOffset.Zero, IntSize(House.W, House.H), IntOffset.Zero, IntSize(size.width.roundToInt(), size.height.roundToInt()), filterQuality = FilterQuality.None)
        }
        // The spirits' words.
        @Suppress("UNUSED_EXPRESSION") redraw
        for (a in world.actors.values) {
            val text = a.bubble?.takeIf { it !in ICONS } ?: continue
            val cx = (a.x + 16 + a.dx) * scale
            val ty = (a.top - 1) * scale
            Text(
                text, style = T.Bubble, maxLines = 1,
                modifier = Modifier
                    .layout { m, c ->
                        val p = m.measure(c.copy(minWidth = 0, maxWidth = (House.W * scale).roundToInt()))
                        layout(p.width, p.height) {
                            val x = (cx - p.width / 2f).roundToInt().coerceIn(0, (House.W * scale - p.width).roundToInt().coerceAtLeast(0))
                            p.place(x, (ty - p.height).roundToInt().coerceAtLeast(0))
                        }
                    }
                    .background(Color(0xFFFFFAF0)).border(2.dp, Color(0xFF2B1C1E)).padding(horizontal = 5.dp, vertical = 1.dp),
            )
        }
    }
}
