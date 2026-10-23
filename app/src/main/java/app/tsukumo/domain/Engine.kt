package app.tsukumo.domain

import java.time.ZoneId

/** What one session changed: the new state, and the pieces to write down. */
class Outcome(
    val state: State,
    val changed: List<Thing>,
    val added: List<Thing>,
    val evs: List<Ev>,
    val bonds: List<Bond>,
)

/** The rules of the house, with no storage: a session goes in, a new state comes out. */
object Engine {
    private const val DAY = 24 * 3600 * 1000L

    fun apply(s0: State, ses: Session, zone: ZoneId = ZoneId.systemDefault()): Outcome {
        val at = ses.at
        val evs = mutableListOf<Ev>()
        fun ev(type: EvType, a: Long = 0, b: Long = 0, c: Long = 0, n: Int = 0, s: String = "") { evs += Ev(0, at, type, a, b, c, n, s) }
        val hobby = requireNotNull(s0.hobby(ses.hobbyId)) { "no hobby ${ses.hobbyId}" }

        s0.sessions.lastOrNull { it.hobbyId == hobby.id }?.let { last ->
            val gap = ((at - last.at) / DAY).toInt()
            if (gap >= 30) ev(EvType.RETURN, a = hobby.id, n = gap)
        }

        val changed = mutableListOf<Thing>()
        val things = s0.things.map { t ->
            if (t.hobbyId != hobby.id || t.kind != Kind.THING) return@map t
            val n = t.copy(pts = t.pts + Rules.gain(t, ses))
            if (n.stage > t.stage) ev(EvType.WAKE, a = t.id, n = n.stage)
            else if (n.awake && n.rank > t.rank) ev(EvType.RANK, a = t.id, n = n.rank)
            changed += n
            n
        }.toMutableList()

        val title0 = s0.title(hobby.id)
        val title1 = Rules.title(s0.minutes(hobby.id) + ses.minutes)
        if (title1 > title0) ev(EvType.TITLE, a = hobby.id, n = title1)

        val candles = s0.candles + 1
        val added = mutableListOf<Thing>()
        var nextId = (s0.things.maxOfOrNull { it.id } ?: 0) + 1
        val wonder = Rules.WONDERS.firstOrNull { it.at == candles }
        if (wonder != null) {
            ev(EvType.WONDER, n = candles, s = wonder.key)
            wonder.rare?.let { r ->
                val t = Thing(nextId++, null, null, "", r.arch, r.pal, Verb.REPEAT, 0, r.pers, at, Kind.RARE, rare = wonder.key)
                things += t; added += t
            }
        } else if (candles % 100 == 0) ev(EvType.CANDLES, n = candles)

        val prevAt = s0.sessions.lastOrNull()?.at ?: s0.start
        val year = Rules.year(s0.start, at)
        if (year > Rules.year(s0.start, prevAt)) ev(EvType.YEAR, n = year)

        // Friendship grows on days two hobbies were both practised.
        var bonds = s0.bonds
        val touched = mutableListOf<Bond>()
        val day = at.day(zone)
        val others = s0.sessions.filter { it.hobbyId != hobby.id && it.at.day(zone) == day }.map { it.hobbyId }.distinct()
        if (others.isNotEmpty()) {
            val mine = things.filter { it.hobbyId == hobby.id && it.kind == Kind.THING && it.awake }
            val theirs = things.filter { it.hobbyId in others && it.kind == Kind.THING && it.awake }
            if (mine.isNotEmpty() && theirs.isNotEmpty()) {
                // The pair holds for a few months, so a friendship deepens rather than scatters.
                val a = mine[mixHash(day / 120, hobby.id) % mine.size]
                val b = theirs[mixHash(day / 120, a.id) % theirs.size]
                val x = minOf(a.id, b.id)
                val y = maxOf(a.id, b.id)
                val old = bonds.firstOrNull { it.a == x && it.b == y }
                if (old?.lastDay != day) {
                    val nb = Bond(x, y, (old?.days ?: 0) + 1, day)
                    bonds = bonds.filterNot { it.a == x && it.b == y } + nb
                    touched += nb
                    fun parented(id: Long) = things.any { it.kind == Kind.KID && (it.parentA == id || it.parentB == id) }
                    if (nb.days == Rules.KID_DAYS && things.count { it.kind == Kind.KID } < Rules.KID_MAX && !parented(a.id) && !parented(b.id)) {
                        val kid = Thing(nextId++, null, null, "", a.arch, b.pal, Verb.REPEAT, 0, Pers.entries[mixHash(a.id, b.id) % Pers.entries.size], at, Kind.KID, parentA = a.id, parentB = b.id)
                        things += kid; added += kid
                        ev(EvType.KID, a = a.id, b = b.id, c = kid.id)
                    }
                }
            }
        }

        val s1 = s0.copy(things = things, sessions = s0.sessions + ses, bonds = bonds, evs = s0.evs + evs)
        return Outcome(s1, changed, added, evs, touched)
    }

    /** Friends of a spirit, closest first. */
    fun friends(s: State, id: Long): List<Pair<Thing, Int>> =
        s.bonds.filter { it.a == id || it.b == id }.sortedByDescending { it.days }
            .mapNotNull { b -> s.thing(if (b.a == id) b.b else b.a)?.let { it to b.days } }
}
