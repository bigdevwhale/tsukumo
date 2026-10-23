package app.tsukumo.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** What a session was spent on. Every thing wakes faster from its own verb. */
enum class Verb { REPEAT, NEW, FIX, FINISH, LEARN, SHARE }

/** Hobby families; each brings its own little tool to the session sheet. */
enum class Cat { MUSIC, DRAW, CRAFT, KITCHEN, WORDS, MIND, MOVE, GARDEN, BUILD }

enum class Pers { SHY, BOLD, SLEEPY, CURIOUS, PROUD, CARING }

/** An ordinary thing of a hobby, a rare spirit that came on a candle night, or the offspring of two friends. */
enum class Kind { THING, RARE, KID }

/** [key] points into the catalogue; [name] is what the person typed and wins over the catalogue's words. */
data class Hobby(
    val id: Long,
    val key: String?,
    val name: String,
    val cat: Cat,
    val archived: Boolean,
    val created: Long,
)

data class Thing(
    val id: Long,
    val hobbyId: Long?,
    val key: String?,
    val name: String,
    val arch: String,
    val pal: String,
    val verb: Verb,
    val pts: Int,
    val pers: Pers,
    val created: Long,
    val kind: Kind = Kind.THING,
    /** For a rare spirit: which wonder brought it. */
    val rare: String? = null,
    val parentA: Long? = null,
    val parentB: Long? = null,
) {
    val special get() = kind != Kind.THING
    val stage get() = if (special) 5 else Rules.stage(pts)
    val rank get() = if (special) 0 else Rules.rank(pts)
    val awake get() = stage >= 5
}

data class Session(
    val id: Long,
    val at: Long,
    val hobbyId: Long,
    val minutes: Int,
    val verbs: Set<Verb>,
    val note: String,
)

enum class EvType { MOVE_IN, HOBBY, WAKE, RANK, TITLE, WONDER, CANDLES, YEAR, KID, PAUSE, RETURN }

/**
 * Something worth remembering, stored without words so the chronicle reads in whatever language the
 * app is in: [a]/[b]/[c] are ids (thing, hobby…), [n] a stage, rank, count or year, [s] a wonder key.
 */
data class Ev(
    val id: Long,
    val at: Long,
    val type: EvType,
    val a: Long = 0,
    val b: Long = 0,
    val c: Long = 0,
    val n: Int = 0,
    val s: String = "",
)

/** Two spirits of different hobbies grow close on days both hobbies were practised. */
data class Bond(val a: Long, val b: Long, val days: Int, val lastDay: Long)

/** Everything the house knows, in memory. */
data class State(
    val hobbies: List<Hobby>,
    val things: List<Thing>,
    val sessions: List<Session>,
    val evs: List<Ev>,
    val bonds: List<Bond>,
    val start: Long,
) {
    val candles: Int get() = sessions.size

    fun hobby(id: Long?) = hobbies.firstOrNull { it.id == id }
    fun thing(id: Long?) = things.firstOrNull { it.id == id }
    fun thingsOf(h: Long) = things.filter { it.hobbyId == h && it.kind == Kind.THING }
    fun minutes(h: Long) = sessions.sumOf { if (it.hobbyId == h) it.minutes else 0 }
    fun hours(h: Long) = minutes(h) / 60.0
    fun title(h: Long) = Rules.title(minutes(h))
    fun has(wonder: String) = candles >= (Rules.WONDERS.firstOrNull { it.key == wonder }?.at ?: Int.MAX_VALUE)
    fun year(at: Long) = Rules.year(start, at)
    val active get() = hobbies.filter { !it.archived }

    /** Things that live in the house now: everything of hobbies not packed away, plus the special spirits. */
    val living: List<Thing>
        get() {
            val packed = hobbies.filter { it.archived }.map { it.id }.toSet()
            return things.filter { it.special || it.hobbyId !in packed }
        }

    val litCandles: Int get() = (candles % 100).let { if (it == 0 && candles > 0) 100 else it }
    val nextWonder get() = Rules.WONDERS.firstOrNull { it.at > candles }

    companion object {
        val EMPTY = State(emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), 0)
    }
}

