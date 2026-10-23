package app.tsukumo.ui.art

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

enum class Season { SPRING, SUMMER, AUTUMN, WINTER }

/**
 * The house, 360×320 art pixels (3× on a 1080-wide phone, 4× on 1440): a room with a tokonoma and its
 * hundred candles, shelves of sleeping things, the kotatsu, the tansu, a paper lamp; on the right the
 * shoji is slid open onto the engawa and a garden with a mountain, a stone lantern, a pond and a tree.
 */
object House {
    const val W = 360
    const val H = 320
    /** Where the wall meets the floor. */
    const val FLOOR = 136
    val SHELF = intArrayOf(62, 96, 130)
    val CROWS = intArrayOf(87, 96, 105, 114, 123)
    fun candleX(i: Int, r: Int) = 11 + i * 3 + (r % 2)

    /** Everything that changes the still picture. */
    data class Key(val season: Season, val night: Boolean, val motif: Int, val tree: Int, val shimenawa: Boolean, val gold: Boolean)

    class Look(val sky: Int, val bot: Int, val hill: Int, val ground: Int, val groundD: Int, val groundL: Int, val canopy: IntArray?)

    fun look(s: Season) = when (s) {
        Season.SPRING -> Look(0x8fc8ec, 0xe4f1f0, 0x8fbf8a, 0x86b860, 0x6f9f4c, 0xa0cc78, intArrayOf(0xf4b6c6, 0xfde2ea, 0xd98fa8))
        Season.SUMMER -> Look(0x5fb0e8, 0xcdeaf6, 0x4f8f4a, 0x5f9e45, 0x4b8536, 0x7cb85a, intArrayOf(0x5f9e47, 0x8bc663, 0x437a33))
        Season.AUTUMN -> Look(0xe8b585, 0xf6e2c4, 0xb8743a, 0xa88c4c, 0x8f753c, 0xc2a564, intArrayOf(0xe0622c, 0xf4a440, 0xb23e1d))
        Season.WINTER -> Look(0xb5c6d8, 0xeaf0f4, 0xd4dee8, 0xf1f4f7, 0xcfd9e4, 0xffffff, null)
    }

    // --------------------------------------------------------------------------- the still picture

    fun base(k: Key): Buf {
        val b = Buf(W, H)
        b.room(k.season, k.motif)
        b.garden(k.season, k.night, k.tree / 25.0, k.shimenawa)
        b.eave(k.season)
        b.shoji(k.night)
        b.floor()
        if (k.night) {
            for (i in b.d.indices) b.d[i] = night(b.d[i])
            // The stone lantern glows only when its wonder has come; the shoji keeps a little of the lamp.
            for (y in 22 until 112) for (x in 268 until 290) b.scr(x, y, 0xf0c070, .18f)
        } else {
            // A sunbeam through the open shoji.
            for (y in 22 until H) {
                val t = (y - 22) / (H - 22.0)
                val x0 = 294 + (180 - 294) * t; val x1 = 352 + (290 - 352) * t
                for (x in x0.roundToInt() until x1.roundToInt()) b.scr(x, y, 0xffe2a0, .09f)
            }
        }
        if (k.gold) for (y in 0 until H) for (x in 0 until W) b.scr(x, y, 0xffc860, .06f)
        return b
    }

