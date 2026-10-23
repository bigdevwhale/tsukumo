package app.tsukumo.ui.chronicle

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.tsukumo.domain.Kind
import app.tsukumo.domain.Rules
import app.tsukumo.ui.Home
import app.tsukumo.ui.components.Bar
import app.tsukumo.ui.components.Eyebrow
import app.tsukumo.ui.components.Gap
import app.tsukumo.ui.components.box
import app.tsukumo.ui.theme.C
import app.tsukumo.ui.theme.T

@Composable
fun ChronicleScreen(h: Home) {
    val w = h.w
    val s = h.s
    val yi = s.year(h.nowMs)
    Column(Modifier.fillMaxSize().background(C.bg).statusBarsPadding().verticalScroll(rememberScrollState()).padding(14.dp)) {
        Text(w.t("Летопись", "Chronicle"), style = T.Display)
        Text(if (s.start > 0) w.t("Дом живёт ${yi + 1}-й год, с ${w.date(s.start)}", "Year ${yi + 1} of the house, since ${w.date(s.start)}") else w.t("Дом ещё пуст.", "The house is still empty."), style = T.Sub)
        Gap(14)

        // the house
        Column(Modifier.fillMaxWidth().box().padding(12.dp)) {
            Eyebrow(w.t("Дом", "The house"))
            Text(w.candles(s.candles), style = T.Number)
            val nx = s.nextWonder
            val prev = Rules.WONDERS.lastOrNull { it.at <= s.candles }?.at ?: 0
            if (nx != null) {
                Text(w.t("следующее чудо — ${w.wonder(nx.key)} на ${nx.at}-й свече", "next wonder: ${w.wonder(nx.key)} on candle ${nx.at}"), style = T.Small)
                Gap(4); Bar((s.candles - prev).toFloat() / (nx.at - prev), color = C.goldDeep)
            } else Text(w.t("все чудеса случились — дом стал ками", "every wonder has come — the house is a kami"), style = T.Small)
            Gap(10)
            for (wd in Rules.WONDERS) {
                val got = s.candles >= wd.at
                Row(Modifier.padding(vertical = 2.dp)) {
                    Text("${wd.at}", style = T.Small.copy(color = if (got) C.gold else C.faint), modifier = Modifier.width(44.dp))
                    Text((if (got) "✓ " else "· ") + w.wonder(wd.key), style = T.Body.copy(color = if (got) C.ink else C.faint))
                }
            }
            Gap(6)
            Text(w.t("При трёх-четырёх занятиях в неделю — примерно десять лет. Дерево в саду растёт с каждой свечой, картина на свитке меняется раз в год.",
                "At three or four sessions a week that's about ten years. The garden tree grows with every candle, the picture in the alcove changes every year."), style = T.Small)
        }
        Gap(12)

        // the spirits
        Column(Modifier.fillMaxWidth().box().padding(12.dp)) {
            Eyebrow(w.t("Духи", "Spirits"))
            val things = s.living.filter { it.kind == Kind.THING }
            Text(w.t("на полках: ${things.count { !it.awake }} · проснулись: ${things.count { it.awake }}", "on the shelves: ${things.count { !it.awake }} · awake: ${things.count { it.awake }}"), style = T.Body)
            for ((i, r) in w.ranks.withIndex()) {
                val n = things.count { it.awake && it.rank == i }
                Text("$r: $n", style = T.Sub.copy(color = if (n > 0) C.ink else C.faint))
            }
            Text(w.t("редких: ${s.things.count { it.kind == Kind.RARE }} · отпрысков: ${s.things.count { it.kind == Kind.KID }}", "rare: ${s.things.count { it.kind == Kind.RARE }} · little ones: ${s.things.count { it.kind == Kind.KID }}"), style = T.Sub)
            Gap(6)
            Text(w.t("Путь проснувшегося духа: ${w.ranks.zip(Rules.RANK.toList()).joinToString(" → ") { "${it.first} ${it.second}" }} очков. Ками — это годы одного хобби. Духи двух хобби, которыми вы занимались в один день, дружат — и однажды у них появляется отпрыск.",
                "An awake spirit's way: ${w.ranks.zip(Rules.RANK.toList()).joinToString(" → ") { "${it.first} ${it.second}" }} points. A kami takes years of one hobby. Spirits of two hobbies practised on the same day become friends — and one day they have a little one."), style = T.Small)
        }
        Gap(12)

        // mastery
        Column(Modifier.fillMaxWidth().box().padding(12.dp)) {
            Eyebrow(w.t("Мастерство", "Mastery"))
            for (hb in s.hobbies) {
                val r = s.title(hb.id); val hh = s.hours(hb.id); val nx = Rules.TITLE_H.getOrNull(r + 1)
                Gap(6)
                Text("${w.catIcon(hb.cat)} ${w.hobby(hb)}" + if (hb.archived) w.t(" (в кладовой)", " (in the storeroom)") else "", style = T.Body)
                Text("${w.titles[r]} · ${w.hours(hh)}", style = T.Small)
                Bar(if (nx == null) 1f else ((hh - Rules.TITLE_H[r]) / (nx - Rules.TITLE_H[r])).toFloat(), color = C.goldDeep, height = 4.dp)
            }
            Gap(6)
            Text(w.t("Звания по часам: ", "Titles by hours: ") + w.titles.zip(Rules.TITLE_H.toList()).joinToString(" · ") { "${it.first} ${it.second}" }, style = T.Small)
        }
        Gap(12)

        // year by year
        Eyebrow(w.t("По годам", "Year by year"))
        for (y in yi downTo 0) {
            val ss = s.sessions.filter { s.year(it.at) == y }
            val evs = s.evs.filter { s.year(it.at) == y && w.big(it) }
            var open by remember(y) { mutableIntStateOf(if (y == yi) 1 else 0) }
            Column(Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { open = 1 - open }) {
                Row {
                    Text(w.t("${y + 1}-й год", "Year ${y + 1}"), style = T.Heading, modifier = Modifier.weight(1f))
                    Text("${w.times(ss.size)} · ${w.hours(ss.sumOf { it.minutes } / 60.0)}", style = T.Small)
                }
                if (open == 1) for (e in evs.reversed()) Row(Modifier.padding(top = 3.dp)) {
                    Text("·", style = T.Body.copy(color = C.accent)); Spacer(Modifier.width(8.dp)); Text(w.event(e, s), style = T.Sub)
                }
            }
        }
        Gap(12)

        // notes
        val notes = s.sessions.filter { it.note.isNotBlank() }.takeLast(30).reversed()
        if (notes.isNotEmpty()) {
            Eyebrow(w.t("Заметки", "Notes"))
            for (n in notes) Column(Modifier.padding(vertical = 4.dp)) {
                Text("${w.date(n.at)} · ${w.hobby(s.hobby(n.hobbyId))} · ${w.minutes(n.minutes)}", style = T.Small)
                Text(n.note, style = T.Body)
            }
        }
        Gap(20)
    }
}
