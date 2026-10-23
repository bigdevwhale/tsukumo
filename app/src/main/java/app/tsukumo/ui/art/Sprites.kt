package app.tsukumo.ui.art

import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.math.sqrt

/**
 * The spirits. Every thing is drawn from one of ~20 silhouettes into a 32×44 grid of materials; the
 * shading comes from the silhouette itself (light from the upper left), shadows lean cool and violet,
 * lights lean warm, and the outline takes the darkest shade of whatever it touches.
 *
 * Materials: b/a = the palette's body and accent; W light wood, D dark wood, G glass, P paper, m metal,
 * y gold, g leaf, T tea, C crimson; n, w and R are flat (ink, white, tongue). A trailing + or - shades
 * a cell one step darker or lighter.
 */
object Sprites {
    const val GW = 32
    const val GH = 44
    /** Rows left free above the body for hats and halos. */
    const val OY = 7
    private const val GN = GW * GH

    class Pal(val b: Int, val a: Int)

    val PALS: Map<String, Pal> = mapOf(
        "wood" to Pal(0xc8884f, 0xd9573f), "indigo" to Pal(0x5b6fb0, 0xe8c35a), "sakura" to Pal(0xf0a3b5, 0x8a5aa0),
        "matcha" to Pal(0x8fb06a, 0xe8d58a), "persimmon" to Pal(0xee8a3c, 0x4f8a3a), "cream" to Pal(0xefe2c2, 0xc4513f),
        "slate" to Pal(0x8d97a3, 0xe7a04a), "plum" to Pal(0x9a5a8e, 0xf0b45a), "mustard" to Pal(0xe0b546, 0x4a6fa5),
        "sky" to Pal(0x79b6d6, 0xf08a7a), "cocoa" to Pal(0x8a5a44, 0xe6c07a),
        "obake" to Pal(0xf4e7c6, 0xd2463a), "tanuki" to Pal(0x9a7454, 0xd9a441), "kitsune" to Pal(0xf4efe6, 0xd2463a), "warashi" to Pal(0xc4413a, 0xf4c8d2),
    )

    private val BASES = mapOf('W' to 0xe2b27a, 'D' to 0x5e3a26, 'G' to 0xcde6ee, 'P' to 0xf3ead6, 'm' to 0xb4bac4, 'y' to 0xe7b847, 'g' to 0x6fa24a, 'T' to 0x7a4a2a, 'C' to 0xc93f36)
    private val FLAT = mapOf('n' to 0x2a2030, 'w' to 0xffffff, 'R' to 0xe0566a)
    private const val EYE = 0x231a26
    private const val BLUSH = 0xf4899c
    private const val TONGUE = 0xe0566a
    private const val BROW = 0xf6f2ea
    private const val HALO = 0xffd75a
    private const val LEGS = 0x2e1e28

    // ---------------------------------------------------------------- grid helpers

    class Grid { val c = arrayOfNulls<String>(GN) }

    private fun Grid.p(x: Double, y: Double, m: String?) { val i = x.roundToInt(); val j = y.roundToInt(); if (i in 0 until GW && j in 0 until GH) c[j * GW + i] = m }
    private fun Grid.p(x: Int, y: Int, m: String?) { if (x in 0 until GW && y in 0 until GH) c[y * GW + x] = m }
    private fun Grid.q(x: Int, y: Int): String? = if (x in 0 until GW && y in 0 until GH) c[y * GW + x] else null
    private fun Grid.r(x: Int, y: Int, w: Int, h: Int, m: String?) { for (j in y until y + h) for (i in x until x + w) p(i, j, m) }
    private fun Grid.rb(x: Int, y: Int, w: Int, h: Int, m: String) { for (j in y until y + h) for (i in x until x + w) if (q(i, j) != null) p(i, j, m) }
    private fun Grid.e(cx: Double, cy: Double, rx: Double, ry: Double, m: String?) {
        for (y in 0 until GH) for (x in 0 until GW) { val dx = (x + .5 - cx) / rx; val dy = (y + .5 - cy) / ry; if (dx * dx + dy * dy <= 1) p(x, y, m) }
    }
    private fun Grid.ring(cx: Double, cy: Double, r: Double, m: String, th: Double = .5, force: Boolean = false) {
        for (y in 0 until GH) for (x in 0 until GW) { val d = hypot(x + .5 - cx, y + .5 - cy); if (abs(d - r) < th && (force || q(x, y) != null)) p(x, y, m) }
    }
    private fun Grid.l(x0: Int, y0: Int, x1: Int, y1: Int, m: String) {
        val n = max(abs(x1 - x0), abs(y1 - y0)).coerceAtLeast(1)
        for (i in 0..n) p(x0 + (x1 - x0) * i / n.toDouble(), y0 + (y1 - y0) * i / n.toDouble(), m)
    }

