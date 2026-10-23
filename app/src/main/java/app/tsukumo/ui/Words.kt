package app.tsukumo.ui

import app.tsukumo.domain.Cat
import app.tsukumo.domain.Catalog
import app.tsukumo.domain.Ev
import app.tsukumo.domain.EvType
import app.tsukumo.domain.Hobby
import app.tsukumo.domain.Kind
import app.tsukumo.domain.Pers
import app.tsukumo.domain.State
import app.tsukumo.domain.Thing
import app.tsukumo.domain.Verb
import app.tsukumo.domain.date
import app.tsukumo.domain.Rules
import app.tsukumo.ui.art.Mood
import app.tsukumo.ui.art.Season
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.random.Random

/** The words of the house that come from data rather than resources, in both languages. */
class Words(val ru: Boolean) {
    fun t(r: String, e: String) = if (ru) r else e

    fun plural(n: Int, one: String, few: String, many: String): String {
        val a = kotlin.math.abs(n) % 100; val b = a % 10
        return when { a in 11..19 -> many; b == 1 -> one; b in 2..4 -> few; else -> many }
    }

    fun years(n: Int) = if (ru) "$n ${plural(n, "год", "года", "лет")}" else "$n year${if (n == 1) "" else "s"}"
    fun days(n: Int) = if (ru) "$n ${plural(n, "день", "дня", "дней")}" else "$n day${if (n == 1) "" else "s"}"
    fun hours(h: Double) = if (h < 10) String.format(Locale.US, "%.1f", h).let { if (ru) "$it ч" else "$it h" } else if (ru) "${h.toInt()} ч" else "${h.toInt()} h"
    fun minutes(m: Int) = if (ru) "$m мин" else "$m min"
    fun candles(n: Int) = if (ru) "$n ${plural(n, "свеча", "свечи", "свечей")}" else "$n candle${if (n == 1) "" else "s"}"
    fun times(n: Int) = if (ru) "$n ${plural(n, "занятие", "занятия", "занятий")}" else "$n session${if (n == 1) "" else "s"}"

    fun hobby(h: Hobby?) = when { h == null -> ""; h.name.isNotBlank() -> h.name; else -> Catalog.hobby(h.key)?.name(ru) ?: "?" }

    fun thing(t: Thing?, s: State? = null): String = when {
        t == null -> "?"
        t.name.isNotBlank() -> t.name
        t.kind == Kind.RARE -> rare(t.rare)
        t.kind == Kind.KID && s != null -> {
            val a = thing(s.thing(t.parentA), s); val b = thing(s.thing(t.parentB), s)
            "$a-${b.lowercase()}"
        }
        t.kind == Kind.KID -> t("Малыш", "Little one")
        else -> Catalog.thing(t.key)?.name(ru) ?: "?"
    }

    fun rare(k: String?) = when (k) {
        "obake" -> t("Тётин-обакэ", "Chōchin-obake"); "tanuki" -> t("Тануки", "Tanuki"); "kitsune" -> t("Кицунэ", "Kitsune"); "warashi" -> t("Дзасики-вараси", "Zashiki-warashi")
        else -> t("Дух", "Spirit")
    }

    val stages get() = if (ru) listOf("Просто вещь", "Иногда вздрагивает", "Один глаз открыт", "Оба глаза открыты", "Отросли ножки", "Дух дома")
        else listOf("Just a thing", "Shivers now and then", "One eye open", "Both eyes open", "Has little legs", "A house spirit")
    private val wakeEv get() = if (ru) listOf("", "— первое шевеление", "— приоткрылся один глаз", "— открылись оба глаза", "— отросли ножки", "— теперь дух дома: с полки на пол!")
        else listOf("", "shivers", "opens one eye", "opens both eyes", "grows little legs", "steps off the shelf — a house spirit now!")

