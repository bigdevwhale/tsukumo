package app.tsukumo.ui.art

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt

/** Small painting tools on top of [Buf], in the spirit of the HTML prototype. Colours are 0xRRGGBB. */

fun Buf.rect(x: Number, y: Number, w: Number, h: Number, c: Int, a: Float = 1f) {
    val x0 = x.toDouble().roundToInt(); val y0 = y.toDouble().roundToInt()
    r(x0, y0, w.toDouble().roundToInt(), h.toDouble().roundToInt(), c, a)
}

fun Buf.px(x: Number, y: Number, c: Int, a: Float = 1f) = p(floor(x.toDouble()).toInt(), floor(y.toDouble()).toInt(), c, a)

/** A filled ellipse made of rows, like a hand-drawn pixel blob. */
fun Buf.blob(cx: Double, cy: Double, rx: Double, ry: Double, c: Int, a: Float = 1f) {
    if (rx <= 0 || ry <= 0) return
    val n = kotlin.math.ceil(ry).toInt()
    for (y in -n..n) {
        val q = 1 - (y * y) / (ry * ry)
        if (q < 0) continue
        val w = (rx * sqrt(q)).roundToInt()
        r((cx - w).roundToInt(), (cy + y).roundToInt(), 2 * w + 1, 1, c, a)
    }
}

/** A line [w] pixels thick. */
fun Buf.thick(x0: Double, y0: Double, x1: Double, y1: Double, w: Int, c: Int) {
    val n = max(abs(x1 - x0), abs(y1 - y0)).roundToInt().coerceAtLeast(1)
    for (i in 0..n) {
        val x = x0 + (x1 - x0) * i / n; val y = y0 + (y1 - y0) * i / n
        r((x - w / 2.0).roundToInt(), (y - w / 2.0).roundToInt(), w, w, c)
    }
}

/** Scattered specks, the same every time for one [seed]. */
fun Buf.speck(x: Int, y: Int, w: Int, h: Int, c: Int, dens: Double, seed: Int, a: Float = 1f) {
    val n = (w * h * dens).toInt()
    for (i in 0 until n) p(x + (hash(seed, i * 2) * w).toInt(), y + (hash(seed, i * 2 + 1) * h).toInt(), c, a)
}

/** Wood with a horizontal grain. */
fun Buf.grainH(x: Int, y: Int, w: Int, h: Int, base: Int, dark: Int, light: Int, seed: Int) {
    r(x, y, w, h, base)
    var k = 0
    for (j in 0 until h) {
        var i = (hash(seed, k++) * 8).toInt()
        while (i < w) {
            val len = 3 + (hash(seed, k++) * 16).toInt()
            if (hash(seed, k++) < .4) r(x + i, y + j, minOf(len, w - i), 1, if (hash(seed, k++) < .75) dark else light)
            i += len + 2 + (hash(seed, k++) * 8).toInt()
        }
    }
}

/** Wood with a vertical grain. */
fun Buf.grainV(x: Int, y: Int, w: Int, h: Int, base: Int, dark: Int, light: Int, seed: Int) {
    r(x, y, w, h, base)
    var k = 0
    for (i in 0 until w) {
        var j = (hash(seed, k++) * 8).toInt()
        while (j < h) {
            val len = 4 + (hash(seed, k++) * 20).toInt()
            if (hash(seed, k++) < .45) r(x + i, y + j, 1, minOf(len, h - j), if (hash(seed, k++) < .75) dark else light)
            j += len + 2 + (hash(seed, k++) * 10).toInt()
        }
    }
}

/** A vertical gradient between two colours, in [steps] dithered bands. */
fun Buf.grad(x: Int, y: Int, w: Int, h: Int, top: Int, bot: Int, steps: Int) {
    for (j in 0 until h) {
        val t = j / max(1.0, h - 1.0) * steps
        val i = floor(t).toInt(); val fr = t - i
        val c1 = mix(top, bot, (i.toFloat() / steps)); val c2 = mix(top, bot, (minOf(steps, i + 1).toFloat() / steps))
        for (k in 0 until w) p(x + k, y + j, if (fr * 16 > bay(x + k, y + j) + .5) c2 else c1)
    }
}

fun mixc(a: Int, b: Int, t: Float) = mix(a or 0xFF000000.toInt(), b or 0xFF000000.toInt(), t) and 0xFFFFFF

/** Night: everything sinks into a deep blue. */
fun night(c: Int): Int {
    fun one(sh: Int, add: Int) = (((c shr sh) and 255) * .44 + add).roundToInt().coerceIn(0, 255)
    return (c and 0xFF000000.toInt()) or (one(16, 7) shl 16) or (one(8, 6) shl 8) or one(0, 24)
}

/** A rectangle of pixels with transparency, painted over a buffer as one piece (furniture, props). */
class Layer(val x: Int, val y: Int, val w: Int, val h: Int, val d: IntArray) {
    fun paint(b: Buf) {
        for (j in 0 until h) {
            val yy = y + j
            if (yy < 0 || yy >= b.h) continue
            for (i in 0 until w) {
                val c = d[j * w + i]
                if (c ushr 24 != 0) b.p(x + i, yy, c, (c ushr 24) / 255f)
            }
        }
    }

    companion object {
        /** Paints [fn] into a scratch buffer and keeps just the box [x, y, w, h]. */
        fun of(bw: Int, bh: Int, x: Int, y: Int, w: Int, h: Int, dark: Boolean, fn: Buf.() -> Unit): Layer {
            val b = Buf(bw, bh); b.fn()
            val d = IntArray(w * h)
            for (j in 0 until h) for (i in 0 until w) {
                val c = b.get(x + i, y + j)
                d[j * w + i] = if (c ushr 24 == 0) 0 else if (dark) night(c) else c
            }
            return Layer(x, y, w, h, d)
        }
    }
}