    private fun Buf.scroll(season: Season, motif: Int) {
        val ink = 0x2b2226
        line(41, 30, 33, 35, 0x3a2a20); line(41, 30, 49, 35, 0x3a2a20)
        rect(30, 35, 22, 2, 0x4a2e1c); rect(29, 35, 1, 2, 0xc99a3a); rect(52, 35, 1, 2, 0xc99a3a)
        rect(31, 37, 20, 39, 0x46597e); speck(31, 37, 20, 39, 0x5468a0, .18, 11); speck(31, 37, 20, 39, 0x3a4b6e, .1, 12)
        rect(34, 40, 14, 32, 0xf1e8d2); speck(34, 40, 14, 32, 0xe6dabf, .12, 13)
        val cx = 41.0; val cy = 54.0
        when (motif % 4) {
            0 -> for (k in 0..64) { val a = -1.2 + k / 64.0 * (PI * 2 - .7); val x = cx + cos(a) * 5.5; val y = cy + sin(a) * 6.0; px(x, y, ink); if (k < 40) px(x + (if (cos(a) > 0) -1 else 1), y, ink) }
            1 -> { line(35, 64, 41, 49, ink); line(41, 49, 47, 64, ink); for (y in 49 until 55) { val w = (y - 49) * .36; rect(41 - w, y, 2 * w + 1, 1, 0xfbf7ee) }; blob(45.0, 46.0, 1.6, 1.6, 0xc4413a) }
            2 -> { blob(cx, 52.0, 5.0, 5.0, 0xe8d8a8); for (k in 0 until 30) { val a = k / 30.0 * PI * 1.3 + 2.2; px(cx + cos(a) * 5, 52 + sin(a) * 5, ink) }; for (k in 0 until 4) for (y in 0 until 8) px(36 + k * 3 + sin(y * .3 + k) * 1.2, 70.0 - y, ink) }
            else -> { thick(37.0, 69.0, 41.0, 64.0, 1, ink); thick(41.0, 64.0, 38.0, 57.0, 1, ink); thick(38.0, 57.0, 43.0, 51.0, 1, ink)
                for ((x, y, w) in listOf(Triple(40, 51, 4), Triple(45, 57, 4), Triple(36, 61, 3), Triple(44, 64, 3))) { blob(x.toDouble(), y.toDouble(), w.toDouble(), 1.5, 0x3b3a36) } }
        }
        val mark = when (season) { Season.SPRING -> 0xee9fb5; Season.SUMMER -> 0x5f9e47; Season.AUTUMN -> 0xc4452a; Season.WINTER -> 0x8fb2cf }
        for ((x, y) in listOf(36 to 65, 38 to 67, 36 to 69)) rect(x, y, 1, 1, mark)
        for (y in 42 until 52 step 2) rect(46, y, 1, 1, ink)
        rect(45, 68, 2, 2, 0xc4413a)
        rect(30, 76, 22, 2, 0x4a2e1c); rect(29, 76, 1, 2, 0xc99a3a); rect(52, 76, 1, 2, 0xc99a3a)
    }

    private fun Buf.room(season: Season, motif: Int) {
        grainH(0, 0, W, 6, 0x4a3022, 0x3a2418, 0x5a3a28, 1); rect(0, 5, W, 1, 0x2a1810)
        rect(0, 6, 262, FLOOR - 6, 0xe6d5b0); speck(0, 6, 262, FLOOR - 6, 0xd9c6a0, .07, 2); speck(0, 6, 262, FLOOR - 6, 0xefe2c2, .05, 3)
        // the transom lattice
        rect(5, 6, 252, 14, 0x3a2618)
        var x = 6
        while (x < 256) { rect(x, 7, 6, 12, 0xb98a5a); rect(x + 1, 8, 4, 10, 0x3a2618); rect(x + 2, 8, 2, 10, 0xa87a4c); rect(x + 1, 12, 4, 2, 0xa87a4c); px(x + 2, 8, 0xc99a68); x += 8 }
        grainH(0, 20, 262, 6, 0x8a5d3b, 0x6e4529, 0xa07048, 4); rect(0, 20, 262, 1, 0xb08058); rect(0, 25, 262, 1, 0x4a2e1c)
        for (cx in intArrayOf(48, 130, 212)) { blob(cx.toDouble(), 23.0, 1.6, 1.6, 0xc99a3a); px(cx - 1, 22, 0xf0d080) }
        rect(0, 26, 262, 2, 0x503420, .16f)
        // tokonoma
        rect(8, 28, 66, 96, 0xd4bf93); speck(8, 28, 66, 96, 0xc8b285, .07, 5); speck(8, 28, 66, 96, 0xddc9a0, .04, 6)
        rect(8, 30, 66, 2, 0x3c2814, .22f); rect(8, 30, 3, 94, 0x3c2814, .18f)
        grainH(7, 26, 68, 4, 0x5e3d27, 0x4a2e1c, 0x70482e, 7)
        scroll(season, motif)
        rect(9, 80, 1, 44, 0x2a1e1a); rect(72, 80, 1, 44, 0x2a1e1a)
        CROWS.forEachIndexed { r, y ->
            rect(9, y, 64, 2, 0x2a1e1a); rect(9, y, 64, 1, 0x4f3f36)
            for (i in 0 until 20) { val cx = candleX(i, r); rect(cx, y - 3, 2, 3, 0xf3e6c8); rect(cx + 1, y - 3, 1, 3, 0xd9c9a6); px(cx, y - 4, 0x3a2a20) }
        }
        rect(8, 125, 66, 2, 0xcfc68c); rect(8, 125, 66, 1, 0xe0d8a0); rect(7, 127, 68, 9, 0x2a1e1a); rect(7, 127, 68, 1, 0x6a5a52); rect(7, 128, 68, 1, 0x3e302a)
        grainV(74, 26, 6, FLOOR - 26, 0xb7895a, 0x9a6e44, 0xcfa070, 8); rect(74, 26, 1, FLOOR - 26, 0x7a5232); rect(79, 26, 1, FLOOR - 26, 0x8a6040)
        rect(80, 133, 176, 3, 0x6e4529); rect(80, 133, 176, 1, 0x8a5d3b)
        for (s in SHELF) {
            grainH(84, s, 172, 4, 0xa8774c, 0x8a5d3b, 0xc08a5a, 9 + s); rect(84, s, 172, 1, 0xc99a68); rect(84, s + 3, 172, 1, 0x6e4529)
            rect(84, s + 4, 172, 2, 0x3c2814, .2f)
            if (s < 130) for (bx in intArrayOf(88, 248)) { rect(bx, s + 4, 4, 2, 0x7a5232); rect(bx + 1, s + 6, 2, 2, 0x7a5232) }
        }
        grainV(0, 6, 5, FLOOR - 6, 0x5e3d27, 0x4a2e1c, 0x70482e, 10); grainV(256, 6, 6, FLOOR - 6, 0x5e3d27, 0x4a2e1c, 0x70482e, 11)
        rect(4, 6, 1, FLOOR - 6, 0x3a2418); rect(256, 6, 1, FLOOR - 6, 0x7a5232)
    }