    val ranks get() = if (ru) listOf("Дух дома", "Хранитель", "Старейшина", "Мудрец", "Ками") else listOf("House spirit", "Keeper", "Elder", "Sage", "Kami")
    private val rankEv get() = if (ru) listOf("", "— ранг Хранитель: красный шарф", "— ранг Старейшина: седые брови и патина", "— ранг Мудрец: соломенная шляпа", "— ранг Ками: над головой нимб")
        else listOf("", "becomes a Keeper and ties on a red scarf", "becomes an Elder: white brows and patina", "becomes a Sage and puts on a straw hat", "becomes a Kami — a halo lights up above it")
    val titles get() = if (ru) listOf("Новичок", "Ученик", "Подмастерье", "Мастер", "Сэнсэй", "Хранитель ремесла", "Живое сокровище")
        else listOf("Beginner", "Student", "Journeyman", "Master", "Sensei", "Keeper of the craft", "Living treasure")

    fun verb(v: Verb) = when (v) {
        Verb.REPEAT -> t("Повторять", "Repeat"); Verb.NEW -> t("Пробовать новое", "Try something new"); Verb.FIX -> t("Исправлять", "Fix")
        Verb.FINISH -> t("Завершать", "Finish"); Verb.LEARN -> t("Учиться", "Learn"); Verb.SHARE -> t("Делиться", "Share")
    }
    fun verbHint(v: Verb) = when (v) {
        Verb.REPEAT -> t("гаммы, петли, этюды", "scales, rows, drills"); Verb.NEW -> t("техника, рецепт, приём", "a technique, a recipe")
        Verb.FIX -> t("переделать, распустить", "redo, unpick, review"); Verb.FINISH -> t("довести работу до конца", "see a piece through")
        Verb.LEARN -> t("урок, книга, видео", "a lesson, a book, a video"); Verb.SHARE -> t("показать, подарить", "show it, give it away")
    }
    /** "…grows faster when you ___" */
    fun verbYou(v: Verb) = when (v) {
        Verb.REPEAT -> t("повторяете", "repeat"); Verb.NEW -> t("пробуете новое", "try something new"); Verb.FIX -> t("исправляете ошибки", "fix mistakes")
        Verb.FINISH -> t("доводите дело до конца", "finish things"); Verb.LEARN -> t("учитесь", "learn"); Verb.SHARE -> t("делитесь с кем-то", "share with someone")
    }

    fun pers(p: Pers) = when (p) {
        Pers.SHY -> t("застенчивый", "shy"); Pers.BOLD -> t("озорной", "mischievous"); Pers.SLEEPY -> t("сонный", "sleepy")
        Pers.CURIOUS -> t("любопытный", "curious"); Pers.PROUD -> t("гордый", "proud"); Pers.CARING -> t("заботливый", "caring")
    }

    fun cat(c: Cat) = when (c) {
        Cat.MUSIC -> t("Музыка", "Music"); Cat.DRAW -> t("Рисунок и фото", "Drawing and photo"); Cat.CRAFT -> t("Рукоделие", "Crafts")
        Cat.KITCHEN -> t("Кухня и чай", "Kitchen and tea"); Cat.WORDS -> t("Слова", "Words"); Cat.MIND -> t("Ум", "Mind")
        Cat.MOVE -> t("Движение", "Movement"); Cat.GARDEN -> t("Сад", "Garden"); Cat.BUILD -> t("Мастерская", "Making")
    }
    fun catIcon(c: Cat) = when (c) { Cat.MUSIC -> "♪"; Cat.DRAW -> "✎"; Cat.CRAFT -> "✂"; Cat.KITCHEN -> "♨"; Cat.WORDS -> "文"; Cat.MIND -> "♞"; Cat.MOVE -> "◎"; Cat.GARDEN -> "❀"; Cat.BUILD -> "⚒" }

