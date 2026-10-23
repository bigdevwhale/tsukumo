package app.tsukumo.ui.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.tsukumo.container
import app.tsukumo.domain.EvType
import app.tsukumo.domain.Outcome
import app.tsukumo.domain.Verb
import app.tsukumo.ui.Home
import app.tsukumo.ui.components.Btn
import app.tsukumo.ui.components.Chip
import app.tsukumo.ui.components.Eyebrow
import app.tsukumo.ui.components.Field
import app.tsukumo.ui.components.Flow
import app.tsukumo.ui.components.Gap
import app.tsukumo.ui.components.Sheet
import app.tsukumo.ui.components.SpriteImage
import app.tsukumo.ui.components.box
import app.tsukumo.ui.components.look
import app.tsukumo.ui.theme.C
import app.tsukumo.ui.theme.T
import kotlinx.coroutines.launch

@Composable
fun SessionSheet(h: Home, preset: Long?, onClose: () -> Unit, onDone: (Outcome, Long) -> Unit) {
    val c = LocalContext.current.container
    val w = h.w
    val s = h.s
    val active = s.active
    val st = h.settings
    val timerOn = st.timerSince > 0
    var hobby by remember { mutableStateOf(preset ?: st.timerHobby.takeIf { timerOn } ?: s.sessions.lastOrNull()?.hobbyId?.takeIf { id -> active.any { it.id == id } } ?: active.firstOrNull()?.id) }
    val fromTimer = timerOn && st.timerHobby == hobby
    var minutes by remember { mutableIntStateOf(if (fromTimer) (((System.currentTimeMillis() - st.timerSince) / 60000).toInt()).coerceIn(1, 600) else 30) }
    var verbs by remember { mutableStateOf(setOf(Verb.REPEAT)) }
    var note by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Sheet(w.t("Занятие", "A session"), onClose, w.t("Закрыть", "Close")) {
        if (active.isEmpty()) { Text(w.t("Сначала заведите хобби на вкладке «Хобби».", "Add a hobby on the Hobbies tab first."), style = T.Body); return@Sheet }
        Eyebrow(w.t("Хобби", "Hobby"))
        Gap(6)
        Flow { for (hb in active) Chip("${w.catIcon(hb.cat)} ${w.hobby(hb)}", hobby == hb.id) { hobby = hb.id } }
        Gap(16)
        Eyebrow(w.t("Сколько минут", "How many minutes"))
        Gap(6)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Btn("−") { minutes = (minutes - 5).coerceAtLeast(1) }
            Text(w.minutes(minutes), style = T.Number, modifier = Modifier.padding(horizontal = 16.dp))
            Btn("+") { minutes = (minutes + 5).coerceAtMost(600) }
        }
        Gap(6)
        Flow { for (m in listOf(10, 15, 20, 30, 45, 60, 90)) Chip("$m", minutes == m) { minutes = m } }
        Gap(8)
        if (timerOn) {
            Btn(w.t("Сбросить таймер", "Drop the timer")) { c.write { settings.update { it.copy(timerHobby = 0, timerSince = 0) } } }
        } else Btn(w.t("Засечь время и заниматься", "Start a timer and practise")) {
            val id = hobby ?: return@Btn
            c.write { settings.update { it.copy(timerHobby = id, timerSince = System.currentTimeMillis()) } }
            onClose()
        }
        Gap(16)
        Eyebrow(w.t("Что вы делали", "What you did"))
        Gap(6)
        val rows = Verb.entries.chunked(2)
        for (r in rows) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (v in r) Chip(w.verb(v), v in verbs, Modifier.weight(1f), sub = w.verbHint(v)) { verbs = if (v in verbs) verbs - v else verbs + v }
            }
            Gap(6)
        }
        val things = hobby?.let { s.thingsOf(it) }.orEmpty()
        val fast = things.filter { it.verb in verbs }
        Text(
            if (fast.isEmpty()) w.t("Эти глаголы не любимые ни у одной вещи хобби — расти будут медленнее.", "None of the hobby's things love these verbs — they'll grow slower.")
            else w.t("Быстрее вырастут: ", "Growing faster: ") + fast.joinToString(", ") { w.thing(it, s) }, style = T.Small,
        )
        Gap(16)
        Eyebrow(w.t("Заметка", "A note"))
        Gap(6)
        Field(note, { note = it }, w.t("например: баррэ без дребезга, наконец-то", "e.g. a clean barre chord at last"), single = false)
        Gap(18)
        Btn(w.t("Зажечь свечу", "Light a candle"), Modifier.fillMaxWidth(), primary = true, enabled = hobby != null && !busy) {
            val id = hobby ?: return@Btn
            busy = true
            scope.launch {
                val out = c.repo.practise(id, minutes, verbs, note)
                if (fromTimer) c.settings.update { it.copy(timerHobby = 0, timerSince = 0) }
                onDone(out, id)
            }
        }
        Gap(8)
        Text(w.t("Свеча — за любое занятие, хоть десять минут. Пропуски ничего не отнимают.", "A candle for any session, even ten minutes. Missed days take nothing away."), style = T.Small)
    }
}

