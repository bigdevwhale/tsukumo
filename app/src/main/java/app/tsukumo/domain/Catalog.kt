package app.tsukumo.domain

import app.tsukumo.domain.Verb.FINISH
import app.tsukumo.domain.Verb.FIX
import app.tsukumo.domain.Verb.LEARN
import app.tsukumo.domain.Verb.NEW
import app.tsukumo.domain.Verb.REPEAT
import app.tsukumo.domain.Verb.SHARE

class CThing(val key: String, val ru: String, val en: String, val arch: String, val verb: Verb, val pal: String) {
    fun name(ru: Boolean) = if (ru) this.ru else en
}

class CHobby(val key: String, val cat: Cat, val ru: String, val en: String, val things: List<CThing>) {
    fun name(ru: Boolean) = if (ru) this.ru else en
}

/** Ready-made hobbies with the things people really use for them. Any hobby can also be made from scratch. */
object Catalog {
    private fun t(ru: String, en: String, arch: String, verb: Verb, pal: String) = CThing("", ru, en, arch, verb, pal)
    private fun h(key: String, cat: Cat, ru: String, en: String, vararg ts: CThing) =
        CHobby(key, cat, ru, en, ts.mapIndexed { i, x -> CThing("$key.$i", x.ru, x.en, x.arch, x.verb, x.pal) })