data class Wonder(val key: String, val at: Int, val rare: Rare? = null)

/** A rare spirit: how it looks. */
data class Rare(val arch: String, val pal: String, val pers: Pers)

object Rules {
    /** Points for each awakening step: shivers, one eye, both eyes, legs, off the shelf. */
    val WAKE = intArrayOf(3, 10, 22, 40, 60)

    /** After waking a spirit keeps growing for years: spirit, keeper, elder, sage, kami. */
    val RANK = intArrayOf(60, 200, 600, 1400, 3000)

    /** Hobby titles by hours of practice. */
    val TITLE_H = intArrayOf(0, 10, 30, 100, 300, 1000, 3000)

    val WONDERS = listOf(
        Wonder("obake", 100, Rare("obake", "obake", Pers.CURIOUS)),
        Wonder("furin", 200),
        Wonder("koi", 300),
        Wonder("toro", 400),
        Wonder("bonsai", 500),
        Wonder("tanuki", 600, Rare("tanuki", "tanuki", Pers.BOLD)),
        Wonder("flies", 750),
        Wonder("kitsune", 900, Rare("kitsune", "kitsune", Pers.PROUD)),
        Wonder("tree", 1000),
        Wonder("shimenawa", 1250),
        Wonder("warashi", 1500, Rare("warashi", "warashi", Pers.CARING)),
        Wonder("gold", 2000),
    )

    /** Days of shared practice before two friends have an offspring; each spirit parents once. */
    const val KID_DAYS = 6
    const val KID_MAX = 8
    const val YEAR_MS = 365L * 24 * 3600 * 1000

    fun stage(pts: Int) = WAKE.count { pts >= it }
    fun rank(pts: Int) = (RANK.count { pts >= it } - 1).coerceAtLeast(0)
    fun title(minutes: Int) = (TITLE_H.count { minutes >= it * 60 } - 1).coerceAtLeast(0)

    /** A session's points for one thing of its hobby. */
    fun gain(t: Thing, s: Session) = 1 + (if (t.verb in s.verbs) 2 else 0) + (if (s.minutes >= 30) 1 else 0)

    fun year(start: Long, at: Long) = ((at - start).coerceAtLeast(0) / YEAR_MS).toInt()

    /** Progress 0..1 towards the next step, rank or the top. */
    fun progress(t: Thing): Float {
        if (t.special) return 1f
        val p = t.pts
        val st = stage(p)
        if (st < 5) {
            val lo = if (st == 0) 0 else WAKE[st - 1]
            return (p - lo).toFloat() / (WAKE[st] - lo)
        }
        val r = rank(p)
        if (r >= RANK.size - 1) return 1f
        return (p - RANK[r]).toFloat() / (RANK[r + 1] - RANK[r])
    }

    /** Points still needed for the next step or rank, or 0 at the top. */
    fun toNext(t: Thing): Int {
        if (t.special) return 0
        val st = stage(t.pts)
        if (st < 5) return WAKE[st] - t.pts
        val r = rank(t.pts)
        return if (r >= RANK.size - 1) 0 else RANK[r + 1] - t.pts
    }
}

fun Long.day(zone: ZoneId = ZoneId.systemDefault()): Long = Instant.ofEpochMilli(this).atZone(zone).toLocalDate().toEpochDay()
fun Long.date(zone: ZoneId = ZoneId.systemDefault()): LocalDate = Instant.ofEpochMilli(this).atZone(zone).toLocalDate()

/** A stable small hash for picking things deterministically. */
fun mixHash(vararg v: Long): Int {
    var h = 1469598103934665603L
    for (x in v) { h = (h xor x) * 1099511628211L; h = h xor (h ushr 29) }
    return (h and 0x7FFFFFFF).toInt()
}