    private fun Grid.lantern(rib: String) {
        e(16.0, 15.0, 11.5, 11.0, "b"); for (y in intArrayOf(6, 9, 20, 23)) rb(0, y, 32, 1, rib)
        r(10, 2, 12, 3, "D"); r(10, 26, 12, 3, "D"); r(10, 2, 12, 1, "D-"); p(15, 0, "m"); p(16, 0, "m"); p(14, 1, "m"); p(17, 1, "m")
    }

    private val KNIGHT = listOf(
        "................", ".....b..b.......", ".....bbbbb......", "....bbbbbbb.....", "...bbbbbbbbb....", "..bbbbbbbbbb....", "..bbb..bbbbb....",
        ".......bbbbb....", "......bbbbbb....", ".....bbbbbbb....", "....aaaaaaaa....", "...bbbbbbbbbb...", "...bbbbbbbbbb...", "...ssssssssss...",
    )

    class Arch(val face: Int, val lx: Int, val rx: Int = 30 - lx, val noMouth: Boolean = false, val noArms: Boolean = false, val draw: Grid.() -> Unit)

    private val ARCH: Map<String, Arch> = mapOf(
        "pencil" to Arch(11, 12) {
            r(15, 1, 2, 2, "n"); r(14, 3, 4, 1, "W"); r(13, 4, 6, 1, "W"); r(12, 5, 8, 1, "W"); r(11, 6, 10, 1, "W"); r(15, 3, 2, 1, "n")
            r(11, 7, 10, 16, "b"); for (x in 11..20 step 2) p(x, 7, "W"); r(14, 8, 1, 15, "b-"); r(17, 8, 1, 15, "b+")
            r(11, 23, 10, 3, "m"); r(11, 24, 10, 1, "y"); r(11, 26, 10, 3, "a"); p(11, 28, null); p(20, 28, null)
        },
        "brush" to Arch(6, 13) {
            r(12, 0, 8, 17, "b"); p(12, 0, null); p(19, 0, null); r(12, 1, 8, 2, "a"); r(12, 13, 8, 1, "b+"); r(12, 14, 8, 1, "b-")
            r(11, 17, 10, 3, "m"); r(11, 18, 10, 1, "m+"); r(11, 20, 10, 4, "D"); r(12, 24, 8, 2, "D"); r(13, 26, 6, 1, "n"); r(14, 27, 4, 1, "n"); r(15, 28, 2, 1, "n"); l(14, 21, 14, 25, "D-")
        },
        "yarn" to Arch(15, 10) {
            l(4, 3, 12, 11, "m"); l(5, 3, 13, 11, "m"); l(27, 2, 20, 10, "m"); l(28, 2, 21, 10, "m"); e(4.5, 2.5, 1.8, 1.8, "a"); e(28.0, 2.0, 1.8, 1.8, "a")
            e(16.0, 17.5, 12.5, 11.5, "b"); ring(8.0, 12.0, 7.0, "b+"); ring(8.0, 12.0, 3.5, "b+"); ring(25.0, 25.0, 7.5, "b+"); ring(16.0, 34.0, 14.0, "b+"); ring(30.0, 4.0, 13.0, "b-")
            l(26, 27, 29, 29, "b"); l(29, 29, 31, 28, "b")
        },
        "book" to Arch(15, 11) {
            r(7, 3, 18, 27, "b"); r(25, 5, 2, 24, "P"); for (y in 7 until 28 step 3) p(26, y, "P+")
            r(7, 3, 3, 27, "a"); r(7, 6, 3, 1, "y"); r(7, 26, 3, 1, "y"); r(12, 7, 9, 5, "P"); for (x in 13 until 20 step 2) p(x, 9, "n")
            p(24, 3, "y"); p(23, 3, "y"); p(24, 4, "y"); p(24, 29, "y"); p(23, 29, "y"); p(24, 28, "y"); r(19, 30, 2, 3, "C"); p(19, 32, null)
        },
        "jar" to Arch(9, 11) {
            r(9, 2, 14, 5, "a"); r(9, 2, 14, 1, "a-"); for (x in 10 until 22 step 2) r(x, 4, 1, 2, "a+"); r(10, 7, 12, 1, "m")
            r(7, 8, 18, 22, "G"); p(7, 8, null); p(24, 8, null); p(7, 29, null); p(24, 29, null)
            r(8, 16, 16, 13, "b"); r(8, 16, 16, 1, "b-"); for ((x, y) in listOf(10 to 25, 21 to 19, 20 to 26)) p(x, y, "b-"); r(9, 9, 1, 5, "w")
        },
        "box" to Arch(16, 10) {
            r(5, 12, 22, 18, "b"); r(7, 14, 18, 1, "b+"); r(7, 27, 18, 1, "b-"); r(7, 14, 1, 14, "b+"); r(24, 14, 1, 14, "b-")
            r(4, 7, 24, 6, "a"); r(4, 7, 24, 1, "a-"); r(4, 12, 24, 1, "a+"); r(14, 11, 4, 4, "y"); p(15, 13, "n"); p(16, 13, "n"); r(5, 28, 2, 2, "m"); r(25, 28, 2, 2, "m")
        },
        "guitar" to Arch(13, 11) {
            r(13, 0, 6, 5, "D"); p(13, 0, null); p(18, 0, null); for (y in intArrayOf(1, 3)) { r(11, y, 2, 1, "m"); r(19, y, 2, 1, "m") }
            r(14, 5, 4, 7, "D"); for (y in intArrayOf(6, 8, 10)) r(14, y, 4, 1, "m")
            e(16.0, 15.0, 7.5, 5.5, "b"); e(16.0, 23.5, 10.0, 6.5, "b"); e(20.5, 25.0, 2.2, 2.8, "b+"); e(16.0, 22.0, 3.6, 3.6, "a"); e(16.0, 22.0, 2.8, 2.8, "n")
            r(12, 27, 8, 2, "D"); r(12, 27, 8, 1, "P"); for (x in intArrayOf(15, 16)) { r(x, 0, 1, 12, "m-"); r(x, 19, 1, 8, "m-") }
        },
        "pick" to Arch(11, 11) {
            e(16.0, 12.0, 11.5, 8.0, "b"); for (y in 12..29) { val hw = 11.5 * (1 - (y - 12) / 18.5) + .6; r((16 - hw).roundToInt(), y, (2 * hw).roundToInt(), 1, "b") }
            l(8, 9, 11, 6, "w"); ring(16.0, 23.0, 2.6, "a", .6); r(15, 23, 2, 1, "a")
        },
        "pot" to Arch(20, 12) {
            e(9.0, 10.0, 6.0, 3.0, "g"); e(23.0, 8.0, 6.0, 3.0, "g"); e(16.0, 5.0, 3.0, 5.5, "g"); l(4, 10, 13, 11, "g+"); l(19, 9, 28, 8, "g+"); l(16, 1, 16, 9, "g+"); r(15, 9, 2, 7, "g+")
            for (y in 19..29) { val hw = 9 - (y - 19) * .3; r((16 - hw).roundToInt(), y, (2 * hw).roundToInt(), 1, "b") }
            r(6, 15, 20, 4, "a"); r(6, 15, 20, 1, "a-"); r(6, 18, 20, 1, "a+"); r(9, 26, 14, 1, "b+")
        },
        "stone" to Arch(17, 10) {
            e(16.0, 20.5, 13.5, 9.0, "b"); var s = 5
            repeat(26) { k -> s = (s * 1103515245 + 12345) and 0x7FFFFFFF; val x = 4 + s % 24; s = (s * 1103515245 + 12345) and 0x7FFFFFFF; val y = 13 + s % 15; if (q(x, y) != null) p(x, y, if (k % 2 == 1) "b+" else "b-") }
            e(9.0, 12.5, 4.0, 1.8, "g"); e(13.0, 12.0, 2.0, 1.2, "g"); l(23, 14, 25, 18, "b+"); l(25, 18, 24, 21, "b+")
        },
        "cup" to Arch(14, 11) {
            e(27.0, 19.0, 4.5, 5.5, "b"); for (y in 0 until GH) for (x in 25 until GW) { val dx = (x + .5 - 27) / 2; val dy = (y + .5 - 19) / 3.2; if (dx * dx + dy * dy <= 1) p(x, y, null) }
            r(7, 10, 18, 20, "b"); p(7, 29, null); p(24, 29, null); r(7, 10, 18, 1, "b-"); r(8, 11, 16, 1, "T"); p(10, 11, "T-"); p(11, 11, "T-")
            r(7, 23, 18, 2, "a"); for (x in 9 until 24 step 4) p(x, 26, "a")
        },
        "bottle" to Arch(16, 11) {
            r(13, 0, 6, 4, "W"); p(13, 0, null); p(18, 0, null); r(13, 4, 6, 5, "G"); r(12, 4, 8, 1, "G-")
            r(11, 9, 10, 1, "G"); r(9, 10, 14, 1, "G"); r(8, 11, 16, 1, "G"); r(7, 12, 18, 18, "G"); p(7, 29, null); p(24, 29, null)
            r(8, 13, 16, 16, "b"); r(7, 15, 18, 9, "P"); r(7, 15, 18, 1, "a"); r(7, 23, 18, 1, "a"); r(20, 20, 2, 2, "C"); r(9, 12, 1, 2, "w"); r(9, 25, 1, 3, "w")
        },
        "knight" to Arch(6, 12) {
            KNIGHT.forEachIndexed { y, row -> row.forEachIndexed { x, ch -> if (ch != '.') r(x * 2, y * 2, 2, 2, if (ch == 'a') "a" else if (ch == 's') "b+" else "b") } }
            for (y in 2 until 19) { var xr = -1; for (x in 0 until GW) if (q(x, y) != null) xr = x; if (xr > 18) { p(xr, y, "a"); p(xr - 1, y, "a") } }
            p(5, 11, "n"); p(11, 3, "b+"); p(17, 3, "b+"); r(6, 22, 20, 1, "b-")
        },
        "sheet" to Arch(8, 11) {
            r(8, 3, 16, 27, "P"); p(8, 29, null); p(23, 29, null)
            for (y in 3..7) { for (x in 19 + (y - 3)..23) p(x, y, null); for (x in 19 until 19 + (y - 3)) p(x, y, "P+") }
            listOf(intArrayOf(10, 14, 16, 21), intArrayOf(10, 12, 14, 20), intArrayOf(10, 17, 19, 21), intArrayOf(10, 15)).forEachIndexed { i, s -> for (k in s.indices step 2) r(s[k], 17 + i * 3, s[k + 1] - s[k] + 1, 1, "a") }
            p(20, 25, "C"); p(19, 26, "C"); p(20, 26, "C"); p(21, 26, "C"); p(20, 27, "C")
        },
        "bell" to Arch(12, 11) {
            ring(16.0, 3.5, 2.4, "m", .8, true); e(16.0, 6.5, 3.0, 1.6, "m"); e(16.0, 18.0, 10.5, 13.0, "b")
            for (y in 23 until GH) for (x in 0 until GW) if (q(x, y) != null) p(x, y, null)
            r(5, 22, 22, 3, "a"); r(5, 22, 22, 1, "a-"); rb(0, 19, 32, 1, "b+"); r(15, 25, 2, 3, "m"); r(14, 28, 4, 1, "m"); l(9, 10, 8, 15, "w")
        },
        "lantern" to Arch(12, 10) { lantern("b+") },
        "spool" to Arch(11, 11) {
            r(8, 6, 16, 18, "b"); for (y in intArrayOf(7, 9, 18, 20, 22)) rb(8, y, 16, 1, "b+"); r(10, 6, 1, 18, "b-")
            for (y in intArrayOf(2, 24)) { r(5, y, 22, 4, "W"); p(5, y, null); p(26, y, null); p(5, y + 3, null); p(26, y + 3, null); r(6, y, 20, 1, "W-") }
            l(24, 12, 30, 5, "m"); p(30, 4, "m")
        },
        "camera" to Arch(5, 12, noMouth = true) {
            r(10, 3, 12, 7, "b"); r(10, 3, 12, 1, "b-"); r(3, 9, 26, 3, "m"); r(3, 9, 26, 1, "m-"); r(3, 12, 26, 15, "b")
            for (y in 14 until 25) for (x in 3 until 29) if ((x + y) % 2 == 0) p(x, y, "b+")
            r(23, 6, 3, 3, "a"); r(5, 7, 4, 2, "m"); r(4, 12, 4, 2, "G")
            e(16.0, 20.0, 7.0, 7.0, "m"); ring(16.0, 20.0, 6.0, "a", .5); e(16.0, 20.0, 4.8, 4.8, "D"); e(16.0, 20.0, 3.3, 3.3, "n"); p(13, 17, "w"); p(14, 17, "w"); p(13, 18, "w")
            r(1, 12, 2, 2, "m"); r(29, 12, 2, 2, "m")
        },
        "mat" to Arch(17, 9, 17) {
            r(4, 14, 21, 14, "b"); r(4, 14, 21, 1, "b-"); r(6, 14, 2, 14, "a"); r(20, 14, 2, 14, "a")
            e(25.0, 20.5, 5.5, 7.0, "b-"); ring(25.0, 20.5, 3.8, "b+", .6); ring(25.0, 20.5, 1.6, "b+", .6); for (y in 15 until 27 step 2) { p(2, y, "a"); p(3, y, "a") }
        },
        "ball" to Arch(14, 11) { e(16.0, 17.0, 12.5, 12.5, "b"); ring(3.0, 17.0, 10.0, "P", .6); ring(29.0, 17.0, 10.0, "P", .6) },
        "clock" to Arch(14, 11) {
            e(7.5, 7.0, 5.0, 4.0, "m"); e(24.5, 7.0, 5.0, 4.0, "m"); r(15, 1, 2, 5, "m"); e(16.0, 1.5, 2.0, 1.5, "m"); r(8, 27, 3, 3, "m"); r(21, 27, 3, 3, "m")
            e(16.0, 18.0, 12.0, 11.5, "b"); e(16.0, 18.0, 9.0, 8.7, "P"); for ((x, y) in listOf(15 to 10, 16 to 10, 24 to 17, 24 to 18, 15 to 26, 16 to 26, 7 to 17, 7 to 18)) p(x, y, "n"); r(15, 22, 2, 2, "a")
        },
        "obake" to Arch(12, 10, noArms = true) { lantern("a"); r(14, 19, 4, 11, "R"); r(15, 30, 2, 1, "R") },
        "tanuki" to Arch(13, 11, noMouth = true) {
            e(9.0, 7.0, 3.2, 3.2, "b"); e(23.0, 7.0, 3.2, 3.2, "b"); e(9.0, 7.0, 1.6, 1.6, "D"); e(23.0, 7.0, 1.6, 1.6, "D")
            e(27.0, 25.0, 4.5, 3.2, "b"); rb(28, 23, 1, 5, "D"); rb(30, 23, 1, 5, "D")
            e(16.0, 19.0, 11.0, 10.5, "b"); e(16.0, 23.0, 6.5, 6.0, "P"); e(11.0, 14.5, 3.6, 2.6, "D"); e(21.0, 14.5, 3.6, 2.6, "D"); e(16.0, 17.5, 1.8, 1.2, "n")
            e(16.0, 7.0, 3.2, 1.8, "g"); l(13, 7, 19, 7, "g+"); p(16, 5, "g+")
        },
        "kitsune" to Arch(14, 11, noMouth = true) {
            e(26.0, 18.0, 4.0, 8.0, "b"); e(27.0, 11.0, 2.6, 2.6, "a")
            for (y in 3..10) { val w = ((y - 3) * .8).roundToInt() + 1; r(8, y, w, 1, "b"); r(24 - w, y, w, 1, "b"); if (w > 2) { p(9, y, "a"); p(22, y, "a") } }
            e(16.0, 16.0, 10.0, 8.0, "b"); e(16.0, 20.0, 4.5, 3.2, "b"); r(15, 21, 2, 1, "n"); e(16.0, 26.0, 8.0, 5.0, "b")
            l(9, 17, 11, 19, "a"); l(22, 17, 20, 19, "a"); r(15, 9, 2, 3, "a")
        },
        "warashi" to Arch(11, 11) {
            r(10, 19, 12, 11, "b"); p(10, 29, null); p(21, 29, null); r(10, 23, 12, 2, "y"); for ((x, y) in listOf(12 to 21, 19 to 27, 13 to 27, 18 to 20)) p(x, y, "a")
            e(16.0, 12.0, 8.5, 8.0, "P"); e(16.0, 8.0, 9.0, 6.0, "n"); for (y in 10 until GH) for (x in 0 until GW) if (q(x, y) == "n") p(x, y, "P")
            r(7, 8, 3, 9, "n"); r(22, 8, 3, 9, "n"); r(9, 9, 14, 1, "n"); for (x in 14..17) p(x, 19, "P")
        },
    )