    val hobbies: List<CHobby> = listOf(
        h("guitar", Cat.MUSIC, "Гитара", "Guitar",
            t("Гитара", "Guitar", "guitar", FINISH, "wood"), t("Медиатор", "Pick", "pick", NEW, "persimmon"),
            t("Метроном", "Metronome", "clock", REPEAT, "cocoa"), t("Песенник", "Songbook", "book", LEARN, "indigo"),
            t("Запасные струны", "Spare strings", "spool", FIX, "slate"), t("Кружка", "Mug", "cup", SHARE, "cream")),
        h("ukulele", Cat.MUSIC, "Укулеле", "Ukulele",
            t("Укулеле", "Ukulele", "guitar", FINISH, "mustard"), t("Тюнер-прищепка", "Clip tuner", "clock", REPEAT, "sky"),
            t("Сборник аккордов", "Chord book", "book", LEARN, "plum"), t("Войлочный медиатор", "Felt pick", "pick", NEW, "sakura")),
        h("piano", Cat.MUSIC, "Фортепиано", "Piano",
            t("Ноты", "Sheet music", "sheet", LEARN, "cream"), t("Метроном", "Metronome", "clock", REPEAT, "wood"),
            t("Сборник пьес", "Book of pieces", "book", FINISH, "indigo"), t("Карандаш для пометок", "Marking pencil", "pencil", FIX, "mustard"),
            t("Чашка чая", "Cup of tea", "cup", SHARE, "matcha")),
        h("singing", Cat.MUSIC, "Пение", "Singing",
            t("Камертон", "Tuning bell", "bell", REPEAT, "mustard"), t("Песенник", "Songbook", "book", LEARN, "sakura"),
            t("Бутылка воды", "Water bottle", "bottle", FINISH, "sky"), t("Тетрадь распевок", "Warm-up notebook", "sheet", FIX, "cream")),
        h("drums", Cat.MUSIC, "Барабаны", "Drums",
            t("Палочки", "Drumsticks", "pencil", REPEAT, "wood"), t("Пэд", "Practice pad", "ball", NEW, "slate"),
            t("Метроном", "Metronome", "clock", FIX, "persimmon"), t("Сборник ритмов", "Book of grooves", "book", LEARN, "plum")),

        h("drawing", Cat.DRAW, "Рисование", "Drawing",
            t("Карандаш", "Pencil", "pencil", REPEAT, "mustard"), t("Ластик", "Eraser", "box", FIX, "sakura"),
            t("Кисть", "Brush", "brush", NEW, "cocoa"), t("Скетчбук", "Sketchbook", "book", FINISH, "matcha"),
            t("Банка с водой", "Water jar", "jar", LEARN, "sky"), t("Открытка", "Postcard", "sheet", SHARE, "cream")),
        h("watercolor", Cat.DRAW, "Акварель", "Watercolour",
            t("Кювета красок", "Paint pan", "box", NEW, "sky"), t("Кисть-белка", "Squirrel brush", "brush", REPEAT, "wood"),
            t("Баночка с водой", "Water jar", "jar", LEARN, "matcha"), t("Лист акварельной бумаги", "Watercolour paper", "sheet", FINISH, "cream"),
            t("Губка", "Sponge", "box", FIX, "mustard")),
        h("digital", Cat.DRAW, "Цифровой рисунок", "Digital art",
            t("Стилус", "Stylus", "pencil", REPEAT, "slate"), t("Планшет", "Tablet", "box", NEW, "indigo"),
            t("Папка референсов", "Reference folder", "book", LEARN, "plum"), t("Распечатка", "Print", "sheet", SHARE, "cream")),
        h("photo", Cat.DRAW, "Фотография", "Photography",
            t("Камера", "Camera", "camera", NEW, "slate"), t("Плёнка", "Film roll", "spool", REPEAT, "persimmon"),
            t("Фотоальбом", "Photo album", "book", FINISH, "cocoa"), t("Отпечаток", "Print", "sheet", SHARE, "cream")),

        h("knitting", Cat.CRAFT, "Вязание", "Knitting",
            t("Клубок", "Ball of yarn", "yarn", REPEAT, "sakura"), t("Спицы", "Needles", "pencil", NEW, "slate"),
            t("Счётчик рядов", "Row counter", "clock", FIX, "mustard"), t("Книга узоров", "Pattern book", "book", LEARN, "plum"),
            t("Шарф", "Scarf", "mat", FINISH, "persimmon"), t("Шкатулка с пуговицами", "Button box", "box", SHARE, "wood")),
        h("crochet", Cat.CRAFT, "Вязание крючком", "Crochet",
            t("Крючок", "Hook", "pencil", REPEAT, "sky"), t("Пряжа", "Yarn", "yarn", NEW, "matcha"),
            t("Схема", "Chart", "sheet", LEARN, "cream"), t("Игрушка", "Amigurumi", "ball", FINISH, "mustard"),
            t("Маркеры петель", "Stitch markers", "spool", FIX, "sakura")),
        h("embroidery", Cat.CRAFT, "Вышивка", "Embroidery",
            t("Мулине", "Floss", "spool", REPEAT, "sakura"), t("Пяльцы", "Hoop", "clock", FINISH, "wood"),
            t("Схема", "Pattern", "sheet", LEARN, "cream"), t("Игольница", "Pincushion", "ball", FIX, "matcha")),
        h("sewing", Cat.CRAFT, "Шитьё", "Sewing",
            t("Катушка ниток", "Spool of thread", "spool", REPEAT, "indigo"), t("Подушечка для игл", "Pincushion", "ball", FIX, "persimmon"),
            t("Выкройка", "Pattern", "sheet", LEARN, "cream"), t("Отрез ткани", "Fabric", "mat", FINISH, "plum"),
            t("Коробка с лоскутами", "Scrap box", "box", NEW, "wood")),
        h("origami", Cat.CRAFT, "Оригами", "Origami",
            t("Бумага для оригами", "Origami paper", "sheet", REPEAT, "sakura"), t("Книга схем", "Book of folds", "book", LEARN, "indigo"),
            t("Журавлик", "Paper crane", "pick", FINISH, "cream"), t("Коробочка", "Little box", "box", SHARE, "persimmon")),
        h("beads", Cat.CRAFT, "Украшения", "Jewellery",
            t("Баночка бусин", "Jar of beads", "jar", REPEAT, "sky"), t("Леска", "Thread", "spool", FIX, "slate"),
            t("Шкатулка", "Trinket box", "box", SHARE, "plum"), t("Схема", "Pattern", "sheet", LEARN, "cream")),

        h("sourdough", Cat.KITCHEN, "Хлеб на закваске", "Sourdough",
            t("Закваска", "Starter", "jar", REPEAT, "cream"), t("Кухонные весы", "Kitchen scale", "clock", FIX, "slate"),
            t("Банетон", "Banneton", "box", NEW, "wood"), t("Тетрадь рецептов", "Recipe notebook", "book", LEARN, "persimmon"),
            t("Каравай", "Loaf", "stone", FINISH, "cocoa"), t("Полотенце", "Tea towel", "mat", SHARE, "sky")),
        h("tea", Cat.KITCHEN, "Чай", "Tea",
            t("Пиала", "Tea bowl", "cup", REPEAT, "matcha"), t("Чайница", "Tea caddy", "jar", LEARN, "cocoa"),
            t("Колокольчик", "Little bell", "bell", SHARE, "mustard"), t("Чайник", "Teapot", "jar", NEW, "persimmon")),
        h("coffee", Cat.KITCHEN, "Кофе", "Coffee",
            t("Кофемолка", "Grinder", "jar", REPEAT, "cocoa"), t("Турка", "Cezve", "bottle", NEW, "persimmon"),
            t("Весы", "Scale", "clock", FIX, "slate"), t("Кружка", "Mug", "cup", SHARE, "cream")),
        h("baking", Cat.KITCHEN, "Выпечка", "Baking",
            t("Скалка", "Rolling pin", "pencil", REPEAT, "wood"), t("Миска", "Mixing bowl", "cup", NEW, "sky"),
            t("Форма для пирога", "Pie tin", "box", FINISH, "slate"), t("Книга рецептов", "Recipe book", "book", LEARN, "sakura"),
            t("Таймер", "Timer", "clock", FIX, "mustard")),
        h("cooking", Cat.KITCHEN, "Готовка", "Cooking",
            t("Деревянная ложка", "Wooden spoon", "pencil", REPEAT, "wood"), t("Баночка специй", "Spice jar", "jar", NEW, "persimmon"),
            t("Тетрадь рецептов", "Recipe notebook", "book", LEARN, "matcha"), t("Фартук", "Apron", "mat", SHARE, "indigo"),
            t("Кастрюлька", "Saucepan", "cup", FINISH, "slate")),

        h("language", Cat.WORDS, "Иностранный язык", "A language",
            t("Карточки", "Flashcards", "sheet", REPEAT, "cream"), t("Словарь", "Dictionary", "book", LEARN, "indigo"),
            t("Тетрадь ошибок", "Mistake notebook", "sheet", FIX, "sakura"), t("Книжка на языке", "A book in it", "book", FINISH, "matcha"),
            t("Чашка", "Cup", "cup", SHARE, "persimmon")),
        h("japanese", Cat.WORDS, "Японский язык", "Japanese",
            t("Карточки", "Flashcards", "sheet", REPEAT, "cream"), t("Словарь", "Dictionary", "book", LEARN, "indigo"),
            t("Прописи", "Kana workbook", "sheet", FIX, "sakura"), t("Чашка матча", "Matcha cup", "cup", NEW, "matcha"),
            t("Ночник", "Night lamp", "lantern", FINISH, "persimmon")),
        h("calligraphy", Cat.WORDS, "Каллиграфия", "Calligraphy",
            t("Кисть", "Brush", "brush", REPEAT, "cocoa"), t("Тушечница", "Inkstone", "stone", FIX, "slate"),
            t("Тушь", "Ink", "bottle", NEW, "indigo"), t("Бумага васи", "Washi paper", "sheet", FINISH, "cream"),
            t("Прописи", "Copybook", "book", LEARN, "plum")),
        h("writing", Cat.WORDS, "Писательство", "Writing",
            t("Ручка", "Pen", "pencil", REPEAT, "indigo"), t("Блокнот идей", "Idea notebook", "book", NEW, "mustard"),
            t("Черновик", "Draft", "sheet", FIX, "cream"), t("Рукопись", "Manuscript", "book", FINISH, "cocoa"),
            t("Кружка кофе", "Coffee mug", "cup", SHARE, "persimmon")),
        h("reading", Cat.WORDS, "Чтение", "Reading",
            t("Книга", "Book", "book", REPEAT, "wood"), t("Закладка", "Bookmark", "sheet", FINISH, "sakura"),
            t("Лампа", "Reading lamp", "lantern", LEARN, "mustard"), t("Плед", "Blanket", "mat", NEW, "plum"),
            t("Чай", "Tea", "cup", SHARE, "matcha")),

        h("chess", Cat.MIND, "Шахматы", "Chess",
            t("Конь", "Knight", "knight", NEW, "cream"), t("Шахматные часы", "Chess clock", "clock", REPEAT, "wood"),
            t("Сборник задач", "Puzzle book", "book", LEARN, "slate"), t("Тетрадь партий", "Game notebook", "sheet", FIX, "cream"),
            t("Доска", "Board", "box", FINISH, "cocoa")),
        h("go", Cat.MIND, "Го", "Go",
            t("Чаша с камнями", "Bowl of stones", "jar", REPEAT, "wood"), t("Камень", "Stone", "stone", NEW, "slate"),
            t("Сборник задач", "Problem book", "book", LEARN, "indigo"), t("Доска", "Board", "box", FINISH, "mustard")),
        h("cube", Cat.MIND, "Кубик и головоломки", "Puzzles",
            t("Кубик", "Cube", "box", REPEAT, "sky"), t("Таймер", "Timer", "clock", NEW, "persimmon"),
            t("Шпаргалка алгоритмов", "Algorithm sheet", "sheet", LEARN, "cream"), t("Мешочек головоломок", "Puzzle bag", "jar", FINISH, "plum")),
        h("code", Cat.MIND, "Программирование", "Programming",
            t("Клавиатура", "Keyboard", "box", REPEAT, "slate"), t("Резиновая уточка", "Rubber duck", "ball", FIX, "mustard"),
            t("Блокнот", "Notebook", "book", LEARN, "indigo"), t("Кружка", "Mug", "cup", SHARE, "cocoa")),

        h("yoga", Cat.MOVE, "Йога", "Yoga",
            t("Коврик", "Mat", "mat", REPEAT, "matcha"), t("Блок", "Block", "box", NEW, "sky"),
            t("Колокольчик", "Bell", "bell", LEARN, "mustard"), t("Бутылка воды", "Water bottle", "bottle", FINISH, "sky")),
        h("running", Cat.MOVE, "Бег", "Running",
            t("Кроссовки", "Running shoes", "box", REPEAT, "persimmon"), t("Часы", "Watch", "clock", NEW, "slate"),
            t("Бутылка воды", "Water bottle", "bottle", FINISH, "sky"), t("Дневник пробежек", "Run diary", "book", FIX, "plum")),
        h("climbing", Cat.MOVE, "Скалолазание", "Climbing",
            t("Мешочек магнезии", "Chalk bag", "jar", REPEAT, "cream"), t("Скальники", "Climbing shoes", "box", NEW, "persimmon"),
            t("Дневник трасс", "Route log", "book", FIX, "slate"), t("Эспандер", "Grip ball", "ball", LEARN, "plum")),
        h("dance", Cat.MOVE, "Танцы", "Dance",
            t("Туфли", "Dance shoes", "box", REPEAT, "plum"), t("Колонка", "Speaker", "bell", SHARE, "slate"),
            t("Тетрадь связок", "Combo notebook", "sheet", LEARN, "cream"), t("Бутылка воды", "Water bottle", "bottle", FINISH, "sky")),

        h("windowsill", Cat.GARDEN, "Сад на подоконнике", "Windowsill garden",
            t("Горшок", "Pot", "pot", REPEAT, "persimmon"), t("Лейка", "Watering can", "bottle", LEARN, "sky"),
            t("Пакетик семян", "Seed packet", "box", NEW, "mustard"), t("Камешек", "Pebble", "stone", FINISH, "slate")),
        h("bonsai", Cat.GARDEN, "Бонсай", "Bonsai",
            t("Деревце", "Little tree", "pot", REPEAT, "indigo"), t("Ножницы", "Shears", "pencil", FIX, "slate"),
            t("Книга о бонсай", "Bonsai book", "book", LEARN, "matcha"), t("Мох", "Moss stone", "stone", FINISH, "matcha")),
        h("garden", Cat.GARDEN, "Огород", "Vegetable patch",
            t("Лейка", "Watering can", "bottle", REPEAT, "matcha"), t("Семена", "Seeds", "box", NEW, "mustard"),
            t("Рассада", "Seedling", "pot", FINISH, "persimmon"), t("Дневник сада", "Garden diary", "book", LEARN, "wood")),

        h("pottery", Cat.BUILD, "Гончарство", "Pottery",
            t("Глина", "Clay", "stone", REPEAT, "cocoa"), t("Губка", "Sponge", "box", FIX, "mustard"),
            t("Глазурь", "Glaze", "bottle", NEW, "sky"), t("Чашка", "Cup", "cup", FINISH, "cream")),
        h("models", Cat.BUILD, "Модели", "Models",
            t("Коробка с деталями", "Box of parts", "box", REPEAT, "slate"), t("Клей", "Glue", "bottle", FIX, "mustard"),
            t("Кисточка", "Paintbrush", "brush", NEW, "plum"), t("Инструкция", "Instructions", "sheet", LEARN, "cream")),
        h("woodcarving", Cat.BUILD, "Резьба по дереву", "Woodcarving",
            t("Резец", "Knife", "pencil", REPEAT, "wood"), t("Брусок липы", "Basswood block", "box", NEW, "mustard"),
            t("Наждачка", "Sandpaper", "sheet", FIX, "cream"), t("Фигурка", "Figurine", "knight", FINISH, "wood"),
            t("Масло", "Finishing oil", "bottle", SHARE, "cocoa")),
    )

