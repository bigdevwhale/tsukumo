package app.tsukumo.ui.art

import app.tsukumo.domain.Pers
import app.tsukumo.domain.Thing
import kotlin.math.PI
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

/** What the house needs to know to draw itself right now. */
data class Mood(
    val season: Season,
    val night: Boolean,
    val motif: Int,
    val tree: Int,
    val lit: Int,
    val wonders: Set<String>,
    /** Nobody has practised for a long while: the spirits sleep under the kotatsu. */
    val asleep: Boolean,
)

/**
 * The spirits moving about the house. Things not yet awake sit on the shelves (and the floor when the
 * shelves are full); awake ones walk, visit each other, sit on the engawa, nap at the kotatsu.
 */
/** Bubbles that are just a sign get a drawn sign instead of a font glyph. */
val ICONS: Map<String, List<String>> = mapOf(
    "♥" to listOf(".rr.rr.", "rrrrrrr", "rrrrrrr", ".rrrrr.", "..rrr..", "...r..."),
    "★" to listOf("...y...", "..yyy..", "yyyyyyy", ".yyyyy.", ".yy.yy.", "y.....y"),
    "♪" to listOf("..kkkk", "..k..k", "..k..k", "kkk.kk", "kkk.kk"),
    "!" to listOf("k", "k", "k", ".", "k"),
    "?" to listOf("kkk", "..k", ".kk", "...", ".k."),
    "z" to listOf("bbbb", "..b.", ".b..", "bbbb"),
    "…" to listOf(".....", ".....", "k.k.k"),
)

class World(private val lines: (Thing, Mood) -> String) {
    enum class Mode { SHELF, IDLE, WALK, TO_SLEEP, SLEEP, JUMP }

    class Actor(var thing: Thing) {
        var mode = Mode.IDLE
        var x = 0.0
        var foot = 0.0
        var tx = 0.0
        var tf = 0.0
        var onShelf = false
        var until = 0.0
        var blinkAt = 0.0
        var blinkUntil = 0.0
        var fxAt = 0.0
        var fxUntil = 0.0
        var talkAt = 0.0
        var bubble: String? = null
        var bubbleUntil = 0.0
        var cel = 0.0
        var dx = 0
        var dy = 0
        var napping = false
        var napEnd = 0.0
        var visit: Long? = null
        var garden = false
        var awoke = false
        var jx0 = 0.0; var jf0 = 0.0; var jx1 = 0.0; var jf1 = 0.0; var jt = 0.0
        /** Where the sprite was last drawn, for taps and bubbles. */
        var top = 0
        var bottom = 0
        val talking get() = bubble != null
    }

    val actors = LinkedHashMap<Long, Actor>()
    private val rnd = Random(7)
    var t = 0.0
        private set
    private var mood: Mood? = null

    private fun r(a: Double, b: Double) = a + rnd.nextDouble() * (b - a)

    private val sleepSlots = listOf(82 to 252, 112 to 252, 142 to 252, 172 to 252, 202 to 252, 44 to 268, 250 to 272, 20 to 300, 270 to 300, 300 to 300, 330 to 304)
    private val overflow = listOf(4 to 236, 38 to 236, 4 to 272, 38 to 272, 4 to 308, 38 to 308, 76 to 308, 110 to 308, 144 to 308, 178 to 308, 212 to 308, 246 to 308)

    private fun slot(i: Int): Triple<Int, Int, Boolean> =
        if (i < 12) Triple(88 + (i % 4) * 42, House.SHELF[i / 4], true)
        else overflow.getOrElse(i - 12) { 4 + ((i - 12) % 8) * 30 to 316 }.let { Triple(it.first, it.second, false) }

    private fun floorPoint(): Pair<Double, Double> {
        repeat(50) {
            val x = r(4.0, 328.0); val f = r(194.0, 316.0)
            val bad = (x < 58 && f < 198) || (x > 62 && x < 250 && f > 204 && f < 262) || (x > 220 && x < 256 && f > 178 && f < 196) ||
                (x > 306 && f < 184) || (x < 74 && f > 226 && actors.values.any { it.mode == Mode.SHELF && !it.onShelf })
            if (!bad) return x to f
        }
        return 280.0 to 300.0
    }

    private fun sleepSlot(a: Actor): Pair<Double, Double> {
        val n = actors.values.count { it !== a && (it.mode == Mode.SLEEP || it.mode == Mode.TO_SLEEP) }
        val s = sleepSlots[n % sleepSlots.size]
        return s.first + r(-2.0, 2.0) to s.second.toDouble()
    }

