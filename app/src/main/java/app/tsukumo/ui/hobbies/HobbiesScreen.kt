package app.tsukumo.ui.hobbies

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.tsukumo.container
import app.tsukumo.domain.Cat
import app.tsukumo.domain.Catalog
import app.tsukumo.domain.Guess
import app.tsukumo.domain.Hobby
import app.tsukumo.domain.Rules
import app.tsukumo.domain.Verb
import app.tsukumo.ui.Home
import app.tsukumo.ui.art.Sprites
import app.tsukumo.ui.components.Bar
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
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size

/** What a hobby's family brings to the session. */
fun module(w: app.tsukumo.ui.Words, c: Cat) = when (c) {
    Cat.MUSIC -> w.t("метроном в голове и таймер занятия", "a session timer for practice")
    Cat.DRAW -> w.t("таймер для набросков", "a timer for sketches")
    Cat.CRAFT -> w.t("таймер, пока вяжется ряд", "a timer while the rows grow")
    Cat.KITCHEN -> w.t("таймер, пока подходит тесто", "a timer while the dough rises")
    Cat.WORDS -> w.t("таймер для чтения и прописей", "a timer for reading and copying")
    Cat.MIND -> w.t("таймер для задач", "a timer for problems")
    Cat.MOVE -> w.t("таймер тренировки", "a workout timer")
    Cat.GARDEN -> w.t("таймер для полива и пересадки", "a timer for watering and repotting")
    Cat.BUILD -> w.t("таймер, пока сохнет клей", "a timer while the glue dries")
}

@Composable
fun HobbiesScreen(h: Home, onSpirit: (Long) -> Unit, onAddHobby: () -> Unit, onAddThing: (Long) -> Unit, onSession: (Long) -> Unit) {
    val c = LocalContext.current.container
    val w = h.w
    val s = h.s
    Column(Modifier.fillMaxSize().background(C.bg).statusBarsPadding().verticalScroll(rememberScrollState()).padding(14.dp)) {
        Text(w.t("Хобби", "Hobbies"), style = T.Display)
        Text(w.t("Вещи каждого хобби стоят на полках и от занятий оживают. Проснувшиеся духи растут дальше — годами.",
            "The things of each hobby sit on the shelves and come alive with practice. Awake spirits keep growing — for years."), style = T.Sub)
        Gap(14)
        for (hb in s.active) { HobbyCard(h, hb, onSpirit, onAddThing, onSession, onPack = { c.write { repo.archive(hb.id, true) } }); Gap(12) }
        Btn(w.t("+ Новое хобби", "+ A new hobby"), Modifier.fillMaxWidth(), primary = s.active.isEmpty(), onClick = onAddHobby)
        val special = s.things.filter { it.special }
        if (special.isNotEmpty()) {
            Gap(18); Eyebrow(w.t("Особые духи", "Special spirits")); Gap(6)
            Flow { for (t in special) Column(Modifier.clickable { onSpirit(t.id) }, horizontalAlignment = Alignment.CenterHorizontally) { SpriteImage(t.look(), 1.5f); Text(w.thing(t, s), style = T.Small, maxLines = 1) } }
        }
        val packed = s.hobbies.filter { it.archived }
        if (packed.isNotEmpty()) {
            Gap(18); Eyebrow(w.t("Кладовая", "The storeroom")); Gap(6)
            Text(w.t("Хобби на паузе. Вернётесь — духи помнят.", "Hobbies on pause. Come back and the spirits remember."), style = T.Small)
            for (hb in packed) Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("${w.catIcon(hb.cat)} ${w.hobby(hb)}", style = T.Body, modifier = Modifier.weight(1f))
                Btn(w.t("Достать", "Bring back")) { c.write { repo.archive(hb.id, false) } }
            }
        }
        Gap(20)
    }
}