    private val STARS = List(26) { i -> Triple(293 + (hash(91, i) * 66).toInt(), 22 + (hash(92, i) * 60).toInt(), hash(93, i) * 6) }

    private fun Buf.garden(season: Season, night: Boolean, f: Double, shimenawa: Boolean) {
        val L = look(season)
        val gx = 292
        if (night) { grad(gx, 20, 68, 100, 0x0e1334, 0x2c3566, 6); for ((x, y) in STARS.map { it.first to it.second }) px(x, y, 0x8a90c0) }
        else grad(gx, 20, 68, 100, L.sky, L.bot, 6)
        // the mountain
        val ml = if (season == Season.AUTUMN) 0x8a8fb0 else 0x8296bb
        val md = if (season == Season.AUTUMN) 0x737a9c else 0x6e83a8
        for (y in 44..98) { val hw = ((y - 44) * 1.25 + 1.5).roundToInt(); clipRect(328 - hw, y, hw, ml); clipRect(328, y, hw + 1, md) }
        for (y in 44..70) { val hw = ((y - 44) * 1.25 + 1.5).roundToInt(); for (x in -hw..hw) { val lim = (if (season == Season.WINTER) 66 else 57) + (sin(x * .9) * 1.5 + cos(x * .37) * 2.5).roundToInt(); if (y < lim && 328 + x >= gx) px(328 + x, y, if (x < 0) 0xf4f7fb else 0xd4dcea) } }
        val fc = L.hill; val fd = mixc(L.hill, 0x203020, .3f)
        for (x in gx until W) { val tp = 88 + (sin(x * .21) * 2 + sin(x * .07) * 3 + sin(x * .53) * 1.2).roundToInt(); rect(x, tp, 1, 40, fc); if (x % 5 == 0) px(x, tp + 2, fd) }
        // bamboo
        val bam = if (season == Season.WINTER) 0x8aa070 else 0x7aa34a
        for ((bx, top) in listOf(352 to 20, 357 to 26, 346 to 34)) {
            rect(bx, top, 2, 120, bam); rect(bx, top, 1, 120, 0x9cc266); var y = top + 8; while (y < FLOOR) { rect(bx, y, 2, 1, 0x55782f); y += 11 }
            for (k in 0 until 5) { val ly = top + 3 + k * 4.0; thick(bx.toDouble(), ly, bx - 4.0 - k, ly + 2, 1, if (season == Season.WINTER) 0xe8eef2 else 0x5f8f3a); thick(bx + 1.0, ly + 1, bx + 6.0, ly + 3, 1, if (season == Season.WINTER) 0xdfe7ee else 0x6f9f45) }
        }
        // a woven bamboo fence
        var x = gx + 1
        while (x < W) { rect(x, 94, 2, 24, 0xc9b26a); rect(x, 94, 1, 24, 0xe0cc88); px(x + 1, 101, 0xa08848); px(x + 1, 110, 0xa08848); if (season == Season.WINTER) rect(x, 93, 2, 1, 0xffffff); x += 7 }
        for (y in intArrayOf(99, 106, 113)) { rect(gx, y, 68, 2, 0xc9b26a); rect(gx, y + 1, 68, 1, 0xa08848); if (season == Season.WINTER) rect(gx, y - 1, 68, 1, 0xffffff); var t = gx + 2; while (t < W) { px(t, y - 1, 0x2a2020); px(t + 1, y, 0x2a2020); t += 7 } }
        // the ground
        rect(gx, 116, 68, 20, L.ground); speck(gx, 116, 68, 20, L.groundD, .12, 40); speck(gx, 116, 68, 20, L.groundL, .06, 41); rect(gx, 116, 68, 1, L.groundD)
        // the pond
        blob(330.0, 128.0, 23.0, 6.0, 0x8a877c); blob(330.0, 127.0, 23.0, 5.0, 0xa19e92)
        blob(330.0, 128.0, 20.0, 4.0, if (season == Season.WINTER) 0xcfe2ee else 0x4f86a8)
        if (season == Season.WINTER) { rect(318, 127, 7, 1, 0xffffff); rect(334, 129, 8, 1, 0xffffff) }
        else { rect(318, 126, 6, 1, 0x7fb0cc); rect(336, 129, 7, 1, 0x7fb0cc); if (season != Season.AUTUMN) { blob(322.0, 129.0, 2.0, 1.0, 0x5a8a3a); px(341, 126, 0xf2a6c0) } }
        tree(season, f, shimenawa)
        // the stone lantern
        val st = 0xa3a198; val sd = 0x7d7b73; val sl = 0xc2c0b6
        rect(296, 128, 15, 4, sd); rect(296, 128, 15, 1, sl); rect(301, 117, 5, 11, st); rect(301, 117, 1, 11, sl); rect(305, 117, 1, 11, sd)
        rect(295, 113, 17, 4, st); rect(295, 113, 17, 1, sl); rect(297, 105, 13, 8, st); rect(297, 105, 1, 8, sl); rect(300, 107, 7, 4, 0x3d3b37)
        for (y in 98 until 105) { val hw = 3 + (y - 98) * 1.1; rect(303.5 - hw, y, 2 * hw, 1, if (y < 100) sl else st) }
        rect(293, 104, 21, 1, sd); blob(303.5, 95.0, 2.0, 2.5, st)
        if (season == Season.WINTER) { for (y in 97 until 100) { val hw = 3 + (y - 97) * 1.1; rect(303.5 - hw, y, 2 * hw, 1, 0xffffff) } } else { px(296, 103, 0x6f9a52); px(310, 102, 0x6f9a52) }
    }