@Composable
fun ResultSheet(h: Home, out: Outcome, hobby: Long, onClose: () -> Unit) {
    val w = h.w
    val s = h.s
    LaunchedEffect(out) {
        val ids = out.evs.filter { it.type == EvType.WAKE || it.type == EvType.RANK }.map { it.a } + out.added.map { it.id }
        h.world.celebrate(ids)
        h.world.cheer(hobby, w.cheers)
    }
    Sheet(w.t("Свеча зажжена", "A candle is lit"), onClose, w.t("В дом", "To the house")) {
        Text(w.t("${s.litCandles} из 100 в нише · всего ${s.candles}", "${s.litCandles} of 100 in the alcove · ${s.candles} in all"), style = T.Sub)
        Gap(14)
        if (out.evs.isEmpty()) {
            Text(w.t("Сегодня без больших событий — вещи «${w.hobby(s.hobby(hobby))}» просто стали чуть ближе к своему пути.", "No big news today — the things of ${w.hobby(s.hobby(hobby))} just came a little further along their way."), style = T.Body)
        }
        for (e in out.evs) {
            val th = if (e.type == EvType.WAKE || e.type == EvType.RANK) s.thing(e.a) else if (e.type == EvType.KID) s.thing(e.c) else null
            Row(Modifier.fillMaxWidth().box().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                if (th != null) { SpriteImage(th.look(), 2f); Spacer(Modifier.width(10.dp)) }
                Text(w.event(e, s), style = T.Body, modifier = Modifier.weight(1f))
            }
            Gap(8)
        }
        val grown = out.changed.filter { it.kind == app.tsukumo.domain.Kind.THING }
        if (grown.isNotEmpty()) {
            Gap(6)
            app.tsukumo.ui.components.Eyebrow(w.t("Что выросло", "What grew"))
            Gap(4)
            for (t in grown) {
                val now = s.thing(t.id) ?: t
                val before = s0Pts(out, t)
                Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                    SpriteImage(now.look(), 1.1f)
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(w.thing(now, s) + "  +" + (now.pts - before), style = T.Body)
                        app.tsukumo.ui.components.Bar(app.tsukumo.domain.Rules.progress(now), color = if (now.awake) C.goldDeep else C.accent, height = 3.dp)
                    }
                }
            }
        }
        Gap(14)
        Btn(w.t("Посмотреть на дом", "Look at the house"), Modifier.fillMaxWidth(), primary = true, onClick = onClose)
    }
}

/** A thing's points before the session that changed it. */
private fun s0Pts(out: Outcome, t: app.tsukumo.domain.Thing): Int {
    val ses = out.state.sessions.last()
    return t.pts - app.tsukumo.domain.Rules.gain(t, ses)
}
