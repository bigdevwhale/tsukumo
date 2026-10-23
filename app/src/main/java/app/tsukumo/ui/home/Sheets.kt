package app.tsukumo.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import app.tsukumo.container
import app.tsukumo.domain.Engine
import app.tsukumo.domain.Kind
import app.tsukumo.domain.Rules
import app.tsukumo.ui.Home
import app.tsukumo.ui.components.Bar
import app.tsukumo.ui.components.Btn
import app.tsukumo.ui.components.Eyebrow
import app.tsukumo.ui.components.Field
import app.tsukumo.ui.components.Gap
import app.tsukumo.ui.components.Sheet
import app.tsukumo.ui.components.SpriteImage
import app.tsukumo.ui.components.box
import app.tsukumo.ui.components.look
import app.tsukumo.ui.theme.C
import app.tsukumo.ui.theme.T

@Composable
fun SpiritSheet(h: Home, id: Long, onClose: () -> Unit, onSession: (Long) -> Unit) {
    val c = LocalContext.current.container
    val w = h.w
    val s = h.s
    val t = s.thing(id) ?: run { onClose(); return }
    var renaming by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(w.thing(t, s)) }
    var letGo by remember { mutableStateOf(false) }
    Sheet(w.thing(t, s), onClose, w.t("Закрыть", "Close")) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SpriteImage(t.look(), 4f)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                val hb = s.hobby(t.hobbyId)
                if (hb != null) Eyebrow(w.hobby(hb))
                Text(if (t.awake) w.ranks[t.rank] else w.stages[t.stage], style = T.Heading)
                Text(w.t("силуэт: ", "shape: ") + w.archName(t.arch), style = T.Small)
                Text(w.t("характер: ", "nature: ") + w.pers(t.pers), style = T.Small)
            }
        }
        Gap(14)
        when (t.kind) {
            Kind.RARE -> {
                val wd = Rules.WONDERS.first { it.key == t.rare }
                Text(w.t("Редкий дух: ${w.wonderDesc(wd.key)}. Пришёл на ${wd.at}-й свече.", "A rare spirit: ${w.wonderDesc(wd.key)}. It came on candle ${wd.at}."), style = T.Body)
            }
            Kind.KID -> Text(w.t("Отпрыск двух подружившихся духов: «${w.thing(s.thing(t.parentA), s)}» и «${w.thing(s.thing(t.parentB), s)}». Силуэт от одного, цвет от другого.",
                "The little one of two friends, “${w.thing(s.thing(t.parentA), s)}” and “${w.thing(s.thing(t.parentB), s)}”: one's shape, the other's colour."), style = T.Body)
            Kind.THING -> {
                val next = Rules.toNext(t)
                Text(
                    if (!t.awake) w.t("До следующей ступени — $next оч.", "$next points to the next step")
                    else if (next > 0) w.t("До ранга «${w.ranks[t.rank + 1]}» — $next оч.", "$next points to ${w.ranks[t.rank + 1]}")
                    else w.t("Высший ранг. Дом гордится.", "The highest rank. The house is proud."), style = T.Body,
                )
                Gap(6)
                Bar(Rules.progress(t), color = if (t.awake) C.goldDeep else C.accent, height = 7.dp)
                Gap(10)
                Text(w.t("Растёт быстрее, когда вы ${w.verbYou(t.verb)}.", "Grows faster when you ${w.verbYou(t.verb)}."), style = T.Sub)
                Gap(10)
                Eyebrow(w.t("Путь", "The way"))
                Text(buildAnnotatedString {
                    val steps = w.stages.drop(1).take(4) + w.ranks
                    val reached = if (t.awake) 4 + t.rank else t.stage - 1
                    steps.forEachIndexed { i, x -> if (i > 0) append(" → "); if (i <= reached) withStyle(SpanStyle(color = C.gold)) { append(x) } else append(x) }
                }, style = T.Small)
                if (t.stage == 0) { Gap(8); Text(w.t("Пока это просто вещь. Но если присмотреться…", "Just a thing, for now. But look closely…"), style = T.Small) }
            }
        }
        val fr = Engine.friends(s, t.id)
        if (fr.isNotEmpty()) {
            Gap(12); Eyebrow(w.t("Дружит с", "Friends"))
            for ((f, d) in fr.take(5)) Text("${w.thing(f, s)} · ${w.days(d)}", style = T.Sub)
        }
        Gap(18)
        if (t.stage >= 3) Btn(w.t("Погладить", "Pet"), Modifier.fillMaxWidth(), primary = true) {
            h.world.actors[t.id]?.let { a -> h.world.say(a, w.pets.random()); a.cel = h.world.t + 1 }
            onClose()
        }
        val hb = s.hobby(t.hobbyId)
        if (hb != null && !hb.archived) { Gap(8); Btn(w.t("Занятие: ${w.hobby(hb)}", "Session: ${w.hobby(hb)}"), Modifier.fillMaxWidth()) { onSession(hb.id) } }
        if (t.kind == Kind.THING) {
            Gap(8)
            if (renaming) {
                Field(name, { name = it }, w.t("Имя", "Name"))
                Gap(6)
                Btn(w.t("Сохранить имя", "Save the name"), Modifier.fillMaxWidth()) { if (name.isNotBlank()) c.write { repo.renameThing(t.id, name) }; renaming = false }
            } else Btn(w.t("Дать имя", "Give it a name"), Modifier.fillMaxWidth()) { renaming = true }
            Gap(8)
            if (!letGo) Text(w.t("Отпустить вещь…", "Let this thing go…"), style = T.Small, modifier = Modifier.clickable { letGo = true }.padding(6.dp))
            else Btn(w.t("Да, отпустить: вещь и её путь исчезнут", "Yes, let go: the thing and its way are gone"), Modifier.fillMaxWidth()) { c.write { repo.removeThing(t.id) }; onClose() }
        }
    }
}