    fun has(arch: String) = arch in ARCH

    // ---------------------------------------------------------------- colour ramps

    private val ramps = HashMap<Int, IntArray>()

    /** Five steps from light to deep shadow, hue-shifted: warm lights, cool violet shadows. */
    fun ramp(hex: Int): IntArray = ramps.getOrPut(hex) {
        val (h, s, l) = hsl(hex)
        intArrayOf(
            fromHsl(toward(h, 55.0, 14.0), s * .9, l + .16), fromHsl(toward(h, 55.0, 7.0), s * .95, l + .08), fromHsl(h, s, l),
            fromHsl(toward(h, 250.0, 10.0), s * 1.05 + .03, l - .11), fromHsl(toward(h, 250.0, 20.0), s * 1.1 + .05, l - .22),
        )
    }

    private fun hsl(c: Int): Triple<Double, Double, Double> {
        val r = (c shr 16 and 255) / 255.0; val g = (c shr 8 and 255) / 255.0; val b = (c and 255) / 255.0
        val mx = max(r, max(g, b)); val mn = min(r, min(g, b)); val l = (mx + mn) / 2
        if (mx == mn) return Triple(0.0, 0.0, l)
        val d = mx - mn
        val s = if (l > .5) d / (2 - mx - mn) else d / (mx + mn)
        val h = when (mx) { r -> (g - b) / d + (if (g < b) 6 else 0); g -> (b - r) / d + 2; else -> (r - g) / d + 4 } * 60
        return Triple(h, s, l)
    }