    fun archName(a: String) = when (a) {
        "pencil" -> t("палочка", "stick"); "brush" -> t("кисточка", "brush"); "yarn" -> t("клубок", "ball of yarn"); "book" -> t("книжка", "book")
        "jar" -> t("баночка", "jar"); "box" -> t("коробочка", "box"); "guitar" -> t("струнный", "strings"); "pick" -> t("медиатор", "pick")
        "pot" -> t("горшочек", "pot"); "stone" -> t("камешек", "pebble"); "cup" -> t("чашечка", "cup"); "bottle" -> t("бутылочка", "bottle")
        "knight" -> t("фигурка", "figure"); "sheet" -> t("листок", "sheet"); "bell" -> t("колокольчик", "bell"); "lantern" -> t("фонарик", "lantern")
        "spool" -> t("катушка", "spool"); "camera" -> t("камера", "camera"); "mat" -> t("свёрток", "roll"); "ball" -> t("мячик", "ball"); "clock" -> t("часики", "clock")
        else -> a
    }

    fun wonder(k: String) = when (k) {
        "obake" -> t("Тётин-обакэ", "Chōchin-obake"); "furin" -> t("Фурин", "Fūrin"); "koi" -> t("Карпы кои", "Koi"); "toro" -> t("Огонь в фонаре", "The lantern lit")
        "bonsai" -> t("Бонсай", "Bonsai"); "tanuki" -> t("Тануки", "Tanuki"); "flies" -> t("Светлячки", "Fireflies"); "kitsune" -> t("Кицунэ", "Kitsune")
        "tree" -> t("Большое дерево", "The great tree"); "shimenawa" -> t("Симэнава", "Shimenawa"); "warashi" -> t("Дзасики-вараси", "Zashiki-warashi"); "gold" -> t("Дом-ками", "The house a kami")
        else -> k
    }
    fun wonderDesc(k: String) = when (k) {
        "obake" -> t("бумажный фонарь-дух приходит в Ночь ста свечей", "a paper-lantern spirit comes on the Night of a Hundred Candles")
        "furin" -> t("стеклянный колокольчик под стрехой", "a glass wind chime under the eaves")
        "koi" -> t("в пруду заводятся карпы", "carp come to the pond")
        "toro" -> t("каменный фонарь в саду светится по ночам", "the stone lantern glows at night")
        "bonsai" -> t("на веранде появляется сосна в горшке", "a little pine appears on the engawa")
        "tanuki" -> t("редкий дух с листком на голове", "a rare spirit with a leaf on its head")
        "flies" -> t("летними ночами в саду", "in the garden on summer nights")
        "kitsune" -> t("белая лиса-дух", "a white fox spirit")
        "tree" -> t("дерево в саду выросло в полную силу", "the garden tree is fully grown")
        "shimenawa" -> t("священная верёвка на дереве — оно стало ками", "a sacred rope on the tree: it has become a kami")
        "warashi" -> t("дух-ребёнок, приносит дому счастье", "a child spirit who brings the house luck")
        "gold" -> t("дом сам стал духом и светится золотом", "the house itself has become a spirit and glows gold")
        else -> ""
    }

    fun season(s: Season) = when (s) { Season.SPRING -> t("весна", "spring"); Season.SUMMER -> t("лето", "summer"); Season.AUTUMN -> t("осень", "autumn"); Season.WINTER -> t("зима", "winter") }

