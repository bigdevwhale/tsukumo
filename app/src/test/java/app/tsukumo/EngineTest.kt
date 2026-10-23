package app.tsukumo

import app.tsukumo.domain.Catalog
import app.tsukumo.domain.Cat
import app.tsukumo.domain.Engine
import app.tsukumo.domain.EvType
import app.tsukumo.domain.Guess
import app.tsukumo.domain.Hobby
import app.tsukumo.domain.Kind
import app.tsukumo.domain.Rules
import app.tsukumo.domain.Session
import app.tsukumo.domain.State
import app.tsukumo.domain.Thing
import app.tsukumo.domain.Verb
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneOffset
import kotlin.random.Random

/** Lives ten years in the house and checks the pace stays slow and steady. */
class EngineTest {
    private val day = 24 * 3600 * 1000L
    private val zone = ZoneOffset.UTC

    private fun house(keys: List<String>): State {
        var id = 0L
        val hobbies = keys.mapIndexed { i, k -> Hobby(i + 1L, k, "", Catalog.hobby(k)!!.cat, false, 0) }
        val things = hobbies.flatMap { h -> Catalog.hobby(h.key)!!.things.map { t -> Thing(++id, h.id, t.key, "", t.arch, t.pal, t.verb, 0, Guess.pers(t.en, h.key!!), 0) } }
        return State(hobbies, things, emptyList(), emptyList(), emptyList(), 0)
    }

    /** About [perWeek] sessions a week, the first hobby loved most. */
    private fun live(s0: State, years: Int, perWeek: Double, seed: Int = 1): State {
        var s = s0
        val r = Random(seed)
        val ids = s.hobbies.map { it.id }
        for (d in 0 until years * 365) {
            if (r.nextDouble() > perWeek / 7) continue
            val h = if (r.nextDouble() < .55) ids[0] else ids[r.nextInt(ids.size)]
            val verbs = buildSet { add(Verb.entries[r.nextInt(6)]); if (r.nextBoolean()) add(Verb.entries[r.nextInt(6)]) }
            s = Engine.apply(s, Session(0, d * day + 18 * 3600_000L, h, 15 + r.nextInt(60), verbs, ""), zone).state
            if (r.nextDouble() < .2) {
                val h2 = ids[r.nextInt(ids.size)]
                if (h2 != h) s = Engine.apply(s, Session(0, d * day + 20 * 3600_000L, h2, 20, setOf(Verb.REPEAT), ""), zone).state
            }
        }
        return s
    }

    @Test fun firstWakingTakesWeeksNotDays() {
        val s = live(house(listOf("guitar")), 1, 3.5)
        val wakes = s.evs.filter { it.type == EvType.WAKE && it.n == 5 }
        assertTrue("somebody wakes in the first year", wakes.isNotEmpty())
        val first = wakes.first().at / day
        assertTrue("the first spirit wakes after a few weeks, not days (day $first)", first in 21..120)
    }

    @Test fun tenYearsHaveRoomToGrow() {
        val s = live(house(listOf("guitar", "knitting", "sourdough")), 10, 3.5)
        val ranks = s.things.filter { it.kind == Kind.THING }.groupingBy { it.rank }.eachCount()
        println("candles ${s.candles}, ranks $ranks, kids ${s.things.count { it.kind == Kind.KID }}, rares ${s.things.count { it.kind == Kind.RARE }}")
        for (y in 0 until 10) {
            val evs = s.evs.filter { s.year(it.at) == y }
            println("year ${y + 1}: wakes ${evs.count { it.type == EvType.WAKE && it.n == 5 }}, ranks ${evs.filter { it.type == EvType.RANK }.map { it.n }}, titles ${evs.filter { it.type == EvType.TITLE }.map { it.n }}, wonders ${evs.filter { it.type == EvType.WONDER }.map { it.s }}, kids ${evs.count { it.type == EvType.KID }}")
        }
        assertTrue("after ten years not everything is a kami", s.things.count { it.rank >= 4 } <= 2)
        assertTrue("but something has become a sage or close", s.things.any { it.rank >= 2 })
        assertTrue("offspring are rare", s.things.count { it.kind == Kind.KID } <= Rules.KID_MAX)
        val parents = s.things.filter { it.kind == Kind.KID }.flatMap { listOf(it.parentA, it.parentB) }
        assertEquals("each spirit parents once", parents.size, parents.toSet().size)
    }

    @Test fun wondersComeInOrder() {
        val s = live(house(listOf("drawing", "yoga")), 12, 4.0)
        val got = s.evs.filter { it.type == EvType.WONDER }.map { it.s }
        assertEquals(Rules.WONDERS.filter { it.at <= s.candles }.map { it.key }, got)
        assertEquals(Rules.WONDERS.count { it.rare != null && it.at <= s.candles }, s.things.count { it.kind == Kind.RARE })
    }

    @Test fun guessesShapesFromWords() {
        assertEquals("guitar", Guess.arch("Старая гитара").arch)
        assertEquals("cup", Guess.arch("кружка с котом").arch)
        assertEquals("yarn", Guess.arch("ball of yarn").arch)
        assertEquals("clock", Guess.arch("метроном").arch)
        assertTrue(Guess.arch("непонятная штука").arch in Catalog.ARCHS)
        assertEquals(Verb.LEARN, Guess.verb("словарь"))
    }

    @Test fun catalogIsWhole() {
        for (h in Catalog.hobbies) {
            assertTrue(h.key, h.things.size >= 3)
            for (t in h.things) {
                assertTrue(t.key, t.arch in Catalog.ARCHS)
                assertTrue(t.key, t.pal in Catalog.PALS)
                assertTrue(t.ru.isNotBlank() && t.en.isNotBlank())
            }
        }
        assertEquals(Catalog.hobbies.size, Catalog.hobbies.map { it.key }.toSet().size)
        assertTrue(Cat.entries.all { c -> Catalog.hobbies.any { it.cat == c } })
    }
}