    private fun fromHsl(h0: Double, s0: Double, l0: Double): Int {
        val h = ((h0 % 360) + 360) % 360; val s = s0.coerceIn(0.0, 1.0); val l = l0.coerceIn(0.0, 1.0)
        val c = (1 - abs(2 * l - 1)) * s; val x = c * (1 - abs((h / 60) % 2 - 1)); val m = l - c / 2
        val (r, g, b) = when { h < 60 -> Triple(c, x, 0.0); h < 120 -> Triple(x, c, 0.0); h < 180 -> Triple(0.0, c, x); h < 240 -> Triple(0.0, x, c); h < 300 -> Triple(x, 0.0, c); else -> Triple(c, 0.0, x) }
        return (((r + m) * 255).roundToInt() shl 16) or (((g + m) * 255).roundToInt() shl 8) or ((b + m) * 255).roundToInt()
    }

    private fun toward(h: Double, t: Double, amt: Double): Double { val d = ((t - h + 540) % 360) - 180; return h + sign(d) * min(abs(d), amt) }

    private fun mixRgb(a: Int, b: Int, t: Double): Int {
        fun one(sh: Int) = (((a shr sh) and 255) * (1 - t) + ((b shr sh) and 255) * t).roundToInt()
        return (one(16) shl 16) or (one(8) shl 8) or one(0)
    }