@Composable
private fun HobbyCard(h: Home, hb: Hobby, onSpirit: (Long) -> Unit, onAddThing: (Long) -> Unit, onSession: (Long) -> Unit, onPack: () -> Unit) {
    val w = h.w
    val s = h.s
    val things = s.thingsOf(hb.id)
    val r = s.title(hb.id)
    val hours = s.hours(hb.id)
    var more by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().box().padding(12.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Column(Modifier.weight(1f)) {
                Text("${w.catIcon(hb.cat)} ${w.cat(hb.cat)}", style = T.Small.copy(color = C.accent))
                Text(w.hobby(hb), style = T.Title)
            }
            Text(w.t("духов: ", "spirits: ") + "${things.count { it.awake }}/${things.size}", style = T.Small)
        }
        val nx = Rules.TITLE_H.getOrNull(r + 1)
        Text("${w.titles[r]} · ${w.hours(hours)}" + (nx?.let { w.t(" · до «${w.titles[r + 1]}» ${(it - hours).toInt().coerceAtLeast(1)} ч", " · ${(it - hours).toInt().coerceAtLeast(1)} h to ${w.titles[r + 1]}") } ?: ""), style = T.Small)
        Gap(4)
        Bar(if (nx == null) 1f else ((hours - Rules.TITLE_H[r]) / (nx - Rules.TITLE_H[r])).toFloat(), color = C.goldDeep, height = 4.dp)
        Gap(10)
        for (pair in things.chunked(2)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (t in pair) Row(Modifier.weight(1f).clickable { onSpirit(t.id) }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    SpriteImage(t.look(), 1.1f)
                    Spacer(Modifier.width(6.dp))
                    Column(Modifier.weight(1f)) {
                        Text(w.thing(t, s), style = T.Body, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(if (t.awake) w.ranks[t.rank] else w.stages[t.stage], style = T.Small, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Gap(2)
                        Bar(Rules.progress(t), color = if (t.awake) C.goldDeep else C.accent, height = 3.dp)
                    }
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        if (things.isEmpty()) Text(w.t("Пока ни одной вещи — добавьте то, чем правда пользуетесь.", "No things yet — add what you really use."), style = T.Small)
        Gap(10)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Btn(w.t("Занятие", "Session"), primary = true) { onSession(hb.id) }
            Btn(w.t("+ вещь", "+ thing")) { onAddThing(hb.id) }
            Btn("…") { more = !more }
        }
        if (more) {
            Gap(8)
            Text(w.t("В мастерской: ", "In the workshop: ") + module(w, hb.cat), style = T.Small)
            Gap(6)
            Btn(w.t("Убрать в кладовую", "Put in the storeroom"), onClick = onPack)
        }
    }
}

@Composable
fun AddHobbySheet(h: Home, onClose: () -> Unit, onCustom: (Long) -> Unit) {
    val c = LocalContext.current.container
    val w = h.w
    val s = h.s
    var q by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var cat by remember { mutableStateOf(Cat.CRAFT) }
    Sheet(w.t("Новое хобби", "A new hobby"), onClose, w.t("Закрыть", "Close")) {
        Text(w.t("Заготовки — хобби вместе с вещами, которыми для него пользуются. Или заведите своё.",
            "Ready-made hobbies come with the things people use for them. Or make your own."), style = T.Sub)
        Gap(10)
        Field(q, { q = it }, w.t("поиск", "search"))
        Gap(12)
        for (ct in Cat.entries) {
            val list = Catalog.hobbies.filter { it.cat == ct && (q.isBlank() || it.name(w.ru).contains(q, true) || it.things.any { t -> t.name(w.ru).contains(q, true) }) }
            if (list.isEmpty()) continue
            Eyebrow("${w.catIcon(ct)} ${w.cat(ct)}")
            Gap(4)
            for (ch in list) {
                val have = s.hobbies.firstOrNull { it.key == ch.key }
                Column(Modifier.fillMaxWidth().box().clickable(enabled = have == null || have.archived) { c.write { repo.adopt(ch.key) }; onClose() }.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(ch.name(w.ru), style = T.Heading, modifier = Modifier.weight(1f))
                        Text(when { have == null -> w.t("завести", "add"); have.archived -> w.t("достать", "bring back"); else -> w.t("уже в доме", "in the house") },
                            style = T.Small.copy(color = if (have == null || have.archived) C.accent else C.faint))
                    }
                    Row { for (t in ch.things) SpriteImage(Sprites.Look(t.arch, t.pal, 3), .9f) }
                    Text(ch.things.joinToString(", ") { it.name(w.ru) }, style = T.Small)
                }
                Gap(6)
            }
            Gap(8)
        }
        Gap(8)
        Eyebrow(w.t("Своё хобби", "Your own hobby"))
        Gap(6)
        Field(name, { name = it }, w.t("например: оригами из купюр", "e.g. bookbinding"))
        Gap(8)
        Text(w.t("Семейство", "Family"), style = T.Small)
        Flow { for (ct in Cat.entries) Chip("${w.catIcon(ct)} ${w.cat(ct)}", cat == ct) { cat = ct } }
        Gap(10)
        Btn(w.t("Завести и добавить вещи", "Add it and its things"), Modifier.fillMaxWidth(), primary = true, enabled = name.isNotBlank()) {
            c.write { val id = repo.custom(name, cat); kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onCustom(id) } }
        }
    }
}