@Composable
fun ChestSheet(h: Home, onClose: () -> Unit) {
    val c = LocalContext.current.container
    val w = h.w
    val s = h.s
    val packed = s.hobbies.filter { it.archived }
    Sheet(w.t("Кладовая", "The storeroom"), onClose, w.t("Закрыть", "Close")) {
        Text(w.t("Хобби на паузе. Их духи спят в сундуке и помнят вас — очки, ранги и часы никуда не деваются.",
            "Hobbies on pause. Their spirits sleep in the chest and remember you — points, ranks and hours stay."), style = T.Sub)
        Gap(12)
        if (packed.isEmpty()) Text(w.t("Сундук пуст. Хобби можно убрать сюда на вкладке «Хобби».", "The chest is empty. A hobby can be put away on the Hobbies tab."), style = T.Body)
        for (hb in packed) {
            val things = s.thingsOf(hb.id)
            Column(Modifier.fillMaxWidth().box().padding(12.dp)) {
                Text("${w.catIcon(hb.cat)} ${w.hobby(hb)}", style = T.Heading)
                Text("${w.titles[s.title(hb.id)]} · ${w.hours(s.hours(hb.id))} · ${w.t("духов", "spirits")}: ${things.count { it.awake }}", style = T.Small)
                Gap(8)
                Btn(w.t("Достать", "Bring it back"), primary = true) { c.write { repo.archive(hb.id, false) }; onClose() }
            }
            Gap(8)
        }
    }
}

@Composable
fun CandlesSheet(h: Home, onClose: () -> Unit, onChronicle: () -> Unit) {
    val w = h.w
    val s = h.s
    Sheet(w.t("Сто свечей", "A hundred candles"), onClose, w.t("Закрыть", "Close")) {
        Text(w.t("Каждое занятие любым хобби зажигает одну свечу в нише. Горит ${s.litCandles} из 100, всего зажжено ${s.candles}.",
            "Every session of any hobby lights one candle in the alcove. ${s.litCandles} of 100 are burning, ${s.candles} lit in all."), style = T.Body)
        Gap(10)
        Text(w.t("В старину рассказывали сто страшных историй при ста свечах — Хякумоногатари. Здесь каждая сотня свечей — праздник, а иногда и чудо.",
            "In old Japan people told a hundred ghost stories by a hundred candles — Hyakumonogatari. Here every hundred candles is a party, and sometimes a wonder."), style = T.Sub)
        Gap(14)
        Eyebrow(w.t("Чудеса дома", "Wonders of the house"))
        for (wd in Rules.WONDERS) {
            val got = s.candles >= wd.at
            Row(Modifier.padding(vertical = 3.dp)) {
                Text("${wd.at}", style = T.Body.copy(color = if (got) C.gold else C.faint), modifier = Modifier.width(52.dp))
                Column {
                    Text((if (got) "✓ " else "· ") + w.wonder(wd.key), style = T.Body.copy(color = if (got) C.ink else C.faint))
                    Text(w.wonderDesc(wd.key), style = T.Small.copy(color = if (got) C.muted else C.faint))
                }
            }
        }
        Gap(14)
        Btn(w.t("Летопись", "The chronicle"), Modifier.fillMaxWidth(), onClick = onChronicle)
    }
}