    private fun Buf.clipRect(x: Int, y: Int, w: Int, c: Int) { val a = max(292, x); val b = minOf(W, x + w); if (b > a) r(a, y, b - a, 1, c) }

    private val CLUST = listOf(doubleArrayOf(-14.0, 0.0, 13.0, 7.0), doubleArrayOf(6.0, -8.0, 16.0, 9.0), doubleArrayOf(20.0, 1.0, 10.0, 6.0), doubleArrayOf(-6.0, 13.0, 10.0, 5.0), doubleArrayOf(14.0, 14.0, 9.0, 5.0))

    private fun Buf.tree(season: Season, f: Double, shimenawa: Boolean) {
        val tr = 0x5a3d2c; val tdk = 0x3e2a1e; val tl = 0x7a5640
        val crown = FLOOR - (FLOOR - 50) * f
        val cx = 340
        for (y in (crown + 4).roundToInt() until FLOOR) {
            val w = (((if (y > 118) 6 else if (y > 90) 5 else 4) * minOf(1.0, f + .25))).roundToInt().coerceAtLeast(2)
            val xx = cx + (sin(y * .05) * 1.5).roundToInt()
            rect(xx - w / 2, y, w, 1, tr); px(xx - w / 2, y, tl); px(xx + (w + 1) / 2 - 1, y, tdk)
        }
        val br = listOf(doubleArrayOf(0.0, 20.0, -18.0, 4.0), doubleArrayOf(0.0, 10.0, 18.0, -6.0), doubleArrayOf(0.0, 30.0, 22.0, 18.0), doubleArrayOf(-9.0, 12.0, -26.0, 9.0))
        for (b in br) thick(cx + b[0] * f, crown + b[1] * f, cx + b[2] * f, crown + b[3] * f, max(1, (2 * f).roundToInt()), tr)
        val can = look(season).canopy
        if (can != null) {
            var k = 0
            for (c in CLUST) {
                val ccx = cx + c[0] * f; val ccy = crown + c[1] * f; val rx = c[2] * f; val ry = c[3] * f
                blob(ccx, ccy + 1, rx, ry, can[2]); blob(ccx, ccy, rx, ry * .9, can[0])
                val n = (rx * ry * .9).toInt()
                for (i in 0 until n) {
                    val a = hash(77, k++) * PI * 2; val q = sqrt(hash(78, k++))
                    px(ccx + cos(a) * q * rx, ccy + sin(a) * q * ry, if (sin(a) > .25) can[2] else if (cos(a) + sin(a) < -.5) can[1] else if (hash(79, k) < .5) can[0] else can[1])
                }
            }
            for (i in 0 until 18) px(293 + hash(80, i) * 66, 118 + hash(81, i) * 17, can[0])
        } else for (b in br) thick(cx + b[0] * f, crown + b[1] * f - 1, cx + b[2] * f, crown + b[3] * f - 1, 1, 0xffffff)
        if (shimenawa) {
            val y = 118
            for (x in cx - 5..cx + 5) { px(x, y + (sin(x * 1.3)).roundToInt(), 0xd9b878); px(x, y + 1 + (sin(x * 1.3)).roundToInt(), 0xb8955a) }
            for (sx in intArrayOf(cx - 3, cx + 2)) for (k in 0 until 5) px(sx + k % 2, y + 3 + k, 0xffffff)
        }
    }