    /** Brings the actors in line with the things in the house. [restored] spirits jump out of the tansu. */
    fun sync(things: List<Thing>, m: Mood, restored: Long? = null) {
        mood = m
        val ids = things.map { it.id }.toSet()
        actors.keys.retainAll(ids)
        var slot = 0
        for (th in things) {
            val old = actors[th.id]
            val a = old ?: Actor(th).also {
                it.blinkAt = t + r(1.0, 4.0); it.fxAt = t + r(1.0, 5.0); it.talkAt = t + r(4.0, 16.0); it.until = t + r(0.0, 3.0)
                actors[th.id] = it
            }
            val wasAwake = old?.thing?.awake == true
            a.thing = th
            if (!th.awake) {
                val (x, f, on) = slot(slot++)
                a.mode = Mode.SHELF; a.x = x.toDouble(); a.foot = f.toDouble(); a.onShelf = on
                continue
            }
            when {
                old != null && old.mode == Mode.SHELF && !wasAwake -> jump(a, a.x, a.foot)
                old == null && restored != null && th.hobbyId == restored -> jump(a, 22.0, 160.0)
                old == null && m.asleep -> { val (x, f) = sleepSlot(a); a.x = x; a.foot = f; a.mode = Mode.SLEEP }
                old == null -> { val (x, f) = floorPoint(); a.x = x; a.foot = f; a.mode = Mode.IDLE; if (th.special) a.cel = t + 2 }
            }
        }
    }

    private fun jump(a: Actor, x0: Double, f0: Double) {
        val (x, f) = floorPoint()
        a.jx0 = x0; a.jf0 = f0; a.jx1 = x; a.jf1 = f; a.jt = 0.0; a.mode = Mode.JUMP; a.awoke = true
    }

    fun say(a: Actor, text: String, dur: Double = if (text.length > 3) 2.8 else 1.6) { a.bubble = text; a.bubbleUntil = t + dur }

    fun celebrate(ids: Collection<Long>) { for (id in ids) actors[id]?.let { it.cel = t + 2.0 } }

    fun cheer(hobby: Long, words: List<String>) {
        for (a in actors.values) if (a.thing.hobbyId == hobby && a.thing.stage >= 3 && a.bubble == null) say(a, words[rnd.nextInt(words.size)])
    }

    private fun choose(a: Actor) {
        val m = mood ?: return
        val p = a.thing.pers
        val q = rnd.nextDouble()
        val others = actors.values.filter { it !== a && it.mode != Mode.SHELF && it.mode != Mode.SLEEP && it.mode != Mode.JUMP }
        if ((m.night && (p == Pers.SLEEPY || q < .2)) || (p == Pers.SLEEPY && q < .12) || (m.season == Season.WINTER && q < .1)) {
            val (x, f) = sleepSlot(a); a.tx = x; a.tf = f; a.mode = Mode.TO_SLEEP; a.napping = true; a.napEnd = t + r(14.0, 40.0); return
        }
        if (q < .18 && others.isNotEmpty()) {
            val b = others[rnd.nextInt(others.size)]
            a.visit = b.thing.id; a.tx = (b.x + if (rnd.nextBoolean()) -22 else 22).coerceIn(4.0, 328.0); a.tf = (b.foot + r(-3.0, 3.0)).coerceIn(194.0, 316.0); a.mode = Mode.WALK; return
        }
        if (q < .42) {
            if (rnd.nextDouble() < .3) { a.tx = r(272.0, 290.0); a.tf = 204.0 } else { a.tx = r(270.0, 328.0); a.tf = r(214.0, 316.0) }
            a.garden = true; a.mode = Mode.WALK; return
        }
        val (x, f) = floorPoint(); a.tx = x; a.tf = f; a.mode = Mode.WALK
    }

    private fun speed(p: Pers) = when (p) { Pers.SHY -> 18.0; Pers.BOLD -> 34.0; Pers.SLEEPY -> 12.0; Pers.CURIOUS -> 26.0; Pers.PROUD -> 20.0; Pers.CARING -> 19.0 }

