package app.tsukumo.data

import app.tsukumo.domain.Bond
import app.tsukumo.domain.Cat
import app.tsukumo.domain.Catalog
import app.tsukumo.domain.Engine
import app.tsukumo.domain.Ev
import app.tsukumo.domain.EvType
import app.tsukumo.domain.Guess
import app.tsukumo.domain.Hobby
import app.tsukumo.domain.Kind
import app.tsukumo.domain.Outcome
import app.tsukumo.domain.Pers
import app.tsukumo.domain.Session
import app.tsukumo.domain.State
import app.tsukumo.domain.Thing
import app.tsukumo.domain.Verb
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** The house on disk, and the live picture of it in [state]. */
class Repo(private val db: HouseDb, private val clock: () -> Long = System::currentTimeMillis) {
    private val dao = db.dao()
    private val lock = Mutex()
    private val _state = MutableStateFlow<State?>(null)
    val state: StateFlow<State?> = _state

    suspend fun refresh(): State = lock.withLock { load().also { _state.value = it } }

    private suspend fun load(): State = State(
        hobbies = dao.hobbies().map { Hobby(it.id, it.key, it.name, Cat.valueOf(it.cat), it.archived, it.created) },
        things = dao.things().map { it.domain() },
        sessions = dao.sessions().map { r -> Session(r.id, r.at, r.hobbyId, r.minutes, r.verbs.split(',').filter { it.isNotBlank() }.map { Verb.valueOf(it) }.toSet(), r.note) },
        evs = dao.evs().map { Ev(it.id, it.at, EvType.valueOf(it.type), it.a, it.b, it.c, it.n, it.s) },
        bonds = dao.bonds().map { Bond(it.a, it.b, it.days, it.lastDay) },
        start = dao.meta("start")?.toLongOrNull() ?: 0,
    )

    private suspend fun current(): State = _state.value ?: load()

    private suspend fun started(at: Long) {
        if (dao.meta("start") == null) {
            dao.meta(MetaRow("start", at.toString()))
            dao.insertEvs(listOf(EvRow(at = at, type = EvType.MOVE_IN.name, a = 0, b = 0, c = 0, n = 0, s = "")))
        }
    }

    /** Moves a catalogue hobby in, its things on the shelf. Returns the hobby id. */
    suspend fun adopt(key: String, at: Long = clock()): Long = lock.withLock {
        val c = requireNotNull(Catalog.hobby(key))
        val s = current()
        s.hobbies.firstOrNull { it.key == key }?.let { h -> if (h.archived) dao.archive(h.id, false); _state.value = load(); return@withLock h.id }
        started(at)
        val id = dao.insert(HobbyRow(key = key, name = "", cat = c.cat.name, archived = false, created = at, ord = s.hobbies.size))
        var next = (s.things.maxOfOrNull { it.id } ?: 0) + 1
        dao.upsertThings(c.things.map { t -> ThingRow(next++, id, t.key, "", t.arch, t.pal, t.verb.name, 0, Guess.pers(t.en, c.en).name, at, Kind.THING.name, null, null, null) })
        dao.insertEvs(listOf(EvRow(at = at, type = EvType.HOBBY.name, a = id, b = 0, c = 0, n = 0, s = "")))
        _state.value = load()
        id
    }

    /** A hobby of the person's own; things are added one by one. */
    suspend fun custom(name: String, cat: Cat, at: Long = clock()): Long = lock.withLock {
        val s = current()
        started(at)
        val id = dao.insert(HobbyRow(key = null, name = name.trim(), cat = cat.name, archived = false, created = at, ord = s.hobbies.size))
        dao.insertEvs(listOf(EvRow(at = at, type = EvType.HOBBY.name, a = id, b = 0, c = 0, n = 0, s = "")))
        _state.value = load()
        id
    }

    suspend fun addThing(hobby: Long, name: String, arch: String, verb: Verb, pal: String, at: Long = clock()): Long = lock.withLock {
        val s = current()
        val h = s.hobby(hobby) ?: return@withLock 0
        val id = (s.things.maxOfOrNull { it.id } ?: 0) + 1
        dao.upsertThings(listOf(ThingRow(id, hobby, null, name.trim(), arch, pal, verb.name, 0, Guess.pers(name, h.name.ifEmpty { h.key ?: "" }).name, at, Kind.THING.name, null, null, null)))
        _state.value = load()
        id
    }

    suspend fun renameThing(id: Long, name: String) = lock.withLock { dao.rename(id, name.trim()); _state.value = load() }
    suspend fun renameHobby(id: Long, name: String) = lock.withLock { dao.renameHobby(id, name.trim()); _state.value = load() }

    /** Lets a thing go. Its friendships go with it; offspring stay. */
    suspend fun removeThing(id: Long) = lock.withLock { dao.deleteThing(id); dao.deleteBonds(id); _state.value = load() }

    suspend fun archive(id: Long, archived: Boolean, at: Long = clock()) = lock.withLock {
        dao.archive(id, archived)
        if (archived) dao.insertEvs(listOf(EvRow(at = at, type = EvType.PAUSE.name, a = id, b = 0, c = 0, n = 0, s = "")))
        _state.value = load()
    }

    /** Lights a candle: the session goes through the rules and everything it changed is saved. */
    suspend fun practise(hobby: Long, minutes: Int, verbs: Set<Verb>, note: String, at: Long = clock()): Outcome = lock.withLock {
        val s0 = current().let { if (it.start == 0L) it.copy(start = at) else it }
        if (dao.meta("start") == null) started(at)
        val ses = Session(0, at, hobby, minutes, verbs, note.trim())
        val out = Engine.apply(s0, ses)
        dao.session(
            SessionRow(at = at, hobbyId = hobby, minutes = minutes, verbs = verbs.joinToString(",") { it.name }, note = note.trim()),
            (out.changed + out.added).map { it.row() },
            out.evs.map { EvRow(at = it.at, type = it.type.name, a = it.a, b = it.b, c = it.c, n = it.n, s = it.s) },
            out.bonds.map { BondRow(it.a, it.b, it.days, it.lastDay) },
        )
        _state.value = load()
        out
    }

    /** Writes a whole simulated past at once (the demo build). */
    suspend fun replay(steps: List<Session>) = lock.withLock {
        var s = current()
        for (ses in steps) {
            val out = Engine.apply(if (s.start == 0L) s.copy(start = ses.at) else s, ses)
            dao.session(
                SessionRow(at = ses.at, hobbyId = ses.hobbyId, minutes = ses.minutes, verbs = ses.verbs.joinToString(",") { it.name }, note = ses.note),
                (out.changed + out.added).map { it.row() },
                out.evs.map { EvRow(at = it.at, type = it.type.name, a = it.a, b = it.b, c = it.c, n = it.n, s = it.s) },
                out.bonds.map { BondRow(it.a, it.b, it.days, it.lastDay) },
            )
            s = out.state
        }
        _state.value = load()
    }

    suspend fun setStart(at: Long) = lock.withLock { dao.meta(MetaRow("start", at.toString())); _state.value = load() }

    private fun ThingRow.domain() = Thing(id, hobbyId, key, name, arch, pal, Verb.valueOf(verb), pts, Pers.valueOf(pers), created, Kind.valueOf(kind), rare, parentA, parentB)
    private fun Thing.row() = ThingRow(id, hobbyId, key, name, arch, pal, verb.name, pts, pers.name, created, kind.name, rare, parentA, parentB)
}