    private val byHobby = hobbies.associateBy { it.key }
    private val byThing = hobbies.flatMap { it.things }.associateBy { it.key }

    fun hobby(key: String?) = key?.let { byHobby[it] }
    fun thing(key: String?) = key?.let { byThing[it] }

    val PALS = listOf("wood", "indigo", "sakura", "matcha", "persimmon", "cream", "slate", "plum", "mustard", "sky", "cocoa")
    val ARCHS = listOf("pencil", "brush", "yarn", "book", "jar", "box", "guitar", "pick", "pot", "stone", "cup", "bottle", "knight", "sheet", "bell", "lantern", "spool", "camera", "mat", "ball", "clock")
}

/** Picks a silhouette, a colour and a favourite verb for something typed in. */
object Guess {
    private val ARCH = listOf(
        "guitar" to "гитар|укулел|скрип|балалай|бас|домр|мандолин|виолончел|арф|лютн|guitar|ukulele|violin|bass|banjo|mandolin|cello|harp|lute",
        "pick" to "медиатор|pick|plectrum",
        "brush" to "кист|перо|brush|quill",
        "pencil" to "карандаш|ручк|мел|уголь|пастел|спиц|крючок|палоч|флейт|стилус|маркер|линер|скалк|ложк|pencil|pen|chalk|charcoal|pastel|needle|hook|stick|flute|stylus|marker|liner|spoon|pin",
        "yarn" to "клуб|пряж|нитк|шерст|yarn|wool",
        "spool" to "катушк|мулине|плёнк|пленк|лент|spool|thread|floss|film|ribbon|tape",
        "book" to "книг|словар|учебник|тетрад|блокнот|альбом|скетчбук|ноты|песенник|сборник|дневник|book|dictionary|notebook|album|sketchbook|journal|diary",
        "jar" to "банк|закваск|тюбик|баноч|мёд|джем|магнез|чайник|чайниц|кофемолк|jar|starter|tube|honey|jam|chalk bag|teapot|caddy|grinder",
        "bottle" to "бутыл|масл|лак|флакон|чернил|тушь|клей|лейк|глазур|турк|bottle|oil|varnish|ink|glue|watering|glaze",
        "cup" to "чашк|кружк|чай|кофе|пиал|стакан|миск|cup|mug|tea|coffee|bowl|glass",
        "pot" to "горш|растен|кактус|рассад|семен|бонсай|цвет|фиалк|суккулент|pot|plant|cactus|seedling|bonsai|flower|succulent",
        "stone" to "камен|камеш|глин|галь|тушечниц|хлеб|каравай|булк|stone|pebble|clay|inkstone|bread|loaf",
        "knight" to "шахмат|фигур|конь|ферз|пешк|ладь|слон|chess|figure|knight|queen|pawn|rook|bishop",
        "sheet" to "лист|бумаг|карточ|открыт|холст|письм|пропис|схем|инструкц|paper|card|postcard|canvas|letter|chart|pattern|instructions|sheet",
        "bell" to "колокол|бубен|тарелк|поющ|колонк|bell|tambourine|cymbal|speaker",
        "lantern" to "фонар|ламп|свеч|ночник|lantern|lamp|candle|light",
        "camera" to "камер|фотоаппар|объектив|camera|lens",
        "mat" to "коврик|полотенц|шарф|плед|одеял|рулон|фартук|ткан|mat|towel|scarf|blanket|roll|apron|fabric",
        "ball" to "мяч|шар|волан|игольниц|ball|shuttlecock|pincushion",
        "clock" to "час|таймер|метроном|секундомер|счётчик|счетчик|весы|пяльц|компас|clock|watch|timer|metronome|stopwatch|counter|scale|hoop|compass",
        "box" to "коробк|набор|шкатул|ящик|пенал|кейс|ластик|губк|блок|доск|кубик|пазл|скальник|кроссов|туфл|клавиатур|планшет|box|kit|case|eraser|sponge|block|board|cube|puzzle|shoes|keyboard|tablet",
    ).map { (a, re) -> a to Regex("(?iu)(?<!\\p{L})($re)") }