    private fun Buf.eave(season: Season) {
        rect(262, 6, 98, 10, 0x3a2618)
        var x = 263; while (x < W) { rect(x, 6, 4, 10, 0x5e3d27); rect(x, 6, 1, 10, 0x70482e); rect(x, 14, 4, 2, 0x8a5d3b); x += 7 }
        rect(262, 16, 98, 4, 0x5f666f)
        x = 262; while (x < W) { blob(x + 3.0, 18.0, 3.0, 2.0, 0x7d858f); px(x + 1, 16, 0xa3acb6); px(x + 2, 16, 0xa3acb6); x += 7 }
        rect(262, 19, 98, 1, 0x3a4048)
        if (season == Season.WINTER) { rect(262, 14, 98, 2, 0xf4f7fb); x = 263; while (x < W) { px(x, 16, 0xffffff); x += 4 } }
    }

    private fun Buf.shoji(night: Boolean) {
        rect(262, 20, 4, FLOOR - 20, 0x4a2e1c)
        rect(266, 20, 26, FLOOR - 20, 0x6e4529)
        rect(268, 22, 22, 90, if (night) 0xefd9a8 else 0xf2ead8); speck(268, 22, 22, 90, if (night) 0xe6cc96 else 0xe8dfca, .05, 50)
        if (!night) for (k in 0 until 18) { val t = hash(51, k); rect(288 - t * 20, 26 + t * 40 + hash(52, k) * 8, 1 + (hash(53, k) * 3).toInt(), 1, 0x787064, .16f) }
        for (x in intArrayOf(275, 282)) rect(x, 22, 1, 90, 0xa8865a)
        var y = 31; while (y < 112) { rect(268, y, 22, 1, 0xa8865a); y += 10 }
        grainH(268, 112, 22, 22, 0x8a5d3b, 0x6e4529, 0xa07048, 52); rect(268, 112, 22, 1, 0x5e3d27); rect(290, 20, 2, FLOOR - 20, 0x4a2e1c)
    }

    private fun Buf.floor() {
        rect(0, FLOOR, 262, H - FLOOR, 0xcdc48a)
        var y = FLOOR; while (y < H) { rect(0, y, 262, 1, 0xc2b87c); y += 2 }
        speck(0, FLOOR, 262, H - FLOOR, 0xd8d09a, .05, 21); speck(0, FLOOR, 262, H - FLOOR, 0xb9ae72, .04, 22)
        fun heri(x: Int, y: Int, w: Int, h: Int) { rect(x, y, w, h, 0x2f3a2c); if (w > h) { var i = x + 1; while (i < x + w) { px(i, y + 1, 0x4b5a45); i += 4 } } else { var j = y + 1; while (j < y + h) { px(x + 1, j, 0x4b5a45); j += 4 } } }
        heri(0, 226, 262, 3); heri(129, FLOOR, 3, 90); heri(86, 229, 3, 91); heri(174, 229, 3, 91)
        grainV(262, FLOOR, 4, H - FLOOR, 0x6e4529, 0x5a3820, 0x8a5d3b, 23); rect(263, FLOOR, 1, H - FLOOR, 0x3a2418)
        for (i in 0 until 21) {
            val yy = FLOOR + i * 9
            val c = intArrayOf(0xb48654, 0xad7f4d, 0xb98b58)[i % 3]
            grainH(266, yy, 94, 9, c, 0x987044, 0xc49a66, 30 + i); rect(266, yy, 94, 1, 0xc9a06a); rect(266, yy + 8, 94, 1, 0x7a5536)
            val jx = 266 + (i * 37) % 80 + 6; rect(jx, yy, 1, 8, 0x7a5536); px(jx + 2, yy + 3, 0x5e3d27)
        }
        // a cushion to watch the garden from
        rect(271, 204, 34, 4, 0x2a3c5e); rect(272, 197, 32, 8, 0x3d5582); rect(273, 196, 30, 1, 0x5a74a8); rect(272, 198, 1, 6, 0x4a64a0); rect(303, 198, 1, 6, 0x33497a)
        blob(288.0, 201.0, 1.6, 1.0, 0xe8c35a); for ((xx, yy) in listOf(271 to 196, 304 to 196, 271 to 206, 304 to 206)) px(xx, yy, 0xe8c35a)
    }

