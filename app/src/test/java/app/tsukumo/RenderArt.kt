package app.tsukumo

import androidx.test.core.app.ApplicationProvider
import app.tsukumo.data.DemoSeed
import app.tsukumo.ui.Words
import app.tsukumo.ui.art.Buf
import app.tsukumo.ui.art.House
import app.tsukumo.ui.art.Season
import app.tsukumo.ui.art.Sprites
import app.tsukumo.ui.art.World
import app.tsukumo.ui.art.baseKey
import app.tsukumo.ui.art.mood
import app.tsukumo.ui.art.sprite
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.time.LocalDateTime

/** Draws the house and the spirits to PNGs for a look (run with `-Prender`); not a test. */
@RunWith(RobolectricTestRunner::class)
class RenderArtScenes {
    private val out = File(System.getProperty("render.out") ?: "build/art").apply { mkdirs() }

    @Test fun sprites() {
        val archs = listOf("pencil", "brush", "yarn", "book", "jar", "box", "guitar", "pick", "pot", "stone", "cup", "bottle", "knight", "sheet", "bell", "lantern", "spool", "camera", "mat", "ball", "clock", "obake", "tanuki", "kitsune", "warashi")
        val pals = listOf("mustard", "cocoa", "sakura", "indigo", "cream", "wood", "wood", "persimmon", "persimmon", "slate", "matcha", "sky", "cream", "cream", "mustard", "persimmon", "sakura", "slate", "plum", "sky", "cocoa", "obake", "tanuki", "kitsune", "warashi")
        val steps = listOf(0 to 0, 1 to 0, 2 to 0, 3 to 0, 4 to 0, 5 to 0, 5 to 1, 5 to 2, 5 to 3, 5 to 4)
        val b = Buf(steps.size * 34 + 2, archs.size * 46 + 2)
        b.r(0, 0, b.w, b.h, 0x2a2130)
        archs.forEachIndexed { j, a ->
            steps.forEachIndexed { i, (st, rk) ->
                b.sprite(Sprites.get(Sprites.Look(a, pals[j], st, rk, legs = if (st >= 4) 1 else 0)), 2 + i * 34, 2 + j * 46 + 44)
            }
        }
        Png.write(File(out, "sprites.png"), b, 3)
    }

    @Test fun house() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<TsukumoApp>()
        val c = app.container
        DemoSeed.seedIfEmpty(app, c)
        val s = c.repo.refresh()
        val w = Words(true)
        val now = LocalDateTime.of(2026, 10, 23, 15, 0)
        for ((name, season, night) in listOf(Triple("autumn-day", Season.AUTUMN, false), Triple("spring-day", Season.SPRING, false), Triple("summer-night", Season.SUMMER, true), Triple("winter-day", Season.WINTER, false))) {
            val m = mood(s, now, System.currentTimeMillis(), season, night)
            val world = World { th, md -> w.line(th, md, s) }
            world.sync(s.living, m)
            repeat(120) { world.update(.1); world.weather(.1, false) }
            val b = House.base(baseKey(m)).let { base -> Buf(House.W, House.H).also { it.copyFrom(base) } }
            world.render(b, 12_000, 7)
            Png.write(File(out, "house-$name.png"), b, 3)
        }
        // A brand-new house: everything still asleep on the shelves.
        val fresh = s.copy(sessions = emptyList(), evs = emptyList(), bonds = emptyList(), things = s.things.filter { !it.special }.map { it.copy(pts = listOf(0, 2, 5, 12, 30, 45)[(it.id % 6).toInt()]) })
        val m = mood(fresh, now, System.currentTimeMillis(), Season.SPRING, false)
        val world = World { th, md -> w.line(th, md, fresh) }
        world.sync(fresh.living, m)
        repeat(60) { world.update(.1) }
        val b = Buf(House.W, House.H).also { it.copyFrom(House.base(baseKey(m))) }
        world.render(b, 12_000, 7)
        Png.write(File(out, "house-new.png"), b, 3)
    }
}

/** The launcher icon's picture: a spirit in candlelight, 60×60 art pixels with a transparent ground. */
@RunWith(RobolectricTestRunner::class)
class RenderArtIcon {
    private val out = File(System.getProperty("render.out") ?: "build/art").apply { mkdirs() }

    private fun icon(arch: String, pal: String, name: String) {
        val b = Buf(60, 60)
        for (y in 0 until 60) for (x in 0 until 60) {
            val d = kotlin.math.hypot(x - 29.5, y - 31.5)
            val lv = ((1 - d / 28).coerceAtLeast(0.0) * 3 + (app.tsukumo.ui.art.bay(x, y) / 16.0 - .5) * .7).toInt()
            if (lv >= 1) b.p(x, y, intArrayOf(0, 0x4e2e44, 0x744048, 0xa0584a)[lv.coerceAtMost(3)])
        }
        b.sprite(Sprites.get(Sprites.Look(arch, pal, 5, 0, Sprites.Eyes.HAPPY, legs = 1)), 14, 52)
        Png.write(File(out, "icon-$name.png"), b, 1)
        Png.write(File(out, "icon-$name-big.png"), b, 8)
    }

    @Test fun icons() {
        icon("yarn", "sakura", "yarn")
        File(out, "icon-yarn.png").copyTo(File(out, "../../../docs/icon-src.png").apply { parentFile?.mkdirs() }, overwrite = true)
    }
}