    /** One step of the simulation. [hearts] hears about two spirits meeting. */
    fun update(dt: Double, onMeet: (Long, Long) -> Unit = { _, _ -> }) {
        t += dt
        val m = mood ?: return
        for (a in actors.values) {
            val st = a.thing.stage
            if (a.bubble != null && t > a.bubbleUntil) a.bubble = null
            if (st >= 2 && t > a.blinkAt) { a.blinkUntil = t + .14; a.blinkAt = t + r(2.0, 5.0) }
            a.dx = 0; a.dy = 0
            when (a.mode) {
                Mode.SHELF -> {
                    if (st == 1) { if (t > a.fxAt) { a.fxUntil = t + .45; a.fxAt = t + r(2.5, 6.0) }; if (t < a.fxUntil) a.dx = if ((t * 14).toInt() % 2 == 0) 1 else -1 }
                    if (st == 4) { if (t > a.fxAt) { a.fxUntil = t + .22; a.fxAt = t + r(1.8, 4.0) }; if (t < a.fxUntil) a.dy = -2 }
                    if (st >= 3 && t > a.talkAt) { say(a, lines(a.thing, m)); a.talkAt = t + r(14.0, 30.0) }
                    continue
                }
                Mode.JUMP -> {
                    a.jt += dt; val k = (a.jt / 1.1).coerceAtMost(1.0)
                    a.x = a.jx0 + (a.jx1 - a.jx0) * k; a.foot = a.jf0 + (a.jf1 - a.jf0) * k - sin(k * PI) * 30
                    if (k >= 1) { a.mode = Mode.IDLE; a.until = t + 2; a.cel = t + 1.2 }
                    continue
                }
                else -> Unit
            }
            if (m.asleep && a.mode != Mode.SLEEP && a.mode != Mode.TO_SLEEP) { val (x, f) = sleepSlot(a); a.tx = x; a.tf = f; a.mode = Mode.TO_SLEEP; a.napping = false }
            if (m.asleep && a.mode == Mode.SLEEP) a.napping = false
            if (!m.asleep && a.mode == Mode.SLEEP && !a.napping) { a.mode = Mode.IDLE; a.until = t + r(.5, 2.0); say(a, "!") }
            if (a.mode == Mode.SLEEP) {
                if (a.napping && t > a.napEnd) { a.napping = false; a.mode = Mode.IDLE; a.until = t + 1 }
                if (t > a.talkAt) { say(a, "z"); a.talkAt = t + r(3.0, 7.0) }
                continue
            }
            if (a.mode == Mode.WALK || a.mode == Mode.TO_SLEEP) {
                val dx = a.tx - a.x; val df = a.tf - a.foot; val d = hypot(dx, df)
                val sp = speed(a.thing.pers) * (if (a.mode == Mode.TO_SLEEP) 1.3 else 1.0)
                if (d < 1.5) {
                    a.x = a.tx; a.foot = a.tf
                    if (a.mode == Mode.TO_SLEEP) { a.mode = Mode.SLEEP; a.talkAt = t + 1; continue }
                    a.mode = Mode.IDLE; a.until = t + (if (a.garden) r(8.0, 16.0) else r(3.0, 8.0)) * (1 + a.thing.rank * .6)
                    if (a.garden) { a.garden = false; if (rnd.nextDouble() < .5) say(a, lines(a.thing, m)) }
                    a.visit?.let { id -> actors[id]?.let { b -> if (hypot(b.x - a.x, b.foot - a.foot) < 46) { say(a, "♥"); say(b, "♥"); b.until = maxOf(b.until, t + 2); onMeet(a.thing.id, b.thing.id) } } }
                    a.visit = null
                } else { val s = minOf(d, sp * dt); a.x += dx / d * s; a.foot += df / d * s }
            }
            if (a.mode == Mode.IDLE && t > a.until) choose(a)
            if (st >= 3 && t > a.talkAt) { say(a, lines(a.thing, m)); a.talkAt = t + r(10.0, 24.0) * (1 + actors.size / 12.0) }
        }
    }

    // ------------------------------------------------------------------------------- drawing

    private fun look(a: Actor): Sprites.Look {
        val th = a.thing
        val st = th.stage
        val moving = a.mode == Mode.WALK || a.mode == Mode.TO_SLEEP || a.mode == Mode.JUMP
        val legs = if (st >= 4 && a.mode != Mode.SLEEP) (if (moving) (if ((t * 6).toInt() % 2 == 0) 2 else 1) else 1) else 0
        val eyes = when { a.mode == Mode.SLEEP || t < a.blinkUntil -> Sprites.Eyes.CLOSED; t < a.cel -> Sprites.Eyes.HAPPY; else -> Sprites.Eyes.OPEN }
        val talk = (a.bubble != null && a.bubble != "z") || t < a.cel
        return Sprites.Look(th.arch, th.pal, st, th.rank, eyes, talk, legs, st >= 5 && a.bubble != null && (a.bubble?.length ?: 0) > 1, th.kind == app.tsukumo.domain.Kind.KID)
    }