    // --------------------------------------------------------------------------- furniture layers

    private fun Buf.futon() {
        val b = 0x9c4f73; val l = 0xb8678d; val d = 0x7d3a5c; val dd = 0x5e2a45
        rect(78, 215, 168, 38, b)
        for (x in 78 until 246) { val k = (sin(x * .13) * 1.2).roundToInt() + 2; rect(x, 253, 1, k, b); px(x, 253 + k, dd) }
        var y = 219; while (y < 252) { var x = 82; while (x < 244) { val ox = if ((y / 7) % 2 == 1) 3 else 0; px(x + ox, y, l); px(x + ox - 1, y + 1, l); px(x + ox + 1, y + 1, l); px(x + ox, y + 2, l); x += 7 }; y += 7 }
        y = 230; while (y < 252) { var x = 80; while (x < 244) { px(x, y, d); x += 3 }; y += 11 }
        rect(78, 215, 3, 41, d); rect(243, 215, 3, 41, d); rect(78, 215, 168, 2, dd)
    }

    private fun Buf.kotatsu() {
        futon()
        grainH(84, 207, 156, 5, 0xa26b40, 0x87552f, 0xb8804f, 60); rect(84, 207, 156, 1, 0xc49060); rect(84, 212, 156, 4, 0x6e4529); rect(84, 212, 156, 1, 0x8a5d3b); rect(84, 215, 156, 1, 0x3a2418)
        // a clay teapot, two cups, a bowl of mandarins
        blob(104.0, 201.0, 7.0, 4.5, 0xb0552f); blob(104.0, 203.0, 6.0, 2.0, 0x8e3f1f); rect(99, 198, 3, 1, 0xcf7a52); blob(104.0, 196.0, 4.0, 1.5, 0x9a4826); px(104, 194, 0x86391f)
        thick(97.0, 200.0, 92.0, 196.0, 2, 0x9a4826); thick(111.0, 199.0, 118.0, 194.0, 2, 0x5e3a24)
        for (cx in intArrayOf(122, 131)) { rect(cx, 201, 6, 6, 0xf1e6cc); rect(cx + 5, 201, 1, 6, 0xd6c7a6); rect(cx, 201, 6, 1, 0x8fb35a) }
        rect(186, 202, 24, 5, 0x34497a); rect(186, 202, 24, 1, 0x5677a8); rect(188, 207, 20, 1, 0x22325a)
        for ((x, y) in listOf(191 to 199, 198 to 199, 205 to 199, 194 to 195, 201 to 195)) { blob(x.toDouble(), y.toDouble(), 3.0, 2.3, 0xf0862a); px(x - 1, y - 2, 0xffc070) }
        px(198, 191, 0x5a8a3a); px(199, 190, 0x5a8a3a)
    }

    private fun Buf.tansu() {
        grainH(6, 146, 50, 44, 0x9a6438, 0x7e4e2a, 0xb07448, 70); rect(6, 146, 50, 2, 0x6a3e20)
        fun dr(x: Int, y: Int, w: Int, h: Int) {
            rect(x, y, w, 1, 0x5e3820); rect(x, y + h - 1, w, 1, 0xc08a5a); rect(x, y, 1, h, 0x5e3820)
            val cx = x + w / 2; val cy = y + h / 2
            rect(cx - 2, cy - 2, 5, 3, 0x2a2224); px(cx - 1, cy - 2, 0x5a5258); rect(cx - 3, cy + 1, 7, 1, 0x2a2224); px(cx - 3, cy, 0x2a2224); px(cx + 3, cy, 0x2a2224)
        }
        dr(8, 150, 22, 11); dr(32, 150, 22, 11); dr(8, 163, 46, 12); dr(8, 177, 46, 11)
        for ((x, y) in listOf(6 to 146, 53 to 146, 6 to 186, 53 to 186)) { rect(x, y, 3, 3, 0x2a2224); px(x + 1, y + 1, 0x5a5258) }
        rect(5, 189, 52, 2, 0x4a2e1c)
    }