@Composable
fun AddThingSheet(h: Home, hobby: Long, onClose: () -> Unit) {
    val c = LocalContext.current.container
    val w = h.w
    val s = h.s
    val hb = s.hobby(hobby) ?: run { onClose(); return }
    var name by remember { mutableStateOf("") }
    var arch by remember { mutableStateOf<String?>(null) }
    var verb by remember { mutableStateOf<Verb?>(null) }
    var pal by remember { mutableStateOf<String?>(null) }
    val n = name.ifBlank { w.t("вещь", "thing") }
    val g = Guess.arch(n)
    val a = arch ?: g.arch
    val v = verb ?: Guess.verb(n)
    val p = pal ?: Guess.pal(n)
    Sheet(w.t("Новая вещь", "A new thing"), onClose, w.t("Закрыть", "Close")) {
        Text(w.t("Для «${w.hobby(hb)}». Впишите вещь, которой правда пользуетесь — силуэт, цвет и характер подберутся сами.",
            "For ${w.hobby(hb)}. Name a thing you really use — its shape, colour and nature are picked for you."), style = T.Sub)
        Gap(10)
        Field(name, { name = it }, w.t("например: кружка с котом", "e.g. the mug with a cat"))
        Gap(12)
        Row(Modifier.fillMaxWidth().box(C.field).padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            for ((st, rk) in listOf(0 to 0, 2 to 0, 3 to 0, 4 to 0, 5 to 0, 5 to 1, 5 to 2, 5 to 3, 5 to 4)) SpriteImage(Sprites.Look(a, p, st, rk, legs = if (st >= 4) 1 else 0), .95f)
        }
        Text(w.t("от вещи — до Ками, за годы", "from a thing to a kami, over years"), style = T.Small, modifier = Modifier.padding(top = 4.dp))
        Gap(4)
        Text(w.t("Силуэт: ", "Shape: ") + w.archName(a) + (if (arch == null) (if (g.byWord) w.t(" (по слову)", " (by the word)") else w.t(" (наугад)", " (by chance)")) else "") +
            " · " + w.t("характер: ", "nature: ") + w.pers(Guess.pers(n, hb.name.ifEmpty { hb.key ?: "" })), style = T.Small)
        Gap(12)
        Eyebrow(w.t("Силуэт", "Shape"))
        Flow(gap = 2.dp) {
            for (k in Catalog.ARCHS) Column(Modifier.border(2.dp, if (k == a) C.accent else Color.Transparent).clickable { arch = k }.padding(2.dp)) { SpriteImage(Sprites.Look(k, p, 3), .9f) }
        }
        Gap(12)
        Eyebrow(w.t("Цвет", "Colour"))
        Flow(gap = 4.dp) {
            for (k in Catalog.PALS) { val pl = Sprites.PALS.getValue(k); androidx.compose.foundation.layout.Box(Modifier.size(30.dp).background(Color(0xFF000000 or pl.b.toLong())).border(3.dp, if (k == p) C.gold else Color(0xFF000000 or pl.a.toLong())).clickable { pal = k }) }
        }
        Gap(12)
        Eyebrow(w.t("Растёт быстрее, когда вы", "Grows faster when you"))
        Flow { for (x in Verb.entries) Chip(w.verbYou(x), x == v) { verb = x } }
        Gap(18)
        Btn(w.t("Поставить на полку", "Put it on the shelf"), Modifier.fillMaxWidth(), primary = true, enabled = name.isNotBlank()) {
            c.write { repo.addThing(hobby, name, a, v, p) }
            onClose()
        }
    }
}