    private fun Buf.actor(a: Actor, dark: Boolean) {
        val s = Sprites.get(look(a))
        val x = a.x.roundToInt() + a.dx
        val foot = a.foot.roundToInt() + a.dy
        if (a.mode != Mode.SHELF && a.mode != Mode.JUMP && a.mode != Mode.SLEEP) { r(x + 9, a.foot.roundToInt() - 2, 14, 2, 0x28190f, .2f); r(x + 11, a.foot.roundToInt(), 10, 1, 0x28190f, .16f) }
        val top = foot - s.foot
        for (j in 0 until Sprites.GH) {
            val y = top + j; if (y < 0 || y >= h) continue
            for (i in 0 until Sprites.GW) { val c = s.px[j * Sprites.GW + i]; if (c ushr 24 != 0) p(x + i, y, if (dark) night(c) else c) }
        }
        a.top = top + s.top; a.bottom = foot
        if (a.thing.stage == 1 && a.dx != 0) { r(x + 2, foot - 12, 1, 3, 0x8a7a6a); r(x + 29, foot - 12, 1, 3, 0x8a7a6a) }
        if (a.thing.rank >= 4 && rnd.nextDouble() < .3) p(x + (4 + rnd.nextInt(24)), top + 6 + rnd.nextInt(24), 0xffe27a)
    }

    class Part(var x: Double, var y: Double, val vx: Double, val vy: Double, val c: Int, val s: Int, val out: Boolean, val g: Double, val ph: Double) { var rest = -1.0 }

    private var parts = ArrayList<Part>()
    private var pacc = 0.0
    private class Fly(var x: Double, var y: Double, val ph: Double, var vx: Double = 0.0, var vy: Double = 0.0)
    private val flies = ArrayList<Fly>()

    fun weather(dt: Double, still: Boolean) {
        val m = mood ?: return
        if (still) return
        val f = .32 + .68 * (m.tree / 25.0)
        pacc += when (m.season) { Season.SPRING -> 4.0; Season.SUMMER -> if (m.night) 0.0 else .3; Season.AUTUMN -> 3.5; Season.WINTER -> 14.0 } * dt
        while (pacc >= 1) {
            pacc -= 1
            if (m.season == Season.WINTER) parts += Part(r(292.0, 370.0), r(18.0, 22.0), r(-6.0, 1.0), r(12.0, 22.0), 0xffffff, 1, rnd.nextDouble() < .8, 0.0, r(0.0, 6.0))
            else {
                val inside = rnd.nextDouble() < .28
                val cols = when (m.season) { Season.SPRING -> intArrayOf(0xf7c0cf, 0xfbe0e7, 0xee9fb5); Season.AUTUMN -> intArrayOf(0xe2672f, 0xf0973d, 0xc5482a, 0xe9b44a); else -> intArrayOf(0x6aa84e) }
                val crown = House.FLOOR - (House.FLOOR - 50) * f
                parts += Part(r(340 - 24 * f, 362.0), r(crown - 6, crown + 14), if (inside) r(-34.0, -20.0) else r(-12.0, -2.0), r(7.0, 13.0), cols[rnd.nextInt(cols.size)], if (m.season == Season.AUTUMN) 2 else 1, !inside, 0.0, r(0.0, 6.0))
            }
            val p = parts.last()
            parts[parts.size - 1] = Part(p.x, p.y, p.vx, p.vy, p.c, p.s, p.out, if (p.out) r(118.0, 136.0) else r(150.0, 316.0), p.ph)
        }
        for (p in parts) { if (p.rest >= 0) continue; p.x += p.vx * dt + sin(t * 2 + p.ph) * .25; p.y += p.vy * dt; if (p.y >= p.g) { p.y = p.g; p.rest = t } }
        parts.removeAll { (it.rest >= 0 && t - it.rest > 5) || it.x < -4 || (it.out && it.x < 292) }
        if (m.season == Season.SUMMER && m.night && "flies" in m.wonders) {
            while (flies.size < 10) flies += Fly(r(296.0, 358.0), r(60.0, 134.0), r(0.0, 6.0))
            for (fl in flies) {
                fl.vx = (fl.vx + r(-14.0, 14.0) * dt).coerceIn(-6.0, 6.0); fl.vy = (fl.vy + r(-14.0, 14.0) * dt).coerceIn(-5.0, 5.0)
                fl.x = (fl.x + fl.vx * dt).coerceIn(294.0, 358.0); fl.y = (fl.y + fl.vy * dt).coerceIn(50.0, 134.0)
            }
        } else flies.clear()
    }