    private val VERB = listOf(
        FIX to "ластик|распар|губк|корректор|дневник|весы|eraser|ripper|sponge|diary|scale",
        LEARN to "словар|книг|учебник|сборник|пропис|схем|инструкц|песенник|dictionary|book|manual|pattern|chart|instructions",
        SHARE to "открыт|кружк|чашк|колокол|шкатул|полотенц|postcard|mug|cup|bell|box|towel",
        FINISH to "альбом|скетчбук|холст|шарф|каравай|album|sketchbook|canvas|scarf|loaf",
        NEW to "медиатор|кист|камер|глазур|тушь|pick|brush|camera|glaze|ink",
    ).map { (v, re) -> v to Regex("(?iu)(?<!\\p{L})($re)") }

    data class Pick(val arch: String, val byWord: Boolean)

    fun arch(name: String): Pick {
        val s = name.trim()
        for ((a, re) in ARCH) if (re.containsMatchIn(s)) return Pick(a, true)
        return Pick(Catalog.ARCHS[stable(s) % Catalog.ARCHS.size], false)
    }

    fun verb(name: String): Verb {
        for ((v, re) in VERB) if (re.containsMatchIn(name)) return v
        return REPEAT
    }

    fun pal(name: String) = Catalog.PALS[stable(name) % Catalog.PALS.size]
    fun pers(name: String, hobby: String) = Pers.entries[stable(name + "·" + hobby) % Pers.entries.size]

    private fun stable(s: String): Int {
        var h = -0x7ee3623b
        for (ch in s.lowercase()) { h = h xor ch.code; h *= 16777619 }
        return h and 0x7FFFFFFF
    }
}