    // ---------------------------------------------------------------- the sprite

    enum class Eyes { OPEN, CLOSED, HAPPY }

    /** One finished frame: ARGB pixels in a 32×44 box and where its feet are. */
    class Sprite(val px: IntArray, val foot: Int, val top: Int)

    data class Look(
        val arch: String, val pal: String, val stage: Int, val rank: Int = 0, val eyes: Eyes = Eyes.OPEN,
        val talk: Boolean = false, val legs: Int = 0, val wave: Boolean = false, val sprout: Boolean = false,
    )

    private val cache = HashMap<Look, Sprite>()

    fun get(look: Look): Sprite = synchronized(cache) { cache.getOrPut(look) { build(look) } }

    private fun build(o: Look): Sprite {
        val A = ARCH[o.arch] ?: ARCH.getValue("box")
        val pal = PALS[o.pal] ?: PALS.getValue("wood")
        val t = Grid().apply { A.draw(this) }
        val g = Grid()
        for (i in 0 until GW * (GH - OY)) if (t.c[i] != null) g.c[i + OY * GW] = t.c[i]
        val fy = A.face + OY
        val lx = A.lx
        val rx = A.rx
        val rank = if (o.stage >= 5) o.rank else 0
        fun span(y: Int): Pair<Int, Int> { var l = -1; var r = -1; for (x in 0 until GW) if (g.q(x, y) != null) { if (l < 0) l = x; r = x }; return l to r }
        fun top(): Int { for (i in 0 until GN) if (g.c[i] != null) return i / GW; return 0 }

        if (rank >= 1) {
            val sy = fy + 6
            for (y in intArrayOf(sy, sy + 1)) for (x in 0 until GW) { val v = g.q(x, y); if (v != null && v[0] !in FLAT) g.p(x, y, "C") }
            val r = span(sy).second
            if (r >= 0) { g.p(r + 1, sy + 1, "C"); g.p(r + 1, sy + 2, "C"); g.p(r + 2, sy + 2, "C+"); g.p(r + 2, sy + 3, "C+") }
        }
        if (o.stage >= 5 && !A.noArms) {
            val ay = fy + 8
            val (l, r) = span(ay)
            if (l >= 0) {
                g.p(l - 1, ay, "b"); g.p(l - 2, ay + 1, "b"); g.p(l - 1, ay + 1, "b")
                if (o.wave) { g.p(r + 1, ay, "b"); g.p(r + 1, ay - 1, "b"); g.p(r + 2, ay - 2, "b"); g.p(r + 2, ay - 3, "b") }
                else { g.p(r + 1, ay, "b"); g.p(r + 2, ay + 1, "b"); g.p(r + 1, ay + 1, "b") }
            }
        }
        if (rank == 3) { val tp = top(); intArrayOf(2, 6, 10, 14, 18).forEachIndexed { k, w -> g.r(16 - w / 2, tp - 5 + k, w, 1, if (k == 4) "W+" else if (k % 2 == 1) "W-" else "W") } }
        if (o.sprout) { val tp = top(); g.p(16, tp - 1, "g"); g.p(16, tp - 2, "g"); g.p(15, tp - 3, "g"); g.p(14, tp - 3, "g"); g.p(17, tp - 3, "g"); g.p(18, tp - 4, "g") }
        var x0 = GW; var x1 = 0; var y0 = GH; var y1 = 0
        fun bbox() { x0 = GW; x1 = 0; y0 = GH; y1 = 0; for (i in 0 until GN) if (g.c[i] != null) { val x = i % GW; val y = i / GW; x0 = min(x0, x); x1 = max(x1, x); y0 = min(y0, y); y1 = max(y1, y) } }
        bbox()
        if (rank >= 2) {
            var s = o.arch.hashCode() * 31 + o.pal.hashCode()
            var n = 0; var k = 0
            while (k < 300 && n < 10) {
                s = (s * 1103515245 + 12345) and 0x7FFFFFFF; val x = x0 + s % (x1 - x0 + 1)
                s = (s * 1103515245 + 12345) and 0x7FFFFFFF; val y = y0 + s % (y1 - y0 + 1)
                val v = g.q(x, y); if (v != null && v[0] == 'b' && (y < fy - 2 || y > fy + 5)) { g.p(x, y, "b+"); n++ }; k++
            }
        }
        bbox()
        val bw = x1 - x0 + 1; val bh = y1 - y0 + 1
        fun dist(x: Int, y: Int, dx: Int, dy: Int): Int { for (k in 1..3) if (g.q(x + dx * k, y + dy * k) == null) return k; return 4 }
        fun hex(m: Char): Int = when (m) { 'b' -> pal.b; 'a' -> pal.a; else -> BASES[m] ?: pal.b }

        val col = IntArray(GN) { -1 }
        for (i in 0 until GN) {
            val s = g.c[i] ?: continue
            val x = i % GW; val y = i / GW; val m = s[0]; val md = if (s.length > 1) s[1] else ' '
            FLAT[m]?.let { col[i] = it; continue }
            val dd = dist(x, y, 0, 1); val dr = dist(x, y, 1, 0); val du = dist(x, y, 0, -1); val dl = dist(x, y, -1, 0)
            val sh = (if (dd == 1) 2 else if (dd == 2) 1 else 0) + (if (dr == 1) 1 else 0)
            var lv = when {
                sh > 0 -> 2 + min(2, sh)
                du == 1 && dl <= 2 -> 0
                du == 1 || dl == 1 -> 1
                (x - x0) + (y - y0) < (bw + bh) * .3 -> 1
                else -> 2
            }
            if (md == '+') lv = min(4, lv + 1) else if (md == '-') lv = max(0, lv - 1)
            col[i] = ramp(hex(m))[lv]
        }
        val out = col.copyOf()
        val nb = arrayOf(intArrayOf(0, 1), intArrayOf(1, 0), intArrayOf(0, -1), intArrayOf(-1, 0))
        for (i in 0 until GN) {
            if (g.c[i] != null) continue
            val x = i % GW; val y = i / GW
            var k = -1; var s: String? = null
            for (j in nb.indices) { val v = g.q(x + nb[j][0], y + nb[j][1]); if (v != null) { k = j; s = v; break } }
            if (s == null) continue
            val deep = if (s[0] in FLAT) 0x2a2030 else ramp(hex(s[0]))[4]
            out[i] = mixRgb(deep, 0x180e18, if (k == 0) .42 else .62)
        }
        fun on(x: Int, y: Int, c: Int) { if (g.q(x, y) != null) out[y * GW + x] = c }

        if (o.arch == "obake") {
            if (o.eyes == Eyes.CLOSED) for (x in 13..18) on(x, fy + 1, EYE)
            else {
                for (y in -2..2) for (x in -3..2) if ((x + .5) * (x + .5) / 9 + y * y / 6.25 <= 1) on(16 + x, fy + y, 0xffffff)
                on(16, fy, EYE); on(17, fy, EYE); on(16, fy + 1, EYE); on(17, fy + 1, EYE)
            }
            for (x in 11..20) on(x, fy + 6, EYE); on(11, fy + 5, EYE); on(20, fy + 5, EYE)
        } else if (o.stage >= 2) {
            fun open(ex: Int) { for (r in 0..2) { on(ex, fy + r, EYE); on(ex + 1, fy + r, EYE) }; on(ex + 1, fy, 0xffffff) }
            fun shut(ex: Int) { on(ex - 1, fy + 1, EYE); on(ex, fy + 2, EYE); on(ex + 1, fy + 2, EYE); on(ex + 2, fy + 1, EYE) }
            fun happy(ex: Int) { on(ex - 1, fy + 2, EYE); on(ex, fy + 1, EYE); on(ex + 1, fy + 1, EYE); on(ex + 2, fy + 2, EYE) }
            val f: (Int) -> Unit = when (o.eyes) { Eyes.CLOSED -> ::shut; Eyes.HAPPY -> ::happy; Eyes.OPEN -> ::open }
            f(lx); if (o.stage >= 3) f(rx) else shut(rx)
            if (o.stage >= 3) {
                fun bl(x: Int): Boolean { if (g.q(x, fy + 3) != null && g.q(x + 1, fy + 3) != null) { on(x, fy + 3, BLUSH); on(x + 1, fy + 3, BLUSH); return true }; return false }
                if (!bl(lx - 2)) bl(lx - 1)
                if (!bl(rx + 2)) bl(rx + 1)
            }
            if (rank >= 2) { for (x in lx - 1..lx + 1) on(x, fy - 2, BROW); for (x in rx..rx + 2) on(x, fy - 2, BROW); on(lx - 1, fy - 1, BROW); on(rx + 2, fy - 1, BROW) }
            if (!A.noMouth && o.stage >= 3) {
                val c0 = ((lx + rx + 2) / 2.0).roundToInt() - 1
                if (o.talk) { on(c0, fy + 4, EYE); on(c0 + 1, fy + 4, EYE); on(c0, fy + 5, TONGUE); on(c0 + 1, fy + 5, TONGUE) }
                else { on(c0 - 1, fy + 3, EYE); on(c0, fy + 4, EYE); on(c0 + 1, fy + 4, EYE); on(c0 + 2, fy + 3, EYE) }
            }
        }
        if (rank >= 4) {
            val tp = top()
            for (x in -6..6) {
                val yy = (sqrt(max(0.0, 1 - x * x / 36.0)) * 1.6).roundToInt()
                for (y in intArrayOf(tp - 4 - yy, tp - 4 + yy)) if (y >= 0) out[y * GW + 16 + x] = HALO
            }
        }
        if (o.legs > 0) {
            var by = 0
            for (i in 0 until GN) if (out[i] != -1) by = i / GW
            fun lp(x: Int, y: Int) { if (x in 0 until GW && y in 0 until GH) out[y * GW + x] = LEGS }
            fun leg(x: Int, n: Int) { for (r in 1..n) { lp(x, by + r); lp(x + 1, by + r) } }
            if (o.legs == 1) { leg(11, 3); leg(19, 3); lp(10, by + 3); lp(21, by + 3) } else { leg(11, 2); lp(10, by + 2); leg(19, 3); lp(21, by + 3) }
        }
        var last = 0; var first = GH
        val px = IntArray(GN) { i -> val v = out[i]; if (v == -1) 0 else { last = i / GW; if (first == GH) first = i / GW; v or (0xFF shl 24) } }
        return Sprite(px, last + 1, first)
    }
}

/** Paints a sprite into the buffer with its feet at (x, foot). */
fun Buf.sprite(s: Sprites.Sprite, x: Int, foot: Int, alpha: Float = 1f) {
    val top = foot - s.foot
    for (j in 0 until Sprites.GH) {
        val y = top + j
        if (y < 0 || y >= h) continue
        for (i in 0 until Sprites.GW) {
            val c = s.px[j * Sprites.GW + i]
            if (c ushr 24 != 0) p(x + i, y, c, alpha)
        }
    }
}