    /** Paints everything that moves over the still picture already in [b]. */
    fun render(b: Buf, ms: Long, tick: Int) {
        val m = mood ?: return
        val props = House.props(m.season, m.night)
        if (!m.night) House.clouds(b, ms, m.season)
        if ("koi" in m.wonders && m.season != Season.WINTER) House.koi(b, ms, m.night)
        for (p in parts) if (p.out) b.r(p.x.roundToInt(), p.y.roundToInt(), p.s, p.s, if (m.night) night(p.c) else p.c)
        if ("furin" in m.wonders) House.furin(b, ms, m.night)
        House.flames(b, m.lit, tick)
        for (a in actors.values) if (a.mode == Mode.SHELF && a.onShelf) b.actor(a, m.night)
        class D(val f: Double, val fn: () -> Unit)
        val list = ArrayList<D>()
        list += D(190.5) { props.tansu.paint(b) }
        list += D(190.6) { props.andon.paint(b) }
        list += D(236.0) { props.kotatsu.paint(b); House.steam(b, 92, 194, ms, 7, 16); House.steam(b, 104, 193, ms, 5, 12) }
        list += D(258.5) { props.flap.paint(b) }
        if ("bonsai" in m.wonders) list += D(176.5) { props.bonsai.paint(b) }
        if (m.season == Season.SUMMER) list += D(213.5) { props.pig.paint(b); House.steam(b, 346, 203, ms, 6, 14) }
        for (a in actors.values) if (!(a.mode == Mode.SHELF && a.onShelf)) list += D(if (a.mode == Mode.JUMP) maxOf(a.foot, a.jf1) else a.foot) { b.actor(a, m.night) }
        list.sortBy { it.f }
        for (d in list) d.fn()
        for (p in parts) if (!p.out) b.r(p.x.roundToInt(), p.y.roundToInt(), p.s, p.s, if (m.night) night(p.c) else p.c)
        if (!m.night) for (k in 0 until 12) {
            val u = ((ms / 1000.0) * .015 + k * .083) % 1.0; val v = (k * .37) % 1.0
            val x0 = 294 + (180 - 294) * u; val x1 = 352 + (290 - 352) * u; val y = 22 + (House.H - 22) * u
            b.p((x0 + (x1 - x0) * v + sin(ms / 1000.0 + k) * 2).roundToInt(), y.roundToInt(), 0xfff0c8, .55f)
        }
        if (m.night) {
            House.flames(b, m.lit, tick)
            House.nightLights(b, m.lit, "toro" in m.wonders, ms)
            for (fl in flies) if (sin(t * 3 + fl.ph) > 0) { b.p(fl.x.roundToInt(), fl.y.roundToInt(), 0xe8ff8a); b.glow(fl.x.roundToInt(), fl.y.roundToInt(), 4, 0xdcff78, .45f) }
        }
        if ("gold" in m.wonders && rnd.nextDouble() < .5) b.p(rnd.nextInt(House.W), rnd.nextInt(House.FLOOR), 0xffe27a, .7f)
        for (a in actors.values) a.bubble?.let { txt -> ICONS[txt]?.let { ic -> b.icon(ic, a.x.roundToInt() + 13 + a.dx, a.top - 9, m.night) } }
        for (a in actors.values) if (t < a.cel) repeat(4) { b.p(a.x.roundToInt() + rnd.nextInt(-4, 36), a.top + rnd.nextInt(0, (a.bottom - a.top).coerceAtLeast(1)), if (rnd.nextBoolean()) 0xffe27a else 0xffffff) }
    }

    private fun Buf.icon(rows: List<String>, x: Int, y: Int, dark: Boolean) {
        val w = rows[0].length
        r(x - 2, y - 2, w + 4, rows.size + 4, 0x2b1c1e); r(x - 1, y - 1, w + 2, rows.size + 2, 0xfffaf0)
        rows.forEachIndexed { j, row -> row.forEachIndexed { i, ch -> val c = when (ch) { 'r' -> 0xe0566a; 'y' -> 0xe7b847; 'k' -> 0x2b1c1e; 'b' -> 0x5a7fb0; else -> -1 }; if (c >= 0) p(x + i, y + j, c) } }
        p(x + w / 2, y + rows.size + 2, 0x2b1c1e); p(x + w / 2 - 1, y + rows.size + 2, 0x2b1c1e); p(x + w / 2, y + rows.size + 3, 0x2b1c1e)
    }

    /** The spirit under an art pixel, the frontmost first. */
    fun at(x: Double, y: Double): Actor? = actors.values.filter { x >= it.x + 3 && x < it.x + 29 && y >= it.top - 2 && y < it.bottom }.maxByOrNull { it.foot }
}
