package app.tsukumo.data

import android.content.Context
import app.tsukumo.AppContainer
import app.tsukumo.domain.Session
import app.tsukumo.domain.Verb
import kotlin.random.Random

/**
 * The demo build's house: about [years] of real-looking practice played through the real rules, so
 * screenshots show awake spirits, ranks, wonders and a chronicle.
 */
object DemoSeed {
    suspend fun seedIfEmpty(ctx: Context, c: AppContainer, years: Double = 3.6) {
        if (c.repo.refresh().hobbies.isNotEmpty()) return
        val now = System.currentTimeMillis()
        val start = now - (years * 365 * 24 * 3600 * 1000L).toLong()
        val day = 24 * 3600 * 1000L
        val guitar = c.repo.adopt("guitar", start)
        val knit = c.repo.adopt("knitting", start + 20 * day)
        val bread = c.repo.adopt("sourdough", start + 200 * day)
        val callig = c.repo.adopt("calligraphy", start + 90 * day)
        c.repo.setStart(start)
        val r = Random(42)
        val notes = listOf("", "", "", "", "баррэ без дребезга, наконец-то", "новый узор — косы", "хлеб поднялся как надо", "", "сыграл для друзей", "")
        val out = ArrayList<Session>()
        var t = start + 9 * 3600 * 1000L
        while (t < now - 2 * 3600 * 1000L) {
            val d = ((t - start) / day).toInt()
            // Three or four evenings a week, with a lazy summer and a few long pauses.
            val busy = when { d % 365 in 180..215 -> .25; d in 400..430 -> .05; else -> .52 }
            if (r.nextDouble() < busy) {
                val pool = buildList {
                    add(guitar); add(guitar); if (d >= 20) add(knit); if (d >= 200) { add(bread) }; if (d in 90..330) add(callig)
                }
                val h = pool[r.nextInt(pool.size)]
                val verbs = buildSet { add(Verb.entries[r.nextInt(6)]); if (r.nextBoolean()) add(Verb.entries[r.nextInt(6)]) }
                out += Session(0, t + r.nextLong(0, 3 * 3600 * 1000L), h, 10 + r.nextInt(80), verbs, notes[r.nextInt(notes.size)])
                // Sometimes a second hobby on the same day — that's how spirits make friends.
                if (r.nextDouble() < .25) { val h2 = pool[r.nextInt(pool.size)]; if (h2 != h) out += Session(0, t + 4 * 3600 * 1000L, h2, 15 + r.nextInt(40), setOf(Verb.entries[r.nextInt(6)]), "") }
            }
            t += day
        }
        c.repo.replay(out)
        c.repo.archive(callig, true, start + 340 * day)
        c.settings.update { it.copy(onboarded = true) }
    }
}