    private fun Buf.andon(lit: Boolean) {
        rect(236, 150, 17, 3, 0x3a2620); rect(236, 150, 17, 1, 0x5a4038); rect(236, 153, 2, 37, 0x3a2620); rect(251, 153, 2, 37, 0x3a2620)
        rect(238, 153, 13, 28, if (lit) 0xffe6a6 else 0xf2ead8)
        if (lit) rect(241, 156, 7, 22, 0xfff2c8) else speck(238, 153, 13, 28, 0xe3d8c0, .08, 80)
        val k = if (lit) 0xe8c07a else 0xb9a07a
        rect(238, 162, 13, 1, k); rect(238, 172, 13, 1, k); rect(244, 153, 1, 28, k)
        rect(236, 181, 17, 2, 0x3a2620); rect(236, 188, 17, 2, 0x3a2620)
    }

    private fun Buf.bonsai(season: Season) {
        rect(326, 166, 26, 8, 0x3e5c8a); rect(324, 164, 30, 3, 0x5677a8); rect(328, 174, 3, 2, 0x2e4468); rect(347, 174, 3, 2, 0x2e4468); rect(326, 166, 26, 1, 0x2a3a5a)
        thick(340.0, 164.0, 337.0, 155.0, 2, 0x6a4a32); thick(337.0, 155.0, 343.0, 148.0, 2, 0x6a4a32); thick(338.0, 152.0, 331.0, 149.0, 1, 0x6a4a32)
        for ((x, y, w) in listOf(Triple(331, 149, 7), Triple(345, 145, 8), Triple(339, 139, 6))) {
            blob(x.toDouble(), y.toDouble(), w.toDouble(), 3.0, 0x3f6a30); blob(x.toDouble(), y - 1.0, w - 1.0, 2.0, 0x4f7a3a)
            var i = -w + 2; while (i < w - 1) { px(x + i, y - 2, 0x6f9a52); i += 3 }
            if (season == Season.WINTER) blob(x.toDouble(), y - 2.5, w - 2.0, 1.0, 0xffffff)
        }
    }

    private fun Buf.pig() {
        blob(340.0, 207.0, 6.0, 4.5, 0x5e6b4a); blob(340.0, 206.0, 5.0, 3.5, 0x6f7d58); blob(334.0, 206.0, 1.6, 1.6, 0x5e6b4a); px(333, 206, 0x3a4430)
        rect(337, 211, 2, 2, 0x4a5638); rect(342, 211, 2, 2, 0x4a5638); px(338, 203, 0x2a2a2a); px(346, 205, 0xff9a3a)
    }

    class Props(val kotatsu: Layer, val flap: Layer, val tansu: Layer, val andon: Layer, val bonsai: Layer, val pig: Layer)

    private val props = HashMap<Pair<Season, Boolean>, Props>()

    fun props(season: Season, night: Boolean): Props = synchronized(props) {
        props.getOrPut(season to night) {
            Props(
                kotatsu = Layer.of(W, H, 76, 188, 172, 72, night) { kotatsu() },
                flap = Layer.of(W, H, 76, 244, 172, 16, night) { futon() },
                tansu = Layer.of(W, H, 4, 144, 54, 48, night) { tansu() },
                // The paper lamp keeps its own light at night.
                andon = Layer.of(W, H, 234, 148, 21, 44, false) { andon(night); if (night) for (y in 0 until H) for (x in 0 until W) { val c = get(x, y); if (c ushr 24 != 0 && (c and 0xFFFFFF) < 0x808080) d[y * W + x] = night(c) } },
                bonsai = Layer.of(W, H, 320, 134, 36, 44, night) { bonsai(season) },
                pig = Layer.of(W, H, 330, 200, 20, 14, night) { pig() },
            )
        }
    }

    // --------------------------------------------------------------------------- moving bits

    fun flames(b: Buf, lit: Int, t: Int) {
        for (n in 0 until lit) {
            val r = n / 20; val i = n % 20
            val x = candleX(i, r); val y = CROWS[r] - 4
            val f = (t + n * 7) % 3
            b.p(x, y - 1, 0xffb43a); b.p(x + 1, y - 1, 0xff8a2a)
            b.p(if (f == 1) x + 1 else x, y - 2, 0xffe27a)
            if (f != 2) b.p(if (f == 0) x else x + 1, y - 3, 0xfff3c0)
        }
    }