    /** One line of the chronicle. */
    fun event(e: Ev, s: State): String {
        fun th(id: Long) = "«${thing(s.thing(id), s)}»".let { if (ru) it else "“${thing(s.thing(id), s)}”" }
        fun hb(id: Long) = hobby(s.hobby(id))
        return when (e.type) {
            EvType.MOVE_IN -> t("Дом заселён: на полках первые вещи.", "The house moved in: the first things on the shelves.")
            EvType.HOBBY -> t("Новое хобби: ${hb(e.a)}.", "A new hobby: ${hb(e.a)}.")
            EvType.WAKE -> "${th(e.a)} ${wakeEv.getOrElse(e.n) { "" }}"
            EvType.RANK -> "${th(e.a)} ${rankEv.getOrElse(e.n) { "" }}"
            EvType.TITLE -> t("${hb(e.a)}: теперь вы ${titles[e.n]} — ${Rules.TITLE_H[e.n]} ч практики", "${hb(e.a)}: you are now a ${titles[e.n]} — ${Rules.TITLE_H[e.n]} h of practice")
            EvType.WONDER -> t("${e.n} свечей! ${wonder(e.s)}: ${wonderDesc(e.s)}.", "${e.n} candles! ${wonder(e.s)}: ${wonderDesc(e.s)}.")
            EvType.CANDLES -> t("Ночь ста свечей (${e.n}). Духи устраивают праздник.", "A Night of a Hundred Candles (${e.n}). The spirits have a party.")
            EvType.YEAR -> t("Дому исполнилось ${years(e.n)}. На свитке в нише — новая картина.", "The house is ${years(e.n)} old. A new picture hangs in the alcove.")
            EvType.KID -> t("У ${th(e.a)} и ${th(e.b)} появился отпрыск — ${th(e.c)}.", "${th(e.a)} and ${th(e.b)} have a little one: ${th(e.c)}.")
            EvType.PAUSE -> t("В кладовую: ${hb(e.a).lowercase()}. Духи спят в сундуке — без обид.", "${hb(e.a)} goes into the storeroom. Its spirits sleep in the chest, no hard feelings.")
            EvType.RETURN -> t("Снова ${hb(e.a).lowercase()} — спустя ${days(e.n)}. Духи помнят.", "Back to ${hb(e.a).lowercase()} after ${days(e.n)}. The spirits remember.")
        }
    }

    /** Is this event big enough for the yearly chronicle? */
    fun big(e: Ev) = e.type != EvType.WAKE || e.n == 5

    fun date(ms: Long): String = ms.date().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(if (ru) Locale("ru") else Locale.ENGLISH))

    private val rnd = Random(3)

    /** Something a spirit says out loud. */
    fun line(th: Thing, m: Mood, s: State?): String {
        if (th.stage < 5) return (if (ru) listOf("…", "♪", "скоро…", "хочу вниз", "(смотрит на вас)") else listOf("…", "♪", "soon…", "let me down", "(looks at you)")).random(rnd)
        val p = when (th.pers) {
            Pers.SHY -> t("…|ой|можно я тут посижу?|(прячется)", "…|oh|may I sit here?|(hides)")
            Pers.BOLD -> t("ещё раз!|хоп!|давай громче!|спорим, я быстрее?", "again!|hop!|louder!|bet I'm faster?")
            Pers.SLEEPY -> t("zzz|ещё пять минут…|уаааа|тёпленько…", "zzz|five more minutes…|yaaawn|so warm…")
            Pers.CURIOUS -> t("а это что?|?|пойдём посмотрим!|а завтра что делаем?", "what's that?|?|let's go and look!|what shall we do tomorrow?")
            Pers.PROUD -> t("смотри, как я!|хм-хм|★|я тут самый блестящий", "look at me!|hm-hm|★|I'm the shiniest here")
            Pers.CARING -> t("чаю?|♥|ты молодец|не торопись", "tea?|♥|well done you|no hurry")
        }.split('|')
        val extra = mutableListOf(
            when (m.season) { Season.SPRING -> t("сакура!", "blossom!"); Season.SUMMER -> t("жарко… цикады", "hot… cicadas"); Season.AUTUMN -> t("листья падают…", "leaves falling…"); Season.WINTER -> t("холодно, к котацу!", "cold! to the kotatsu!") },
        )
        s?.hobby(th.hobbyId)?.let { extra += t("${hobby(it).lowercase()} сегодня?", "${hobby(it).lowercase()} today?") }
        if (m.night) extra += t("звёзды…", "stars…")
        if (th.kind == Kind.KID) extra += t("я ещё маленький", "I'm still little")
        if (th.rank >= 3) extra += t("я помню, как всё начиналось", "I remember how it all began")
        return (if (rnd.nextDouble() < .65) p else extra).random(rnd)
    }

    val cheers get() = if (ru) listOf("♪", "ура!", "ещё!", "★") else listOf("♪", "yay!", "more!", "★")
    val pets get() = if (ru) listOf("♥", "хи-хи", "мурр…", "ещё!") else listOf("♥", "hee-hee", "purr…", "more!")
}