    fun clouds(b: Buf, ms: Long, season: Season) {
        val col = if (season == Season.WINTER) 0xeef1f4 else 0xffffff
        val sh = if (season == Season.AUTUMN) 0xf0d8c0 else 0xdbe6ef
        for ((i, c) in listOf(Triple(0.0, 30.0, 8.0), Triple(.4, 44.0, 6.0), Triple(.7, 26.0, 10.0)).withIndex()) {
            val span = 100.0
            val x = 280 + ((ms / 1000.0 * (1.5 + i * .4) + c.first * span) % span) * 1.0
            for (k in 0..2) {
                val cx = x + (k - 1) * c.third * .45; val ry = if (k == 1) 3.0 else 2.0; val rx = c.third * (if (k == 1) 1.0 else .55)
                val n = kotlin.math.ceil(ry).toInt()
                for (yy in -n..n) { val q = 1 - yy * yy / (ry * ry); if (q < 0) continue; val w = (rx * sqrt(q)).roundToInt(); for (xx in (cx - w).roundToInt()..(cx + w).roundToInt()) if (xx in 292 until W) b.p(xx, (c.second + yy + (if (k == 1) 0 else 1)).roundToInt(), col) }
            }
            for (xx in (x - c.third).roundToInt()..(x + c.third).roundToInt()) if (xx in 292 until W) b.p(xx, (c.second + 3).roundToInt(), sh)
        }
    }

    fun koi(b: Buf, ms: Long, dark: Boolean) {
        for (i in 0 until 3) {
            val an = ms / 1000.0 * .35 * (1 + i * .25) + i * 2.1
            val x = 330 + cos(an) * 15; val y = 128 + sin(an) * 2.5
            val dir = if (-sin(an) > 0) 1 else -1
            val a = intArrayOf(0xf07a2a, 0xf6f0e8, 0xe8452a)[i]; val c = intArrayOf(0xf6f0e8, 0xf07a2a, 0xf6f0e8)[i]
            fun k(v: Int) = if (dark) night(v) else v
            val xi = x.roundToInt(); val yi = y.roundToInt()
            b.r(xi - 2, yi, 4, 1, k(a)); b.p(xi + dir * 2, yi, k(c)); b.p(xi - dir * 3, yi, k(a)); b.p(xi - dir * 4, yi - 1, k(a)); b.p(xi - dir * 4, yi + 1, k(a))
        }
    }

    fun furin(b: Buf, ms: Long, dark: Boolean) {
        fun k(v: Int) = if (dark) night(v) else v
        val sw = sin(ms / 1000.0 * 1.6) * 1.6 + sin(ms / 1000.0 * .6) * 1.2
        val fx = 322
        b.r(fx, 20, 1, 3, k(0x5a5a5a))
        intArrayOf(3, 5, 5, 5).forEachIndexed { i, w -> b.r(fx - w / 2, 23 + i, w, 1, k(if (i == 0) 0xe8f6fa else 0xcfe8f0)) }
        b.p(fx - 1, 25, k(0xd2463a)); b.p(fx + 1, 26, k(0xd2463a)); b.p(fx - 1, 24, k(0xffffff))
        for (r in 0 until 4) b.p(fx + (sw * r / 10).roundToInt(), 27 + r, k(0x6a6a6a))
        for (r in 0 until 8) { val ox = (sw * (r + 4) / 10).roundToInt(); b.r(fx - 1 + ox, 31 + r, 3, 1, k(if (r == 2) 0xd2463a else 0xf4e4b0)) }
    }

    fun steam(b: Buf, x0: Int, y0: Int, ms: Long, n: Int, len: Int) {
        for (k in 0 until n) {
            val ph = ((ms / 1000.0 * .5 + k.toDouble() / n) % 1.0)
            b.p((x0 + sin(ms / 500.0 + k * 1.3) * 2 * ph).roundToInt(), (y0 - ph * len).roundToInt(), 0xffffff, ((1 - ph) * .55).toFloat())
        }
    }

    /** The bright bits of the night: candles, the lamp, the stone lantern, the moon. */
    fun nightLights(b: Buf, lit: Int, toro: Boolean, ms: Long) {
        for ((x, y, p) in STARS) if (sin(ms / 700.0 + p) > .2) b.p(x, y, 0xffffff)
        for (j in -4..4) for (i in -4..4) if (i * i + j * j <= 17) b.p(304 + i, 40 + j, 0xf6eec8)
        for (j in -4..4) for (i in -4..4) if ((i - 2) * (i - 2) + (j + 1) * (j + 1) <= 13) b.p(304 + i, 40 + j, night(0x1b2246))
        val fl = .3f + (sin(ms / 140.0) * .015).toFloat()
        b.glow(244, 168, 64, 0xffbe6e, fl)
        if (lit > 0) b.glow(40, 106, 36, 0xffbe64, .06f + .3f * lit / 100f)
        if (toro) { b.r(300, 107, 7, 4, 0xffd98a); b.glow(303, 109, 16, 0xffc878, .4f) }
    }
}
